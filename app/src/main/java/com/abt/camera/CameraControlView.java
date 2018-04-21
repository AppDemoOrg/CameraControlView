package com.abt.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * @描述： @相机控制面板
 * @作者： @黄卫旗
 * @创建时间： @2017-10-17
 */
public class CameraControlView extends View {

    private static final String TAG = CameraControlView.class.getSimpleName();
    private static final int SMOOTH_TO_LEFT = 1;  //定义向左向右滑动
    private static final int SMOOTH_TO_RIGHT = 2;
    private int mCenterX, mCenterY;
    private int mWidgetWidth, mWidgetHeight;
    private int mRadius;//设置绘制的圆的半径
    private static final int mInterval = 75; // 图标圆心 和 弧线圆心 纵坐标间距

    private Context mContext;
    private Paint mPaint;
    private int mIndex = 0;
    private int mSize;

    private int mDownX, mDownY; //按下的X，Y轴坐标
    private int mPicSize = 120; //设置需要绘制的图片的大小
    private int mSmoothDistance = 0; //滑动间距
    private int mCenterPosition = 2;  //最中间位置的坐标
    private static final int mDEFAULT_POSITION = 5;
    private int mSelectedPosition = mDEFAULT_POSITION;  // 中间位置
    private Canvas mCanvas;

    private float mTextSize = dipToPx(15);
    private static final float DEFAULT_BORDER_WIDTH = 6f;//圆弧的宽度
    private int mPadding = dipToPx(10);//默认胖和瘦距离上面圆环的距离
    private int mMarging = (int) (Math.max(DEFAULT_BORDER_WIDTH, mTextSize));
    private int DEFAULT_LITLE_WIDTH = dipToPx(7);

    private static final float mStartAngle = 251;
    private static final float mTravelAngle = 38;
    private float mCurrentAngle = mStartAngle;
    private boolean mIsMoving = false;

