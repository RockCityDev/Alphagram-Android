
package teleblock.widget.font;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.ruffian.library.widget.RTextView;

public class MediumTextView extends RTextView {

    public MediumTextView(Context context) {
        super(context);
        init(context);
    }

    public MediumTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * 设置字体
     *
     * @param context
     */
    private void init(Context context) {
        AssetManager assets = context.getAssets();
        Typeface font = Typeface.createFromAsset(assets, "fonts/rmedium.ttf");
        setTypeface(font);
        setIncludeFontPadding(false);
    }
}