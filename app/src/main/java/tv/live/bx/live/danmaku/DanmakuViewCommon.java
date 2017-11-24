/**
 * Project Name:feizao File Name:DanmakuViewCommon.java Package
 * Name:com.efeizao.feizao.library.common Date:2015-11-17上午11:38:25
 */

package tv.live.bx.live.danmaku;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

import tv.live.bx.common.Constants;
import tv.live.bx.common.Utils;

import java.io.InputStream;
import java.util.HashMap;

import master.flame.danmaku.controller.DrawHandler.Callback;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.model.android.SpannedCacheStuffer;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;

/**
 * 弹幕公共类 REASON. Date: 2015-11-17 上午11:38:25
 *
 * @author Live
 * @version 1.0
 */
public class DanmakuViewCommon {
	/**
	 * 弹幕
	 */
	protected IDanmakuView mDanmakuView;
	protected BaseDanmakuParser mParser;
	protected DanmakuContext mDanmakuContext;

	public DanmakuViewCommon(IDanmakuView mDanmakuView) {
		this.mDanmakuView = mDanmakuView;
	}

	/**
	 * 弹幕唤醒，请在activity中onResume方法中调用
	 */
	public void onResume() {
		if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
			mDanmakuView.resume();
		}
	}

	/**
	 * 弹幕暂停，请在activity中onPause方法中调用
	 */
	public void onPause() {
		if (mDanmakuView != null && mDanmakuView.isPrepared()) {
			mDanmakuView.pause();
		}
	}

	public void clear() {
		if (mDanmakuView != null) {
			mDanmakuView.clearDanmakusOnScreen();
		}
	}

	/**
	 * 初始化弹幕
	 */
	public void initDanmaku() {
		// // 设置最大显示行数
		HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
		maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 3); // 滚动弹幕最大显示3行
		// 设置是否禁止重叠
		HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
		overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
		overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_BOTTOM, false);
		overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, false);

		mDanmakuContext = DanmakuContext.create();
		mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_NONE).setDuplicateMergingEnabled(false)
				.setScrollSpeedFactor(2f).setScaleTextSize(1.2f).setCacheStuffer(new SpannedCacheStuffer(), mCacheStufferAdapter) // 图文混排使用SpannedCacheStuffer
//				.setCacheStuffer(new BackgroundCacheStuffer())
				// 绘制背景使用BackgroundCacheStuffer
				.setMaximumLines(maxLinesPair)
				.alignBottom(true)
				.preventOverlapping(overlappingEnablePair);

		if (mDanmakuView != null) {
			mParser = createParser(null);
			mDanmakuView.setCallback(new Callback() {
				@Override
				public void updateTimer(DanmakuTimer timer) {
				}

				@Override
				public void danmakuShown(BaseDanmaku danmaku) {

				}

				@Override
				public void drawingFinished() {

				}

				@Override
				public void prepared() {
					mDanmakuView.start();
				}
			});
			mDanmakuView.prepare(mParser, mDanmakuContext);
			mDanmakuView.showFPS(false);
			mDanmakuView.enableDanmakuDrawingCache(true);
			// mDanmakuView.hide();
		}
	}

