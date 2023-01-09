package teleblock.util;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.NetworkUtils;
import com.github.gzuliyujiang.oaid.DeviceID;
import com.github.gzuliyujiang.oaid.IGetter;

import org.telegram.messenger.ApplicationLoader;


/**
 * Created by LSD on 2020/12/26.
 * Desc
 */
public class ADUtil {
    public static String ip = "49.64.196.165";
    public static String oaid = "";

    public static void init() {
        NetworkUtils.getIPAddressAsync(true, s -> {
            TGLog.debug("IP地址：" + s);
            ip = s;
        });
        DeviceID.getOAID(ApplicationLoader.applicationContext, new IGetter() {
            @Override
            public void onOAIDGetComplete(@NonNull String result) {
                oaid = result;
            }

            @Override
            public void onOAIDGetError(@NonNull Exception error) {
            }
        });
    }

    //在线广告配置
    public static void getAdConfig() {
//        String phone = UserConfig.getInstance(UserConfig.selectedAccount).getPhone();
//        if (!TextUtils.isEmpty(phone) && phone.length() >= 5) {
//            PaperUtil.onlineAdConfig(new ADEntity());
//            Api.adSwitch(phone.substring(0, 5), new NSCallback<ADSwitchEntity>(ApplicationLoader.applicationContext, ADSwitchEntity.class) {
//                @Override
//                public void onSuccess(ADSwitchEntity adEntity) {
//                    PaperUtil.adSwitch(adEntity);
//                }
//            });
//        }
    }

    // 是否有广告开启
    public static boolean hasADOpened() {
        return false;
//        return PaperUtil.adSwitch().hasADOpened();
    }

    //倒计时消失
    public static int adCountDownTime() {
        return 3;
    }

    //今天是否显示贴片广告
    public static boolean todayNeedShowPosterAd() {
        return false;
//        long lastTime = PaperUtil.lastPosterAdTime();
//        if (lastTime == 0) {
//            PaperUtil.lastPosterAdTime(System.currentTimeMillis());
//            PaperUtil.todayPosterAdCount(PaperUtil.todayPosterAdCount() + 1);
//            return true;
//        }
//        if (TimeUtils.isToday(lastTime)) {
//            if (PaperUtil.todayPosterAdCount() >= 6) {
//                return false;
//            } else {
//                PaperUtil.lastPosterAdTime(System.currentTimeMillis());
//                PaperUtil.todayPosterAdCount(PaperUtil.todayPosterAdCount() + 1);
//                return true;
//            }
//        } else {
//            PaperUtil.lastPosterAdTime(System.currentTimeMillis());
//            PaperUtil.todayPosterAdCount(0);
//            return true;
//        }
    }

    //第一个显示广告的位置
    public static int firstPosterADShowPosition() {
        return 1;
    }

    //广告间隔
    public static int getPosterADInterval() {
        return 5;
    }

    //channel广告间隔
    public static int getChannelADInterval() {
        return 5;
    }

    // 是否显示视频首页广告(每天出现5次)
    public static boolean showVideoPageAd() {
        return false;
//        long lastTime = PaperUtil.lastVideoHomeAdTime();
//        if (TimeUtils.isToday(lastTime)) {
//            if (PaperUtil.todayVideoHomeAdCount() >= 5) {
//                return false;
//            } else {
//                PaperUtil.lastVideoHomeAdTime(System.currentTimeMillis());
//                PaperUtil.todayVideoHomeAdCount(PaperUtil.todayVideoHomeAdCount() + 1);
//                if (PaperUtil.userInfo() != null && PaperUtil.userInfo().newer) return false;
//                return true;
//            }
//        } else {
//            PaperUtil.lastVideoHomeAdTime(System.currentTimeMillis());
//            PaperUtil.todayVideoHomeAdCount(0);
//            if (PaperUtil.userInfo() != null && PaperUtil.userInfo().newer) return false;
//            return true;
//        }
    }

    //聊天频道是否显示广告
    public static boolean showChatPageAd(long dialogId) {
//        long lastTime = PaperUtil.lastChatPageAdTime(dialogId);
//        if (lastTime == 0 || !TimeUtils.isToday(lastTime)) {
//            return true;
//        }
//        return false;
        return false;
    }

    //关闭广告
    public static void closeChatPageAd(long dialogId) {
//        PaperUtil.lastChatPageAdTime(dialogId, System.currentTimeMillis());
    }

    // 媒体详情页是否显示广告(每2次)
    public static boolean showMediaViewAd() {
        return false;
//        if (PaperUtil.mediaViewAdCount() >= 1) {
//            PaperUtil.mediaViewAdCount(0);
//            return true;
//        } else {
//            PaperUtil.mediaViewAdCount(PaperUtil.mediaViewAdCount() + 1);
//            return false;
//        }
    }

    // 文章页是否显示弹屏广告(每10次)
    public static boolean showArticleViewAd() {
        return false;
//        if (PaperUtil.articleViewAdCount() >= 9) {
//            PaperUtil.articleViewAdCount(0);
//            return true;
//        } else {
//            PaperUtil.articleViewAdCount(PaperUtil.articleViewAdCount() + 1);
//            return false;
//        }
    }
}
