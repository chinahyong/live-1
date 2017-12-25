package com.bixin.bixin.library.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface.OnDismissListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

import java.util.Calendar;

import tv.live.bx.R;


/**
 * 日期时间选择控件 使用方法： private EditText inputDate;//需要设置的日期时间文本编辑框 private String
 * 
 * @author
 */
public class TimePickerDialogUtil {
	private TimePicker timePicker;

	private Dialog ad;
	private String dateTime;
	private String initDateTime;
	private Activity activity;

	private OnDismissListener mOnDismissListener;

	private OnClickListener mOnClickListener;

	/**
	 * 日期时间弹出选择框构造函数
	 * 
	 * @param activity ：调用的父activity
	 * @param initDateTime 初始日期时间值，作为弹出窗口的标题和日期时间初始值
	 */
	public TimePickerDialogUtil(Activity activity, String initDateTime) {
		this.activity = activity;
		this.initDateTime = initDateTime;

	}

	public void init(TimePicker datePicker) {
		Calendar calendar = Calendar.getInstance();
		if (!(null == initDateTime || "".equals(initDateTime))) {
			calendar = this.getCalendarByInintData(initDateTime);
		} else {
			initDateTime = calendar.get(Calendar.YEAR) + "年" + calendar.get(Calendar.MONTH) + "月"
					+ calendar.get(Calendar.DAY_OF_MONTH) + "日 ";
		}
		// 初始化时间
		calendar.setTimeInMillis(System.currentTimeMillis());
		timePicker.setIs24HourView(true);
		timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
		dateTime = calendar.get(Calendar.HOUR_OF_DAY) + "." + calendar.get(Calendar.MINUTE);
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				dateTime = hourOfDay + "." + minute;
			}
		});

	}

	public void setOnDismissListener(OnDismissListener onDismissListener) {
		this.mOnDismissListener = onDismissListener;
	}

	public void setOnClickListener(OnClickListener onClickListener) {
		this.mOnClickListener = onClickListener;
	}

	/**
	 * 弹出日期时间选择框方法
	 * 
	 * @param inputDate :为需要设置的日期时间文本编辑框
	 * @return
	 */
	@SuppressLint("InflateParams")
	public Dialog dateTimePicKDialog(final TextView inputDate) {
		LinearLayout dateTimeLayout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.time_picker_dialog,
				null);
		timePicker = (TimePicker) dateTimeLayout.findViewById(R.id.datepickert);

		init(timePicker);

		Button mPositive = (Button) dateTimeLayout.findViewById(R.id.positive);
		Button mNegative = (Button) dateTimeLayout.findViewById(R.id.negative);
		// ad = new
		// AlertDialog.Builder(activity).setTitle(initDateTime).setView(dateTimeLayout)
		// .setPositiveButton("设置", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int whichButton) {
		// inputDate.setText(dateTime);
		// }
		// }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int whichButton) {
		// }
		// }).show();

		ad = new Dialog(activity, R.style.base_dialog);
		ad.setContentView(dateTimeLayout);
		mPositive.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				inputDate.setText(dateTime);
				ad.dismiss();
				if (mOnClickListener != null)
					mOnClickListener.onClick(arg0);
			}
		});

		mNegative.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ad.dismiss();
			}
		});

		if (mOnDismissListener != null) {
			ad.setOnDismissListener(mOnDismissListener);
		}

		ad.show();

		// onDateChanged(null, 0, 0, 0);
		return ad;
	}

	@SuppressLint("SimpleDateFormat")
	// public void onDateChanged(DatePicker view, int year, int monthOfYear, int
	// dayOfMonth) {
	// // 获得日历实例
	// Calendar calendar = Calendar.getInstance();
	//
	// calendar.set(timePicker.getYear(), timePicker.getMonth(),
	// timePicker.getDayOfMonth());
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	//
	// dateTime = sdf.format(calendar.getTime());
	// ad.setTitle(dateTime);
	// }
	/**
	 * 实现将初始日期时间2012年07月02日 16:45 拆分成年 月 日 时 分 秒,并赋值给calendar
	 * 
	 * @param initDateTime 初始日期时间值 字符串型
	 * @return Calendar
	 */
	private Calendar getCalendarByInintData(String initDateTime) {
		Calendar calendar = Calendar.getInstance();

		// 将初始日期时间2012年07月02日 16:45 拆分成年 月 日 时 分 秒
		String date = spliteString(initDateTime, "日", "index", "front"); // 日期

		String yearStr = spliteString(date, "年", "index", "front"); // 年份
		String monthAndDay = spliteString(date, "年", "index", "back"); // 月日

		String monthStr = spliteString(monthAndDay, "月", "index", "front"); // 月
		String dayStr = spliteString(monthAndDay, "月", "index", "back"); // 日

		int currentYear = Integer.valueOf(yearStr.trim()).intValue();
		int currentMonth = Integer.valueOf(monthStr.trim()).intValue() - 1;
		int currentDay = Integer.valueOf(dayStr.trim()).intValue();

		calendar.set(currentYear, currentMonth, currentDay);
		return calendar;
	}

	/**
	 * 截取子串
	 * 
	 * @param srcStr 源串
	 * @param pattern 匹配模式
	 * @param indexOrLast
	 * @param frontOrBack
	 * @return
	 */
	public static String spliteString(String srcStr, String pattern, String indexOrLast, String frontOrBack) {
		String result = "";
		int loc = -1;
		if (indexOrLast.equalsIgnoreCase("index")) {
			loc = srcStr.indexOf(pattern); // 取得字符串第一次出现的位置
		} else {
			loc = srcStr.lastIndexOf(pattern); // 最后一个匹配串的位置
		}
		if (frontOrBack.equalsIgnoreCase("front")) {
			if (loc != -1)
				result = srcStr.substring(0, loc); // 截取子串
		} else {
			if (loc != -1)
				result = srcStr.substring(loc + 1, srcStr.length()); // 截取子串
		}
		return result;
	}

}
