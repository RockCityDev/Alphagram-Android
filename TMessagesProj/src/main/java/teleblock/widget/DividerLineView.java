package teleblock.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import org.telegram.ui.ActionBar.Theme;

/**
 * Time:2022/10/28
 * Author:Perry
 * Description：分割线view 主题适配
 */
public class DividerLineView extends View {

    public DividerLineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Theme.getColor(Theme.key_divider));
    }
}
