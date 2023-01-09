package teleblock.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.telegram.messenger.R;

import java.util.List;

public class IntroImageAdapter extends BaseQuickAdapter<Integer, BaseViewHolder> {
    Context context;
    int imgW, imgH;
    int[] backgroundColors = new int[]{Color.parseColor("#03BDFF"), Color.parseColor("#9000FF"), Color.parseColor("#3402FD")};

    public IntroImageAdapter(Context context, List<Integer> images) {
        super(R.layout.layout_intro_image_item, images);
        this.context = context;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, Integer id) {
        int position = baseViewHolder.getAdapterPosition();
        FrameLayout imageFrame = baseViewHolder.findView(R.id.image_frame);
        imageFrame.setBackgroundColor(backgroundColors[position]);

        if (imgW == 0) {
            int screenH = ScreenUtils.getScreenHeight();
            int imgContentH = 481 * screenH / 760;

            imgH = (imgContentH - ConvertUtils.dp2px(30));
            imgW = 1080 * imgH / 1443;
        }

        ImageView imageView = baseViewHolder.findView(R.id.intro_img);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
        params.width = imgW;
        params.height = imgH;
        imageView.setLayoutParams(params);

        Glide.with(context).load(id).into(imageView);
    }
}
