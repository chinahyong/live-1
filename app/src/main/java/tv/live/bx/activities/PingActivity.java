package tv.live.bx.activities;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.efeizao.bx.R;
import tv.live.bx.activities.base.BaseFragmentActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by Administrator on 2017/3/9.
 */

public class PingActivity extends BaseFragmentActivity {
	private String TAG = PingActivity.class.getSimpleName();
	Button btn_ping;
	EditText et_ip;
	TextView tv_show;

	String ipAddress;

	boolean flag = false;

	@Override
	protected int getLayoutRes() {
		return R.layout.activity_ping_layout;
	}

	@Override
	public void initWidgets() {
		btn_ping = (Button) findViewById(R.id.ping_btn);
		et_ip = (EditText) findViewById(R.id.edit_ip_address);
		tv_show = (TextView) findViewById(R.id.ping_info_tv);
		initTitle();
	}

	/**
	 * 初始化title信息
	 */
	@Override
	protected void initTitleData() {
		mTopTitleTv.setText(R.string.help);
		mTopBackLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	protected void setEventsListeners() {
		btn_ping.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startPing();
			}
		});
	}

	@Override
	protected void initData(Bundle savedInstanceState) {

	}

	@Override
	public void handleMessage(Message msg)// 接收消息的方法
	{
		// String str = (String) msg.obj;// 类型转化
		// tv_show.setText(str);// 执行
		switch (msg.what) {
			case 10:
				String resultmsg = (String) msg.obj;
				tv_show.append(resultmsg);
				break;
			case 11:
				btn_ping.setEnabled(true);
			default:
				break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		flag = false;
	}

	private void startPing() {
		ipAddress = et_ip.getText().toString();
		if (TextUtils.isEmpty(ipAddress)) {
			showTips("请输入IP地址");
			return;
		}

		flag = true;
		tv_show.setText("");
		btn_ping.setEnabled(false);

		String countCmd = " -c " + "4" + " ";
		String sizeCmd = " -s " + "64" + " ";
		String timeCmd = " -i " + "1" + " ";
		final String pingCmd = "ping" + countCmd + timeCmd + sizeCmd + ipAddress;

		Thread a = new Thread()// 创建子线程
		{
			public void run() {
				// for (int i = 0; i < 100; i++) {
				// try {
				// sleep(500);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// Message msg = new Message();// 创建消息类
				// msg.obj = "线程进度 ：" + i;// 消息类对象中存入消息
				// handler1.sendMessage(msg);// 通过handler对象发送消息
				// }

				Process process = null;
				BufferedReader successReader = null;
				BufferedReader errorReader = null;

				DataOutputStream dos = null;
				try {
					// 闃诲澶勭悊
					process = Runtime.getRuntime().exec(pingCmd);
					// dos = new DataOutputStream(process.getOutputStream());
					// status = process.waitFor();
					InputStream in = process.getInputStream();

					OutputStream out = process.getOutputStream();
					// success

					successReader = new BufferedReader(
							new InputStreamReader(in));

					// error
					errorReader = new BufferedReader(new InputStreamReader(
							process.getErrorStream()));

					String lineStr;
					while ((lineStr = successReader.readLine()) != null && flag) {
						Log.i(TAG, "====receive====:" + lineStr);

						Message msg = mHandler.obtainMessage();
						msg.obj = lineStr + "\r\n";
						msg.what = 10;
						msg.sendToTarget();

						if (lineStr.contains("packet loss")) {
							Log.i(TAG, "=====Message=====" + lineStr.toString());
							int i = lineStr.indexOf("received");
							int j = lineStr.indexOf("%");
							Log.i(TAG,
									"====丢包率====:"
											+ lineStr.substring(i + 10, j + 1));//
//							lost = lineStr.substring(i + 10, j + 1);
						}
						if (lineStr.contains("avg")) {
							int i = lineStr.indexOf("/", 20);
							int j = lineStr.indexOf(".", i);
							Log.i(TAG,
									"====平均时延:===="
											+ lineStr.substring(i + 1, j));
//							delay = lineStr.substring(i + 1, j);
//							delay = delay + "ms";
						}
						// tv_show.setText("丢包率:" + lost.toString() + "\n" +
						// "平均时延:"
						// + delay.toString() + "\n" + "IP地址:");// +
						// getNetIpAddress()
						// + getLocalIPAdress() + "\n" + "MAC地址:" +
						// getLocalMacAddress() + getGateWay());
						sleep(1 * 1000);
					}
					// tv_show.setText(result);

//					while ((lineStr = errorReader.readLine()) != null) {
//						Log.i(TAG, "==error======" + lineStr);
//						// tv_show.setText(lineStr);
//					}

				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (dos != null) {
							dos.close();
						}
						if (successReader != null) {
							successReader.close();
						}
						if (errorReader != null) {
							errorReader.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (process != null) {
						process.destroy();
					}
					Message msg = mHandler.obtainMessage();
					msg.what = 11;
					msg.sendToTarget();
				}
			}
		};
		a.start();
	}
}
