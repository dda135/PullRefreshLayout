package fanjh.mine.pullrefreshlayout.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import fanjh.mine.pulllayout.ILoadMoreListener;
import fanjh.mine.pulllayout.IRefreshListener;
import fanjh.mine.pulllayout.PullLayout;
import fanjh.mine.pulllayout.PullLayoutOption;
import fanjh.mine.pullrefreshlayout.R;

/**
 * 内容固定模式，类似于swipeRefresh
 * Created by xuws on 2017/5/11.
 */

public class ContentFixedActivity extends Activity {

    ContentFixedActivity mContext;
    RecyclerView contentRecycleView;
    PullLayout pullLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_fixed);
        mContext = this;
        initVeiw();
    }

    private void initVeiw(){
        pullLayout = (PullLayout) findViewById(R.id.content_fixed_pullLayout);
        contentRecycleView = (RecyclerView) findViewById(R.id.content_fixedrecycleview);
        contentRecycleView.setLayoutManager(new LinearLayoutManager(mContext));
        MyAdapter myAdapter = new MyAdapter();
        contentRecycleView.setAdapter(myAdapter);
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
                return !contentRecycleView.canScrollVertically(-1);
            }

            @Override
            public boolean canDownToUp() {
                //recycleview不能向下滑动，即在首部了，这时候，可以下拉刷新
                return !contentRecycleView.canScrollVertically(1);
            }
        });
    }

    class MyAdapter extends RecyclerView.Adapter{

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(mContext);
            textView.setTextSize(16);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(10,10,10,10);
            return new ViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
             ViewHolder viewHolder = (ViewHolder) holder;
             viewHolder.textView.setText("item" + position);;
        }

        @Override
        public int getItemCount() {
            return 50;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView textView;
            public ViewHolder(View itemView) {
                super(itemView);
                textView = (TextView) itemView;
            }
        }
    }
}
