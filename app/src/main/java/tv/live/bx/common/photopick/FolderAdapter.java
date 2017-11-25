package tv.live.bx.common.photopick;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tv.live.bx.R;
import tv.live.bx.imageloader.ImageLoaderUtil;

/**
 * Created by chenchao on 15/5/6.
 */
public class FolderAdapter extends BaseAdapter {

	private String mSelect = "";
	ArrayList<ImageInfoExtra> mFolderData = new ArrayList<ImageInfoExtra>();

	public FolderAdapter(ArrayList<ImageInfoExtra> mFolderData) {
		this.mFolderData = mFolderData;
	}

	public String getSelect() {
		return mSelect;
	}

	public void setSelect(int pos) {
		if (pos >= getCount()) {
			return;
		}

		mSelect = mFolderData.get(pos).getmName();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mFolderData.size();
	}

	@Override
	public Object getItem(int position) {
		return mFolderData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.photopick_list_item, parent, false);
			holder = new ViewHolder();
			holder.foldIcon = (ImageView) convertView.findViewById(R.id.foldIcon);
			holder.foldName = (TextView) convertView.findViewById(R.id.foldName);
			holder.photoCount = (TextView) convertView.findViewById(R.id.photoCount);
			holder.check = convertView.findViewById(R.id.check);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ImageInfoExtra data = mFolderData.get(position);
		String uri = data.getPath();
		int count = data.getCount();

		holder.foldName.setText(data.getmName());
		holder.photoCount.setText(String.format("%d张", count));

		ImageLoaderUtil.with().loadImage(parent.getContext(), holder.foldIcon, ImageInfo.pathAddPreFix(uri), 0, R.drawable.image_not_exist);

		if (data.getmName().equals(mSelect)) {
			holder.check.setVisibility(View.VISIBLE);
		} else {
			holder.check.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	static class ViewHolder {
		ImageView foldIcon;
		TextView foldName;
		TextView photoCount;
		View check;
	}
}
