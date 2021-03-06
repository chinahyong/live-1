package com.bixin.bixin.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation.CornerType;
import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;

/**
 * 饭圈成员适配器
 */
public class PublishRoomAdapter extends BaseAdapter {

    private Context moContext;
    private List<Map<String, Object>> mlPlayers;

    public PublishRoomAdapter(Context poContext) {
        moContext = poContext;
        mlPlayers = new ArrayList<Map<String, Object>>();
    }

    /**
     * 清除绑定的数据，注意，此方法没有刷新UI
     */
    public void clearData() {
        mlPlayers.clear();
    }

    /**
     * 添加数据，并自动刷新UI
     */
    public void addData(List<Map<String, Object>> data) {
        if (data != null) {
            mlPlayers.addAll(data);
            notifyDataSetChanged();
        }
    }

    /**
     * 添加数据到第一项，并自动刷新
     */
    public void addFirstItem(Map<String, Object> item) {
        if (item != null) {
            mlPlayers.add(0, item);
            notifyDataSetChanged();
        }
    }

    /**
     * @return 获取数据
     */
    public List<Map<String, Object>> getData() {
        return mlPlayers;
    }

    /**
     * @return 数据是否为空
     */
    public boolean isDataEmpty() {
        return mlPlayers.isEmpty();
    }

    @Override
    public int getCount() {
        return mlPlayers.size();
    }

    @Override
    public Object getItem(int position) {
        return mlPlayers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"InflateParams", "NewApi"})
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder loHolder;
        if (convertView == null) {
            LayoutInflater loInflater = LayoutInflater.from(moContext);
            convertView = loInflater.inflate(R.layout.item_publish_room, null);
            loHolder = new Holder();
            loHolder.mMenberLogo = (ImageView) convertView.findViewById(R.id.item_headicon);
            loHolder.mNickName = (TextView) convertView.findViewById(R.id.item_name);
            loHolder.mPublishTime = (TextView) convertView.findViewById(R.id.item_publish_time);
            convertView.setTag(loHolder);
        } else {
            loHolder = (Holder) convertView.getTag();
        }
        @SuppressWarnings("unchecked") Map<String, Object> subjectInfo = (Map<String, Object>) getItem(
            position);
        ImageLoaderUtil.getInstance()
            .loadImageCorner(moContext, loHolder.mMenberLogo, (String) subjectInfo.get("logo"),
                Constants.COMMON_DISPLAY_IMAGE_CORNER_2, CornerType.ALL);
        loHolder.mNickName.setText((String) subjectInfo.get("nickname"));
        loHolder.mPublishTime.setText((String) subjectInfo.get("playForecast"));
        return convertView;
    }

    class Holder {

        ImageView mMenberLogo;
        TextView mNickName, mPublishTime;
    }

}
