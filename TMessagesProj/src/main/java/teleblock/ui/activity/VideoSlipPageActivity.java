package teleblock.ui.activity;

import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.BarUtils;

import org.telegram.messenger.R;

import teleblock.model.VideoSlipEntity;
import teleblock.ui.fragment.VideoSlipPageFragment2;


/**
 * Created by LSD on 2021/5/3.
 * Desc 视频抖音样式载体Activity
 */
public class VideoSlipPageActivity extends BaseActivity {
    private VideoSlipEntity entity;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        BarUtils.transparentStatusBar(mActivity);//透明状态栏
        setContentView(R.layout.activity_video_slip);

        getExtras();
        initView();
    }

    private void getExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            entity = (VideoSlipEntity) bundle.getSerializable("entity");
        }
    }

    private void initView() {
        findViewById(R.id.iv_back).setOnClickListener(view -> {
            finish();
        });

        Fragment fragment = VideoSlipPageFragment2.instance(entity);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.slip_frame, fragment);
        transaction.show(fragment);
        transaction.commitAllowingStateLoss();
    }
}
