package com.bixin.bixin.library.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import tv.live.bx.R;

/**
 * Title: DateUtil Description: 时间日期工具类 Copyright: Copyright (c) 2008
 *
 * @version 1.0
 * @CreateDate 2013-5-28 下午3:52:11
 */
public class DateUtil {
	private static final String TAG = "DateUtil";

	public static final long DAY_TIMEMILLS = 1000 * 60 * 60 * 24L;

	public static final long HOUR_TIMEMILLS = 1000 * 60 * 60L;

	public static final long MINUTE_TIMEMILLS = 1000 * 60L;

	/**
	 * 格式 yyyy-MM-dd HH:mm:ss
	 */
	public static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 格式 yyyyMMddHHmmss
	 */
	public static final String DATE_FORMAT_1 = "yyyyMMddHHmmss";

	/**
	 * 格式 yyyy-MM-dd HH:mm
	 */
	public static final String DATE_FORMAT_2 = "yyyy-MM-dd HH:mm";

	/**
	 * 格式 yyyy-MM-dd
	 */
	public static final String DATE_FORMAT_3 = "yyyy-MM-dd";

	/**
	 * 格式 yyyy年MM月dd日
	 */
	public static final String DATE_FORMAT_4 = "yyyy年MM月dd日";

	/**
	 * 格式 HH:mm:ss
	 */
	public static final String DATE_FORMAT_5 = "HH:mm:ss";

	public static final String DATE_FORMAT_6 = "yyyyMMdd.hhmmss";

	public static final String DATE_FORMAT_7 = "yyyy/MM/dd HHmm";

	public static final String DATE_FORMAT_8 = "HH:mm";

	public static final String DATE_FORMAT_Hm = "HH点mm分";

	public static final String DATE_FORMAT_MS = "mm:ss";


	/**
	 * 格式 yyyy年MM月dd日 星期X
	 */
	public static final String DATE_FORMAT_9 = "yyyy年MM月dd日 EEE";

	/**
	 * 格式 yyyy年MM月dd日 HH:mm 星期X
	 */
	public static final String DATE_FORMAT_10 = "yyyy年MM月dd日 HH:mm EEE";

	public static final String DATE_FORMAT_11 = "MM月dd日 HH小时mm分";
	public static final String DATE_FORMAT_12 = "yyyy年MM月dd日";
	public static final String DATE_FORMAT_13 = "MM月dd日";
	public static final String DATE_FORMAT_14 = "yyyy.MM.dd";

