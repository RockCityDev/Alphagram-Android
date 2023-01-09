package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActPermissionsChatlistBinding;
import org.telegram.messenger.databinding.ViewCheckListEmptyBinding;
import org.telegram.messenger.databinding.ViewNftEmptyBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.List;

import teleblock.manager.PermissionsChatManager;
import teleblock.model.PermissChatEntity;
import teleblock.ui.adapter.PermissionsChatAdp;
import teleblock.util.MMKVUtil;

/**
 * Time:2022/8/5
 * Author:Perry
 * Description：权限聊天列表-包括黑名单和白名单
 */
public class PermissionsChatListAct extends BaseFragment {

    //是否是黑名单列表
    private boolean ifBlackListPage;

    private ActPermissionsChatlistBinding binding;
    private List<PermissChatEntity> chatList;

    private PermissionsChatAdp mPermissionsChatAdp = new PermissionsChatAdp();

    private String chatsNumFormat;

    public PermissionsChatListAct(boolean ifBlackListPage) {
        this.ifBlackListPage = ifBlackListPage;
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE,true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(ifBlackListPage ? LocaleController.getString("act_relatedme_setting_blacklist", R.string.act_relatedme_setting_blacklist)
                : LocaleController.getString("act_relatedme_setting_whitelist", R.string.act_relatedme_setting_whitelist));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActPermissionsChatlistBinding.inflate(LayoutInflater.from(context));

        initView();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvClickAddList.setText(ifBlackListPage ? LocaleController.getString("act_permissions_chat_addblacklist", R.string.act_permissions_chat_addblacklist)
                : LocaleController.getString("act_permissions_chat_addwhitelist", R.string.act_permissions_chat_addwhitelist));

        binding.rvChatList.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        binding.rvChatList.setAdapter(mPermissionsChatAdp);
        chatsNumFormat = ifBlackListPage ? LocaleController.getString("act_permissions_chat_blacklist_num", R.string.act_permissions_chat_blacklist_num) : LocaleController.getString("act_permissions_chat_whitelist_num", R.string.act_permissions_chat_whitelist_num);
        mPermissionsChatAdp.setEmptyView(createEmptyView(ifBlackListPage));
        mPermissionsChatAdp.getEmptyLayout().setVisibility(View.GONE);

        //跳转到选择会话列表页面
        binding.tvClickAddList.setOnClickListener(view -> {
            presentFragment(new PermissionsSelectoUserChatListAct(ifBlackListPage));
        });

        //移除黑名单
        mPermissionsChatAdp.setOnItemClickListener((adapter, view, position) -> {
            List<Long> dialogIds;
            if (ifBlackListPage) {//黑名单
                dialogIds = MMKVUtil.blackChatList();
            } else {
                dialogIds = MMKVUtil.whiteChatList();
            }

            dialogIds.remove(dialogIds.indexOf(mPermissionsChatAdp.getData().get(position).getDialogId()));

            if (ifBlackListPage) {
                MMKVUtil.setBlackChatList(dialogIds);
            } else {
                MMKVUtil.setWhiteChatList(dialogIds);
            }

            mPermissionsChatAdp.remove(mPermissionsChatAdp.getData().get(position));
            setViewIfVisOrHide(mPermissionsChatAdp.getData().size());
        });
    }

    private View createEmptyView(boolean ifBlackListPage) {
        binding.tvChatNums.setText(String.format(chatsNumFormat, 0));
        ViewCheckListEmptyBinding binding = ViewCheckListEmptyBinding.inflate(LayoutInflater.from(getParentActivity()));
        if (ifBlackListPage) {
            binding.tvEmptyTip1.setText(LocaleController.getString("act_permissions_black_null_tips1", R.string.act_permissions_black_null_tips1));
            binding.tvEmptyTip2.setText(LocaleController.getString("act_permissions_black_null_tips2", R.string.act_permissions_black_null_tips2));
        } else {
            binding.tvEmptyTip1.setText(LocaleController.getString("act_permissions_white_null_tips1", R.string.act_permissions_white_null_tips1));
            binding.tvEmptyTip2.setText(LocaleController.getString("act_permissions_white_null_tips2", R.string.act_permissions_white_null_tips2));
        }
        return binding.getRoot();
    }

    private void loadData() {
        //获取黑名单或者白名单数据
        chatList = PermissionsChatManager.getInstance().permissionsChatList(ifBlackListPage);
        //列表
        mPermissionsChatAdp.setList(chatList);
        setViewIfVisOrHide(chatList.size());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    /**
     * 控制view显示或者隐藏
     *
     * @param num
     */
    private void setViewIfVisOrHide(int num) {
        if (num > 0) {
            mPermissionsChatAdp.getEmptyLayout().setVisibility(View.GONE);
            //binding.tvChatNums.setVisibility(View.VISIBLE);
            //binding.rvChatList.setVisibility(View.VISIBLE);
            //binding.tvChatNums.setText(String.format(chatsNumFormat, num));
        } else {
            mPermissionsChatAdp.getEmptyLayout().setVisibility(View.VISIBLE);
            //binding.tvChatNums.setVisibility(View.GONE);
            //binding.rvChatList.setVisibility(View.GONE);
        }
    }
}
