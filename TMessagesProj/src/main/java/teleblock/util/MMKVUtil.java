package teleblock.util;

import android.text.TextUtils;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.coingecko.domain.Coins.CoinList;
import com.tencent.mmkv.MMKV;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.walletconnect.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import teleblock.config.AppConfig;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.SystemEntity;
import teleblock.model.VideoApplyEntity;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.model.wallet.NFTInfo;
import teleblock.model.wallet.TTToken;
import teleblock.model.wallet.WalletInfo;

public class MMKVUtil {

    private static MMKV kv;

    public static String getKeyWithUser(String key) {
        StringBuilder stringBuilder = new StringBuilder(key);
        TLRPC.User currentUser = UserConfig.getInstance(UserConfig.selectedAccount).getCurrentUser();
        if (currentUser != null && !TextUtils.isEmpty(currentUser.phone)) {
            stringBuilder.append(currentUser.phone);
        }
        return stringBuilder.toString();
    }

    private static MMKV getMMKV() {
        if (kv == null) {
            kv = MMKV.defaultMMKV();
        }
        return kv;
    }

    public static Boolean saveValue(String key, Object value) {
        if (value instanceof String) {
            return getMMKV().encode(key, (String) value);
        } else if (value instanceof Integer) {
            return getMMKV().encode(key, (Integer) value);
        } else if (value instanceof Float) {
            return getMMKV().encode(key, (Float) value);
        } else if (value instanceof Boolean) {
            return getMMKV().encode(key, (Boolean) value);
        } else if (value instanceof Long) {
            return getMMKV().encode(key, (Long) value);
        } else {
            return getMMKV().encode(key, value.toString());
        }
    }

    public static String getString(String key) {
        return getMMKV().decodeString(key, "");
    }

    public static Integer getInt(String key) {
        return getMMKV().decodeInt(key, 0);
    }

    public static Float getFloat(String key) {
        return getMMKV().decodeFloat(key, 0.0f);
    }

    public static Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static Boolean getBoolean(String key, boolean defaultValue) {
        return getMMKV().decodeBool(key, defaultValue);
    }

    public static Long getLong(String key) {
        return getMMKV().decodeLong(key, 0L);
    }

    public static void removeValue(String key) {
        getMMKV().removeValueForKey(key);
    }

    public static void clearAll() {
        getMMKV().clearAll();
    }


    /**********************************************************************************************/

    /**
     * ??????????????????
     *
     * @param map
     */
    public static void setMemberCounts(Map<Long, Integer> map) {
        saveValue(getKeyWithUser(AppConfig.MkKey.MEMBER_COUNTS), JsonUtil.parseObjToJson(map));
    }

    public static Map<Long, Integer> getMemberCounts() {
        String json = getString(getKeyWithUser(AppConfig.MkKey.MEMBER_COUNTS));
        return JsonUtil.parseJsonToMap(json, Long.class, Integer.class);
    }

    /**
     * ????????????????????????
     *
     * @param data
     */
    public static void setSystemMsg(SystemEntity data) {
        saveValue(AppConfig.MkKey.SYSTEM_MSG, JsonUtil.parseObjToJson(data));
    }

    public static SystemEntity getSystemMsg() {
        String json = getString(AppConfig.MkKey.SYSTEM_MSG);
        return JsonUtil.parseJsonToBean(json, SystemEntity.class);
    }

    /**
     * ????????????????????????
     *
     * @param speed
     */
    public static void setPlaySpeed(float speed) {
        saveValue(AppConfig.MkKey.PLAY_SPEED, speed);
    }

    /**
     * ????????????????????????
     */
    public static Float getPlaySpeed() {
        float speed = getFloat(AppConfig.MkKey.PLAY_SPEED);
        if (speed == 0.0f) {
            speed = 1.0f;
        }
        return speed;
    }

    /**
     * ?????????????????????id
     *
     * @param dialogId
     * @param id
     */
    public static void setLastPlayId(long dialogId, int id) {
        saveValue(AppConfig.MkKey.LAST_PLAY_ID, id);
    }

    public static int getLastPlayId(long dialogId) {
        return getInt(AppConfig.MkKey.LAST_PLAY_ID);
    }

    /**
     * ????????????????????????dialog
     *
     * @param currentDialog
     * @param dialogId
     */
    public static void setLastPlayItemDialogId(long currentDialog, long dialogId) {
        saveValue(AppConfig.MkKey.LAST_PLAY_ITEM_DIALOGID, dialogId);
    }

    public static long getLastPlayItemDialogId(long currentDialog) {
        return getLong(AppConfig.MkKey.LAST_PLAY_ITEM_DIALOGID);
    }