	/**
	 * 常用的格式化对象
	 */
	public static final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_DEFAULT, Locale.getDefault());

	/**
	 * 年月日时分秒
	 */
	public static final SimpleDateFormat sdf1 = new SimpleDateFormat(DATE_FORMAT_1, Locale.getDefault());

	/**
	 *
	 */
	public static final SimpleDateFormat sdf5 = new SimpleDateFormat(DATE_FORMAT_2, Locale.getDefault());

	/**
	 * 年-月-日
	 */
	public static final SimpleDateFormat sdf2 = new SimpleDateFormat(DATE_FORMAT_3, Locale.getDefault());

	/**
	 * 时:分:秒
	 */
	public static final SimpleDateFormat sdf3 = new SimpleDateFormat(DATE_FORMAT_5, Locale.getDefault());

	/**
	 * 时:分
	 */
	public static final SimpleDateFormat sdf8 = new SimpleDateFormat(DATE_FORMAT_8, Locale.getDefault());

	/**
	 * 时:分
	 */
	public static final SimpleDateFormat sdfHm = new SimpleDateFormat(DATE_FORMAT_Hm, Locale.getDefault());

	/**
	 * MM月dd日 HH小时mm分
	 */
	public static final SimpleDateFormat sdf11 = new SimpleDateFormat(DATE_FORMAT_11, Locale.getDefault());
	/**
	 * yyyy年MM月dd日 HH小时mm分
	 */
	public static final SimpleDateFormat sdf12 = new SimpleDateFormat(DATE_FORMAT_12, Locale.getDefault());

	/**
	 * MM月dd日
	 */
	public static final SimpleDateFormat sdf13 = new SimpleDateFormat(DATE_FORMAT_13, Locale.getDefault());

	/**
	 *
	 */
	public static final SimpleDateFormat sdf4 = new SimpleDateFormat(DATE_FORMAT_6, Locale.getDefault());


	/**
	 * mm：ss
	 */
	public static final SimpleDateFormat sdfmmss = new SimpleDateFormat(DATE_FORMAT_MS, Locale.getDefault());

	/**
	 * 时间差
	 */
	private static long span = 0;

	/**
	 * getDate 获取当前时间
	 */
	public static Date getDate() {
		Date nowAndroid = new Date();
		long nowPlant = nowAndroid.getTime() + span;

		Date nowPlantDate = new Date(nowPlant);

		return nowPlantDate;
	}

	/**
	 * getSystemCalendar 获取当前Calendar
	 */
	public static Calendar getSystemCalendar() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(getDate());
		return cal;
	}

	/**
	 * getAndroidDate 获取本地时间
	 */
	public static Date getAndroidDate(Date plantDate) {
		long nowAndroid = plantDate.getTime() - span;
		Date androidDate = new Date(nowAndroid);
		return androidDate;
	}

	/**
	 * getDateString 获取字符串形式的当前时间
	 */
	public static String getSystemDateTimeString() {
		Date date = getDate();
		return sdf.format(date);
	}

	/**
	 * 时间转换
	 */

	public static String getTimeShort(long currentTime) {
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_MS);
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * getDateString 获取日期字符串
	 */
	public static String getDateString(Date date) {
		return sdf.format(date);
	}

	public static String getThisDateToString(String format) {
		Date date = getDate();

		if (format == null) {
			format = DATE_FORMAT_DEFAULT;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat(format);

		return dateFormat.format(date);
	}

	/**
	 * getDateToString 根据指定日期格式获取对应字符串
	 */
	public static String getDateToString(Date date, String format) {
		if (date == null) {
			return null;
		}

		if (format == null) {
			format = DATE_FORMAT_DEFAULT;
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat(format);

		return dateFormat.format(date);

	}

	public static Date getDefaultDateByParse(String dateTime) {
		Date date = null;

		try {
			date = sdf.parse(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static long getSpan() {
		return span;
	}

	public static void setSpan(long span) {
		DateUtil.span = span;
	}

	/**
	 * getDayOfWeek(这里用一句话描述这个方法的作用) 计算今天是星期几
	 */
	public static int getDayOfWeek() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getDate());
		return calendar.get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * getBeginAndEndDateThisWeek(这里用一句话描述这个方法的作用) 获取本周的起始截止日期
	 */
	public static List<Date> getBeginAndEndDateThisWeek() {
		List<Date> ret = new ArrayList<Date>();
		Calendar calendar = Calendar.getInstance();
		int weekday = calendar.get(7) - 2;
		calendar.add(5, -weekday);
		ret.add(calendar.getTime());
		calendar.add(5, 6);
		ret.add(calendar.getTime());
		return ret;
	}

	/**
	 * 获取相对于今天的日期
	 *
	 * @param rel
	 * @return
	 */
	public static Date getRelativeDate(int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getDate());
		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + num);
		return calendar.getTime();
	}

	/**
	 * 获取相对于指定日期的天数
	 *
	 * @param relDate
	 * @param num
	 * @return
	 */
	public static Date getRelativeDate(Date relDate, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(relDate);
		calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + num);
		return calendar.getTime();
	}

	/**
	 * 时间字符串转换长整型
	 *
	 * @param dateStr 字符串
	 * @param format  格式
	 * @return
	 */
	public static long fmtStringToTimeMills(String dateStr, SimpleDateFormat format) {
		if (StringUtil.isNullOrEmpty(dateStr))
			return 0;
		try {
			Date date = format.parse(dateStr);
			return date.getTime();
		} catch (Exception e) {
			EvtLog.e(TAG, e);
			return 0;
		}
	}

	/**
	 * 长整型转换成字符串
	 *
	 * @param milliseconds
	 * @param format
	 * @return
	 */
	public static String fmtTimeMillsToString(long milliseconds, SimpleDateFormat format) {
		try {
			Date date = new Date(milliseconds);
			return format.format(date);
		} catch (Exception e) {
			EvtLog.e(TAG, e);
			return "";
		}
	}

	/**
	 * 指定SimpleDateFormat对象
	 *
	 * @param format
	 * @return
	 */
	public static SimpleDateFormat getSimpleDateFormat(String format) {
		return new SimpleDateFormat(format, Locale.getDefault());
	}

	/**
	 * 返回时间长整型
	 *
	 * @param date "endDate":"2013-06-22"
	 * @param time "endTime":1830
	 * @return
	 */
	public static long fmtServerDateStringToTimemills(String date, String time) {
		if (StringUtil.isNullOrEmpty(date) || StringUtil.isNullOrEmpty(time))
			return 0;
		try {
			int timeValue = Integer.valueOf(time).intValue();
			long timeMills = (timeValue / 100 * HOUR_TIMEMILLS) + (timeValue % 100 * MINUTE_TIMEMILLS);
			long dateMills = fmtStringToTimeMills(date, sdf2);
			return timeMills + dateMills;
		} catch (Exception e) {
			EvtLog.e(TAG, e);
			return 0;
		}
	}

	/**
	 * 将时间戳转化成形如 0709 或 09 的字符串
	 *
	 * @param milliseconds
	 * @param hasMonth
	 * @param hasDay
	 * @return
	 */
	public static String fmtTimemillsToServerDateflag(long milliseconds, boolean hasMonth, boolean hasDay) {
		StringBuffer sb = new StringBuffer();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliseconds);
		String monthString = "";
		String dayString = "";
		if (hasMonth) {
			if (calendar.get(Calendar.MONTH) + 1 < 10)
				monthString = "0" + (calendar.get(Calendar.MONTH) + 1);
			else
				monthString = "" + (calendar.get(Calendar.MONTH) + 1);
		}
		if (hasDay) {
			if (calendar.get(Calendar.DAY_OF_MONTH) < 10)
				dayString = "0" + calendar.get(Calendar.DAY_OF_MONTH);
			else
				dayString = "" + calendar.get(Calendar.DAY_OF_MONTH);
		}
		return sb.append(monthString).append(dayString).toString();
	}

	// 倒计时类型（可继续添加其他：天：小时：分：秒，等）
	public static final int TYPE_SECOND_MILLION = 0;    //秒：毫秒
	public static final int TYPE_MINUTE_SECOND = 1;        //分：秒
	public static final int TYPE_HOUR_MINUTE = 2;        //小时：分
	public static final int TYPE_DAY_HOUR = 3;            //天：小时

	public static final String TIME_SIGN = ":";
	public static final String TIME_SIGN_POINT = ".";

	/**
	 * 倒计时：间隔为":"
	 *
	 * @param secTime
	 * @param type
	 * @return
	 */
	public static String secToTime(long secTime, int type) {
		return secToTime(secTime, type, TIME_SIGN);
	}

	/**
	 * 倒计时 毫秒
	 *
	 * @param secTime 单位毫秒
	 * @return
	 */
	public static String secToTime(long secTime, int type, String sign) {
		int se = 1000;
		int mi = se * 60;
		int hh = mi * 60;
		int dd = hh * 24;

		long day = secTime / dd;
		long hour = (secTime - day * dd) / hh;
		long minute = (secTime - day * dd - hour * hh) / mi;
		long second = (secTime - day * dd - hour * hh - minute * mi) / se;
		long milliSecond = (secTime - day * dd - hour * hh - minute * mi - second * 1000) / 10;

		String strDay = day < 10 ? "0" + day : "" + day; //天
		String strHour = hour < 10 ? "0" + hour : "" + hour;//小时
		String strMinute = minute < 10 ? "0" + minute : "" + minute;//分钟
		String strSecond = second < 10 ? "0" + second : "" + second;//秒
		String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;//毫秒
		switch (type) {
			case TYPE_SECOND_MILLION:
				return strSecond + sign + strMilliSecond;
			case TYPE_MINUTE_SECOND:
				return strMinute + sign + strSecond;
			case TYPE_HOUR_MINUTE:
				return strHour + sign + strMinute;
			case TYPE_DAY_HOUR:
				return strDay + sign + strHour;
			default:
				break;
		}
		return "";
	}

	/**
	 * 1分钟内，显示刚刚; 1小时内，显示几分钟前; 8小时内，显示几小时前 ; 24小时内，显示几点几分; 1年内，显示月日; 其他，显示年月日
	 * 将时间戳转化成形如 "刚刚","一个小时前 "
	 *
	 * @param seconds 秒
	 * @return
	 */
	public static String fmtTimemillsToTextFormat(long seconds) {
		Date now = new Date();

		long time = now.getTime() / 1000 - seconds;
		// 如果小于一分钟
		if (time < 60) {
			return "刚刚";
		}
		// 如果小于一个小时钟
		else if (time < 60 * 60) {
			int t = (int) time / (60);
			return t + "分钟前";
		}
		// 如果小于6个小时钟
		else if (time < 24 * 60 * 60) {
			int t = (int) time / (60 * 60);
			return t + "小时前";
		}
		// 如果小于24个小时钟
		else if (time < 48 * 60 * 60) {
			Date same = new Date(seconds * 1000);
			String t = sdfHm.format(same);
			return "昨天";
		}
		// 如果小於72小時大於24小時
		else if (time < 72 * 60 * 60) {
			Date same = new Date(seconds * 1000);
			String t = sdfHm.format(same);
			return "前天";
		} else {
			return sdf12.format(new Date(seconds * 1000));
		}
//		// 如果小于1年
//		else if (time < 365 * 24 * 60 * 60 * 1000) {
//			return sdf13.format(new Date(seconds * 1000));
//		}
//		// 如果大于1年
//		else {
//			return sdf12.format(new Date(seconds * 1000));
//		}
	}

	public static boolean oneDayDifference(Date oldDate, Date newDate) {
		Calendar oldCal = Calendar.getInstance();
		Calendar newCal = Calendar.getInstance();

		oldCal.setTime(oldDate);
		newCal.setTime(newDate);
		return Math.abs(newCal.get(Calendar.DAY_OF_YEAR) - oldCal.get(Calendar.DAY_OF_YEAR)) == 1;
	}

	/**
	 * 是否同一天
	 */
	public static boolean isSameDay(Date date, Date sameDate) {

		if (null == date || null == sameDate) {
			return false;
		}
		Calendar nowCalendar = Calendar.getInstance();
		nowCalendar.setTime(sameDate);
		Calendar dateCalendar = Calendar.getInstance();
		dateCalendar.setTime(date);
		return nowCalendar.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR)
				&& nowCalendar.get(Calendar.MONTH) == dateCalendar.get(Calendar.MONTH)
				&& nowCalendar.get(Calendar.DATE) == dateCalendar.get(Calendar.DATE);
	}

	/**
	 * 将时间戳转为指定日期格式
	 *
	 * @param unixTime
	 * @param formator
	 * @return
	 */
	public static String getFormatTime(long unixTime, SimpleDateFormat formator) {
		Long timestamp = (unixTime) * 1000;
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		date.setTime(timestamp);
		String str_date = formator.format(cal.getTime());
		return str_date;
	}

	public static int getCurYear() {
		Calendar c = Calendar.getInstance();
		return c.get(Calendar.YEAR);
	}

	/**
	 * 秒转成天
	 *
	 * @param second
	 * @return
	 */
	public static int secondToDay(long second) {
		double daySecond = 24 * 60 * 60;
		return (int) Math.ceil(second / daySecond);
	}

	/**
	 * 根据日期获取对应星座
	 *
	 * @param month
	 * @param day
	 * @return
	 */
	public static int getConstellation(int month, int day) {
		if ((month == 1 && day > 19) || (month == 2 && day < 19)) {
			return R.string.setting_account_aquarius;
		} else if ((month == 2 && day > 18) || (month == 3 && day < 21)) {
			return R.string.setting_account_pisces;
		} else if ((month == 3 && day > 20) || (month == 4 && day < 20)) {
			return R.string.setting_account_aries;
		} else if ((month == 4 && day > 19) || (month == 5 && day < 21)) {
			return R.string.setting_account_taurus;
		} else if ((month == 5 && day > 20) || (month == 6 && day < 22)) {
			return R.string.setting_account_gemini;
		} else if ((month == 6 && day > 21) || (month == 7 && day < 23)) {
			return R.string.setting_account_cancer;
		} else if ((month == 7 && day > 22) || (month == 8 && day < 23)) {
			return R.string.setting_account_leo;
		} else if ((month == 8 && day > 22) || (month == 9 && day < 23)) {
			return R.string.setting_account_virgo;
		} else if ((month == 9 && day > 22) || (month == 10 && day < 24)) {
			return R.string.setting_account_libra;
		} else if ((month == 10 && day > 23) || (month == 11 && day < 23)) {
			return R.string.setting_account_scorpio;
		} else if ((month == 11 && day > 22) || (month == 12 && day < 22)) {
			return R.string.setting_account_sagittarius;
		} else if ((month == 12 && day > 21) || (month == 1 && day < 20)) {
			return R.string.setting_account_capricorn;
		}
		return 0;
	}

}
