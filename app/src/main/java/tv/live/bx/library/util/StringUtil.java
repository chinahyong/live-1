package tv.live.bx.library.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.live.bx.R;

/**
 * @author
 */
public class StringUtil {
	private static final int OffsetBig = 256;
	private static final int OffsetSmall = 16;

	private static final int MOBIL_F_TAG = 2;
	private static final int MOBIL_L_TAG = 7;
	private static final int MOBIL_P_TAG_PREFIX = 6;
	private static final int MOBIL_N_TAG_PREFIX = 10;

	private static final String ENCRYPT_SALTE = "calendar";
	private static final String TAG = "StringUtil";
	/*主播开播话题正则表达式  '#文本#'*/
	private static final String HIGH_LIGNT_MATCHER = "\\#[^\\#]+\\#";
	/**
	 * 字符串去空格，回车，换行，制表符
	 *
	 * @param str 要修改的字符串
	 * @return 修改完成的字符串
	 */
	public static String replaceBlank(String str) {
		String dest = "";
		if (str != null) {
			Pattern p = Pattern.compile("\t|\r|\n");
			Matcher m = p.matcher(str);
			dest = m.replaceAll("");
		}
		return dest;
	}

	/**
	 * 将输入的字符串进行html编码
	 *
	 * @param input 输入的字符串
	 * @return html编码后的结果
	 */
	public static String htmEncode(String input) {
		if (null == input || "".equals(input)) {
			return input;
		}

		StringBuffer stringbuffer = new StringBuffer();
		int j = input.length();
		for (int i = 0; i < j; i++) {
			char c = input.charAt(i);
			switch (c) {
				case 60:
					stringbuffer.append("&lt;");
					break;
				case 62:
					stringbuffer.append("&gt;");
					break;
				case 38:
					stringbuffer.append("&amp;");
					break;
				case 34:
					stringbuffer.append("&quot;");
					break;
				case 169:
					stringbuffer.append("&copy;");
					break;
				case 174:
					stringbuffer.append("&reg;");
					break;
				case 165:
					stringbuffer.append("&yen;");
					break;
				case 8364:
					stringbuffer.append("&euro;");
					break;
				case 8482:
					stringbuffer.append("&#153;");
					break;
				case 13:
					if (i < j - 1 && input.charAt(i + 1) == 10) {
						stringbuffer.append("<br>");
						i++;
					}
					break;
				case 32:
					if (i < j - 1 && input.charAt(i + 1) == ' ') {
						stringbuffer.append(" &nbsp;");
						i++;
						break;
					}
				default:
					stringbuffer.append(c);
					break;
			}
		}
		return new String(stringbuffer.toString());
	}

	/**
	 * 判断字符串是否为null或者空字符串
	 *
	 * @param input 输入的字符串
	 * @return 如果为null或者空字符串，返回true；否则返回false
	 */
	public static boolean isNullOrEmpty(String input) {
		return null == input || "".equals(input);
	}

	/**
	 * 判断字符串中是否含有中文字符
	 *
	 * @param s
	 * @return
	 */
	public static boolean containChinese(String s) {
		if (null == s) {
			return false;
		}

		Pattern pattern = Pattern.compile(".*[\u4e00-\u9fbb]+.*");
		Matcher matcher = pattern.matcher(s);
		return matcher.matches();
	}

	/**
	 * 获取MD5加密后Hash字符串
	 *
	 * @param strOriginal 初始字符串
	 * @return MD5加密后Hash字符串
	 */
	public static String getMd5Hash(String strOriginal) {
		StringBuilder sbList = new StringBuilder();
		try {
			MessageDigest mMD5 = MessageDigest.getInstance("MD5");
			byte[] data = strOriginal.getBytes("utf-8");
			byte[] dataPWD = mMD5.digest(data);
			for (int offset = 0; offset < dataPWD.length; offset++) {
				int i = dataPWD[offset];
				if (i < 0) {
					i += OffsetBig;
				}
				if (i < OffsetSmall) {
					sbList.append("0");
				}
				sbList.append(Integer.toHexString(i));
			}
			return sbList.toString();
		} catch (NoSuchAlgorithmException e) {
			EvtLog.w(TAG, e);
		} catch (UnsupportedEncodingException e) {
			EvtLog.w(TAG, e);
		}
		return null;
	}

