package teleblock.util;

import android.content.Context;
import android.os.Bundle;

import com.flurry.android.FlurryAgent;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;

import java.util.HashMap;
import java.util.Map;

import teleblock.manager.LoginManager;
import teleblock.network.api.AppTrackApi;
import timber.log.Timber;

/**
 * Time:2022/7/6
 * Author:Perry
 * Description：数据埋点
 */
public class EventUtil {

    public enum Even {
        //启动
        第一次打开("first_open"),
        欢迎页展示("start_page_show"),
        欢迎页开始按钮点击("start_button_click"),
        欢迎页注册按钮点击("register_button_click"),
        电话号码页面展示("phone_number_show"),
        电话号码页面下一步("phone_number_next"),
        确认验证码页面展示("check_code_show"),
        Telegram新用户完善资料("telegram_new_user_info"),
        app聊天页面展示("chat_page_show"),

        //首页
        侧边无痕模式启用("no_read_open"),
        登录无痕模式启用("no_read_login_open"),
        聊天无痕模式启用("no_read_chat_open"),
        聊天翻译("translate_chat"),
        清除消息点击("numberclean_click"),
        搜索点击("home_search_click"),
        加号点击("topbar_more_click"),

        //工具栏
        好友点击("friends_message_click"),
        与我相关点击("aboutme_click"),
        非联系人点击("not_contact_click"),
        好工具点击("tool_click"),
        好工具清理缓存点击("tool_cleancache_click"),
        好工具扫一扫("tool_scan_click"),
        好工具邀请好友("tool_invite_click"),
        好工具消息分组("tool_folder_click"),
        好工具官方群("tool_official_group_click"),
        好工具官方频道("tool_official_channel_click"),
        信息分组加号点击("folder_add_click"),

        //主题
        主题页面热门展示("theme_page_hot_show"),
        主题页面最新展示("theme_page_new_show"),
        主题页面动态主题展示("theme_page_videotheme_show"),
        自定义主题入口按钮点击("theme_custom_btn_click"),
        主题页动态主题入口按钮点击("theme_videowallpaper_btn_click"),
        主题条目点击("theme_item_click"),
        动态主题条目点击("theme_video_item_apply"),
        主题套用成功("theme_apply_ok"),
        动态主题套用成功("theme_video_apply_ok"),
        动态主题自定义添加点击("theme_videowallpaper_add_click"),
        动态主题自定义添加应用("theme_videowallpaper_add_apply"),
        动态主题自定义推荐点击("theme_videowallpaper_recommend_click"),
        动态主题自定义推荐应用("theme_videowallpaper_recommend_apply"),

        //底部tab
        频道点击("channel_tab_click"),
        联系人点击("contacts_tab_click"),
        setting点击("settings_tab_click"),

        //频道tab
        频道页面展示("channel_tab_show"),
        频道点赞click("channel_tab_like_click"),
        频道转发click("channel_tab_send_click"),
        频道评论click("channel_tab_comment_click"),

        //好评
        好评弹窗展示("comment_dialog_show"),

        //表情包
        表情包收藏点击("sticker_collect"),
        表情收藏("sticker_collect"),
        GIF收藏("gif_collect"),
        表情包收藏界面展示("sticker_collect_page_show"),
        表情包输入框星星点击("sticker_collect_text_click"),
        表情包收藏界面发送("sticker_collect_page_send"),

        //更多功能
        更多功能按钮点击("function_more_btn_click"),
        更多功能按钮_联系我们点击("function_more_contactus_click"),
        更多功能按钮_官方群組点击("function_more_officialgroup_click"),
        更多功能按钮_清理缓存点击("function_more_cleancache_click"),
        更多功能按钮_主题市场点击("function_more_themestore_click"),
        更多功能按钮_动态壁纸点击("function_more_videowallpaper_click"),
        更多功能按钮_消息分组点击("function_more_messagefolder_click"),
        更多功能按钮_我的收藏点击("function_more_mycollect_click"),
        更多功能按钮_资源导航点击("function_more_telegramres_click"),
        更多功能按钮_下载管理点击("function_more_download_click"),
        更多功能按钮_清除未读消息("function_more_clean_unreadmsg_click"),

        //语言
        电话号码页语言选择("phonenumber_language_click"),
        侧边栏语言点击("menu_language_click"),

        //钱包首页
        资产页Tab点击("profile_tab_click"),
        资产页TT点击("profile_tt_click"),
        资产页Oasis点击("profile_oasis_click"),
        资产页Polygon点击("profile_polygon_click"),
        资产页Eth点击("profile_eth_click"),
        币圈页_群创建点击("group_home_group_create_click"),
        币圈页_群加入点击("group_home_group_join_click"),

        //设置页
        设置页_链接钱包("setting_connect_wallet_click"),

        //链接钱包页
        链接钱包_metamask点击("connect_wallet_metamask_click"),
        链接钱包_imToken点击("connect_wallet_imtoken_click"),
        链接钱包_tokenpocket点击("connect_wallet_tokenpocket_click"),
        metamask链接成功("metamask_connect_ok"),
        imToken链接成功("imtoken_connect_ok"),
        tokenpocket链接成功("tokenpocket_connect_ok"),

        //他人主页
        他人主页_转账给他("user_profile_transfer"),
        他人主页_查看钱包("user_profile_click"),
        ;

        public String eventId;

        Even(String eventId) {
            this.eventId = eventId;
        }
    }

    /**
     * 埋点
     *
     * @param context
     * @param data
     */
    public static void track(Context context, Even event, Map<String, Object> data) {
        try {
            if (data == null) data = new HashMap<>();
            if (!data.containsKey("uid")) {
                data.put("uid", LoginManager.getUserId());
                data.put("token", LoginManager.getUserToken() + "");
            }

            Timber.tag("TrackEvent").d(event.name() + "(" + event.eventId + ")" + " ->" + JsonUtil.parseObjToJson(data));

            //flurry
            Map<String, String> map = new HashMap<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                map.put(entry.getKey(), (String) entry.getValue());
            }
            FlurryAgent.logEvent(event.eventId, map);

            //firebase
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
            Bundle bundle = new Bundle();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                bundle.putString(entry.getKey(), (String) entry.getValue());
            }
            mFirebaseAnalytics.logEvent(event.eventId, bundle);

            //api
            String name = event.name();
            String key = event.eventId;
            String dataJson = new Gson().toJson(data);
            EasyHttp.post(new ApplicationLifecycle())
                    .api(new AppTrackApi()
                            .setKey(key)
                            .setName(name)
                            .setEvent_key(key)
                            .setEvent_name(name)
                            .setData(dataJson))
                    .request(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
