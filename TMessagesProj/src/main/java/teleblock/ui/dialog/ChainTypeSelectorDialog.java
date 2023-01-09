package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogChaintypeSelectorBinding;

import java.util.List;

import teleblock.model.WalletNetworkConfigEntity;
import teleblock.ui.adapter.TranferSelectorChainTypeAdapter;
import teleblock.util.MMKVUtil;

/**
 * Time:2022/9/29
 * Author:Perry
 * Description：选择链类型的对话框
 */
public class ChainTypeSelectorDialog extends Dialog {

    private DialogChaintypeSelectorBinding binding;

    private TranferSelectorChainTypeAdapter mTranferSelectorChainTypeAdapter;

    public ChainTypeSelectorDialog setCurrentChainType(WalletNetworkConfigEntity.WalletNetworkConfigChainType mWalletNetworkConfigChainType) {
        if (mTranferSelectorChainTypeAdapter.getData().isEmpty()) {
            mTranferSelectorChainTypeAdapter.setList(MMKVUtil.getWalletNetworkConfigEntity().getChainType());
        }

        mTranferSelectorChainTypeAdapter.setCurrentChainTypeData(mWalletNetworkConfigChainType);
        return this;
    }

    public ChainTypeSelectorDialog(
            @NonNull Context context,
            TransferSelectorChaintypeDialogListener listener
    ) {
        super(context, R.style.dialog2);
        setCancelable(true);
        binding = DialogChaintypeSelectorBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        initView(listener);
    }

    private void initView(TransferSelectorChaintypeDialogListener listener) {
        binding.tvTitle.setText(LocaleController.getString("dialog_transfer_selector_chaintype_title", R.string.dialog_transfer_selector_chaintype_title));
        binding.rvSelect.setLayoutManager(new LinearLayoutManager(getContext()));

        mTranferSelectorChainTypeAdapter = new TranferSelectorChainTypeAdapter();
        binding.rvSelect.setAdapter(mTranferSelectorChainTypeAdapter);

        binding.fl.setOnClickListener(view -> dismiss());
        binding.tvTitle.setOnClickListener(view -> dismiss());
        //点击事件
        mTranferSelectorChainTypeAdapter.setOnItemClickListener((adapter, view, position) -> {
            listener.selectorChainData(mTranferSelectorChainTypeAdapter.getData().get(position));
            dismiss();
        });
    }

    public interface TransferSelectorChaintypeDialogListener {
        void selectorChainData(WalletNetworkConfigEntity.WalletNetworkConfigChainType data);
    }
}
