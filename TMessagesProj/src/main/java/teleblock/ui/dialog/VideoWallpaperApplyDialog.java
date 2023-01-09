package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;

public class VideoWallpaperApplyDialog extends Dialog {

    private TextView tvApplyChat;
    private TextView tvApplyList;
    private TextView tvApplyAll;

    Context context;
    ApplyCallback callback;

    public interface ApplyCallback {
        void onSelect(int position);
    }

    public VideoWallpaperApplyDialog(@NonNull Context context, ApplyCallback setCallback) {
        super(context, R.style.dialogBottomEnter);
        this.context = context;
        this.callback = setCallback;
        setTranslucentStatus();
        setContentView(R.layout.dialog_video_wallpaper_apply);

        initView();
    }

    private void setTranslucentStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0 全透明实现
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else {//4.4 全透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void initView() {
        tvApplyChat = findViewById(R.id.tv_apply_chat);
        tvApplyList = findViewById(R.id.tv_apply_list);
        tvApplyAll = findViewById(R.id.tv_apply_all);

        tvApplyChat.setText(LocaleController.getString("ac_title_video_wallpaper_apply_chat", R.string.ac_title_video_wallpaper_apply_chat));
        tvApplyList.setText(LocaleController.getString("ac_title_video_wallpaper_apply_list", R.string.ac_title_video_wallpaper_apply_list));
        tvApplyAll.setText(LocaleController.getString("ac_title_video_wallpaper_apply_all", R.string.ac_title_video_wallpaper_apply_all));


        findViewById(R.id.holder).setOnClickListener(view -> {
            dismiss();
        });
        findViewById(R.id.tv_apply_chat).setOnClickListener(view -> {
            dismiss();
            callback.onSelect(0);
        });
        findViewById(R.id.tv_apply_list).setOnClickListener(view -> {
            dismiss();
            callback.onSelect(1);
        });
        findViewById(R.id.tv_apply_all).setOnClickListener(view -> {
            dismiss();
            callback.onSelect(2);
        });
    }
}
