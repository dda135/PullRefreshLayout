package fanjh.mine.pullrefreshlayout.footerview;

import android.content.Context;
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

import fanjh.mine.pulllayout.ILoadMoreListener;
import fanjh.mine.pulllayout.IRefreshListener;
import fanjh.mine.pullrefreshlayout.R;

import static android.view.animation.Animation.INFINITE;

/**
 * Created by faker on 2017/6/8.
 */
public class FooterLayout extends FrameLayout implements ILoadMoreListener{
    private View mBeginView;
    private View mCanLoadView;
    private View mLoadingView;
    private View mLoadCompleteView;
    private ImageView mLoadingImage;
    private boolean isLoading;
    private RotateAnimation mRotateAnimation;

    public FooterLayout(@NonNull Context context) {
        this(context,null);
    }

    public FooterLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FooterLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.view_footer, this, true);
        mBeginView = view.findViewById(R.id.fl_normal_layout);
        mCanLoadView = view.findViewById(R.id.fl_can_load_layout);
        mLoadingView = view.findViewById(R.id.fl_loading_layout);
        mLoadCompleteView = view.findViewById(R.id.fl_load_complete_layout);
        mLoadingImage = (ImageView) mLoadingView.findViewById(R.id.iv_loading);
        mRotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setDuration(400);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.setRepeatCount(INFINITE);
        mLoadingImage.setAnimation(mRotateAnimation);
    }

    @Override
    public void onBeforeLoad() {
        if(isLoading){
            return;
        }
        mBeginView.setVisibility(VISIBLE);
        mCanLoadView.setVisibility(GONE);
        mLoadingView.setVisibility(GONE);
        mLoadCompleteView.setVisibility(GONE);
    }

    @Override
    public void onUIPositionChanged(int oldOffset, int newOffset, int refreshOffset) {
        if(isLoading){
            return;
        }
        if(newOffset <= refreshOffset){
            onCanLoadMore();
        }else{
            onBeforeLoad();
        }
    }

    @Override
    public void onLoadMoreBegin() {
        if(isLoading){
            return;
        }
        isLoading = true;
        mBeginView.setVisibility(GONE);
        mCanLoadView.setVisibility(GONE);
        mLoadingView.setVisibility(VISIBLE);
        mRotateAnimation.start();
        mLoadCompleteView.setVisibility(GONE);
    }

    @Override
    public void onLoadMoreComplete() {
        if(!isLoading){
            return;
        }
        isLoading = false;
        mBeginView.setVisibility(GONE);
        mCanLoadView.setVisibility(GONE);
        mLoadingView.setVisibility(GONE);
        mLoadCompleteView.setVisibility(VISIBLE);
    }

    @Override
    public void onCanLoadMore() {
        if(isLoading){
            return;
        }
        mBeginView.setVisibility(GONE);
        mCanLoadView.setVisibility(VISIBLE);
        mLoadingView.setVisibility(GONE);
        mLoadCompleteView.setVisibility(GONE);
    }

}
