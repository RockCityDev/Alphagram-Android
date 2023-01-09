package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogGastipsBinding;

/**
 * Time:2022/9/13
 * Author:Perry
 * Description：什么是gas费 dialog
 */
public class GasTipsDialog extends Dialog {
    private DialogGastipsBinding binding;

    public GasTipsDialog(@NonNull Context context) {
        super(context, R.style.dialog2);
        binding = DialogGastipsBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        setCancelable(true);

        binding.tvTitle.setText(LocaleController.getString("gas_tips_dialog_title", R.string.gas_tips_dialog_title));
        binding.tvContent.setText(LocaleController.getString("gas_tips_dialog_content", R.string.gas_tips_dialog_content));
        binding.tvBottomTips.setText(LocaleController.getString("gas_tips_dialog_bottom_tips", R.string.gas_tips_dialog_bottom_tips));

        binding.tvTitle.setOnClickListener(view -> dismiss());
    }
}
