package fanjh.mine.pullrefreshlayout.headerview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import fanjh.mine.pulllayout.ILoadMoreListener;

/**
* @author fanjh
* @date 2017/5/4 11:10
* @description 底部视图
**/
public class HeaderProgressView extends ProgressBar implements ILoadMoreListener{
    private boolean isLoadMoreing;

    public HeaderProgressView(Context context) {
        this(context,null);
    }

    public HeaderProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setIndeterminate(true);
    }

    @Override
    public void onBeforeLoad() {

    }

    @Override
    public void onUIPositionChanged(int oldOffset, int newOffset, int loadMoreOffset) {

    }

    @Override
    public void onLoadMoreBegin() {

    }

    @Override
    public void onLoadMoreComplete() {

    }

    @Override
    public void onCanLoadMore() {

    }

}
