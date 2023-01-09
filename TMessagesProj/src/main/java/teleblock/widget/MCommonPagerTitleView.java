package teleblock.widget;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView;

public class MCommonPagerTitleView extends CommonPagerTitleView {
    public View contentView;

    public MCommonPagerTitleView(Context context) {
        super(context);
    }

    @Override
    public void setContentView(View contentView, FrameLayout.LayoutParams lp) {
        super.setContentView(contentView, lp);
        this.contentView = contentView;
    }

    public View getContentView() {
        return contentView;
    }
}
