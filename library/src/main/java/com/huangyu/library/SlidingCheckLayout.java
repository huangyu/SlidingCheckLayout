package com.huangyu.library;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.OverScroller;

/**
 * 滑动选择布局，用于嵌套RecyclerView实现滑动选择功能
 * Created by huangyu on 2018/8/21.
 */
public class SlidingCheckLayout extends FrameLayout {

    // 用于计算TouchSlop，即识别为滑动，每个item宽度 / TOUCH_SLOP_RATE
    private static final float TOUCH_SLOP_RATE = 0.20f;
    // 最大滚动距离常量
    private static final int MAX_SCROLL_DISTANCE = 24;
    // 滚动因子
    private static final int SCROLL_FACTOR = 3;
    // 起始pos和终点pos
    private int startPos, endPos;
    // 上一个起始pos和终点pos
    private int lastStartPos, lastEndPos;

    // 是否正在滑动
    private boolean isSliding;
    // 是否处于顶部，即自动滚动状态
    private boolean inTopSpot;
    // 是否处于底部，即自动滚动状态
    private boolean inBottomSpot;

    // 横轴滑动阈值，超过阈值表示触发横轴滑动
    private float xTouchSlop;
    // 纵轴滑动阈值，超过阈值表示触发纵轴滑动
    private float yTouchSlop;
    // 初始值x
    private float startX;
    // 初始值y
    private float startY;
    // 上次点击的x坐标
    private float lastX;
    // 上次点击的y坐标
    private float lastY;
    // 滚动距离
    private float scrollDistance;

