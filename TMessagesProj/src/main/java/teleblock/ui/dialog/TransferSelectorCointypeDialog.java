package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.blankj.utilcode.util.SizeUtils;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogTransferSelectorCointypeBinding;

import java.util.List;

import teleblock.model.ui.MyCoinListData;
import teleblock.ui.adapter.TranferSelectorCoinTypeAdapter;

/**
 * Time:2022/9/30
 * Author:Perry
 * Description：选择币种对话框
 */
public class TransferSelectorCointypeDialog extends Dialog {
    private DialogTransferSelectorCointypeBinding binding;
    private TransferSelectorCointypeDialogListener mTransferSelectorCointypeDialogListener;
    //币种适配器
    private TranferSelectorCoinTypeAdapter mTranferSelectorCoinTypeAdapter;

    public TransferSelectorCointypeDialog(@NonNull Context context, TransferSelectorCointypeDialogListener listener) {
        super(context, R.style.dialog2);
        mTransferSelectorCointypeDialogListener = listener;
        binding = DialogTransferSelectorCointypeBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(binding.getRoot());
        initView();
    }

    public void setData(List<MyCoinListData> data) {
        mTranferSelectorCoinTypeAdapter.setList(data);
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("dialog_transfer_selector_cointype_title", R.string.dialog_transfer_selector_cointype_title));
        binding.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        mTranferSelectorCoinTypeAdapter = new TranferSelectorCoinTypeAdapter();
        binding.rv.setAdapter(mTranferSelectorCoinTypeAdapter);

        binding.tvTitle.setOnClickListener(view -> dismiss());

        //列表点击事件
        mTranferSelectorCoinTypeAdapter.setOnItemClickListener((adapter, view, position) -> {
            mTransferSelectorCointypeDialogListener.selectorCoin(mTranferSelectorCoinTypeAdapter.getData().get(position));
            dismiss();
        });
    }

    public interface TransferSelectorCointypeDialogListener {
        void selectorCoin(MyCoinListData coinData);
    }
}
