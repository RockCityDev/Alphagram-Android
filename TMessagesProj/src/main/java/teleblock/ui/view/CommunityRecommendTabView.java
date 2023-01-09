package teleblock.ui.view;

import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.CommunityRecommendViewBinding;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.DialogsActivity;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.model.BaseLoadmoreModel;
import teleblock.model.HotRecommendData;
import teleblock.model.HottopicEntity;
import teleblock.network.BaseBean;
import teleblock.network.api.HotRecommendApi;
import teleblock.network.api.HottopicApi;
import teleblock.ui.activity.AllRecommendActivity;
import teleblock.ui.activity.RecommendSearchActivity;
import teleblock.ui.adapter.HotRecommendAdp;
import teleblock.ui.adapter.HotTopicAdp;
import teleblock.ui.adapter.RecommendVerAdp;
import teleblock.ui.dialog.CommRecomLanguageDialog;
import teleblock.util.AdapterUtil;
import teleblock.util.MMKVUtil;

/**
 * Time:2022/8/9
 * Author:Perry
 * Description：社群推荐
 */
public class CommunityRecommendTabView extends FrameLayout {

    private CommunityRecommendViewBinding binding;
    private BaseFragment mBaseFragment;

    //专属推荐适配器
    private HotRecommendAdp mHotRecommendAdp;

    //热门话题适配器
    private HotTopicAdp mHotTopicAdp;

    //推荐垂直列表适配器
    private RecommendVerAdp mRecommendVerAdp;

    //头像
    private BackupImageView avatarImageView;

    //打开次数
    private int openCount = 0;

    //点击的标签选项
    private List<Integer> ids = new ArrayList<>();

    //语言选择
    private CommRecomLanguageDialog mCommRecomLanguageDialog;

    //语言环境ID
    private int languageID;
    private String languageName;

    public CommunityRecommendTabView(@NonNull DialogsActivity dialogsActivity) {
        super(dialogsActivity.getParentActivity());
        this.mBaseFragment = dialogsActivity;
        initView();
        setVisibility(GONE);
    }

    private void initView() {
        mHotTopicAdp = new HotTopicAdp();
        mHotRecommendAdp = new HotRecommendAdp();

        binding = CommunityRecommendViewBinding.inflate(LayoutInflater.from(getContext()), this, true);
        setOnClickListener(view -> {});
        binding.cl.setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);

        binding.tvTitle.setText(LocaleController.getString("view_community_recommendtab_title", R.string.view_community_recommendtab_title));
        binding.tvSerach.setText(LocaleController.getString("view_community_recommendtab_search", R.string.view_community_recommendtab_search));
        binding.tvRecommend.setText(LocaleController.getString("view_community_recommendtab_recommend", R.string.view_community_recommendtab_recommend));
        binding.tvShowallRecommend.setText(LocaleController.getString("view_community_recommendtab_showall_recommend", R.string.view_community_recommendtab_showall_recommend));
        binding.tvHottopic.setText(LocaleController.getString("view_community_recommendtab_hottopic", R.string.view_community_recommendtab_hottopic));

        if (mCommRecomLanguageDialog == null) {
            mCommRecomLanguageDialog = new CommRecomLanguageDialog(mBaseFragment.getParentActivity(), (id, name) -> {
                //语言id
                languageID = id;
                languageName = name;
                getHotTopicData();

                //右上角语言名称
                binding.tvLanguage.setText(languageName);
            });
        }