//	private class BackgroundCacheStuffer extends SpannedCacheStuffer {
//		// 通过扩展SimpleTextCacheStuffer或SpannedCacheStuffer个性化你的弹幕样式
//		final Paint paint = new Paint();
//
//		@Override
//		public void measure(BaseDanmaku danmaku, TextPaint paint) {
//			danmaku.padding = 10; // 在背景绘制模式下增加padding
//			super.measure(danmaku, paint);
//		}
//
//		@Override
//		public void drawBackground(BaseDanmaku danmaku, Canvas canvas, float left, float top) {
//			paint.setColor(0x00000000);
//			canvas.drawRect(left + 2, top + 2, left + danmaku.paintWidth - 2, top + danmaku.paintHeight - 2, paint);
//		}
//
//		@Override
//		public void drawStroke(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, Paint paint) {
//			// 禁用描边绘制
//		}
//	}

	/**
	 * 创建字幕解析器
	 */
	public BaseDanmakuParser createParser(InputStream stream) {

		if (stream == null) {
			return new BaseDanmakuParser() {

				@Override
				protected Danmakus parse() {
					return new Danmakus();
				}
			};
		}

		ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);

		try {
			loader.load(stream);
		} catch (IllegalDataException e) {
			e.printStackTrace();
		}
		BaseDanmakuParser parser = new BiliDanmukuParser();
		IDataSource<?> dataSource = loader.getDataSource();
		parser.load(dataSource);
		return parser;

	}

	public void hideDanmakuView() {
		if (mDanmakuView != null) {
			mDanmakuView.clearDanmakusOnScreen();
			mDanmakuView.pause();
			mDanmakuView.hide();
		}
	}

	public void showDanmakuView() {
		if (mDanmakuView != null) {
			mDanmakuView.clearDanmakusOnScreen();
			mDanmakuView.resume();
			mDanmakuView.show();
		}
	}

	public void destoryDanmaku() {
		if (mDanmakuView != null) {
			// dont forget release!
			mDanmakuView.release();
			mDanmakuView = null;
		}
	}

	/**
	 * 发送图文字幕
	 */
	@SuppressWarnings("unused")
	public void addDanmaKuShowTextAndImage(CharSequence text, boolean isPrivate, int mCurrentScreenType) {
		if (mDanmakuView == null || mCurrentScreenType == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
				|| mDanmakuView.isPaused())
			return;
		BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
		// String text = "bitmap";
		// SpannableStringBuilder spannableStringBuilder = new
		// SpannableStringBuilder(text);
		// Drawable drawable =
		// getResources().getDrawable(R.drawable.icon_guanzhu);
		// drawable.setBounds(0, 0, 100, 100);
		// ImageSpan span = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
		// spannableStringBuilder.setSpan(span, 0, text.length(),
		// Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		// spannableStringBuilder.append("图文混排");
		// spannableStringBuilder.setSpan(new
		// BackgroundColorSpan(Color.parseColor("#8A2233B1")), 0,
		// spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

		danmaku.text = text;
		danmaku.priority = 0;
		danmaku.isLive = false;
		danmaku.setTime(mDanmakuView.getCurrentTime() + 50);
		danmaku.textSize = 16f * (mParser.getDisplayer().getDensity() - 0.6f);
		danmaku.textColor = Color.WHITE;
		// danmaku.textColor = Color.RED;
		if (isPrivate) {
			danmaku.borderColor = Color.RED;
		} else {
			danmaku.borderColor = 0;
		}
		danmaku.textShadowColor = Color.BLACK; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
		// danmaku.underlineColor = Color.GREEN;
		mDanmakuView.enableDanmakuDrawingCache(true);
		mDanmakuView.addDanmaku(danmaku);
	}

	/**
	 * 显示用户类别图标
	 */
	public static void showUserType(SpannableStringBuilder ss, String userType) {
		if (Constants.USER_TYPE_OFFICIAL_ADMIN.equals(userType)) {
			ss.append(Utils.getImageToSpannableString(Utils.getFiledDrawable(Constants.USER_TYPE_PIX, Constants.USER_TYPE_OFFICIAL)));
			ss.append(Utils.getImageToSpannableString(Utils.getFiledDrawable(Constants.USER_TYPE_PIX, Constants.USER_TYPE_ADMIN)));
		} else {
			ss.append(Utils.getImageToSpannableString(Utils.getFiledDrawable(Constants.USER_TYPE_PIX, userType)));
		}
	}

	private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {

		@Override
		public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
			if (danmaku.text instanceof Spanned) { // 根据你的条件检查是否需要需要更新弹幕
				// FIXME 这里只是简单启个线程来加载远程url图片，请使用你自己的异步线程池，最好加上你的缓存池
			}
		}

		@Override
		public void releaseResource(BaseDanmaku danmaku) {
			// TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
		}
	};
}
