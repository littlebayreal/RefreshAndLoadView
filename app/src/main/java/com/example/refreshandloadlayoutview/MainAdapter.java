package com.example.refreshandloadlayoutview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainHolder> {
	private Context mContext;
	private List<String> mList;
	private onItemListener mOnItemListener;

	public MainAdapter(Context context, List<String> list) {
		this.mContext = context;
		this.mList = list;
	}

	@NonNull
	@Override
	public MainHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
		Log.i("MainAdapter", "i:" + i);
		View item = LayoutInflater.from(mContext).inflate(R.layout.item_main, viewGroup, false);
		return new MainHolder(item);
	}

	@Override
	public void onBindViewHolder(@NonNull final MainHolder mainHolder, final int i) {
		mainHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mOnItemListener != null)
					mOnItemListener.onItemClick(mainHolder.itemView, i);
			}
		});
		((TextView) mainHolder.itemView.findViewById(R.id.tv)).setText(mList.get(i));
	}

	@Override
	public int getItemCount() {
		return mList.size();
	}

	public class MainHolder extends RecyclerView.ViewHolder {
		public View itemView;

		public MainHolder(@NonNull View itemView) {
			super(itemView);
			this.itemView = itemView;
		}
	}

	public void setmOnItemListener(onItemListener mOnItemListener) {
		this.mOnItemListener = mOnItemListener;
	}

	public interface onItemListener {
		void onItemClick(View item, int position);
	}
}