	/**
	 * 获取MD5加密后Hash字符串
	 *
	 * @param strOriginal 初始字符串
	 * @param strSalt 种子字符串
	 * @return MD5加密后Hash字符串
	 */
	public static String getMd5Hash(String strOriginal, String strSalt) {
		String mStrSalt;
		String mStrOriginal = strOriginal;
		// 如果调用未给Salt值,则默认
		if (strSalt == null) {
			mStrSalt = ENCRYPT_SALTE;
		} else {
			mStrSalt = strSalt;
		}
		mStrOriginal = mStrOriginal + mStrSalt;
		return getMd5Hash(mStrOriginal);
	}

	/**
	 * @param phoneNum 需要处理的手机号码
	 * @return String 处理后的手机号码
	 * @throws
	 * @Method: getProcessedDrawMobile
	 * @Description: 处理手机号码
	 */
	public static String getProcessedMobile(String phoneNum) {
		String processedDrawMobile = "";
		if (!StringUtil.isNullOrEmpty(phoneNum)) {
			// EvtLog.d(TAG, phoneNum);
			Pattern p1 = Pattern.compile("^((\\+{0,1}(0)*86){0,1})1[0-9]{10}");
			Matcher m1 = p1.matcher(phoneNum);
			if (m1.matches()) {
				Pattern p2 = Pattern.compile("^((\\+{0,1}(0)*86){0,1})");
				Matcher m2 = p2.matcher(phoneNum);
				StringBuffer sb = new StringBuffer();
				while (m2.find()) {
					m2.appendReplacement(sb, "");
				}
				m2.appendTail(sb);
				processedDrawMobile = sb.toString();

			} else {
				processedDrawMobile = phoneNum;
			}
		}
		return processedDrawMobile;
	}

