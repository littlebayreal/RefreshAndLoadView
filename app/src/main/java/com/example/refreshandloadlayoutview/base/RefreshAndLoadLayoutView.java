package com.example.refreshandloadlayoutview.base;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

/**
 * Created by LiTtleBayReal.
 * Date: 2019/12/24
 * Time: 20:19
 * Example:实现下拉刷新和上拉加载的控件 其中上拉加载有粘连模式和列表自动加载模式
 */
public class RefreshAndLoadLayoutView extends LinearLayout {
    private static final String TAG = RefreshAndLoadLayoutView.class.getSimpleName();
    private RefreshAndLoadViewHolder mRefreshViewHolder;
    //获取触发控件移动事件的最短距离
    private int mTouchSlop = 0;
    private Handler mHandler;
    /**
     * 整个头部控件，下拉刷新控件mRefreshHeaderView和下拉刷新控件下方的自定义组件mCustomHeaderView的父控件
     */
    private LinearLayout mWholeHeaderView;
    /**
     * 下拉刷新控件
     */
    private View mRefreshHeaderView;

    /**
     * 下拉控件高度
     * @param context
     */
    private int mRefreshHeaderViewHeight;
    /**
     * 刷新后的提示控件
     */
    private View mShowToastView;
    /**
     * 上拉加载控件
     * @param context
     */
    private View mLoadMoreFooterView;

    /**
     * 上拉加载控件的高度
     * @param context
     */
    private int mLoadMoreFooterViewHeight;

    /**
     * 下拉刷新和上拉加载更多代理
     */
    private RefreshAndLoadListener mDelegate;

    /**
     * 设置上拉加载的方式 0：上拉式  1：列表滚动到自动加载
     * @param context
     */
    private int pullUpType = 0;

    public static final int PULL_UP_HAND = 0;
    public static final int PULL_UP_AUTO = 1;
    public RefreshAndLoadLayoutView(Context context) {
        this(context,null);
    }

    public RefreshAndLoadLayoutView(Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }

    public RefreshAndLoadLayoutView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mHandler = new Handler(Looper.getMainLooper());
        initWholeHeaderView();
    }

    /**
     * 初始化头部布局
     */
    private void initWholeHeaderView() {
        mWholeHeaderView = new LinearLayout(getContext());
        mWholeHeaderView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mWholeHeaderView.setOrientation(LinearLayout.VERTICAL);
        addView(mWholeHeaderView);
    }

    public void setRefreshViewHolder(RefreshAndLoadViewHolder refreshViewHolder) {
        mRefreshViewHolder = refreshViewHolder;
        mRefreshViewHolder.setRefreshLayout(this);
        initRefreshHeaderView();
//        initLoadMoreFooterView();
    }

    /**
     * 初始化头部布局
     */
    private void initRefreshHeaderView() {
//       mWholeHeaderView
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mRefreshViewHolder == null)
        throw new NullPointerException("在使用前，请设置RefreshAndLoadLayoutView");
//        if (pullUpType == )
            throw new NullPointerException("在使用前，请设置RefreshAndLoadLayoutView");
    }

    interface RefreshAndLoadListener {
        /**
         * 刷新回调
         * @param refreshAndLoadLayoutView
         */
        void onRefresh(RefreshAndLoadLayoutView refreshAndLoadLayoutView);

        /**
         * 加载回调
         * @param refreshAndLoadLayoutView
         */
        void onLoad(RefreshAndLoadLayoutView refreshAndLoadLayoutView);
    }
}
