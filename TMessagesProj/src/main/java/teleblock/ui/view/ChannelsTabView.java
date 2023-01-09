package teleblock.ui.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ViewTabContentBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.DialogsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.ChannelFeedEntity;
import teleblock.model.ChannelTagEntity;
import teleblock.telegram.channels.ChannelTagManager;
import teleblock.ui.adapter.TgFragmentVp2Adapter;
import teleblock.ui.fragment.ChannelFeedFragment;
import teleblock.util.EventUtil;
import teleblock.util.ViewUtil;

/**
 * 创建日期：2022/4/19
 * 描述：频道焦点页面
 */
public class ChannelsTabView extends FrameLayout {
    private ViewTabContentBinding binding;

    private BaseFragment mBaseFragment;
    private List<ChannelTagEntity> tagList = new ArrayList<>();
    private List<teleblock.ui.fragment.BaseFragment> pageFragmentView = new ArrayList<>();
    private BackupImageView avatarImageView;
    private TgFragmentVp2Adapter mTgFragmentVp2Adapter;

    //打开次数
    private int openCount = 0;

    public ChannelsTabView(@NonNull DialogsActivity dialogsActivity) {
        super(dialogsActivity.getParentActivity());
        this.mBaseFragment = dialogsActivity;
        EventBus.getDefault().register(this);
        initView();
        setVisibility(GONE);
        setOnClickListener(v -> {
        });
    }

    private void initView() {
        binding = ViewTabContentBinding.inflate(LayoutInflater.from(getContext()), this, true);
        binding.viewStatusBar.getLayoutParams().height = AndroidUtilities.statusBarHeight;
        binding.rlActionBar.getLayoutParams().height = ActionBar.getCurrentActionBarHeight();
        binding.ivDrawerMenu.setImageDrawable(new MenuDrawable());
        binding.tvTitle.setText(LocaleController.getString("fragment_channel_coutent_title", R.string.fragment_channel_coutent_title));
        binding.tvTitle.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        updateStyle();

        binding.avatarFrameContent.setOnClickListener(view -> {
            EventBus.getDefault().post(new MessageEvent(EventBusTags.OPEN_DRAWER));
        });

        avatarImageView = new BackupImageView(getContext());
        avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(20));
        binding.avatarFrame.addView(avatarImageView);

        //vp滑动事件绑定
        ViewUtil.vbBindMiTabListener(binding.magicIndicator, binding.channelFeedViewpage, null);

        //修改tab栏顺序
        binding.ivTagEdit.setOnClickListener(view -> {});

        //点击按钮刷新
        binding.ivRefresh.setOnClickListener(view -> {
            ((ChannelFeedFragment) pageFragmentView.get(binding.channelFeedViewpage.getCurrentItem())).refreshData();
        });
    }

    public void initData() {
        EventUtil.track(getContext(), EventUtil.Even.频道页面展示, new HashMap<>());
        updateUserInfo();

        if (openCount == 0) {
            //删除空channel的标签
            ChannelTagManager.getInstance().deleteTagWithNoChannel();

            getChannelTag();
        }
        openCount++;
    }

    /**
     * 获取频道标签
     */
    private void getChannelTag() {
        //获取标签列表
        List<ChannelTagEntity> tmpList = ChannelTagManager.getInstance().getTagList();
        tagList.clear();
        pageFragmentView.clear();

        ChannelTagEntity secretTag = null;
        String secretStr = LocaleController.getString("channel_recommend_tag_secret", R.string.channel_recommend_tag_secret);
        for (ChannelTagEntity tagEntity : tmpList) {
            if (secretStr.equals(tagEntity.tagName)) {
                secretTag = tagEntity;
            } else {
                tagList.add(tagEntity);
            }
        }
        if (secretTag != null) {
            tagList.add(secretTag);
        }

        ChannelTagEntity tagALL = new ChannelTagEntity();
        tagALL.tagId = -1;
        tagALL.tagName = LocaleController.getString("channel_tag_all_text", R.string.channel_tag_all_text);
        tagList.add(0, tagALL);

        List<String> tabNameList = new ArrayList<>();
        for (int i = 0; i < tagList.size(); i++) {
            tabNameList.add(tagList.get(i).tagName);
            ChannelFeedEntity entity = new ChannelFeedEntity();
            if (i == 0) {
                entity.from = "homeFeedView";
                entity.title = "信息流首页";
                entity.tagId = -1;
                entity.chatId = -1;
            } else {
                entity.from = "channelTag:" + tagList.get(i).tagId;
                entity.title = tagList.get(i).tagName;
                entity.tagId = tagList.get(i).tagId;
                entity.chatId = 0;
            }
            ChannelFeedFragment pageFragment = new ChannelFeedFragment();
            pageFragment.setmBaseFragment(mBaseFragment);
            pageFragment.setEntity(entity);
            pageFragmentView.add(pageFragment);
        }

        binding.tabLayout.setVisibility(tabNameList.size() == 1 ? View.GONE : View.VISIBLE);
        //tab适配器
        CommonNavigatorAdapter mibTabAdapter = ViewUtil.normalTextMibAdapter(tabNameList, binding.channelFeedViewpage);
        binding.magicIndicator.setNavigator(ViewUtil.mibSetNavigat(getContext(), mibTabAdapter));

        //初始化适配器
        mTgFragmentVp2Adapter = new TgFragmentVp2Adapter(mBaseFragment.getParentActivity(), pageFragmentView);
        binding.channelFeedViewpage.setAdapter(mTgFragmentVp2Adapter);

        //vp默认页面
        binding.channelFeedViewpage.setCurrentItem(0, false);
        //禁止左右滑动
        binding.channelFeedViewpage.setUserInputEnabled(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventInfo(MessageEvent messageEvent) {
        switch (messageEvent.getType()) {
            case EventBusTags.CHANNEL_TAG_REFRASH:
                //标签栏有变化
                getChannelTag();
                break;

            case EventBusTags.CHANNEL_WITH_TAG_REFRASH:
                //tag下的channel有变化
                ChannelTagEntity data = (ChannelTagEntity) messageEvent.getData();
                for (int i = 0; i < tagList.size(); i++) {
                    if (tagList.get(i).tagId == data.tagId) {
                        ((ChannelFeedFragment) pageFragmentView.get(i)).refrashDataByTag();
                    }
                }
                break;
        }
    }

    public void updateUserInfo() {
        TLRPC.User user = mBaseFragment.getUserConfig().getCurrentUser();
        if (user != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable(user);
            avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
            avatarImageView.setForUserOrChat(user, avatarDrawable);
        }
    }

    public void updateStyle() {
        binding.viewStatusBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        binding.rlActionBar.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        binding.tvTitle.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));
        binding.ivRefresh.setColorFilter(Theme.getColor(Theme.key_actionBarDefaultIcon));
    }
}