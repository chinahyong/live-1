package com.bixin.bixin.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.framework.net.impl.CallbackDataHandle;
import com.bixin.bixin.App;
import com.bixin.bixin.home.act.AdviceActivity;
import com.bixin.bixin.activities.SettingsActivity;
import com.bixin.bixin.activities.WebViewActivity;
import com.bixin.bixin.callback.MyUserInfoCallbackDataHandle;
import com.bixin.bixin.common.BusinessUtils;
import com.bixin.bixin.common.Constants;
import com.bixin.bixin.common.Consts;
import com.bixin.bixin.common.MsgTypes;
import com.bixin.bixin.common.helper.operation.OperationHelper;
import com.bixin.bixin.common.helper.photo.PhotoSelectImpl;
import com.bixin.bixin.common.Utils;
import com.bixin.bixin.common.model.WebConstants;
import com.bixin.bixin.common.config.AppConfig;
import com.bixin.bixin.common.config.UserInfoConfig;
import com.bixin.bixin.common.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.BitmapUtility;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.ui.ActionSheetDialog;
import com.bixin.bixin.ui.cropimage.CropImageActivity;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;
import com.umeng.analytics.MobclickAgent;
import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tv.live.bx.R;


public class MeFragment extends BaseFragment implements OnClickListener, OnRefreshListener {

    private static final int ANCHOR_REQUEST_CODE = 0x001;
    private static final int RECHANGE_REQUEST_CODE = 0x201;
    private static final int REQUEST_CODE_FLUSH_ACTIVITY = 0x100;
    /**
     * 与此Fragment关联的Activity
     */
    private RelativeLayout mSettingLayout, mStore, mBackpack, mIncomeLayout, moLlAdvice, mLlLevel, mLlAnchorLevel, mLlRedPackage;
    private View mIncomeLine;
    private ImageView moIvPhoto, moIvPhotoV, mIvUserLevel, mIvUserAnchorLevel, mIvUserSex, mIvBgImg;
    private RelativeLayout moBtRecharge;
    private TextView moTvNickname, moTvCoinBalance, mIncomeTitle, mTvIncome, moTvUserId, moTvEdit, mTvVerifyInfo, mTvLevel, mTvAnchorLevel, mTvIntroduction, mTvNewProducts;
    /* 弹出对话框 */
    private ActionSheetDialog actionSheetDialog;
    private File mCameraFile;


    // 下拉刷新
    private SwipeRefreshLayout swipeRefreshLayout;

