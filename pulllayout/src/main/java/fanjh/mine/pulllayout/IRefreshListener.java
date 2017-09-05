package fanjh.mine.pulllayout;

/**
* @author fanjh
* @date 2017/4/27 9:28
* @description 监听回调
* @note 可以通过实现该回调，处理各种状态
**/
public interface IRefreshListener {
    void onBeforeRefresh();//当前偏移量没有达到刷新的标准时松手，然后头部开始回弹的回调
    void onRefreshBegin();//开始刷新的回调
    void onUIPositionChanged(int oldOffset, int newOffset, int refreshOffset);//视图滑动过程中的回调
    void onRefreshComplete();//刷新完成的回调
    void onCanRefresh();//当前偏移量已经超过刷新的标准的时候，还在滑动的话会触发的回调
}
