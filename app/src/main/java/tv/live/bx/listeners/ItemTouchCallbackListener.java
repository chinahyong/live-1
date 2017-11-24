package tv.live.bx.listeners;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by Live on 2017/4/24.
 * Description：RecyclerView的拖动的TouchHelper实现
 */

public class ItemTouchCallbackListener extends ItemTouchHelper.Callback {
	private ItemTouchAdapter mItemTouchAdapter;

	private ItemDragListener mItemDragListener;
	private Drawable mBackground = null;
	private int mBkcolor = -1;

	public ItemTouchCallbackListener(ItemTouchAdapter itemTouchAdapter) {
		mItemTouchAdapter = itemTouchAdapter;
	}

	@Override
	public boolean isLongPressDragEnabled() {
		return false;
	}

	@Override
	public boolean isItemViewSwipeEnabled() {
		return true;
	}

	@Override
	public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
			//支持上下左右拖动
			final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
			// 不支持左右滑动
			final int swipeFlags = 0;
			return makeMovementFlags(dragFlags, swipeFlags);
		} else {
			//支持上下拖动
			final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
			// 不支持左右滑动
			final int swipeFlags = 0;
			return makeMovementFlags(dragFlags, swipeFlags);
		}
	}

	// 监听移动
	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
		int oldPosition = viewHolder.getAdapterPosition();        //旧的位置
		int newPosition = target.getAdapterPosition();        //新的位置
		if (mItemTouchAdapter != null) {
			mItemTouchAdapter.onMove(oldPosition, newPosition);
		}
		return true;
	}

	// 监听滑动
	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
		int position = viewHolder.getAdapterPosition();
		if (mItemTouchAdapter != null) {
			mItemTouchAdapter.onSwiped(position);
		}
	}

	// 移动Item时的重绘
	@Override
	public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
		// 当前正在被拖动
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
			// 修改透明度
			float alpha = 1 - Math.abs(dX) / viewHolder.itemView.getWidth();
			viewHolder.itemView.setAlpha(alpha);
			viewHolder.itemView.setTranslationX(dX);
		} else {
			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
		}
	}

	/**
	 * View 被选中时
	 *
	 * @param viewHolder
	 * @param actionState
	 */
	@Override
	public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
		// 当前选中，状态未闲置
		if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
			if (mBackground == null && mBkcolor == -1) {
				Drawable drawable = viewHolder.itemView.getBackground();
				if (drawable == null) {
					mBkcolor = 0;
				} else {
					mBackground = drawable;
				}
			}
			viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
		}
		super.onSelectedChanged(viewHolder, actionState);
	}

	@Override
	public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		super.clearView(recyclerView, viewHolder);
		// 拖动结束  还原透明度
		viewHolder.itemView.setAlpha(1.0f);
		if (mBackground != null) viewHolder.itemView.setBackgroundDrawable(mBackground);
		if (mBkcolor != -1) viewHolder.itemView.setBackgroundColor(mBkcolor);
		// 拖动结束 回调通知
		if (mItemDragListener != null) {
			mItemDragListener.onDragFinish();
		}
	}

	public void setItemTouchAdapter(ItemTouchAdapter itemTouchAdapter) {
		this.mItemTouchAdapter = itemTouchAdapter;
	}

	public void setItemDragListener(ItemDragListener itemDragListener) {
		this.mItemDragListener = itemDragListener;
	}

	public interface ItemTouchAdapter {
		void onMove(int fromPosition, int toPosition);

		void onSwiped(int position);
	}

	public interface ItemDragListener {
		void onDragFinish();
	}
}
