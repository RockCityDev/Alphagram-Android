package teleblock.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.blankj.utilcode.util.SizeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;

import java.util.concurrent.ExecutionException;

public class GlideHelper {

    public static void displayImage(Context context, ImageView imageView, String url) {
        displayImage(context, imageView, url, R.color.grey, R.color.grey);
    }

    public static void displayImage(Context context, ImageView imageView, String url, int error) {
        displayImage(context, imageView, url, error, R.color.grey);
    }

    public static void displayImage(Context context, ImageView imageView, String url, int error, int holder) {
        RequestOptions options = new RequestOptions()
                .skipMemoryCache(false) // 内存缓存
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 磁盘缓存所有图
                .error(error)
                .placeholder(holder);
        Glide.with(context)
                .load(url)
                .apply(options)
                .into(imageView);
    }

    public static void displayImage(Context context, ImageView imageView, int drawable) {
        Glide.with(context)
                .load(drawable)
                .into(imageView);
    }


    public static void displayRoundImage(Context context, ImageView imageView, int drawable, int radius) {
        RequestOptions roundOptions = new RequestOptions()
                .transform(new RoundedCorners(SizeUtils.dp2px(radius)));
        Glide.with(context)
                .load(drawable)
                .apply(roundOptions)
                .into(imageView);
    }

    public static void displayRoundImage(Context context, ImageView imageView, String url, int radius) {
        displayRoundImage(context, imageView, url, radius, R.color.grey);
    }

    public static void displayRoundImage(Context context, ImageView imageView, String url, int radius, int holder) {
        RequestOptions roundOptions = new RequestOptions()
                .skipMemoryCache(false) // 内存缓存
                .diskCacheStrategy(DiskCacheStrategy.ALL) // 磁盘缓存所有图
                .transform(new RoundedCorners(SizeUtils.dp2px(radius)))
                .placeholder(holder)
                .error(holder);
        Glide.with(context)
                .load(url)
                .apply(roundOptions)
                .into(imageView);
    }

    /**
     * 这是一个耗时的操作需要异步处理
     * @param url 通过URL得到 Drawable
     * @return
     */
    public static void getDrawableGlide(Context context, String url, @NotNull RequestDrawableSuccessfulListener listener) {
        new Thread(() -> {
            try {
                Drawable drawable = Glide.with(context).load(url).submit().get();
                AndroidUtilities.runOnUIThread(() -> {
                    if (drawable != null) {
                        listener.requestDrawableSuccessful(drawable);
                    }
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public interface RequestDrawableSuccessfulListener { void requestDrawableSuccessful(Drawable drawable);}
}