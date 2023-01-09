package teleblock.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import org.telegram.messenger.R;


public abstract class ViewPagerFragment extends BaseFragment {

    protected boolean isInit = false;
    protected boolean isLoad = false;
    protected ProgressBar mProgressWheel;
    protected FrameLayout mContentLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_lazy_view_pager, container, false);
        mContentLayout = view.findViewById(R.id.lazy_content_view);
        mProgressWheel = view.findViewById(R.id.loading_progress);
        rootView = getFrameLayout(inflater);
        mContentLayout.setVisibility(View.GONE);
        mContentLayout.addView(rootView);
        isInit = true;
        /**初始化的时候去加载数据**/
        onViewCreated();
        isCanLoadData();
        return view;
    }

    /**
     * 视图是否已经对用户可见，系统的方法
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isCanLoadData();
    }

    /**
     * 是否可以加载数据
     * 可以加载数据的条件：
     * 1.视图已经初始化
     * 2.视图对用户可见
     */
    private void isCanLoadData() {
        if (!isInit) {
            return;
        }
        if (getUserVisibleHint()) {
            if (!isLoad) {
                lazyLoad();
                isLoad = true;
            }
        } else {
            if (isLoad) {
                stopLoad();
            }
        }
    }

    /**
     * 当视图初始化并且对用户可见的时候去真正的加载数据
     */
    protected abstract void lazyLoad();

    /**
     * 当视图已经对用户不可见并且加载过数据，如果需要在切换到其他页面时停止加载数据，可以覆写此方法
     */
    protected void stopLoad() {
    }

    public void hideInitLoading() {
        mContentLayout.setVisibility(View.VISIBLE);
        mProgressWheel.setVisibility(View.GONE);
    }

    public void showInitLoading() {
        mContentLayout.setVisibility(View.GONE);
        mProgressWheel.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isInit = false;
        isLoad = false;
    }

}
