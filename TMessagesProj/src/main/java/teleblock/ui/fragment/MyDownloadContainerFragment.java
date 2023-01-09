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

import com.google.android.exoplayer2.metadata.emsg.EventMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

import teleblock.event.EventBusTags;
import teleblock.event.MessageEvent;
import teleblock.event.data.DeleteVideoSelectEvent;


/**
 * Created by LSD on 2021/3/20.
 * Desc
 */
public class MyDownloadContainerFragment extends BaseFragment {
    Handler mHandler = new Handler();
    public boolean edit = false;
    TextView tvSaveCache;
    TextView tvSaveAlbum;
    public ImageView ivEdit;
    public TextView tvCancel;

    TextView tv_download_checkall;
    TextView tv_download_select_num;

    LinearLayout action_layout;
    LinearLayout action_state_layout;
    boolean checkAll = false;

    int currentTab = 0;
    int extTab = -1;
    String downloadTags[] = new String[]{"save_cache", "save_album"};

    public static MyDownloadContainerFragment instance(int tab) {
        MyDownloadContainerFragment fragment = new MyDownloadContainerFragment();
        Bundle args = new Bundle();
        args.putInt("tab", tab);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        EventBus.getDefault().unregister(this);
        super.onDestroyView();
    }

    @Override
    protected View getFrameLayout(LayoutInflater inflater) {
        return inflater.inflate(R.layout.fragment_mydownload, null);
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
        tvCancel = rootView.findViewById(R.id.tv_download_cancel);

        tvSaveCache = rootView.findViewById(R.id.tv_save_cache);
        tvSaveCache.setText(LocaleController.getString("vw_save_cache", R.string.vw_save_cache));
        tvCancel.setText(LocaleController.getString("ac_down_cancel", R.string.ac_down_cancel));

        tvSaveCache.setOnClickListener(view -> {
            tvSaveCache.setSelected(true);
            tvSaveAlbum.setSelected(false);

            if (currentTab == 0) return;
            currentTab = 0;
            fragmentLoop();
        });
        tvSaveAlbum = rootView.findViewById(R.id.tv_save_album);
        tvSaveAlbum.setText(LocaleController.getString("vw_save_album", R.string.vw_save_album));
        tvSaveAlbum.setOnClickListener(view -> {
            tvSaveCache.setSelected(false);
            tvSaveAlbum.setSelected(true);

            if (currentTab == 1) return;
            currentTab = 1;
            fragmentLoop();
        });
        (ivEdit = rootView.findViewById(R.id.iv_edit)).setOnClickListener(view -> {
            edit = true;
            changeEdit();
        });
        tvCancel.setOnClickListener(view -> {
            edit = false;
            changeEdit();
        });
        (tv_download_checkall = rootView.findViewById(R.id.tv_download_checkall)).setOnClickListener(view -> {
            changCheckAll();
        });

        if (currentTab == 0) {
            tvSaveCache.setSelected(true);
            tvSaveAlbum.setSelected(false);
        } else if (currentTab == 1) {
            tvSaveCache.setSelected(false);
            tvSaveAlbum.setSelected(true);
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
        Fragment fragment = manager.findFragmentByTag(downloadTags[currentTab]);
        if (fragment != null) {
            if (currentTab == 0) {
                MySaveCacheFragment mySaveCacheFragment = (MySaveCacheFragment) fragment;
                mySaveCacheFragment.checkAllOrUnCheckAll(checkAll);
            } else if (currentTab == 1) {
                MySaveAlbumFragment mySaveAlbumFragment = (MySaveAlbumFragment) fragment;
                mySaveAlbumFragment.checkAllOrUnCheckAll(checkAll);
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
        Fragment fragment = manager.findFragmentByTag(downloadTags[currentTab]);
        if (fragment != null) {
            if (currentTab == 0) {
                MySaveCacheFragment mySaveCacheFragment = (MySaveCacheFragment) fragment;
                mySaveCacheFragment.notifyEditChange(edit);
                mySaveCacheFragment.checkAllOrUnCheckAll(false);
            } else if (currentTab == 1) {
                MySaveAlbumFragment mySaveAlbumFragment = (MySaveAlbumFragment) fragment;
                mySaveAlbumFragment.notifyEditChange(edit);
                mySaveAlbumFragment.checkAllOrUnCheckAll(false);
            }
        }
    }

    private void fragmentLoop() {
        if (!isAdded()) return;
        FragmentManager manager = getChildFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment fragment = manager.findFragmentByTag(downloadTags[currentTab]);
        if (fragment == null) {
            if (currentTab == 0) {
                fragment = MySaveCacheFragment.instance();
            } else if (currentTab == 1) {
                fragment = MySaveAlbumFragment.instance();
            }
            transaction.add(R.id.download_frame_layout, fragment, downloadTags[currentTab]);
        }
        if (extTab != -1) {
            Fragment ex_fragment = manager.findFragmentByTag(downloadTags[extTab]);
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

            case EventBusTags.DELETE_VIDEO_SELECT:
                DeleteVideoSelectEvent data = (DeleteVideoSelectEvent) event.getData();
                String format = LocaleController.getString("ac_downed_text_check_num", R.string.ac_downed_text_check_num);
                String showText = String.format(format, data.num);
                tv_download_select_num.setText(showText);
                if (data.selectAll) {
                    tv_download_checkall.setText(LocaleController.getString("ac_downed_text_checkno", R.string.ac_downed_text_checkno));
                } else {
                    tv_download_checkall.setText(LocaleController.getString("ac_downed_text_checkall", R.string.ac_downed_text_checkall));
                }
                break;
        }
    }
}
