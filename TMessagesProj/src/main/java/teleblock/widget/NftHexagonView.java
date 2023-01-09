package teleblock.widget;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

import com.blankj.utilcode.util.SizeUtils;

import java.util.ArrayList;

import teleblock.util.TGLog;
import timber.log.Timber;

/**
 * Time:2022/8/24
 * Author:Perry
 * Description：nft六边形view
 */
public class NftHexagonView extends View {

    //默认模式
    public static final int DEFULT = 1;
    //强制显示
    public static final int FORCE_DISPLAY = 2;
    //强制不显示
    public static final int FORCE_NOT_SHOW = 3;

    //view宽高
    private float parentWidth = (float) SizeUtils.dp2px(110f);
    //传入的drawable
    private Bitmap bitmap;
    //线条弧度
    private float sixAngle = (float) SizeUtils.dp2px(6f);
    //第一层边框宽度  默认
    private float firstBorderWidth = (float) SizeUtils.dp2px(5f);
    //第二层边框宽度 默认
    private float secondBorderWidth = (float) SizeUtils.dp2px(10f);
    //文字大小
    private float textSizePx = (float) SizeUtils.sp2px(7f);
    //六角形图片
    private Bitmap hexagonBitmap;

    private double radian60 = 60 * Math.PI / 180;

    //文字path路径集合
    private ArrayList<Path> textPathList = new ArrayList<>();

    private Canvas mCanvas;
    private Paint mPaint = new Paint();
    private Path path = new Path();
    private String tvLeftTop = ""; // 左上角
    private String tvRightBottom = ""; // 右下角

    private boolean ifPhotoShowNftID; //图片是否显示nftid 和 黑边框
    private int model = DEFULT; //显示模式 1默认模式，图片的nftid和黑边框是否显示根据图片大小来决定，2 强制显示nftid 3强制不显示nftid

    {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setPathEffect(new CornerPathEffect(sixAngle));
        mPaint.setStyle(Paint.Style.FILL);
    }

    /**
     * 渐变色
     *
     * @return
     */
    private LinearGradient linearGradientColor() {
        return new LinearGradient(0f, 0f, 0f, parentWidth,
                new int[]{
                        Color.parseColor("#01B4FF"),
                        Color.parseColor("#8836DF")
                },
                new float[]{
                        0f, 0.8f
                },
                Shader.TileMode.CLAMP
        );
    }


