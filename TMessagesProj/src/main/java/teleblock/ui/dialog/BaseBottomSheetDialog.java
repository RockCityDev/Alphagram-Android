package teleblock.ui.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.KeyboardUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.telegram.messenger.R;


public class BaseBottomSheetDialog extends BottomSheetDialog {

    public BaseBottomSheetDialog(@NonNull Context context) {
        super(context, R.style.BottomSheetDialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(Color.TRANSPARENT);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void show() {
        super.show();
    }

    public void resetPeekHeight() {
        FrameLayout bottomSheet = findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        //直接展开
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        //是否开启STATE_HIDDEN状态
        behavior.setHideable(true);
        //设置STATE_COLLAPSED状态的高度
        bottomSheet.post(new Runnable() {
            @Override
            public void run() {
                behavior.setPeekHeight(bottomSheet.getHeight());
            }
        });
    }

    @Override
    public void dismiss() {
        KeyboardUtils.hideSoftInput(getWindow());
        super.dismiss();
    }
}