        //用户头像
        avatarImageView = new BackupImageView(getContext());
        avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(20));
        binding.avatarFrame.addView(avatarImageView);

        //用户头像点击事件
        binding.avatarFrameContent.setOnClickListener(view -> {
            EventBus.getDefault().post(new MessageEvent(EventBusTags.OPEN_DRAWER));
        });

        //专属推荐样式
        binding.rvRecommend.setLayoutManager(new LinearLayoutManager(mBaseFragment.getParentActivity(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvRecommend.setAdapter(mHotRecommendAdp);

        //热门话题rv样式
        StaggeredGridLayoutManager hottopiclm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL);
        binding.rvHottopic.setLayoutManager(hottopiclm);
        binding.rvHottopic.setAdapter(mHotTopicAdp);

        //下部分的推荐列表
        binding.rvVerRecommend.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecommendVerAdp = new RecommendVerAdp();
        binding.rvVerRecommend.setAdapter(mRecommendVerAdp);

        //上拉加载更多
        mRecommendVerAdp.getLoadMoreModule().setEnableLoadMore(true);
        mRecommendVerAdp.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
        mRecommendVerAdp.getLoadMoreModule().setPreLoadNumber(3);//预加载
        mRecommendVerAdp.getLoadMoreModule().setOnLoadMoreListener(() -> requestRecommendData(false));

        //点击事件
        mRecommendVerAdp.setOnItemClickListener((adapter, view, position) -> {
            AdapterUtil.recommendListIndexChatActOperation(
                    mBaseFragment,
                    mRecommendVerAdp,
                    mRecommendVerAdp.getData().get(position),
                    position
            );
        });

        //语言环境切换点击事件
        binding.tvLanguage.setOnClickListener(view -> {
            mCommRecomLanguageDialog.show();
        });

        //跳转到搜索页面
        binding.tvSerach.setOnClickListener(view -> {
            mBaseFragment.presentFragment(new RecommendSearchActivity());
        });

        //跳转到显示全部推荐页面
        binding.tvShowallRecommend.setOnClickListener(view -> {
            mBaseFragment.presentFragment(new AllRecommendActivity(AllRecommendActivity.RECOMMEND_TYPE, languageID));
        });

        //点击热门推荐跳转
        mHotRecommendAdp.setOnItemClickListener((adapter, view, position) -> {
            switch (adapter.getItemViewType(position)) {
                case HotRecommendAdp.MORE_RECOMMENDL_TYPE:
                    mBaseFragment.presentFragment(new AllRecommendActivity(AllRecommendActivity.RECOMMEND_TYPE));
                    break;
                default:
                    AdapterUtil.recommendListIndexChatActOperation(
                            mBaseFragment,
                            mHotRecommendAdp,
                            mHotRecommendAdp.getData().get(position),
                            position
                    );
                    break;
            }
        });

        //热门话题点击事件
        mHotTopicAdp.setOnItemClickListener((adapter, view, position) -> {
            mHotTopicAdp.tabSelectorLogicalProcessing(position, () -> {
                ids.clear();
                ids.add(mHotTopicAdp.getData().get(position).getId());
                requestRecommendData(true);
            });
        });
    }


    public void initData() {
        if (openCount == 0) {
            if (!(MMKVUtil.getSystemMsg().language == null || MMKVUtil.getSystemMsg().language.isEmpty())) {
                languageID = MMKVUtil.getSystemMsg().language.get(0).value;
                languageName = MMKVUtil.getSystemMsg().language.get(0).key;
                binding.tvLanguage.setText(languageName);
            }

            //获取热门话题数据
            getHotTopicData();
        }
        updateUserInfo();
        openCount++;
    }

    /**
     * 请求热门话题数据
     */
    private void getHotTopicData() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new HottopicApi()
                        .setLanguage_id(String.valueOf(languageID))
                )
                .request(new OnHttpListener<BaseBean<List<HottopicEntity>>>() {
                    @Override
                    public void onSucceed(BaseBean<List<HottopicEntity>> result) {
                        if (result.getData() != null) {
                            mHotTopicAdp.setList(result.getData());
                            result.getData().get(0).setSelectorStatus(true);
                            mHotTopicAdp.clickPosition = 0;
                            ids.clear();
                            ids.add(result.getData().get(0).getId());

                            //获取专属推荐数据
                            getRecommendData();

                            //获取页面下部分列表数据
                            requestRecommendData(true);
                        }
                    }

                    @Override
                    public void onFail(Exception e) {

                    }
                });
    }

    /**
     * 获取专属推荐数据
     */
    private void getRecommendData() {
        EasyHttp.post(new ApplicationLifecycle())
                .api(new HotRecommendApi(1, 10).setLanguage_id(String.valueOf(languageID)))
                .request(new OnHttpListener<BaseBean<BaseLoadmoreModel<HotRecommendData>>>() {
                    @Override
                    public void onSucceed(BaseBean<BaseLoadmoreModel<HotRecommendData>> result) {
                        mHotRecommendAdp.setList(dealWithHotRecommendData(result.getData().getData()));
                    }

                    @Override
                    public void onFail(Exception e) {

                    }
                });
    }

    /**
     * 处理
     * @param data
     */
    private List<HotRecommendData> dealWithHotRecommendData(List<HotRecommendData> data) {
        if (data.size() > 3) {
            int dataSize = data.size();
            HotRecommendData item = new HotRecommendData();
            List<String> avatar = new ArrayList<>();
            avatar.add(data.get(dataSize - 2).getAvatar());
            avatar.add(data.get(dataSize - 1).getAvatar());
            item.setAvatarList(avatar);
            data.add(item);
        }

        return data;
    }

    /**
     * 请求频道/群组/机器人数据
     */
    private int page = 1;
    private int pageSize = 10;
    private void requestRecommendData(boolean ifRefresh) {
        if (ifRefresh) {
            page = 1;
        } else {
            page++;
        }

        EasyHttp.post(new ApplicationLifecycle())
                .api(new HotRecommendApi(page, pageSize)
                        .setTag_ids(ids)
                )
                .request(new OnHttpListener<BaseBean<BaseLoadmoreModel<HotRecommendData>>>() {
                    @Override
                    public void onSucceed(BaseBean<BaseLoadmoreModel<HotRecommendData>> result) {
                        List<HotRecommendData> resultData = result.getData().getData();
                        if (resultData.isEmpty()) {
                            mRecommendVerAdp.getLoadMoreModule().loadMoreEnd(true);
                            return;
                        }

                        if (resultData.size() < pageSize) {
                            mRecommendVerAdp.getLoadMoreModule().loadMoreEnd(true);
                        }

                        //添加到集合
                        if (ifRefresh) {
                            mRecommendVerAdp.setList(result.getData().getData());
                        } else {
                            mRecommendVerAdp.addData(result.getData().getData());
                        }
                    }

                    @Override
                    public void onFail(Exception e) {}

                    @Override
                    public void onEnd(Call call) {
                        mRecommendVerAdp.getLoadMoreModule().loadMoreComplete();
                    }
                });
    }


    /**
     * 根据无痕模式显示状态
     * @param show
     */
    public void changeStealth(boolean show) {
        binding.ivStealthMode.setVisibility(show ? VISIBLE : GONE);
    }

    /**
     * 更新用户信息
     */
    public void updateUserInfo() {
        TLRPC.User user = mBaseFragment.getUserConfig().getCurrentUser();
        if (user != null) {
            AvatarDrawable avatarDrawable = new AvatarDrawable(user);
            avatarDrawable.setColor(Theme.getColor(Theme.key_avatar_backgroundInProfileBlue));
            avatarImageView.setForUserOrChat(user, avatarDrawable);
        }
    }
}
