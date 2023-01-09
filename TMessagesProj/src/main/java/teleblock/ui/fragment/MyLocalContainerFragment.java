package teleblock.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.file.KKLocalFileManager;


/**
 * Created by LSD on 2021/3/20.
 * Desc
 */
public class MyLocalContainerFragment extends BaseFragment {

    LinearLayout action_layout;
    LinearLayout action_state_layout;
    TextView tv_download_checkall;
    TextView tv_download_select_num;

    Handler mHandler = new Handler();
    public boolean edit = false;
    TextView tv_local_scan;
    TextView tv_local_cache;
    public ImageView ivEdit;
    public TextView tvCancel;
    boolean checkAll = false;

    int currentTab = 0;
    int extTab = -1;
    String localTabs[] = new String[]{"scan", "cache"};

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_mylocal, null);
    }

    public static MyLocalContainerFragment instance() {
        MyLocalContainerFragment fragment = new MyLocalContainerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    protected void onViewCreated() {
        EventBus.getDefault().register(this);
        currentTab = getArguments().getInt("tab");
        initView();
    }

    private void initView() {
        action_layout = rootView.findViewById(R.id.action_layout);
        action_state_layout = rootView.findViewById(R.id.action_state_layout);
        tv_download_select_num = rootView.findViewById(R.id.tv_download_select_num);
        tv_local_scan = rootView.findViewById(R.id.tv_local_scan);
        tvCancel = rootView.findViewById(R.id.tv_download_cancel);
        tv_download_checkall = rootView.findViewById(R.id.tv_download_checkall);
        ivEdit = rootView.findViewById(R.id.iv_edit);
        tv_local_cache = rootView.findViewById(R.id.tv_local_cache);

        tv_local_scan.setText(LocaleController.getString("vw_local_scan", R.string.vw_local_scan));
        tv_local_scan.setOnClickListener(view -> {
            tv_local_scan.setSelected(true);
            tv_local_cache.setSelected(false);

            if (currentTab == 0) return;
            currentTab = 0;
            fragmentLoop();
        });

        tv_local_cache.setText(LocaleController.getString("vw_local_cache", R.string.vw_local_cache));
        tv_local_cache.setOnClickListener(view -> {
            tv_local_scan.setSelected(false);
            tv_local_cache.setSelected(true);

            if (currentTab == 1) return;
            currentTab = 1;
            fragmentLoop();
        });

        ivEdit.setOnClickListener(view -> {
            edit = true;
            changeEdit();
        });

        tvCancel.setText(LocaleController.getString("ac_down_cancel", R.string.ac_down_cancel));
        tvCancel.setOnClickListener(view -> {
            edit = false;
            changeEdit();
        });

        tv_download_checkall.setText(LocaleController.getString("ac_downed_text_checkall", R.string.ac_downed_text_checkall));
        tv_download_checkall.setOnClickListener(view -> {
            changCheckAll();
        });

        if (currentTab == 0) {
            tv_local_scan.setSelected(true);
            tv_local_cache.setSelected(false);
        }
        mHandler.postDelayed(() -> fragmentLoop(), 200);
    }

    private void changCheckAll() {
        checkAll = !checkAll;
        if (checkAll) {
            tv_download_checkall.setText(LocaleController.getString("ac_downed_text_checkno", R.string.ac_downed_text_checkno));
        } else {
            tv_download_checkall.setText(LocaleController.getString("ac_downed_text_checkall", R.string.ac_downed_text_checkall));
        }
        FragmentManager manager = getChildFragmentManager();
        Fragment fragment = manager.findFragmentByTag(localTabs[currentTab]);
        if (fragment != null) {
            if (fragment instanceof MyLocalScanFragment) {
                ((MyLocalScanFragment) fragment).checkAllOrUnCheckAll(checkAll);
            } else if (fragment instanceof MyLocalCacheFragment) {
                ((MyLocalCacheFragment) fragment).checkAllOrUnCheckAll(checkAll);
            }
        }
    }

    private void changeEdit() {
        if (edit) {
            ivEdit.setImageResource(R.drawable.ic_mix_close);
            action_state_layout.setVisibility(View.VISIBLE);
            action_layout.setVisibility(View.GONE);
        } else {
            ivEdit.setImageResource(R.drawable.ic_mix_delete);
            action_state_layout.setVisibility(View.GONE);
            action_layout.setVisibility(View.VISIBLE);
            tv_download_checkall.setText(LocaleController.getString("ac_downed_text_checkall", R.string.ac_downed_text_checkall));
        }

        FragmentManager manager = getChildFragmentManager();
        Fragment fragment = manager.findFragmentByTag(localTabs[currentTab]);
        if (fragment != null) {
            if (fragment instanceof MyLocalScanFragment) {
                ((MyLocalScanFragment) fragment).notifyEditChange(edit);
            } else if (fragment instanceof MyLocalCacheFragment) {
                ((MyLocalCacheFragment) fragment).notifyEditChange(edit);
            }
        }
    }

    private void fragmentLoop() {
        if (!isAdded()) return;
        FragmentManager manager = getChildFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag(localTabs[currentTab]);
        if (fragment == null) {
            if (currentTab == 0) {
                fragment = MyLocalScanFragment.instance();
            } else if (currentTab == 1) {
                fragment = MyLocalCacheFragment.instance();
            }
            transaction.add(R.id.local_frame_layout, fragment, localTabs[currentTab]);
        }
        if (extTab != -1) {
            Fragment ex_fragment = manager.findFragmentByTag(localTabs[extTab]);
            if (ex_fragment != null && !ex_fragment.isHidden()) {
                transaction.hide(ex_fragment);
            }
        }
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();
        extTab = currentTab;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventBus(MessageEvent event) {
        switch (event.getType()) {
            case EventBusTags.DELETE_VIDEO_OK:
                edit = false;
                changeEdit();
                break;
        }
    }

    public void setSelectText(int size, boolean checkAll) {
        String format = LocaleController.getString("ac_downed_text_check_num", R.string.ac_downed_text_check_num);
        String showText = String.format(format, size);
        tv_download_select_num.setText(showText);
        if (checkAll) {
            tv_download_checkall.setText(LocaleController.getString("ac_downed_text_checkno", R.string.ac_downed_text_checkno));
        } else {
            tv_download_checkall.setText(LocaleController.getString("ac_downed_text_checkall", R.string.ac_downed_text_checkall));
        }
    }
}
