package tv.live.bx.adapters;

import android.annotation.SuppressLint;
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
import java.util.Map;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation.CornerType;
import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.library.util.EvtLog;

/**
 * title:最新主播列表适配器 ClassName: LiveNewListAdapter <br/>
 * @author Live
 * @version 2.4.0 2016.4.26
 */
public class LiveNearListAdapter extends BaseAdapter {

    public final static int Uniform_Space = 7;
    private Context moContext;
    private List<Map<String, Object>> mlPlayers;
    private int mImageHeight;

    public LiveNearListAdapter(Context ctx) {
        moContext = ctx;
        mImageHeight = (FeizaoApp.metrics.widthPixels - Utils.dpToPx(Uniform_Space) * 3) / 2;
        mlPlayers = new ArrayList<>();
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
        EvtLog.e("", "getView " + position);
        final Holder loHolder;
        if (convertView == null) {
            LayoutInflater loInflater = LayoutInflater.from(moContext);
            convertView = loInflater.inflate(R.layout.item_live_near, null);

            loHolder = new Holder();
            loHolder.moIvPhoto = (ImageView) convertView.findViewById(R.id.item_live_new_photo);
            loHolder.moIvStatus = (ImageView) convertView.findViewById(R.id.item_live_new_status);
            loHolder.mNickName = (TextView) convertView.findViewById(R.id.item_live_nickname);
            loHolder.mNearDistance = (TextView) convertView
                .findViewById(R.id.item_live_near_distance);

            RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) loHolder.moIvPhoto
                .getLayoutParams();
            lp2.height = mImageHeight;
            loHolder.moIvPhoto.setLayoutParams(lp2);

            convertView.setTag(loHolder);
        } else {
            loHolder = (Holder) convertView.getTag();
        }

        @SuppressWarnings("unchecked") Map<String, Object> subjectInfo = (Map<String, Object>) getItem(
            position);

        ImageLoaderUtil.getInstance()
            .loadImageCorner(moContext, loHolder.moIvPhoto, (String) subjectInfo.get("headPic"),
                Constants.COMMON_DISPLAY_IMAGE_CORNER_2, CornerType.ALL);
        loHolder.mNickName.setText((String) subjectInfo.get("nickname"));
        loHolder.mNearDistance.setText(subjectInfo.get("distance") + "km");
        return convertView;
    }

    class Holder {

        ImageView moIvPhoto, moIvStatus;
        TextView mNickName, mNearDistance;
    }

}
