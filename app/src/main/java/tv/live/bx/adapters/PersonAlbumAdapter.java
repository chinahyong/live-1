package tv.live.bx.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import tv.live.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.model.AlbumBean;

/**
 * 礼物GridView的适配器
 */
public class PersonAlbumAdapter extends BaseAdapter {
	//礼物类型
	private Context mContext;
	private AlbumGridClickListener lisGridItemOnClick;

	private List<AlbumBean> giftsData = new ArrayList<>();

	private void onSetIGiftGridItemOnClick(AlbumGridClickListener lisGridItemOnClick) {
		this.lisGridItemOnClick = lisGridItemOnClick;
	}

	/**
	 * 构造方法
	 *
	 * @param context 上下文
	 */
	public PersonAlbumAdapter(Context context, AlbumGridClickListener lisGridItemOnClick) {
		mContext = context;
		this.lisGridItemOnClick = lisGridItemOnClick;
	}

	public void updateData(List<AlbumBean> data) {
		giftsData = data;
		notifyDataSetChanged();
	}

	public int getCount() {
		// TODO Auto-generated method stub
		return giftsData.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return giftsData.get(position);
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(final int position, View convertView, final ViewGroup parent) {
		Holder loHolder;
		final AlbumBean data = giftsData.get(position);
		if (convertView == null) {
			loHolder = new Holder();

			LayoutInflater loInflater = LayoutInflater.from(mContext);
			convertView = loInflater.inflate(R.layout.item_edit_album, parent, false);

			WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
			int width = wm.getDefaultDisplay().getWidth();
			ViewGroup.LayoutParams layoutParams = convertView.getLayoutParams();
			layoutParams.height = (width - 5 * Utils.dip2px(mContext, 5)) / 4;
			layoutParams.width = layoutParams.height;
			convertView.setLayoutParams(layoutParams);
			loHolder.giftPhoto = (ImageView) convertView.findViewById(R.id.item_edit_album_photo);
			convertView.setTag(loHolder);
		} else {
			loHolder = (Holder) convertView.getTag();
		}
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (lisGridItemOnClick != null) {
					lisGridItemOnClick.onClick(parent, v, position, data);
				}
			}
		});
		String url = data.getUrl();
		if (!TextUtils.isEmpty(url)) {
			if (url.indexOf("://") == -1) {
				url = Constants.FILE_PXI + url;
			}
			ImageLoaderUtil.with().loadImageTransformRoundedCorners(mContext, loHolder.giftPhoto, url, Constants.COMMON_DISPLAY_IMAGE_CORNER_1);
		}
		return convertView;
	}

	/**
	 * 用于点击礼物项的回调事件
	 */
	public interface AlbumGridClickListener {
		void onClick(ViewGroup parent, View v, int position, AlbumBean data);
	}

	class Holder {
		ImageView giftPhoto;
	}
}
