package com.appupdate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import com.framework.lang.Md5Coder;
import com.update.AppUpdateManager;

/**
 * Title: XXXX (类或者接口名称) Description: XXXX (简单对此类或接口的名字进行描述) Copyright:
 * @version 1.0
 */
public class AppUpdate {

	private static Map<String, AppUpdate> instance = new HashMap<String, AppUpdate>();
	private AUpdateObserver observer;
	private boolean isFinish = true;

	private AppUpdate(Context context, String packageName, String appName, Bitmap res_icon, String tickText,
			String downloadUrl, ActivityCallBack activityCallBack) {
		this.observer = new DefaultUpdateObserver(context, null, packageName, appName, res_icon, tickText, downloadUrl,
				true, AppUpdateManager.ACTION_UPDATE, activityCallBack);
	}

	public static synchronized AppUpdate getInstance(Context context, String packageName, String appName,
			Bitmap res_icon, String tickText, String downloadUrl, ActivityCallBack activityCallBack) {
		if (instance.get(packageName) == null) {
			instance.put(packageName, new AppUpdate(context, packageName, appName, res_icon, tickText, downloadUrl,
					activityCallBack));
		}
		return instance.get(packageName);
	}

	public void setObserver(AUpdateObserver observer) {
		this.observer = observer;
	}

	public static String getFileNamePath(String downloadUrl) {

		StringBuffer sb = new StringBuffer();

		sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());

		sb.append(File.separator);

		sb.append(String.format("%s.apk", Md5Coder.md5(downloadUrl)));

		return sb.toString();
	}

	public void startUpdate() {
		if (observer == null) {
			return;
		}
		if (!android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
			observer.noMiniSD();
			return;
		}

		Log.i("qinqiu", "isFinish=====" + isFinish);

		if (!isFinish) {
			observer.noFinish();
			return;
		}
		new Thread(new UpdateRunnable()).start();
	}

	private class UpdateRunnable implements Runnable {

		/**
		 * 下载应用，并将应用的状态通知到观察者，如果应用之前已经下载完成，则不重新下载
		 */
		@Override
		public void run() {
			Log.i("qinqiu", "UpdateRunnable");
			if (observer == null) {
				return;
			}
			observer.start();
			isFinish = false;
			String downloadUrl = observer.getDownloadUrl();
			// String fileName = String
			// .format("%s.apk", Md5Coder.md5(downloadUrl));
			// Log.i("liaoguang", "downloadUrl============"+downloadUrl);
			BufferedInputStream bis = null;
			// BufferedOutputStream bos = null;
			// File file = new File(Environment.getExternalStorageDirectory(),
			// fileName);
			File file = new File(getFileNamePath(downloadUrl));
			// if (file.exists()) {
			// Log.i("liaoguang", "文件存在");
			// } else {
			//
			// Log.i("liaoguang", "文件不存在");
			// }
			long localFileSize = file.length();
			// Log.i("liaoguang", localFileSize + "");
			boolean isError = false;
			HttpURLConnection conn = null;
			RandomAccessFile oSavedFile = null;
			try {
				URL url = new URL(downloadUrl);
				conn = (HttpURLConnection) url.openConnection();
				// 设置User-Agent
				conn.setRequestProperty("User-Agent", "NetFox");
				// 设置断点续传的开始位置
				conn.setRequestProperty("RANGE", "bytes=" + localFileSize + "-");
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5000); // 5秒
				conn.setReadTimeout(10000); // 10秒
				conn.setDoInput(true);
				conn.setDoOutput(false);
				conn.setUseCaches(false);
				// Log.i("liaoguang",
				// "conn.getResponseCode()======="
				// + conn.getResponseCode());
				if (conn.getResponseCode() == 206) {
					bis = new BufferedInputStream(conn.getInputStream());
					long length = conn.getContentLength();
					// Log.i("liaoguang", "length====="+length);
					// file = new
					// File(Environment.getExternalStorageDirectory(),
					// fileName);
					// Log.d("app-update", String.format("filePath:%s",
					// file.getAbsolutePath()));

					// if (!observer.isRedownloadIfExists() && file.exists()
					// && file.length() == length) {
					// return;
					// }
					// if (file.exists() && file.length() == length) {
					// Log.i("liaoguang", "文件已存在，并且已下载完成");
					// return;
					// }

					// bos = new BufferedOutputStream(new
					// FileOutputStream(file));
					oSavedFile = new RandomAccessFile(file, "rw");
					oSavedFile.seek(localFileSize);

					byte[] buf = new byte[4 * 1024];
					int ch = -1;
					long downloaded = 0;
					int timediff = 500;
					long time1 = System.currentTimeMillis(), time2 = System.currentTimeMillis();
					// long flag = 0;
					while ((ch = bis.read(buf)) != -1) {

						// if (flag == 800) {
						//
						// Log.i("down1", "downloaded===" + downloaded);
						//
						// throw new SocketTimeoutException();
						//
						// }

						// bos.write(buf, 0, ch);
						oSavedFile.write(buf, 0, ch);
						downloaded += ch;

						// flag++;

						// Log.i("liaoguang",
						// String.format("%s:%s", downloaded, length));
						// Log.i("liaoguang",
						// "localFileSize===="+localFileSize);
						if (time2 - time1 > timediff || time1 == time2) {
							// Log.i("liaoguang",
							// String.format("%s:%s", downloaded+localFileSize,
							// length+localFileSize));
							// Log.i("liaoguang",(int)((downloaded+localFileSize)*100
							// / (length+localFileSize))+"");
							observer.downloading((int) (((downloaded + localFileSize) * 100) / (length + localFileSize)));
							time1 = time2;
						}
						time2 = System.currentTimeMillis();
					}
					// Log.i("liaoguang", "downloaded==="+downloaded);
				} else if (conn.getResponseCode() == 416) {

					if (file.exists()) {
						// Log.i("liaoguang", "文件已存在，并且已下载完成");
						return;
					}
				} else {
					// Log.i("liaoguang", "observer.error()");
					observer.error(-1);
					isError = true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				isError = true;
				observer.error(-1);
			} catch (IOException e) {
				int errorCode = -1;
				e.printStackTrace();

				Log.i("liaoguang", "IOException");
				if (e.toString().indexOf("space") != -1) {

					Log.i("liaoguang", "SD卡空间不足");
					errorCode = 2;
				} else if (e.toString().indexOf("Socket") != -1) {

					Log.i("liaoguang", "下载超时");
					errorCode = 1;

				}
				isError = true;
				observer.error(errorCode);

			} catch (Exception e) {
				e.printStackTrace();
				int errorCode = -1;
				Log.i("liaoguang", "Exception");
				Log.i("liaoguang", e.toString());
				if (e.toString().indexOf("Socket") != -1) {

					Log.i("liaoguang", "下载超时");
					errorCode = 1;

				}
				isError = true;
				observer.error(errorCode);
			} finally {
				if (conn != null) {
					conn.disconnect();

				}
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				// if (bos != null) {
				// try {
				// bos.flush();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// try {
				// bos.close();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// }
				if (!isError) {
					// Log.i("liaoguang", "observer.finish(file)");
					observer.finish(file);
				}
				if (oSavedFile != null) {
					try {
						oSavedFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				isFinish = true;
			}

		}
	}
}