    /**
     * dip 转换成 px
     */
    public int dipToPx(float dip) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f * (dip >= 0 ? 1 : -1));
    }

    private int[] mSrc = new int[] {
            R.drawable.take_video_select,
            R.drawable.take_picture_select,
            R.drawable.take_live_select,
    };

    private int[] mSrcTemp = new int[] {
            R.drawable.take_video_select,
            R.drawable.take_picture_select,
            R.drawable.take_live_select,
    };

    private int[] mSrcSelect = new int[] {
            R.drawable.take_video,
            R.drawable.take_picture,
            R.drawable.take_live,
    };

    public CameraControlView(Context context) {
        super(context);
        init(context);
    }

    public CameraControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(0xffff2222);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidgetWidth = MeasureSpec.getSize(widthMeasureSpec);
        mWidgetHeight = MeasureSpec.getSize(heightMeasureSpec);
        mRadius = mWidgetWidth / 2 + 800;
        mCenterX = mWidgetWidth / 2;
        mCenterY = mWidgetHeight + mRadius / 2 + mWidgetHeight *13/12;
        Log.d(TAG, "mWidgetWidth  : "+ mWidgetWidth);
        Log.d(TAG, "mWidgetHeight  : "+ mWidgetHeight);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        int layerId = canvas.saveLayer(0, 0, mWidgetWidth, mWidgetHeight, mPaint, Canvas.ALL_SAVE_FLAG);
        canvas.restoreToCount(layerId);

        drawIcons(canvas);
        drawArc(canvas);
        drawCircle(canvas);
    }

    private void drawArc(Canvas canvas) {
        //绘制默认灰色的圆弧
        Paint paintDefault = new Paint();
        paintDefault.setColor(getResources().getColor(R.color.time_lapse_arc));
        paintDefault.setStrokeCap(Paint.Cap.SQUARE);//设置笔刷的样式 Paint.Cap.Round, Cap.SQUARE等分别为圆形、方形

        Log.d(TAG, "getWidth : "+getWidth());
        Log.d(TAG, "getHeight : "+getHeight());
        RectF oval = new RectF(mCenterX-mRadius, mCenterY-mRadius+mInterval,
                mCenterX+mRadius, mCenterY+mRadius+mInterval);
        paintDefault.setStyle(Paint.Style.STROKE); // 设置填充样式 FILL:填充内部 STROKE:仅描边
        paintDefault.setAntiAlias(true);//抗锯齿功能
        paintDefault.setStrokeWidth(DEFAULT_BORDER_WIDTH);//设置画笔宽度
        canvas.drawArc(oval, mStartAngle, mTravelAngle, false, paintDefault);//绘制默认灰色的圆弧
    }

    private void drawCircle(Canvas canvas) {
        //4---绘制圆弧上的小圆球--根据currentAngle
        Paint paintCircle = new Paint();
        paintCircle.setStyle(Paint.Style.FILL);//设置填充样式
        paintCircle.setAntiAlias(true);//抗锯齿功能
        paintCircle.setColor(Color.YELLOW);

        /**
         * 第一个参数为正则顺时针，否则逆时针
         * 后面两个参数是圆心
         * 画布的旋转一定要在，画图形之前进行旋转
         */
        int nextX;
        int nextY;
        if (!mIsMoving) {
            nextX = (mWidgetWidth) / 10 * mSelectedPosition;
            nextY = (mCenterY + mInterval) - (int) Math.sqrt(Math.pow(mRadius, 2) - Math.pow((nextX - mCenterX), 2));
        } else {
            nextX = mDownX;
            nextY = (mCenterY + mInterval) - (int) Math.sqrt(Math.pow(mRadius, 2) - Math.pow((nextX - mCenterX), 2));
        }
        canvas.drawCircle(nextX, nextY, DEFAULT_LITLE_WIDTH, paintCircle);
    }

    /**
     * 绘制背景
     */
    private void drawBackground(Canvas canvas) {
        Bitmap bitmapBg = BitmapFactory.decodeResource(getResources(), R.drawable.test_bg);  //取得背景图片
        Bitmap bitmap = Bitmap.createScaledBitmap(bitmapBg, mWidgetWidth, mWidgetHeight, false); //将取得的背景图片压缩到指定的大小
        if (bitmapBg != null) {
            //设置重复模式，TileMode.CLAMP:用边缘色彩填充多余空间
            mPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            Bitmap des = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(des);
            Paint paint = new Paint();
            c.drawCircle(mCenterX, mCenterY, mRadius, mPaint);  //绘制圆弧
            canvas.drawBitmap(des, 0, 0, paint);
        }
    }

    /**
     * 绘制圆弧上面显示的图标
     */
    private void drawIcons(Canvas canvas) {
        int nextX, nextY;
        Bitmap bitmap;
        Matrix matrix = new Matrix();
        for (int i = 1; i < 10; i += 4) {  //将屏幕分为10份，取 1 5 9 来作为显示位置
            //取得相对应的bitmap
            if (mSelectedPosition == i) {
                bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), mSrcSelect[(i-1)/4]), mPicSize, mPicSize, false);
            } else {
                bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), mSrcTemp[(i-1)/4]), mPicSize, mPicSize, false);
            }
            //取得下一个图片的x ， Y 轴坐标
            nextX = (mWidgetWidth) / 10 * i;
            nextY = mCenterY - (int) Math.sqrt(Math.pow(mRadius, 2) - Math.pow((nextX-mCenterX), 2));
            nextX = nextX - mPicSize / 2;
            nextY = nextY - mPicSize / 2;
            matrix.reset();
            matrix.setTranslate(nextX, nextY);
            canvas.drawBitmap(bitmap, matrix, null);  //将bitmap绘制到画布上
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                handleClickEvent(mDownX, mDownY);
                return true;

            case MotionEvent.ACTION_MOVE:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                int min = (mWidgetWidth) / 10 * 1;
                int max = (mWidgetWidth) / 10 * 9;
                if (mDownX < min) {
                    mDownX = min;
                }
                if (mDownX > max) {
                    mDownX = max;
                }
                handlerMoveEvent(mDownX, mDownY);
                return true;

            case MotionEvent.ACTION_UP:
                // TODO 取消滑动处理
                int min1 = (mWidgetWidth) / 10 * 1;
                int min3 = (mWidgetWidth) / 10 * 3;
                int min5 = (mWidgetWidth) / 10 * 5;
                int min7 = (mWidgetWidth) / 10 * 7;
                int max9 = (mWidgetWidth) / 10 * 9;
                if (mDownX < min3) {
                    mDownX = min1;
                    mSelectedPosition = 1;
                }
                if (mDownX >= min3 && mDownX < min7) {
                    mDownX = min5;
                    mSelectedPosition = 5;
                }
                if (mDownX >= min7) {
                    mDownX = max9;
                    mSelectedPosition = 9;
                }
                if (null != onClickListener) {
                    onClickListener.itemOnclickListener(mSelectedPosition);
                }
                this.invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void handlerMoveEvent(int downX, int downY) {
        mIsMoving = true;
        this.invalidate();
    }

    private void handleClickEvent(int downX, int downY) {
        Log.d(TAG, "downX Position : "+ downX);
        Log.d(TAG, "downY Position : "+ downY);
        int nextX, nextY;
        boolean isClick = false;
        for (int i = 1; i < 10; i += 4) {
            nextX = (mWidgetWidth) / 10 * i;
            nextY = mCenterY - (int) Math.sqrt(Math.pow(mRadius, 2) - Math.pow((nextX-mCenterX), 2));
            nextX = nextX - mPicSize / 2;
            nextY = nextY - mPicSize / 2;
            isClick = new Rect(nextX - mPicSize, nextY - mPicSize, nextX + mPicSize, nextY + mPicSize).contains(downX, downY);
            Log.d(TAG, "isClick : "+ isClick);
            if (isClick && null != onClickListener) {
                onClickListener.itemOnclickListener(i);
                mSelectedPosition = i;
                Log.d(TAG, "Clicked Position : "+ i);
                this.invalidate();
                break;
            }
        }
    }

    /**
     * 滑动处理
     * 分为向左滑动，和向右滑动
     */
    private void smoothToLeftOrRight(int type) {
        int position = 0;
        if (type == SMOOTH_TO_LEFT) {  //向左滑动和向右滑动，坐标变化
            ++mIndex;
            if (mIndex >= mSrc.length) mIndex = 0;
        } else if (type == SMOOTH_TO_RIGHT) {
            --mIndex;
            if (mIndex < 0) mIndex = mSrc.length - 1;
        }
        //实现循序数组
        for (int i = 0; i < mSrc.length; i++) {
            position = (mIndex + i);
            if (position >= mSrc.length) {
                position -= mSrc.length;
            }
            mSrcTemp[i] = mSrc[position];
        }
        if (mIndex <= 2) {
            mCenterPosition = mIndex + 2;
        } else {
            mCenterPosition = mIndex - 3;
        }
        mSrcTemp[2] = mSrcSelect[mCenterPosition];  //设置最中间的图标为选中状态
        Log.d(TAG, "mIndex : " + mIndex);
        postInvalidate();
    }

    private OnClickListener onClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void itemOnclickListener(int index);
    }
}
