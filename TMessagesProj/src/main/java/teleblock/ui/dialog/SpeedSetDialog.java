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

import org.telegram.messenger.R;

import teleblock.util.MMKVUtil;


public class SpeedSetDialog extends Dialog implements View.OnClickListener {
    public interface SpeedSetCallback {
        void onChange(float speed);
    }

    Context context;
    TextView tv_speed20;
    TextView tv_speed15;
    TextView tv_speed125;
    TextView tv_speed10;
    TextView tv_speed075;
    SpeedSetCallback callback;

    public SpeedSetDialog(@NonNull Context context, SpeedSetCallback setCallback) {
        super(context, R.style.dialog);
        this.context = context;
        this.callback = setCallback;
        setTranslucentStatus();
        setContentView(R.layout.dialog_speed_set);
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
        findViewById(R.id.view_holder).setOnClickListener(view -> {
            dismiss();
        });
        tv_speed20 = findViewById(R.id.tv_speed2);
        tv_speed15 = findViewById(R.id.tv_speed1_5);
        tv_speed125 = findViewById(R.id.tv_speed1_25);
        tv_speed10 = findViewById(R.id.tv_speed1);
        tv_speed075 = findViewById(R.id.tv_speed0_75);

        tv_speed20.setOnClickListener(this::onClick);
        tv_speed15.setOnClickListener(this::onClick);
        tv_speed125.setOnClickListener(this::onClick);
        tv_speed10.setOnClickListener(this::onClick);
        tv_speed075.setOnClickListener(this::onClick);

        checkSelect();
    }

    private void checkSelect() {
        tv_speed20.setSelected(false);
        tv_speed15.setSelected(false);
        tv_speed125.setSelected(false);
        tv_speed10.setSelected(false);
        tv_speed075.setSelected(false);
        float speed = MMKVUtil.getPlaySpeed();
        if (speed == 2.0f) {
            tv_speed20.setSelected(true);
        } else if (speed == 1.5f) {
            tv_speed15.setSelected(true);
        } else if (speed == 1.25f) {
            tv_speed125.setSelected(true);
        } else if (speed == 1.0f) {
            tv_speed10.setSelected(true);
        } else if (speed == 0.75f) {
            tv_speed075.setSelected(true);
        }
    }

    @Override
    public void onClick(View view) {
        float speed = 1.0f;
        switch (view.getId()) {
            case R.id.tv_speed2:
                speed = 2.0f;
                break;
            case R.id.tv_speed1_5:
                speed = 1.5f;
                break;
            case R.id.tv_speed1_25:
                speed = 1.25f;
                break;
            case R.id.tv_speed1:
                speed = 1.0f;
                break;
            case R.id.tv_speed0_75:
                speed = 0.75f;
                break;
        }
        MMKVUtil.setPlaySpeed(speed);
        callback.onChange(speed);
        dismiss();
    }
}
