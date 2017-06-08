package fanjh.mine.pulllayout;

/**
* @author fanjh
* @date 2017/4/27 9:28
* @description 监听回调
* @note 可以通过实现该回调，处理各种状态
**/
public interface ILoadMoreListener {
    void onBeforeLoad();//没有达到加载条件底部开始回弹的回调
    void onUIPositionChanged(int oldOffset, int newOffset, int loadMoreOffset);
    void onLoadMoreBegin();
    void onLoadMoreComplete();
    void onCanLoadMore();
}
