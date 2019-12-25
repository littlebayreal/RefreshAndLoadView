package com.example.refreshandloadlayoutview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.refreshandloadlayoutview.base.BGANormalRefreshViewHolder;
import com.example.refreshandloadlayoutview.base.RefreshAndLoadLayoutView;
import com.example.refreshandloadlayoutview.base.TipView;

public class MainActivity extends AppCompatActivity {
    private RefreshAndLoadLayoutView mRefreshLoadView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRefreshLoadView = findViewById(R.id.rl);
		BGANormalRefreshViewHolder normalRefreshViewHolder = new BGANormalRefreshViewHolder(this,false);
        mRefreshLoadView.setRefreshViewHolder(normalRefreshViewHolder);
        mRefreshLoadView.setDelegate(new RefreshAndLoadLayoutView.RefreshAndLoadListener() {
			@Override
			public void onRefresh(final RefreshAndLoadLayoutView refreshAndLoadLayoutView) {
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
			public void onLoad(RefreshAndLoadLayoutView refreshAndLoadLayoutView) {

			}
		});
    }
}
