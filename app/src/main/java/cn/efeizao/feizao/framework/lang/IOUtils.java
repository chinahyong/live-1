package cn.efeizao.feizao.framework.lang;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Title: XXXX (类或者接口名称) Description: XXXX (简单对此类或接口的名字进行描述) Copyright:
 * @version 1.0
 */
public class IOUtils {
	/**
	 * 普通输入流转化为字节数组
	 * 
	 * @param is
	 * @return
	 */
	public static byte[] getByteByStream(InputStream is) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			byte[] buf = new byte[1024];
			int num;
			while ((num = is.read(buf, 0, buf.length)) != -1) {
				out.write(buf, 0, num);
			}
			out.close();
			return out.toByteArray();
		} finally {
			try {
				if (out != null) {
					out.close();
					out = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 获取压缩网络输入流的byte数组
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static byte[] getGZipBytes(InputStream inputStream)
			throws IOException {
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
		byte[] buffer = new byte[1024];
		int num = -1;
		try {
			while ((num = gzipInputStream.read(buffer)) != -1) {
				arrayOutputStream.write(buffer, 0, num);
			}
			arrayOutputStream.close();
			return arrayOutputStream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		return null;
	}

}
