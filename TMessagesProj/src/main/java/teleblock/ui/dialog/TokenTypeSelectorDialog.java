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
import teleblock.ui.adapter.SelectorCoinAdapter;

/**
 * Time:2022/10/26
 * Author:Perry
 * Description：token类型选择对话框
 */
public class TokenTypeSelectorDialog extends Dialog {

    private DialogChaintypeSelectorBinding binding;
    private SelectorCoinAdapter mSelectorTokenAdapter;

    public TokenTypeSelectorDialog(@NonNull Context context, TokenTypeSelectorDialogListener listener) {
        super(context, R.style.dialog2);
        setCancelable(true);
        binding = DialogChaintypeSelectorBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        initView(listener);
    }

    private void initView(TokenTypeSelectorDialogListener listener) {
        binding.tvTitle.setText(LocaleController.getString("create_group_token_type", R.string.create_group_token_type));
        binding.rvSelect.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.fl.setOnClickListener(view -> dismiss());
        binding.tvTitle.setOnClickListener(view -> dismiss());

        mSelectorTokenAdapter = new SelectorCoinAdapter();
        binding.rvSelect.setAdapter(mSelectorTokenAdapter);

        //选择币种的适配器
        mSelectorTokenAdapter.setOnItemClickListener((adapter, view, position) -> {
            dismiss();
            listener.selectorTokenData(mSelectorTokenAdapter.getData().get(position));
        });
    }

    /**
     * 设置适配器数据
     * @param list
     */
    public void setList(List<WalletNetworkConfigEntity.WalletNetworkConfigEntityItem> list) {
        mSelectorTokenAdapter.setList(list);
    }

    /**
     * 设置当前的token数据
     * @param currentTokenData
     */
    public void setCurrentTokenData(WalletNetworkConfigEntity.WalletNetworkConfigEntityItem currentTokenData) {
        mSelectorTokenAdapter.setCurrentCoinData(currentTokenData);
    }

    public interface TokenTypeSelectorDialogListener {
        void selectorTokenData(WalletNetworkConfigEntity.WalletNetworkConfigEntityItem data);
    }
}
