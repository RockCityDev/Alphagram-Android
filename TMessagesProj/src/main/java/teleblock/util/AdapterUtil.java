package teleblock.util;

import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;

import teleblock.model.HotRecommendData;
import teleblock.network.api.RecommendStatusChangeApi;
import teleblock.ui.dialog.CommonTipsDialog;

/**
 * Time:2022/8/12
 * Author:Perry
 * Description：适配器工具类
 */
public class AdapterUtil {

    /**
     * 推荐列表跳转聊天页面工具类
     * @param mBaseFragment
     * @param adapter
     * @param data
     * @param position
     */
    public static void recommendListIndexChatActOperation(
            BaseFragment mBaseFragment,
            BaseQuickAdapter adapter,
            HotRecommendData data,
            int position
    ) {
        final AlertDialog progressDialog = new AlertDialog(mBaseFragment.getParentActivity(), 3);
        progressDialog.show();
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = data.getChat_link();
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                FileLog.e(e);
            }
            if (error != null) {
                try {
                    if (error != null && error.text != null && error.text.startsWith("FLOOD_WAIT")) {
                        ToastUtils.showShort(LocaleController.getString("FloodWait", R.string.FloodWait));
                    } else {
//                        Toast.makeText(mBaseFragment.getParentActivity(), LocaleController.getString("NoUsernameFound", R.string.NoUsernameFound), Toast.LENGTH_SHORT).show();
                        new CommonTipsDialog(adapter.getContext(), LocaleController.getString("view_community_recommend_invate", R.string.view_community_recommend_invate)) {
                            @Override
                            public void onRightClick() {
                                adapter.removeAt(position);

                                EasyHttp.post(new ApplicationLifecycle())
                                        .api(new RecommendStatusChangeApi()
                                                .setChat_id(String.valueOf(data.getChat_id()))
                                        ).request(null);
                            }
                        }.setRightTextAndColor(LocaleController.getString("view_community_recommend_confirm", R.string.view_community_recommend_confirm),
                                        ContextCompat.getColor(adapter.getContext(), R.color.theme_color))
                                .setHideLeftText()
                                .show();
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
                return;
            }
            final TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
            MessagesController.getInstance(UserConfig.selectedAccount).putUsers(res.users, false);
            MessagesController.getInstance(UserConfig.selectedAccount).putChats(res.chats, false);
            MessagesStorage.getInstance(UserConfig.selectedAccount).putUsersAndChats(res.users, res.chats, false, true);

            if (res.chats.size() > 0) {
                long chat_id = res.chats.get(0).id;
                //打开群聊或者频道
                Bundle args = new Bundle();
                args.putLong("chat_id", chat_id);
                mBaseFragment.presentFragment(new ChatActivity(args));
            } else {
//                Toast.makeText(mBaseFragment.getParentActivity(), LocaleController.getString("NoUsernameFound", R.string.NoUsernameFound), Toast.LENGTH_SHORT).show();
                new CommonTipsDialog(adapter.getContext(), LocaleController.getString("view_community_recommend_invate", R.string.view_community_recommend_invate)) {
                    @Override
                    public void onRightClick() {
                        adapter.removeAt(position);

                        EasyHttp.post(new ApplicationLifecycle())
                                .api(new RecommendStatusChangeApi()
                                        .setChat_id(String.valueOf(data.getChat_id()))
                                ).request(null);
                    }
                }.setRightTextAndColor(LocaleController.getString("view_community_recommend_confirm", R.string.view_community_recommend_confirm),
                                ContextCompat.getColor(adapter.getContext(), R.color.theme_color))
                        .setHideLeftText()
                        .show();
            }
        }));

    }
}
