package com.abt.camera.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.abt.camera.R;

/**
 * @描述： @相机控制面板
 * @作者： @黄卫旗
 * @创建时间： @2018-02-06
 */
public class CameraSwitchView extends View {

    private static final String TAG = CameraSwitchView.class.getSimpleName();
    private Paint mPaint;                                       /** 画笔 */
    private Scroller mScroller;                                 /** 控件滚动条 */

    private static final int mBlocks = 10;                      /** 控件宽度分块数 */
    private static final int mInterval = 2;                     /** 控件图标间隔 */
    private static final int mPhotoIconIndex = 3;               /** 拍照图标索引 */
    private static final int mVideoIconIndex = 5;               /** 录像图标索引 */

    private static final int mNextY = 20;                       /** Y坐标高度 */
    private static final int mNextCenterY = 80;                 /** Y坐标高度 */
    private static final int mSlidingOffset = 24;               /** 左右滑动偏移量 */
    private static final int mPicSize = 120;                    /** 需要绘制的图片大小 */
    private static final int mSlidingDuration = 800;            /** 滑动时间 */

    private static final int mLeftMoveDistance = 0;             /** 左滑距离 */
    private int mRightMoveDistance = -216;                      /** 右滑距离 = - (mTotalWidth / 10 * 2) */
    private int mTotalWidth, mTotalHeight;                      /** 控件的宽高 */
    private int mDownX, mDownY;                                 /** 按下的坐标 */
    private int mScrollOffset;                                  /** 控件滚动偏移量 */
    private int mLastScrollX = mLeftMoveDistance;               /** 最后滚动X坐标*/

    private boolean mInitialized = false;                       /** 初始化标志 */
    private boolean mIsClick = false;                           /** 是否点击标志 */
    private boolean mVideoSelected = false;                     /** 是否是视频标志 */
    private volatile int mSelectedPosition = mPhotoIconIndex;   /** 选中的位置索引 */

    private int[] mSrcNormal = new int[] {                      /** 正常图标*/
            R.drawable.photo_normal,
            R.drawable.video_normal,
    };

    private int[] mSrcSelect = new int[] {                      /** 选中图标*/
            R.drawable.photo_selected,
            R.drawable.video_selected,
    };

    public CameraSwitchView(Context context) {
        super(context);
        init(context);
    }

