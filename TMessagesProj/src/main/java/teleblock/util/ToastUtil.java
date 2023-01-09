package teleblock.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Bulletin;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;

public class ToastUtil {

    public static void showStealthModeView(Context context, boolean openStealthMode) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.toast_stealth_mode, null);
        ImageView iv_stealth_mode = rootView.findViewById(R.id.iv_stealth_mode);
        TextView tv_stealth_mode = rootView.findViewById(R.id.tv_stealth_mode);
        if (openStealthMode) {
            iv_stealth_mode.setImageResource(R.drawable.ic_toast_stealth_mode_open);
            tv_stealth_mode.setText(LocaleController.getString("stealth_toast_open", R.string.stealth_toast_open));
        } else {
            iv_stealth_mode.setImageResource(R.drawable.ic_toast_stealth_mode_close);
            tv_stealth_mode.setText(LocaleController.getString("stealth_toast_close", R.string.stealth_toast_close));
        }
        ToastUtils.make().setGravity(Gravity.CENTER, 0, 0).show(rootView);
    }

    public static void showSaveToGalleryView(Context context, BulletinFactory.FileType fileType, int filesAmount, Theme.ResourcesProvider resourcesProvider) {
        Bulletin.LottieLayout layout = new Bulletin.LottieLayout(context, resourcesProvider);
        layout.setLayoutParams(LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        layout.setAnimation(fileType.icon.resId, fileType.icon.layers);
        layout.textView.setText(fileType.getText(filesAmount));
        if (fileType.icon.paddingBottom != 0) {
            layout.setIconPaddingBottom(fileType.icon.paddingBottom);
        }
        layout.imageView.playAnimation();
        ToastUtils.make().setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, AndroidUtilities.dp(50)).show(layout);

    }
}