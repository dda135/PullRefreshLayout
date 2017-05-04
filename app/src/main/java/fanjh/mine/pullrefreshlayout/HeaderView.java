package fanjh.mine.pullrefreshlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import fanjh.mine.pulllayout.IRefreshListener;

/**
* @author fanjh
* @date 2017/5/4 11:10
* @description 头部视图
**/
public class HeaderView extends TextView implements IRefreshListener{
    private boolean isBegining;
    public HeaderView(Context context) {
        this(context,null);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        setTextSize(22);
        setBackgroundColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    public void onRefreshBegin() {
        isBegining = true;
        setText("onRefreshBegin");
    }

    @Override
    public void onUIPositionChanged(int oldOffset, int newOffset) {
        if(!isBegining) {
            setText("oldOffset-->" + oldOffset + "--newOffset-->" + newOffset);
        }
    }

    @Override
    public void onRefreshComplete() {
        isBegining = false;
        setText("onRefreshComplete");
    }

}