    public CameraSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /** 初始化 */
    private void init(Context context) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScroller = new Scroller(context);
    }

    /** 测量宽高 */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mTotalWidth = MeasureSpec.getSize(widthMeasureSpec);
        mTotalHeight = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    /** 绘制 */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int layerId = canvas.saveLayer(0, 0, mTotalWidth,
                mTotalHeight, mPaint, Canvas.ALL_SAVE_FLAG);
        canvas.restoreToCount(layerId);

        drawIcons(canvas); /** 画图标 */

        if (!mInitialized) {
            mInitialized = true;
            mRightMoveDistance = -(mTotalWidth / mBlocks * mInterval); /** 动态计算右滑距离 */
            scrollTo(mRightMoveDistance);
        }
    }

    /** 绘制图标 */
    private void drawIcons(Canvas canvas) {
        int nextX;
        Bitmap bitmap;
        Matrix matrix = new Matrix();

        for (int i = mPhotoIconIndex; i <= mVideoIconIndex; i += mInterval) {  // 将屏幕分为10份，取 3 5 来作为显示位置
            // 取得相对应的Bitmap
            if (mSelectedPosition == i) {
                bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                        mSrcSelect[(i- mPhotoIconIndex)/mInterval]), mPicSize, mPicSize, false);
            } else {
                bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),
                        mSrcNormal[(i- mPhotoIconIndex)/mInterval]), mPicSize, mPicSize, false);
            }
            // 取得下一个图标的X轴坐标
            nextX = ((mTotalWidth) / mBlocks * i) - mPicSize / 2;
            matrix.reset();
            matrix.setTranslate(nextX, mNextY);
            canvas.drawBitmap(bitmap, matrix, null);  // 将bitmap绘制到画布上
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventX = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mIsClick = true;
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                return true;

            case MotionEvent.ACTION_MOVE:
                mIsClick = false;
                mScrollOffset = eventX - mDownX;
                smoothSliding(mLastScrollX - mScrollOffset);
                return true;

            case MotionEvent.ACTION_UP:
                if (mIsClick) {
                    handleClickEvent(mDownX, mDownY);
                    return true;
                }
                handleMoveEvent();
                return true;
        }
        return super.onTouchEvent(event);
    }

    /** 处理滑动事件 */
    private void handleMoveEvent() {
        if (mScrollOffset < 0) { // 左滑
            videoSelected(true);
        } else {                 // 右滑
            videoSelected(false);
        }
        this.invalidate();
    }

    /** 处理选中事件 */
    private void videoSelected(boolean flag) {
        mVideoSelected = flag;

        if (mVideoSelected) {
            mSelectedPosition = mVideoIconIndex;
            smoothScrollTo(mLeftMoveDistance, mSlidingDuration); // X = mLeftMoveDistance, 回到初始位置
        } else {
            mSelectedPosition = mPhotoIconIndex;
            smoothScrollTo(mRightMoveDistance, mSlidingDuration); // X = 0, 回到初始偏移位置
        }

        if (null != mOnCheckListener) {
            mOnCheckListener.itemOnCheckListener(mVideoSelected);
        }
    }

    /** 跟手水平滑动 */
    private void smoothSliding(int endX) {
        Log.d(TAG, "smoothSliding endX = "+endX);
        if (endX < mRightMoveDistance-mSlidingOffset) {                   // 增加右滑偏移
            endX = mRightMoveDistance-mSlidingOffset;                     // 避免右滑太过
        }
        if (endX > mLeftMoveDistance+mSlidingOffset) {                    // 增加左滑偏移
            endX = mLeftMoveDistance+mSlidingOffset;                      // 避免左滑太过
        }

        smoothScrollTo(endX, mSlidingDuration);
        this.invalidate();
    }

    /** 滚动到指定位置 */
    private void scrollTo(int destX) {
        scrollTo(destX, 0);
        mLastScrollX = destX;
        this.invalidate();
    }

    /** 平滑滚动到 */
    public void smoothScrollTo(int destX, int duration) {
        mScroller.startScroll(getScrollX(), 0, destX - getScrollX(), 0, duration);
        mLastScrollX = destX;
        this.invalidate();
    }

    /** 处理滑动逻辑 */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    /** 处理点击事件 */
    private void handleClickEvent(int downX, int downY) {
        int nextCenterX;
        boolean isClick;

        for (int index = mPhotoIconIndex; index <= mVideoIconIndex +mInterval; index += mInterval) {
            nextCenterX = (mTotalWidth) / mBlocks * index;
            nextCenterX = nextCenterX - mPicSize / 2;

            Rect rect = new Rect(nextCenterX - mPicSize, mNextCenterY - mPicSize,
                    nextCenterX + mPicSize, mNextCenterY + mPicSize);
            isClick = rect.contains(downX, downY);

            if (isClick) {
                handleClick(index);
                this.invalidate();
                break;
            }
        }
    }

    /** 处理点击 */
    private void handleClick(int index) {
        if (mOnCheckListener == null) throw new RuntimeException();

        if (!mVideoSelected) {
            if (index==mVideoIconIndex) {
                photoClick();
            } else if (index==mVideoIconIndex+mInterval) {
                smoothScrollTo(mLeftMoveDistance, mSlidingDuration);
                videoClick();
            }
        } else {
            if (index==mPhotoIconIndex) {
                smoothScrollTo(mRightMoveDistance, mSlidingDuration);
                photoClick();
            } else if (index== mVideoIconIndex) {
                videoClick();
            }
        }
    }

    /** 处理拍照按钮点击 */
    private void photoClick() {
        mSelectedPosition = mPhotoIconIndex;
        mVideoSelected = false;
        mOnCheckListener.itemOnCheckListener(false);
    }

    /** 处理录像按钮点击 */
    private void videoClick() {
        mSelectedPosition = mVideoIconIndex;
        mVideoSelected = true;
        mOnCheckListener.itemOnCheckListener(true);
    }

    /** 点击监听接口 */
    private OnCheckListener mOnCheckListener;

    /** 设置监听接口 */
    public void setOnCheckListener(OnCheckListener onCheckListener) {
        this.mOnCheckListener = onCheckListener;
    }

    /** 监听接口 */
    public interface OnCheckListener {
        void itemOnCheckListener(boolean videoChecked);
    }

}