    private static String GUIDE_ME = "guide_me";
    private static String GUIDE_EDIT = "guide_edit";
    private Dialog mGuideDialog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_my_info;
    }

    @Override
    protected void initMembers() {
        moTvEdit = (TextView) mRootView.findViewById(R.id.my_info_edit);
        moTvEdit.setVisibility(View.VISIBLE);

        moBtRecharge = (RelativeLayout) mRootView.findViewById(R.id.my_info_balance);
        moLlAdvice = (RelativeLayout) mRootView.findViewById(R.id.my_info_ll_advice);
        mLlLevel = (RelativeLayout) mRootView.findViewById(R.id.my_info_level);
        mStore = (RelativeLayout) mRootView.findViewById(R.id.my_info_store);
        mLlAnchorLevel = (RelativeLayout) mRootView.findViewById(R.id.my_info_anchor_level);
        mLlRedPackage = (RelativeLayout) mRootView.findViewById(R.id.my_info_redpackage_layout);

        mBackpack = (RelativeLayout) mRootView.findViewById(R.id.my_info_backpack);

        mSettingLayout = (RelativeLayout) mRootView.findViewById(R.id.my_info_ll_setting);
        mIncomeLayout = (RelativeLayout) mRootView.findViewById(R.id.me_income_layout);
        mIncomeLine = mRootView.findViewById(R.id.me_income_line);
        //        mLevelProgress = (ProgressBar) mRootView.findViewById(R.id.my_level_progress);

        moIvPhoto = (ImageView) mRootView.findViewById(R.id.my_info_img_user);
        moIvPhotoV = (ImageView) mRootView.findViewById(R.id.my_info_img_user_v);
        mIvBgImg = (ImageView) mRootView.findViewById(R.id.my_info_bg_img);
        mIvUserAnchorLevel = (ImageView) mRootView.findViewById(R.id.item_user_anchor_level);
        mIvUserSex = (ImageView) mRootView.findViewById(R.id.item_user_iv_sex);
        moTvNickname = (TextView) mRootView.findViewById(R.id.my_info_tv_name);
        moTvUserId = (TextView) mRootView.findViewById(R.id.my_info_tv_user_id);
        mTvVerifyInfo = (TextView) mRootView.findViewById(R.id.my_info_tv_verify_info);
        mTvIntroduction = (TextView) mRootView.findViewById(R.id.my_info_tv_introduction);
        mTvNewProducts = (TextView) mRootView.findViewById(R.id.tv_new_products);

        mIvUserLevel = (ImageView) mRootView.findViewById(R.id.item_user_level);
        mTvLevel = (TextView) mRootView.findViewById(R.id.my_info_tv_level_num);
        mTvAnchorLevel = (TextView) mRootView.findViewById(R.id.my_info_tv_anchor_level_num);
        // moTvDesc = (TextView)
        // mRootView.findViewById(R.id.my_info_tv_introduction);
        moTvCoinBalance = (TextView) mRootView.findViewById(R.id.my_info_tv_balance);

        mIncomeTitle = (TextView) mRootView.findViewById(R.id.me_income_tv_title);
        mTvIncome = (TextView) mRootView.findViewById(R.id.tv_income);

        swipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);
		/* 方法过时，setColorScheme也是在调用setColorSchemeResources */
        swipeRefreshLayout
            .setColorSchemeResources(R.color.a_bg_color_da500e, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

    }

    @Override
    protected void initData(Bundle bundle) {
        if (!Utils.getBooleanFlag(
            Utils.getCfg(mActivity, Constants.COMMON_SF_NAME, GUIDE_EDIT, "false"))) {
            showFullDialog(R.layout.dialog_guide_edit_layout,
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        Utils.setCfg(mActivity, Constants.COMMON_SF_NAME, GUIDE_EDIT, "true");
                    }
                });
        }
    }

    public void showFullDialog(int layoutId, DialogInterface.OnDismissListener dismissListener) {
        mGuideDialog = new Dialog(mActivity, R.style.notitleDialog);
        View rootView = mInflater.inflate(layoutId, null);
        View layout = rootView.findViewById(R.id.main_layout);
        layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mGuideDialog.dismiss();
            }
        });
        mGuideDialog.setOnDismissListener(dismissListener);
        mGuideDialog.setContentView(rootView);
        mGuideDialog.show();
    }

    @Override
    public void onRefresh() {
        BusinessUtils.getMyUserInfo(mActivity, new MyUserInfoCallbackDataHandle(mHandler));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MsgTypes.GET_MY_USER_INFO_SUCCESS:
                swipeRefreshLayout.setRefreshing(false);
                updateUserInfo();
                break;
            case MsgTypes.GET_MY_USER_INFO_FAILED:
                swipeRefreshLayout.setRefreshing(false);
                if (msg.obj != null) {
                    UiHelper.showToast(mActivity, (String) msg.obj);
                }
                break;
            case MsgTypes.UPLOAD_BG_SUCCESS:
                updatePickData();
                UiHelper.showShortToast(mActivity, R.string.me_update_user_bg);
                break;
            case MsgTypes.UPLOAD_BG_FAILED:
                UiHelper.showToast(mActivity, msg.getData().getString("errorMsg"));
                break;
            default:
                break;
        }
    }

    public void initWidgets() {
        updateUserInfo();
    }

    protected void setEventsListeners() {
        moIvPhoto.setOnClickListener(this);
        mIvBgImg.setOnClickListener(this);
        moTvEdit.setOnClickListener(this);
        moBtRecharge.setOnClickListener(this);
        mBackpack.setOnClickListener(this);
        moLlAdvice.setOnClickListener(this);
        mLlLevel.setOnClickListener(this);
        mStore.setOnClickListener(this);
        mLlAnchorLevel.setOnClickListener(this);
        mLlRedPackage.setOnClickListener(this);

        mSettingLayout.setOnClickListener(this);
        mIncomeLayout.setOnClickListener(this);

    }

    private void updateUserInfo() throws NotFoundException, NumberFormatException {
        String lsPhoto = UserInfoConfig.getInstance().headPic;
        String lsNickname = UserInfoConfig.getInstance().nickname;
        String lsCoin = UserInfoConfig.getInstance().coin;
        String introduction = UserInfoConfig.getInstance().signature;
        String level = UserInfoConfig.getInstance().level + "";
        String userLevelName = UserInfoConfig.getInstance().userLevelName;
        String verifyInfo = UserInfoConfig.getInstance().verifyInfo;
        String moderatorLevel = UserInfoConfig.getInstance().moderatorLevel + "";
        String moderatorLevelName = UserInfoConfig.getInstance().moderatorLevelName;
        //背景图
        String bgImg = UserInfoConfig.getInstance().bgImg;
        // 2 设置
        if (!TextUtils.isEmpty(lsPhoto)) {
            if (lsPhoto.indexOf("://") == -1) {
                lsPhoto = "file://" + lsPhoto;
            }
            ImageLoaderUtil.getInstance().loadHeadPic(mActivity, moIvPhoto, lsPhoto);
        }
        moIvPhotoV
            .setVisibility(UserInfoConfig.getInstance().isVerifyed() ? View.VISIBLE : View.GONE);
        if (!TextUtils.isEmpty(level)) {
            ImageLoaderUtil.getInstance().loadImage(mIvUserLevel,
                Utils.getLevelImageResourceUri(Constants.USER_LEVEL_PIX, level));
            mTvLevel.setText(userLevelName);
        }
        moTvUserId.setText(UserInfoConfig.getInstance().id);
        if (lsNickname != null) {
            moTvNickname.setText(lsNickname);
        }
        //认证信息
        if (!TextUtils.isEmpty(verifyInfo)) {
            mTvVerifyInfo.setVisibility(View.VISIBLE);
            mTvVerifyInfo
                .setText(String.format(getString(R.string.common_verify_info), verifyInfo));
        } else {
            mTvVerifyInfo.setVisibility(View.GONE);
        }
        //性别图标
        if (UserInfoConfig.getInstance().sex == Consts.GENDER_MALE) {
            mIvUserSex.setImageResource(R.drawable.icon_my_info_man);
        } else {
            mIvUserSex.setImageResource(R.drawable.icon_my_info_feman);
        }
        //主播等级
        if (!TextUtils.isEmpty(moderatorLevel)) {
            mIvUserAnchorLevel.setVisibility(View.VISIBLE);
            ImageLoaderUtil.getInstance().loadImage(mIvUserAnchorLevel,
                Utils.getLevelImageResourceUri(Constants.USER_ANCHOR_LEVEL_PIX, moderatorLevel));
            //			mTvAnchorLevel.setText(String.valueOf(UserInfoConfig.getInstance().moderatorLevel + 1));
            mTvAnchorLevel.setText(moderatorLevelName);
//            mLlAnchorLevel.setVisibility(View.VISIBLE);
        } else {
            mIvUserAnchorLevel.setVisibility(View.GONE);
            mLlAnchorLevel.setVisibility(View.GONE);
        }
        //签名
        if (!TextUtils.isEmpty(introduction)) {
            mTvIntroduction.setVisibility(View.VISIBLE);
            mTvIntroduction.setText(introduction);
        } else {
            mTvIntroduction.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(bgImg)) {
            ImageLoaderUtil.getInstance().loadImage(mIvBgImg, bgImg);
        }
        //            if (!Utils.isStrEmpty(lsDesc))
        //                moTvDesc.setText("简介：" + lsDesc);
        SpannableString sp = new SpannableString(
            mActivity.getResources().getString(R.string.me_income_tip) + "  (微信提现)");
        int start = mActivity.getResources().getString(R.string.me_income_tip).length();
        sp.setSpan(new ForegroundColorSpan(0xffaaaaaa), start, sp.length(),
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        sp.setSpan(new AbsoluteSizeSpan(14, true), start, sp.length(),
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        mIncomeTitle.setText(sp);
        if (UserInfoConfig.getInstance().incomeAvailable != null) {
            mIncomeLayout.setVisibility(View.VISIBLE);
            mIncomeLine.setVisibility(View.VISIBLE);
            mTvIncome.setText(UserInfoConfig.getInstance().incomeAvailable);
        } else {
            // 如果提现金额为空，直接隐藏
            mIncomeLayout.setVisibility(View.GONE);
            mIncomeLine.setVisibility(View.GONE);
        }
        if (!Utils.isStrEmpty(lsCoin))
        //#ec4c09--上一版本的颜色
        {
            moTvCoinBalance.setText(Html.fromHtml(
                mActivity.getResources().getString(R.string.me_balance_text)
                    + "<font color='#aaaaaa'> " + lsCoin + "</font>"));
        }
        if (AppConfig.getInstance().hasNewShop()) {
            mTvNewProducts.setVisibility(View.VISIBLE);
        } else {
            mTvNewProducts.setVisibility(View.GONE);
        }
    }

    /**
     * 弹出相片操作选择
     */
    private void showGetPhotoDialog() {
        actionSheetDialog = new ActionSheetDialog(mActivity).builder().setCancelable(true)
            .setCanceledOnTouchOutside(true)
            .addSheetItem(getString(R.string.system_camera), ActionSheetDialog.SheetItemColor.BLACK,
                new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        mCameraFile = PhotoSelectImpl.takePhoto(mActivity);
                    }
                }).addSheetItem(getString(R.string.system_gallery_select),
                ActionSheetDialog.SheetItemColor.BLACK,
                new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        PhotoSelectImpl.selectPhoto(mActivity);
                    }
                });
        actionSheetDialog.show();
    }

    /**
     * 更新选图数据
     */
    private void updatePickData() {
        String imagePath = String.valueOf(mIvBgImg.getTag());
        if (mIvBgImg.getTag() != null && !TextUtils.isEmpty(imagePath)) {
            if (!imagePath.startsWith(Constants.FILE_PXI)) {
                imagePath = Constants.FILE_PXI + imagePath;
            }
            EvtLog.d(TAG, "ImagePath After Crop updatePickData: " + imagePath);
            ImageLoaderUtil.getInstance().loadHeadPic(mActivity, mIvBgImg, imagePath);
            mIvBgImg.setTag("");
        }
    }

    /**
     * 当选中fragment时，更新相关数据
     */
    @Override
    protected void onTabSelected() {
        super.onTabSelected();
        if (mActivity != null) {
            BusinessUtils.getMyUserInfo(mActivity, new MyUserInfoCallbackDataHandle(mHandler));
            initWidgets();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initWidgets();
    }

    // 友盟统计
    public void onResume() {
        super.onResume();
        BusinessUtils.getMyUserInfo(mActivity, new MyUserInfoCallbackDataHandle(mHandler));
    }

    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ANCHOR_REQUEST_CODE:
                break;
            case REQUEST_CODE_FLUSH_ACTIVITY:
                // 跳到他人主页、动态详情页，返回进行刷新
                BusinessUtils.getMyUserInfo(mActivity, new MyUserInfoCallbackDataHandle(mHandler));
                break;
            case PhotoSelectImpl.REQUEST_ALBUM:
                //选择相片回调
                if (data != null) {
                    Uri uri = data.getData();
                    String selectPath = BitmapUtility.getFilePathFromUri(mActivity, uri);
                    EvtLog.e(TAG, "ImagePath Before Crop: " + selectPath);
                    //关闭裁剪
                    PhotoSelectImpl.jumpToCrop(mActivity, uri);
                }
                break;
            case PhotoSelectImpl.REQUEST_CAMERA:
                if (mCameraFile != null && resultCode == Activity.RESULT_OK) {
                    Uri imgUri = Uri.fromFile(mCameraFile);
                    if (imgUri != null) {
                        PhotoSelectImpl.jumpToCrop(mActivity, imgUri);
                    }
                }
                break;
            case PhotoSelectImpl.REQUEST_CROP:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String path = data.getStringExtra(CropImageActivity.EXA_IMAGE_PATH);
                    EvtLog.d(TAG, "ImagePath After Crop: " + path);
                    mIvBgImg.setTag(path);
                    BusinessUtils.uploadUserBg(mActivity, new UploadBgCallbackData(this), path);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /**
             * 功能由 跳转个人主页  转换为   更换背景图片
             * @verison 2.5.0
             */
            case R.id.my_info_img_user:
                //点击头像查看大图
                List<String> imgUrl = new ArrayList<>();
                imgUrl.add(UserInfoConfig.getInstance().headPic);
                ActivityJumpUtil.toImageBrowserActivity(mActivity, 0, imgUrl);
                break;
            case R.id.my_info_bg_img:
                // 弹出选择图片的加载方式，选择背景图片
                showGetPhotoDialog();
                break;
            case R.id.my_info_edit:
                MobclickAgent.onEvent(App.mContext, "editInpersonalPage");
                ActivityJumpUtil.toEditDataActivity(mActivity, true, 0);
                break;

            case R.id.me_income_layout:
                MobclickAgent.onEvent(App.mContext, "earnings");
                ActivityJumpUtil.toWebViewActivity(mActivity,
                    WebConstants.getFullWebMDomain(WebConstants.COMMON_ME_INCOME_URL), true);
                break;
            case R.id.my_info_balance:
                MobclickAgent.onEvent(App.mContext, "clickPaopao");
                OperationHelper.onEvent(App.mContext, "clickPaopao", null);
                ActivityJumpUtil.toWebViewActivity(mActivity,
                    WebConstants.getFullWebMDomain(WebConstants.RECHARGE_WEB_URL), true,
                    RECHANGE_REQUEST_CODE);
                break;
            case R.id.my_info_backpack:
                OperationHelper.onEvent(App.mContext, "clickMyBackpackInMine", null);
                ActivityJumpUtil.toWebViewActivity(mActivity,
                    WebConstants.getFullWebMDomain(WebConstants.WEB_MY_BACKPACK), true, -1);
                break;
            /**
             * 将设置中的 意见反馈放出来
             * @version 2.5.0
             */
            case R.id.my_info_ll_advice:
                ActivityJumpUtil.gotoActivity(mActivity, AdviceActivity.class, false, null, null);
                break;
            case R.id.my_info_ll_setting:
                MobclickAgent.onEvent(App.mContext, "clickSettingButton");
                ActivityJumpUtil.gotoActivity(mActivity, SettingsActivity.class, false, null, null);
                break;
            /**
             * 查看等级
             * @version 2.5.0
             */
            case R.id.my_info_level:
                MobclickAgent.onEvent(App.mContext, "level");
                Map<String, String> webInfo = new HashMap<>();
                webInfo.put(WebViewActivity.URL,
                    WebConstants.getFullWebMDomain(WebConstants.GET_MY_LEVEL_URL));
                webInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
                ActivityJumpUtil
                    .gotoActivity(mActivity, WebViewActivity.class, false, WebViewActivity.WEB_INFO,
                        (Serializable) webInfo);
                break;
            case R.id.my_info_store:
                OperationHelper.onEvent(App.mContext, "clickStoreInMine", null);
                ActivityJumpUtil.toWebViewActivity(mActivity,
                    WebConstants.getFullWebMDomain(WebConstants.WEB_STORE), true, -1);
                //标记为已阅读
                if (AppConfig.getInstance().hasNewShop()) {
                    AppConfig.getInstance().updateLastShopVersionStatus();
                }
                break;
            case R.id.my_info_anchor_level:
                MobclickAgent.onEvent(App.mContext, "clickLevelOfBroadcaster");
                Map<String, String> webAnchorInfo = new HashMap<>();
                webAnchorInfo.put(WebViewActivity.URL,
                    WebConstants.getFullWebMDomain(WebConstants.GET_MY_ANCHOR_LEVEL_URL));
                webAnchorInfo.put(WebViewActivity.IS_NOT_SHARE, String.valueOf(true));
                ActivityJumpUtil
                    .gotoActivity(mActivity, WebViewActivity.class, false, WebViewActivity.WEB_INFO,
                        (Serializable) webAnchorInfo);
                break;
            case R.id.my_info_redpackage_layout:
                ActivityJumpUtil.toWebViewActivity(mActivity,
                    WebConstants.getFullWebMDomain(WebConstants.REDPACKET_WEB_URL), true);
                break;
            default:
                break;
        }
    }

    private static class UploadBgCallbackData implements CallbackDataHandle {

        private final WeakReference<BaseFragment> mFragment;

        public UploadBgCallbackData(BaseFragment fragment) {
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
            EvtLog.d(TAG, "UploadBgCallbackData success " + success + " errorCode" + errorCode);
            Message msg = new Message();
            if (success) {
                try {
                    msg.what = MsgTypes.UPLOAD_BG_SUCCESS;
                    MeFragment meFragment = (MeFragment) mFragment.get();
                    // 如果fragment未回收，发送消息
                    if (meFragment != null) {
                        meFragment.sendMsg(msg);
                    }
                } catch (Exception e) {

                }
            } else {
                msg.what = MsgTypes.UPLOAD_BG_FAILED;
                if (TextUtils.isEmpty(errorMsg)) {
                    errorMsg = Constants.NETWORK_FAIL;
                }
                Bundle bundle = new Bundle();
                bundle.putString("errorCode", errorCode);
                bundle.putString("errorMsg", errorMsg);
                msg.setData(bundle);
                MeFragment meFragment = (MeFragment) mFragment.get();
                // 如果fragment未回收，发送消息
                if (meFragment != null) {
                    meFragment.sendMsg(msg);
                }
            }
        }
    }
}
