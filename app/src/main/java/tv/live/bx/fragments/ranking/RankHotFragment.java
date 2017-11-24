package tv.live.bx.fragments.ranking;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.efeizao.bx.R;
import tv.live.bx.activities.RankActivity;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;
import tv.live.bx.fragments.BaseFragment;
import tv.live.bx.imageloader.ImageLoaderUtil;
import tv.live.bx.model.RankBean;

import java.util.HashMap;
import java.util.Map;

/**
 * 热门榜页面
 */
public class RankHotFragment extends BaseFragment implements RankActivity.IUpdateData {
	private TextView rankInstruction;
	private ListView listView;

	//榜单数据
	private RankBean rankData;

	public RankHotFragment() {
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected int getLayoutRes() {
		return R.layout.fragment_rank_hot;
	}

	@Override
	protected void initMembers() {
		rankInstruction = (TextView) mRootView.findViewById(R.id.rank_instruction);
		listView = (ListView) mRootView.findViewById(R.id.rank_listview);

		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Map<String, String> lmItem = new HashMap<>();
				lmItem.put("id", rankData.all.get(position).mid);
				ActivityJumpUtil.toPersonInfoActivity(mActivity, lmItem, -1);
			}
		});
	}

	@Override
	protected void initWidgets() {
		rankInstruction.setText("热门榜为上热一次数前十名的播主");
	}

	@Override
	protected void setEventsListeners() {
	}

	@Override
	protected void initData(Bundle bundle) {

	}

	@Override
	public void update(RankBean bean) {
		rankData = bean;
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		listView = null;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	private BaseAdapter adapter = new BaseAdapter() {
		@Override
		public int getCount() {
			return  rankData == null ? 0 : rankData.all.size();
		}

		@Override
		public Object getItem(int position) {
			return rankData.all.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder loHolder;
			if (convertView == null) {
				loHolder = new Holder();
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.a_main_rank_popular_item, listView, false);
				loHolder.moRankNo = (ImageView) convertView.findViewById(R.id.item_rank_no);
				loHolder.moRankNoText = (TextView) convertView.findViewById(R.id.item_rank_no_text);
				loHolder.moIv = (ImageView) convertView.findViewById(R.id.item_fm_rank_field_photo);
				loHolder.moIvPhotoV = (ImageView) convertView.findViewById(R.id.item_fm_rank_field_photo_v);
				loHolder.moTvNickname = (TextView) convertView.findViewById(R.id.item_fm_rank_field_tv_nickname);
				loHolder.moTvTimes = (TextView) convertView.findViewById(R.id.item_tv_times);
				loHolder.mUserLevel = (ImageView) convertView.findViewById(R.id.item_user_level);
				convertView.setTag(loHolder);
			} else {
				loHolder = (Holder) convertView.getTag();
			}

			// 如果需要突然显示“前10名”
			if (position < 10) {
				loHolder.moRankNo.setVisibility(View.VISIBLE);
				loHolder.moRankNoText.setVisibility(View.GONE);
				loHolder.moRankNo.setImageResource(Utils.getFiledDrawable(Constants.USER_RANK_PIX, String.valueOf(position)));
			} else {
				loHolder.moRankNo.setVisibility(View.GONE);
				loHolder.moRankNoText.setVisibility(View.VISIBLE);
				loHolder.moRankNoText.setText(String.valueOf(position + 1));
			}

			RankBean.UserBean bean = rankData.all.get(position);
			if (!TextUtils.isEmpty(bean.headPic))
				ImageLoaderUtil.with().loadImageTransformRoundCircle(getActivity(), loHolder.moIv, bean.headPic);
			loHolder.moTvNickname.setText(bean.nickname);
			loHolder.moIvPhotoV.setVisibility(bean.verified ? View.VISIBLE : View.GONE);
			loHolder.moTvTimes.setText(bean.hot_count + "次");
			ImageLoaderUtil.with().loadImage(getActivity(), loHolder.mUserLevel, Utils.getLevelImageResourceUri(Constants.USER_ANCHOR_LEVEL_PIX, String.valueOf(bean.moderatorLevel)));

			return convertView;
		}
	};

	private class Holder {
		protected ImageView moIv, moRankNo, mUserLevel, moIvPhotoV;
		protected TextView moTvNickname, moTvTimes, moRankNoText;
	}

}
