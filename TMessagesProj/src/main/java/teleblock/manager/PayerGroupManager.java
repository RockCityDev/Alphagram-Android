package teleblock.manager;

import android.app.Activity;
import android.os.Bundle;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.MapUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.telegram.messenger.BaseController;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.LaunchActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import teleblock.model.OrderResultEntity;
import teleblock.model.PrivateGroupEntity;
import teleblock.model.ShopInfoEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.OrderResultApi;
import teleblock.network.api.ShopInfoApi;
import teleblock.ui.dialog.GroupPayJoinDialog;
import teleblock.ui.dialog.GroupValidateJoinDialog;
import teleblock.util.TelegramUtil;
import timber.log.Timber;

/**
 * 创建日期：2022/9/5
 * 描述：付费群管理
 */
public class PayerGroupManager extends BaseController {

    private static volatile PayerGroupManager[] Instance = new PayerGroupManager[UserConfig.MAX_ACCOUNT_COUNT];

    private Map<String, Integer> txHashMap = new HashMap<>();
    private Timer timer;
    private TimerTask task;
    private boolean running;

    public static PayerGroupManager getInstance(int num) {
        PayerGroupManager localInstance = Instance[num];
        if (localInstance == null) {
            synchronized (PayerGroupManager.class) {
                localInstance = Instance[num];
                if (localInstance == null) {
                    Instance[num] = localInstance = new PayerGroupManager(num);
                }
            }
        }
        return localInstance;
    }

    public PayerGroupManager(int num) {
        super(num);
        timer = new Timer();
        initTimerTask();
    }

    private void initTimerTask() {
        task = new TimerTask() {
            @Override
            public void run() {
                running = true;
                executeTask();
            }
        };
    }

    public void executeTask() {
        MapUtils.forAllDo(txHashMap, new MapUtils.Closure<String, Integer>() {
            @Override
            public void execute(String key, Integer value) {
                int count = --value;
                if (count == 0) {
                    removeTxHash(key);
                    return;
                }
                EasyHttp.post(new ApplicationLifecycle())
                        .api(new OrderResultApi()
                                .setTx_hash(key)
                        ).request(new OnHttpListener<BaseBean<OrderResultEntity>>() {

                            @Override
                            public void onSucceed(BaseBean<OrderResultEntity> result) {
                                txHashMap.put(key, count);
                                if (result.getData().ship != null) {
                                    Activity activity = ActivityUtils.getTopActivity();
                                    if (activity instanceof LaunchActivity) {
                                        LaunchActivity launchActivity = (LaunchActivity) activity;
                                        BaseFragment fragment = launchActivity.getActionBarLayout().getLastFragment();
                                        TelegramUtil.silenceJoinChatInvite(fragment, result.getData().ship.url);
                                        removeTxHash(result.getData().tx_hash);
                                    }
                                }
                            }

                            @Override
                            public void onFail(Exception e) {
                                Timber.e(e);
                                removeTxHash(key);
                            }
                        });
            }
        });
    }

    public void addTxHash(String s) {
        txHashMap.put(s, 12);
        if (running) return;
        initTimerTask();
        timer.schedule(task, 10000, 5000);
    }

    public void removeTxHash(String s) {
        txHashMap.remove(s);
        if (txHashMap.isEmpty()) {
            running = false;
            task.cancel();
        }
    }

    public void handleShopInfo(BaseFragment fragment, PrivateGroupEntity privateGroup) {
        AlertDialog progressDialog = new AlertDialog(fragment.getParentActivity(), 3);
        progressDialog.setCanCancel(false);
        progressDialog.show();
        EasyHttp.post(new ApplicationLifecycle())
                .api(new ShopInfoApi()
                        .setGroup_id(privateGroup.getId()))
                .request(new OnHttpListener<BaseBean<ShopInfoEntity>>() {
                    @Override
                    public void onSucceed(BaseBean<ShopInfoEntity> result) {
                        progressDialog.dismiss();
                        ShopInfoEntity shopInfo = result.getData();
                        TLRPC.Chat chat = getMessagesController().getChat(Math.abs(shopInfo.chat_id));
                        if (!ChatObject.isNotInChat(chat)) {
                            Bundle args = new Bundle();
                            args.putLong("chat_id", chat.id);
                            fragment.presentFragment(new ChatActivity(args));
                        } else {
                            if (!CollectionUtils.isEmpty(shopInfo.order_info)) {
                                ShopInfoEntity.OrderInfoEntity orderInfo = CollectionUtils.find(shopInfo.order_info, new CollectionUtils.Predicate<ShopInfoEntity.OrderInfoEntity>() {
                                    @Override
                                    public boolean evaluate(ShopInfoEntity.OrderInfoEntity item) {
                                        return item.ship != null;
                                    }
                                });
                                if (orderInfo != null) {
                                    Browser.openUrl(fragment.getParentActivity(), orderInfo.ship.url);
                                    if (privateGroup.getJoin_type() == 3) {
                                        removeTxHash(orderInfo.tx_hash);
                                    }
                                } else if (privateGroup.getJoin_type() == 3) {
                                    ToastUtils.showLong(LocaleController.getString("vip_group_wating", R.string.vip_group_wating));
                                }
                            } else if (privateGroup.getJoin_type() == 1) {
                                Browser.openUrl(fragment.getParentActivity(), (result.getData().chat_link));
                            } else if (privateGroup.getJoin_type() == 2) {
                                new GroupValidateJoinDialog(fragment, privateGroup).show();
                            } else if (privateGroup.getJoin_type() == 3) {
                                privateGroup.setDecimal(shopInfo.decimal);
                                new GroupPayJoinDialog(fragment, privateGroup).show();
                            }
                        }

                    }

                    @Override
                    public void onFail(Exception e) {
                        progressDialog.dismiss();
                        ToastUtils.showLong(e.getMessage());
                    }
                });

    }
}