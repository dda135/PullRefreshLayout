package fanjh.mine.pullrefreshlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ProgressBar;
import android.widget.TextView;

import fanjh.mine.pulllayout.ILoadMoreListener;

/**
* @author fanjh
* @date 2017/5/4 11:10
* @description 底部视图
**/
public class FooterProgressView extends ProgressBar implements ILoadMoreListener{
    private boolean isLoadMoreing;

    public FooterProgressView(Context context) {
        this(context,null);
    }

    public FooterProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setIndeterminate(true);
    }

    @Override
    public void onBeforeLoad() {

    }

    @Override
    public void onUIPositionChanged(int oldOffset, int newOffset) {
    }

    @Override
    public void onLoadMoreBegin() {

    }

    @Override
    public void onLoadMoreComplete() {

    }

}
