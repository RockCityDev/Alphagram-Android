package teleblock.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.R;

/**
 * Created by LSD on 2021/10/14.
 * Desc
 */
public class LoadingDialog extends Dialog {
    public LoadingDialog(@NonNull Context context, String loadingText) {
        super(context, R.style.dialog2);
        setContentView(R.layout.dialog_loading);
        TextView textView = findViewById(R.id.tv_loading);
        textView.setText(loadingText);
    }
}
