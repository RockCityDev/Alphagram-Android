package teleblock.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.databinding.ViewUserWalletBinding;

/**
 * 创建日期：2022/6/9
 * 描述：用户简介-钱包页
 */
public class UserWalletView extends FrameLayout {

    public UserWalletView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        ViewUserWalletBinding.inflate(LayoutInflater.from(getContext()), this, true);
    }
}