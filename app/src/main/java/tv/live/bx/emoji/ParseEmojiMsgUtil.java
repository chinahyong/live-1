package tv.live.bx.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.ui.VerticalImageSpan;

public class ParseEmojiMsgUtil {
	private static final String TAG = ParseEmojiMsgUtil.class.getSimpleName();
	private static final String REGEX_STR = "\\[e\\](.*?)\\[/e\\]";
	private static final String REGEX_STR_NEW = "\\[(:?\\d|[a-z]){4,5}\\]";
	/**
	 * 表情中文描述
	 */
	private static final String REGEX_STR_CHINESE = "\\[(.*?)\\]";// "\\[(.*?)\\u4E00-\\u9FA5\\uF900-\\uFA2D\\]";

	/**
	 * 对spanableString进行正则判断，如果符合要求，则以表情图片代替
	 */
	public static void dealExpression(SpannableString spannableString, Pattern patten, int start)
			throws Exception {
		Matcher matcher = patten.matcher(spannableString);
		while (matcher.find()) {
			String key = matcher.group();
			Log.d("ParseEmojiMsgUtil key", key);
			if (matcher.start() < start) {
				continue;
			}
			if (MsgFaceUtils.facesMap.get(key) == null) {
				continue;
			}
			// Field field = R.drawable.class.getDeclaredField("emoji_"
			// + key.substring(key.indexOf("[") + 1, key.lastIndexOf("]")));
			// int resId = Integer.parseInt(field.get(null).toString());
			int resId = MsgFaceUtils.facesMap.get(key);
			if (resId != 0) {
				Drawable drawable = FeizaoApp.mContext.getResources().getDrawable(resId);
				drawable.setBounds(0, 0, (int) (FeizaoApp.mContext.getResources().getDimension(R.dimen.image_emoji_width)),
						(int) (FeizaoApp.mContext.getResources().getDimension(R.dimen.image_emoji_width)));
				VerticalImageSpan imageSpan = new VerticalImageSpan(drawable);
				int end = matcher.start() + key.length();
				spannableString.setSpan(imageSpan, matcher.start(), end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				if (end < spannableString.length()) {
					dealExpression(spannableString, patten, end);
				}
				break;
			}
		}
	}

	/**
	 * @param str
	 * @return
	 * @desc <pre>
	 * 解析字符串中的表情字符串替换成表情图片
	 * </pre>
	 * @author Weiliang Hu
	 * @date 2013-12-17
	 */
	public static SpannableString getExpressionString(CharSequence str) {
		SpannableString spannableString = new SpannableString(str);
		Pattern sinaPatten = Pattern.compile(REGEX_STR_CHINESE, Pattern.CASE_INSENSITIVE);
		try {
			dealExpression(spannableString, sinaPatten, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return spannableString;
	}

	/**
	 * @param cs
	 * @param mContext
	 * @return
	 * @desc <pre>表情解析,转成unicode字符</pre>
	 * @author Weiliang Hu
	 * @date 2013-12-17
	 * @deprecated
	 */
	public static CharSequence convertToMsg(CharSequence cs, Context mContext) {
		SpannableStringBuilder ssb = new SpannableStringBuilder(cs);
		ImageSpan[] spans = ssb.getSpans(0, cs.length(), ImageSpan.class);
		Pattern sinaPatten = Pattern.compile(REGEX_STR_NEW, Pattern.CASE_INSENSITIVE);
		for (int i = 0; i < spans.length; i++) {
			ImageSpan span = spans[i];
			String c = span.getSource();
			int a = ssb.getSpanStart(span);
			int b = ssb.getSpanEnd(span);
			Matcher matcher = sinaPatten.matcher(c);
			while (matcher.find()) {
				ssb.replace(a, b, convertUnicode(c));
			}
			// if (c.contains("[")) {
			// ssb.replace(a, b, convertUnicode(c));
			// }
		}
		// ssb.clearSpans();
		return ssb;
	}

	private static String convertUnicode(String emo) {
		emo = emo.substring(1, emo.length() - 1);
		if (emo.length() < 6) {
			return new String(Character.toChars(Integer.parseInt(emo, 16)));
		}
		String[] emos = emo.split("_");
		char[] char0 = Character.toChars(Integer.parseInt(emos[0], 16));
		char[] char1 = Character.toChars(Integer.parseInt(emos[1], 16));
		char[] emoji = new char[char0.length + char1.length];
		for (int i = 0; i < char0.length; i++) {
			emoji[i] = char0[i];
		}
		for (int i = char0.length; i < emoji.length; i++) {
			emoji[i] = char1[i - char0.length];
		}
		return new String(emoji);
	}

}
