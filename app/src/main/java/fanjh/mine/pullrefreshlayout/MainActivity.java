package fanjh.mine.pullrefreshlayout;

import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.ScrollView;

import fanjh.mine.pulllayout.ILoadMoreListener;
import fanjh.mine.pulllayout.IRefreshListener;
import fanjh.mine.pulllayout.PullLayout;
import fanjh.mine.pulllayout.PullLayoutOption;

public class MainActivity extends FragmentActivity {
    private PullLayout mPullLayout;
    private ScrollView mScrollview;
    private HeaderView mHeaderView;
    private FooterView mFooterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        mPullLayout = (PullLayout) findViewById(R.id.pl_parent_layout);
        mHeaderView = (HeaderView) findViewById(R.id.hv_header);
        mFooterView = (FooterView) findViewById(R.id.hv_footer);
        mScrollview = (ScrollView) findViewById(R.id.sv_content);
        mPullLayout.setOnCheckHandler(new PullLayoutOption.OnCheckHandler() {
            @Override
            public boolean canUpTpDown() {
                return !mScrollview.canScrollVertically(-1);
            }

            @Override
            public boolean canDownToUp() {
                return !mScrollview.canScrollVertically(1);
            }
        });
        mPullLayout.addRefreshListener(new IRefreshListener() {
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
            public void onUIPositionChanged(int oldOffset, int newOffset) {

            }

            @Override
            public void onRefreshComplete() {

            }
        });
        mPullLayout.addLoadMoreListener(new ILoadMoreListener() {
            @Override
            public void onUIPositionChanged(int oldOffset, int newOffset) {

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
        });

        mPullLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullLayout.autoRefresh();
            }
        },1000);


    }
}
