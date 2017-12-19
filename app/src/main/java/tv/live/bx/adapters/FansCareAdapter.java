package tv.live.bx.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.live.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Consts;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;

/**
 * Created by valar on 2017/3/23.
 * detail 关注我的 适配器
 */

public class FansCareAdapter extends MyBaseAdapter {

    List<Map<String, Object>> mFansCareListDate;

    public FansCareAdapter(Context poContext) {
        super(poContext);
        mFansCareListDate = new ArrayList<>();
    }

    @Override
    public Object getItem(int position) {
        return mFansCareListDate.get(position);
    }

    @Override
    public int getCount() {
        return mFansCareListDate.size();
    }

    @Override
    public void clearData() {
        mFansCareListDate.clear();
    }

    @Override
    public void addData(List data) {
        if (data != null) {
            mFansCareListDate.addAll(data);
            notifyDataSetChanged();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            LayoutInflater loInflater = LayoutInflater.from(mContext);
            convertView = loInflater.inflate(R.layout.item_fans_care_list, null);
            holder = new Holder();
            holder.mFansCareCard = convertView.findViewById(R.id.fans_care_ticket);
            holder.mFansCareHead = convertView.findViewById(R.id.fans_care_headpic);
            holder.mFansCareName = convertView.findViewById(R.id.fans_care_name);
            holder.mFansCareLevel = convertView.findViewById(R.id.fans_care_level);
            holder.mFansCareSex = convertView.findViewById(R.id.fans_care_sex);
            holder.mFansCareState = convertView.findViewById(R.id.fans_care_state);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        Map<String, Object> fansCareListInfo = mFansCareListDate.get(position);
        String fansName = (String) fansCareListInfo.get("nickname");
        String fansLevel = (String) fansCareListInfo.get("level");
        String fansSex = (String) fansCareListInfo.get("sex");
        String fansheadPic = (String) fansCareListInfo.get("headPic");
        String fansState = (String) fansCareListInfo.get("lastState");
        String fansIsUseCard = (String) fansCareListInfo.get("messageCardAvailable");
        holder.mFansCareName.setText(fansName);
        if (!TextUtils.isEmpty(fansLevel)) {
            ImageLoaderUtil.getInstance().loadImage(holder.mFansCareLevel,
                Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, fansLevel));
        }
        if (!TextUtils.isEmpty(fansSex)) {
            if (Integer.parseInt(fansSex) == Consts.GENDER_MALE) {
                holder.mFansCareSex.setImageResource(R.drawable.icon_my_info_man);
            } else {
                holder.mFansCareSex.setImageResource(R.drawable.icon_my_info_feman);
            }
        }
        ImageLoaderUtil.getInstance().loadHeadPic(mContext, holder.mFansCareHead, fansheadPic);
        if ("1".equals(fansState)) {
            holder.mFansCareState.setText("今日已发");
            holder.mFansCareState.setVisibility(View.VISIBLE);
        } else {
            holder.mFansCareState.setVisibility(View.GONE);
        }
        if ("1".equals(fansIsUseCard)) {
            holder.mFansCareCard.setVisibility(View.VISIBLE);
        } else {
            holder.mFansCareCard.setVisibility(View.GONE);
        }
        return convertView;
    }

    public interface OnItemClickListener {

        void onClick(View view, int position);
    }

    class Holder {

        ImageView mFansCareHead, mFansCareCard, mFansCareSex, mFansCareLevel;
        TextView mFansCareName, mFansCareState;
    }

}
