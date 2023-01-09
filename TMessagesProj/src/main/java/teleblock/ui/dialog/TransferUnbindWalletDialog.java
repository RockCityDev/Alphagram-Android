package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogTransferUnbindWalletBinding;

/**
 * Time:2022/9/8
 * Author:Perry
 * Description：转账未绑定钱包对话框
 */
public class TransferUnbindWalletDialog extends Dialog {

    public TransferUnbindWalletDialog(@NonNull Context context, boolean isSelf, TransferUnbindWalletDialogListener listener) {
        super(context, R.style.dialog2);
        setCancelable(true);
        DialogTransferUnbindWalletBinding binding = DialogTransferUnbindWalletBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        if (isSelf) { //自己未绑定钱包
            binding.tvTitle.setText(LocaleController.getString("chat_transfer_meunbind_wallet", R.string.chat_transfer_meunbind_wallet));
            binding.tvContent.setText(LocaleController.getString("chat_transfer_meubbind_tips", R.string.chat_transfer_meubbind_tips));
            binding.tvBtn.setText(LocaleController.getString("chat_transfer_bind_wallet", R.string.chat_transfer_bind_wallet));
        } else {
            binding.tvTitle.setText(LocaleController.getString("chat_transfer_otherunbind_wallet", R.string.chat_transfer_otherunbind_wallet));
            binding.tvContent.setText(LocaleController.getString("chat_transfer_otherubbind_tips", R.string.chat_transfer_otherubbind_tips));
            binding.tvBtn.setText(LocaleController.getString("chat_transfer_invite_bind_wallet", R.string.chat_transfer_invite_bind_wallet));
        }

        binding.tvBtn.setOnClickListener(view -> {
            listener.click();
            dismiss();
        });

        binding.cl.setOnClickListener(view -> dismiss());
    }

    public interface TransferUnbindWalletDialogListener {
        void click();
    }
}
