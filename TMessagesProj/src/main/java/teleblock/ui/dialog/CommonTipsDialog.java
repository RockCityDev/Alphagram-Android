package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogCommonTipsBinding;

public class CommonTipsDialog extends Dialog {
    private DialogCommonTipsBinding binding;
    String title, leftText, rightText;

    public CommonTipsDialog(@NonNull Context context, String title) {
        this(context, title, "", "");
    }

    public CommonTipsDialog(@NonNull Context context, String title, String leftText, String rightText) {
        super(context, R.style.dialog2);
        this.title = title;
        this.leftText = leftText;
        this.rightText = rightText;

        binding = DialogCommonTipsBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        initView();
    }

    public CommonTipsDialog setTitle(String title) {
        this.title = title;
        binding.tvTitle.setText(title);
        return this;
    }

    public CommonTipsDialog setRightTextAndColor(String rightText, int color) {
        this.rightText = rightText;
        binding.tvRight.setText(rightText);
        if (color != 0) binding.tvRight.setTextColor(color);
        return this;
    }

    public CommonTipsDialog setHideLeftText() {
        binding.tvLeft.setVisibility(View.GONE);
        return this;
    }


    public CommonTipsDialog setLeftTextAndColor(String leftText, int color) {
        this.leftText = leftText;
        binding.tvLeft.setText(leftText);
        if (color != 0) binding.tvLeft.setTextColor(color);
        return this;
    }

    private void initView() {
        binding.tvTitle.setText(title);
        binding.tvLeft.setText(leftText);
        binding.tvRight.setText(rightText);

        binding.tvLeft.setOnClickListener(view -> {
            dismiss();
            onLeftClick();
        });
        binding.tvRight.setOnClickListener(view -> {
            dismiss();
            onRightClick();
        });
    }

    public void onLeftClick() {
    }

    public void onRightClick() {
    }
}
