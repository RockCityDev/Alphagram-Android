
package teleblock.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hjq.http.EasyHttp;
import com.hjq.http.lifecycle.ApplicationLifecycle;
import com.hjq.http.listener.OnHttpListener;
import com.ruffian.library.widget.RTextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.databinding.ActivityRecommendSearchBinding;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ChatActivity;

import java.util.List;

import okhttp3.Call;
import teleblock.model.BaseLoadmoreModel;
import teleblock.model.HotRecommendData;
import teleblock.network.BaseBean;
import teleblock.network.api.HotRecommendApi;
import teleblock.ui.adapter.RecommendVerAdp;
import teleblock.ui.adapter.TgSearchResultAdp;
import teleblock.util.AdapterUtil;

/***
 * 社群推荐搜索
 */
public class RecommendSearchActivity extends BaseFragment {
    private ActivityRecommendSearchBinding binding;

    //结果适配器
    private RecommendVerAdp mRecommendVerAdp;

    //tg搜索结果适配器
    private TgSearchResultAdp mTgSearchResultAdp;

    //文字搜索
    private String searchText;

    private Runnable mRunnable = () -> {
        search();
        requestRecommendData(true);
    };
    private Handler mHandler = new Handler();

    //查看更多view
    private View moreView;

    @Override
    public View createView(Context context) {
        setNavigationBarColor(Color.WHITE,true);
        binding = ActivityRecommendSearchBinding.inflate(LayoutInflater.from(context));
        fragmentView = binding.getRoot();
        initView();
        removeActionbarViews();
        AndroidUtilities.runOnUIThread(() -> {
            binding.etSearch.requestFocus();
            AndroidUtilities.showKeyboard(binding.etSearch);
        }, 600);
        return fragmentView;
    }

    private void initView() {
        binding.searchMain.setPadding(0, AndroidUtilities.statusBarHeight, 0, 0);

        binding.ivBack.setOnClickListener(view -> finishFragment());
        binding.ivClose.setOnClickListener(view -> {
            binding.etSearch.setText("");
            view.setVisibility(View.GONE);
        });

        binding.tvTeleblockSearch.setText(LocaleController.getString("act_recommend_search_text", R.string.act_recommend_search_text));
        binding.tvTgSearch.setText(LocaleController.getString("act_recommend_tg_search_text", R.string.act_recommend_tg_search_text));

        //展示更多view
        moreView = LayoutInflater.from(getParentActivity()).inflate(R.layout.more_search_view, binding.getRoot(), false);
        RTextView morTv = moreView.findViewById(R.id.tv_more);
        morTv.setText(LocaleController.getString("act_recommend_tg_search_more", R.string.act_recommend_tg_search_more));
        moreView.setOnClickListener(view -> requestRecommendData(false));

        //搜索监听
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchText = editable.toString();
                if (TextUtils.isEmpty(searchText)) return;
                binding.ivClose.setVisibility(View.VISIBLE);

                mHandler.removeCallbacks(mRunnable);
                mHandler.postDelayed(mRunnable, 500);
            }
        });

        //下拉刷新
        binding.refreshLayout.setOnRefreshListener(refreshLayout -> requestRecommendData(true));

        //rv
        binding.rvRecommend.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        mRecommendVerAdp = new RecommendVerAdp();
        moreView.setVisibility(View.GONE);
        mRecommendVerAdp.addFooterView(moreView);
        binding.rvRecommend.setAdapter(mRecommendVerAdp);

        binding.rvTg.setLayoutManager(new LinearLayoutManager(getParentActivity()));
        mTgSearchResultAdp = new TgSearchResultAdp();
        binding.rvTg.setAdapter(mTgSearchResultAdp);

        //点击事件
        mRecommendVerAdp.setOnItemClickListener((adapter, view, position) -> {
            AdapterUtil.recommendListIndexChatActOperation(
                    this,
                    mRecommendVerAdp,
                    mRecommendVerAdp.getData().get(position),
                    position
            );
        });

        mTgSearchResultAdp.setOnItemClickListener((adapter, view, position) -> {
            //打开群聊或者频道
            Bundle args = new Bundle();
            args.putLong("chat_id", mTgSearchResultAdp.getData().get(position).id);
            presentFragment(new ChatActivity(args));
        });
    }

    /**
     * 数据请求
     */
    private int page = 1;
    private int pageSize = 8;

    private void requestRecommendData(boolean ifRefresh) {
        if (ifRefresh) {
            page = 1;
        } else {
            page++;
        }

        EasyHttp.post(new ApplicationLifecycle())
                .api(new HotRecommendApi(page, pageSize)
                        .setChat_title(searchText)
                ).request(new OnHttpListener<BaseBean<BaseLoadmoreModel<HotRecommendData>>>() {
                    @Override
                    public void onSucceed(BaseBean<BaseLoadmoreModel<HotRecommendData>> result) {
                        List<HotRecommendData> resultData = result.getData().getData();
                        if (result.getData().whetherRemaining()) {
                            moreView.setVisibility(View.VISIBLE);
                        } else {
                            moreView.setVisibility(View.GONE);
                        }

                        //添加到集合
                        if (ifRefresh) {
                            mRecommendVerAdp.setList(resultData);
                        } else {
                            mRecommendVerAdp.addData(resultData);
                        }

                        binding.tvTeleblockSearch.setVisibility(mRecommendVerAdp.getData().isEmpty() ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onFail(Exception e) {
                    }

                    @Override
                    public void onEnd(Call call) {
                        binding.refreshLayout.finishRefresh();
                    }
                });
    }

    private void search() {
        mTgSearchResultAdp.getData().clear();
        mTgSearchResultAdp.notifyDataSetChanged();
        TLRPC.TL_contacts_search req = new TLRPC.TL_contacts_search();
        req.q = searchText;
        req.limit = 100;
        ConnectionsManager.getInstance(UserConfig.selectedAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                TLRPC.TL_contacts_found res = (TLRPC.TL_contacts_found) response;
                MessagesController.getInstance(UserConfig.selectedAccount).putUsers(res.users, false);
                MessagesController.getInstance(UserConfig.selectedAccount).putChats(res.chats, false);
                MessagesStorage.getInstance(UserConfig.selectedAccount).putUsersAndChats(res.users, res.chats, false, true);

                mTgSearchResultAdp.setList(res.chats);

                binding.tvTgSearch.setVisibility(mTgSearchResultAdp.getData().isEmpty() ? View.GONE : View.VISIBLE);
            }
        }), ConnectionsManager.RequestFlagFailOnServerErrors);
    }
}