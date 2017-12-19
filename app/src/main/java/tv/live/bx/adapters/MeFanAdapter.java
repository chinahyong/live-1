package tv.live.bx.adapters;

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
import tv.live.bx.adapters.FanExpandableAdapter.OnItemClickListener;
import tv.live.bx.common.Constants;
import tv.live.bx.imageloader.ImageLoaderUtil;

/**
 * 推荐饭圈
 */
public class MeFanAdapter extends BaseAdapter {

    private Context moContext;
    private List<Map<String, Object>> mlPlayers;

    private OnItemClickListener mOnItemClickListener;

    public MeFanAdapter(Context poContext) {
        moContext = poContext;
        mlPlayers = new ArrayList<Map<String, Object>>();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
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
            convertView = loInflater.inflate(R.layout.item_me_fan_list, null);
            loHolder = new Holder();
            loHolder.mFanLogo = (ImageView) convertView.findViewById(R.id.item_fanquan_logo);
            loHolder.mFanName = (TextView) convertView.findViewById(R.id.item_fanquan_name);
            loHolder.mPeopleNum = (TextView) convertView.findViewById(R.id.item_fanquan_people);
            loHolder.mFanAdd = (ImageView) convertView.findViewById(R.id.item_fanquan_add);
            convertView.setTag(loHolder);
        } else {
            loHolder = (Holder) convertView.getTag();
        }
        @SuppressWarnings("unchecked") Map<String, Object> subjectInfo = (Map<String, Object>) getItem(
            position);

        ImageLoaderUtil.getInstance()
            .loadImageCorner(moContext, loHolder.mFanLogo, subjectInfo.get("logo"),
                Constants.COMMON_DISPLAY_IMAGE_CORNER_2, CornerType.ALL);

        loHolder.mFanName.setText((String) subjectInfo.get("name"));
        // loHolder.mFanAdd.setOnClickListener(new OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // if (mOnItemClickListener != null) {
        // mOnItemClickListener.onClick(v,
        // FanExpandableAdapter.CATEGORY_REMEMENT, mGroupPosition, position);
        // }
        // }
        // });
        // 显示标题
        loHolder.mPeopleNum.setText(subjectInfo.get("memberTotal") + "人");
        // if ("0".equals(subjectInfo.get("joined"))) {
        // loHolder.mFanAdd.setImageResource(R.drawable.btn_add_nor);
        // } else {
        // loHolder.mFanAdd.setImageResource(R.drawable.btn_add_nor);
        // }

        return convertView;
    }

    class Holder {

        ImageView mFanLogo;
        TextView mPeopleNum;
        TextView mFanName;
        ImageView mFanAdd;
    }

}
