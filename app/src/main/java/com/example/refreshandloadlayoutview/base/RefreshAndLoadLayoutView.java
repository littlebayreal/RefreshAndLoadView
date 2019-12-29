package com.example.refreshandloadlayoutview.base;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.refreshandloadlayoutview.base.util.BGARefreshScrollingUtil;

import org.xml.sax.HandlerBase;

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
     *
     * @param context
     */
    private int mRefreshHeaderViewHeight;
    /**
     * 整个头部控件最小的paddingTop
     */
    private int mMinWholeHeaderViewPaddingTop;
    /**
     * 整个头部控件最大的paddingTop
     */
    private int mMaxWholeHeaderViewPaddingTop;
    /**
     * 刷新后的提示控件
     */
    private TipView mShowToastView;
    /**
     * 提示控件的高度
     */
    private int mShowToastViewHeight;
    /**
     * 上拉加载控件
     *
     * @param context
     */
    private View mLoadMoreFooterView;

    /**
     * 上拉加载控件的高度
     *
     * @param context
     */
    private int mLoadMoreFooterViewHeight;

    /**
     * 上拉加载控件的临界值
     */
    private int mLoadMoreFooterViewCriticalValue;
    /**
     * 下拉刷新和上拉加载更多代理
     */
    private RefreshAndLoadListener mDelegate;

    /**
     * 设置上拉加载的方式 0：上拉式  1：列表滚动到自动加载
     *
     * @param context
     */
    private int pullUpType = PULL_UP_AUTO;

    public static final int PULL_UP_HAND = 0;
    public static final int PULL_UP_AUTO = 1;

    /**
     * 当前刷新状态 默认是普通状态
     */
    private RefreshStatus mCurrentRefreshStatus = RefreshStatus.IDLE;
    /**
     * 是否处于正在加载更多状态
     */
