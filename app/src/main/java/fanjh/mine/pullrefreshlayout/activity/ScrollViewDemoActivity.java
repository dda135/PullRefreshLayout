package fanjh.mine.pullrefreshlayout.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.ScrollView;
import android.widget.Toast;

import fanjh.mine.pulllayout.ILoadMoreListener;
import fanjh.mine.pulllayout.IRefreshListener;
import fanjh.mine.pulllayout.PullLayout;
import fanjh.mine.pulllayout.PullLayoutOption;
import fanjh.mine.pullrefreshlayout.R;

/**
 * Created by xuws on 2017/5/11.
 */

public class ScrollViewDemoActivity extends Activity {

    ScrollViewDemoActivity mContext;
    PullLayout pullLayout;
    ScrollView scrollView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_scrollview);
        pullLayout = (PullLayout) findViewById(R.id.scrollviewview_pullLayout);
        scrollView = (ScrollView) findViewById(R.id.content_scrollview);
        mContext = this;
        initView();
    }

    private void initView(){
        /**
         * 刷新滑动监听
         */
        pullLayout.addRefreshListener(new IRefreshListener() {
            @Override
            public void onBeforeRefresh() {
                Toast.makeText(mContext,"亲，还没到刷新条件哦~",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRefreshBegin() {
                Toast.makeText(mContext,"亲，开始刷新了哦~",Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullLayout.refreshComplete();
                    }
                },3000);
            }

            @Override
            public void onUIPositionChanged(int oldOffset, int newOffset, int refreshOffset) {

            }

            @Override
            public void onRefreshComplete() {
                Toast.makeText(mContext,"亲，刷新已完成~",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCanRefresh() {

            }
        });

        /**
         * 加载滑动监听
         */
        pullLayout.addLoadMoreListener(new ILoadMoreListener() {
            @Override
            public void onBeforeLoad() {
                Toast.makeText(mContext,"亲，还没到加载条件哦~",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUIPositionChanged(int oldOffset, int newOffset, int loadMoreOffset) {

            }

            @Override
            public void onLoadMoreBegin() {
                Toast.makeText(mContext,"亲，加载开始了哦~",Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullLayout.loadingComplete();
                    }
                },3000);
            }

            @Override
            public void onLoadMoreComplete() {
                Toast.makeText(mContext,"亲，加载完成了哦~",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCanLoadMore() {

            }
        });

        /**
         * 设置可以下拉刷新和上拉加载更多的条件
         */
        pullLayout.setOnCheckHandler(new PullLayoutOption.OnCheckHandler() {
            @Override
            public boolean canUpTpDown() {
                //recycleView不能向上滑动，即滑到底了，这时候，可以加载更多
                return !scrollView.canScrollVertically(-1);
            }

            @Override
            public boolean canDownToUp() {
                //recycleview不能向下滑动，即在首部了，这时候，可以下拉刷新
                return !scrollView.canScrollVertically(1);
            }
        });
    }
}
