package com.bixin.bixin.common;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.bixin.bixin.config.UserInfoConfig;
import com.bixin.bixin.model.UserActionBean;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import com.framework.net.impl.CallbackDataHandle;

/**
 * Created by Live on 2017/2/19.
 */

public class OperationHelper {
	// 统计用户操作文件名称 保存地址：/data/data/包名/files
	public static final String USER_ACTION_FILE_NAME = "user_action.txt";
	// 用户操作文件达到 10B 10240个字节上上传
	public static final int USER_ACTION_FILE_SIZE_MAX = 1024 * 10;

	private static final Task mTask = new Task();

	/**
	 * 统计事件，默认会统计Uid
	 *
	 * @param context
	 * @param eventId
	 * @param eventData
	 */
	public static void onEvent(Context context, String eventId, Map<String, String> eventData) {
		if (eventData == null) {
			eventData = new HashMap<>();
		}
		// 如果本地存在uid，就进行上传
		if (!TextUtils.isEmpty(UserInfoConfig.getInstance().id)) {
			eventData.put("uid", UserInfoConfig.getInstance().id);
		}
		mTask.onEvent(context, eventId, eventData);
	}

	/**
	 * 统计事件，默认会统计Uid
	 *
	 * @param context
	 * @param eventId
	 */
	public static void onEvent(Context context, String eventId) {
		onEvent(context, eventId, null);
	}

	/**
	 * 文件大于0 直接上传
	 *
	 * @param context
	 */
	public static void onEventEnd(final Context context) {
		mTask.onEvent(context, null, new HashMap<String, String>());
	}

	/**
	 * 写入用户操作到本地文件
	 *
	 * @param t 事件发生时间
	 * @param e 事件名称
	 * @param c 扩展信息
	 *          此方法需要同步调用
	 */
	private static void saveFile(Context context, String t, String e, Map<String, String> c) {
		UserActionBean bean = new UserActionBean(t, e, c);
		ObjectMapper mapper = new ObjectMapper();
		FileOutputStream out;
		BufferedWriter writer = null;
		try {
			// 对象转换为json字符串
			JsonGenerator generator = mapper.getJsonFactory().createJsonGenerator(System.out, JsonEncoding.UTF8);
			generator.writeObject(bean);
			// 文件写入
			out = context.openFileOutput(USER_ACTION_FILE_NAME, Context.MODE_APPEND);
			writer = new BufferedWriter(new OutputStreamWriter(out));
			writer.write(mapper.writeValueAsString(bean) + "\r\n"); //换行
			writer.flush();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * 获取文件大小
	 *
	 * @return
	 */
	private static int getFileSize(Context context) {
		FileInputStream in = null;        //输入流
		int size = 0;        //文件大小
		try {
			in = context.openFileInput(USER_ACTION_FILE_NAME);
			size = in.available();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			return size;
		}
	}


	/**
	 * 读取用户操作到本地文件
	 * 此方法需要同步调用
	 *
	 * @return 返回加密后的结果
	 */
	private static String readFile(Context context) {
		FileInputStream in = null;
		BufferedReader reader = null;
		StringBuilder sb = new StringBuilder();
		String line = "";
		try {
			in = context.openFileInput(USER_ACTION_FILE_NAME);
			reader = new BufferedReader(new InputStreamReader(in));
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\r\n");
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return sb.toString();
		}
	}

	/**
	 * 请求服务器
	 *
	 * @date
	 */
	private static void request(final Context context) {
		String content = readFile(context);
		String date = String.valueOf(System.currentTimeMillis() / 1000);
		BusinessUtils.reportUserAction(context, AESHelper.encrypt(content, date), date, new CallbackDataHandle() {
			@Override
			public void onCallback(boolean success, String errorCode, String errorMsg, Object result) {
				if (success) {
					// 上传成功移除文件
					context.deleteFile(USER_ACTION_FILE_NAME);
				}
			}
		});
	}

	private static class Task {

		public void onEvent(final Context context, final String eventId, Map<String, String> eventData) {
			new AsyncTask<Map<String, String>, Void, Integer>() {//开启一个多线程池，大小为cpu数量+1

				@Override
				protected Integer doInBackground(Map<String, String>... params) {
					if (!TextUtils.isEmpty(eventId)) {
						String date = String.valueOf(System.currentTimeMillis() / 1000);
						// 写入文件到本地
						saveFile(context, date, eventId, params[0]);
					}
					// 获取文件大小
					Integer size = getFileSize(context);
					return size;
				}

				@Override
				protected void onPostExecute(Integer result) {
					// 文件超过一定大小上传到服务器
					if (result >= USER_ACTION_FILE_SIZE_MAX && !TextUtils.isEmpty(eventId)) {
						request(context);
					} else if (result > 0 && TextUtils.isEmpty(eventId)) {
						request(context);
					}
				}
			}.execute(eventData);
		}
	}
}
