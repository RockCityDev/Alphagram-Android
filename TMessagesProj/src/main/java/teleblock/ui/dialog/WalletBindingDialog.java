package teleblock.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.blankj.utilcode.util.ScreenUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogWalletBindingBinding;

import teleblock.blockchain.BlockchainConfig;
import teleblock.blockchain.WCSessionManager;

/**
 * 钱包授权绑定
 */
public class WalletBindingDialog extends BaseAlertDialog {

    private final String pkg;
    private DialogWalletBindingBinding binding;

    public WalletBindingDialog(Context context, String pkg) {
        super(context);
        this.pkg = pkg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogWalletBindingBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        getWindow().getAttributes().width = (int) (ScreenUtils.getScreenWidth() * 0.85);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
    }

    private void initView() {
        binding.tvBindingTitle.setText(LocaleController.getString("dialog_wallet_binding_title", R.string.dialog_wallet_binding_title));
        binding.tvCancel.setText(LocaleController.getString("dialog_wallet_binding_cancel", R.string.dialog_wallet_binding_cancel));

        binding.ivWalletIcon.setImageResource(BlockchainConfig.getWalletIconByPkg(pkg));
        String name = BlockchainConfig.getWalletNameByPkg(pkg);
        String format = LocaleController.getString("dialog_wallet_binding_content", R.string.dialog_wallet_binding_content);
        binding.tvBindingContent.setText(String.format(format, name));
        binding.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WCSessionManager.getInstance().pkg = "";
                dismiss();
            }
        });
    }

}