package teleblock.ui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.flurry.android.FlurryAgent;

import org.greenrobot.eventbus.EventBus;
import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ArticleViewer;
import org.telegram.ui.PhotoViewer;

import java.util.ArrayList;

import teleblock.event.BindEventBus;

public class BaseActivity extends FragmentActivity {

    public Activity mActivity;
    INavigationLayout actionBarLayout;
    protected Dialog visibleDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //判断是否需要注册EventBus
        if (this.getClass().isAnnotationPresent(BindEventBus.class)) {
            EventBus.getDefault().register(this);
        }
        mActivity = this;
        actionBarLayout = INavigationLayout.newLayout(this);
    }

    public void presentFragment(BaseFragment baseFragment) {
        actionBarLayout.presentFragment(baseFragment, false, true, true, false);
    }


    public Dialog showDialog(Dialog dialog) {
        return showDialog(dialog, null);
    }

    public Dialog showDialog(Dialog dialog, final Dialog.OnDismissListener onDismissListener) {
        if (dialog == null) {
            return null;
        }
        try {
            if (visibleDialog != null) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
        try {
            visibleDialog = dialog;
            visibleDialog.setCanceledOnTouchOutside(true);
            visibleDialog.setOnDismissListener(dialog1 -> {
                if (onDismissListener != null) {
                    onDismissListener.onDismiss(dialog1);
                }
                if (dialog1 == visibleDialog) {
                    visibleDialog = null;
                }
            });
            visibleDialog.show();
            return visibleDialog;
        } catch (Exception e) {
            FileLog.e(e);
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //判断是否需要注销EventBus
        if (this.getClass().isAnnotationPresent(BindEventBus.class)) {
            EventBus.getDefault().removeAllStickyEvents();
            EventBus.getDefault().unregister(this);
        }
        if (visibleDialog != null) {
            visibleDialog.dismiss();
        }
    }
}
