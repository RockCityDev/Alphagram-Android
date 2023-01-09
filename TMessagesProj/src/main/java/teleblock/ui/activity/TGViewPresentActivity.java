package teleblock.ui.activity;

import android.content.res.Configuration;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.arch.core.util.Function;

import com.blankj.utilcode.util.BarUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.INavigationLayout;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ArticleViewer;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.CountrySelectActivity;
import org.telegram.ui.LoginActivity;
import org.telegram.ui.PhotoViewer;
import org.telegram.ui.ThemePreviewActivity;

import java.io.File;
import java.util.ArrayList;

import teleblock.util.ColorUtil;

/***
 * TG的view临时这么打开吧
 */
public class TGViewPresentActivity extends BaseActivity implements INavigationLayout.INavigationLayoutDelegate {
    private ArrayList<BaseFragment> mainFragmentsStack = new ArrayList<>();
    private INavigationLayout actionBarLayout;
    private FrameLayout frameLayout;
    protected DrawerLayoutContainer drawerLayoutContainer;
    private boolean tabletFullSize;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        int color = Theme.getColor(Theme.key_actionBarDefault);
        BarUtils.setStatusBarColor(mActivity, ColorUtil.getDarkerColor(color));
        actionBarLayout = INavigationLayout.newLayout(this);
        frameLayout = new FrameLayout(this);
        setContentView(frameLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        drawerLayoutContainer = new DrawerLayoutContainer(this);
        drawerLayoutContainer.setBehindKeyboardColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        frameLayout.addView(drawerLayoutContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        if (AndroidUtilities.isTablet()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            RelativeLayout launchLayout = new RelativeLayout(this) {
                private boolean inLayout;

                @Override
                public void requestLayout() {
                    if (inLayout) {
                        return;
                    }
                    super.requestLayout();
                }

                @Override
                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    inLayout = true;
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = MeasureSpec.getSize(heightMeasureSpec);
                    setMeasuredDimension(width, height);

                    if (!AndroidUtilities.isInMultiwindow && (!AndroidUtilities.isSmallTablet() || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
                        tabletFullSize = false;
                        int leftWidth = width / 100 * 35;
                        if (leftWidth < AndroidUtilities.dp(320)) {
                            leftWidth = AndroidUtilities.dp(320);
                        }
                        actionBarLayout.getView().measure(MeasureSpec.makeMeasureSpec(leftWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                    } else {
                        tabletFullSize = true;
                        actionBarLayout.getView().measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                    }

                    inLayout = false;
                }

                @Override
                protected void onLayout(boolean changed, int l, int t, int r, int b) {
                    if (!AndroidUtilities.isInMultiwindow && (!AndroidUtilities.isSmallTablet() || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
                        actionBarLayout.getView().layout(0, 0, actionBarLayout.getView().getMeasuredWidth(), actionBarLayout.getView().getMeasuredHeight());
                    } else {
                        actionBarLayout.getView().layout(0, 0, actionBarLayout.getView().getMeasuredWidth(), actionBarLayout.getView().getMeasuredHeight());
                    }
                }
            };
            drawerLayoutContainer.addView(launchLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

            BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.catstile);
            drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

            launchLayout.addView(actionBarLayout.getView());

        } else {
            drawerLayoutContainer.addView(actionBarLayout.getView(), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        drawerLayoutContainer.setParentActionBarLayout(actionBarLayout);
        actionBarLayout.setDrawerLayoutContainer(drawerLayoutContainer);
        actionBarLayout.setFragmentStack(mainFragmentsStack);
        actionBarLayout.setDelegate(this);
        checkLayout();

        //处理请求
        handleIntent();
    }

    private void checkLayout() {
        if (!AndroidUtilities.isTablet()) {
            return;
        }
        if (!AndroidUtilities.isInMultiwindow && (!AndroidUtilities.isSmallTablet() || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
            tabletFullSize = false;
            if (actionBarLayout.getFragmentStack().size() > 0) {
                for (int a = 1; a < actionBarLayout.getFragmentStack().size(); a++) {
                    BaseFragment chatFragment = actionBarLayout.getFragmentStack().get(a);
                    chatFragment.onPause();
                    actionBarLayout.getFragmentStack().remove(a);
                    a--;
                }
            }
        } else {
            tabletFullSize = true;
        }
    }

    public void showBulletin(Function<BulletinFactory, Bulletin> createBulletin) {
        BaseFragment topFragment = null;
        if (!mainFragmentsStack.isEmpty()) {
            topFragment = mainFragmentsStack.get(mainFragmentsStack.size() - 1);
        }
        if (BulletinFactory.canShowBulletin(topFragment)) {
            createBulletin.apply(BulletinFactory.of(topFragment)).show();
        }
    }

    private void handleIntent() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("type")) {
            String type = bundle.getString("type");
            if ("toChat".equals(type)) {
                long chatId = bundle.getLong("chatId");
                Bundle args = new Bundle();
                args.putLong("chat_id", chatId);
                ChatActivity fragment = new ChatActivity(args);
                presentFragment(fragment);
            } else if ("toTheme".equals(type)) {
                Theme.ThemeInfo themeInfo = Theme.applyThemeFile(new File("/storage/emulated/0/Telegram/Telegram Documents/4_5954051856081617568.attheme"), "themeName", null, true);
                if (themeInfo != null) {
                    presentFragment(new ThemePreviewActivity(themeInfo));
                }
            }
        }
    }

    public void presentFragment(BaseFragment fragment) {
        actionBarLayout.presentFragment(fragment);
    }

    public boolean presentFragment(final BaseFragment fragment, final boolean removeLast, boolean forceWithoutAnimation) {
        return actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        actionBarLayout.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        actionBarLayout.onPause();
    }

    /*
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            BaseFragment lastFragment = null;
            if (AndroidUtilities.isTablet()) {
                if (!actionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1);
                }
            } else {
                if (!actionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1);
                }
            }
            if (lastFragment != null) {
                Bundle args = lastFragment.getArguments();
                if (lastFragment instanceof ChatActivity && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "chat");
                } else if (lastFragment instanceof GroupCreateFinalActivity && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "group");
                } else if (lastFragment instanceof WallpapersListActivity) {
                    outState.putString("fragment", "wallpapers");
                } else if (lastFragment instanceof ProfileActivity) {
                    ProfileActivity profileActivity = (ProfileActivity) lastFragment;
                    if (profileActivity.isSettings()) {
                        outState.putString("fragment", "settings");
                    } else if (profileActivity.isChat() && args != null) {
                        outState.putBundle("args", args);
                        outState.putString("fragment", "chat_profile");
                    }
                } else if (lastFragment instanceof ChannelCreateActivity && args != null && args.getInt("step") == 0) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "channel");
                }
                lastFragment.saveSelfArgs(outState);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }
    }*/

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (actionBarLayout != null) {
            actionBarLayout.onLowMemory();
        }
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        if (Build.VERSION.SDK_INT >= 23 && mode.getType() == ActionMode.TYPE_FLOATING) {
            return;
        }
        actionBarLayout.onActionModeStarted(mode);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        if (Build.VERSION.SDK_INT >= 23 && mode.getType() == ActionMode.TYPE_FLOATING) {
            return;
        }
        actionBarLayout.onActionModeFinished(mode);
    }

    /*
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && !SharedConfig.isWaitingForPasscodeEnter) {
            if (AndroidUtilities.isTablet()) {
                actionBarLayout.onKeyUp(keyCode, event);
            } else {
                if (actionBarLayout.fragmentsStack.size() == 1) {
                    if (!drawerLayoutContainer.isDrawerOpened()) {
                        if (getCurrentFocus() != null) {
                            AndroidUtilities.hideKeyboard(getCurrentFocus());
                        }
                        drawerLayoutContainer.openDrawer(false);
                    } else {
                        drawerLayoutContainer.closeDrawer(false);
                    }
                } else {
                    actionBarLayout.onKeyUp(keyCode, event);
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }*/

    @Override
    public boolean onPreIme() {
        return false;
    }

    @Override
    public boolean needPresentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation, INavigationLayout layout) {
        if (AndroidUtilities.isTablet()) {
            drawerLayoutContainer.setAllowOpenDrawer(!(fragment instanceof LoginActivity || fragment instanceof CountrySelectActivity), true);
            if (fragment instanceof ChatActivity && !((ChatActivity) fragment).isInScheduleMode()) {
                if (!tabletFullSize || tabletFullSize && layout == actionBarLayout) {
                    boolean result = !(tabletFullSize && layout == actionBarLayout && actionBarLayout.getFragmentStack().size() == 1);
                    if (!result) {
                        actionBarLayout.presentFragment(fragment, false, forceWithoutAnimation, false, false);
                    }
                    return result;
                } else if (tabletFullSize && layout != actionBarLayout) {
                    actionBarLayout.presentFragment(fragment, actionBarLayout.getFragmentStack().size() > 1, forceWithoutAnimation, false, false);
                    return false;
                } else {
                    actionBarLayout.presentFragment(fragment, actionBarLayout.getFragmentStack().size() > 1, forceWithoutAnimation, false, false);
                    return false;
                }
            }
        } else {
            drawerLayoutContainer.setAllowOpenDrawer(true, false);
        }
        return true;
    }

    @Override
    public boolean needAddFragmentToStack(BaseFragment fragment, INavigationLayout layout) {
        if (AndroidUtilities.isTablet()) {
            if (fragment instanceof ChatActivity && !((ChatActivity) fragment).isInScheduleMode()) {
                if (tabletFullSize && layout != actionBarLayout) {
                    actionBarLayout.addFragmentToStack(fragment);
                    return false;
                }
            }
        } else {
            drawerLayoutContainer.setAllowOpenDrawer(true, false);
        }
        return true;
    }

    @Override
    public boolean needCloseLastFragment(INavigationLayout layout) {
        if (AndroidUtilities.isTablet()) {
            if (layout == actionBarLayout && layout.getFragmentStack().size() <= 1) {
                finish();
                return false;
            } else if (actionBarLayout.getFragmentStack().isEmpty()) {
                finish();
                return false;
            }
        } else {
            if (layout.getFragmentStack().size() <= 1) {
                finish();
                return false;
            }
            if (layout.getFragmentStack().size() >= 2 && !(layout.getFragmentStack().get(0) instanceof LoginActivity)) {
                drawerLayoutContainer.setAllowOpenDrawer(true, false);
            }
        }
        return true;
    }

    public void rebuildAllFragments(boolean last) {
        actionBarLayout.rebuildAllFragmentViews(last, last);
    }

    @Override
    public void onRebuildAllFragments(INavigationLayout layout, boolean last) {
        if (AndroidUtilities.isTablet()) {
            actionBarLayout.rebuildAllFragmentViews(last, last);
        }
    }
}