	/**
	 * 获取当期是星期几（从星期天开始）
	 *
	 * @param weeknum 当前是第几天（0-6）
	 * @return 星期*
	 */
	public static String getDayOfWeek(int weeknum) {
		weeknum--;
		if (weeknum > 7) {
			weeknum = weeknum % 7;
		}

		if (weeknum < 0) {
			weeknum = -weeknum;
		}

		String[] weekArray = new String[]{"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
		return weekArray[weeknum];

	}

	/**
	 * 重载方法，获取当期是星期几
	 *
	 * @param c 日历
	 * @return 星期
	 */
	public static String getDayOfWeek(Calendar c) {
		return getDayOfWeek(c.get(Calendar.DAY_OF_WEEK));
	}

	/**
	 * 检查是不是中文
	 *
	 * @param str 检查字符串
	 * @return boolean 是否为中文
	 * @throws
	 * @Method: checkStringIsChinses
	 */
	public static boolean checkStringIsChinses(String str) {
		for (int i = 0; i < str.length(); i++) {
			String test = str.substring(i, i + 1);
			if (!test.matches("[\\u4E00-\\u9FA5]+")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 转换保留小数位 字符串
	 *
	 * @param i 小数位数
	 * @param numStr 数字字符串
	 * @return String
	 */
	public static String getDecimalFormat(int i, String numStr) {
		try {
			if (numStr != null && !"".equals(numStr)) {
				BigDecimal bd = new BigDecimal(numStr);
				bd = bd.setScale(i, BigDecimal.ROUND_HALF_UP);

				return bd.toString();
			} else {
				return "";
			}
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * 是否为email格式
	 *
	 * @param strEmail
	 * @return
	 */
	public static boolean isEmail(String strEmail) {
		if (isNullOrEmpty(strEmail))
			return false;
		String strPattern = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}\\@[a-zA-Z0-9]"
				+ "[a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+";
		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(strEmail);
		return m.matches();
	}

	/**
	 * 是否为中国移动号码
	 *
	 * @param phoneNumber
	 * @return
	 */
	public static boolean isChinaMobileNumber(String phoneNumber) {
		if (isNullOrEmpty(phoneNumber))
			return false;
		return phoneNumber.matches("^((86)?13)[4-9][0-9]{8}$|^((86)?15)[012789][0-9]{8}$|^((86)?18)"
				+ "[2378][0-9]{8}$|^((86)?14)[7][0-9]{8}$");
	}

	/**
	 * URL检查<br>
	 * <br>
	 *
	 * @param pInput 要检查的字符串<br>
	 * @return boolean 返回检查结果<br>
	 */
	public static boolean checkUrl(String pInput) {
		if (isNullOrEmpty(pInput)) {
			return false;
		}
		return pInput.matches("[a-zA-z]+://[^\\s]*");
	}

	/**
	 * 获得一个UUID
	 *
	 * @return String UUID
	 */
	public static String getUUID() {
		String uuid = UUID.randomUUID().toString();
		return uuid;
	}

	/**
	 * 字符串转换整型值
	 *
	 * @param numStr
	 * @return
	 */
	public static int getIntValue(String numStr) {
		try {
			return Integer.valueOf(numStr).intValue();
		} catch (Exception e) {
			EvtLog.e(TAG, e);
			return 0;
		}
	}

	/**
	 * 字符串转换长整型值
	 *
	 * @param numStr
	 * @return
	 */
	public static long getLongValue(String numStr) {
		try {
			return Long.valueOf(numStr).longValue();
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * 获取邮箱地址前缀
	 *
	 * @param strEmail
	 * @return
	 */
	public static String getEmailPrefix(String strEmail) {
		if (!isEmail(strEmail))
			return "";

		int index = strEmail.indexOf("@");
		if (index == -1) {
			return strEmail;
		}
		return strEmail.substring(0, index);
	}

	/**
	 * 是否为139邮箱
	 *
	 * @param strEmail
	 * @return
	 */
	public static boolean is139Email(String strEmail) {
		return isEmail(strEmail) && strEmail.toLowerCase().endsWith("@139.com");
	}

	// 顺序表
	static String orderStr = "";

	static {
		for (int i = 33; i < 127; i++) {
			orderStr += Character.toChars(i)[0];
		}
	}

	/**
	 * 判断是否有顺序
	 */
	public static boolean isOrder(String str) {
		if (!str.matches("((\\d)|([a-z])|([A-Z]))+")) {
			return false;
		}
		return orderStr.contains(str);
	}

	/**
	 * @return 是否为相同字符
	 */
	public static boolean isSameChars(String str) {
		if (isNullOrEmpty(str)) {
			return true;
		}
		char[] chars = str.toCharArray();
		char c = chars[0];
		for (int i = 1; i < chars.length; i++) {
			if (c != chars[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param str
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String base64Decode(String str) {
		byte[] keys = "efeizao@sz".getBytes();
		byte[] arrays = Base64.decode(str, Base64.NO_WRAP);
		byte[] result = new byte[arrays.length];
		for (int i = 0; i < arrays.length; i++) {
			result[i] = (byte) (arrays[i] ^ keys[i % keys.length]);
		}
		return new String(result);
	}

	/**
	 * 半角转为全角
	 */
	public static String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}

	/**
	 * 解析开播时的输入话题 (#  #)字符串中的成对#
	 *
	 * @param context
	 * @param str
	 */
	public static void setHighLigntText(Context context, Editable str) {
		Pattern sinaPatten = Pattern.compile(HIGH_LIGNT_MATCHER, Pattern.CASE_INSENSITIVE);
		try {
			ForegroundColorSpan[] toRemoveSpans = str.getSpans(0, str.length(), ForegroundColorSpan.class);
			for (int i = 0; i < toRemoveSpans.length; i++)
				str.removeSpan(toRemoveSpans[i]);
			Matcher matcher = sinaPatten.matcher(str);
			while (matcher.find()) {
				String st = matcher.group();
				if (matcher.start() < 0) {
					continue;
				}
				str.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.a_text_color_da500e)), matcher.start(), matcher.start() + st.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Spannable setHighLigntText(Context context, String str) {
		Spannable span = new SpannableString(str);
		Pattern sinaPatten = Pattern.compile(HIGH_LIGNT_MATCHER, Pattern.CASE_INSENSITIVE);
		try {
			ForegroundColorSpan[] toRemoveSpans = span.getSpans(0, str.length(), ForegroundColorSpan.class);
			for (int i = 0; i < toRemoveSpans.length; i++)
				span.removeSpan(toRemoveSpans[i]);
			Matcher matcher = sinaPatten.matcher(str);
			while (matcher.find()) {
				String st = matcher.group();
				if (matcher.start() < 0) {
					continue;
				}
				span.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.a_text_color_da500e)), matcher.start(), matcher.start() + st.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return span;
	}

	/**
	 * 字符串集合转换为： ",," 以逗号隔开每个元素的字符串
	 *
	 * @param lists
	 * @return
	 */
	public static String strListToString(List<String> lists) {
		String result = lists.toString().replace("[", "").replace("]", "");
		return result;
	}
}