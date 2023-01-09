package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.CollectionUtils;
import com.google.android.flexbox.FlexboxLayoutManager;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActPermissionsSelectuserChatlistBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import teleblock.manager.PermissionsChatManager;
import teleblock.model.PermissChatEntity;
import teleblock.ui.adapter.AllPermissionsChatAdp;
import teleblock.ui.adapter.SelectorChatAdp;
import teleblock.util.MMKVUtil;
import teleblock.util.TGLog;

/**
 * Time:2022/8/8
 * Author:Perry
 * Description：用户选择黑名单/白名单页面
 */
public class PermissionsSelectoUserChatListAct extends BaseFragment {

    //是否是选择黑名单用户
    private boolean ifSelectorBlackListUserPage;

    private ActPermissionsSelectuserChatlistBinding binding;

    //所有的会话列表
    private List<PermissChatEntity> allChatList;
    private AllPermissionsChatAdp mAllPermissionsChatAdp = new AllPermissionsChatAdp();

    //已选择会话数据
    private List<PermissChatEntity> selectorChatList;
    private SelectorChatAdp mSelectorChatAdp = new SelectorChatAdp();

    public PermissionsSelectoUserChatListAct(boolean ifSelectorBlackListUserPage) {
        this.ifSelectorBlackListUserPage = ifSelectorBlackListUserPage;
    }

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE,true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("act_permissions_chat_selectoruser", R.string.act_permissions_chat_selectoruser));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActPermissionsSelectuserChatlistBinding.inflate(LayoutInflater.from(context));
        initView();

        loadData();
        return fragmentView = binding.getRoot();
    }

    private void initView() {
        binding.tvSelectorTips.setText(ifSelectorBlackListUserPage
                ? LocaleController.getString("act_permissions_chat_confirm_addblacklist", R.string.act_permissions_chat_confirm_addblacklist)
                : LocaleController.getString("act_permissions_chat_confirm_addwhitelist", R.string.act_permissions_chat_confirm_addwhitelist));

        //所有会话列表
        binding.rvAllChat.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        binding.rvAllChat.setAdapter(mAllPermissionsChatAdp);

        //选择会话列表
        binding.rvSelect.setLayoutManager(new FlexboxLayoutManager(getParentActivity()));
        binding.rvSelect.setAdapter(mSelectorChatAdp);

        //把所有的值存到mmkv里面去
        binding.llBottom.setOnClickListener(view -> {
            List<Long> dialogIds = new ArrayList<>();
            for (PermissChatEntity selectorData : mSelectorChatAdp.getData()) {
                dialogIds.add(selectorData.getDialogId());
            }

            if (ifSelectorBlackListUserPage) {
                MMKVUtil.setBlackChatList(dialogIds);
            } else {
                MMKVUtil.setWhiteChatList(dialogIds);
            }

            finishFragment();
        });

        //点击事件
        mAllPermissionsChatAdp.setOnItemClickListener((adapter, view, position) -> {
            adapterSelectorStatus(mAllPermissionsChatAdp.getData(), position);
        });
    }

    private void loadData() {
        //获取所有的会话列表
        allChatList = PermissionsChatManager.getInstance().chatListFilter(ifSelectorBlackListUserPage);
        //已经选择的黑名单或者白名单数据
        selectorChatList = PermissionsChatManager.getInstance().permissionsChatList(ifSelectorBlackListUserPage);
        if (!selectorChatList.isEmpty()) {
            for (PermissChatEntity allChatData : allChatList) {
                for (PermissChatEntity selectorData : selectorChatList) {
                    if (allChatData.getDialogId() == selectorData.getDialogId()) {
                        allChatData.setIfChecked(true);
                    }
                }
            }

            //显示选中的布局
            mSelectorChatAdp.setList(selectorChatList);
        }
        setViewIfVisOrHide(selectorChatList.size());

        //所有的会话列表
        mAllPermissionsChatAdp.setList(allChatList);
    }

    /**
     * 适配器更新选中状态
     * @param data
     * @param position
     */
    private void adapterSelectorStatus(List<PermissChatEntity> data, int position) {
        //如果是长按事件的话，就默认设置成true，如果是点击事件的话，就反向操作
        boolean clickChatSelectorStatus = data.get(position).isIfChecked();
        clickChatSelectorStatus = !clickChatSelectorStatus;
        data.get(position).setIfChecked(clickChatSelectorStatus);

        //筛选出来选中的个数
        long selectorCount = data.stream().filter(PermissChatEntity::isIfChecked).count();
        setViewIfVisOrHide((int) selectorCount);

        //选中的添加到选择集合里面去
        if (clickChatSelectorStatus) {//添加
            //判断集合里面存不存在这条数据
            if (mSelectorChatAdp.getData().stream().filter(filter -> filter.getDialogId() == data.get(position).getDialogId()).count() == 0) {
                mSelectorChatAdp.addData(data.get(position));
            }
        } else {//删除
            for (int i = 0; i < mSelectorChatAdp.getData().size(); i++) {
                if (mSelectorChatAdp.getData().get(i).getDialogId() == data.get(position).getDialogId()) {
                    mSelectorChatAdp.removeAt(i);
                }
            }
        }

        //刷新适配器
        mAllPermissionsChatAdp.notifyItemChanged(position);
    }

    /**
     * 控制当前页面组件是否显示
     */
    private void setViewIfVisOrHide(int selecorNum) {
        if (selecorNum > 0) {
            binding.rvSelect.setVisibility(View.VISIBLE);
            binding.llBottom.setVisibility(View.VISIBLE);
            binding.tvSelectorNum.setText(String.valueOf(selecorNum));
        } else {
            binding.rvSelect.setVisibility(View.GONE);
            binding.llBottom.setVisibility(View.GONE);
        }
    }
}
