package com.example.refreshandloadlayoutview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;


import com.example.easyrefreshloadview.base.BGANormalEasyRefreshViewHolder;
import com.example.easyrefreshloadview.base.EasyRefreshLoadView;
import com.example.easyrefreshloadview.base.EasyRefreshLoadViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
	private EasyRefreshLoadView mRefreshLoadView;
	private RecyclerView mRv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRefreshLoadView = findViewById(R.id.rl);
		mRv = findViewById(R.id.rv);
		mRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
		List<String> list = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			String s = "黑人问号脸" + i;
			list.add(s);
		}
		MainAdapter mainAdapter = new MainAdapter(this, list);
		mRv.setAdapter(mainAdapter);

		mainAdapter.setmOnItemListener(new MainAdapter.onItemListener() {
			@Override
			public void onItemClick(View item, int position) {
				Log.i("MainActivity", "item:" + position);
			}
		});
//
		EasyRefreshLoadViewHolder normalRefreshViewHolder = new BGANormalEasyRefreshViewHolder(this, true,EasyRefreshLoadView.PULL_UP_HAND);
		mRefreshLoadView.setRefreshViewHolder(normalRefreshViewHolder);
		mRefreshLoadView.setDelegate(new EasyRefreshLoadView.RefreshAndLoadListener() {
			@Override
			public void onRefresh(final EasyRefreshLoadView refreshAndLoadLayoutView) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(2000);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									refreshAndLoadLayoutView.endRefreshing("刷新成功");
								}
							});
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();


			}

			@Override
			public void onLoad(final EasyRefreshLoadView refreshAndLoadLayoutView) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(2000);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Log.v("MainActivity","结束加载");
									refreshAndLoadLayoutView.endLoadingMore();
								}
							});
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});
	}
}
