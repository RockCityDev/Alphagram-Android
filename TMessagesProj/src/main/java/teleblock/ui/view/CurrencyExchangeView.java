package teleblock.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.databinding.ViewCurrencyExchangeBinding;

/**
 * 创建日期：2022/6/9
 * 描述：
 */
public class CurrencyExchangeView extends FrameLayout {

    public CurrencyExchangeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        ViewCurrencyExchangeBinding.inflate(LayoutInflater.from(getContext()), this, true);
    }
}