package teleblock.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.support.LongSparseIntArray;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.EmojiThemes;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.FilterCreateActivity;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.PrivacyControlActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import teleblock.database.KKVideoMessageDB;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.manager.DialogManager;
import teleblock.manager.LoginManager;
import teleblock.model.EvaluateAppEntity;
import teleblock.model.LoginDataResult;
import teleblock.model.SystemEntity;
import teleblock.model.wallet.WalletInfo;
import teleblock.network.BaseBean;
import teleblock.network.api.TgInfoApi;
import teleblock.network.api.TgUseridApi;
import teleblock.network.api.UnBindWalletApi;
import teleblock.network.api.UpdateNftStatusApi;
import timber.log.Timber;

/**
 * Time:2022/6/29
 * Author:Perry
 * Description：
 */
public class TelegramUtil {

    public static final int LocaleCN = 1;
    public static final int LocaleHK = 2;
    public static final int LocaleTW = 3;
    public static final int LocaleEN = 4;
    public static final int LocaleBAIN = 5;
    public static final int LocaleBAML = 6;
    public static final int LocalePOR = 7;
    public static final int LocaleESP = 8;

    /**
     * 设置语言
     */
    public static void loadAndSetLanguage(Activity activity) {
        if (activity == null) return;
        int select = MMKVUtil.loginSelectLanguage();
        if (select == -1) {
            return;
        }
        MMKVUtil.loginSelectLanguage(-1);
        LocaleController.LocaleInfo existingInfo = null;
        if (select == LocaleTW) {//台湾
            existingInfo = LocaleController.getInstance().getLanguageFromDict("zh_tw");
        } else if (select == LocaleHK) {//香港
            existingInfo = LocaleController.getInstance().getLanguageFromDict("zh_hk");
        } else if (select == LocaleCN) {//中文
            existingInfo = LocaleController.getInstance().getLanguageFromDict("zh_cn");
        } else if (select == LocaleEN) {//英文
            existingInfo = LocaleController.getInstance().getLanguageFromDict("en");
        } else if (select == LocaleBAIN) {//印尼
            existingInfo = LocaleController.getInstance().getLanguageFromDict("id");
        } else if (select == LocaleBAML) {//马来
            existingInfo = LocaleController.getInstance().getLanguageFromDict("ms");
        } else if (select == LocalePOR) {//葡萄牙
            existingInfo = LocaleController.getInstance().getLanguageFromDict("pt_br");
        } else if (select == LocaleESP) {//西班牙
            existingInfo = LocaleController.getInstance().getLanguageFromDict("es");
        }
        if (existingInfo != null) {
            LocaleController.LocaleInfo fExistingInfo = existingInfo;
            AndroidUtilities.runOnUIThread(() -> {
                LocaleController.getInstance().applyLanguage(fExistingInfo, true, false, false, true, UserConfig.selectedAccount,null);
                if (activity instanceof LaunchActivity) {
                    ((LaunchActivity) activity).rebuildAllFragments(true);
                }
            });
        }
    }

