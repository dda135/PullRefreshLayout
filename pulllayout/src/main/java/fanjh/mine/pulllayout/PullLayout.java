package fanjh.mine.pulllayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import java.util.ArrayList;

/**
 * @author fanjh
 * @date 2017/4/27 9:34
 * @description you can use custom condition to refresh/load and add listener to handle some state
 * @note child[0]：content(must)
 *       child[1]：header(non-essential)
 *       child[2]：footer(non-essential)
 **/
public class PullLayout extends ViewGroup implements NestedScrollingParent,NestedScrollingChild {
    //content
    private View mContentView;
    //show this view when refreshing
    private View mHeaderView;
    //show this view when loading
    private View mFooterView;
    //in touch event mode
    private boolean isOnTouch;
    //params option
    private PullLayoutOption mOption;
    //headerView's height
    private int mHeaderHeight;
    //footerView's height
    private int mFooterHeight;
    //last MotionEvent's coordinate
    private Point mLastPoint;
    //layout current scroll offset
    private int mCurrentOffset;
    //last scroll offset
    private int mPrevOffset;
    private int mTouchSlop;
    //refresh/load state change listener
    private ArrayList<IRefreshListener> mRefreshListeners;
    private ArrayList<ILoadMoreListener> mLoadMoreListeners;
    //is refreshing
    private boolean isRefreshing;
    //is loading
    private boolean isLoading;
    //use to smooth scroll
    private ScrollerWorker mScroller;
    //sign can UP/DOWN scroll
    private boolean canUp;
    private boolean canDown;
    //is in NestedScrolling state
    private boolean isNestedScrolling;
    //can use NestedScrolling
    private boolean disabledNestedScrolling;

    private NestedScrollingParentHelper mParentHelper;
    private NestedScrollingChildHelper mChildHelper;

    public PullLayout(Context context) {
        this(context, null);
    }

