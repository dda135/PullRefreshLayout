package fanjh.mine.pulllayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
* @author fanjh
* @date 2017/6/22 14:12
* @description
* @note
**/
public class IconHeadLayout extends FrameLayout implements IRefreshListener{
    private EasyPathView normalPathView;
    private EasyPathView lightPathView;
    private TextView hintTextView;
    private LinearLayout parentLayout;


    public IconHeadLayout(Context context) {
        this(context,null);
    }

    public IconHeadLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconHeadLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parentLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.view_icon,null);
        FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDimensionPixelOffset(R.dimen.pull_layout_header_height));
        params.gravity = Gravity.CENTER;
        normalPathView = (EasyPathView) parentLayout.findViewById(R.id.epv_header_icon);
        lightPathView = (EasyPathView) parentLayout.findViewById(R.id.epv_header_icon_light);
        hintTextView = (TextView) parentLayout.findViewById(R.id.tv_header_hint_text);
        hintTextView.setText("准备加载！");
        addView(parentLayout,params);
    }

    @Override
    public void onBeforeRefresh() {

    }

    @Override
    public void onRefreshBegin() {
        hintTextView.setText("加载中。。。");
        lightPathView.startDraw(true);
    }

    @Override
    public void onUIPositionChanged(int oldOffset, int newOffset, int refreshOffset) {
        normalPathView.setAnimProgress((newOffset * 1.0f) / refreshOffset);
    }

    @Override
    public void onRefreshComplete() {
        lightPathView.stopRepeat();
        hintTextView.setText("加载完成");
    }

    @Override
    public void onCanRefresh() {

    }
}
