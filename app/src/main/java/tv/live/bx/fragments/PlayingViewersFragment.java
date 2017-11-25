package tv.live.bx.fragments;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;

import java.util.HashMap;
import java.util.Map;

import tv.live.bx.R;
import tv.live.bx.adapters.AudienceAdapter;
import tv.live.bx.common.Constants;
import tv.live.bx.common.MsgTypes;
import tv.live.bx.library.util.EvtLog;

public class PlayingViewersFragment extends BaseFragment {
	private RadioButton moRbtAdministrator, moRbtAudience;
	private ListView moLvAdministrator, moLvAudience;
	private LinearLayout moLlAdministrator, moLlAudience;

	private AudienceAdapter moAdminAdapter, moAudienceAdapter;

	@Override
	protected int getLayoutRes() {

		// TODO Auto-generated method stub
		return R.layout.fragment_playing_viewers;
	}

	@Override
	protected void initData(Bundle bundle) {

	}

	protected void initMembers() {
		moRbtAdministrator = (RadioButton) mRootView.findViewById(R.id.fragment_playing_viewers_rbt_administrator);
		moRbtAudience = (RadioButton) mRootView.findViewById(R.id.fragment_playing_viewers_rbt_audience);
		moLvAdministrator = (ListView) mRootView.findViewById(R.id.fragment_playing_viewers_administrator_lv);
		moLvAudience = (ListView) mRootView.findViewById(R.id.fragment_playing_viewers_audience_lv);
		moLlAdministrator = (LinearLayout) mRootView.findViewById(R.id.fragment_playing_viewers_ll_administrator);
		moLlAudience = (LinearLayout) mRootView.findViewById(R.id.fragment_playing_viewers_ll_audience);

		moAdminAdapter = new AudienceAdapter(mActivity, AudienceAdapter.VIEWER_TYPE_ADMIN);
		moAudienceAdapter = new AudienceAdapter(mActivity, AudienceAdapter.VIEWER_TYPE_AUDIENCE);

		moLvAdministrator.setAdapter(moAdminAdapter);
		moLvAudience.setAdapter(moAudienceAdapter);
	}

	@Override
	protected void handleMessage(Message msg) {
		switch (msg.what) {
		case MsgTypes.ADD_AUDIENCE:
			@SuppressWarnings("unchecked")
			Map<String, String> lmItem = (Map<String, String>) msg.obj;
			EvtLog.e(TAG, "ADD_AUDIENCE" + lmItem.toString());
			String lsType = lmItem.get("type");
			if (lsType == null)
				return;
			// type为2,3,5都为管理员
			if (lsType.equals(Constants.USER_TYPE_ANCHOR) || lsType.equals(Constants.USER_TYPE_ADMIN)
					|| lsType.equals(Constants.USER_TYPE_ROOMOWNER))
				moAdminAdapter.insertData(lmItem);
			else if (lsType.equals(Constants.USER_TYPE_UNLOGIN)) {
				lmItem.put("nickname", "匿名用户");
				// lmItem.put("photo", R.drawable.moren);
				moAudienceAdapter.insertData(moAudienceAdapter.getCount(), lmItem);
			} else
				// if (lsType.equals("1") || lsType.equals("4"))
				moAudienceAdapter.insertData(lmItem);
			moRbtAudience.setText("观众：" + moAudienceAdapter.getCount());
			moRbtAdministrator.setText("管理员：" + moAdminAdapter.getCount());
			break;
		case MsgTypes.DEL_AUDIENCE:
			EvtLog.e(TAG, "DEL_AUDIENCE 管理员：" + moAdminAdapter.getCount());
			@SuppressWarnings("unchecked")
			Map<String, String> dlmItem = (Map<String, String>) msg.obj;
			String dlsType = dlmItem.get("type");
			if (dlsType == null)
				return;
			if (dlsType.equals(Constants.USER_TYPE_ANCHOR) || dlsType.equals(Constants.USER_TYPE_ADMIN)
					|| dlsType.equals(Constants.USER_TYPE_ROOMOWNER))
				moAdminAdapter.removeData("cid", dlmItem.get("cid"));
			else
				moAudienceAdapter.removeData("cid", dlmItem.get("cid"));
			moRbtAudience.setText("观众：" + moAudienceAdapter.getCount());
			moRbtAdministrator.setText("管理员：" + moAdminAdapter.getCount());
			break;

		default:
			break;
		}
	}

	public void initWidgets() {
	}

	protected void setEventsListeners() {
		moRbtAdministrator.setOnClickListener(new OnAdministratorClick());
		moRbtAudience.setOnClickListener(new OnAudienceClick());

	}

	public void addUser(String piUid, String piType, String psNickname, String level, String fromModeratorLevel,
			String psPhoto, String cid) {
		Map<String, String> lmItem = new HashMap<String, String>();
		lmItem.put("uid", piUid);
		lmItem.put("type", piType);
		lmItem.put("moderatorLevel", fromModeratorLevel);
		lmItem.put("nickname", psNickname);
		lmItem.put("photo", psPhoto);
		lmItem.put("cid", cid);
		lmItem.put("level", level);
		Message loMsg = new Message();
		loMsg.obj = lmItem;
		loMsg.what = MsgTypes.ADD_AUDIENCE;
		sendMsg(loMsg);
	}

	public void delUser(String piUid, String piType, String cid) {
		Map<String, String> lmItem = new HashMap<String, String>();
		lmItem.put("uid", piUid);
		lmItem.put("type", piType);
		lmItem.put("cid", cid);
		Message loMsg = new Message();
		loMsg.obj = lmItem;
		loMsg.what = MsgTypes.DEL_AUDIENCE;
		sendMsg(loMsg);
	}

	/*************************** 点击事件 **********************************/
	private class OnAdministratorClick implements OnClickListener {

		@Override
		public void onClick(View v) {

			moLlAdministrator.setVisibility(View.VISIBLE);
			moLlAudience.setVisibility(View.GONE);

		}
	}

	private class OnAudienceClick implements OnClickListener {

		@Override
		public void onClick(View v) {

			moLlAdministrator.setVisibility(View.GONE);
			moLlAudience.setVisibility(View.VISIBLE);
		}
	}

}
