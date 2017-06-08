package fanjh.mine.pullrefreshlayout;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import fanjh.mine.pulllayout.ILoadMoreListener;
import fanjh.mine.pulllayout.IRefreshListener;
import fanjh.mine.pulllayout.PullLayout;
import fanjh.mine.pulllayout.PullLayoutOption;
import fanjh.mine.pullrefreshlayout.footerview.FooterProgressView;
import fanjh.mine.pullrefreshlayout.footerview.FooterView;
import fanjh.mine.pullrefreshlayout.headerview.HeaderView;

public class MainActivity extends FragmentActivity {
    private PullLayout mPullLayout;
    private NestedScrollView mScrollview;
    private RecyclerView mListView;
    private HeaderView mHeaderView;
    private FooterView mFooterView;
    private FooterProgressView mFootProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_list);
        mPullLayout = (PullLayout) findViewById(R.id.pl_parent_layout);
        //mPullLayout.setContentFixed(false);
        /*mPullLayout.post(new Runnable() {
            @Override
            public void run() {
                //mPullLayout.setLoadMoreOffset(mPullLayout.getFooterHeight(),mPullLayout.getFooterHeight()<<1);
                //mPullLayout.setRefreshOffset(mPullLayout.getHeaderHeight(),mPullLayout.getHeaderHeight()<<1);
                mPullLayout.setLoadMoreOffset(mPullLayout.getFooterHeight(),450);
                mPullLayout.setRefreshOffset(mPullLayout.getHeaderHeight(),600);
            }
        });*/
        //mHeaderView = (HeaderView) findViewById(R.id.hv_header);
        //mFooterView = (FooterView) findViewById(R.id.hv_footer);
        //mScrollview = (NestedScrollView) findViewById(R.id.sv_content);
        mListView = (RecyclerView) findViewById(R.id.sv_content);
        mListView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        mListView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView textView = new TextView(getApplicationContext());
                textView.setTextSize(20);
                textView.setGravity(Gravity.CENTER);
                textView.setPadding(10,10,10,10);
                return new ViewHolder(textView);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ViewHolder viewHolder = (ViewHolder) holder;
                viewHolder.textView.setText("list"+position);
            }

            @Override
            public int getItemCount() {
                return 60;
            }

            class ViewHolder extends RecyclerView.ViewHolder{
                TextView textView;
                public ViewHolder(View itemView) {
                    super(itemView);
                    textView = (TextView) itemView;
                }
            }

        });
        mPullLayout.setOnCheckHandler(new PullLayoutOption.OnCheckHandler() {
            @Override
            public boolean canUpTpDown() {
                //return !mScrollview.canScrollVertically(-1);
                return !mListView.canScrollVertically(-1);
            }

            @Override
            public boolean canDownToUp() {
                //return !mScrollview.canScrollVertically(1);
                return !mListView.canScrollVertically(1);
            }
        });
        mPullLayout.addRefreshListener(new IRefreshListener() {
            @Override
            public void onBeforeRefresh() {

            }

            @Override
            public void onRefreshBegin() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullLayout.refreshComplete();
                    }
                }, 3000);
            }

            @Override
            public void onUIPositionChanged(int oldOffset, int newOffset, int refreshOffset) {

            }

            @Override
            public void onRefreshComplete() {

            }

            @Override
            public void onCanRefresh() {

            }
        });
        mPullLayout.addLoadMoreListener(new ILoadMoreListener() {
            @Override
            public void onBeforeLoad() {

            }

            @Override
            public void onUIPositionChanged(int oldOffset, int newOffset, int loadMoreOffset) {

            }

            @Override
            public void onLoadMoreBegin() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullLayout.loadingComplete();
                    }
                }, 3000);
            }

            @Override
            public void onLoadMoreComplete() {

            }

            @Override
            public void onCanLoadMore() {

            }
        });

       /* mPullLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullLayout.autoRefresh();
                mPullLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullLayout.autoLoading();
                    }
                },5000);
            }
        },1000);*/


    }
}
