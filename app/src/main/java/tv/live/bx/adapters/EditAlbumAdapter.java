package tv.live.bx.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation.CornerType;
import tv.live.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.EvtLog;
import tv.live.bx.listeners.ItemTouchCallbackListener;
import tv.live.bx.model.AlbumBean;

/**
 * Created by Live on 2017/4/25.
 */

public class EditAlbumAdapter extends RecyclerView.Adapter<EditAlbumAdapter.ImageHolder> implements
    ItemTouchCallbackListener.ItemTouchAdapter {

    private ArrayList<AlbumBean> mPhotoBeans;
    private Context mContext;
    private LayoutInflater mInflater;
    private int mHeight;

    public EditAlbumAdapter(Context context) {
        super();
        this.mPhotoBeans = new ArrayList<>();
        this.mContext = context.getApplicationContext();
        this.mInflater = LayoutInflater.from(mContext);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        mHeight = (width - 5 * Utils.dip2px(mContext, 5)) / 4;
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        EvtLog.e("EditAlbumAdapter", "onCreateViewHolder:" + viewType);
        View conventView = mInflater.inflate(R.layout.item_edit_album, parent, false);
        ImageHolder holder = new ImageHolder(conventView);
        return holder;
    }

    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        holder.mTvStatus.setVisibility(View.GONE);
        if (position == 0) {
            holder.mIvPhoto.setImageResource(R.drawable.ic_my_photo_add_nor);
            return;
        }
        String url = mPhotoBeans.get(position - 1).getUrl();
        if (mPhotoBeans.get(position - 1).getStatus() == 1) {
            // 审核中
            holder.mTvStatus.setVisibility(View.VISIBLE);
            holder.mTvStatus.setText(R.string.edit_album_checking);
        } else if (mPhotoBeans.get(position - 1).getStatus() == -1) {
            // 添加图片失败，给一个状态
            holder.mTvStatus.setVisibility(View.VISIBLE);
            holder.mTvStatus.setText(R.string.edit_album_failed);
        }
        //图片列表
        if (!TextUtils.isEmpty(url)) {
            if (url.indexOf("://") == -1) {
                url = Constants.FILE_PXI + url;
            }
            ImageLoaderUtil.getInstance().loadImageCorner(mContext, holder.mIvPhoto, url,
                Constants.COMMON_DISPLAY_IMAGE_CORNER_1, CornerType.ALL);
        } else {
            String path = mPhotoBeans.get(position - 1).getPath();
            //图片列表
            if (!TextUtils.isEmpty(path)) {
                if (path.indexOf("://") == -1) {
                    path = Constants.FILE_PXI + path;
                }
                ImageLoaderUtil.getInstance().loadImageCorner(mContext, holder.mIvPhoto, path,
                    Constants.COMMON_DISPLAY_IMAGE_CORNER_1, CornerType.ALL);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mPhotoBeans.size() + 1;
    }

    public void addDatas(List<AlbumBean> photoBeens) {
        if (photoBeens != null) {
            mPhotoBeans.addAll(photoBeens);
            notifyDataSetChanged();
        }
    }

    public void addData(AlbumBean bean) {
        if (mPhotoBeans != null) {
            mPhotoBeans.add(bean);
        }
    }

    public ArrayList<AlbumBean> getData() {
        return mPhotoBeans;
    }

    public void clearData() {
        mPhotoBeans.clear();
    }

    public List<Integer> getIds() {
        if (mPhotoBeans.isEmpty()) {
            return null;
        }
        List<Integer> ids = new ArrayList<>();
        for (AlbumBean bean : mPhotoBeans) {
            ids.add(bean.getId());
        }
        return ids;
    }

    @Override
    public void onMove(int fromPosition, int toPosition) {
        EvtLog.e("EditAlbumAdapter",
            "onMove:fromPosition:" + fromPosition + "  toPosition:" + toPosition);
        // 当位置为0时，禁止移动
        if (fromPosition == 0 || toPosition == 0) {
            return;
        }
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mPhotoBeans, i - 1, i);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mPhotoBeans, i - 1, i - 2);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onSwiped(int position) {
        EvtLog.e("EditAlbumAdapter", "onSwiped:position:" + position);
        if (position == 0) {
            return;
        }
        mPhotoBeans.remove(position);
        notifyItemRemoved(position);
    }

    public class ImageHolder extends RecyclerView.ViewHolder {

        private ImageView mIvPhoto;
        private TextView mTvStatus;        //0：通过审核   1：审核中   -1：违规（前端自己将添加失败作为违规）

        public ImageHolder(final View itemView) {
            super(itemView);
            ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
            layoutParams.height = mHeight;
            layoutParams.width = mHeight;
            itemView.setLayoutParams(layoutParams);
            mIvPhoto = (ImageView) itemView.findViewById(R.id.item_edit_album_photo);
            mTvStatus = (TextView) itemView.findViewById(R.id.item_edit_album_status);
        }
    }
}
