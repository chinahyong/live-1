/**
 * Project Name:feizao File Name:AsyncLoadNetworkPic.java Package
 * Name:com.efeizao.feizao.tasks Date:2015-9-7下午12:30:40
 */

package tv.live.bx.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.widget.TextView;

import tv.live.bx.library.util.EvtLog;

/**
 * ClassName:AsyncLoadNetworkPic Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2015-9-7 下午12:30:40
 * @author Live
 * @version 1.0
 */
/**
 * 加载网络图片异步类
 * @author Susie
 */
public class AsyncLoadNetworkPic extends AsyncTask<String, Integer, Void> {

	@SuppressWarnings("unused")
	private TextView mTextView;
	private String mSource;
	private ImageGetter mImageGetter;

	public AsyncLoadNetworkPic(TextView textView, String source, ImageGetter imageGetter) {
		this.mTextView = textView;
		this.mSource = source;
		this.mImageGetter = imageGetter;
	}

	@Override
	protected Void doInBackground(String... params) {
		// 加载网络图片
		loadNetPic(params);
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		// 当执行完成后再次为其设置一次
		mTextView.setText(Html.fromHtml(mSource, mImageGetter, null));
	}

	/** 加载网络图片 */
	private void loadNetPic(String... params) {
		String path = params[0];

		File file = new File(Environment.getExternalStorageDirectory(), "111.png");

		InputStream in = null;

		FileOutputStream out = null;

		try {
			URL url = new URL(path);

			HttpURLConnection connUrl = (HttpURLConnection) url.openConnection();

			connUrl.setConnectTimeout(5000);

			connUrl.setRequestMethod("GET");

			if (connUrl.getResponseCode() == 200) {

				in = connUrl.getInputStream();

				out = new FileOutputStream(file);

				byte[] buffer = new byte[1024];

				int len;

				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
			} else {
				EvtLog.i("AsyncLoadNetworkPic", connUrl.getResponseCode() + "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
