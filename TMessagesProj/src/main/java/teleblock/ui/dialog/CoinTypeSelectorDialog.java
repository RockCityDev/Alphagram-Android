package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

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
 * Description：币种类型选择对话框
 */
public class CoinTypeSelectorDialog extends Dialog {

    private DialogChaintypeSelectorBinding binding;
    private SelectorCoinAdapter mSelectorCoinAdapter;

    public CoinTypeSelectorDialog(
            @NonNull Context context,
            CoinTypeSelectorDialogListener listener
    ) {
        super(context, R.style.dialog2);
        setCancelable(true);
        binding = DialogChaintypeSelectorBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        initView(listener);
    }

    private void initView(CoinTypeSelectorDialogListener listener) {
        binding.tvTitle.setText(LocaleController.getString("create_group_coin_type", R.string.create_group_coin_type));
        binding.rvSelect.setLayoutManager(new LinearLayoutManager(getContext()));

        binding.fl.setOnClickListener(view -> dismiss());
        binding.tvTitle.setOnClickListener(view -> dismiss());

        mSelectorCoinAdapter = new SelectorCoinAdapter();
        binding.rvSelect.setAdapter(mSelectorCoinAdapter);

        //选择币种的适配器
        mSelectorCoinAdapter.setOnItemClickListener((adapter, view, position) -> {
            dismiss();
            listener.selectorCoinData(mSelectorCoinAdapter.getData().get(position));
        });
    }

    /**
     * 设置适配器数据
     * @param list
     */
    public void setList(List<WalletNetworkConfigEntity.WalletNetworkConfigEntityItem> list) {
        mSelectorCoinAdapter.setList(list);
    }

    /**
     * 设置当前的币种数据
     * @param currentCoinData
     */
    public void setCurrentCoinData(WalletNetworkConfigEntity.WalletNetworkConfigEntityItem currentCoinData) {
        mSelectorCoinAdapter.setCurrentCoinData(currentCoinData);
    }

    public interface CoinTypeSelectorDialogListener {
        void selectorCoinData(WalletNetworkConfigEntity.WalletNetworkConfigEntityItem data);
    }

}