    /**
     * 应用隐私设置
     */
    public static void applyPrivacySettings() {
        if (MMKVUtil.disallowAllSeePhone() && !MMKVUtil.seePhoneApply()) {
            TLRPC.TL_account_setPrivacy req = new TLRPC.TL_account_setPrivacy();
            req.key = new TLRPC.TL_inputPrivacyKeyPhoneNumber();
            req.rules.add(new TLRPC.TL_inputPrivacyValueDisallowAll());
            ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                if (error == null) {
                    TLRPC.TL_account_privacyRules privacyRules = (TLRPC.TL_account_privacyRules) response;
                    ContactsController.getInstance(UserConfig.selectedAccount).setPrivacyRules(privacyRules.rules, PrivacyControlActivity.PRIVACY_RULES_TYPE_PHONE);
                    MMKVUtil.seePhoneApply(true);
                }
            }), ConnectionsManager.RequestFlagFailOnServerErrors);
        }
    }

    /**
     * 应用主题
     */
    public static void applyTheme(Theme.ThemeInfo themeInfo) {
        if (themeInfo.info != null) {
            if (!themeInfo.themeLoaded) {
                return;
            }
        }
        if (!TextUtils.isEmpty(themeInfo.assetName)) {
            Theme.PatternsLoader.createLoader(false);
        }

        SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("themeconfig", Activity.MODE_PRIVATE).edit();
        //editor.putString(themeInfo.isDark() ? "lastDarkTheme" : "lastDayTheme", themeInfo.getKey());
        editor.putString("lastDayTheme", themeInfo.getKey());
        editor.commit();

        if (themeInfo == Theme.getCurrentTheme()) {
            return;
        }
        NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.needSetDayNightTheme, themeInfo, false, null, -1);
        EmojiThemes.saveCustomTheme(themeInfo, themeInfo.currentAccentId);
    }

    /**
     * 保存货币数据
     */
    public static void saveCoinData() {
//        if (true) return; // 暂时屏蔽
        //登录请求
//        LoginManager.userLogin(() -> {
//            CoinGeckoManager.getInstance().getCoinList(new CoinGeckoManager.Callback<List<CoinList>>() {
//                @Override
//                public void onSuccess(List<CoinList> data) {
//                    MMKVUtil.setCoinList(data);
        //关键词返回
//                    EasyHttp.post(new ApplicationLifecycle())
//                            .api(new CurrencyKeywordsApi())
//                            .request(new OnHttpListener<BaseBean<List<String>>>() {
//                                @Override
//                                public void onSucceed(BaseBean<List<String>> result) {
//                                    new Thread(() -> {
//                                        List<String> keywords = new ArrayList<>();
//                                        for (String s : result.getData()) {
//                                            for (CoinList coinList : data) {
//                                                if (s.equalsIgnoreCase(coinList.getSymbol())) {
//                                                    keywords.add(s);
//                                                    break;
//                                                }
//                                            }
//                                        }
//                                        MMKVUtil.setCoinKeywords(keywords);
//                                    }).start();
//                                }
//
//                                @Override
//                                public void onFail(Exception e) {
//
//                                }
//                            });
//                }
//            });
//        }, null);
    }

    /**
     * 应用内评价
     */
    public static void handleEvaluateApp(Activity activity) {
        if (activity == null) return;
        if (MMKVUtil.isNeverEvaluate()) return;
        EvaluateAppEntity evaluateAppEntity = JsonUtil.parseJsonToBean(MMKVUtil.getEvaluateApp(), EvaluateAppEntity.class);
        if (evaluateAppEntity.isEvaluatedApp()) return;
        long openAppDate = evaluateAppEntity.getFirstOpenAppDate();
        if (openAppDate == 0) {
            evaluateAppEntity.setFirstOpenAppDate(TimeUtil.getTimerShort(TimeUtil.getNowTimestamp2ShortY()));
            MMKVUtil.setEvaluateApp(JsonUtil.parseObjToJson(evaluateAppEntity));
        } else {
            long nowDate = TimeUtil.getTimerShort(TimeUtil.getNowTimestamp2ShortY());
            if (nowDate != evaluateAppEntity.getCurrentDate()) {
                evaluateAppEntity.setCurrentDate(nowDate);
                evaluateAppEntity.setDayFirstTime(true);
            } else {
                evaluateAppEntity.setDayFirstTime(false);
            }
            MMKVUtil.setEvaluateApp(JsonUtil.parseObjToJson(evaluateAppEntity));
            if (!evaluateAppEntity.isDayFirstTime()) return;
            long days = TimeUtils.getTimeSpan(nowDate, openAppDate, TimeConstants.DAY);
            if (days == 3 || days == 5) {
                ReviewManager manager = ReviewManagerFactory.create(activity);
                Task<ReviewInfo> request = manager.requestReviewFlow();
                request.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (activity.isDestroyed()) return;
                        ReviewInfo reviewInfo = task.getResult();
                        Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                        flow.addOnCompleteListener(task1 -> {
                            EventUtil.track(activity, EventUtil.Even.好评弹窗展示, null);
                            evaluateAppEntity.setEvaluatedApp(true);
                            MMKVUtil.setEvaluateApp(JsonUtil.parseObjToJson(evaluateAppEntity));
                        });
                    }
                });
            } else if (days > 5) {
                MMKVUtil.setNeverEvaluate(true);
            }
        }
    }

    /**
     * 添加默认分组
     *
     * @param fragment
     */
    public static void addDefaultFilters(BaseFragment fragment) {
        ArrayList<MessagesController.DialogFilter> filters = fragment.getMessagesController().dialogFilters;
        // 只有全部对话时添加默认分组
        if (filters.size() == 1 && !MMKVUtil.addedDefaultFilters()) {
            AlertDialog progressDialog = new AlertDialog(fragment.getParentActivity(), 3);
            progressDialog.setCanCancel(false);
            progressDialog.show();
            new Thread(() -> {
                int[] flags = new int[]{
                        MessagesController.DIALOG_FILTER_FLAG_GROUPS,
                        MessagesController.DIALOG_FILTER_FLAG_CHANNELS,
                        MessagesController.DIALOG_FILTER_FLAG_CONTACTS,
                        MessagesController.DIALOG_FILTER_FLAG_NON_CONTACTS,
                        MessagesController.DIALOG_FILTER_FLAG_BOTS,
                        95,
                };
                String[] newFilters = new String[]{
                        LocaleController.getString("FilterGroupsNew", R.string.FilterGroupsNew),
                        LocaleController.getString("FilterChannelsNew", R.string.FilterChannelsNew),
                        LocaleController.getString("FilterContactsNew", R.string.FilterContactsNew),
                        LocaleController.getString("FilterNonContactsNew", R.string.FilterNonContactsNew),
                        LocaleController.getString("FilterBotsNew", R.string.FilterBotsNew),
                        LocaleController.getString("FilterUnreadNew", R.string.FilterUnreadNew)
                };
                for (int i = 0; i < newFilters.length; i++) {
                    String string = newFilters[i];
                    int filterFlags = 0;
                    filterFlags |= flags[i];

                    MessagesController.DialogFilter filter = new MessagesController.DialogFilter();
                    filter.id = 2;
                    while (MessagesController.getInstance(UserConfig.selectedAccount).dialogFiltersById.get(filter.id) != null) {
                        filter.id++;
                    }
                    filter.name = "";
                    int newFilterFlags = filterFlags;
                    String newFilterName = string;
                    ArrayList<Long> newAlwaysShow = new ArrayList<>(filter.alwaysShow);
                    ArrayList<Long> newNeverShow = new ArrayList<>(filter.neverShow);
                    LongSparseIntArray newPinned = filter.pinnedDialogs.clone();

                    final CountDownLatch countDownLatch = new CountDownLatch(1);
                    FilterCreateActivity.saveFilterToServer(filter, newFilterFlags, newFilterName, newAlwaysShow, newNeverShow, newPinned, true, false, false, true, false, fragment, new Runnable() {
                        @Override
                        public void run() {
                            countDownLatch.countDown();
                        }
                    });
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                AndroidUtilities.runOnUIThread(() -> {
                    progressDialog.dismiss();
                    MMKVUtil.addedDefaultFilters(true);
                    fragment.getNotificationCenter().postNotificationName(NotificationCenter.dialogFiltersUpdated);
                    MessagesStorage.getInstance(UserConfig.selectedAccount).saveDialogFiltersOrder();
                    TLRPC.TL_messages_updateDialogFiltersOrder req = new TLRPC.TL_messages_updateDialogFiltersOrder();
                    ArrayList<MessagesController.DialogFilter> dialogFilters = fragment.getMessagesController().dialogFilters;
                    for (int a = 0, N = dialogFilters.size(); a < N; a++) {
                        MessagesController.DialogFilter filter = dialogFilters.get(a);
                        req.order.add(filter.id);
                    }
                    fragment.getConnectionsManager().sendRequest(req, (response, error) -> {

                    });
                });
            }).start();
        }
    }

    // 取消自动夜间主题
    public static void cancelAutoNightTheme() {
        SharedPreferences.Editor editor = MessagesController.getGlobalMainSettings().edit();
        editor.putInt("selectedAutoNightType", Theme.AUTO_NIGHT_TYPE_NONE);
        editor.remove("nighttheme");
        editor.commit();
        Theme.selectedAutoNightType = Theme.AUTO_NIGHT_TYPE_NONE;
    }

    /**
     * 更新用户的tg数量
     */
    public static void updateTgInfo() {
        new Handler().postDelayed(() -> LoginManager.userLogin(() -> {
            EasyHttp.post(new ApplicationLifecycle())
                    .api(new TgInfoApi()
                            .setTg_group_num(DialogManager.getInstance(UserConfig.selectedAccount).groupCount)
                            .setTg_channel_num(DialogManager.getInstance(UserConfig.selectedAccount).channelCount)
                            .setTg_user_num(ContactsController.getInstance(UserConfig.selectedAccount).contacts.size() - 1)
                            .setTg_bot_num(DialogManager.getInstance(UserConfig.selectedAccount).botCount))
                    .request(null);
        }, null), 10000);
    }

    /***
     * 静默处理群邀请
     * @param fragment
     * @param inviteUrl
     */
    public static void silenceJoinChatInvite(BaseFragment fragment, String inviteUrl) {
        String hash = inviteUrl.replace("+", "").replace("https://t.me/", "");
        final TLRPC.TL_messages_importChatInvite reqImp = new TLRPC.TL_messages_importChatInvite();
        reqImp.hash = hash;
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(reqImp, (response, error) -> {
            if (error == null) {
                TLRPC.Updates updates = (TLRPC.Updates) response;
                MessagesController.getInstance(UserConfig.selectedAccount).processUpdates(updates, false);
            }
            AndroidUtilities.runOnUIThread(() -> {
                if (fragment == null || fragment.getParentActivity() == null) {
                    return;
                }
                if (error == null) {
                    TLRPC.Updates updates = (TLRPC.Updates) response;
                    if (!updates.chats.isEmpty()) {
                        TLRPC.Chat chat = updates.chats.get(0);
                        chat.left = false;
                        chat.kicked = false;
                        MessagesController.getInstance(UserConfig.selectedAccount).putUsers(updates.users, false);
                        MessagesController.getInstance(UserConfig.selectedAccount).putChats(updates.chats, false);
                    }
                } else {
                    //AlertsCreator.processError(UserConfig.selectedAccount, error, fragment, reqImp);
                    Timber.tag("TelegramUtil").e(error.text);
                }
            });
        }, ConnectionsManager.RequestFlagFailOnServerErrors);
    }

    /**
     * 获取机器人信息 从会调里面获取
     * @param systemEntity
     * @param runnable
     */
    public static void getBotInfo(SystemEntity systemEntity, Runnable runnable) {
        //先从本地获取看看有没有机器人缓存
        TLRPC.User localBot = MessagesController.getInstance(UserConfig.selectedAccount).getUser(Math.abs(systemEntity.bot_id));
        if (localBot != null) {
            runnable.run();
        } else {
            //从网络获取
            searchBotSaveToCache(systemEntity, runnable);
        }
    }

    /**
     * 搜索机器人并存储到缓存
     * @param systemEntity
     * @param runnable
     */
    private static void searchBotSaveToCache(SystemEntity systemEntity, Runnable runnable) {
        TLRPC.TL_contacts_search req = new TLRPC.TL_contacts_search();
        req.q = systemEntity.bot_nickname;
        req.limit = 20;
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                TLRPC.TL_contacts_found res = (TLRPC.TL_contacts_found) response;
                //机器人信息存储到缓存里面
                MessagesController.getInstance(UserConfig.selectedAccount).putChats(res.chats, false);
                MessagesController.getInstance(UserConfig.selectedAccount).putUsers(res.users, false);
                MessagesStorage.getInstance(UserConfig.selectedAccount).putUsersAndChats(res.users, res.chats, true, true);

                TLRPC.User bot = null;
                if (res.users != null) {
                    for (TLRPC.User user : res.users) {
                        if (systemEntity.bot_username.equals(user.username)) {
                            bot = user;
                            break;
                        }
                    }
                }

                if (bot != null) {
                    runnable.run();
                }
            }
        }));
    }

    /***
     * 添加机器人到群/设置管理员
     * @param baseFragment
     * @param chatId
     */
    public static void searchAddBotToGroup(BaseFragment baseFragment, long chatId, Runnable runnable) {
        //获取机器人信息
        getBotInfo(MMKVUtil.getSystemMsg(), () -> {
            TLRPC.User bot = MessagesController.getInstance(UserConfig.selectedAccount).getUser(Math.abs(MMKVUtil.getSystemMsg().bot_id));
            setBotToAdmin(baseFragment, chatId, bot, runnable);
        });
    }

    /**
     * 设置机器人为群管理员
     * @param baseFragment
     * @param chatId
     * @param bot
     * @param runnable
     */
    public static void setBotToAdmin(BaseFragment baseFragment, long chatId, TLRPC.User bot, Runnable runnable) {
        AndroidUtilities.runOnUIThread(() -> {
            TLRPC.TL_chatAdminRights adminRights = new TLRPC.TL_chatAdminRights();
            adminRights.change_info = true;
            adminRights.post_messages = true;
            adminRights.edit_messages = true;
            adminRights.delete_messages = true;
            adminRights.manage_call = true;
            adminRights.ban_users = true;
            adminRights.invite_users = true;
            adminRights.pin_messages = true;
            adminRights.other = true;
            MessagesController.getInstance(UserConfig.selectedAccount).setUserAdminRole(chatId, bot, adminRights, "", false, baseFragment, false, false, null, runnable::run, err -> false);
        }, 200);
    }

    /***
     * 添加用户到群
     * @param baseFragment
     * @param chatId
     * @param user
     * @param onFinishRunnable
     */
    public static void addUserToGroup(BaseFragment baseFragment, long chatId, TLRPC.User user, Runnable onFinishRunnable) {
        baseFragment.getMessagesController().addUserToChat(chatId, user, 100, null, baseFragment, onFinishRunnable);
    }


    /**
     * 获取用户nft数据封装
     */
    public interface UserNftDataListener {
        void nftDataRequestSuccessful(List<WalletInfo> walletInfoList);

        void nftDataRequestError(String errorMsg);
    }

    /**
     * 请求用户nft配置数据
     * @param tgUserId
     * @param listener
     */
    public static void getUserNftData(long tgUserId, @Nullable UserNftDataListener listener) {
        List<Long> userIdList = new ArrayList<>();
        userIdList.add(tgUserId);
        getUserNftData(userIdList, listener);
    }

    public static void getUserNftData(List<Long> userIdList, @Nullable UserNftDataListener listener) {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new TgUseridApi().setTg_user_id(userIdList))
                .request(new OnHttpListener<BaseBean<List<WalletInfo>>>() {
                    @Override
                    public void onSucceed(BaseBean<List<WalletInfo>> result) {
                        if (!CollectionUtils.isEmpty(result.getData())) {
                            for (WalletInfo walletInfo : result.getData()) {
                                //如果显示nft头像或者有绑定的nft头像则加入数据库
                                if (walletInfo.getIs_show_wallet() == 1 && walletInfo.getIs_bind_wallet() == 1) {
                                    boolean insertState = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).insertUserNftData(walletInfo);
                                    TGLog.info("userId NFT数据是否插入成功：" + insertState);
                                } else {
                                    boolean isDelete = KKVideoMessageDB.getInstance(UserConfig.selectedAccount).deleteUserNftData(walletInfo.getTg_user_id());
                                    TGLog.info("userId NFT数据是删除：" + isDelete);
                                }
                            }

                            if (listener != null) {
                                listener.nftDataRequestSuccessful(result.getData());
                            }
                        } else {
                            if (listener != null) {
                                listener.nftDataRequestError("not data");
                            }
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        if (listener != null) {
                            listener.nftDataRequestError(e.getMessage());
                        }
                    }
                });
    }


    /**
     * 更改用户nft状态
     * @param status  1=打开，0=关闭
     * @param runnable
     */
    public static void changeUserNftStatus(int status, Runnable runnable) {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new UpdateNftStatusApi().setStatus(status))
                .request(new OnHttpListener<BaseBean<LoginDataResult.UserEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<LoginDataResult.UserEntity> result) {
//                        LoginManager.saveUserInfo(result.getData());
                        MMKVUtil.setNftphotoIfShow(result.getData().getIs_show_wallet());

                        if (result.getData().getIs_show_wallet() == 1) { //用户打开了头像
                            //请求自己的nft数据，存到库里面
                            getUserNftData(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser().id, new UserNftDataListener() {
                                @Override
                                public void nftDataRequestSuccessful(List<WalletInfo> walletInfoList) {
                                    //发送通知更改头像
                                    EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_AVATAR_STATE));
                                }

                                @Override
                                public void nftDataRequestError(String errorMsg) {}
                            });
                        } else { //用户关闭了头像显示
                            boolean delete = KKVideoMessageDB.getInstance(UserConfig.selectedAccount)
                                    .deleteUserNftData(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser().id);
                            TGLog.info("用户nft数据是否删除：" + delete);
                            //发送通知更改头像
                            EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_AVATAR_STATE));
                        }
                        runnable.run();
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showShort(e.getMessage());
                    }
                });
    }

    /**
     * 解绑钱包
     * @param runnable
     */
    public static void unbindWallet(Runnable runnable) {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new UnBindWalletApi())
                .request(new OnHttpListener<BaseBean<LoginDataResult.UserEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<LoginDataResult.UserEntity> result) {
                        MMKVUtil.setNftphotoIfShow(0);//设置nft状态为0
                        boolean delete = KKVideoMessageDB.getInstance(UserConfig.selectedAccount)
                                .deleteUserNftData(UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser().id);
                        TGLog.info("用户nft数据是否删除：" + delete);
                        //发送通知更改头像
                        EventBus.getDefault().post(new MessageEvent(EventBusTags.UPDATE_AVATAR_STATE));
                        if (runnable != null) {
                            runnable.run();
                        }
                    }

                    @Override
                    public void onFail(Exception e) {
                        ToastUtils.showShort(e.getMessage());
                    }
                });
    }
}