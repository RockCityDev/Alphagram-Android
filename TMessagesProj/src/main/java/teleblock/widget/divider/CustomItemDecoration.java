package teleblock.widget.divider;

import android.graphics.Color;

import androidx.recyclerview.widget.RecyclerView;


public class CustomItemDecoration extends DividerItemDecoration {

    private int color = Color.TRANSPARENT;
    private int orientation, spanCount;
    private float widthDp, heightDp, startPaddingDp, endPaddingDp;
    private boolean hasFirstLast;

    public CustomItemDecoration(int spanCount, float widthDp) {
        super();
        this.spanCount = spanCount;
        this.widthDp = widthDp;
    }

    public CustomItemDecoration(int spanCount, float widthDp, float heightDp) {
        super();
        this.spanCount = spanCount;
        this.widthDp = widthDp;
        this.heightDp = heightDp;
    }

    public CustomItemDecoration(int orientation, int color, float widthDp) {
        super();
        this.orientation = orientation;
        this.color = color;
        this.widthDp = widthDp;
    }

    public CustomItemDecoration(int spanCount, float widthDp, boolean hasFirstLast) {
        super();
        this.spanCount = spanCount;
        this.widthDp = widthDp;
        this.hasFirstLast = hasFirstLast;
    }

    public CustomItemDecoration(int spanCount, float widthDp, float heightDp, boolean hasFirstLast) {
        super();
        this.spanCount = spanCount;
        this.widthDp = widthDp;
        this.heightDp = heightDp;
        this.hasFirstLast = hasFirstLast;
    }

    public CustomItemDecoration(int orientation, int color, float widthDp, float paddingDp) {
        super();
        this.orientation = orientation;
        this.color = color;
        this.widthDp = widthDp;
        this.startPaddingDp = this.endPaddingDp = paddingDp;
    }

    public CustomItemDecoration(int orientation, float widthDp, float startPaddingDp, float endPaddingDp) {
        super();
        this.orientation = orientation;
        this.widthDp = widthDp;
        this.startPaddingDp = startPaddingDp;
        this.endPaddingDp = endPaddingDp;
    }

    @Override
    public Y_Divider getDivider(int itemPosition) {
        Y_DividerBuilder builder = new Y_DividerBuilder();
        if (spanCount != 0) {
            float left, right;
            int column = itemPosition % spanCount;
            if (hasFirstLast) {
                left = widthDp - column * widthDp / spanCount;
                right = (column + 1) * widthDp / spanCount;
            } else {
                left = column * widthDp / spanCount;
                right = widthDp - (column + 1) * widthDp / spanCount;
            }
            builder.setLeftSideLine(true, color, left, startPaddingDp, endPaddingDp)
                    .setRightSideLine(true, color, right, startPaddingDp, endPaddingDp)
                    .setTopSideLine(itemPosition < spanCount, color, heightDp, startPaddingDp, endPaddingDp)
                    .setBottomSideLine(true, color, heightDp, startPaddingDp, endPaddingDp);
        } else {
            switch (orientation) {
                case RecyclerView.HORIZONTAL:
                    builder.setLeftSideLine(itemPosition == 0, color, widthDp, startPaddingDp, endPaddingDp)
                            .setRightSideLine(true, color, widthDp, startPaddingDp, endPaddingDp);
                    break;
                case RecyclerView.VERTICAL:
                    builder.setTopSideLine(itemPosition == 0 && color == 0, color, widthDp, startPaddingDp, endPaddingDp)
                            .setBottomSideLine(true, color, widthDp, startPaddingDp, endPaddingDp);
                    break;
                default:
                    break;
            }
        }
        return builder.create();
    }
}
