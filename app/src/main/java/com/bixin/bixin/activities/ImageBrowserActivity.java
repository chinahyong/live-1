package com.bixin.bixin.activities;

import static com.bixin.bixin.fragments.ImagePagerFragment.IMAGE_CACHE_PATH;
import static com.bixin.bixin.fragments.ImagePagerFragment.IMAGE_FIX;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import tv.guojiang.baselib.image.listener.ImageLoadingListener;
import tv.guojiang.baselib.image.model.ImageSize;
import tv.live.bx.R;
import com.bixin.bixin.activities.base.BaseFragmentActivity;
import com.bixin.bixin.adapters.ImageBrowserAdapter;
import com.bixin.bixin.imageloader.ImageLoaderUtil;
import com.bixin.bixin.library.util.BitmapUtils;
import com.bixin.bixin.library.util.DateUtil;
import com.bixin.bixin.library.util.EvtLog;
import com.bixin.bixin.library.util.FileUtil;
import com.bixin.bixin.ui.ScrollViewPager;
import com.bixin.bixin.util.UiHelper;

public class ImageBrowserActivity extends BaseFragmentActivity implements OnPageChangeListener,
    OnClickListener {

    public static String IS_NEED_EIDT = "is_need_edit";
    private ScrollViewPager mSvpPager;
    private ImageBrowserAdapter mAdapter;
    private TextView mSvpText, mTvDelete, mTvSave;
    private String mType;
    private int mPosition;
    private int mTotal;
    private List<String> imageUrl = new ArrayList<String>();
    private ArrayList<String> mDelUrls = new ArrayList<String>();
    /**
     * Head布局
     */
    private RelativeLayout mTopLayout;

    public static final String INIT_SHOW_INDEX = "init_show_position";
    public static final String IMAGE_URL = "image_url";
    private boolean isNeedEdit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isSystemBarTint = false;
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            EvtLog.e(TAG, "onCreate savedInstanceState != null,mPosition= " + mPosition);
            mPosition = bundle.getInt(INIT_SHOW_INDEX, 0);
            imageUrl = (ArrayList<String>) bundle.getSerializable(IMAGE_URL);

            isNeedEdit = bundle.getBoolean(IS_NEED_EIDT, false);
        }
        init();
    }

    @Override
    protected int getLayoutRes() {
        // TODO Auto-generated method stub
        return R.layout.activity_imagebrowser;
    }

    @Override
    public void initWidgets() {
        mSvpPager = (ScrollViewPager) findViewById(R.id.imagebrowser_svp_pager);
        mSvpPager.setOnPageChangeListener(this);
        mTvSave = (TextView) findViewById(R.id.image_browser_save);
        mTvDelete = (TextView) findViewById(R.id.image_browser_delete);
    }

    @Override
    protected void setEventsListeners() {
        // TODO Auto-generated method stub
        mTvDelete.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageUrl != null) {
            outState.putSerializable(IMAGE_URL, (Serializable) imageUrl);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            imageUrl = (ArrayList<String>) savedInstanceState.getSerializable(IMAGE_URL);
        }
    }

    private void init() {
        //		mSvpText = (TextView) findViewById(R.id.imagebrowser_ptv_page);
        mTopLayout = (RelativeLayout) findViewById(R.id.top_layout);
        if (isNeedEdit) {
            mTvDelete.setVisibility(View.VISIBLE);
        } else {
            mTvDelete.setVisibility(View.GONE);
            //			mSvpText.setVisibility(View.VISIBLE);
        }
        initTitleData();
        mTotal = imageUrl.size();
        if (mPosition >= mTotal) {
            mPosition = mTotal - 1;
        }
        if (mTotal >= 1) {
            mTopRightText.setText((mPosition + 1) + "/" + mTotal);
            //			mSvpText.setText((mPosition + 1) + "/" + mTotal);
            mAdapter = new ImageBrowserAdapter(mActivity, imageUrl, getSupportFragmentManager());
            mSvpPager.setAdapter(mAdapter);
            mSvpPager.setCurrentItem(mPosition, false);
        }
    }

    /**
     * 初始化title信息
     */
    @Override
    protected void initTitleData() {
        mTopBackLayout = (RelativeLayout) findViewById(R.id.ry_bar_left);
        mTopBackIv = (ImageView) findViewById(R.id.iv_bar_left);
        mTopRightTextLayout = (RelativeLayout) findViewById(R.id.ry_bar_right_text);
        mTopRightText = (TextView) findViewById(R.id.tv_bar_right);

        mTopBackLayout.setOnClickListener(this);
        mTopRightTextLayout.setOnClickListener(this);
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        mPosition = arg0;
        setPosDisplay(arg0);
    }

    @Override
    public void onClick(View arg0) {
        // showCustomToast("图片已保存到本地");
        switch (arg0.getId()) {
            case R.id.ry_bar_left:
                onBackPressed();
                break;
            // 删除操作
            case R.id.image_browser_delete:
                UiHelper.showConfirmDialog(ImageBrowserActivity.this, R.string.sure_delete,
                    R.string.determine, R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String s = imageUrl.remove(mPosition);
                            mDelUrls.add(s);
                            if (imageUrl.isEmpty()) {
                                onBackPressed();
                            } else {
                                setPosDisplay(mSvpPager.getCurrentItem());
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                break;
            //保存操作
            case R.id.image_browser_save:
                // 获取当前展示图片地址
                String path = imageUrl.get(mPosition);
                if (TextUtils.isEmpty(path)) {
                    return;
                }

                ImageLoaderUtil.getInstance()
                    .loadImage(path, ImageSize.SIZE_ORIGINAL, ImageSize.SIZE_ORIGINAL,
                        new ImageLoadingListener() {
                            @Override
                            public void onLoadStarted(Drawable drawable) {

                            }

                            @Override
                            public void onLoadFailed(Drawable drawable) {

                            }

                            @Override
                            public void onLoadingComplete(Drawable resource) {
                                BitmapDrawable bd = (BitmapDrawable) resource;
                                Bitmap bm = bd.getBitmap();
                                // 将图片转换为Bitmap
                                // 保存到本地地址
                                String destPath =
                                    FileUtil.getDiskCachePath(mActivity, IMAGE_CACHE_PATH)
                                        + File.separator + DateUtil
                                        .fmtTimeMillsToString(System.currentTimeMillis(),
                                            DateUtil.sdf1) + IMAGE_FIX;
                                boolean flag = BitmapUtils.writeImage(bm, destPath, 100);
                                // 其次把文件插入到系统图库
                                try {
                                    File file = new File(destPath);
                                    MediaStore.Images.Media
                                        .insertImage(getContentResolver(), file.getAbsolutePath(),
                                            file.getName(), null);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                // 最后通知图库更新
                                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                    Uri.parse("file://" + destPath)));
                                UiHelper.showToast(mActivity,
                                    flag ? getString(R.string.commutity_image_save_success)
                                        : getString(R.string.commutity_image_save_fail));
                            }

                            @Override
                            public void onLoadCleared(Drawable drawable) {

                            }
                        });
                break;
            default:
                break;
        }
    }

    private void setPosDisplay(int position) {
        String pos = String.format("%d/%d", position + 1, imageUrl.size());
        mTopRightText.setText(pos);
        //		mSvpText.setText(pos);
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(0, R.anim.a_fade_out);
        if (mDelUrls.isEmpty()) {
            setResult(RESULT_CANCELED);
        } else {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("mDelUrls", mDelUrls);
            setResult(RESULT_OK, intent);
        }
        super.onBackPressed();
    }

}
