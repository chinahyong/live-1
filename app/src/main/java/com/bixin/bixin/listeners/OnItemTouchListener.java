package com.bixin.bixin.listeners;

import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Live on 2017/4/24.
 * Description:RecyclerView的点击、长按事件
 */

public class OnItemTouchListener implements RecyclerView.OnItemTouchListener {
	private GestureDetector mGestureDetector;
	private RecyclerView mRecyclerView;

	public OnItemTouchListener(RecyclerView recyclerView) {
		this.mRecyclerView = recyclerView;
		mGestureDetector = new GestureDetector(recyclerView.getContext(), new OnGestureListener());
	}

	@Override
	public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
		mGestureDetector.onTouchEvent(e);
		return false;
	}

	@Override
	public void onTouchEvent(RecyclerView rv, MotionEvent e) {
		mGestureDetector.onTouchEvent(e);
	}

	@Override
	public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

	}

	private class OnGestureListener implements GestureDetector.OnGestureListener {


		@Override
		public boolean onDown(MotionEvent motionEvent) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent motionEvent) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent motionEvent) {
			// 点击   findChildViewUnder：通过手机按下坐标来获取该位置是否在某个item对应的范围内，从而得到view，主要使用ChildHelper
			View childView = mRecyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
			if (childView != null) {
				// getChildViewHolder 用于获取childView对应的ViewHolder对象
				RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(childView);
				onItemClick(holder);
			}
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent motionEvent) {
			//长按
			View childView = mRecyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
			if (childView != null) {
				RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(childView);
				onLongClick(holder);
			}
		}

		@Override
		public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
			return false;
		}
	}

	public void onLongClick(RecyclerView.ViewHolder vh) {
	}

	public void onItemClick(RecyclerView.ViewHolder vh) {
	}
}
