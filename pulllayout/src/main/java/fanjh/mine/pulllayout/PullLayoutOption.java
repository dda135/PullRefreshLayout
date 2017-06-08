package fanjh.mine.pulllayout;

import android.util.Log;

/**
* @author fanjh
* @date 2017/4/27 9:44
* @description 具有刷新和加载功能的布局的一些基础配置
* @note
 * 1.需要在代码中设置
 * 2.整体的偏移量以初始位置为准，向下为真，向上为负
**/
public class PullLayoutOption {
    //触发顶部刷新的偏移量
    private int mRefreshOffset;
    //布局向下滑动的最大偏移量，要大于等于mRefreshOffset才能使刷新有效
    private int mMaxDownOffset;
    //滑动的系数，主要是在手指滑动的距离的基础上乘以系数，从而产生阻尼或放大的感觉
    private float mMoveRatio;
    //触发底部加载更多的偏移量，这个是负数
    private int mLoadMoreOffset;
    //布局向上滑动的最大偏移量，这个是负数，要小于等于mLoadMoreOffset才能使加载有效
    private int mMaxUpOffset;
    //内容视图位置是否固定，即不会随着手指滑动而移动位置，类似于SwipeRefreshLayout
    private boolean isContentFixed;
    //校验监听
    private OnCheckHandler mOnCheckHandler;
    //调用刷新完成之后实际开始操作的延时
    private int mRefreshCompleteDelayed;
    //调用加载完成之后实际开始操作的延时
    private int mLoadCompleteDelayed;

    public interface OnCheckHandler{
        boolean canUpTpDown();
        boolean canDownToUp();
    }

    public PullLayoutOption() {
    }

    public void setOnCheckHandler(OnCheckHandler mOnCheckHandler) {
        this.mOnCheckHandler = mOnCheckHandler;
    }

    public PullLayoutOption setRefreshOffset(int mRefreshOffset) {
        this.mRefreshOffset = mRefreshOffset;
        return this;
    }

    public PullLayoutOption setMaxDownOffset(int mMaxDownOffset) {
        this.mMaxDownOffset = mMaxDownOffset;
        return this;
    }

    public PullLayoutOption setMoveRatio(float mMoveRatio) {
        this.mMoveRatio = mMoveRatio;
        return this;
    }

    public PullLayoutOption setLoadMoreOffset(int mLoadMoreOffset) {
        this.mLoadMoreOffset = (-mLoadMoreOffset);//要转为负数
        return this;
    }

    public PullLayoutOption setMaxUpOffset(int mMaxUpOffset) {
        this.mMaxUpOffset = (-mMaxUpOffset);//要转为负数
        return this;
    }

    public PullLayoutOption setContentFixed(boolean contentFixed) {
        isContentFixed = contentFixed;
        return this;
    }

    public int getRefreshOffset() {
        return mRefreshOffset;
    }

    public int getMaxDownOffset() {
        return mMaxDownOffset;
    }

    public float getMoveRatio() {
        return mMoveRatio;
    }

    public int getLoadMoreOffset() {
        return mLoadMoreOffset;
    }

    public int getMaxUpOffset() {
        return mMaxUpOffset;
    }

    public boolean isContentFixed() {
        return isContentFixed;
    }

    /**
     * 是否可以因为顶部刷新从而使得手指可以从上到下滑动
     * @return true可以，否则不行
     */
    public boolean canUpToDown(){
        //没有手动设置监听
        return null == mOnCheckHandler || mOnCheckHandler.canUpTpDown();
    }

    /**
     * 是否可以因为底部加载从而使得手指可以从下到上滑动
     * @return true可以，否则不行
     */
    public boolean canDownToUp(){
        //没有手动设置监听
        return null == mOnCheckHandler || mOnCheckHandler.canDownToUp();
    }

    public void setRefreshCompleteDelayed(int mRefreshCompleteDelayed) {
        this.mRefreshCompleteDelayed = mRefreshCompleteDelayed;
    }

    public int getRefreshCompleteDelayed() {
        return mRefreshCompleteDelayed;
    }

    public int getLoadCompleteDelayed() {
        return mLoadCompleteDelayed;
    }

    public void setLoadCompleteDelayed(int mLoadCompleteDelayed) {
        this.mLoadCompleteDelayed = mLoadCompleteDelayed;
    }
}
