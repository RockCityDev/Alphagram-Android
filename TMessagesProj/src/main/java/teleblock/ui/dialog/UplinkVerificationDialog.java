package teleblock.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ScreenUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogUplinkVerificationBinding;

/**
 * 上链验证
 */
public class UplinkVerificationDialog extends BaseAlertDialog {

    private DialogUplinkVerificationBinding binding;

    public UplinkVerificationDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogUplinkVerificationBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        getWindow().getAttributes().width = ScreenUtils.getScreenWidth();
        getWindow().getAttributes().height = ScreenUtils.getScreenHeight();
        initView();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("uplink_verification_title", R.string.uplink_verification_title));
        binding.tvContent.setText(LocaleController.getString("uplink_verification_content", R.string.uplink_verification_content));
        binding.tvKnow.setText(LocaleController.getString("uplink_verification_know", R.string.uplink_verification_know));

        binding.tvKnow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

}