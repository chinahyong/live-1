package com.bixin.bixin.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation.CornerType;
import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;
import com.bixin.bixin.listeners.RecyclerViewOnItemClickListener;
import com.bixin.bixin.model.AlbumBean;

public class HorizontalListViewPhotoAdapter extends
    RecyclerView.Adapter<HorizontalListViewPhotoAdapter.ImageHolder> {

    private ArrayList<AlbumBean> mPhotoDatas;
    private Context mContext;
    private LayoutInflater mInflater;
    private RecyclerViewOnItemClickListener onItemClick;

    public HorizontalListViewPhotoAdapter(Context context) {
        this.mContext = context;
        mPhotoDatas = new ArrayList<>();
        mInflater = (LayoutInflater) mContext
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);// LayoutInflater.from(mContext);
    }

    /**
     * 清除绑定的数据，注意，此方法没有刷新UI
     */
    public void clearData() {
        mPhotoDatas.clear();
    }

    /**
     * 添加数据，并自动刷新UI
     */
    public void addData(List<AlbumBean> data) {
        if (data != null) {
            mPhotoDatas.addAll(data);
            notifyDataSetChanged();
        }
    }

    public ArrayList<AlbumBean> getData() {
        return mPhotoDatas;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = mInflater.inflate(R.layout.item_horizontall_list_photo, parent, false);
        ImageHolder holder = new ImageHolder(convertView);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ImageHolder holder, final int position) {
        AlbumBean data = mPhotoDatas.get(position);
        if (data != null) {
            String url = data.getUrl();
            //头像
            if (!TextUtils.isEmpty(url)) {
                if (url.indexOf("://") == -1) {
                    url = Constants.FILE_PXI + url;
                }
                ImageLoaderUtil.getInstance().loadImageCorner(mContext, holder.mPhoto, url,
                    Constants.COMMON_DISPLAY_IMAGE_CORNER_2, CornerType.ALL);
            }
        }
        holder.mPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClick != null) {
                    onItemClick.onItemClick(holder.itemView, position);
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mPhotoDatas.size();
    }

    public RecyclerViewOnItemClickListener getOnItemClick() {
        return onItemClick;
    }

    public void setOnItemClick(RecyclerViewOnItemClickListener onItemClick) {
        this.onItemClick = onItemClick;
    }

    public class ImageHolder extends RecyclerView.ViewHolder {

        private ImageView mPhoto;

        public ImageHolder(View itemView) {
            super(itemView);
            mPhoto = (ImageView) itemView.findViewById(R.id.item_horizontall_list_photo);
        }
    }

}