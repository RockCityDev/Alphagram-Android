package teleblock.util;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import org.telegram.ui.ActionBar.Theme;

/**
 * Created by LSD on 2021/9/9.
 * Desc
 */
public class ColorUtil {
    // 获取更深颜色
    public static int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv
        // make darker
        hsv[1] = hsv[1] + 0.1f; // 饱和度更高
        hsv[2] = hsv[2] - 0.1f; // 明度降低
        int darkerColor = Color.HSVToColor(hsv);
        return darkerColor;
    }

    // 获取更浅的颜色
    public static int getBrighterColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv); // convert to hsv

        hsv[1] = hsv[1] - 0.1f; // less saturation
        hsv[2] = hsv[2] + 0.1f; // more brightness
        int darkerColor = Color.HSVToColor(hsv);
        return darkerColor;
    }

    //改变ImageView图标颜色
    public static void setBottomTabState(ImageView imageView, boolean select) {
        imageView.setSelected(select);
        int color;
        if (select) {
            color = Theme.getColor(Theme.key_actionBarDefaultTitle);
        } else {
            color = Theme.getColor(Theme.key_actionBarDefaultTitle);
        }
        Drawable drawable = imageView.getDrawable();
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        imageView.setImageDrawable(drawable);
    }
}
