package teleblock.config;


import com.blankj.utilcode.util.SizeUtils;

import org.telegram.messenger.BuildConfig;

/**
 * Time:2022/6/20
 * Author:Perry
 * Description：app配置信息
 */
public interface AppConfig {

    String LOG_TAG = "Telegram";

    boolean DEBUG = BuildConfig.DEBUG;

    int OS_TYPE = 2;
    String API_VERSION = "v1";

    //分享request
    int GROUP_SHARE_REQUEST = 9999;

    class NetworkConfig {
        //请求Host
        public static String API_BASE_URL = DEBUG ? "https://xxxx.app/api/" + API_VERSION : "https://xxxx.app/api/" + API_VERSION;
        //加密name
        public final static String WG_NAME = "alphagram";
        //加密KEY
        public final static String WG_KEY = "ERFEijdfyueysttg493j5346456432565943WCREDivideoldruere";
    }

    class ENCRYCONFIG {
        public final static byte[] key = "1tgvdAACES9KEY2".getBytes();
        public final static byte[] iv = "1stgEDGGSESl4IV2".getBytes();
    }

    class MkKey {
        //成员人数
        public final static String MEMBER_COUNTS = "member_counts";
        //设备号
        public final static String DEVICE_ID = "device_id";
        //区号
        public final static String COUNTRY_CODE = "country_code";
        //sim识别码
        public final static String SIM_CODE = "sim_code";
        //登录返回数据
        public final static String LOGIN_DATA = "login_data";
        //关键词
        public final static String COIN_KEYWORDS = "coin_Keywords";
        //货币数据
        public final static String COIN_LIST = "coin_list";
        //系统配置数据
        public final static String SYSTEM_MSG = "system_msg";
        //视频播放速度
        public final static String PLAY_SPEED = "play_speed";
        //最后一个播放的id
        public final static String LAST_PLAY_ID = "last_play_id";
        //最后一个视频来着dialog
        public final static String LAST_PLAY_ITEM_DIALOGID = "last_play_item_dialogid";
        //最后一个视频的发布时间
        public final static String LAST_PLAY_ITEM_TIME = "last_play_item_time";
        //是否显示用户评价弹窗
        public final static String NEVER_EVALUATE = "never_evaluate";
        //app跳转google商店评论显示配置数据
        public final static String EVALUATE_APP_ENTITY = "evaluate_app_entity";
        //是否显示清除未读消息弹窗
        public final static String SHOW_CLEAR_UNREADMSG = "show_clear_unreadmsg";
        //上次登录的tgid
        public final static String LAST_LOGIN_TGID = "last_login_tgid";
        //是否开启置顶列表按钮
        public final static String IF_OPEN_TOPPING = "if_open_topping";
        //是否开启所有归档列表按钮
        public final static String IF_OPEN_ARCHIVE = "if_open_archive";
        //是否开启群组人数筛选按钮
        public final static String IF_OPEN_PEOPLE_FILTER = "if_open_people_filter";
        //群组人数筛选-默认30人
        public final static String PEOPLE_FILTER_NUM = "people_filter_num";
        //黑名单会话ID
        public final static String BLACK_CHAT_ID_LIST = "black_chat_id_list";
        //白名单会话ID
        public final static String WHITE_CHAT_ID_LIST = "white_chat_id_list";
        //钱包配置数据
        public final static String WALLET_CONFIG_DATA = "wallet_config_data";

        //nft头像是否显示
        public final static String NFTPHOTO_IF_SHOW = "nftphoto_if_show";
        //群组分享的图片地址
        public final static String GROUP_SHARE_IMG_URL = "group_share_img_url";
        //群组分享的邀请链接
        public final static String GROUP_SHARE_INVITER_URL = "group_share_inviter_url";

        //是否添加过机器人
        public final static String IF_ADD_BOT = "if_add_bot";
    }


    //广告UNIT_ID支持服务器配置，本地缓存
    class AD_UNIT {
        //视频流原生广告
        public static String SLIP_FEED_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110";
        //channel信息流原生广告
        public static String CHANNEL_FEED_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110";
        //底部banner广告
        public static String BOTTOM_BANNER_ID = "ca-app-pub-3940256099942544/6300978111";
        //通用banner广告
        public static String COMMON_BANNER_ID = "ca-app-pub-3940256099942544/6300978111";
    }

    class HomeTab {
        public static final int TAB_CHAT = 0;
        public static final int TAB_VIDEO = 1;
        public static final int TAB_CHANNEL = 2;
        public static final int TAB_CONTACT = 3;
        public static final int TAB_MY = 4;
    }

    static void init() {
        if (DEBUG) {//forTest
        }
    }

    class ViewConfig {
        //头像右上角 币种icon缩放比例
        public static final float COIN_ICON_SCALING = 0.265f;

        //币种icon图标 margintop的比例
        public static final float COIN_ICON_MAGIN_TOP = 0.1f;

        //币种icon图标 marginleft的比例
        public static final float COIN_ICON_MAGIN_LEFT = 0.4f;

        //首页聊天列表nft尺寸
        public static final int HOME_CHAT_AVATAR_SIZE = SizeUtils.dp2px(60f);

        //首页聊天列表nft尺寸
        public static final int GROUP_CHAT_AVATAR_SIZE = SizeUtils.dp2px(50f);
    }
}