    public NftHexagonView(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setNftData(String nft_name, String nft_token_id) {
        tvLeftTop = nft_name == null ? "" : nft_name;
        tvRightBottom = nft_token_id == null ? "" : nft_token_id;
    }

    public void setModel(int model) {
        this.model = model;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    /**
     * 测量
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        changeSize(getMeasuredWidth(), getMeasuredHeight());
    }

    /**
     * 缩放值
     *
     * @param measuredWidth
     * @param measuredHeight
     */
    public void changeSize(int measuredWidth, int measuredHeight) {
        Timber.i("changeSize-->" + measuredWidth);
        if (parentWidth == measuredWidth) return;
        parentWidth = measuredWidth;
        float ratio = SizeUtils.px2dp(measuredWidth) / 108f;
        firstBorderWidth = (float) SizeUtils.dp2px(5f * ratio);
        secondBorderWidth = (float) SizeUtils.dp2px(10f * ratio);
        textSizePx = (float) SizeUtils.sp2px(7f * ratio);
        boolean bigSize = SizeUtils.px2dp(measuredWidth) > 60;

        switch (model) {
            case DEFULT:
                ifPhotoShowNftID = bigSize;
                break;
            case FORCE_DISPLAY:
                ifPhotoShowNftID = true;
                break;

            case FORCE_NOT_SHOW:
                ifPhotoShowNftID = false;
                break;
        }

        ifPhotoShowNftID = false;//后面删除掉这个代码
        if (!ifPhotoShowNftID) {
            secondBorderWidth = 0f;
        }
    }

    /**
     * 绘制
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        TGLog.erro("绘制了");
        if (canvas != null) {
            mCanvas = canvas;
        }

        //绘制第一个六边形
        mPaint.setShader(linearGradientColor());//设置最外层渐变色边框
        drawHexagon(path, parentWidth);
        mCanvas.drawPath(path, mPaint);
        mCanvas.save();

        if (ifPhotoShowNftID) {
            //绘制第二个六边形
            mPaint.setShader(null);
            mPaint.setColor(Color.BLACK);
            drawHexagon(path, parentWidth - (firstBorderWidth * 2));
            mCanvas.translate(firstBorderWidth, firstBorderWidth);//画布平移居中
            mCanvas.drawPath(path, mPaint);
            mCanvas.save();

            //绘制文字
            mPaint.setColor(Color.parseColor("#FFE088"));
            mPaint.setTextSize(textSizePx);

            //文字高度
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
            float fontHeight = fontMetrics.bottom - fontMetrics.top;

            //垂直偏移量
            float vOffset1 = (secondBorderWidth - fontHeight) / 2 + firstBorderWidth / 2;
            float vOffset2 = secondBorderWidth - (secondBorderWidth - fontHeight) / 2 - firstBorderWidth;
            //横向偏移量
            float hOffset1 = (parentWidth / 2f - mPaint.measureText(tvRightBottom)) / 2;
            float hOffset2 = (parentWidth / 2f - mPaint.measureText(tvLeftTop)) / 2;

            mCanvas.drawTextOnPath(tvRightBottom, textPathList.get(0), hOffset1, -vOffset1, mPaint);
            mCanvas.drawTextOnPath(tvLeftTop, textPathList.get(1), hOffset2, vOffset2, mPaint);
            mCanvas.save();
        }

        //绘制第三个六边形图片
        hexagonBitmap = canvasBitmap(bitmap);
        if (hexagonBitmap != null) {
            mCanvas.translate(ifPhotoShowNftID ? secondBorderWidth : firstBorderWidth, ifPhotoShowNftID ? secondBorderWidth : firstBorderWidth);//画布平移居中
            mCanvas.drawBitmap(hexagonBitmap, 0f, 0f, mPaint);
            mCanvas.restore();
        }
    }

    /**
     * 绘制六边形
     */
    private void drawHexagon(Path path, float width) {
        float circlePointX = width / 2f;
        float xLength = (float) (circlePointX * sin(radian60));
        float yLength = (float) (circlePointX * cos(radian60));

        textPathList.clear();
        path.reset();
        path.moveTo(circlePointX, 0f);

        path.lineTo(circlePointX + xLength, yLength);

        path.lineTo(circlePointX + xLength, circlePointX + yLength);

        path.lineTo(circlePointX, width);

        path.lineTo(circlePointX - xLength, circlePointX + yLength);

        path.lineTo(circlePointX - xLength, yLength);
        path.close();

        Path path1 = new Path();
        path1.moveTo(circlePointX, width);
        path1.lineTo(circlePointX + xLength, circlePointX + yLength);
        textPathList.add(path1);

        Path path2 = new Path();
        path2.moveTo(circlePointX - xLength, yLength);
        path2.lineTo(circlePointX, 0f);
        textPathList.add(path2);
    }

    /**
     * 绘制六边形bitmap
     */
    private Bitmap canvasBitmap(Bitmap bitmap) {
        int bitmapWH = (int) (parentWidth - ((firstBorderWidth + secondBorderWidth) * 2));
        if (bitmapWH <= 0) return null;

        //缩放bitmap到尺寸与最里面六边形宽高一样
        int width = bitmap.getWidth();
        float scaleWidth = (float) bitmapWH / width;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleWidth);
        if (width <= 0 || bitmap.getHeight() <= 0) return null;
        Bitmap newBitMap = Bitmap.createBitmap(bitmap, 0, 0, width, bitmap.getHeight(), matrix, true);

        Path path = new Path();
        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setPathEffect(new CornerPathEffect(sixAngle));
        mPaint.setStyle(Paint.Style.FILL);

        //绘制六边形
        Bitmap cBitmap = Bitmap.createBitmap(bitmapWH, bitmapWH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cBitmap);
        drawHexagon(path, bitmapWH);
        canvas.drawPath(path, mPaint);

        if (newBitMap != null) {//混合渲染模式绘制bitmap
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(newBitMap, 0f, 0f, mPaint);
        }
        return cBitmap;
    }
}