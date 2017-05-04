package fanjh.mine.pulllayout;

/**
* @author fanjh
* @date 2017/4/27 9:28
* @description 监听回调
* @note 可以通过实现该回调，处理各种状态
**/
public interface ILoadMoreListener {
    void onUIPositionChanged(int oldOffset, int newOffset);
    void onLoadMoreBegin();
    void onLoadMoreComplete();
}
