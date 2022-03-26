package com.dhg.packkit.widget.wheel;//package com.cylan.widget.wheel;
//
//import android.content.Context;
//import android.content.res.Resources;
//import android.content.res.TypedArray;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.os.Handler;
//import android.os.HandlerThread;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.util.SparseArray;
//import android.view.GestureDetector;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewConfiguration;
//import android.widget.EdgeEffect;
//import android.widget.OverScroller;
//
//import androidx.annotation.IntDef;
//import androidx.annotation.Nullable;
//import androidx.core.view.GestureDetectorCompat;
//
//import com.cylan.jiafeigou.BuildConfig;
//import com.cylan.jiafeigou.R;
//import com.cylan.smartcall.oss.CloudVideo;
//
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Collection;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//import java.util.NavigableSet;
//import java.util.TimeZone;
//import java.util.TreeSet;
//import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by yanzhendong on 2017/12/22.
// */
//
//public class HistoryWheelView extends View implements GestureDetector.OnGestureListener {
//    private static final String TAG = HistoryWheelView.class.getSimpleName();
//    private static final boolean DEBUG = BuildConfig.DEBUG;
//    private static final int DRAW_TYPE_BACKGROUND = 0;
//    private static final int DRAW_TYPE_BLOCK = 1;
//    private static final int DRAW_TYPE_TIME_TEXT = 2;
//    private static final int DRAW_TYPE_DIVIDER = 3;
//
//    private OverScroller mScroller;
//    private GestureDetectorCompat mDetector;
//
//    private Paint markerPaint = new Paint();
//    private Paint naturalDateLinePaint = new Paint();
//    private Paint naturalDateTextPaint = new Paint();
//    private Paint dataMaskPaint = new Paint();
//
//    private int mTouchSlop;
//    private volatile boolean mLocked = false;
//    private volatile boolean mHasPendingUpdateAction = false;
//    private volatile boolean mHasPendingSnapAction = false;
//    private volatile boolean mHasPendingChangePrecisionAction = false;
//    private int markerColor;
//    private int maskColor;
//    private int lineColor;
//    private int textColor;
//    private int lineInterval;//小刻度的距离
//    private int lineWidth;
//    private int textSize;
//    private int shortLineHeight;
//    private int longLineHeight;
//    /**
//     * 在内部更新或外部带锁定操作更新后,锁定历史时间轴一段时间,在这段时间里
//     * 外部更新将会被忽略,内部更新任何时候是不受影响的
//     */
//    private int scrollerLockTime;
//    /***
//     *滑动停止后延迟一定的时间再通知更新,这样做的原因是用户可能在连续滑动
//     *如果滑动一停止立即通知更新,可能不是所希望的结果,延迟一段时间以确保
//     *用户没有在继续操作了
//     */
//    private int updateDelay;
//    /***
//     *标定刻度是否居于屏幕正中间,如果为 true 标定刻度将居于屏幕正中,否则居于控件正中
//     */
//    private boolean markerCenterInScreen;
//    private float mCenterPositionPercent;
//    private int textTopMargin;
//    private int textBottomMargin;
//    private double fontHeight;
//    private EdgeEffect mEdgeEffectLeft;
//    private EdgeEffect mEdgeEffectRight;
//    private boolean mOverScrollerMode = true;
//    private boolean mIsTouchEventFinished = true;
//    private int mDrawDirection;
//    private int mDrawLineStartY;
//    private int mDrawLineShortEndY;
//    private int mDrawLineLongEndY;
//    private int mDrawTextStartY;
//    private HistoryDataAdapter mDataAdapter;
//    private long mDisplayTime;
//    private long mTargetTime;
//    private SelectionHelper mSelectionHelper;
//
//
//    @IntDef({SnapDirection.MOVE_DIRECTION, SnapDirection.LEFT, SnapDirection.RIGHT, SnapDirection.AUTO})
//    @Retention(RetentionPolicy.SOURCE)
//    public @interface SnapDirection {
//        int MOVE_DIRECTION = -1;
//        int LEFT = 0;
//        int RIGHT = 1;
//        int AUTO = 2;
//    }
//
//    private int mSnapDirection = SnapDirection.RIGHT;
//    /**
//     * 用来标记控件内部是否可以动态改变 mSnapDirection,在 onScroll 或者 onFling 方法里会
//     * 读取这个字段,如果SnapDirection 没有被锁住,onScroll 或者 onFling 会根据手指滑动的
//     * 方向动态的改变 SnapDirection,否则将按照外部设定的 SnapDirection 进行吸附
//     */
//    private boolean mSnapDirectionLocked = false;
//    private Calendar mCalendar = Calendar.getInstance();
//    private long mZeroTime;
//    private static Comparator<CloudVideo> mHistoryComparator = new Comparator<CloudVideo>() {
//        @Override
//        public int compare(CloudVideo o1, CloudVideo o2) {
//            return (int) (o1.begin - o2.begin);
//        }
//    };
//
//    private Runnable mUnlockRunnable = new Runnable() {
//        @Override
//        public void run() {
//            mLocked = false;
//        }
//    };
//
//    private Runnable mNotifyRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (mDataAdapter == null || mDataAdapter.sortedSet.size() == 0) {
//                return;
//            }
//            if (DEBUG) {
//                Log.d(TAG, "notify time is" + new Date(mDisplayTime).toLocaleString());
//            }
//            if (mHistoryListener != null) {
//                mHistoryListener.onHistoryTimeChanged(mDisplayTime / 1000 * 1000);//转成秒
//            }
//        }
//    };
//
//    private Runnable mMarkerPositionRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (!mScroller.isFinished()) {
//                removeCallbacks(mMarkerPositionRunnable);
//                post(mMarkerPositionRunnable);
//                return;
//            }
//            long currentTime = getCurrentTime();
//            if (markerCenterInScreen) {
//                int[] location = new int[2];
//                getLocationOnScreen(location);
//                mCenterPositionPercent = (getResources().getDisplayMetrics().widthPixels / 2 - location[0]) / (float) getMeasuredWidth();
//            } else {
//                mCenterPositionPercent = 0.5F;
//            }
//            scrollToPositionInternal(currentTime);
//        }
//    };
//
//    private HistoryListener mHistoryListener;
//
//
//    public HistoryWheelView(Context context) {
//        this(context, null);
//    }
//
//    public HistoryWheelView(Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//        init(context, attrs);
//    }
//
//    private void init(Context context, AttributeSet attrs) {
//        mScroller = new OverScroller(getContext());
//        mDetector = new GestureDetectorCompat(getContext(), this);
//        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
//        mCalendar.set(Calendar.MINUTE, 0);
//        mCalendar.set(Calendar.SECOND, 0);
//        mZeroTime = mCalendar.getTimeInMillis();
//
//        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.HistoryWheelView);
//        markerColor = attributes.getColor(R.styleable.HistoryWheelView_hw_marker_color, Color.parseColor("#FF0199FB"));
//        markerCenterInScreen = attributes.getBoolean(R.styleable.HistoryWheelView_hw_marker_on_screen_center, true);
//        maskColor = attributes.getColor(R.styleable.HistoryWheelView_hw_mask_color, Color.parseColor("#302595FD"));
//        textColor = attributes.getColor(R.styleable.HistoryWheelView_hw_text_color, Color.parseColor("#FF888888"));
//        textSize = attributes.getDimensionPixelSize(R.styleable.HistoryWheelView_hw_text_size, dp2px(10));
//        lineColor = attributes.getColor(R.styleable.HistoryWheelView_hw_line_color, Color.parseColor("#FFDEDEDE"));
////        lineInterval = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_line_interval, (int) (0.04 * 10 * 60));
//        lineInterval = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_line_interval, dp2px(8));
//        lineWidth = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_line_width, dp2px(2.5f));
//        shortLineHeight = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_short_line_height, dp2px(10));
//        longLineHeight = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_long_line_height, dp2px(25));
//        scrollerLockTime = attributes.getInteger(R.styleable.HistoryWheelView_hw_scroller_lock_time, 2_100);//锁的时间够久以确保历史录像有足够的时间切换
//        updateDelay = attributes.getInteger(R.styleable.HistoryWheelView_hw_history_update_delay, 700);
//        textTopMargin = attributes.getDimensionPixelOffset(R.styleable.HistoryWheelView_hw_history_text_top_margin, dp2px(5));
//        textBottomMargin = attributes.getDimensionPixelSize(R.styleable.HistoryWheelView_hw_history_text_bottom_margin, dp2px(5));
//        mDrawDirection = attributes.getInt(R.styleable.HistoryWheelView_hw_draw_direction, 1);
//        markerPaint.setAntiAlias(true);
//        markerPaint.setColor(markerColor);
//        markerPaint.setStyle(Paint.Style.STROKE);
//        markerPaint.setStrokeWidth(lineWidth);
//
//        naturalDateLinePaint.setAntiAlias(true);
//        naturalDateLinePaint.setColor(lineColor);
//        naturalDateLinePaint.setStrokeWidth(lineWidth);
//
//        dataMaskPaint.setAntiAlias(true);
//        dataMaskPaint.setColor(maskColor);
//
//        naturalDateTextPaint.setAntiAlias(true);
//        naturalDateTextPaint.setColor(textColor);
//        naturalDateTextPaint.setTextSize(textSize);
//        naturalDateTextPaint.setTextAlign(Paint.Align.CENTER);
//        Paint.FontMetrics fm = naturalDateTextPaint.getFontMetrics();
//        fontHeight = Math.ceil(fm.descent - fm.ascent);
//        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
//        mEdgeEffectLeft = new EdgeEffect(getContext());
//        mEdgeEffectRight = new EdgeEffect(getContext());
//        attributes.recycle();
//
//        mSelectionHelper = new SelectionHelper();
//        mSelectionHelper.listenerList.add(mSelectionChangeListener);
//    }
//
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        /*
//         *之所以用 post 是因为在这里直接获取的话获取不到控件在屏幕上的位置
//         */
//        mDrawLineStartY = mDrawDirection == 1 ? 0 : h;
//        mDrawLineShortEndY = mDrawDirection == 1 ? shortLineHeight : h - shortLineHeight;
//        mDrawLineLongEndY = mDrawDirection == 1 ? longLineHeight : h - longLineHeight;
//        mDrawTextStartY = (int) (mDrawDirection == 1 ?
//                Math.max(mDrawLineLongEndY + textBottomMargin, h - textBottomMargin - fontHeight) :
//                Math.max(mDrawLineLongEndY - textBottomMargin - fontHeight, textBottomMargin));
//        removeCallbacks(mMarkerPositionRunnable);
//        post(mMarkerPositionRunnable);
//        mSelectionHelper.onSizeChanged(w, h, oldw, oldh);
//    }
//
//    private int dp2px(float dp) {
//        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
//    }
//
//    @Override
//    public boolean onDown(MotionEvent e) {
//        return true;
//    }
//
//    @Override
//    public void onShowPress(MotionEvent e) {
//
//    }
//
//    @Override
//    public boolean onSingleTapUp(MotionEvent e) {
//        return false;
//    }
//
//    private volatile float mDistanceX = 0;
//
//    @Override
//    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        mDistanceX += distanceX;
//        /*
//         *当 distanceX 的距离小于 touchSlop 时,滑动没有效果,所有这里要积攒到最小到 touchSlop 才开始滑动
//         */
//        if (Math.abs(mDistanceX) < mTouchSlop) {
//            return true;
//        }
//
//        disableExternalScrollAction();
//        mHasPendingUpdateAction = true;
//        if (mSnapDirection == SnapDirection.MOVE_DIRECTION || !mSnapDirectionLocked) {
//            mHasPendingSnapAction = true;
//        }
//        distanceX = mDistanceX;
//        if (!mSnapDirectionLocked) {
//            mSnapDirection = distanceX >= 0 ? SnapDirection.RIGHT : SnapDirection.LEFT;
//        }
//        mDistanceX = 0;
//
//        if (mOverScrollerMode) {
//            float exceptX = mScroller.getCurrX() + distanceX;
//            float finalX = distanceX > 0 ? Math.min(getMaxScrollX(), exceptX) : Math.max(getMinScrollX(), exceptX);
//            distanceX = finalX - mScroller.getCurrX();
//        }
//
//        if (distanceX != 0) {
//            mScroller.startScroll(mScroller.getFinalX(), 0, (int) distanceX, 0);
//            calculateTargetTime();
//            invalidate();
//        }
//        return true;
//    }
//
//    private void calculateTargetTime() {
//        int finalX = mScroller.getFinalX();
//        int currX = mScroller.getCurrX();
//        long currentTime = getCurrentTime();
//        mTargetTime = currentTime + (finalX - currX) * getPixelTime();
//    }
//
//    @Override
//    public void onLongPress(MotionEvent e) {
//
//    }
//
//    @Override
//    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//        disableExternalScrollAction();
//        mHasPendingUpdateAction = true;
//        if (mSnapDirection == SnapDirection.MOVE_DIRECTION || !mSnapDirectionLocked) {
//            mHasPendingSnapAction = true;
//        }
//        if (!mSnapDirectionLocked) {
//            mSnapDirection = velocityX <= 0 ? SnapDirection.RIGHT : SnapDirection.LEFT;
//        }
//        int minX = Integer.MIN_VALUE;
//        int maxX = Integer.MAX_VALUE;
//        if (mOverScrollerMode) {
//            minX = getMinScrollX();
//            maxX = getMaxScrollX();
//        }
//        mScroller.fling(mScroller.getCurrX(), 0, (int) -velocityX, 0, minX, maxX, 0, 0, 100, 0);
//        calculateTargetTime();
//        invalidate();
//        return true;
//    }
//
//    private int getMinScrollX() {
//        long minTime = mZeroTime;
//        if (mDataAdapter != null && mDataAdapter.sortedSet.size() > 0) {
//            CloudVideo first = mDataAdapter.sortedSet.first();
//            minTime = first.begin;
//        }
//        return (int) (getDistanceByTime(minTime, mZeroTime) - mCenterPositionPercent * getMeasuredWidth());
//    }
//
//    private int getMaxScrollX() {
//        long emptyMaxTime = mZeroTime + getPixelTime() * getMeasuredWidth();
//        long maxTime = emptyMaxTime;
//        if (mDataAdapter != null && mDataAdapter.sortedSet.size() > 0) {
//            CloudVideo last = mDataAdapter.sortedSet.last();
//            maxTime = (long) (last.begin + last.duration * 1000L);
//        }
//        return (int) (getDistanceByTime(maxTime, emptyMaxTime) + (getMeasuredWidth() - mCenterPositionPercent * getMeasuredWidth()));
//    }
//
//    @Override
//    public void computeScroll() {
//
//        if (mHasPendingChangePrecisionAction) {
//            mHasPendingChangePrecisionAction = false;
//            mScroller.abortAnimation();
//            setScrollX(mScroller.getFinalX());
//            notifyScrollCompleted();
//        } else if (mScroller.computeScrollOffset()) {
//            mDisplayTime = getCurrentTime();
//            if (mHistoryListener != null && mHasPendingUpdateAction && !mScroller.isOverScrolled()) {
//                //非常没必要,但测试说要加
//                mHistoryListener.onScrolling(mDisplayTime);
//            }
//            scrollTo(mScroller.getCurrX(), 0);
//            invalidate();
//        } else if (mIsTouchEventFinished) {
//            notifyScrollCompleted();
//        }
//    }
//
//    private void disableExternalScrollAction() {
//        this.mLocked = true;
//        removeCallbacks(mUnlockRunnable);
//    }
//
//    private void enableExternalScrollAction() {
//        removeCallbacks(mUnlockRunnable);
//        postDelayed(mUnlockRunnable, scrollerLockTime);
//    }
//
//    public long getCurrentTime() {
//        return getSelectTimeByPercent(mCenterPositionPercent);
//    }
//
//
//    public long setRelativeTime(long relativeTime) {
//        if (!mIsTouchEventFinished) {
//            return getCurrentTime();
//        }
//        double reallyTime = 0;
//        double duration = 0;
//        if (mDataAdapter != null && mDataAdapter.sortedSet.size() > 0) {
//            for (CloudVideo cloudVideo : mDataAdapter.sortedSet) {
//                duration += cloudVideo.duration * 1000;
//                if (duration > (double) relativeTime || ((int) duration == (int) relativeTime)) {
//                    reallyTime = cloudVideo.begin + relativeTime - (duration - cloudVideo.duration * 1000);
//                    scrollToPosition((long) reallyTime, true);
//                    break;
//                }
//            }
//        }
//        return reallyTime == 0 ? getCurrentTime() : (long) reallyTime;
//    }
//
//
//    public long getRelativeTime() {
//        long relativeTime = 0;
//        if (mDataAdapter != null && mDataAdapter.sortedSet.size() > 0) {
//            long currentTime = getCurrentTime();
//            for (CloudVideo cloudVideo : mDataAdapter.sortedSet) {
//                if (currentTime <= (cloudVideo.begin + cloudVideo.duration * 1000)) {
//                    //找到了
//                    if (currentTime >= cloudVideo.begin) {
//                        relativeTime += currentTime - cloudVideo.begin;
//                    } else {
//                        relativeTime += 1000;
//                    }
//                    break;
//                } else {
//                    relativeTime += (cloudVideo.duration * 1000);
//                }
//            }
//        }
//        return relativeTime;
//    }
//
//    public long getRelativeTime(long currentTime) {
//        long relativeTime = 0;
//        if (mDataAdapter != null && mDataAdapter.sortedSet.size() > 0) {
//            for (CloudVideo cloudVideo : mDataAdapter.sortedSet) {
////                Log.i(TAG,"cloudVideo time"+cloudVideo.url +"cloudVideo total"+cloudVideo.begin +cloudVideo.duration*1000+"duration:"+cloudVideo.duration);
//                if (currentTime <= (cloudVideo.begin + cloudVideo.duration * 1000)) {
//                    //找到了
//                    if (currentTime >= cloudVideo.begin) {
//                        relativeTime += currentTime - cloudVideo.begin;
////                        Log.i(TAG,"cloudVideo relativeTime is:"+relativeTime);
//                    } else {
//                        relativeTime += 1000;
//                    }
//
//                    break;
//                } else {
//                    relativeTime += (cloudVideo.duration * 1000);
//                }
//            }
//        }
//        return relativeTime;
//    }
//
//    public long getFinalTime() {
//        return getPixelTime() * (mScroller.getFinalX() + (long) (mCenterPositionPercent * getMeasuredWidth())) + mZeroTime;
//    }
//
//    public void scrollToPosition(long time, boolean locked) {
//        scrollToPosition(time, locked, false);
//    }
//
//    public void scrollToPosition(long time, boolean locked, boolean focus) {
//        boolean externalScrollDisabled = mIsTouchEventFinished && (!mLocked || focus);
//        if (externalScrollDisabled) {
//            if (locked) {
//                disableExternalScrollAction();
//            }
//            scrollToPositionInternal(time);
//        }
//    }
//
//    public void snapScrollToPosition(long time, boolean locked) {
//        long currentTime = getCurrentTime();
//        time -= getSnapDistanceX(time, time >= currentTime ? SnapDirection.RIGHT : SnapDirection.LEFT);
//        scrollToPosition(time, locked, false);
//    }
//
//    private void scrollToPositionInternal(long time) {
//        if (time == 0) {
//            time = mZeroTime;
//        }
//        long currentTime = getCurrentTime();
//        mTargetTime = time;
//        long distance = getDistanceByTime(time, currentTime);
//        if (DEBUG) {
//            Log.d(TAG, "scroller time is:" + new Date(time).toLocaleString() +
//                    ",current time is:" + new Date(currentTime).toLocaleString() +
//                    ",scroller distance is:" + distance +
//                    ",currentX:" + mScroller.getCurrX() +
//                    ",target time is:" + new Date(currentTime + distance * getPixelTime()).toLocaleString());
//        }
//
//        if (distance != 0) {
//            removeCallbacks(mNotifyRunnable);
//            mScroller.startScroll(mScroller.getCurrX(), 0, (int) distance, 0);
//        } else {
//            notifyUpdate();
//        }
//        invalidate();
//    }
//
//    private CloudVideo mSnapHistoryBlock = new CloudVideo(0, 0, "");
//
//    private long getSnapDistanceX(long start, int snapDirection) {
//        if (mDataAdapter == null || mDataAdapter.sortedSet.size() == 0) {
//            return 0;
//        }
//        mSnapHistoryBlock.begin = start;
//        CloudVideo floor = mDataAdapter.sortedSet.floor(mSnapHistoryBlock);
//        CloudVideo ceiling = mDataAdapter.sortedSet.ceiling(mSnapHistoryBlock);
//        if (floor == null) {
//            floor = mDataAdapter.sortedSet.first();
//        }
//        if (ceiling == null) {
//            ceiling = mDataAdapter.sortedSet.last();
//        }
//
//        //判断当前是否需要做吸附操作,如果当前在数据区,则不需要做吸附操作了
//        boolean inFloor = mSnapHistoryBlock.begin >= floor.begin && mSnapHistoryBlock.begin <= (floor.begin + floor.duration * 1000L);
//        boolean inCeiling = mSnapHistoryBlock.begin >= ceiling.begin && mSnapHistoryBlock.begin <= (ceiling.begin + ceiling.duration * 1000L);
//        if (inFloor || inCeiling) {
//            return 0;
//        }
//        long distance = start;
//
//        //在边界处是没有吸附规则的自动吸附到第一个或最后一个的开始处
//        if (mSnapHistoryBlock.begin < floor.begin) {//处理左边界的情况
//            distance = (mSnapHistoryBlock.begin - floor.begin);
//        } else if (mSnapHistoryBlock.begin > ceiling.begin + ceiling.duration * 1000L) {//处理右边界的情况
//            distance = (mSnapHistoryBlock.begin - ceiling.begin);
//        } else if (snapDirection == SnapDirection.LEFT) {//处理左吸附的情况
//            distance = (mSnapHistoryBlock.begin - (floor.begin + (long) floor.duration * 1000L));
//        } else if (snapDirection == SnapDirection.RIGHT) {//处理右吸附的情况
//            distance = (mSnapHistoryBlock.begin - ceiling.begin);
//        } else if (snapDirection == SnapDirection.AUTO) {//处理最短吸附的情况
//            long distanceF = mSnapHistoryBlock.begin - (floor.begin + (long) floor.duration * 1000L);
//            long distanceC = mSnapHistoryBlock.begin - ceiling.begin;
//            distance = start - (Math.abs(distanceC) < Math.abs(distanceF) ? distanceC : distanceF);
//        }
//        return distance;
//    }
//
//    private void notifySnapAction() {
//        mHasPendingSnapAction = false;
//        long currentTime = getCurrentTime();
//        long distanceX = getSnapDistanceX(currentTime, mSnapDirection);
//        if (distanceX == 0) {
//            notifyUpdate();
//        } else {
//            scrollToPositionInternal(currentTime - distanceX);
//        }
//    }
//
//    private void notifyUpdate() {
//        enableExternalScrollAction();
//        if (mHasPendingUpdateAction) {
//            mHasPendingUpdateAction = false;
//            removeCallbacks(mNotifyRunnable);
//            postDelayed(mNotifyRunnable, updateDelay);
//        }
//    }
//
//    private void notifyScrollCompleted() {
//        if (mHasPendingSnapAction) {
//            notifySnapAction();
//        } else {
//            notifyUpdate();
//        }
//    }
//
//    private Paint makeDrawPaintByType(int drawType) {
//        Paint drawPaint;
//        switch (drawType) {
//            case DRAW_TYPE_BACKGROUND: {
//                drawPaint = naturalDateLinePaint;
//            }
//            break;
//            case DRAW_TYPE_BLOCK: {
//                drawPaint = dataMaskPaint;
//            }
//            break;
//            case DRAW_TYPE_TIME_TEXT: {
//                drawPaint = naturalDateTextPaint;
//            }
//            break;
//            case DRAW_TYPE_DIVIDER: {
//                drawPaint = markerPaint;
//            }
//            break;
//            default: {
//                drawPaint = naturalDateLinePaint;
//            }
//            break;
//        }
//        return drawPaint;
//    }
//
//    @Override
//    public void draw(Canvas canvas) {
//        super.draw(canvas);
//        drawBackground(canvas);
//        drawHistoryBlock(canvas);
//        drawTimeText(canvas);
//        drawDivider(canvas);
//        if (mOverScrollerMode) {
//            drawEdgeEffect(canvas);
//        }
//        mSelectionHelper.drawSelection(canvas);
//    }
//
//
//    private CloudVideo mStartHistoryBlock = new CloudVideo(0, 0, "");
//    private CloudVideo mStopHistoryBlock = new CloudVideo(0, 0, "");
//
//    /**
//     * 每个像素多少时间
//     *
//     * @return
//     */
//    private long getPixelTime() {
//        if (mDataAdapter != null) {
//            return mDataAdapter.precision * getUnitTime() / lineInterval;
//        }
//        return (long) (10 * 60 * getUnitTime() / lineInterval);
//    }
//
//    /**
//     * 每一个小距离距离代表多少时间
//     *
//     * @return　毫秒
//     */
//    private long getUnitTime2() {
//        if (mDataAdapter != null) {
//            return mDataAdapter.precision * getUnitTime();
//        }
//        return (long) (10 * 60 * getUnitTime());
//    }
//
//
//    private void drawHistoryBlock(Canvas canvas) {
//        if (mDataAdapter == null) {
//            return;
//        }
//        int startX = mScroller.getCurrX() - getMeasuredWidth() / 2;
//        int stopX = startX + getMeasuredWidth() * 2;
//        mStartHistoryBlock.begin = (startX * getPixelTime() + mZeroTime);
//        mStopHistoryBlock.begin = (stopX * getPixelTime() + mZeroTime);
//        CloudVideo start = mDataAdapter.sortedSet.floor(mStartHistoryBlock);
//        if (start == null && mDataAdapter.sortedSet.size() > 0) {
//            start = mDataAdapter.sortedSet.first();
//        }
//        CloudVideo stop = mDataAdapter.sortedSet.ceiling(mStopHistoryBlock);
//        if (stop == null && mDataAdapter.sortedSet.size() > 0) {
//            stop = mDataAdapter.sortedSet.last();
//        }
//        try {
//            if (mDataAdapter.sortedSet.size() > 0) {
//                NavigableSet<CloudVideo> historyFiles = mDataAdapter.sortedSet.subSet(start, true, stop, true);
//                Paint blockPaint = makeDrawPaintByType(DRAW_TYPE_BLOCK);
//                for (CloudVideo file : historyFiles) {
//                    long distanceX1 = getDistanceByTime(file.begin, mZeroTime);
//                    long distanceX2 = getDistanceByTime((long) (file.begin + file.duration * 1000L), mZeroTime);
//                    canvas.drawRect(distanceX1, 0, Math.max(distanceX1 + 1, distanceX2)/*最少要画一个像素,否则还以为没有数据*/, getMeasuredHeight(), blockPaint);
//                }
//            }
//        } catch (Exception e) {
//            if (DEBUG) {
//                Log.d(TAG, "迭代过程中数据发生了变化,需要重新绘制");
//            }
//            postInvalidate();
//        }
//    }
//
//
//    private long getDistanceByTime(long start, long end) {
//        return (start - end) / getPixelTime();
//    }
//
//    private void drawDivider(Canvas canvas) {
//        int divider = (int) (mScroller.getCurrX() + mCenterPositionPercent * getMeasuredWidth());
//        Paint dividerPaint = makeDrawPaintByType(DRAW_TYPE_DIVIDER);
//        canvas.drawLine(divider, 0, divider, getMeasuredHeight(), dividerPaint);
//
//    }
//
//    private void drawBackground(Canvas canvas) {
//        long precisionInterval = mDataAdapter == null ? 6 : mDataAdapter.precisionInterval;
//        long minDisplayTime = mDataAdapter == null ? 0 : mDataAdapter.getMinDisplayTime();
//        long maxDisplayTime = mDataAdapter == null ? Long.MAX_VALUE : mDataAdapter.getMaxDisplayTime();
//        long pixelTime = getPixelTime();
//        long min = (minDisplayTime - mZeroTime) / pixelTime;
//        long max = (maxDisplayTime - mZeroTime) / pixelTime;
//        int startX = (int) ((Math.max(mScroller.getCurrX() - getMeasuredWidth() / 2, min) / lineInterval) * lineInterval);
//        int stopX = (int) Math.min(startX + getMeasuredWidth() * 2, max);
//        Paint backgroundPaint = makeDrawPaintByType(DRAW_TYPE_BACKGROUND);
//        while (startX <= stopX) {
//            canvas.drawLine(startX, mDrawLineStartY, startX, startX % (lineInterval * precisionInterval) == 0 ? mDrawLineLongEndY : mDrawLineShortEndY, backgroundPaint);
//            startX += lineInterval;
//        }
//    }
//
//    private void drawEdgeEffect(Canvas canvas) {
//        EdgeEffect edgeEffect = new EdgeEffect(getContext());
//        edgeEffect.draw(canvas);
//    }
//
//    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
//
//    private long getUnitTime() {
//        long unitTime = 1;
//        if (mDataAdapter != null) {
//            switch (mDataAdapter.timeUnit) {
//                case MILLISECONDS: {
//                    unitTime = 1;
//                }
//                break;
//                case SECONDS: {
//                    unitTime = 1000;
//                }
//                break;
//                case MINUTES: {
//                    unitTime = 1000 * 60;
//                }
//                break;
//                case HOURS: {
//                    unitTime = 1000 * 60 * 60;
//                }
//                break;
//                case DAYS: {
//                    unitTime = 1000 * 60 * 60 * 24;
//                }
//                break;
//            }
//        }
//        return unitTime;
//    }
//
//    private void drawTimeText(Canvas canvas) {
//        long precisionInterval = mDataAdapter == null ? 6 : mDataAdapter.precisionInterval;
//        long minDisplayTime = mDataAdapter == null ? Long.MIN_VALUE : mDataAdapter.getMinDisplayTime();
//        long maxDisplayTime = mDataAdapter == null ? Long.MAX_VALUE : mDataAdapter.getMaxDisplayTime();
//        long pixelTime = getPixelTime();
//        long min = (minDisplayTime - mZeroTime) / pixelTime;
//        long max = (maxDisplayTime - mZeroTime) / pixelTime;
//        int startX = (int) ((Math.max(mScroller.getCurrX() - getMeasuredWidth() / 2, min)) / lineInterval * lineInterval);
//        int stopX = (int) Math.min(startX + getMeasuredWidth() * 2, max);
//        Paint timeTextPaint = makeDrawPaintByType(DRAW_TYPE_TIME_TEXT);
//        while (startX <= stopX) {
//            if (startX % (lineInterval * precisionInterval) == 0) {
////                long distanceTime = startX * getPixelTime();//用除法运算容易产生误差
//                int hours = (int) (startX / (lineInterval * precisionInterval));//多少个小时
//                long l = getUnitTime2() * precisionInterval;//每个大距离的时间
//                long distanceTime = l * hours;
//                canvas.drawText(dateFormat.format(mZeroTime + distanceTime), startX, mDrawTextStartY, timeTextPaint);
//            }
//            startX += lineInterval;
//        }
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
////        Log.e(TAG, "onTouchEvent: " );
//        getParent().requestDisallowInterceptTouchEvent(true);
//        int eventAction = event.getAction();
//        switch (eventAction) {
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL: {
//                mIsTouchEventFinished = true;
//                computeScroll();
//            }
//            break;
//            case MotionEvent.ACTION_DOWN: {
//                mIsTouchEventFinished = false;
//            }
//            break;
//            case MotionEvent.ACTION_MOVE: {
//                mIsTouchEventFinished = false;
//            }
//            break;
//        }
//        return mSelectionHelper.onTouchEvent(event) || mDetector.onTouchEvent(event);
//    }
//
//    public void setTimeZone(TimeZone timeZone) {
//        dateFormat.setTimeZone(timeZone);
//        postInvalidate();
//    }
//
//    public void setSnapDirection(@SnapDirection int snapDirection) {
//        this.mSnapDirection = snapDirection;
//        this.mSnapDirectionLocked = snapDirection != SnapDirection.MOVE_DIRECTION;
//    }
//
//    public long getSelectTimeByPercent(float percent) {
//        return getPixelTime() * (mScroller.getCurrX() + (long) (getMeasuredWidth() * percent)) + mZeroTime;
//    }
//
//    private static final Handler mHandler;
//
//    static {
//        HandlerThread mHandlerThread = new HandlerThread("worker-handler-thread");
//        mHandlerThread.start();
//        mHandler = new Handler(mHandlerThread.getLooper());
//    }
//
//    public boolean isLocked() {
//        return mLocked;
//    }
//
//    public void setHistoryListener(HistoryListener listener) {
//        this.mHistoryListener = listener;
//    }
//
//    private Runnable mChangePrecisionRunnable = new Runnable() {
//        @Override
//        public void run() {
//            Log.d(TAG, "targetTime is:" + mTargetTime);
//            scrollToPositionInternal(mTargetTime);
//        }
//    };
//
//    private DataSetObserver mObserver = new DataSetObserver() {
//        @Override
//        void onDataSetChanged() {
//            mHasPendingChangePrecisionAction = true;
//            removeCallbacks(mChangePrecisionRunnable);
//            postDelayed(mChangePrecisionRunnable, 100);
//        }
//    };
//
//    public void setDataAdapter(HistoryDataAdapter dataAdapter) {
////        if (mDataAdapter != null) {
////            mDataAdapter.setDataSetObserver(null);
////        }
//        this.mDataAdapter = dataAdapter;
//        if (mDataAdapter != null) {
//            this.mDataAdapter.addDataSetObserver(mObserver);
//            this.mDataAdapter.notifyDataSetChanged();
//        }
//    }
//
//    public boolean isEmptySet() {
//        return mDataAdapter == null || mDataAdapter.sortedSet == null || mDataAdapter.sortedSet.size() == 0;
//    }
//
//    public interface HistoryListener {
//        void onHistoryTimeChanged(long time);
//
//        void onScrolling(long time);
//    }
//
//    abstract class DataSetObserver {
//        abstract void onDataSetChanged();
//    }
//
//    public static abstract class HistoryDataAdapter {
//        TimeUnit timeUnit = TimeUnit.SECONDS; //单位
//        long precision = 10 * 60;//小刻度代表多少时间,基于 timeUnit
//        long precisionInterval = 6;//多少小刻度后显示一个大刻度
//        TreeSet<CloudVideo> sortedSet = new TreeSet<>(mHistoryComparator);
//        SparseArray<DataSetObserver> dataSetObservers = new SparseArray<DataSetObserver>();
//
//        public void setTimeUnit(TimeUnit timeUnit) {
//            this.timeUnit = timeUnit;
//        }
//
//        public TimeUnit getTimeUnit() {
//            return timeUnit;
//        }
//
//        public void setPrecision(int precision) {
//            this.precision = precision;
//            if (dataSetObservers.size() != 0) {
//                for (int i = 0; i < dataSetObservers.size(); i++) {
//                    dataSetObservers.get(i).onDataSetChanged();
//                }
//            }
//        }
//
//        public long getPrecision() {
//            return precision;
//        }
//
//        public void setPrecisionInterval(long precisionInterval) {
//            this.precisionInterval = precisionInterval;
//            if (dataSetObservers.size() != 0) {
//                for (int i = 0; i < dataSetObservers.size(); i++) {
//                    dataSetObservers.get(i).onDataSetChanged();
//                }
//            }
//        }
//
//        protected long getMinDisplayTime() {
//            return 0;
//        }
//
//        protected long getMaxDisplayTime() {
//            return Long.MAX_VALUE;
//        }
//
//
//        public abstract Collection<CloudVideo> getDataSet();
//
//        public synchronized void notifyDataSetChanged() {
//            sortedSet.clear();
//            Collection<CloudVideo> dataSet = getDataSet();
//            if (dataSet != null) {
//                sortedSet.addAll(dataSet);
//            }
//            if (dataSetObservers.size() != 0) {
//                for (int i = 0; i < dataSetObservers.size(); i++) {
//                    dataSetObservers.get(i).onDataSetChanged();
//                }
//            }
//        }
//
//        void addDataSetObserver(DataSetObserver dataSetObserver) {
//            dataSetObservers.append(dataSetObservers.size(), dataSetObserver);
//        }
//    }
//
//    public void enableSelection(boolean enable) {
//        mSelectionHelper.setSelectionModeEnable(enable);
//    }
//
//    public SelectionTime getSelectionTime() {
//        return mSelectionHelper.makeSelectionTime();
//    }
//
//    public static final int MAX_SELECTION_UN_LIMIT = -1;
//    public static final int MAX_SELECTION_10_MINUTE_LIMIT = -2;
//    public static final int MIN_SELECTION_DEFAULT = -3;
//
//    public void setSelectionParameter(long maxSelectionTime, long minSelectionTime) {
//        mSelectionHelper.setSelectionParameter(maxSelectionTime, minSelectionTime);
//    }
//
//    private SelectionChangeListener mSelectionChangeListener = new SelectionChangeListener() {
//        @Override
//        public void onSelectionChanged(SelectionTime selectionTime) {
//        }
//    };
//
//    public void addSelectionChangedListener(SelectionChangeListener listener) {
//        if (!mSelectionHelper.listenerList.contains(listener)) {
//            mSelectionHelper.listenerList.add(listener);
//        }
//    }
//
//    public static class SelectionTime {
//        public long startTime;
//        public long endTime;
//        public int rectLeft;
//        public int rectRight;
//    }
//
//    public interface SelectionChangeListener {
//        void onSelectionChanged(SelectionTime selectionTime);
//    }
//
//    private class SelectionHelper {
//        private Paint mSelectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        private boolean mIsSelectionMode = false;
//        private float mLastX = -1;
//        private final int SLIDE_MISS = 0;
//        private final int SLIDE_LEFT = 1;
//        private final int SLIDE_RIGHT = 2;
//        private final int SLIDE_INSIDE = 3;
//        private float[] mDrawLines = new float[24];
//        private int mCornerRadius = 30;
//        private int mBorderWidth = dp2px(20);
//        private int mCoverRectColor = Color.parseColor("#40FA0A0A");
//        private int mCornerColor = Color.parseColor("#FFFA0A0A");
//        private int mCornerStrokeWidth = 10;
//        private int mMaxSelectionInPixel = 200;
//        private int mMinSelectionInPixel = dp2px(14);
//        private Rect mSelectionRect = new Rect(0, 0, mMaxSelectionInPixel, 0);
//        private Rect mDrawRect = new Rect();
//        private int mViewWidth, mViewHeight;
//        private int mDragSlide = SLIDE_MISS;
//        private int mDefaultSelectionTime = 10 * 60 * 1000;
//        private long mMaxSelectionTime = mDefaultSelectionTime, mMinSelectionTime = 0;
//        private SelectionTime mSelectionTime = new SelectionTime();
//        private List<SelectionChangeListener> listenerList = new CopyOnWriteArrayList<>();
//
//        boolean onTouchEvent(MotionEvent event) {
//            if (!mIsSelectionMode) {
//                return false;
//            }
//            boolean isTouchEventConsumed = false;
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN: {
//                    float eventX = event.getX();
//                    float eventY = event.getY();
//                    boolean inSlide = Math.min(Math.abs(eventX - mSelectionRect.left), Math.abs(eventX - mSelectionRect.right)) < mCornerRadius;
//                    if (inSlide || mSelectionRect.contains((int) eventX, (int) eventY)) {
//                        mLastX = eventX;
//                        isTouchEventConsumed = true;
//                    }
//                }
//                break;
//                case MotionEvent.ACTION_MOVE: {
//                    isTouchEventConsumed = handleMoveEvent(event);
//                }
//                break;
//                case MotionEvent.ACTION_CANCEL:
//                case MotionEvent.ACTION_UP: {
//                    if (mLastX != -1) {
//                        isTouchEventConsumed = true;
//                    }
//                    mLastX = -1;
//                    mDragSlide = SLIDE_MISS;
//                }
//                break;
//            }
//            if (isTouchEventConsumed) {
//                invalidate();
//            }
//            return isTouchEventConsumed;
//        }
//
//        void setSelectionModeEnable(boolean enable) {
//            this.mIsSelectionMode = enable;
//            updateSelection();
//        }
//
//        void drawSelection(Canvas canvas) {
//            if (!mIsSelectionMode) {
//                return;
//            }
//            mSelectionPaint.setStyle(Paint.Style.FILL);
//            mSelectionPaint.setColor(mCoverRectColor);
//            canvas.drawRect(makeDrawRect(), mSelectionPaint);
//
//            mSelectionPaint.setColor(mCornerColor);
//            mSelectionPaint.setStrokeWidth(mCornerStrokeWidth);
//            mSelectionPaint.setStyle(Paint.Style.STROKE);
//            canvas.drawLines(makeDrawLines(), mSelectionPaint);
//            for (SelectionChangeListener listener : listenerList) {
//                listener.onSelectionChanged(makeSelectionTime());
//            }
//        }
//
//        int decideWhichSlide(MotionEvent event, int deltaX) {
//            int whichSlide = SLIDE_INSIDE;
//            float eventX = event.getX();
//            float distanceLeft = Math.abs(eventX - mSelectionRect.left);
//            float distanceRight = Math.abs(eventX - mSelectionRect.right);
//            if (distanceLeft < mBorderWidth || distanceRight < mBorderWidth) {
//                whichSlide = distanceLeft - distanceRight > 0 ? SLIDE_RIGHT : SLIDE_LEFT;
//            }
//            if (mDragSlide != SLIDE_MISS && mDragSlide != SLIDE_INSIDE) {
//                whichSlide = mDragSlide;
//            }
//            if (whichSlide == SLIDE_INSIDE) {
//                return SLIDE_INSIDE;
//            }
//
//            boolean isIncreaseWidth = (whichSlide == SLIDE_LEFT && deltaX < 0) || (whichSlide == SLIDE_RIGHT && deltaX > 0);
//            if (mSelectionRect.width() >= mMaxSelectionInPixel && isIncreaseWidth) {
//                return SLIDE_INSIDE;
//            }
//
//            boolean isDecreaseWidth = (whichSlide == SLIDE_LEFT && deltaX > 0) || (whichSlide == SLIDE_RIGHT && deltaX < 0);
//            if (mSelectionRect.width() <= mMinSelectionInPixel && isDecreaseWidth) {
//                whichSlide = whichSlide == SLIDE_LEFT ? SLIDE_RIGHT : SLIDE_LEFT;
//            }
//            return whichSlide;
//        }
//
//        boolean handleMoveEvent(MotionEvent event) {
//            if (mLastX == -1) {
//                return false;
//            }
//            float eventX = event.getX();
//            int deltaX = (int) (eventX - mLastX);
//            switch (mDragSlide = decideWhichSlide(event, deltaX)) {
//                case SLIDE_INSIDE: {
//                    mSelectionRect.offset(getSelectionMoveOffset(SLIDE_INSIDE, deltaX), 0);
//                }
//                break;
//                case SLIDE_LEFT: {
//                    mSelectionRect.set(mSelectionRect.left + getSelectionMoveOffset(SLIDE_LEFT, deltaX),
//                            mSelectionRect.top,
//                            mSelectionRect.right,
//                            mSelectionRect.bottom);
//                }
//                break;
//                case SLIDE_RIGHT: {
//                    mSelectionRect.set(mSelectionRect.left,
//                            mSelectionRect.top,
//                            mSelectionRect.right + getSelectionMoveOffset(SLIDE_RIGHT, deltaX),
//                            mSelectionRect.bottom);
//                }
//                break;
//            }
//            mLastX = eventX;
//            return true;
//        }
//
//        private Rect makeDrawRect() {
//            mDrawRect.set(mSelectionRect);
//            mDrawRect.offset(mScroller.getCurrX(), 0);
//            return mDrawRect;
//        }
//
//        float[] makeDrawLines() {
//            int halfCornerStokeWidth = mCornerStrokeWidth / 2;
//            int scrollerCurrX = mScroller.getCurrX();
//            mDrawLines[0] = mSelectionRect.left + halfCornerStokeWidth + scrollerCurrX;
//            mDrawLines[1] = mSelectionRect.bottom;
//            mDrawLines[2] = mSelectionRect.left + halfCornerStokeWidth + scrollerCurrX;
//            mDrawLines[3] = mSelectionRect.top;
//
//            mDrawLines[4] = mSelectionRect.left + scrollerCurrX;
//            mDrawLines[5] = mSelectionRect.top + halfCornerStokeWidth;
//            mDrawLines[6] = mSelectionRect.left + mCornerRadius + scrollerCurrX;
//            mDrawLines[7] = mSelectionRect.top + halfCornerStokeWidth;
//
//            mDrawLines[8] = mSelectionRect.right - mCornerRadius + scrollerCurrX;
//            mDrawLines[9] = mSelectionRect.top + halfCornerStokeWidth;
//            mDrawLines[10] = mSelectionRect.right + scrollerCurrX;
//            mDrawLines[11] = mSelectionRect.top + halfCornerStokeWidth;
//
//            mDrawLines[12] = mSelectionRect.right - halfCornerStokeWidth + scrollerCurrX;
//            mDrawLines[13] = mSelectionRect.top;
//            mDrawLines[14] = mSelectionRect.right - halfCornerStokeWidth + scrollerCurrX;
//            mDrawLines[15] = mSelectionRect.bottom;
//
//            mDrawLines[16] = mSelectionRect.right + scrollerCurrX;
//            mDrawLines[17] = mSelectionRect.bottom - mCornerStrokeWidth / 2;
//            mDrawLines[18] = mSelectionRect.right - mCornerRadius + scrollerCurrX;
//            mDrawLines[19] = mSelectionRect.bottom - mCornerStrokeWidth / 2;
//
//            mDrawLines[20] = mSelectionRect.left + mCornerRadius + scrollerCurrX;
//            mDrawLines[21] = mSelectionRect.bottom - mCornerStrokeWidth / 2;
//            mDrawLines[22] = mSelectionRect.left + scrollerCurrX;
//            mDrawLines[23] = mSelectionRect.bottom - mCornerStrokeWidth / 2;
//
//            return mDrawLines;
//        }
//
//        void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
//            mViewWidth = width;
//            mViewHeight = height;
//            mSelectionRect.bottom = mViewHeight;
//            invalidate();
//        }
//
//        SelectionTime makeSelectionTime() {
//            mSelectionTime.startTime = getSelectTimeByPercent((float) mSelectionRect.left / Math.max(1, mViewWidth));
//            mSelectionTime.endTime = Math.min(mSelectionTime.startTime + mMaxSelectionTime,
//                    getSelectTimeByPercent((float) mSelectionRect.right / Math.max(1, mViewWidth)));
//            mSelectionTime.rectLeft = mSelectionRect.left;
//            mSelectionTime.rectRight = mSelectionRect.right;
//            return mSelectionTime;
//        }
//
//        void setSelectionParameter(long maxSelectionTime, long minSelectionTime) {
//            this.mMaxSelectionTime = maxSelectionTime;
//            this.mMinSelectionTime = minSelectionTime;
//            long pixelTime = getPixelTime();
//            if (maxSelectionTime == MAX_SELECTION_UN_LIMIT) {
//                this.mMaxSelectionInPixel = mViewWidth;
//                this.mMaxSelectionTime = pixelTime * mMaxSelectionInPixel;
//            }
//            if (maxSelectionTime == MAX_SELECTION_10_MINUTE_LIMIT) {
//                this.mMaxSelectionTime = 10 * 60 * 1000;
//                this.mMaxSelectionInPixel = (int) (this.mMaxSelectionTime / pixelTime);
//            }
//            if (minSelectionTime == MIN_SELECTION_DEFAULT) {
//                this.mMinSelectionTime = 0;
//                this.mMinSelectionInPixel = dp2px(14);
//            }
//            updateSelection();
//        }
//
//        void updateSelection() {
//            long pixelTime = getPixelTime();
//            if (this.mMaxSelectionTime > 0) {
//                this.mMaxSelectionInPixel = (int) (this.mMaxSelectionTime / pixelTime);
//            }
//            if (this.mMinSelectionTime > 0) {
//                this.mMinSelectionInPixel = (int) (this.mMinSelectionTime / pixelTime);
//            }
//            int defaultSelectionInPixel = (int) (this.mDefaultSelectionTime / pixelTime);
//            int selectionX = Math.abs((mViewWidth - defaultSelectionInPixel) / 2);
//            mSelectionRect.set(selectionX, 0, selectionX + defaultSelectionInPixel, mViewHeight);
//            invalidate();
//        }
//
//        int getSelectionMoveOffset(int slide, int deltaX) {
//            int selectionMoveOffset;
//            int leftX = mSelectionRect.left + deltaX;
//            int rightX = mSelectionRect.right + deltaX;
//            int leftLimit = leftX < 0 ? deltaX - leftX : deltaX;
//            int rightLimit = rightX > mViewWidth ? deltaX - (rightX - mViewWidth) : deltaX;
//            switch (slide) {
//                case SLIDE_LEFT: {
//                    selectionMoveOffset = leftLimit;
//                }
//                break;
//                case SLIDE_RIGHT: {
//                    selectionMoveOffset = rightLimit;
//                }
//                break;
//                case SLIDE_INSIDE: {
//                    selectionMoveOffset = deltaX > 0 ? rightLimit : leftLimit;
//                }
//                break;
//                default: {
//                    selectionMoveOffset = deltaX;
//                }
//            }
//            return selectionMoveOffset;
//        }
//    }
//}
