/**
 * Project Name:feizao File Name:GiftEffectViewData.java Package
 * Name:com.efeizao.feizao.model Date:2016-3-4下午6:51:16
 */

package tv.live.bx.model;

/**
 * ClassName:GiftEffectViewData Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2016-3-4 下午6:51:16
 *
 * @author Live
 * @version 1.0
 */

/** 礼物特效数据 */
public class GiftEffectViewData {

	public GiftEffectViewData(int what) {
		this.msgWhat = what;
	}

	public int mGiftPrice;
	public String mUserId;
	public String mUserName;
	public String mGiftId;
	public String mGiftCount;
	public String mGiftName;
	public int mGiftGroupNum;
	public int mGiftNum;
	public String mGiftGroup;
	public String mUserPhoto;
	public String mGiftPhoto;
	public String bonusButtonEnabled;
	public boolean isVisible = false;
	/** 毫秒数 */
	public int mVisibleTime;
	public int msgWhat;
}
