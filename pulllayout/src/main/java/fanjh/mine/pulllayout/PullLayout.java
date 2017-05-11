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
import android.widget.Scroller;

import java.util.ArrayList;

/**
 * @author fanjh
 * @date 2017/4/27 9:34
 * @description 可能具有顶部刷新和底部加载功能的布局
 * @note 视图的添加顺序为内容、头部（非必要）、底部（非必要）
 **/
public class PullLayout extends ViewGroup implements NestedScrollingParent,NestedScrollingChild {
    //内容视图
    private View mContentView;
    //顶部刷新的时候会显示的视图
    private View mHeaderView;
    //底部加载的时候会显示的视图
    private View mFooterView;
    //当前是否在触摸状态下
    private boolean isOnTouch;
    private PullLayoutOption mOption;
    //头部视图的高度
    private int mHeaderHeight;
    //底部视图的高度
    private int mFooterHeight;
    //上次的触摸事件坐标
    private Point mLastPoint;
    //当前偏移量
    private int mCurrentOffset;
    //上次的偏移量
    private int mPrevOffset;
    private int mTouchSlop;
    //刷新和加载更多的回调
    private ArrayList<IRefreshListener> mRefreshListeners;
    private ArrayList<ILoadMoreListener> mLoadMoreListeners;
    //当前是否在刷新中
    private boolean isRefreshing;
    //当前是否在加载中
    private boolean isLoading;
    //缓慢滑动工作者
    private ScrollerWorker mScroller;
    //主要用于标记当前事件的意义
    private boolean canUpIntercept;
    private boolean canDownIntercept;
    //一次拦截事件的时候当前是否可以顶部或底部刷新
    private boolean canUp;
    private boolean canDown;
    //当前是否处于嵌套滑动中
    private boolean isNestedScrolling;
    //是否允许嵌套滑动，没有使用isNestedScrolling是因为版本问题
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
        initData();
        TypedArray array = context.obtainStyledAttributes(attrs,R.styleable.PullLayout);
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
        boolean contentFixed = array.getBoolean(R.styleable.PullLayout_contentFixed,false);
        mOption.setContentFixed(contentFixed);
        disabledNestedScrolling = array.getBoolean(R.styleable.PullLayout_disabledNestedScrolling,false);
        array.recycle();
    }

    private void initData() {
        mOption = new PullLayoutOption();
        mLastPoint = new Point();
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mRefreshListeners = new ArrayList<>();
        mLoadMoreListeners = new ArrayList<>();
        mScroller = new ScrollerWorker(getContext());
        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        switch (childCount) {
            case 1://这种时候默认只有一个内容视图
                mContentView = getChildAt(0);
                break;
            case 2://默认优先支持顶部刷新
                mContentView = getChildAt(0);
                mHeaderView = getChildAt(1);
                break;
            case 3:
                mContentView = getChildAt(0);
                mHeaderView = getChildAt(1);
                mFooterView = getChildAt(2);
                break;
            default:
                throw new IllegalArgumentException("必须包括1到3个子视图");
        }
        checkHeaderAndFooterAndAddListener();
    }

    /**
     * 检查头部和底部是否为监听，是的话添加到监听回调列表中
     */
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
        left = (l + getPaddingLeft() + lp.leftMargin);
        if (mOption.isContentFixed()) {
            top = (t + getPaddingTop() + lp.topMargin);
        }else{
            top = (t + getPaddingTop() + lp.topMargin) + mCurrentOffset;
        }
        mContentView.layout(left, top, left + mContentView.getMeasuredWidth(), top + mContentView.getMeasuredHeight());
        if (null != mHeaderView) {
            lp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            left = (l + getPaddingLeft() + lp.leftMargin);
            top = (t + getPaddingTop() + lp.topMargin) - mHeaderHeight + mCurrentOffset;
            mHeaderView.layout(left, top, left + mHeaderView.getMeasuredWidth(), top + mHeaderView.getMeasuredHeight());
        }
        if (null != mFooterView) {
            lp = (MarginLayoutParams) mFooterView.getLayoutParams();
            left = (l + getPaddingLeft() + lp.leftMargin);
            top = (b - getPaddingBottom() + lp.topMargin) + mCurrentOffset;
            mFooterView.layout(left, top, left + mFooterView.getMeasuredWidth(), top + mFooterView.getMeasuredHeight());
        }
    }

    /**
     * 处理LayoutParams支持Margin相关属性
     **/
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
                    canUpIntercept = (deltaY > 0 && canUp);
                    canDownIntercept = (deltaY < 0 && canDown);
                    return canUpIntercept || canDownIntercept;
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
                updatePos((int) (event.getY() - mLastPoint.y));
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isOnTouch = false;
                if (mCurrentOffset > 0) {
                    tryPerformRefresh();
                } else {
                    tryPerformLoading();
                }
                break;
        }
        mLastPoint.set((int) event.getX(), (int) event.getY());
        return true;
    }

    /**
     * 修改偏移量，改变视图位置
     *
     * @param deltaY 当前位置的偏移量
     */
    private void updatePos(int deltaY) {
        if (!hasHeaderOrFooter() || deltaY == 0) {//不需要偏移
            return;
        }
        if (isOnTouch) {
            if (!canUp && (mCurrentOffset + deltaY > 0)) {//此时偏移量不应该>0
                deltaY = (0 - mCurrentOffset);
            } else if (!canDown && (mCurrentOffset + deltaY < 0)) {//此时偏移量不应该<0
                deltaY = (0 - mCurrentOffset);
            }
        }
        mPrevOffset = mCurrentOffset;
        mCurrentOffset += deltaY;
        mCurrentOffset = Math.max(Math.min(mCurrentOffset, mOption.getMaxDownOffset()), mOption.getMaxUpOffset());
        deltaY = mCurrentOffset - mPrevOffset;
        if (deltaY == 0) {//不需要偏移
            return;
        }
        callUIPositionChangedListener(mPrevOffset, mCurrentOffset);
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
     * 是否有头部或者底部视图
     *
     * @return true是
     */
    private boolean hasHeaderOrFooter() {
        return null != mHeaderView || null != mFooterView;
    }


    /**
     * 尝试处理加载更多
     */
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

    /**
     * 尝试处理刷新回调
     */
    private void tryPerformRefresh() {
        if (isOnTouch || isRefreshing || isNestedScrolling) {//触摸中或者刷新中不进行回调
            return;
        }
        if (mCurrentOffset >= mOption.getRefreshOffset()) {
            startRefreshing();
        } else {//没有达到刷新条件，还原状态
            mScroller.trySmoothScrollToOffset(0);
            if(mCurrentOffset > 0) {
                callBeforeRefreshListener();
            }
        }
    }

    /**
     * 处理刷新
     */
    private void startRefreshing() {
        isRefreshing = true;
        callRefreshBeginListener();
        mScroller.trySmoothScrollToOffset(mOption.getRefreshOffset());
    }

    /**
     * 处理加载
     */
    private void startLoading() {
        isLoading = true;
        callLoadMoreBeginListener();
        mScroller.trySmoothScrollToOffset(mOption.getLoadMoreOffset());
    }

    /**
     * 刷新完成
     */
    public void refreshComplete() {
        if (!isRefreshing) {
            return;
        }
        isRefreshing = false;
        mScroller.trySmoothScrollToOffset(0);
        callRefreshCompleteListener();
    }

    /**
     * 加载完成
     */
    public void loadingComplete() {
        if (!isLoading) {
            return;
        }
        isLoading = false;
        mScroller.trySmoothScrollToOffset(0);
        callLoadMoreCompleteListener();
    }

    /**
     * 回调刷新的各种监听
     **/
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

    private void callUIPositionChangedListener(int oldOffset, int newOffset) {
        for (IRefreshListener listener : mRefreshListeners) {
            listener.onUIPositionChanged(oldOffset, newOffset);
        }
        for (ILoadMoreListener loadMoreListener : mLoadMoreListeners) {
            loadMoreListener.onUIPositionChanged(oldOffset, newOffset);
        }
    }

    /**
     * 回调加载的监听
     */
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
    /** end **/

    /**
     * 添加和移除监听
     **/
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
     * 配置相关
     **/
    public void setOnCheckHandler(PullLayoutOption.OnCheckHandler handler) {
        mOption.setOnCheckHandler(handler);
    }

    /**
     * 在滑动过程中内容视图是否跟着移动
     * @param contentFixed true不移动，默认false移动
     */
    public void setContentFixed(boolean contentFixed){
        mOption.setContentFixed(contentFixed);
    }

    /**
     * 必须在外面设置触发加载更多的偏移量以及底部上拉的最大偏移量
     * @param loadMoreOffset 触发加载更多的偏移量>0
     * @param maxUpOffset 底部上拉的最大偏移量>0
     */
    public void setLoadMoreOffset(int loadMoreOffset,int maxUpOffset) {
        if(loadMoreOffset > maxUpOffset){
            throw new IllegalArgumentException("触发加载更多偏移量不能大于底部上拉的最大偏移量！");
        }
        mOption.setLoadMoreOffset(loadMoreOffset).setMaxUpOffset(maxUpOffset);
    }

    /**
     * 必须在外面设置触发刷新的偏移量以及顶部下拉的最大偏移量
     * @param refreshOffset 触发刷新的偏移量>0
     * @param maxDownOffset 顶部下拉的最大偏移量>0
     */
    public void setRefreshOffset(int refreshOffset,int maxDownOffset) {
        if(refreshOffset > maxDownOffset){
            throw new IllegalArgumentException("触发刷新的偏移量不能大于顶部下拉的最大偏移量！");
        }
        mOption.setRefreshOffset(refreshOffset).setMaxDownOffset(maxDownOffset);
    }

    /**
     * 获取头部视图的高度，在设置有头部视图并且测量完成后才有值
     */
    public int getHeaderHeight() {
        return mHeaderHeight;
    }

    /**
     * 获取底部视图的高度，在设置有地不是图并且测量完成后才有值
     */
    public int getFooterHeight() {
        return mFooterHeight;
    }

    /** end **/

    /**
     * 处理自动刷新
     */
    public void autoRefresh() {
        boolean hasView = (mHeaderView != null && isEnabled());
        boolean isWorking = (isRefreshing || isLoading || mScroller.isRunning);
        boolean isTouch = (isOnTouch || isNestedScrolling);
        if (!hasView || isWorking || isTouch) {
            return;
        }
        mScroller.mSmoothScrollTime = ScrollerWorker.AUTO_REFRESH_SMOOTH_TIME;
        startRefreshing();
        mScroller.mSmoothScrollTime = ScrollerWorker.DEFAULT_SMOOTH_TIME;
    }

    /**
     * 处理自动加载
     */
    public void autoLoading() {
        boolean hasView = (mFooterView != null && isEnabled());
        boolean isWorking = (isLoading || isRefreshing || mScroller.isRunning);
        boolean isTouch = (isOnTouch || isNestedScrolling);
        if (!hasView || isWorking || isTouch) {
            return;
        }
        mScroller.mSmoothScrollTime = ScrollerWorker.AUTO_REFRESH_SMOOTH_TIME;
        startLoading();
        mScroller.mSmoothScrollTime = ScrollerWorker.DEFAULT_SMOOTH_TIME;
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        //只接收竖直方向上面的嵌套滑动
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
            } else {//没有达到刷新条件，还原状态
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
            updatePos(deltaY);
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
                updatePos(-dyUnconsumed);
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
     * 处理SmoothScroll
     */
    private class ScrollerWorker implements Runnable {
        public static final int DEFAULT_SMOOTH_TIME = 400;//ms
        public static final int AUTO_REFRESH_SMOOTH_TIME = 200;//ms,自动刷新和自动加载时布局弹出时间
        private int mSmoothScrollTime;
        private int mLastY;//上次的Y坐标偏移量
        private Scroller mScroller;//间隔计算执行者
        private Context mContext;//上下文
        private boolean isRunning;//当前是否运行中

        public ScrollerWorker(Context mContext) {
            this.mContext = mContext;
            mScroller = new Scroller(mContext);
            mSmoothScrollTime = DEFAULT_SMOOTH_TIME;
        }

        public void setSmoothScrollTime(int mSmoothScrollTime) {
            this.mSmoothScrollTime = mSmoothScrollTime;
        }

        @Override
        public void run() {
            boolean isFinished = (!mScroller.computeScrollOffset() || mScroller.isFinished());
            if (isFinished) {
                end();
            } else {
                int y = mScroller.getCurrY();
                int deltaY = (y - mLastY);
                boolean isDown = ((mPrevOffset == mOption.getRefreshOffset()) && deltaY > 0);
                boolean isUp = ((mPrevOffset == mOption.getLoadMoreOffset()) && deltaY < 0);
                if (isDown || isUp) {//不需要进行多余的滑动
                    end();
                    return;
                }
                updatePos(deltaY);
                mLastY = y;
                post(this);
            }
        }

        /**
         * 尝试缓慢滑动到指定偏移量
         *
         * @param targetOffset 需要滑动到的偏移量
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
         * 结束Scroller
         */
        private void endScroller() {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            mScroller.abortAnimation();
        }

        /**
         * 停止并且还原滑动工作
         */
        public void end() {
            removeCallbacks(this);
            endScroller();
            isRunning = false;
            mLastY = 0;
        }

    }

}
