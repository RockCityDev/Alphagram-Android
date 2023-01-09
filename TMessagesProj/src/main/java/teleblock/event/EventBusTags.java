/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package teleblock.event;

/**
 * 放置 EventBus 的 Tag, 便于检索
 */
public interface EventBusTags {

    // 用户信息已更改
    String USER_INFO_CHANGED = "user_info_changed";
    // 无痕模式已更改
    String STEALTH_MODE_CHANGED = "stealth_mode_changed";
    // 更新未读数
    String UPDATE_UNREAD_COUNT = "update_unread_count";
    // 更新对话数据
    String UPDATE_DIALOGS_DATA = "update_dialogs_data";
    // 扫一扫
    String OPEN_CAMERA_SCAN = "open_camera_scan";
    // 清除缓存成功
    String CLEAR_CACHE_OK = "clear_cache_ok";
    // 预览主题
    String PRE_THEME_VIEW = "pre_theme_view";
    // 取消弹窗
    String DISMISS_DIALOG = "dismiss_dialog";
    // 显示弹窗
    String SHOW_DIALOG = "show_dialog";
    // 打开侧边栏
    String OPEN_DRAWER = "open_drawer";
    // 钱包连接已批准
    String WALLET_CONNECT_APPROVED = "wallet_connect_approved";
    // 钱包连接已断开
    String WALLET_CONNECT_CLOSED = "wallet_connect_closed";
    // 上传用户头像
    String UPLOAD_USER_PROFILE = "upload_user_profile";
    // 置顶会话
    String PIN_DIALOG = "pin_dialog";
    // 当前链已切换
    String CHAIN_TYPE_CHANGED = "chain_type_changed";

    String COLLECT_CHANGE = "collect_change";

    String DELETE_VIDEO_OK = "delete_video_ok";

    String DELETE_VIDEO_ITEM = "delete_video_item";

    String DELETE_VIDEO_SELECT = "delete_video_select";

    String VIDEO_SAVE_GALLERY = "video_save_gallery";

    String CHANNEL_WITH_TAG_REFRASH = "channel_with_tag_refrash";

    String CHANNEL_TAG_REFRASH = "channel_tag_refrash";

    //推荐筛选ids
    String RECOMMEND_IDS = "recommend_ids";

    //选择的文件路径
    String SELECTOR_FILE_PATH = "selector_file_path";

    //创建群成功
    String CRATE_NEW_GROUP = "crate_new_group";

    //修改群或者频道信息成功
    String EDIT_GC_DETAILS_SUCCESSFUL = "EDIT_GC_DETAILS_SUCCESSFUL";

    //主题切换
    String SYSTEM_CHANGE_THEME = "system_change_theme";
    String SHOW_CAMERA_FUNCTION = "show_camera_function";//显示扫码
    String CAMERA_SCAN_RESULT = "camera_scan_result";
    //清除被删除消息
    String DEL_DB_DELETE_MSG = "del_db_delete_msg";
    String STICKER_GIF_COLLECT_CHANGE = "sticker_gif_collect_change";

    //更新头像状态
    String UPDATE_AVATAR_STATE = "update_avatar_state";

    //获取配置数据成功
    String OBTION_BOT_SUCCESSFUL = "obtion_bot_successful";
}
