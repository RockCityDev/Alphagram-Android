package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogClearUnreadMsgBinding;

/**
 * Time:2022/7/21
 * Author:Perry
 * Description：清除未读消息对话框
 */
public class ClearUnReadMessageDialog extends Dialog {

    private DialogClearUnreadMsgBinding binding;
    private ClearUnReadMessageDialogListener mClearUnReadMessageDialogListener;

    public ClearUnReadMessageDialog(
            @NonNull Context context,
            ClearUnReadMessageDialogListener mClearUnReadMessageDialogListener
    ) {
        super(context, R.style.dialog2);
        this.mClearUnReadMessageDialogListener = mClearUnReadMessageDialogListener;
        binding = DialogClearUnreadMsgBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        binding.tvTitle.setText(LocaleController.getString("dialog_clean_all",R.string.dialog_clean_all));
        binding.tvClearMessage.setText(LocaleController.getString("dialog_clean_tv_ok",R.string.dialog_clean_tv_ok));
        binding.tvCancel.setText(LocaleController.getString("dialog_clean_tv_cancel",R.string.dialog_clean_tv_cancel));

        //清除未读
        binding.tvClearMessage.setOnClickListener(view -> {
            if (mClearUnReadMessageDialogListener != null) {
                mClearUnReadMessageDialogListener.clearUnReadMessage();
            }
            dismiss();
        });

        //取消弹窗
        binding.tvCancel.setOnClickListener(view -> {
            dismiss();
        });
    }

    public interface ClearUnReadMessageDialogListener {
        void clearUnReadMessage();
    }
}
