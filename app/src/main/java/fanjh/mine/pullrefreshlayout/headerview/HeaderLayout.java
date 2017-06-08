package fanjh.mine.pullrefreshlayout.headerview;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import fanjh.mine.pulllayout.IRefreshListener;
import fanjh.mine.pullrefreshlayout.R;

import static android.view.animation.Animation.INFINITE;

/**
 * Created by faker on 2017/6/8.
 */
public class HeaderLayout extends FrameLayout implements IRefreshListener{
    private View mBeginView;
    private View mCanRefreshView;
    private View mRefreshingView;
    private View mRefreshCompleteView;
    private ImageView mRefreshingImage;
    private boolean isRefreshing;
    private RotateAnimation mRotateAnimation;

    public HeaderLayout(@NonNull Context context) {
        this(context,null);
    }

    public HeaderLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.view_header, this, true);
        mBeginView = view.findViewById(R.id.fl_normal_layout);
        mCanRefreshView = view.findViewById(R.id.fl_can_refresh_layout);
        mRefreshingView = view.findViewById(R.id.fl_refreshing_layout);
        mRefreshCompleteView = view.findViewById(R.id.fl_refresh_complete_layout);
        mRefreshingImage = (ImageView) mRefreshingView.findViewById(R.id.iv_refreshing);
        mRotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setDuration(400);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setRepeatCount(INFINITE);
        mRefreshingImage.setAnimation(mRotateAnimation);
    }

    @Override
    public void onBeforeRefresh() {
        if(isRefreshing){
            return;
        }
        mBeginView.setVisibility(VISIBLE);
        mCanRefreshView.setVisibility(GONE);
        mRefreshingView.setVisibility(GONE);
        mRefreshCompleteView.setVisibility(GONE);
    }

    @Override
    public void onRefreshBegin() {
        if(isRefreshing){
            return;
        }
        isRefreshing = true;
        mBeginView.setVisibility(GONE);
        mCanRefreshView.setVisibility(GONE);
        mRefreshingView.setVisibility(VISIBLE);
        mRotateAnimation.start();
        mRefreshCompleteView.setVisibility(GONE);
    }

    @Override
    public void onUIPositionChanged(int oldOffset, int newOffset, int refreshOffset) {
        if(isRefreshing){
            return;
        }
        if(newOffset < refreshOffset){
            onBeforeRefresh();
        }else{
            onCanRefresh();
        }
    }

    @Override
    public void onRefreshComplete() {
        if(!isRefreshing){
            return;
        }
        isRefreshing = false;
        mBeginView.setVisibility(GONE);
        mCanRefreshView.setVisibility(GONE);
        mRefreshingView.setVisibility(GONE);
        mRefreshCompleteView.setVisibility(VISIBLE);
    }

    @Override
    public void onCanRefresh() {
        if(isRefreshing){
            return;
        }
        mBeginView.setVisibility(GONE);
        mCanRefreshView.setVisibility(VISIBLE);
        mRefreshingView.setVisibility(GONE);
        mRefreshCompleteView.setVisibility(GONE);
    }
}
