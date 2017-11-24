/**
 * Project Name:feizao File Name:HtmlTagHandler.java Package
 * Name:com.efeizao.feizao.library.util Date:2016-1-21上午9:51:40
 */

package tv.live.bx.library.util;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Html.TagHandler;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;

import tv.live.bx.common.Constants;
import tv.live.bx.fragments.LiveChatFragment;
import tv.live.bx.ui.ChatTextViewClickableSpan;

import org.xml.sax.XMLReader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClassName:HtmlTagHandler Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2016-1-21 上午9:51:40
 * @author Live
 * @version 1.0
 */
public class HtmlTagHandler implements TagHandler {

	protected static String GROUP_TAG = "group";
	private int startIndex = 0;
	private int stopIndex = 0;

	private String groupName;
	private String groupId;

	private LiveChatFragment.IClickUserName iclickUserName;

	public void setOnIClickUsernName(LiveChatFragment.IClickUserName iclickUserName) {
		this.iclickUserName = iclickUserName;
	}

	@Override
	public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
		if (tag.equals(GROUP_TAG)) {
			if (opening) {
				startTag(tag, output, xmlReader);
			} else {
				endTag(tag, output, xmlReader);
			}
		}

	}

	public void startTag(String tag, Editable output, XMLReader xmlReader) {
		startIndex = output.length();
		// try {
		// Field elementField = xmlReader.getClass().getDeclaredField("name");
		// elementField.setAccessible(true);
		// String element = (String) elementField.get(xmlReader);
		// // groupId = (String) xmlReader.getProperty("id");
		// // groupName = (String) xmlReader.getProperty("name");
		//
		// } catch (Exception e) {
		//
		// EvtLog.e("", e.toString());
		// e.printStackTrace();
		//
		// }

	}

	public void endTag(String tag, Editable output, XMLReader xmlReader) {
		stopIndex = output.length();
		String content = output.subSequence(startIndex, stopIndex).toString();
		if (!TextUtils.isEmpty(content)) {
			String[] groupInfo = content.split(",");
			SpannableString spanStr = new SpannableString(Constants.COMMON_INSERT_POST_PIX + groupInfo[1]
					+ Constants.COMMON_INSERT_POST_PIX);
			ChatTextViewClickableSpan span = new ChatTextViewClickableSpan(iclickUserName, groupInfo[1], "",
					groupInfo[0], Color.parseColor("#da500e"));
			spanStr.setSpan(span, 0, spanStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			output.replace(startIndex, stopIndex, spanStr);
		}
	}

	/**
	 * @desc <pre>解析饭圈标签</pre>
	 * @author Weiliang Hu
	 * @date 2013-12-17
	 * @param cs
	 * @param isInsertGroup 是否插入饭圈标签
	 * @param mContext
	 * @return
	 */
	public static String convertToMsg(CharSequence cs, Context mContext, boolean isInsertGroup) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(cs);
		Pattern sinaPatten = Pattern.compile(Constants.REGULAR_GROUP_INFO, Pattern.CASE_INSENSITIVE);
		ImageSpan[] spans = ssb.getSpans(0, cs.length(), ImageSpan.class);
		for (int i = 0; i < spans.length; i++) {
			ImageSpan span = spans[i];
			String group = span.getSource();
			int a = ssb.getSpanStart(span);
			int b = ssb.getSpanEnd(span);

			Matcher matcher = sinaPatten.matcher(group);
			while (matcher.find()) {
				if (isInsertGroup) {
					ssb.replace(a, b, convertGroupTag(group));
				} else {
					ssb.replace(a, b, convertGroupText(group));
				}
			}
		}
		// ssb.clearSpans();
		return ssb.toString();
	}

	/** 转成服务器饭圈标签 */
	public static String convertGroupTag(String group) {
		String groupTmp = group.substring(1, group.length() - 1);
		String groupInfo[] = groupTmp.split(",");
		StringBuilder sb = new StringBuilder();
		sb.append("<group>").append(groupInfo[0]).append(",").append(groupInfo[1]).append("</group>");
		return sb.toString();
	}

	/** 直接转成文字 */
	public static String convertGroupText(String group) {
		String groupTmp = group.substring(1, group.length() - 1);
		String groupInfo[] = groupTmp.split(",");
		return groupInfo[1];
	}
}
