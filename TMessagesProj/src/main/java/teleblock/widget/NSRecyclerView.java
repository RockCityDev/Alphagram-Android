package teleblock.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by LSD on 2021/9/10.
 * Desc 滑动冲突。斜着往上滑的时候，会滑动viewpage2
 */
public class NSRecyclerView extends RecyclerView {
    float startX = 0;  //手指碰到屏幕时的 X坐标
    float startY = 0; //手机碰到屏幕时的 Y坐标

    public NSRecyclerView(@NonNull Context context) {
        super(context);
    }

    public NSRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                //抬起手后得到的坐标，
                float endX = ev.getX();
                float endY = ev.getY();
                //得到绝对值 。
                float disX = Math.abs(endX - startX);
                float disY = Math.abs(endY - startY);
                //如果X轴 大于Y 轴，说明实在左右移动 为什么？
                // 屏幕坐标，X，Y从左上角开始。0，0
                if (disX > disY) {
                    //这个地方，判断了左右滑动的灵敏度，只有当左右滑动距离110 此时父布局才有作用，不拦截。
                    if (disX > 110) { //结束的时候大于
                        //当滑动的距离大于100的时候，才不拦截parent的事件 父控件才会有用。
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                } else {
                    // 说明是上下滑动  //canScrollVertically 检查此视图是否可以按某个方向垂直滚动。 负数表示上下滚动。正数表示左右滚动
                    //return  true如果视图可以按指定的方向滚动，否则为false。
                    //既然是上下滑动，此时，父控件就不能有 事件 true停止
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;

        }
        return super.dispatchTouchEvent(ev);
    }
}
