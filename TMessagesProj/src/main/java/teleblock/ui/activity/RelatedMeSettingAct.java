package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActRelatedmeSettingBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SlideChooseView;

import java.util.Arrays;

import teleblock.manager.DialogManager;
import teleblock.util.MMKVUtil;
import teleblock.util.TGLog;

/**
 * Time:2022/8/5
 * Author:Perry
 * Description： 与我相关设置
 */
public class RelatedMeSettingAct extends BaseFragment {

    private ActRelatedmeSettingBinding binding;

    //加入置顶列表的切换按钮
    private NotificationsCheckCell addToppingListSwitchBtn;
    //加入所有归档列表
    private NotificationsCheckCell addAllArchiveListSwitchBtn;
    //管理人数以内的群组
    private NotificationsCheckCell peopleNumberScreenSwitchBtn;

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.parseColor("#EFEFEF"),true);
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString("act_relatedme_setting_title", R.string.act_relatedme_setting_title));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActRelatedmeSettingBinding.inflate(LayoutInflater.from(context));
        initView(context);
        return fragmentView = binding.getRoot();
    }

    private void initView(Context context) {
        String format = LocaleController.getString("act_relatedme_setting_meetcriteria_group", R.string.act_relatedme_setting_meetcriteria_group);

        //归档群数量
        int archiveGroupsNum = getMessagesController().getDialogs(1).size();
        //置顶列表数量
        int toppingGroupsNum;
        if (DialogManager.getInstance(currentAccount).relatedMeDialogs.isEmpty()) {
            toppingGroupsNum = 0;
        } else {
            if (DialogManager.getInstance(currentAccount).relatedMeDialogs.get(2) != null) {
                toppingGroupsNum = DialogManager.getInstance(currentAccount).relatedMeDialogs.get(2).size();
            } else {
                toppingGroupsNum = 0;
            }
        }
        //如果归档数量大于0，置顶列表数据-1
        if (archiveGroupsNum > 0) {
            toppingGroupsNum = toppingGroupsNum - 1;
        }

        //置顶群聊
        boolean toppingSwitch = MMKVUtil.ifOpenTopping();
        String toppingMeets = String.format(format, toppingGroupsNum);//符合该条件的群组
        addToppingListSwitchBtn = new NotificationsCheckCell(context);
        addToppingListSwitchBtn.setTextAndValueAndCheck(
                LocaleController.getString("act_relatedme_setting_addtoping_list", R.string.act_relatedme_setting_addtoping_list),
                toppingMeets, toppingSwitch,0, true, true
        );
        addToppingListSwitchBtn.setBackgroundColor(Color.WHITE);
        binding.flAddtoppingSwitch.addView(addToppingListSwitchBtn, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        binding.flAddtoppingSwitch.setOnClickListener(view -> {
            boolean switchStatus = addToppingListSwitchBtn.isChecked();

            addToppingListSwitchBtn.setChecked(!switchStatus);
            MMKVUtil.setIfOpenTopping(!switchStatus);
        });

        //加入所有归档列表
        boolean allArchiveSwitcg = MMKVUtil.ifOpenArchive();
        String archiveMeets = String.format(format, archiveGroupsNum);//符合该条件的群组
        addAllArchiveListSwitchBtn = new NotificationsCheckCell(context);
        addAllArchiveListSwitchBtn.setTextAndValueAndCheck(
                LocaleController.getString("act_relatedme_setting_archive_list", R.string.act_relatedme_setting_archive_list),
                archiveMeets, allArchiveSwitcg,0, true, true
        );
        addAllArchiveListSwitchBtn.setBackgroundColor(Color.WHITE);
        binding.flArchiveSwitch.addView(addAllArchiveListSwitchBtn, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        binding.flArchiveSwitch.setOnClickListener(view -> {
            boolean switchStatus = addAllArchiveListSwitchBtn.isChecked();

            addAllArchiveListSwitchBtn.setChecked(!switchStatus);
            MMKVUtil.setIfOpenArchive(!switchStatus);
        });

        //筛选人数 默认30人
        String filterNum = String.valueOf(MMKVUtil.groupFilterPeopleNum());
        String peopleFilterFormat = LocaleController.getString("act_relatedme_setting_peoplenumber_screen_list_tips", R.string.act_relatedme_setting_peoplenumber_screen_list_tips);
        boolean groupPeopleNumSwitch = MMKVUtil.ifOpenPeopleFilter();
        String groupPeopleNumMeets = String.format(peopleFilterFormat, MMKVUtil.groupFilterPeopleNum());//符合该条件的群组
        ifShowGroupPeopleFilterView(groupPeopleNumSwitch);
        peopleNumberScreenSwitchBtn = new NotificationsCheckCell(context);
        peopleNumberScreenSwitchBtn.setTextAndValueAndCheck(
                LocaleController.getString("act_relatedme_setting_peoplenumber_screen_list", R.string.act_relatedme_setting_peoplenumber_screen_list),
                groupPeopleNumMeets, groupPeopleNumSwitch,0, true, true
        );
        peopleNumberScreenSwitchBtn.setBackgroundColor(Color.WHITE);
        binding.flPeoplenumberScreenSwitch.addView(peopleNumberScreenSwitchBtn, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        binding.flPeoplenumberScreenSwitch.setOnClickListener(view -> {
            boolean switchStatus = peopleNumberScreenSwitchBtn.isChecked();
            ifShowGroupPeopleFilterView(!switchStatus);
            peopleNumberScreenSwitchBtn.setChecked(!switchStatus);
            MMKVUtil.setIfOpenPeopleFilter(!switchStatus);
        });

        String[] groupPeopleNumStrs = new String[]{"3","30","50","100","150","200","300"};
        SlideChooseView slideChooseView = new SlideChooseView(context);
        slideChooseView.setCallback(new SlideChooseView.Callback() {
            @Override
            public void onOptionSelected(int index) {
                MMKVUtil.setGroupFilterPeopleNum(Integer.parseInt(groupPeopleNumStrs[index]));
            }

            @Override
            public void onTouchEnd() {
            }
        });

        int contains = Arrays.asList(groupPeopleNumStrs).indexOf(filterNum);
        slideChooseView.setOptions(contains == -1 ? 3 : contains, groupPeopleNumStrs);
        binding.flGroupPopuonumScreen.addView(slideChooseView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER, 14, 0, 14, 0));

        //筛选
        binding.tvFilterTitle.setText(LocaleController.getString("act_relatedme_setting_filter_title", R.string.act_relatedme_setting_filter_title));
        binding.tvWhitelistTitle.setText(LocaleController.getString("act_relatedme_setting_whitelist", R.string.act_relatedme_setting_whitelist));
        binding.tvBlacklistTitle.setText(LocaleController.getString("act_relatedme_setting_blacklist", R.string.act_relatedme_setting_blacklist));

        //黑名单点击
        binding.rlClickBlacklist.setOnClickListener(view -> {
            presentFragment(new PermissionsChatListAct(true));
        });

        //白名单点击
        binding.rlClickWhitelist.setOnClickListener(view -> {
            presentFragment(new PermissionsChatListAct(false));
        });
    }

    /**
     * 是否显示群人数筛选view
     * @param show
     */
    private void ifShowGroupPeopleFilterView(boolean show) {
        binding.flGroupPopuonumScreen.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        setViewIsVisOrHide();
    }

    private void setViewIsVisOrHide() {
        int whiteListNum = MMKVUtil.whiteChatList().size();
        int blackListNum = MMKVUtil.blackChatList().size();

        if (whiteListNum > 0) {
            binding.tvWhitelistNum.setText(String.valueOf(whiteListNum));
            binding.tvWhitelistNum.setVisibility(View.VISIBLE);
        } else {
            binding.tvWhitelistNum.setVisibility(View.GONE);
        }

        if (blackListNum > 0) {
            binding.tvBlacklistNum.setText(String.valueOf(blackListNum));
            binding.tvBlacklistNum.setVisibility(View.VISIBLE);
        } else {
            binding.tvBlacklistNum.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        DialogManager.getInstance(currentAccount).updateAllDialogs(true);
    }
}
