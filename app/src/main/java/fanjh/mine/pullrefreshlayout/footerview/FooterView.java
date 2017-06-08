package fanjh.mine.pullrefreshlayout.footerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import fanjh.mine.pulllayout.ILoadMoreListener;
import fanjh.mine.pullrefreshlayout.R;

/**
* @author fanjh
* @date 2017/5/4 11:10
* @description 底部视图
**/
public class FooterView extends TextView implements ILoadMoreListener{
    private boolean isLoadMoreing;

    public FooterView(Context context) {
        this(context,null);
    }

    public FooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        setTextSize(22);
        setBackgroundColor(getResources().getColor(R.color.colorAccent));
    }

    @Override
    public void onBeforeLoad() {

    }

    @Override
    public void onUIPositionChanged(int oldOffset, int newOffset, int loadMoreOffset) {

    }

    @Override
    public void onLoadMoreBegin() {
        isLoadMoreing = true;
        setText("onLoadMoreBegin");
    }

    @Override
    public void onLoadMoreComplete() {
        isLoadMoreing = false;
        setText("onLoadMoreComplete");
    }

    @Override
    public void onCanLoadMore() {

    }

}
