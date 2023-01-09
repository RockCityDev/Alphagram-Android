package teleblock.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.ScreenUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.databinding.DialogWalletOperaBinding;

import teleblock.blockchain.BlockchainConfig;
import teleblock.model.WalletNetworkConfigEntity;
import teleblock.util.MMKVUtil;
import teleblock.util.TelegramUtil;
import teleblock.util.WalletUtil;

/**
 * Time:2022/9/20
 * Author:Perry
 * Description：钱包操作对话框
 */
public class WalletOperaDialog extends BaseAlertDialog {

    private WalletOperaDialogListener listener;
    private DialogWalletOperaBinding binding;
    private String address;

    public WalletOperaDialog(@NonNull Context context, String address, WalletOperaDialogListener listener) {
        super(context);
        this.listener = listener;
        this.address = address;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogWalletOperaBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        getWindow().getAttributes().width = (int) (ScreenUtils.getScreenWidth() * 0.8);
        initView();
    }

    private void initView() {
        String format = LocaleController.getString("dialog_wallet_opera_title", R.string.dialog_wallet_opera_title);
        binding.tvTitle.setText(String.format(format, BlockchainConfig.getWalletNameByPkg(MMKVUtil.connectedWalletPkg())));
        binding.tvDisconnect.setText(LocaleController.getString("dialog_wallet_opera_disconnect", R.string.dialog_wallet_opera_disconnect));
        binding.tvChange.setText(LocaleController.getString("dialog_wallet_opera_change", R.string.dialog_wallet_opera_change));
        binding.tvCopyaddress.setText(LocaleController.getString("dialog_wallet_opera_copyaddress", R.string.dialog_wallet_opera_copyaddress));
        binding.tvViewexplorer.setText(LocaleController.getString("dialog_wallet_opera_viewexplorer", R.string.dialog_wallet_opera_viewexplorer));

        //钱包名称
        String pkg = MMKVUtil.connectedWalletPkg();
        binding.tvWalletAddress.setText(WalletUtil.formatAddress(address));
        //钱包图标
        binding.tvWalletAddress.getHelper().setIconNormalLeft(ResourceUtils.getDrawable(BlockchainConfig.getWalletIconByPkg(pkg)));

        binding.ivClose.setOnClickListener(view -> dismiss());

        //断开钱包
        binding.tvDisconnect.setOnClickListener(view -> {
            //请求服务器绑定钱包地址
            TelegramUtil.unbindWallet(() -> {
                listener.disconnect();
                dismiss();
            });
        });

        //修改钱包
        binding.tvChange.setOnClickListener(view -> {
            dismiss();
            listener.changeAddress();
        });

        //复制钱包地址
        binding.tvCopyaddress.setOnClickListener(view -> {
            dismiss();
            listener.copyAddress();
        });

        //跳转
        binding.tvViewexplorer.setOnClickListener(view -> {
            WalletNetworkConfigEntity.WalletNetworkConfigChainType chainType = MMKVUtil.currentChainConfig();
            if (chainType == null) return;
            String symbol = chainType.getMain_currency_name();
            if ("ETH".equalsIgnoreCase(symbol)) {
                Browser.openUrl(getContext(), "https://etherscan.io/address/" + address);
            } else if ("MATIC".equalsIgnoreCase(symbol)) {
                Browser.openUrl(getContext(), "https://polygonscan.com/address/" + address);
            } else if ("TT".equalsIgnoreCase(symbol)) {
                Browser.openUrl(getContext(), "https://viewblock.io/thundercore/address/" + address);
            } else if ("ROSE".equalsIgnoreCase(symbol)) {
                Browser.openUrl(getContext(), "https://explorer.emerald.oasis.dev/" + address);
            }
            dismiss();
        });
    }

    public interface WalletOperaDialogListener {
        void disconnect();

        void changeAddress();

        void copyAddress();
    }
}
