package com.bixin.bixin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation.CornerType;
import com.bixin.bixin.App;
import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;
import com.bixin.bixin.model.AnchorBean;


/**
 * Created by BYC on 2017/5/23.
 */

public class LiveRecommendListAdapter extends BaseAdapter {

    public final static int UNIFORM_SPACE = 7;

    private int mImageHeight;
    private Context moContext;
    private List<AnchorBean> anchors = new ArrayList<>();

    public LiveRecommendListAdapter(Context ctx) {
        moContext = ctx;
        mImageHeight = (App.metrics.widthPixels - Utils.dpToPx(UNIFORM_SPACE) * 3) / 2;
    }

    /**
     * 清除数据
     */
    public void clear() {
        anchors.clear();
        this.notifyDataSetChanged();
    }

    /**
     * 添加额外数据
     */
    public void addItems(List<AnchorBean> adds) {
        anchors.addAll(adds);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return anchors.size();
    }

    @Override
    public Object getItem(int position) {
        return anchors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder loHolder;
        if (convertView == null) {
            LayoutInflater loInflater = LayoutInflater.from(moContext);
            convertView = loInflater.inflate(R.layout.item_live_recommend, null);

            loHolder = new Holder();
            loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_iv_photo);
            loHolder.mOnlineNum = (TextView) convertView.findViewById(R.id.item_tv_online_num);
            loHolder.mAnnouncement = (TextView) convertView.findViewById(R.id.item_tv_announcement);
            loHolder.mLocation = (TextView) convertView.findViewById(R.id.item_tv_location);

            RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, mImageHeight);
            loHolder.moIvPhoto.setLayoutParams(lp2);

            convertView.setTag(loHolder);
        } else {
            loHolder = (Holder) convertView.getTag();
        }

        if (position % 2 == 0) {
            convertView.setPadding(Utils.dpToPx(LiveRecommendListAdapter.UNIFORM_SPACE), 0, 0, 0);
        } else {
            convertView.setPadding(0, 0, Utils.dpToPx(LiveRecommendListAdapter.UNIFORM_SPACE), 0);
        }

        AnchorBean anchor = anchors.get(position);

        ImageLoaderUtil.getInstance().loadImageCorner(moContext, loHolder.moIvPhoto, anchor.headPic,
            Constants.COMMON_DISPLAY_IMAGE_CORNER_2, CornerType.ALL);
        loHolder.mAnnouncement.setText(anchor.announcement);
        loHolder.mOnlineNum.setText(String.valueOf(anchor.onlineNum));
        loHolder.mLocation.setText(anchor.city);

        return convertView;
    }

    private class Holder {

        ImageView moIvPhoto;
        TextView mAnnouncement, mLocation, mOnlineNum;
    }
}
