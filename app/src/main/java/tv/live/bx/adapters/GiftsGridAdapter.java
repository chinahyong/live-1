package tv.live.bx.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.live.bx.R;
import tv.live.bx.common.Utils;
import tv.live.bx.imageloader.ImageLoaderUtil;

/**
 * 礼物GridView的适配器
 */
public class GiftsGridAdapter extends BaseAdapter {

    //礼物类型
    public static final String GIFT_TYPE_BONUS = "3"; // 暴击礼物 类型
    public static final String GIFT_TYPE_BANDS = "2"; // 打榜礼物 类型
    public static final int GIFT_PORTART_PAGE_SIZE = 8;// 每一页装载数据的大小
    public static final int GIFT_LANDSCAPE_PAGE_SIZE = 7;
    private Context mContext;
    private static String PRICE_0 = "免费";
    private static String PRICE_UNIT = " 克拉";
    private static String FREE_PRICE_UNIT = " 点点";
    private String unit = PRICE_UNIT;
    private static final int miTotalSecs = 60;
    private IGiftGridItemOnClick lisGridItemOnClick;

    private List<Map<String, String>> giftsData = new ArrayList<>();

    private void onSetIGiftGridItemOnClick(IGiftGridItemOnClick lisGridItemOnClick) {
        this.lisGridItemOnClick = lisGridItemOnClick;
    }

    /**
     * 构造方法
     * @param context 上下文
     */
    public GiftsGridAdapter(Context context, IGiftGridItemOnClick lisGridItemOnClick) {
        mContext = context;
        this.lisGridItemOnClick = lisGridItemOnClick;
        //		// 如果是“免费”礼物
        //		if ("1".equals(giftsData.get(0).get("type"))) {
        //			this.unit = FREE_PRICE_UNIT;
        //		}
    }

    public void updateData(List<Map<String, String>> data) {
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
        @SuppressWarnings("unchecked")        final Map<String, String> giftInfo = giftsData
            .get(position);
        if (convertView == null) {
            LayoutInflater loInflater = LayoutInflater.from(mContext);
            convertView = loInflater.inflate(R.layout.gift_grid_item, null);

            loHolder = new Holder();

            //			loHolder.giftNum = (TextView) convertView.findViewById(R.id.gifts_bottom_info_tv_rose_count);
            loHolder.giftTypeBg = (ImageView) convertView.findViewById(R.id.gifts_tyte);
            loHolder.giftPhoto = (ImageView) convertView.findViewById(R.id.gifts_bottom_info_img);
            loHolder.giftName = (TextView) convertView.findViewById(R.id.gifts_bottom_info_name);
            loHolder.giftPrice = (TextView) convertView.findViewById(R.id.gifts_bottom_info_price);
            //			loHolder.mGiftBonusBg = (ImageView) convertView.findViewById(R.id.gifts_bottom_info_img_is_bonus_bg);
            //			loHolder.mGiftCharts = (ImageView) convertView.findViewById(R.id.gifts_bottom_info_img_is_band_bg);

            convertView.setTag(loHolder);
        } else {
            loHolder = (Holder) convertView.getTag();
        }

        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (lisGridItemOnClick != null) {
                    lisGridItemOnClick.onClick(parent, v, position, giftInfo);
                }
            }
        });
        String price = giftInfo.get("price") + this.unit;
        if (TextUtils.isEmpty(giftInfo.get("cornerMark"))) {
            loHolder.giftTypeBg.setImageBitmap(null);
        } else {
            ImageLoaderUtil.getInstance()
                .loadImage(loHolder.giftTypeBg, giftInfo.get("cornerMark"));
        }

        //如果不是空格子
        if (!TextUtils.isEmpty(giftInfo.get("name"))) {
            loHolder.giftPhoto.setVisibility(View.VISIBLE);
            loHolder.giftName.setVisibility(View.VISIBLE);
            loHolder.giftPrice.setVisibility(View.VISIBLE);
            //如果是背包物品，否则就是礼物
            if (!TextUtils.isEmpty(giftInfo.get("pkgItemsetId"))) {
                loHolder.giftName.setText(giftInfo.get("name") + " x" + giftInfo.get("num"));
            } else {
                loHolder.giftName.setText(giftInfo.get("name"));
            }
            ImageLoaderUtil.getInstance().loadImage(loHolder.giftPhoto, giftInfo.get("imgPreview"));
            loHolder.giftPrice.setText(price);

        } else {
            loHolder.giftPhoto.setVisibility(View.INVISIBLE);
            loHolder.giftName.setVisibility(View.INVISIBLE);
            loHolder.giftPrice.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    private void setFlowerProgress(float pfPercent, ImageView moVProgress) {
        Bitmap loBmp = Bitmap
            .createBitmap(Utils.dip2px(mContext, 60), Utils.dip2px(mContext, 60), Config.ARGB_8888);
        Canvas loCvs = new Canvas(loBmp);
        Paint loPt = new Paint();
        loCvs.drawColor(Color.TRANSPARENT);
        int liCenterX = loBmp.getWidth() / 2;
        int liCenterY = loBmp.getHeight() / 2;
        int liRadius = loBmp.getWidth() / 2 - 3;

        // 画外层的圆
        loPt.setColor(mContext.getResources().getColor(R.color.light_blue));
        loPt.setStyle(Paint.Style.STROKE);
        loPt.setStrokeWidth(6);
        loPt.setAntiAlias(true);
        loCvs.drawCircle(liCenterX, liCenterY, liRadius, loPt);

        // 画进度
        loPt.setColor(mContext.getResources().getColor(R.color.a_bg_color_ffa200));
        RectF loRf = new RectF(0 + 3, 0 + 3, loBmp.getWidth() - 3, loBmp.getHeight() - 3);
        loCvs.drawArc(loRf, 45, 360 * pfPercent / 100, false, loPt);
        moVProgress.setImageBitmap(loBmp);
    }

    /**
     * 用于点击礼物项的回调事件
     */
    public interface IGiftGridItemOnClick {

        void onClick(ViewGroup parent, View v, int position, Map<String, String> giftInfo);
    }

    class Holder {

        ImageView giftPhoto, giftTypeBg;
        TextView giftName, giftPrice;
    }
}