//	private boolean mIsLoadingMore = false;
    private LoadMoreStatus mCurrentLoadMoreStatus = LoadMoreStatus.IDLE;

    private AbsListView mAbsListView;
    private ScrollView mScrollView;
    private RecyclerView mRecyclerView;
    private View mNormalView;
    private WebView mWebView;
    //	private BGAStickyNavLayout mStickyNavLayout;
    private View mContentView;
    private boolean isLoadMoreEnable = false;

    public RefreshAndLoadLayoutView(Context context) {
        this(context, null);
    }

    public RefreshAndLoadLayoutView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
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

        isLoadMoreEnable = refreshViewHolder.mIsLoadingMoreEnabled;
        pullUpType = refreshViewHolder.getMPullType();
        initRefreshHeaderView();
        initLoadMoreFooterView();


        onFinishInflate();
    }

    /**
     * 初始化底部布局
     */
    private void initLoadMoreFooterView() {
        mLoadMoreFooterView = mRefreshViewHolder.getLoadMoreFooterView();
        if (mLoadMoreFooterView != null) {
            // 测量上拉加载更多控件的高度
            mLoadMoreFooterView.measure(0, 0);
            if (pullUpType == PULL_UP_HAND) {
                mLoadMoreFooterViewHeight = (int) (mLoadMoreFooterView.getMeasuredHeight() * 1.5);
                Log.i(TAG, "mLoadMoreFooterView()"+ mLoadMoreFooterViewHeight);
//			mLoadMoreFooterViewCriticalValue = (int) (mLoadMoreFooterViewHeight * mRefreshViewHolder.getSpringDistanceScale());
                mLoadMoreFooterViewCriticalValue = mLoadMoreFooterViewHeight - 50;
            }
            if (pullUpType == PULL_UP_AUTO) {
                mLoadMoreFooterViewHeight = (int) mLoadMoreFooterView.getMeasuredHeight();
                mLoadMoreFooterView.setVisibility(GONE);
            }
        }
    }

    /**
     * 初始化头部布局
     */
    private void initRefreshHeaderView() {
        mRefreshHeaderView = mRefreshViewHolder.getRefreshHeaderView();
        mShowToastView = mRefreshViewHolder.getShowToastView();
        if (mRefreshHeaderView != null) {
            mRefreshHeaderView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            mRefreshHeaderViewHeight = mRefreshViewHolder.getRefreshHeaderViewHeight();
            mMinWholeHeaderViewPaddingTop = -mRefreshHeaderViewHeight;
            mMaxWholeHeaderViewPaddingTop = (int) (mRefreshHeaderViewHeight * mRefreshViewHolder.getSpringDistanceScale());
//			mMaxWholeHeaderViewPaddingTop = mRefreshHeaderViewHeight;
            Log.d(TAG, "mMinWholeHeaderViewPaddingTop:" + mMinWholeHeaderViewPaddingTop + "mMaxWholeHeaderViewPaddingTop:" + mMaxWholeHeaderViewPaddingTop);
            mWholeHeaderView.setPadding(0, mMinWholeHeaderViewPaddingTop, 0, 0);
            mWholeHeaderView.addView(mRefreshHeaderView, 0);
        }
        if (mShowToastView != null) {
            //初始高度为0
            mShowToastView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            mShowToastViewHeight = mRefreshViewHolder.getShowToastViewHeight();
            mWholeHeaderView.addView(mShowToastView, 1);
        }
    }

    /**
     * 是否已经设置内容控件滚动监听器
     */
    private boolean mIsInitedContentViewScrollListener = false;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // 被添加到窗口后再设置监听器，这样开发者就不必烦恼先初始化RefreshLayout还是先设置自定义滚动监听器
        if (!mIsInitedContentViewScrollListener && mLoadMoreFooterView != null) {
            Log.i(TAG, "onAttachedToWindow()"+ mLoadMoreFooterViewHeight);
            setRecyclerViewOnScrollListener();
//			setAbsListViewOnScrollListener();
            /**
             * 将加载更多添加到view中
             */
            mLoadMoreFooterView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mLoadMoreFooterViewHeight));
            addView(mLoadMoreFooterView, getChildCount());
            Log.i(TAG, "onAttachedToWindow()"+ getChildCount());
            mIsInitedContentViewScrollListener = true;
        }
    }

    /**
     * 通过recyclerview的滚动监听 决定是否显示加载更多
     */
    private void setRecyclerViewOnScrollListener() {
        if (pullUpType == PULL_UP_AUTO && isLoadMoreEnable && mRecyclerView != null) {
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    if ((newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_SETTLING) && shouldHandleRecyclerViewLoadingMore(mRecyclerView)) {
                        beginLoading();
                    }
                }
            });
        }
    }

    @Override
    protected void onFinishInflate() {
        Log.i(TAG, "onFinishInflate()" + getChildCount());
        if (mRefreshViewHolder != null) {
            super.onFinishInflate();
//		if (pullUpType == PULL_UP_HAND && getChildCount() != 3)
//			throw new RuntimeException("在使用前，请设置mLoadMoreFooterView");
            if (pullUpType == PULL_UP_AUTO && getChildCount() != 2)
                throw new RuntimeException("在使用前，请设置mRefreshHeaderView");

            mContentView = getChildAt(1);
            if (mContentView instanceof AbsListView) {
                mAbsListView = (AbsListView) mContentView;
            } else if (mContentView instanceof RecyclerView) {
                mRecyclerView = (RecyclerView) mContentView;
                if (pullUpType == PULL_UP_HAND) {
                    Log.i(TAG, "onFinishInflate() pullUpType" + pullUpType);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                    mRecyclerView.setLayoutParams(params);
                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0);
                    params.weight = 1;
                    mRecyclerView.setLayoutParams(params);
                }
            } else if (mContentView instanceof ScrollView) {
                mScrollView = (ScrollView) mContentView;
            } else if (mContentView instanceof WebView) {
                mWebView = (WebView) mContentView;
            } else if (mContentView instanceof FrameLayout) {
                FrameLayout frameLayout = (FrameLayout) mContentView;
                View childView = frameLayout.getChildAt(0);
                if (childView instanceof RecyclerView) {
                    mRecyclerView = (RecyclerView) childView;
                }
            } else {
                mNormalView = mContentView;
                // 设置为可点击，否则在空白区域无法拖动
                mNormalView.setClickable(true);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //所有事件都接收并默认处理
        super.dispatchTouchEvent(ev);
        return true;
    }

    private float mInterceptTouchDownX = 0;
    private float mInterceptTouchDownY = 0;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInterceptTouchDownX = event.getRawX();
                mInterceptTouchDownY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if ((mCurrentLoadMoreStatus != LoadMoreStatus.LOADING) && (mCurrentRefreshStatus != RefreshStatus.REFRESHING)) {
                    if (mInterceptTouchDownX == -1) {
                        mInterceptTouchDownX = event.getRawX();
                    }
                    if (mInterceptTouchDownY == -1) {
                        mInterceptTouchDownY = event.getRawY();
                    }
                    int interceptTouchMoveDistanceY = (int) (event.getRawY() - mInterceptTouchDownY);
                    // 可以没有上拉加载更多，但是必须有下拉刷新，否则就不拦截事件
                    // 滑动的判断必须是垂直的滑动距离大于横向的滑动距离
                    Log.i(TAG, "shouldHandleLoadingMore():" + shouldHandleLoadingMore());
                    if (Math.abs(event.getRawX() - mInterceptTouchDownX) < Math.abs(interceptTouchMoveDistanceY) && mRefreshHeaderView != null) {
                        if ((interceptTouchMoveDistanceY > mTouchSlop && shouldHandleRefresh())
                                || (interceptTouchMoveDistanceY < -mTouchSlop && shouldHandleLoadingMore())) {
                            // ACTION_DOWN时没有消耗掉事件，子控件会处于按下状态，这里设置ACTION_CANCEL，使子控件取消按下状态
                            event.setAction(MotionEvent.ACTION_CANCEL);
                            super.onInterceptTouchEvent(event);
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // 重置
                mInterceptTouchDownX = -1;
                mInterceptTouchDownY = -1;
                break;
        }
        return super.onInterceptTouchEvent(event);
    }

    /**
     * 手指按下时，y轴方向的偏移量
     */
    private int mWholeHeaderDownY = -1;
    /**
     * 记录开始下拉刷新时的downY
     */
    private int mRefreshDownY = -1;
    /**
     * 记录上拉加载时的拉动距离
     */
    private int mLoadMoreDiffHeight = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mRefreshHeaderView != null) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mWholeHeaderDownY = (int) event.getY();
//					if (mCustomHeaderView != null) {
//						mWholeHeaderViewDownPaddingTop = mWholeHeaderView.getPaddingTop();
//					}
//					if (!mIsCustomHeaderViewScrollable) {
//						mRefreshDownY = (int) event.getY();
//					}
//					if (mCurrentRefreshStatus == RefreshStatus.IDLE) {
                    mRefreshDownY = (int) event.getY();
//						return true;
//					}
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (handleActionMove(event)) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (handleActionUpOrCancel(event)) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 处理手指滑动事件
     *
     * @param event
     * @return true表示自己消耗掉该事件，false表示不消耗该事件
     */
    private boolean handleActionMove(MotionEvent event) {
        if (mCurrentRefreshStatus == RefreshStatus.REFRESHING || mCurrentLoadMoreStatus == LoadMoreStatus.LOADING) {
            return false;
        }
        //当第一个action_down的时候 事件被传到子控件 所以这里是默认值-1
        if (mRefreshDownY == -1) {
            mRefreshDownY = (int) event.getY();
        }
        int refreshDiffY = (int) event.getY() - mRefreshDownY;
        Log.d(TAG, "refreshDiffY:" + refreshDiffY);
        refreshDiffY = (int) (refreshDiffY / mRefreshViewHolder.getPaddingTopScale());
        // 如果是向下拉，并且当前可见的第一个条目的索引等于0，才处理整个头部控件的padding  这是处理下拉事件
        if (refreshDiffY > 0 && shouldHandleRefresh()) {
            int paddingTop = mMinWholeHeaderViewPaddingTop + refreshDiffY;
            Log.d(TAG, "paddingTop：" + paddingTop);
            //paddingTop > 0 就是下拉控件完全显示
            if (paddingTop > 0 && mCurrentRefreshStatus != RefreshStatus.RELEASE_REFRESH) {
                // 下拉刷新控件完全显示，并且当前状态没有处于释放开始刷新状态
                mCurrentRefreshStatus = RefreshStatus.RELEASE_REFRESH;
                handleRefreshStatusChanged();
                mRefreshViewHolder.handleScale(1.0f, refreshDiffY);

//				if (mRefreshScaleDelegate != null) {
//					mRefreshScaleDelegate.onRefreshScaleChanged(1.0f, refreshDiffY);
//				}
            } else if (paddingTop < 0) {//下拉刷新控件在初始状态或者是下拉到一部分的状态
                // 下拉刷新控件没有完全显示，并且当前状态没有处于下拉刷新状态
                Log.v(TAG, "mCurrentLoadMoreStatus1：" + mCurrentRefreshStatus);
                if (mCurrentRefreshStatus != RefreshStatus.PULL_DOWN) {
                    boolean isPreRefreshStatusNotIdle = mCurrentRefreshStatus != RefreshStatus.IDLE;
                    mCurrentRefreshStatus = RefreshStatus.PULL_DOWN;
                    if (isPreRefreshStatusNotIdle) {
                        Log.v(TAG, "mCurrentLoadMoreStatus1：handleRefreshStatusChanged");
                        handleRefreshStatusChanged();
                    }
                }
                float scale = 1 - paddingTop * 1.0f / mMinWholeHeaderViewPaddingTop;
                /**
                 * 往下滑
                 * paddingTop    mMinWholeHeaderViewPaddingTop 到 0
                 * scale         0 到 1
                 * 往上滑
                 * paddingTop    0 到 mMinWholeHeaderViewPaddingTop
                 * scale         1 到 0
                 */
                mRefreshViewHolder.handleScale(scale, refreshDiffY);

//				if (mRefreshScaleDelegate != null) {
//					mRefreshScaleDelegate.onRefreshScaleChanged(scale, refreshDiffY);
//				}
            }
            //比较一个相对较小的值 设置整体头布局的paddingtop  这样就不会让头布局超出最大的下拉范围
            paddingTop = Math.min(paddingTop, mMaxWholeHeaderViewPaddingTop);
            mWholeHeaderView.setPadding(0, paddingTop, 0, 0);

//			if (mRefreshViewHolder.canChangeToRefreshingStatus()) {
//				mWholeHeaderDownY = -1;
//				mRefreshDownY = -1;
//				//执行刷新逻辑
//				beginRefreshing();
//			}
            return true;
        }
        Log.i(TAG, "refreshDiffY:" + refreshDiffY + "shouldHandleLoadingMore()" + shouldHandleLoadingMore());
        //这是处理上拉加载
        if (isLoadMoreEnable && pullUpType == PULL_UP_HAND && refreshDiffY < 0 && shouldHandleLoadingMore()) {
            mLoadMoreDiffHeight = Math.abs(refreshDiffY);
            if (mLoadMoreDiffHeight > mLoadMoreFooterViewCriticalValue && mCurrentLoadMoreStatus != LoadMoreStatus.RELEASE_LOAD) {
                mCurrentLoadMoreStatus = LoadMoreStatus.RELEASE_LOAD;
                handleLoadMoreStatusChanged();
                Log.i(TAG, "mCurrentLoadMoreStatus2：" + mCurrentLoadMoreStatus);
            } else if (mLoadMoreDiffHeight > 0 && mLoadMoreDiffHeight < mLoadMoreFooterViewCriticalValue) {//没有上拉到临界值
                Log.v(TAG, "mCurrentLoadMoreStatus2：" + mCurrentLoadMoreStatus);
                if (mCurrentLoadMoreStatus != LoadMoreStatus.PULL_UP) {
                    boolean isPreLoadStatusNotIdle = mCurrentLoadMoreStatus != LoadMoreStatus.IDLE;
                    mCurrentLoadMoreStatus = LoadMoreStatus.PULL_UP;
                    if (isPreLoadStatusNotIdle) {
                        handleLoadMoreStatusChanged();
                    }
                }
            }
            mLoadMoreDiffHeight = Math.min(mLoadMoreDiffHeight, mLoadMoreFooterViewHeight);
            Log.i(TAG, "height:" + mLoadMoreDiffHeight);

            //通过整体布局的移动实现上拉加载的效果
            requestLayout();
            return true;
        }
//		if (mCustomHeaderView != null && mIsCustomHeaderViewScrollable) {
//			if (mWholeHeaderDownY == -1) {
//				mWholeHeaderDownY = (int) event.getY();
//
//				if (mCustomHeaderView != null) {
//					mWholeHeaderViewDownPaddingTop = mWholeHeaderView.getPaddingTop();
//				}
//			}
//
//			int wholeHeaderDiffY = (int) event.getY() - mWholeHeaderDownY;
//			if ((mPullDownRefreshEnable && !isWholeHeaderViewCompleteInvisible()) || (wholeHeaderDiffY > 0 && shouldInterceptToMoveCustomHeaderViewDown()) || (wholeHeaderDiffY < 0 && shouldInterceptToMoveCustomHeaderViewUp())) {
//
//				int paddingTop = mWholeHeaderViewDownPaddingTop + wholeHeaderDiffY;
//				if (paddingTop < mMinWholeHeaderViewPaddingTop - mCustomHeaderView.getMeasuredHeight()) {
//					paddingTop = mMinWholeHeaderViewPaddingTop - mCustomHeaderView.getMeasuredHeight();
//				}
//				mWholeHeaderView.setPadding(0, paddingTop, 0, 0);
//
//				return true;
//			}
//		}
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i(TAG, "shouldHandleLoadingMore123：" + shouldHandleLoadingMore() + "mWholeHeaderView.getPaddingTop()：" + mWholeHeaderView.getPaddingTop());
        //只有在需要处理上拉加载并且头布局已经还原到初始位置
        if (isLoadMoreEnable && pullUpType == PULL_UP_HAND && shouldHandleLoadingMore() && mWholeHeaderView.getPaddingTop() == mMinWholeHeaderViewPaddingTop) {
            //假如提示view这时候还没有隐藏 用户着急上拉加载 那么提前关闭并绘制一次布局 保证内容完整显示
            if (mShowToastView.getHeight() != 0) {
                mShowToastView.hide();
                super.onLayout(changed, l, t, r, b);
            }
            //开始手动布局整体结构
            mWholeHeaderView.layout(0, mMinWholeHeaderViewPaddingTop - mLoadMoreDiffHeight, mWholeHeaderView.getMeasuredWidth(), -mLoadMoreDiffHeight);
            mContentView.layout(0, mWholeHeaderView.getMeasuredHeight() - mLoadMoreDiffHeight, mContentView.getMeasuredWidth(), mWholeHeaderView.getMeasuredHeight() + mContentView.getMeasuredHeight() - mLoadMoreDiffHeight);
            mLoadMoreFooterView.layout(0, mContentView.getMeasuredHeight() - mLoadMoreDiffHeight, mLoadMoreFooterView.getMeasuredWidth(), mLoadMoreFooterView.getMeasuredHeight() + mContentView.getMeasuredHeight() - mLoadMoreDiffHeight);
            return;
        }
        super.onLayout(changed, l, t, r, b);
    }

    /**
     * 处理手指抬起事件
     *
     * @return true表示自己消耗掉该事件，false表示不消耗该事件
     */
    private boolean handleActionUpOrCancel(MotionEvent event) {
        boolean isReturnTrue = false;
        // 如果当前头部刷新控件没有完全隐藏，则需要返回true，自己消耗ACTION_UP事件
        if (mWholeHeaderView.getPaddingTop() != mMinWholeHeaderViewPaddingTop) {
            isReturnTrue = true;
        }

        if (mCurrentRefreshStatus == RefreshStatus.PULL_DOWN || mCurrentRefreshStatus == RefreshStatus.IDLE) {
            // 处于下拉刷新状态，松手时隐藏下拉刷新控件
            if (mWholeHeaderView.getPaddingTop() < 0 && mWholeHeaderView.getPaddingTop() > mMinWholeHeaderViewPaddingTop) {
                hiddenRefreshHeaderView();
            }
            mCurrentRefreshStatus = RefreshStatus.IDLE;
            handleRefreshStatusChanged();
        } else if (mCurrentRefreshStatus == RefreshStatus.RELEASE_REFRESH) {
            // 处于松开进入刷新状态，松手时完全显示下拉刷新控件，进入正在刷新状态
            beginRefreshing();
        }
        if (isLoadMoreEnable && pullUpType == PULL_UP_HAND && (mCurrentLoadMoreStatus == LoadMoreStatus.PULL_UP || mCurrentLoadMoreStatus == LoadMoreStatus.IDLE)) {
            if (mLoadMoreFooterView.getPaddingBottom() > -mLoadMoreFooterViewHeight && mLoadMoreFooterView.getPaddingBottom() < mLoadMoreFooterViewCriticalValue) {
                hiddenLoadMoreFooterView();
            }
        } else if (pullUpType == PULL_UP_HAND && mCurrentLoadMoreStatus == LoadMoreStatus.RELEASE_LOAD) {
            beginLoading();
        }
//		Log.i(TAG,"有bug:"+ mCurrentRefreshStatus +"paddingtop:"+ mWholeHeaderView.getPaddingTop());
//		if (mCurrentRefreshStatus == RefreshStatus.IDLE && mWholeHeaderView.getPaddingTop() != mMinWholeHeaderViewPaddingTop){
//			Log.i(TAG,"有bug");
//			hiddenRefreshHeaderView();
//		}
//		if (mRefreshDownY == -1) {
//			mRefreshDownY = (int) event.getY();
//		}
//		int diffY = (int) event.getY() - mRefreshDownY;
//		if (shouldHandleLoadingMore() && diffY <= 0) {
//			// 处理上拉加载更多，需要返回true，自己消耗ACTION_UP事件
//			isReturnTrue = true;
//			beginLoadingMore();
//		}

        mWholeHeaderDownY = -1;
        mRefreshDownY = -1;
        return isReturnTrue;
    }

    /**
     * 处理下拉刷新控件状态变化
     */
    private void handleRefreshStatusChanged() {
        switch (mCurrentRefreshStatus) {
            case IDLE:
                mRefreshViewHolder.changeToIdle();
                break;
            case PULL_DOWN:
                mRefreshViewHolder.changeToPullDown();
                break;
            case RELEASE_REFRESH:
                mRefreshViewHolder.changeToReleaseRefresh();
                break;
            case REFRESHING:
                mRefreshViewHolder.changeToRefreshing();
                break;
            default:
                break;
        }
    }

    /**
     * 处理上拉加载控件状态变化
     */
    private void handleLoadMoreStatusChanged() {
        switch (mCurrentLoadMoreStatus) {
            case IDLE:
                mRefreshViewHolder.changeToIdleForLoad();
                break;
            case PULL_UP:
                mRefreshViewHolder.changeToPullUp();
                break;
            case RELEASE_LOAD:
                mRefreshViewHolder.changeToReleaseLoad();
                break;
            case LOADING:
                mRefreshViewHolder.changeToLoading();
                break;
            default:
                break;
        }
    }

    /**
     * 切换到正在刷新状态，会调用delegate的onBGARefreshLayoutBeginRefreshing方法
     */
    public void beginRefreshing() {
        if (mCurrentRefreshStatus != RefreshStatus.REFRESHING && mDelegate != null) {
            mCurrentRefreshStatus = RefreshStatus.REFRESHING;
            changeRefreshHeaderViewToZero();
            handleRefreshStatusChanged();
            mDelegate.onRefresh(this);
        }
    }

    /**
     * 切换到正在加载装填
     */
    public void beginLoading() {
        if (mCurrentLoadMoreStatus != LoadMoreStatus.LOADING && mDelegate != null) {
            mCurrentLoadMoreStatus = LoadMoreStatus.LOADING;
            handleLoadMoreStatusChanged();
            if (pullUpType == PULL_UP_HAND) {
                changeLoadMoreFooterViewToZero();
            } else {
                mLoadMoreFooterView.setVisibility(VISIBLE);
                mRefreshViewHolder.changeToLoading();
            }
            mDelegate.onLoad(this);
        }
    }

    /**
     * 结束下拉刷新
     */
    public void endRefreshing(String tip) {
        Log.i(TAG, "mCurrentRefreshStatus:" + mCurrentRefreshStatus);
        if (mCurrentRefreshStatus == RefreshStatus.REFRESHING) {
            mCurrentRefreshStatus = RefreshStatus.IDLE;
            hiddenRefreshHeaderView();
            handleRefreshStatusChanged();
            mRefreshViewHolder.onEndRefreshing();
            handleShowToastView(tip);
        }
    }

    /**
     * 结束上拉加载更多
     */
    public void endLoadingMore() {
        if (mCurrentLoadMoreStatus == LoadMoreStatus.LOADING) {
            // 避免WiFi环境下请求数据太快，加载更多控件一闪而过
            mHandler.postDelayed(mDelayHiddenLoadingMoreViewTask, 0);
        }

    }

    private Runnable mDelayHiddenLoadingMoreViewTask = new Runnable() {
        @Override
        public void run() {
            mCurrentLoadMoreStatus = LoadMoreStatus.IDLE;
            if (pullUpType == PULL_UP_HAND)
                hiddenLoadMoreFooterView();
            else {
                mLoadMoreFooterView.setVisibility(GONE);
            }
            mRefreshViewHolder.onEndLoadingMore();
        }
    };

    /**
     * 显示用于提示的showToastView
     */
    private void handleShowToastView(String tip) {
        if (mShowToastView != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mShowToastView.getLayoutParams();
            //获取当前控件的布局对象
            params.height = 100;//设置当前控件布局的高度
            mShowToastView.setLayoutParams(params);//将设置好的布局参数应用到控件中

            mShowToastView.show(tip);
        }
    }

    /**
     * 设置下拉刷新控件的paddingTop到0，带动画
     */
    private void changeRefreshHeaderViewToZero() {
        ValueAnimator animator = ValueAnimator.ofInt(mWholeHeaderView.getPaddingTop(), 0);
        animator.setDuration(mRefreshViewHolder.getTopAnimDuration());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int paddingTop = (int) animation.getAnimatedValue();
                mWholeHeaderView.setPadding(0, paddingTop, 0, 0);
            }
        });
        animator.start();
    }

    /**
     * 隐藏下拉刷新控件，带动画
     */
    private void hiddenRefreshHeaderView() {
        ValueAnimator animator = ValueAnimator.ofInt(mWholeHeaderView.getPaddingTop(), mMinWholeHeaderViewPaddingTop);
        animator.setDuration(mRefreshViewHolder.getTopAnimDuration());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int paddingTop = (int) animation.getAnimatedValue();
                Log.i(TAG, "hiddenRefreshHeaderView：" + paddingTop);
                mWholeHeaderView.setPadding(0, paddingTop, 0, 0);
            }
        });
        animator.start();
    }

    /**
     * 设置上拉加载控件到刷新高度，带动画
     */
    private void changeLoadMoreFooterViewToZero() {
        ValueAnimator animator = ValueAnimator.ofInt(mLoadMoreFooterViewHeight, mLoadMoreFooterViewCriticalValue);
        animator.setDuration(mRefreshViewHolder.getTopAnimDuration());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLoadMoreDiffHeight = (int) animation.getAnimatedValue();
                Log.i(TAG, "changeLoadMoreFooterViewToZero：" + mLoadMoreDiffHeight);
                requestLayout();
            }
        });
        animator.start();
    }

    /**
     * 隐藏上拉加载控件，带动画
     */
    private void hiddenLoadMoreFooterView() {
        ValueAnimator animator = ValueAnimator.ofInt(mLoadMoreDiffHeight, 0);
        animator.setDuration(mRefreshViewHolder.getTopAnimDuration());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLoadMoreDiffHeight = (int) animation.getAnimatedValue();
                Log.i(TAG, "hiddenLoadMoreFooterView：" + mLoadMoreDiffHeight);
                requestLayout();
            }
        });
        animator.start();
    }

    /**
     * 是否满足处理刷新的条件
     *
     * @return
     */
    private boolean shouldHandleRefresh() {
        if (mCurrentLoadMoreStatus == LoadMoreStatus.LOADING || mCurrentRefreshStatus == RefreshStatus.REFRESHING || mRefreshHeaderView == null || mDelegate == null) {
            return false;
        }

        return isContentViewToTop();
    }

    /**
     * 是否满足处理刷新的条件
     *
     * @return
     */
    private boolean shouldHandleLoadingMore() {
        if (mCurrentRefreshStatus == RefreshStatus.REFRESHING || mLoadMoreFooterView == null || mDelegate == null) {
            return false;
        }

        // 内容是普通控件，满足
        if (mNormalView != null) {
            return true;
        }

        if (BGARefreshScrollingUtil.isWebViewToBottom(mWebView)) {
            return true;
        }

        if (BGARefreshScrollingUtil.isScrollViewToBottom(mScrollView)) {
            return true;
        }

//		if (mAbsListView != null) {
//			return shouldHandleAbsListViewLoadingMore(mAbsListView);
//		}

        if (mRecyclerView != null) {
            Log.i(TAG, "shouldHandleRecyclerViewLoadingMore:" + shouldHandleRecyclerViewLoadingMore(mRecyclerView));
            return shouldHandleRecyclerViewLoadingMore(mRecyclerView);
        }

//		if (mStickyNavLayout != null) {
//			return mStickyNavLayout.shouldHandleLoadingMore();
//		}

        return false;
    }

    public boolean shouldHandleRecyclerViewLoadingMore(RecyclerView recyclerView) {
//		mCurrentLoadMoreStatus == LoadMoreStatus.LOADING ||
        if (mCurrentRefreshStatus == RefreshStatus.REFRESHING || mLoadMoreFooterView == null || mDelegate == null || recyclerView.getAdapter() == null || recyclerView.getAdapter().getItemCount() == 0) {
            Log.i(TAG, "shouldHandleRecyclerViewLoadingMore111:");
            return false;
        }
        Log.i(TAG, "shouldHandleRecyclerViewLoadingMore222:" + BGARefreshScrollingUtil.isRecyclerViewToBottom(recyclerView));
        return BGARefreshScrollingUtil.isRecyclerViewToBottom(recyclerView);
    }

    /**
     * 判断内容是否滚动到最顶端
     *
     * @return
     */
    private boolean isContentViewToTop() {
        // 内容是普通控件，满足
        if (mNormalView != null) {
            return true;
        }

        if (BGARefreshScrollingUtil.isScrollViewOrWebViewToTop(mWebView)) {
            return true;
        }

        if (BGARefreshScrollingUtil.isScrollViewOrWebViewToTop(mScrollView)) {
            return true;
        }

        if (BGARefreshScrollingUtil.isAbsListViewToTop(mAbsListView)) {
            return true;
        }

        if (BGARefreshScrollingUtil.isRecyclerViewToTop(mRecyclerView)) {
            return true;
        }

//		if (BGARefreshScrollingUtil.isStickyNavLayoutToTop(mStickyNavLayout)) {
//			return true;
//		}

        return false;
    }

    /**
     * 设置下拉刷新上拉加载更多代理
     *
     * @param delegate
     */
    public void setDelegate(RefreshAndLoadListener delegate) {
        mDelegate = delegate;
    }

    public interface RefreshAndLoadListener {
        /**
         * 刷新回调
         *
         * @param refreshAndLoadLayoutView
         */
        void onRefresh(RefreshAndLoadLayoutView refreshAndLoadLayoutView);

        /**
         * 加载回调
         *
         * @param refreshAndLoadLayoutView
         */
        void onLoad(RefreshAndLoadLayoutView refreshAndLoadLayoutView);
    }

    public enum RefreshStatus {
        IDLE, PULL_DOWN, RELEASE_REFRESH, REFRESHING
    }

    public enum LoadMoreStatus {
        IDLE, PULL_UP, RELEASE_LOAD, LOADING
    }
}