    // Scroller
    private OverScroller scroller;
    // 内部的rv
    private RecyclerView targetRlv;
    // 兼容滑动监听
    private RecyclerView.OnScrollListener innerOnScrollListener;
    // 滑动选中监听
    private OnSlidingCheckListener onSlidingCheckListener;
    /**
     * 计算滚动线程
     */
    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (scroller != null && scroller.computeScrollOffset()) {
                startScrollBy((int) scrollDistance);
                ViewCompat.postOnAnimation(targetRlv, scrollRunnable);
            }
        }
    };

    public SlidingCheckLayout(Context context) {
        this(context, null);
    }

    public SlidingCheckLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addOnScrollListener(RecyclerView.OnScrollListener innerOnScrollListener) {
        this.innerOnScrollListener = innerOnScrollListener;
    }

    public void setOnSlidingCheckListener(OnSlidingCheckListener onSlidingCheckListener) {
        this.onSlidingCheckListener = onSlidingCheckListener;
    }

    /**
     * 为RecyclerView设置监听事件
     */
    private void initRecyclerView() {
        targetRlv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (innerOnScrollListener != null) {
                    innerOnScrollListener.onScrolled(recyclerView, dx, dy);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (innerOnScrollListener != null) {
                    innerOnScrollListener.onScrollStateChanged(recyclerView, newState);
                }
            }
        });
    }

    /**
     * 获取RecyclerView
     */
    private void ensureTargetRlv() {
        if (targetRlv != null) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof RecyclerView) {
                targetRlv = (RecyclerView) childAt;
                initRecyclerView();
                return;
            }
        }
    }

    /**
     * 换LayoutManager需要调用
     * 获取itemCount，初始化item的高度和宽度
     */
    private void ensureLayoutManager() {
        if (targetRlv == null) {
            return;
        }
        RecyclerView.LayoutManager lm = targetRlv.getLayoutManager();
        if (lm == null) {
            return;
        }
        if (lm instanceof GridLayoutManager) {
            GridLayoutManager glm = (GridLayoutManager) lm;
            int itemSpanCount = glm.getSpanCount();
            int size = getResources().getDisplayMetrics().widthPixels / itemSpanCount;
            xTouchSlop = yTouchSlop = size * TOUCH_SLOP_RATE;
        } else {
            throw new IllegalStateException("只支持GridLayoutManager布局！");
        }
    }

    /**
     * 是否可以开始拦截处理事件，当recyclerView数据完全ok之后开始
     *
     * @return 是否可以开始拦截处理事件
     */
    private boolean isReadyToIntercept() {
        return targetRlv != null
                && targetRlv.getAdapter() != null;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return super.onInterceptTouchEvent(ev);
        }
        ensureTargetRlv();
        ensureLayoutManager();
        if (!isReadyToIntercept()) {
            return super.onInterceptTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                reset();

                // 计算起始点坐标
                View child = targetRlv.findChildViewUnder(ev.getX(), ev.getY());
                if (child != null) {
                    int position = targetRlv.getChildAdapterPosition(child);
                    if (position != RecyclerView.NO_POSITION && startPos != position) {
                        startPos = position;
                    }
                }
                startX = ev.getX();
                startY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float xDiff = Math.abs(ev.getX() - startX);
                float yDiff = Math.abs(ev.getY() - startY);
                if (yDiff < yTouchSlop && xDiff > xTouchSlop) {
                    isSliding = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return isSliding;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (!inTopSpot && !inBottomSpot) {
                    // 否则处理滑动事件
                    updateSelectedRange(targetRlv, ev);
                }
                // 在顶部或者底部触发自动滑动
                processAutoScroll(ev);
                break;
            case MotionEvent.ACTION_UP:
                resetParams();
                stopAutoScroll();
                performClick();
                return false;

        }
        return isSliding;
    }

    /**
     * 用于避免onTouchEvent warning
     */
    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * 更新滑动选择区域
     *
     * @param rv RecyclerView
     * @param e  MotionEvent
     */
    private void updateSelectedRange(RecyclerView rv, MotionEvent e) {
        updateSelectedRange(rv, e.getX(), e.getY());
    }

    /**
     * 更新滑动选择区域
     *
     * @param rv RecyclerView
     * @param x  x坐标
     * @param y  y坐标
     */
    private void updateSelectedRange(RecyclerView rv, float x, float y) {
        View child = rv.findChildViewUnder(x, y);
        if (child != null) {
            int position = rv.getChildAdapterPosition(child);
            if (position != RecyclerView.NO_POSITION && endPos != position) {
                endPos = position;
                notifySelectRangeChange();
            }
        }
    }

    /**
     * 核心处理逻辑，计算当前滑动的起始点和终点
     * 外部使用回调对起始点至终点部分进行选择处理
     */
    private void notifySelectRangeChange() {
        if (onSlidingCheckListener == null) {
            return;
        }

        if (startPos == RecyclerView.NO_POSITION || endPos == RecyclerView.NO_POSITION) {
            return;
        }

        int newStart, newEnd;
        newStart = Math.min(startPos, endPos);
        newEnd = Math.max(startPos, endPos);

        if (lastStartPos == RecyclerView.NO_POSITION || lastEndPos == RecyclerView.NO_POSITION) {
            onSlidingCheckListener.onSlidingCheckPos(newStart, newEnd);
        } else {
            if (newStart > lastStartPos) {
                onSlidingCheckListener.onSlidingCheckPos(lastStartPos, newStart - 1);
            } else if (newStart < lastStartPos) {
                onSlidingCheckListener.onSlidingCheckPos(newStart, lastStartPos - 1);
            }
            if (newEnd > lastEndPos) {
                onSlidingCheckListener.onSlidingCheckPos(lastEndPos + 1, newEnd);
            } else if (newEnd < lastEndPos) {
                onSlidingCheckListener.onSlidingCheckPos(newEnd + 1, lastEndPos);
            }
        }

        lastStartPos = newStart;
        lastEndPos = newEnd;
    }

    /**
     * 重置参数
     */
    private void resetParams() {
        lastX = Float.MIN_VALUE;
        lastY = Float.MIN_VALUE;
        isSliding = false;
        inTopSpot = false;
        inBottomSpot = false;
    }

    /**
     * 重置为初始状态
     */
    private void reset() {
        startPos = RecyclerView.NO_POSITION;
        endPos = RecyclerView.NO_POSITION;
        lastStartPos = RecyclerView.NO_POSITION;
        lastEndPos = RecyclerView.NO_POSITION;
        inTopSpot = false;
        inBottomSpot = false;
        lastX = Float.MIN_VALUE;
        lastY = Float.MIN_VALUE;
        stopAutoScroll();
    }

    /**
     * 处理自动滚动
     *
     * @param event 触摸事件
     */
    private void processAutoScroll(MotionEvent event) {
        int height = targetRlv.getHeight();
        float mTopBound = yTouchSlop * 2;
        float mBottomBound = height - yTouchSlop * 2;
        int y = (int) event.getY();
        if (y < mTopBound) {
            lastX = event.getX();
            lastY = event.getY();
            scrollDistance = -(mTopBound - y) / SCROLL_FACTOR;
            if (!inTopSpot) {
                inTopSpot = true;
                startAutoScroll();
            }
        } else if (y > mBottomBound) {
            lastX = event.getX();
            lastY = event.getY();
            scrollDistance = (y - mBottomBound) / SCROLL_FACTOR;
            if (!inBottomSpot) {
                inBottomSpot = true;
                startAutoScroll();
            }
        } else {
            inBottomSpot = false;
            inTopSpot = false;
            lastX = Float.MIN_VALUE;
            lastY = Float.MIN_VALUE;
            stopAutoScroll();
        }
    }

    /**
     * 开始自动滚动
     */
    private void startAutoScroll() {
        if (targetRlv == null) {
            return;
        }
        if (scroller == null) {
            scroller = new OverScroller(targetRlv.getContext(), new LinearInterpolator());
        }
        if (scroller.isFinished()) {
            targetRlv.removeCallbacks(scrollRunnable);
            scroller.startScroll(0, scroller.getCurrY(), 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            ViewCompat.postOnAnimation(targetRlv, scrollRunnable);
        }
    }

    /**
     * 停止自动滚动
     */
    private void stopAutoScroll() {
        if (scroller != null && !scroller.isFinished()) {
            targetRlv.removeCallbacks(scrollRunnable);
            scroller.abortAnimation();
        }
    }

    /**
     * 滚动
     *
     * @param scrollDistance 滚动距离
     */
    private void startScrollBy(int scrollDistance) {
        int targetDistance;
        if (scrollDistance > 0) {
            targetDistance = Math.min(scrollDistance, MAX_SCROLL_DISTANCE);
        } else {
            targetDistance = Math.max(scrollDistance, -MAX_SCROLL_DISTANCE);
        }
        targetRlv.scrollBy(0, targetDistance);
        if (lastX != Float.MIN_VALUE && lastY != Float.MIN_VALUE) {
            updateSelectedRange(targetRlv, lastX, lastY);
        }
    }

    /**
     * 回调接口
     */
    public interface OnSlidingCheckListener {
        /**
         * 回调选择事件
         *
         * @param startPos 第一个item
         * @param endPos   最后一个item
         */
        void onSlidingCheckPos(int startPos, int endPos);
    }

}
