package me.fichardu.circleprogress;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

public class CircleProgress extends View {

    private static final int RED = 0xFFE5282C; // 红色
    private static final int YELLOW = 0xFF1F909A; // 黄色
    private static final int BLUE = 0xFFFC9E12; // 蓝色
    private static final int COLOR_NUM = 3; // 颜色的种类
    private int[] COLORS;     // 用于存放颜色的数组
    private TimeInterpolator mInterpolator = new EaseInOutCubicInterpolator(); // 插值器

    private final double DEGREE = Math.PI / 180;
    private Paint mPaint;     // 圆圈上每个点的画笔
    private int mViewSize;    // 圆形所在视图的大小
    private int mPointRadius; // 画笔的半径
    private long mStartTime;  // 动画开始时间
    private long mPlayTime;   // 动画播放时间
    private boolean mStartAnim = false; // 是否播放动画
    private Point mCenter = new Point(); // 中心点

    private ArcPoint[] mArcPoint;             // 圆圈上每个圆点的画笔集合
    private static final int POINT_NUM = 15;  // 圆形上点的个数
    private static final int DELTA_ANGLE = 360 / POINT_NUM; // 圆圈上每两个点之间与中心点形成的角度数
    private long mDuration = 1000 * 2;  // 默认从开始收缩到重新展开的这一个周期内的使用时间

    public CircleProgress(Context context) {
        super(context);
        init(null, 0);
    }

    public CircleProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CircleProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mArcPoint = new ArcPoint[POINT_NUM];

        mPaint = new Paint();
        mPaint.setAntiAlias(true); // 消除锯齿
        mPaint.setStyle(Paint.Style.FILL);  // 设置圆圈上每个圆点画笔为实心

        /** 获取自定义属性的值 */
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircleProgress, defStyle, 0);
        int color1 = a.getColor(R.styleable.CircleProgress_color1, RED);
        int color2 = a.getColor(R.styleable.CircleProgress_color2, YELLOW);
        int color3 = a.getColor(R.styleable.CircleProgress_color3, BLUE);
        a.recycle();

        COLORS = new int[]{color1, color2, color3};
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 获取默认的progressbar的尺寸大小
        int defaultSize = getResources().getDimensionPixelSize(R.dimen.default_circle_view_size);
        int width = getDefaultSize(defaultSize, widthMeasureSpec);
        int height = getDefaultSize(defaultSize, heightMeasureSpec);
        mViewSize = Math.min(width, height);        // 获得圆圈所在视图的大小，取宽和搞的最小值，保证能完全显示
        setMeasuredDimension(mViewSize, mViewSize); // 测量view的大小

        // mCenter建议此处使用width/2,源码项目中使用的是mViewSize/2
        // 使用width/2 可以是圆形始终处于真个view的最中间，位置，无论是match_parent还是wrap_content
        mCenter.set(width / 2, mViewSize / 2);  // 设置中心点的位置

//        Log.i("123", mCenter.toString() + "mViewSize:" + mViewSize + ",width:" + width + ",height:" + height);
        calPoints(1.0f);   // 计算
    }

    /**
     * 计算每个小圆点的位置区域，并初始化画笔
     * @param factor 半径偏移量
     */
    private void calPoints(float factor) {
        // 这里使用mViewSize/3 * factor求得view试图内，圆圈的半径，
        // 这样处理是为了使圆圈与编剧有一个默认的距离，就是半径的一半，factor是为了控制边距的大小不建议大于1.5F
        int radius = (int) (mViewSize / 3 * factor);

        // 计算圆圈边缘每个小圆点的半径大小
        mPointRadius = radius / 10;

        // 次循环是为了初始化每个圆点的应该画在的区域
        for (int i = 0; i < POINT_NUM; ++i) {
            float x = radius * -(float) Math.cos(DEGREE * DELTA_ANGLE * i);
            float y = radius * -(float) Math.sin(DEGREE * DELTA_ANGLE * i);

            ArcPoint point = new ArcPoint(x, y, COLORS[i % COLOR_NUM]);
            mArcPoint[i] = point;
            point = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        // 将画布移动到视图中心
        canvas.translate(mCenter.x, mCenter.y);

        float factor = getFactor();

        // 旋转画布
        canvas.rotate(36 * factor);
//        Log.i("123","factor:"+factor);
        float x, y;
        for (int i = 0; i < POINT_NUM; ++i) {
            mPaint.setColor(mArcPoint[i].color);
            float itemFactor = getItemFactor(i, factor);
            x = mArcPoint[i].x - 2 * mArcPoint[i].x * itemFactor;
            y = mArcPoint[i].y - 2 * mArcPoint[i].y * itemFactor;
            canvas.drawCircle(x, y, mPointRadius, mPaint);
        }

        canvas.restore();

        if (mStartAnim) {
            postInvalidate();
        }
    }


    /**
     * 获取一个0~1之间的浮点型的随机数
     * @return
     */
    private float getFactor() {
        if (mStartAnim) {
            mPlayTime = AnimationUtils.currentAnimationTimeMillis() - mStartTime;
        }
        float factor = mPlayTime / (float) mDuration;
        return factor % 1f;
    }

    private float getItemFactor(int index, float factor) {
        float itemFactor = (factor - 0.66f / POINT_NUM * index) * 3;
        if (itemFactor < 0f) {
            itemFactor = 0f;
        } else if (itemFactor > 1f) {
            itemFactor = 1f;
        }
        return mInterpolator.getInterpolation(itemFactor);
    }

    /**
     * 开始旋转动画
     */
    public void startAnim() {
        mPlayTime = mPlayTime % mDuration;
        mStartTime = AnimationUtils.currentAnimationTimeMillis() - mPlayTime;
        mStartAnim = true;
        postInvalidate();
    }

    /**
     * 重制
     */
    public void reset() {
        stopAnim();
        mPlayTime = 0;
        postInvalidate();
    }


    /**
     * 停止动画
     */
    public void stopAnim() {
        mStartAnim = false;
    }

    /**
     * 设置插值器
     * @param interpolator
     */
    public void setInterpolator(TimeInterpolator interpolator) {
        mInterpolator = interpolator;
    }

    /**
     * 设置持续时间
     * @param duration
     */
    public void setDuration(long duration) {
        mDuration = duration;
    }

    /**
     * 设置半径
     * @param factor
     */
    public void setRadius(float factor) {
        stopAnim();
        calPoints(factor);
        startAnim();
    }

    static class ArcPoint {
        float x;
        float y;
        int color;

        ArcPoint(float x, float y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
    }

}
