package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import org.telegram.messenger.R;
import org.telegram.messenger.databinding.DialogFountaionBinding;

/**
 * Time:2023/2/10
 * Author:Perry
 * Description：
 */
public class FountaionDialog extends Dialog {
    private DialogFountaionBinding binding;

    public FountaionDialog(@NonNull Context context) {
        super(context, R.style.dialog2);
        binding = DialogFountaionBinding.inflate(LayoutInflater.from(context));
        setContentView(binding.getRoot());

        binding.tvBtn.setOnClickListener(view -> dismiss());

    }
}
