package com.bixin.bixin.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Map;

import tv.live.bx.R;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;

public class MyFocusAdapter extends MyBaseAdapter<String, Object> {

    public MyFocusAdapter(Context poContext) {
        super(poContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater loInflater = LayoutInflater.from(mContext);
            convertView = loInflater.inflate(R.layout.item_lv_focuser, null);
            Holder loHolder = new Holder();
            loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_lv_focus_photo);
            loHolder.moIvPhotoV = (ImageView) convertView.findViewById(R.id.item_lv_focus_photo_v);
            loHolder.moTvNickname = (TextView) convertView
                .findViewById(R.id.item_lv_focus_nickname);
            loHolder.mUserIntrotion = (TextView) convertView.findViewById(R.id.item_lv_focus_intro);
            loHolder.mIsPlaing = (ImageView) convertView.findViewById(R.id.isPlaying);
            loHolder.mIsPlayingLayout = (RelativeLayout) convertView
                .findViewById(R.id.isPlaying_layout);
            loHolder.mUserLevel = (ImageView) convertView.findViewById(R.id.item_user_level);
            convertView.setTag(loHolder);
        }
        Holder loHolder = (Holder) convertView.getTag();
        @SuppressWarnings("unchecked") Map<String, Object> lmItem = (Map<String, Object>) getItem(
            position);
        ImageLoaderUtil.getInstance()
            .loadHeadPic(mContext, loHolder.moIvPhoto, (String) lmItem.get("headPic"));
        loHolder.moTvNickname.setText((String) lmItem.get("nickname"));
        loHolder.mUserIntrotion.setText((String) lmItem.get("moderatorDesc"));
        if (!TextUtils.isEmpty((String) lmItem.get("moderatorLevel"))) {
            ImageLoaderUtil.getInstance().loadImage(loHolder.mUserLevel, Utils
                .getLevelImageResourceUri(Constants.USER_ANCHOR_LEVEL_PIX,
                    (String) lmItem.get("moderatorLevel")));
        }
        loHolder.mIsPlayingLayout.setVisibility(
            Utils.getBooleanFlag(lmItem.get("isPlaying")) ? View.VISIBLE : View.GONE);
        loHolder.moIvPhotoV
            .setVisibility(Utils.getBooleanFlag(lmItem.get("verified")) ? View.VISIBLE : View.GONE);
        return convertView;
    }

    private class Holder {

        private ImageView moIvPhoto, mUserLevel, mIsPlaing, moIvPhotoV;
        private TextView moTvNickname, mUserIntrotion;
        private RelativeLayout mIsPlayingLayout;
    }

}
