package fanjh.mine.pullrefreshlayout.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import fanjh.mine.pullrefreshlayout.R;

/**
 * Created by xuws on 2017/5/11.
 */

public class ProjectActivity extends Activity{

    private ProjectActivity mContext;
    Button contentFixed;
    Button btnScrollView;
    Button btnListView;
    Button btnRecycleView;
    Button btnNestScrooling;
    Button btnAutoRefreshOrLoad;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_project);
        initView();
        initListener();
    }

    private void initView(){
        contentFixed = (Button) findViewById(R.id.btn_contentfixed);
        btnListView = (Button) findViewById(R.id.btn_contatin_listview);
        btnRecycleView = (Button) findViewById(R.id.btn_contatin_recycleview);
        btnScrollView = (Button) findViewById(R.id.btn_contatin_scrollview);
        btnNestScrooling = (Button) findViewById(R.id.btn_contatin_nestscrooling);
        btnAutoRefreshOrLoad = (Button) findViewById(R.id.btn_autoRefreshingOrLoadMore);
    }

    private void initListener(){
        contentFixed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProjectActivity.this,ContentFixedActivity.class));
            }
        });

        btnListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProjectActivity.this,ListViewDemoActivity.class));
            }
        });

        btnScrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProjectActivity.this,ScrollViewDemoActivity.class));
            }
        });

       btnRecycleView.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(ProjectActivity.this,RecycleViewDemoActivity.class));
           }
       });

       btnNestScrooling.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               startActivity(new Intent(ProjectActivity.this,NestScrollingDemoActivity.class));
           }
       });

        btnAutoRefreshOrLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProjectActivity.this,AutoRefreshOrLoadDemoActivity.class));
            }
        });

    }

}
