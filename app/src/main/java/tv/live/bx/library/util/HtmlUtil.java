/**
 * Project Name:feizao File Name:HtmlUtil.java Package
 * Name:com.efeizao.feizao.library.util Date:2016-1-12下午3:33:13
 */

package tv.live.bx.library.util;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.view.View;

import tv.live.bx.FeizaoApp;
import tv.live.bx.activities.WebViewActivity;
import tv.live.bx.util.ActivityJumpUtil;
import tv.live.bx.emoji.ParseEmojiMsgUtil;
import tv.live.bx.fragments.LiveChatFragment;
import tv.live.bx.ui.ChatTextViewClickableSpan;
import tv.live.bx.ui.VerticalImageSpan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName:HtmlUtil Function: TODO ADD FUNCTION. Reason: TODO ADD REASON.
 * Date: 2016-1-12 下午3:33:13
 *
 * @author Live
 * @version 1.0
 */
public class HtmlUtil {
	// html换行符
	public static final String COMMON_HTML_NEW_LINE = "<br/>";
	// text 换行符
	public static final String COMMON_TEXT_NEW_LINE = "\n";

	/**
	 * 处理html文本显示；
	 * 1、解析图片；2、增加表情解析；
	 *
	 * @param source      源字符串
	 * @param imgGetter   图片解析器  可以为null
	 * @param mTagHandler 自定义标签解析 可以为null
	 * @return
	 */
	public static SpannableString htmlTextDeal(final Context mContext, String source, ImageGetter imgGetter,
											   TagHandler mTagHandler) {
		String htmlText = source.replaceAll(COMMON_TEXT_NEW_LINE, COMMON_HTML_NEW_LINE);
		// 解析html标签，返回spanned
		Spanned span = Html.fromHtml(htmlText, imgGetter, mTagHandler);
		// 创建最终返回的SpannableStringBuilder
		SpannableStringBuilder ssb = SpannableStringBuilder.valueOf(span);
		// 获取图片spans
		ImageSpan[] iss = span.getSpans(0, span.length(), ImageSpan.class);

		// 设置图片居中，添加点击放大功能
		if (iss != null) {
			final List<String> imageUrl = new ArrayList<>();
			int position = 0;
			for (int i = 0; i < iss.length; i++) {
				ImageSpan is = iss[i];
				if (is.getSource().contains("/emoji/")) {
					break;
				}
				// gif表情，需要放大一般显示
				else if (is.getSource().contains(".gif")) {
					break;
				} else {
					VerticalImageSpan vis = new VerticalImageSpan(is.getDrawable());
					final int imagePosition = position;
					imageUrl.add(is.getSource());
					int start = span.getSpanStart(is);
					int end = span.getSpanEnd(is);
					ssb.setSpan(is, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					ssb.setSpan(new ChatTextViewClickableSpan(new LiveChatFragment.IClickUserName() {

						@Override
						public void onClick(String imageSrc, String uid) {
							ActivityJumpUtil.toImageBrowserActivity(mContext, imagePosition, imageUrl);
						}
					}, is.getSource(), "", "", Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					ssb.setSpan(vis, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					position++;
				}

			}
		}
		SpannableString content = ParseEmojiMsgUtil.getExpressionString(ssb);
		return content;
	}

	/**
	 * 处理html文本Url点击事件；
	 * 1、增加URl解析
	 *
	 * @param mContext     上下文
	 * @param charSequence 源字符串
	 * @return
	 */
	public static SpannableStringBuilder htmlTextUrlClick(Context mContext, CharSequence charSequence) {
		// 生成最终返回的SpannableStringBuilder
		SpannableStringBuilder ssb = SpannableStringBuilder.valueOf(charSequence);
		URLSpan[] urlSpan = ssb.getSpans(0, charSequence.length(), URLSpan.class);
		EvtLog.e("HtmlUtil", "urlSpan:" + urlSpan.length);
		int start = 0;
		int end = 0;
		for (URLSpan url : urlSpan) {
			start = ssb.getSpanStart(url);
			end = ssb.getSpanEnd(url);
			MyURLSpan myURLSpan = new MyURLSpan(url.getURL());
			EvtLog.e("HtmlUtil", "urlSpan:xxx" + url.getURL());
			// to replace each link span with customized ClickableSpan
			ssb.setSpan(myURLSpan, start, end, ssb.getSpanFlags(urlSpan));
		}
		return ssb;
	}

	/**
	 * 点击会话信息的URL
	 */
	public static class MyURLSpan extends ClickableSpan {
		private String mUrl;

		MyURLSpan(String url) {
			mUrl = url;
		}

		@Override
		public void onClick(View widget) {
			EvtLog.e("HtmlUtil", "MyURLSpan:" + mUrl.toString());
			widget.setBackgroundColor(Color.parseColor("#00000000"));
			Map<String, String> lmPageInfo = new HashMap<String, String>();
			lmPageInfo.put(WebViewActivity.URL, mUrl);
			ActivityJumpUtil.gotoActivity(FeizaoApp.mConctext, WebViewActivity.class, false, WebViewActivity.WEB_INFO,
					(Serializable) lmPageInfo);
		}
	}
}