    public PullLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        setAlwaysDrawnWithCacheEnabled(false);
        initData();
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PullLayout);
        int refreshOffset = array.getDimensionPixelOffset(R.styleable.PullLayout_refreshOffset,0);
        if(0 != refreshOffset){
            mOption.setRefreshOffset(refreshOffset);
        }
        int loadMoreOffset = array.getDimensionPixelOffset(R.styleable.PullLayout_loadMoreOffset,0);
        if(0 != loadMoreOffset){
            mOption.setLoadMoreOffset(loadMoreOffset);
        }
        int maxUpOffset = array.getDimensionPixelOffset(R.styleable.PullLayout_maxUpOffset,0);
        if(0 != maxUpOffset){
            mOption.setMaxUpOffset(maxUpOffset);
        }
        int maxDownOffset = array.getDimensionPixelOffset(R.styleable.PullLayout_maxDownOffset,0);
        if(0 != maxDownOffset){
            mOption.setMaxDownOffset(maxDownOffset);
        }
        float movieRatio = array.getFloat(R.styleable.PullLayout_movieRatio,1);
        mOption.setMoveRatio(movieRatio);
        boolean contentFixed = array.getBoolean(R.styleable.PullLayout_contentFixed,false);
        mOption.setContentFixed(contentFixed);
        disabledNestedScrolling = array.getBoolean(R.styleable.PullLayout_disabledNestedScrolling,false);
        int refreshCompleteDelayedTime = array.getInt(R.styleable.PullLayout_refreshCompleteDelayedTime,0);
        mOption.setRefreshCompleteDelayed(refreshCompleteDelayedTime);
        array.recycle();
    }

    private void initData() {
        if(null == mOption) {
            mOption = new PullLayoutOption();
        }
        mLastPoint = new Point();
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mRefreshListeners = new ArrayList<>();
        mLoadMoreListeners = new ArrayList<>();
        mScroller = new ScrollerWorker(getContext(),mOption.getSmoothScroller());
        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);
    }

    public void setOption(PullLayoutOption mOption) {
        this.mOption = mOption;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        //you should care child view position
        switch (childCount) {
            case 1:
                mContentView = getChildAt(0);
                break;
            case 2:
                mContentView = getChildAt(0);
                mHeaderView = getChildAt(1);
                break;
            case 3:
                mContentView = getChildAt(0);
                mHeaderView = getChildAt(1);
                mFooterView = getChildAt(2);
                break;
            default:
                throw new IllegalArgumentException("must cover 1-3 child view");
        }
        checkHeaderAndFooterAndAddListener();
    }

    private void checkHeaderAndFooterAndAddListener() {
        if (mHeaderView instanceof IRefreshListener) {
            mRefreshListeners.add((IRefreshListener) mHeaderView);
        }
        if (mFooterView instanceof ILoadMoreListener) {
            mLoadMoreListeners.add((ILoadMoreListener) mFooterView);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildWithMargins(mContentView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        MarginLayoutParams lp = null;
        if (null != mHeaderView) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            mHeaderHeight = mHeaderView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        }
        if (null != mFooterView) {
            measureChildWithMargins(mFooterView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            mFooterHeight = mFooterView.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left, top;
        MarginLayoutParams lp;
        lp = (MarginLayoutParams) mContentView.getLayoutParams();
        left = (getPaddingLeft() + lp.leftMargin);
        if (mOption.isContentFixed()) {
            top = (getPaddingTop() + lp.topMargin);
        }else{
            top = (getPaddingTop() + lp.topMargin) + mCurrentOffset;
        }
        mContentView.layout(left, top, left + mContentView.getMeasuredWidth(), top + mContentView.getMeasuredHeight());
        if (null != mHeaderView) {
            lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            left = (getPaddingLeft() + lp.leftMargin);
            top = (getPaddingTop() + lp.topMargin) - mHeaderHeight + mCurrentOffset;
            mHeaderView.layout(left, top, left + mHeaderView.getMeasuredWidth(), top + mHeaderView.getMeasuredHeight());
        }
        if (null != mFooterView) {
            lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            left = (getPaddingLeft() + lp.leftMargin);
            top = (b - t - getPaddingBottom() + lp.topMargin) + mCurrentOffset;
            mFooterView.layout(left, top, left + mFooterView.getMeasuredWidth(), top + mFooterView.getMeasuredHeight());
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null && p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @SuppressWarnings({"unused"})
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    /**
     * LayoutParams end
     **/

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || !hasHeaderOrFooter() || isRefreshing || isLoading || isNestedScrolling) {
            return false;
        }
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX();
                int y = (int) event.getY();
                int deltaY = (y - mLastPoint.y);
                int dy = Math.abs(deltaY);
                int dx = Math.abs(x - mLastPoint.x);
                Log.d(getClass().getSimpleName(), "dx-->" + dx + "--dy-->" + dy + "--touchSlop-->" + mTouchSlop);
                if (dy > mTouchSlop && dy >= dx) {
                    canUp = mOption.canUpToDown();
                    canDown = mOption.canDownToUp();
                    Log.d(getClass().getSimpleName(), "canUp-->" + canUp + "--canDown-->" + canDown + "--deltaY-->" + deltaY);
                    return (deltaY > 0 && canUp) || (deltaY < 0 && canDown);
                }
                return false;
        }
        mLastPoint.set((int) event.getX(), (int) event.getY());
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !hasHeaderOrFooter() || isRefreshing || isLoading || isNestedScrolling) {
            return false;
        }
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_MOVE:
                isOnTouch = true;
                updatePos((int) (mOption.getMoveRatio() * (event.getY() - mLastPoint.y)));
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isOnTouch = false;
                if (mCurrentOffset > 0) {
                    tryPerformRefresh();
                } else if(mCurrentOffset < 0){
                    tryPerformLoading();
                }
                break;
        }
        mLastPoint.set((int) event.getX(), (int) event.getY());
        return true;
    }

    /**
     * try scroll content/header/footer
     *
     * @param deltaY scroll deltaY
     */
    private void updatePos(int deltaY) {
        if (!hasHeaderOrFooter() || deltaY == 0) {
            return;
        }
        if (isOnTouch) {
            if (!canUp && (mCurrentOffset + deltaY > 0)) {
                deltaY = (0 - mCurrentOffset);
            } else if (!canDown && (mCurrentOffset + deltaY < 0)) {
                deltaY = (0 - mCurrentOffset);
            }
        }
        mPrevOffset = mCurrentOffset;
        mCurrentOffset += deltaY;
        mCurrentOffset = Math.max(Math.min(mCurrentOffset, mOption.getMaxDownOffset()), mOption.getMaxUpOffset());
        deltaY = mCurrentOffset - mPrevOffset;
        if (deltaY == 0) {
            return;
        }
        callUIPositionChangedListener(mPrevOffset, mCurrentOffset);
        if (mCurrentOffset >= mOption.getRefreshOffset()) {
            callCanRefreshListener();
        } else if (mCurrentOffset <= mOption.getLoadMoreOffset()) {
            callCanLoadMoreListener();
        }
        if (!mOption.isContentFixed()) {
            mContentView.offsetTopAndBottom(deltaY);
        }
        if (null != mHeaderView) {
            mHeaderView.offsetTopAndBottom(deltaY);
        }
        if (null != mFooterView) {
            mFooterView.offsetTopAndBottom(deltaY);
        }
        invalidate();
    }

    /**
     * has Header or Footer
     *
     * @return true has
     */
    private boolean hasHeaderOrFooter() {
        return null != mHeaderView || null != mFooterView;
    }


    private void tryPerformLoading() {
        if (isOnTouch || isLoading || isNestedScrolling) {
            return;
        }
        if (mCurrentOffset <= mOption.getLoadMoreOffset()) {
            startLoading();
        } else {
            mScroller.trySmoothScrollToOffset(0);
            if(mCurrentOffset < 0) {
                callBeforeLoadMoreListener();
            }
        }
    }

    private void tryPerformRefresh() {
        if (isOnTouch || isRefreshing || isNestedScrolling) {
            return;
        }
        if (mCurrentOffset >= mOption.getRefreshOffset()) {
            startRefreshing();
        } else {
            mScroller.trySmoothScrollToOffset(0);
            if(mCurrentOffset > 0) {
                callBeforeRefreshListener();
            }
        }
    }

    private void startRefreshing() {
        isRefreshing = true;
        callRefreshBeginListener();
        mScroller.trySmoothScrollToOffset(mOption.getRefreshOffset());
    }

    private void startLoading() {
        isLoading = true;
        callLoadMoreBeginListener();
        mScroller.trySmoothScrollToOffset(mOption.getLoadMoreOffset());
    }

    /**
     * when refresh completed,you should call this method to notify PullLayout
     */
    public void refreshComplete() {
        if (!isRefreshing) {
            return;
        }
        callRefreshCompleteListener();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if(null != getContext()) {
                    isRefreshing = false;
                    mScroller.trySmoothScrollToOffset(0);
                }
            }
        },mOption.getRefreshCompleteDelayed());
    }

    /**
     * when loading completed,you should call this method to notify PullLayout
     */
    public void loadingComplete() {
        if (!isLoading) {
            return;
        }
        callLoadMoreCompleteListener();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if(null != getContext()) {
                    isLoading = false;
                    mScroller.trySmoothScrollToOffset(0);
                }
            }
        },mOption.getLoadCompleteDelayed());
    }

    private void callBeforeRefreshListener(){
        for (IRefreshListener listener : mRefreshListeners) {
            listener.onBeforeRefresh();
        }
    }
    private void callRefreshBeginListener() {
        for (IRefreshListener listener : mRefreshListeners) {
            listener.onRefreshBegin();
        }
    }

    private void callRefreshCompleteListener() {
        for (IRefreshListener listener : mRefreshListeners) {
            listener.onRefreshComplete();
        }
    }

    private void callCanRefreshListener() {
        for (IRefreshListener listener : mRefreshListeners) {
            listener.onCanRefresh();
        }
    }

    private void callUIPositionChangedListener(int oldOffset, int newOffset) {
        for (IRefreshListener listener : mRefreshListeners) {
            listener.onUIPositionChanged(oldOffset, newOffset, mOption.getRefreshOffset());
        }
        for (ILoadMoreListener loadMoreListener : mLoadMoreListeners) {
            loadMoreListener.onUIPositionChanged(oldOffset, newOffset, mOption.getLoadMoreOffset());
        }
    }

    private  void callBeforeLoadMoreListener(){
        for (ILoadMoreListener listener : mLoadMoreListeners) {
            listener.onBeforeLoad();
        }
    }
    private void callLoadMoreBeginListener() {
        for (ILoadMoreListener listener : mLoadMoreListeners) {
            listener.onLoadMoreBegin();
        }
    }

    private void callLoadMoreCompleteListener() {
        for (ILoadMoreListener listener : mLoadMoreListeners) {
            listener.onLoadMoreComplete();
        }
    }

    private void callCanLoadMoreListener() {
        for (ILoadMoreListener listener : mLoadMoreListeners) {
            listener.onCanLoadMore();
        }
    }

    /** end **/

    /** use some methods to add/remove listener **/

    public void addRefreshListener(IRefreshListener listener) {
        mRefreshListeners.add(listener);
    }

    public void removeRefreshListener(IRefreshListener listener) {
        mRefreshListeners.remove(listener);
    }

    public void addLoadMoreListener(ILoadMoreListener listener) {
        mLoadMoreListeners.add(listener);
    }

    public void removeLoadMoreListener(ILoadMoreListener listener) {
        mLoadMoreListeners.remove(listener);
    }
    /** end **/

    /**
     * use this method to add condition
     * check how can refresh/load
     **/
    public void setOnCheckHandler(PullLayoutOption.OnCheckHandler handler) {
        mOption.setOnCheckHandler(handler);
    }

    /**
     * get header's height
     * @note you should use this method when completed measure
     */
    public int getHeaderHeight() {
        return mHeaderHeight;
    }

    /**
     * get footer's height
     * @note you should use this method when completed measure
     */
    public int getFooterHeight() {
        return mFooterHeight;
    }

    /** end **/

    /**
     * use to auto refresh
     * @note should use this method when completed measure and layout,for example,
     * use getViewTreeObserver().addonPreDrawListener(...)
     */
    public void autoRefresh() {
        boolean hasView = (mHeaderView != null && isEnabled());
        boolean isWorking = (isRefreshing || isLoading || mScroller.isRunning);
        boolean isTouch = (isOnTouch || isNestedScrolling);
        if (!hasView || isWorking || isTouch) {
            return;
        }
        mScroller.mSmoothScrollTime = mOption.getAutoRefreshPopTime();
        startRefreshing();
        mScroller.mSmoothScrollTime = mOption.getKickBackTime();
    }

    /**
     * use to auto load
     * @note should use this method when completed measure and layout,for example,
     * use getViewTreeObserver().addonPreDrawListener(...)
     */
    public void autoLoading() {
        boolean hasView = (mFooterView != null && isEnabled());
        boolean isWorking = (isLoading || isRefreshing || mScroller.isRunning);
        boolean isTouch = (isOnTouch || isNestedScrolling);
        if (!hasView || isWorking || isTouch) {
            return;
        }
        mScroller.mSmoothScrollTime = mOption.getAutoRefreshPopTime();
        startLoading();
        mScroller.mSmoothScrollTime = mOption.getKickBackTime();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        //only handle Vertical scroll
        boolean isVerticalScroll = (nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL);
        boolean canTouchMove = isEnabled() && hasHeaderOrFooter();
        return !disabledNestedScrolling && isVerticalScroll && canTouchMove;
    }

    @Override
    public void onStopNestedScroll(View child) {
        if(disabledNestedScrolling){
            return;
        }
        mParentHelper.onStopNestedScroll(child);
        if (isNestedScrolling) {
            isNestedScrolling = false;
            isOnTouch = false;
            if (mCurrentOffset >= mOption.getRefreshOffset()) {
                startRefreshing();
            } else if(mCurrentOffset <= mOption.getLoadMoreOffset()){
                startLoading();
            } else {//Not up to refresh condition
                mScroller.trySmoothScrollToOffset(0);
                if(mCurrentOffset < 0){
                    callBeforeLoadMoreListener();
                }else if(mCurrentOffset > 0){
                    callBeforeRefreshListener();
                }
            }
        }
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if(disabledNestedScrolling){
            return;
        }
        if (isNestedScrolling) {
            canUp = mOption.canUpToDown();
            canDown = mOption.canDownToUp();
            int minOffset = canDown?mOption.getMaxUpOffset():0;
            int maxOffset = canUp?mOption.getMaxDownOffset():0;
            int nextOffset = (mCurrentOffset - dy);
            int sureOffset = Math.min(Math.max(minOffset,nextOffset),maxOffset);
            int deltaY = sureOffset - mCurrentOffset;
            consumed[1] = (-deltaY);
            updatePos((int) (mOption.getMoveRatio() * deltaY));
        }
        dispatchNestedPreScroll(dx, dy, consumed, null);
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if(disabledNestedScrolling){
            return;
        }
        boolean canTouch = !isLoading && !isRefreshing && !isOnTouch;
        if (dyUnconsumed != 0 && canTouch) {
            canUp = mOption.canUpToDown();
            canDown = mOption.canDownToUp();
            boolean canUpToDown = (canUp && dyUnconsumed < 0);
            boolean canDownToUp = (canDown && dyUnconsumed > 0);
            if(canUpToDown || canDownToUp){
                isOnTouch = true;
                isNestedScrolling = true;
                updatePos((int) (mOption.getMoveRatio() * -dyUnconsumed));
                dyConsumed = dyUnconsumed;
                dyUnconsumed = 0;
            }
        }
        dispatchNestedScroll(dxConsumed,dxUnconsumed,dyConsumed,dyUnconsumed,null);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }

    // NestedScrollingChild

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    /**
     * handle smooth scroll
     */
    private class ScrollerWorker implements Runnable {
        private int mSmoothScrollTime;
        private int mLastY;//last calculate pointY，use to calculate deltaY
        private Scroller mScroller;
        private Context mContext;
        private boolean isRunning;//scroller is running？

        public ScrollerWorker(Context mContext,Scroller scroller) {
            this.mContext = mContext;
            mScroller = null != scroller?scroller:new Scroller(mContext,new DecelerateInterpolator());
            mSmoothScrollTime = mOption.getKickBackTime();
        }

        @Override
        public void run() {
            boolean isFinished = (!mScroller.computeScrollOffset() || mScroller.isFinished());
            if (isFinished) {
                //some case,Scroller will end unexpected
                //should check it
                if(mScroller.getCurrY() != mLastY){
                    checkScrollerAndRun();
                }
                end();
            } else {
                checkScrollerAndRun();
            }
        }

        /**
         * check scroller state
         * It will continue scrolling when scroller's state is not end
         */
        private void checkScrollerAndRun(){
            int y = mScroller.getCurrY();
            int deltaY = (y - mLastY);
            boolean isDown = ((mPrevOffset == mOption.getRefreshOffset()) && deltaY > 0);
            boolean isUp = ((mPrevOffset == mOption.getLoadMoreOffset()) && deltaY < 0);
            if (isDown || isUp) {//don't should scroll
                end();
                return;
            }
            updatePos(deltaY);
            mLastY = y;
            post(this);
        }

        /**
         * try smooth scroll to target offset
         *
         * @param targetOffset target offset
         */
        public void trySmoothScrollToOffset(int targetOffset) {
            if (!hasHeaderOrFooter()) {
                return;
            }
            endScroller();
            removeCallbacks(this);
            mLastY = 0;
            int deltaY = (targetOffset - mCurrentOffset);
            mScroller.startScroll(0, 0, 0, deltaY, mSmoothScrollTime);
            isRunning = true;
            post(this);
        }

        /**
         * only end scroller
         */
        private void endScroller() {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            mScroller.abortAnimation();
        }

        /**
         * end Scroller and reset params
         */
        public void end() {
            removeCallbacks(this);
            endScroller();
            isRunning = false;
            mLastY = 0;
        }

    }

}
