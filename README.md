# PullRefreshLayout
已知问题：<br/>
1.在遇到低于23.0.0版本的RecyclerView的时候，因为RecyclerView本身对于Nested的支持不完全，所以这种时候不建议使用nested模式。<br/>
2.稍微解释一下添加一个canDisallowInterceptorTouchEvent的意义，比方说一个RecyclerView默认在move的时候是禁止父布局拦截事件的，<br/>
  但是有的时候可能你希望RecyclerView划到顶部的时候就要触发顶部刷新事件，这个属性可以帮助到你。<br/>
3.在使用autoRefresh这种方法的时候，和SwipeRefreshLayout一样，我建议在onPreDraw中执行，保证视图在绘制和测量一次之后进行<br/>
使用说明：<br/>
1.支持固定视图内容的模式，类似SwipeRefreshLayout<br/>
2.支持滑动阻尼比例<br/>
3.支持自定义滑动条件，从而实现支持不同子布局<br/>
4.头部视图等都是推荐自己实现的，包括记录是否刷新中等状态，从而做到轻松的分离和组合<br/>
5.支持刷新完成后延时，可以做到今日头条拉取的停顿展示效果<br/>
等等<br/>
更多细节解释在http://www.jianshu.com/p/8bc3310eb404<br/>
![效果图.gif](http://upload-images.jianshu.io/upload_images/2406298-759c803c0ee295d7.gif?imageMogr2/auto-orient/strip)

usage  
------- 
```
compile 'com.github.fanjinhao:pull-refresh-layout:1.0.0'
```

```
<?xml version="1.0" encoding="utf-8"?>
<fanjh.mine.pulllayout.PullLayout android:id="@+id/nestscrolling_pullLayout"
                                  xmlns:android="http://schemas.android.com/apk/res/android"
                                  xmlns:app="http://schemas.android.com/apk/res-auto"
                                  android:layout_width="match_parent"
                                  android:layout_height="match_parent"
                                  android:orientation="vertical"
                                  app:contentFixed="false"
                                  app:disabledNestedScrolling="false"
                                  app:loadMoreOffset="60dp"
                                  app:maxDownOffset="240dp"
                                  app:maxUpOffset="120dp"
                                  app:refreshCompleteDelayedTime="500"
                                  app:refreshOffset="60dp"
    >

    <LinearLayout
        android:id="@+id/linear"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:gravity="center"
            android:text="我是标题"/>

        <android.support.v4.widget.NestedScrollView
            android:id="@+id/content_nestscrolling"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:orientation="vertical">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:background="@color/colorPrimaryDark"/>

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:background="@color/colorGray"/>

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:background="@color/colorPrimary"/>

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:background="@color/colorGray"/>

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:background="@color/colorPrimaryDark"/>

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:background="@color/colorGray"/>

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="200dp"
                    android:background="@color/colorPrimaryDark"/>

            </LinearLayout>

        </android.support.v4.widget.NestedScrollView>

    </LinearLayout>

    <fanjh.mine.pullrefreshlayout.headerview.HeaderLayout
        android:id="@+id/nestscrolling_headerview"
        android:layout_width="match_parent"
        android:layout_height="60dp"/>

    <fanjh.mine.pullrefreshlayout.footerview.FooterLayout
        android:id="@+id/nestscrolling_footerview"
        android:layout_width="match_parent"
        android:layout_height="60dp"/>


</fanjh.mine.pulllayout.PullLayout>
```
简单的使用
```
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
         * 设置可以下拉刷新和上拉加载更多的条件,主要用于自定义能否触发滑动事件
         */
        pullLayout.setOnCheckHandler(new PullLayoutOption.OnCheckHandler() {
            @Override
            public boolean canUpTpDown() {
                //recycleView不能向上滑动，即滑到底了，这时候，可以加载更多
                return !nestedScrollView.canScrollVertically(-1);
            }

            @Override
            public boolean canDownToUp() {
                //recycleview不能向下滑动，即在首部了，这时候，可以下拉刷新
                return !nestedScrollView.canScrollVertically(1);
            }
        });
```
更多可以看代码里面的demo