    public static void startRegister(boolean b) {
        saveValue("startRegister", b);
    }

    public static boolean startRegister() {
        return getBoolean("startRegister");
    }

    /**
     * firstLogin
     *
     * @param login
     */
    public static void firstLogin(boolean login) {
        saveValue("firstLogin", login);
    }

    public static boolean firstLogin() {
        return getBoolean("firstLogin", true);
    }

    public static void firstLoad(boolean login) {
        saveValue("firstLoad", login);
    }

    public static boolean firstLoad() {
        return getBoolean("firstLoad", true);
    }

    /***
     * ???????????????????????????????????????
     * @param set
     */
    public static void disallowAllSeePhone(boolean set) {
        saveValue(getKeyWithUser("disallowAllSeePhone"), set);
    }

    public static boolean disallowAllSeePhone() {
        return getMMKV().decodeBool(getKeyWithUser("disallowAllSeePhone"), true);
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @param b
     */
    public static void seePhoneApply(boolean b) {
        saveValue(getKeyWithUser("seePhoneApply"), b);
    }

    public static boolean seePhoneApply() {
        return getBoolean(getKeyWithUser("seePhoneApply"));
    }

    public static void setTranslateCode(String TranslateCode) {
        saveValue("TranslateCode", TranslateCode);
    }

    public static String getTranslateCode() {
        return getString("TranslateCode");
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    public static boolean isOpenStealthMode() {
        return getMMKV().decodeBool(getKeyWithUser("StealthMode"));
    }

    public static void setStealthMode(boolean stealthMode) {
        saveValue(getKeyWithUser("StealthMode"), stealthMode);
    }

    /**
     * ??????tg?????????
     *
     * @return
     */
    public static boolean telegramNewUser() {
        return getBoolean(getKeyWithUser("telegramNewUser"));
    }

    public static void telegramNewUser(boolean stealthMode) {
        saveValue(getKeyWithUser("telegramNewUser"), stealthMode);
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public static int loginSelectLanguage() {
        return getMMKV().decodeInt("loginSelectLanguage", -1);
    }

    public static void loginSelectLanguage(int value) {
        saveValue("loginSelectLanguage", value);
    }

    /**
     * ?????????????????????????????????
     *
     * @param dialogId
     * @param time
     */
    public static void setLastPlayItemTime(long dialogId, long time) {
        saveValue(AppConfig.MkKey.LAST_PLAY_ITEM_TIME, time);
    }

    public static long getLastPlayItemTime(long dialogId) {
        return getLong(AppConfig.MkKey.LAST_PLAY_ITEM_TIME);
    }

    /**
     * ????????????????????????
     *
     * @return
     */
    public static boolean isNeverEvaluate() {
        return getBoolean(AppConfig.MkKey.NEVER_EVALUATE);
    }

    public static void setNeverEvaluate(boolean neverEvaluate) {
        saveValue(AppConfig.MkKey.NEVER_EVALUATE, neverEvaluate);
    }

    /**
     * app??????google??????????????????????????????
     *
     * @param evaluateApp
     */
    public static void setEvaluateApp(String evaluateApp) {
        saveValue(AppConfig.MkKey.EVALUATE_APP_ENTITY, evaluateApp);
    }

    public static String getEvaluateApp() {
        String json = getString(AppConfig.MkKey.EVALUATE_APP_ENTITY);
        if (json.isEmpty()) {
            return "{}";
        }
        return json;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public static boolean chatTranslationSwitch() {
        return getMMKV().decodeBool(getKeyWithUser("chatTranslationSwitch"), true);
    }

    public static void chatTranslationSwitch(boolean mSwitch) {
        saveValue(getKeyWithUser("chatTranslationSwitch"), mSwitch);
    }

    /**
     * ???????????????????????????
     */
    public static boolean addedDefaultFilters() {
        return getBoolean(getKeyWithUser("addedDefaultFilters"));
    }

    public static void addedDefaultFilters(boolean b) {
        saveValue(getKeyWithUser("addedDefaultFilters"), b);
    }

    /**
     * ?????????????????????
     */
    public static String connectedWalletAddress() {
        return getString(getKeyWithUser("connectedWalletAddress"));
    }

    public static void connectedWalletAddress(String s) {
        saveValue(getKeyWithUser("connectedWalletAddress"), s);
    }

    /**
     * ?????????????????????
     */
    public static String connectedWalletPkg() {
        return getString(getKeyWithUser("connectedWalletPkg"));
    }

    public static void connectedWalletPkg(String s) {
        saveValue(getKeyWithUser("connectedWalletPkg"), s);
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    public static WalletNetworkConfigEntity.WalletNetworkConfigChainType currentChainConfig() {
        String json = getString(getKeyWithUser("currentChainConfig"));
        if (TextUtils.isEmpty(json)) { // ?????????????????????
            List<WalletNetworkConfigEntity.WalletNetworkConfigChainType> chainTypes = MMKVUtil.getWalletNetworkConfigEntity().getChainType();
            if (!CollectionUtils.isEmpty(chainTypes)) {
                WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType = chainTypes.get(0);
                saveValue(getKeyWithUser("currentChainConfig"), json = JsonUtil.parseObjToJson(chainType));
            }
        }
        return JsonUtil.parseJsonToBean(json, WalletNetworkConfigEntity.WalletNetworkConfigChainType.class);
    }

    public static void currentChainConfig(WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType) {
        saveValue(getKeyWithUser("currentChainConfig"), JsonUtil.parseObjToJson(chainType));
        EventBus.getDefault().post(new MessageEvent(EventBusTags.CHAIN_TYPE_CHANGED));
    }

    /**
     * ??????????????????
     */
    public static boolean ifOpenTopping() {
        return getBoolean(getKeyWithUser(AppConfig.MkKey.IF_OPEN_TOPPING), true);
    }

    public static void setIfOpenTopping(boolean b) {
        saveValue(getKeyWithUser(AppConfig.MkKey.IF_OPEN_TOPPING), b);
    }

    /**
     * ????????????????????????
     */
    public static boolean ifOpenArchive() {
        return getBoolean(getKeyWithUser(AppConfig.MkKey.IF_OPEN_ARCHIVE), true);
    }

    public static void setIfOpenArchive(boolean b) {
        saveValue(getKeyWithUser(AppConfig.MkKey.IF_OPEN_ARCHIVE), b);
    }

    /**
     * ?????????????????????
     */
    public static boolean ifOpenPeopleFilter() {
        return getBoolean(getKeyWithUser(AppConfig.MkKey.IF_OPEN_PEOPLE_FILTER), true);
    }

    public static void setIfOpenPeopleFilter(boolean b) {
        saveValue(getKeyWithUser(AppConfig.MkKey.IF_OPEN_PEOPLE_FILTER), b);
    }

    //applyVideoWallpaper
    public static void applyVideoWallpaper(VideoApplyEntity entity) {
        saveValue("applyVideoWallpaper", JsonUtil.parseObjToJson(entity));
    }

    public static VideoApplyEntity applyVideoWallpaper() {
        String json = getString("applyVideoWallpaper");
        if (!TextUtils.isEmpty(json)) {
            return JsonUtil.parseJsonToBean(json, VideoApplyEntity.class);
        }
        return null;
    }

    public static void removeVideoWallpaper() {
        getMMKV().removeValueForKey("applyVideoWallpaper");
    }

    /**
     * ???????????????
     */
    public static int groupFilterPeopleNum() {
        int num = getInt(getKeyWithUser(AppConfig.MkKey.PEOPLE_FILTER_NUM));
        if (num != 0) {
            return num;
        } else {
            return 30;
        }
    }

    public static void setGroupFilterPeopleNum(int num) {
        saveValue(getKeyWithUser(AppConfig.MkKey.PEOPLE_FILTER_NUM), num);
    }

    /**
     * ???????????????
     *
     * @return
     */
    public static List<Long> blackChatList() {
        String json = getString(getKeyWithUser(AppConfig.MkKey.BLACK_CHAT_ID_LIST));
        if (json.isEmpty()) {
            return new ArrayList<>();
        } else {
            return JsonUtil.parseJsonToList(json, Long.class);
        }
    }

    public static void setBlackChatList(List<Long> chatIds) {
        saveValue(getKeyWithUser(AppConfig.MkKey.BLACK_CHAT_ID_LIST), JsonUtil.parseObjToJson(chatIds));
    }

    /**
     * ???????????????
     */
    public static List<Long> whiteChatList() {
        String json = getString(getKeyWithUser(AppConfig.MkKey.WHITE_CHAT_ID_LIST));
        if (json.isEmpty()) {
            return new ArrayList<>();
        } else {
            return JsonUtil.parseJsonToList(json, Long.class);
        }
    }

    public static void setWhiteChatList(List<Long> chatIds) {
        saveValue(getKeyWithUser(AppConfig.MkKey.WHITE_CHAT_ID_LIST), JsonUtil.parseObjToJson(chatIds));
    }

//    /**
//     * ??????????????????
//     */
//    public static boolean setWalletInfo(WalletInfo data) {
//        return saveValue(getKeyWithUser("walletInfo"), JsonUtil.parseObjToJson(data));
//    }
//
//    public static WalletInfo getWalletInfo() {
//        String json = getString(getKeyWithUser("walletInfo"));
//        return JsonUtil.parseJsonToBean(json, WalletInfo.class);
//    }

    /**
     * ??????????????????
     */
    public static void sessionConfig(Session.Config config) {
        saveValue(getKeyWithUser("sessionConfig"), JsonUtil.parseObjToJson(config));
    }

    public static Session.Config sessionConfig() {
        String json = getString(getKeyWithUser("sessionConfig"));
        return JsonUtil.parseJsonToBean(json, Session.Config.class);
    }

    //????????????????????????
    public static void deleteMessageSwitch(boolean mSwitch) {
        saveValue(getKeyWithUser("deleteMessageSwitch"), mSwitch);
    }

    public static boolean deleteMessageSwitch() {
        return getBoolean(getKeyWithUser("deleteMessageSwitch"), false);
    }

    /**
     * ???????????????
     */
    public static void setCoinKeywords(List<String> keywords) {
        saveValue(AppConfig.MkKey.COIN_KEYWORDS, JsonUtil.parseObjToJson(keywords));
    }

    public static List<String> getCoinKeywords() {
        String json = getString(AppConfig.MkKey.COIN_KEYWORDS);
        if (json.isEmpty()) {
            return new ArrayList<>();
        } else {
            return JsonUtil.parseJsonToList(json, String.class);
        }
    }

    /**
     * ??????????????????
     */
    public static void setCoinList(List<CoinList> keywords) {
        saveValue(AppConfig.MkKey.COIN_LIST, JsonUtil.parseObjToJson(keywords));
    }

    public static List<CoinList> getCoinList() {
        String json = getString(AppConfig.MkKey.COIN_LIST);
        if (json.isEmpty()) {
            return new ArrayList<>();
        } else {
            return JsonUtil.parseJsonToList(json, CoinList.class);
        }
    }

    /**
     * ??????????????? ???????????????
     */
    public static WalletNetworkConfigEntity getWalletNetworkConfigEntity() {
        String json = getString(AppConfig.MkKey.WALLET_CONFIG_DATA);
        if (json.isEmpty()) {
            return new WalletNetworkConfigEntity();
        } else {
            return JsonUtil.parseJsonToBean(json, WalletNetworkConfigEntity.class);
        }
    }

    public static void setWalletNetworkConfigData(WalletNetworkConfigEntity walletNetworkConfig) {
        saveValue(AppConfig.MkKey.WALLET_CONFIG_DATA, JsonUtil.parseObjToJson(walletNetworkConfig));
    }

    /**
     * ????????????nft??????
     */
    public static boolean getNftphotoIfShow() {
        int num = getInt(getKeyWithUser(AppConfig.MkKey.NFTPHOTO_IF_SHOW));
        return num == 1;
    }

    public static void setNftphotoIfShow(int num) {
        saveValue(getKeyWithUser(AppConfig.MkKey.NFTPHOTO_IF_SHOW), num);
    }

    /**
     * TT?????????????????????
     */
    public static void setTTTokens() {
        saveValue("TTTokens", ResourceUtils.readAssets2String("blockchain/TTTokens.json"));
    }

    public static List<TTToken> getTTTokens() {
        String json = getString("TTTokens");
        return JsonUtil.parseJsonToList(json, TTToken.class);
    }

    /**
     * ??????????????????????????????url
     */
    public static void setGroupShareImgPath(String path) {
        MMKVUtil.saveValue(AppConfig.MkKey.GROUP_SHARE_IMG_URL, path);
    }

    public static String getGroupShareImgPath() {
        return MMKVUtil.getString(AppConfig.MkKey.GROUP_SHARE_IMG_URL);
    }

    /**
     * ???????????????????????????????????????
     * @param path
     */
    public static void setGroupShareInviterImgPath(String path) {
        MMKVUtil.saveValue(AppConfig.MkKey.GROUP_SHARE_INVITER_URL, path);
    }

    public static String getGroupShareInviterImgPath() {
        return MMKVUtil.getString(AppConfig.MkKey.GROUP_SHARE_INVITER_URL);
    }

    /**
     * ????????????????????????????????????
     * @param ifAddBot
     */
    public static void setIfAddBot(boolean ifAddBot) {
        MMKVUtil.saveValue(AppConfig.MkKey.IF_ADD_BOT, ifAddBot);
    }

    public static boolean getIfAddBot() {
        return MMKVUtil.getBoolean(AppConfig.MkKey.IF_ADD_BOT, false);
    }

}
