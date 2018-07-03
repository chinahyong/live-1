package com.bixin.bixin.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lonzh.lib.LZBaseAdapter;

import java.util.Iterator;
import java.util.Map;

import tv.live.bx.R;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;

public class AudienceAdapter extends LZBaseAdapter<Map<String, String>> {

    public static final int VIEWER_TYPE_ADMIN = 0;
    public static final int VIEWER_TYPE_AUDIENCE = 1;

    private Context moContext;
    private int miType;

    public AudienceAdapter(Context poContext, int piType) {
        moContext = poContext;
        miType = piType;
    }

    /**
     * @param pmData
     */
    public void insertData(Map<String, String> pmData) {
        Iterator<Map<String, String>> iterator = mlData.iterator();
        while (iterator.hasNext()) {
            Map<String, String> item = iterator.next();
            if (item.get("uid").equals(pmData.get("uid"))) {
                iterator.remove();
            }
        }
        // 如果是观众，且有等级，就根据等级排序，否则直接放到最后
        if (miType == VIEWER_TYPE_AUDIENCE && !TextUtils.isEmpty(pmData.get("level"))) {
            int pmLevel = Integer.parseInt(pmData.get("level"));
            int position = 0;
            for (int i = 0; i < mlData.size(); i++) {
                if (!TextUtils.isEmpty(mlData.get(i).get("level"))) {
                    if (pmLevel >= Integer.parseInt(mlData.get(i).get("level"))) {
                        position = i;
                        break;
                    }
                } else {
                    position = i;
                    break;
                }
            }
            mlData.add(position, pmData);
        } else {
            mlData.add(pmData);
        }
        notifyDataSetChanged();
    }

    /**
     * 匿名用户 插入数据
     */
    @Override
    public void insertData(int piIndex, Map<String, String> pmData) {
        Iterator<Map<String, String>> iterator = mlData.iterator();
        while (iterator.hasNext()) {
            Map<String, String> item = iterator.next();
            if (item.get("cid").equals(pmData.get("cid"))) {
                iterator.remove();
            }
        }
        mlData.add(pmData);
        notifyDataSetChanged();
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater loInflater = LayoutInflater.from(moContext);
            convertView = loInflater.inflate(R.layout.item_fm_viewers_administrator, null);
            Holder loHolder = new Holder();
            loHolder.moIvPhoto = (ImageView) convertView
                .findViewById(R.id.item_fm_rank_field_photo);
            loHolder.mUserLevel = (ImageView) convertView.findViewById(R.id.item_user_level);
            loHolder.moTvNickname = (TextView) convertView
                .findViewById(R.id.item_fm_rank_field_tv_nickname);
            convertView.setTag(loHolder);
        }
        @SuppressWarnings("unchecked") Map<String, String> lmItem = (Map<String, String>) getItem(
            position);
        Holder loHolder = (Holder) convertView.getTag();
        String lsNickname = lmItem.get("nickname");
        String lsPhoto = lmItem.get("photo");
        String lstype = lmItem.get("type");
        if (lsNickname != null) {
            loHolder.moTvNickname.setText(lsNickname);
        }

        ImageLoaderUtil.getInstance()
            .loadImage(loHolder.mUserLevel, Utils.getLevelImageResourceUri(lmItem, true));
        loHolder.moIvPhoto.setImageResource(R.drawable.bg_user_default);

        if (!lstype.equals("-1") && lsPhoto != null) {
            ImageLoaderUtil.getInstance().loadHeadPic(moContext, loHolder.moIvPhoto, lsPhoto);
        }

        return convertView;
    }

    private class Holder {

        private ImageView moIvPhoto, mUserLevel;
        private TextView moTvNickname;
    }
}
