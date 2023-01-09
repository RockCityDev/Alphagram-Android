package teleblock.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.SizeUtils;

import timber.log.Timber;

/**
 * NFT边框
 */
public class NftBorderView extends View {

    private Paint outerCirclePaint;
    private float outerCircleWidth = SizeUtils.dp2px(6);
    private Paint innerCirclePaint;
    private float innerCircleWidth = SizeUtils.dp2px(18);
    private Paint textPaint;
    private float textSize = SizeUtils.sp2px(16);
    private String text;

    public NftBorderView(Context context) {
        this(context, null);
    }

    public NftBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        outerCirclePaint = new Paint();
        outerCirclePaint.setAntiAlias(true);
        outerCirclePaint.setStyle(Paint.Style.STROKE);
        outerCirclePaint.setStrokeWidth(outerCircleWidth);

        innerCirclePaint = new Paint();
        innerCirclePaint.setColor(Color.BLACK);
        innerCirclePaint.setAntiAlias(true);
        innerCirclePaint.setStyle(Paint.Style.STROKE);
        innerCirclePaint.setStrokeWidth(innerCircleWidth);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#EFD281"));
        textPaint.setTextSize(textSize);
        textPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float ratio = getMeasuredWidth() / 512f;
        outerCircleWidth = SizeUtils.dp2px(6 * ratio);
        outerCirclePaint.setStrokeWidth(outerCircleWidth);
        innerCircleWidth = SizeUtils.dp2px(18 * ratio);
        innerCirclePaint.setStrokeWidth(innerCircleWidth);
        textSize = SizeUtils.sp2px(16 * ratio);
        textPaint.setTextSize(textSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (text == null) return;
        Timber.i("onDraw-->"+getMeasuredWidth());
        // 绘制外部圆形轨迹
        Path gradientPath = new Path();
        float gradientSize = getMeasuredWidth() - outerCircleWidth / 2f;
        LinearGradient linearGradient = new LinearGradient((gradientSize / 2) / 2f, outerCircleWidth / 2, gradientSize / 2f, gradientSize, Color.parseColor("#01B4FF"), Color.parseColor("#8836DF"), Shader.TileMode.CLAMP);
        outerCirclePaint.setShader(linearGradient);
        RectF rectF = new RectF(outerCircleWidth / 2f, outerCircleWidth / 2f, gradientSize, gradientSize);
        gradientPath.addArc(rectF, 0, 360);
        canvas.drawPath(gradientPath, outerCirclePaint);
        // 绘制内部圆形轨迹
        Path innerCirclePath = new Path();
        float innerCircleSize = getMeasuredWidth() - outerCircleWidth - innerCircleWidth / 2f;
        rectF = new RectF(outerCircleWidth + innerCircleWidth / 2f, outerCircleWidth + innerCircleWidth / 2f, innerCircleSize, innerCircleSize);
        innerCirclePath.addArc(rectF, 0, 360);
        canvas.drawPath(innerCirclePath, innerCirclePaint);
        // 绘制文本
        float circumference = (float) ((getMeasuredWidth() - outerCircleWidth * 2 - innerCircleWidth) * Math.PI); // 圆周长
        float hOffset = circumference * 3 / 4 - (textPaint.measureText(text) / 2f); // 横向偏移量
        float vOffset = outerCircleWidth; // 垂直偏移量
        canvas.drawTextOnPath(text, innerCirclePath, hOffset, vOffset, textPaint);
        // 绘制圆形图片
//        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        canvas.drawBitmap(toRoundBitmap(bitmap), outerCircleWidth + innerCircleWidth, outerCircleWidth + innerCircleWidth, paint);
    }

    public void setNftData(String text) {
        this.text = text;
        invalidate();
    }

    /**
     * 把bitmap转成圆形
     */
    public Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = (int) (bitmap.getWidth() - outerCircleWidth * 2 - innerCircleWidth * 2);
        int height = (int) (bitmap.getHeight() - outerCircleWidth * 2 - innerCircleWidth * 2);
        int r = 0;
        // 取最短边做边长
        if (width < height) {
            r = width;
        } else {
            r = height;
        }
        // 构建一个bitmap
        Bitmap backgroundBm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // new一个Canvas，在backgroundBmp上画图
        Canvas canvas = new Canvas(backgroundBm);
        Paint p = new Paint();
        // 设置边缘光滑，去掉锯齿
        p.setAntiAlias(true);
        RectF rect = new RectF(0, 0, r, r);
        // 通过制定的rect画一个圆角矩形，当圆角X轴方向的半径等于Y轴方向的半径时，
        // 且都等于r/2时，画出来的圆角矩形就是圆形
        canvas.drawRoundRect(rect, r / 2, r / 2, p);
        // 设置当两个图形相交时的模式，SRC_IN为取SRC图形相交的部分，多余的将被去掉
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // canvas将bitmap画在backgroundBmp上
        canvas.drawBitmap(bitmap, null, rect, p);
        return backgroundBm;
    }
}