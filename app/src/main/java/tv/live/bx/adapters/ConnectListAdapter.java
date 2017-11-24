package tv.live.bx.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import com.efeizao.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.websocket.model.InviteVideoChat;

import java.util.Iterator;

/**
 * Created by Live on 2017/6/9.
 */

public class ConnectListAdapter extends RecyclerViewBaseAdapter<InviteVideoChat> {
	final int MAX_SIZE = 12;
	final boolean useActivated = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB;

	public ConnectListAdapter(Context context) {
		super(context);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof ConnectListAdapter.ViewHolder) {
			int invitePosition = holder.getAdapterPosition();
			InviteVideoChat inviteVideoChat = getData(invitePosition);
			((ConnectListAdapter.ViewHolder) holder).setData(mContext, inviteVideoChat);
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = mInflater.inflate(R.layout.item_live_connect_list, parent, false);
		ViewHolder holder = new ViewHolder(view);
		return holder;
	}

	public void removeItem(String uid) {
		if (TextUtils.isEmpty(uid))
			return;
		Iterator<InviteVideoChat> iterator = mArrayList.iterator();
		while (iterator.hasNext()) {
			InviteVideoChat data = iterator.next();
			if (uid.equals(data.getUid())) {
				iterator.remove();
				break;
			}
		}
		notifyDataSetChanged();
	}

	public void setSelectItem(int position) {
		for (int i = 0; i < mArrayList.size(); i++) {
			if (i == position) {
				mArrayList.get(i).setSelected(true);
			} else {
				mArrayList.get(i).setSelected(false);
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * 设置数据已读
	 */
	public void setAllRead() {
		Iterator<InviteVideoChat> iterator = mArrayList.iterator();
		while (iterator.hasNext()) {
			InviteVideoChat data = iterator.next();
			data.setRead(true);
		}
	}

	/**
	 * 获取未读邀请数
	 *
	 * @return
	 */
	public int getUnReadNum() {
		int unReadNum = 0;
		Iterator<InviteVideoChat> iterator = mArrayList.iterator();
		while (iterator.hasNext()) {
			InviteVideoChat data = iterator.next();
			if (!data.isRead()) {
				unReadNum++;
			}
		}
		return unReadNum;
	}

	/**
	 * 添加数据到第一项，并自动刷新
	 * 如果超过12条数据，增加一条就删除一条
	 *
	 * @param item
	 */
	@Override
	public void addFirstItem(InviteVideoChat item) {
		if (item != null) {
			mArrayList.add(0, item);
			if (mArrayList.size() > MAX_SIZE) {
				mArrayList.remove(MAX_SIZE);
			}
			notifyDataSetChanged();
		}
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private ImageView mIvInviteType, mIvHeadPic, mIvLevel;
		private TextView mTvNickname;
		private Button mBtnConnect;

		public ViewHolder(View itemView) {
			super(itemView);
			mIvInviteType = (ImageView) itemView.findViewById(R.id.live_connect_type);
			mIvHeadPic = (ImageView) itemView.findViewById(R.id.live_connect_list_headpic);
			mIvLevel = (ImageView) itemView.findViewById(R.id.live_connect_list_level);
			mTvNickname = (TextView) itemView.findViewById(R.id.live_connect_list_name);
			mBtnConnect = (Button) itemView.findViewById(R.id.live_connect_list_connect);
		}

		public void setData(Context context, InviteVideoChat data) {
			ImageLoaderUtil.with().loadImageTransformRoundCircle(context, this.mIvHeadPic, data.getHeadPic());
			this.mTvNickname.setText(data.getNickname(), TextView.BufferType.SPANNABLE);
			ImageLoaderUtil.with().loadImage(context, this.mIvLevel, Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, data.getLevel()));
			if (data.getVideoChatType() == InviteVideoChat.INVITE_CHAT_TYPE_MIC) {
				this.mIvInviteType.setImageResource(R.drawable.invite_type_mic);
			} else {
				this.mIvInviteType.setImageResource(R.drawable.invite_type_video);
			}
			if (data.isSelected()) {
				updateViewStatus(this.itemView, true);
				this.mTvNickname.setVisibility(View.INVISIBLE);
				this.mIvLevel.setVisibility(View.INVISIBLE);
				this.mBtnConnect.setVisibility(View.VISIBLE);
			} else {
				updateViewStatus(this.itemView, false);
				this.mTvNickname.setVisibility(View.VISIBLE);
				this.mIvLevel.setVisibility(View.VISIBLE);
				this.mBtnConnect.setVisibility(View.INVISIBLE);
			}
			if (recyclerViewOnItemClickListener != null) {
				View.OnClickListener listener = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						recyclerViewOnItemClickListener.onItemClick(v, ViewHolder.this.getAdapterPosition());
					}
				};
				this.itemView.setOnClickListener(listener);
				this.mBtnConnect.setOnClickListener(listener);
			}
		}

		private void updateViewStatus(View view, boolean status) {
			if (view instanceof Checkable) {
				((Checkable) view).setChecked(status);
			} else if (useActivated) {
				view.setActivated(status);
			}
		}
	}
}
