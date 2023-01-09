package teleblock.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.hjq.http.request.PostRequest;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.ActAllRecommendBinding;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;

import java.util.List;

import okhttp3.Call;
import teleblock.model.BaseLoadmoreModel;
import teleblock.model.HotRecommendData;
import teleblock.network.BaseBean;
import teleblock.network.api.HotRecommendApi;
import teleblock.ui.adapter.RecommendVerAdp;
import teleblock.util.AdapterUtil;

/**
 * Time:2022/7/15
 * Author:Perry
 * Description：所有的专属推荐，频道推荐，群组推荐数据
 */
public class AllRecommendActivity extends BaseFragment {
    //频道推荐
    public static final int CHANNEL_TYPE = 1;
    //群组推荐
    public static final int GROUP_TYPE = 2;
    //专属推荐
    public static final int RECOMMEND_TYPE = 0;

    private ActAllRecommendBinding binding;

    //所有推荐数据
    private RecommendVerAdp mRecommendVerAdp;

    //页面类型
    private int pageType;

    //语言ID
    private int languageId;

    public AllRecommendActivity(int pageType) {
        this.pageType = pageType;
    }

    //只用从专属推荐跳转过来才需要调用这个
    public AllRecommendActivity(int pageType, int languageId) {
        this.pageType = pageType;
        this.languageId = languageId;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);

        String title;
        switch (pageType) {
            case RECOMMEND_TYPE:
                title = LocaleController.getString("view_community_recommendtab_recommend", R.string.view_community_recommendtab_recommend);
                break;
            case CHANNEL_TYPE:
                title = LocaleController.getString("commontools_channel_recommend", R.string.commontools_channel_recommend);
                break;
            case GROUP_TYPE:
                title = LocaleController.getString("commontools_group_recommend", R.string.commontools_group_recommend);
                break;
            default:
                title = LocaleController.getString("view_community_recommendtab_title", R.string.view_community_recommendtab_title);
                break;
        }
        actionBar.setTitle(title);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        binding = ActAllRecommendBinding.inflate(LayoutInflater.from(context));
        fragmentView = binding.getRoot();
        initView();
        return fragmentView;
    }

    private void initView() {
        mRecommendVerAdp = new RecommendVerAdp();
        binding.rvHotChannel.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        binding.rvHotChannel.setAdapter(mRecommendVerAdp);

        //下拉刷新
        binding.refreshLayout.autoRefresh();
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            requestRecommendData(true);
        });

        //上拉加载更多
        mRecommendVerAdp.getLoadMoreModule().setEnableLoadMore(true);
        mRecommendVerAdp.getLoadMoreModule().setEnableLoadMoreIfNotFullPage(true);
        mRecommendVerAdp.getLoadMoreModule().setPreLoadNumber(3);//预加载
        mRecommendVerAdp.getLoadMoreModule().setOnLoadMoreListener(() -> requestRecommendData(false));

        mRecommendVerAdp.setOnItemClickListener((adapter, view, position) -> {
            AdapterUtil.recommendListIndexChatActOperation(
                    this,
                    mRecommendVerAdp,
                    mRecommendVerAdp.getData().get(position),
                    position
            );
        });
    }

    /**
     * 请求推荐数据
     */
    private int page = 1;
    private int pageSize = 10;
    private void requestRecommendData(boolean ifRefresh) {
        if (ifRefresh) {
            page = 1;
        } else {
            page++;
        }

        PostRequest request = null;
        switch (pageType) {
            case CHANNEL_TYPE:
                request = EasyHttp.post(new ApplicationLifecycle())
                        .api(new HotRecommendApi(page, pageSize).setChat_type(1));
                break;
            case GROUP_TYPE:
                request = EasyHttp.post(new ApplicationLifecycle())
                        .api(new HotRecommendApi(page, pageSize).setChat_type(2));
                break;
            default:
                request = EasyHttp.post(new ApplicationLifecycle())
                        .api(new HotRecommendApi(page, pageSize).setLanguage_id(String.valueOf(languageId)));
                break;
        }

        request.request(new OnHttpListener<BaseBean<BaseLoadmoreModel<HotRecommendData>>>() {
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
            public void onFail(Exception e) {

            }

            @Override
            public void onEnd(Call call) {
                mRecommendVerAdp.getLoadMoreModule().loadMoreComplete();
                binding.refreshLayout.finishRefresh();
            }
        });
    }
}
