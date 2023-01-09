package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogSelectorGasfeeBinding;

import java.math.BigDecimal;
import java.util.List;

import teleblock.model.ui.MyCoinListData;
import teleblock.model.ui.SelectorGasFeeData;
import teleblock.ui.adapter.SelectorGassFeeAdapter;

/**
 * Time:2022/9/19
 * Author:Perry
 * Description：选择gas费用对话框
 */
public class SelectorGasFeeDialog extends Dialog {
    private DialogSelectorGasfeeBinding binding;

    private SelectorGassFeeAdapter mSelectorGassFeeAdapter;
    private SelectorGasFeeDialogListener mSelectorGasFeeDialogListener;

    public SelectorGasFeeDialog(
            @NonNull Context context,
            SelectorGasFeeDialogListener listener
    ) {
        super(context, R.style.dialog2);
        this.mSelectorGasFeeDialogListener = listener;
        binding = DialogSelectorGasfeeBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        initView();
    }

    /**
     * 设置gas数据
     * @param gasFeeList
     * @param dollerPrice
     * @param gasLimit
     * @param symbol
     */
    public void setGasData(
            List<SelectorGasFeeData> gasFeeList,
            BigDecimal dollerPrice,
            String gasLimit,
            String symbol,
            BigDecimal coinPrice,
            BigDecimal mainCoinPrice
    ) {
        mSelectorGassFeeAdapter.setGasData(dollerPrice, gasLimit, symbol, coinPrice, mainCoinPrice);
        mSelectorGassFeeAdapter.setList(gasFeeList);
    }

    private void initView() {
        //标题
        binding.tvTitle.setText(LocaleController.getString("dialog_selector_gassfee_title", R.string.dialog_selector_gassfee_title));
        //adapter
        mSelectorGassFeeAdapter = new SelectorGassFeeAdapter();
        binding.rvSelecGassfee.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSelecGassfee.setAdapter(mSelectorGassFeeAdapter);

        //点击事件
        mSelectorGassFeeAdapter.setOnItemClickListener((adapter, view, position) -> {
            mSelectorGassFeeAdapter.selectorUi(position);
            mSelectorGasFeeDialogListener.clickListener(mSelectorGassFeeAdapter.getData().get(position).getPrice());
            dismiss();
        });

        //关闭对话框
        binding.tvTitle.setOnClickListener(view -> dismiss());
    }

    public interface SelectorGasFeeDialogListener {
        void clickListener(String price);
    }
}