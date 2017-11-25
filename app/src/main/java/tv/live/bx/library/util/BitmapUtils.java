package tv.live.bx.library.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.util.StateSet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import tv.live.bx.FeizaoApp;
import tv.live.bx.R;
import tv.live.bx.common.Constants;

/**
 * Title: BitmapUtils.java</br> Description: 处理图片的工具类</br> Copyright: Copyright
 * (c) 2008</br>
 *
 * @version 1.0
 */
public class BitmapUtils {

	/**
	 * @param context  上下文对象
	 * @param resId    原始的Drawable资源
	 * @param pointRes 需要绘制的圆点资源
	 * @return 在指定的Drawable上画pointRes
	 */
	public static Bitmap drawPointToDrawable(Context context, int drawableRes, int pointRes) {
		Resources resources = context.getResources();
		Bitmap bitmap = BitmapFactory.decodeResource(resources, drawableRes);
		Bitmap point = BitmapFactory.decodeResource(resources, pointRes);

		android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
		// set default bitmap config if none
		if (bitmapConfig == null) {
			bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
		}
		// resource bitmaps are imutable,
		// so we need to convert it to mutable one
		bitmap = bitmap.copy(bitmapConfig, true);
		point = point.copy(bitmapConfig, true);

		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setDither(true);
		paint.setFilterBitmap(true);

		int left = bitmap.getWidth() - point.getScaledWidth(canvas);
		canvas.drawBitmap(point, left, 0, paint);

		return bitmap;
	}

	/**
	 * @param bmpOriginal 原始图片
	 * @return 去色后的黑白图片
	 */
	public static Bitmap toGrayscale(Bitmap bmpOriginal) {
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	/**
	 * @param context 上下文
	 * @param resId   drawable资源id
	 * @return 按下(press)效果为原图透明度50%的StateListDrawable对象
	 */
	public static StateListDrawable getPressSelector(Context context, int resId) {
		StateListDrawable drawable = new StateListDrawable();
		Drawable normal = context.getResources().getDrawable(resId);
		Bitmap normalBitmap = ((BitmapDrawable) normal).getBitmap();

		// Setting alpha directly just didn't work, so we draw a new bitmap!
		Bitmap pressBitmap = Bitmap.createBitmap(normal.getIntrinsicWidth(), normal.getIntrinsicHeight(),
				Config.ARGB_8888);
		Canvas canvas = new Canvas(pressBitmap);
		Paint paint = new Paint();
		paint.setAlpha(126);
		canvas.drawBitmap(normalBitmap, 0, 0, paint);

		BitmapDrawable press = new BitmapDrawable(context.getResources(), pressBitmap);
		drawable.addState(new int[]{android.R.attr.state_pressed}, press);
		drawable.addState(StateSet.WILD_CARD, normal);
		return drawable;
	}

	/**
	 * @param path 图片的绝对路径 压缩图片的分辨率，压缩后的图片保存到指定的路径下
	 */
	// public static String compressPhotoSize(String path) throws IOException {
	// if (path == null || path.trim().equals("")) {
	// return null;
	// }
	//
	// if(!new File(path).exists()){
	// return null;
	// }
	//
	// BitmapFactory.Options options = BitmapUtility.obtainBitmapOptions(path,
	// Constants.PWD_MAX_LENGHT);
	// Bitmap bmp = null;
	// try {
	// bmp = BitmapFactory.decodeFile(path, options);
	// } catch (OutOfMemoryError e) {
	// System.gc();
	// bmp = null;
	// return null;
	// }
	//
	// if(bmp == null) {
	// return null;
	// }
	//
	// /*Bitmap bitmap = null;
	// if(options.inSampleSize > 1){
	// int w = bmp.getWidth();
	// int h = bmp.getHeight();
	// float scale = (float)h / (float)w;
	// try {
	// bitmap = BitmapUtility.ZoomImg(bmp, Constants.PIC_MAX_SIZE, (int)
	// (Constants.PIC_MAX_SIZE * scale));
	// } catch (Exception e) {
	// e.printStackTrace();
	// return null;
	// }
	// if(!bitmap.equals(bmp)){
	// bmp.recycle();
	// }
	// bmp = null;
	// }else{
	// bitmap = bmp;
	// }*/
	//
	// // 将bitmap存入SD卡
	// File dir = new File(Constants.TEMP_PHOTO_PATH);
	// if(!dir.exists()) {
	// dir.mkdirs();
	// }
	// // 原文件名
	// String fileName = path.substring(path.lastIndexOf("/") + 1);
	// File newFile = new File(dir, fileName);
	// FileOutputStream out = new FileOutputStream(newFile);
	// bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
	// out.flush();
	// out.close();
	//
	// // 将文件用md5值重命名
	// String md5 = FileUtil.getFileMD5(new FileInputStream(newFile));
	// String extensionName = fileName.substring(fileName.lastIndexOf('.'));
	// File newPathFile = new File(newFile.getParentFile(), md5 +
	// extensionName);
	// newFile.renameTo(newPathFile);
	//
	// String newPath = newPathFile.getAbsolutePath();
	// EvtLog.d("BitmapUtils", "成功压缩、重命名并保存到新目录：" + newPath);
	// return newPath;
	// }

	/**
	 * 将图片对象写入指定的路径中
	 *
	 * @param bitmap   Bitmap对象
	 * @param destPath 目标路径
	 * @param quality  图片写入质量（1 - 100）
	 */
	public static boolean writeImage(Bitmap bitmap, String destPath, int quality) {
		if (bitmap == null)
			return false;
		try {
			FileUtil.delete(destPath);
			if (FileUtil.createFiles(destPath)) {
				FileOutputStream out = new FileOutputStream(destPath);
				if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
					out.flush();
					out.close();
					out = null;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 转换图片成圆形
	 *
	 * @param bitmap 传入Bitmap对象
	 * @return
	 */
	public static Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;

			left = 0;
			top = 0;
			right = width;
			bottom = width;

			height = width;

			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;

			float clip = (width - height) / 2;

			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;

			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);// 设置画笔无锯齿

		canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas

		// 以下有两种方法画圆,drawRounRect和drawCircle
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
		// canvas.drawCircle(roundPx, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
		canvas.drawBitmap(bitmap, src, dst, paint); // 以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

		return output;
	}

	/**
	 * 扫描指定的文件，让Android系统将其识别为图片
	 *
	 * @param context 上下文
	 * @param f       指定的文件对象
	 */
	public static void fileScan(Context context, File f) {
		Uri data = Uri.fromFile(f);
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
	}

	/**
	 * 把返回的人名，转换成bitmap
	 *
	 * @param name
	 * @return
	 */
	public static Bitmap getNameBitmap(String name, int textSize) {
		/* 把@相关的字符串转换成bitmap 然后使用DynamicDrawableSpan加入输入框中 */
		name = Constants.COMMON_INSERT_POST_PIX + name + Constants.COMMON_INSERT_POST_PIX;
		Paint paint = new Paint();
		paint.setColor(FeizaoApp.mConctext.getResources().getColor(R.color.a_text_color_da500e));
		paint.setAntiAlias(true);
		paint.setTextSize(textSize);
		Rect rect = new Rect();

		paint.getTextBounds(name, 0, name.length(), rect);
		// 获取字符串在屏幕上的长度
		int width = (int) (paint.measureText(name));

		final Bitmap bmp = Bitmap.createBitmap(width, rect.height(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		canvas.drawText(name, rect.left, rect.height() - rect.bottom, paint);

		return bmp;
	}

	/**
	 * 获取视频第一帧
	 *
	 * @param path
	 * @return
	 */
	@SuppressLint("NewApi")
	public static Bitmap createVideoThumbnail(String path) {
		if (!TextUtils.isEmpty(path)) {
			MediaMetadataRetriever mr = null;
			Bitmap bitmap = null;
			try {
				mr = new MediaMetadataRetriever();
				mr.setDataSource(path);
				// String width =
				// mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
				// String height =
				// mr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
				bitmap = mr.getFrameAtTime();
				return bitmap;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			} finally {
				if (mr != null) {
					mr.release();
					mr = null;
				}
			}
		}
		return null;
	}

	/**
	 * 高斯模糊算法：指定像素和其周围像素进行加权平均来得到最终结果
	 *
	 * @param srcBitmap        原图
	 * @param radius
	 * @param canReuseInBitmap
	 * @return
	 * @since JDK 1.6
	 */
	public static Bitmap doBlur(Bitmap srcBitmap, int radius, boolean canReuseInBitmap) {
		Bitmap bitmap;
		if (canReuseInBitmap) {
			bitmap = srcBitmap;
		} else {
			bitmap = srcBitmap.copy(srcBitmap.getConfig(), true);
		}

		if (radius < 1) {
			return (null);
		}

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);

		int wm = w - 1;
		int hm = h - 1;
		int wh = w * h;
		int div = radius + radius + 1;

		int r[] = new int[wh];
		int g[] = new int[wh];
		int b[] = new int[wh];
		int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
		int vmin[] = new int[Math.max(w, h)];

		int divsum = (div + 1) >> 1;
		divsum *= divsum;
		int dv[] = new int[256 * divsum];
		for (i = 0; i < 256 * divsum; i++) {
			dv[i] = (i / divsum);
		}

		yw = yi = 0;

		int[][] stack = new int[div][3];
		int stackpointer;
		int stackstart;
		int[] sir;
		int rbs;
		int r1 = radius + 1;
		int routsum, goutsum, boutsum;
		int rinsum, ginsum, binsum;

		for (y = 0; y < h; y++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			for (i = -radius; i <= radius; i++) {
				p = pix[yi + Math.min(wm, Math.max(i, 0))];
				sir = stack[i + radius];
				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);
				rbs = r1 - Math.abs(i);
				rsum += sir[0] * rbs;
				gsum += sir[1] * rbs;
				bsum += sir[2] * rbs;
				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}
			}
			stackpointer = radius;

			for (x = 0; x < w; x++) {

				r[yi] = dv[rsum];
				g[yi] = dv[gsum];
				b[yi] = dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (y == 0) {
					vmin[x] = Math.min(x + radius + 1, wm);
				}
				p = pix[yw + vmin[x]];

				sir[0] = (p & 0xff0000) >> 16;
				sir[1] = (p & 0x00ff00) >> 8;
				sir[2] = (p & 0x0000ff);

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[(stackpointer) % div];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi++;
			}
			yw += w;
		}
		for (x = 0; x < w; x++) {
			rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
			yp = -radius * w;
			for (i = -radius; i <= radius; i++) {
				yi = Math.max(0, yp) + x;

				sir = stack[i + radius];

				sir[0] = r[yi];
				sir[1] = g[yi];
				sir[2] = b[yi];

				rbs = r1 - Math.abs(i);

				rsum += r[yi] * rbs;
				gsum += g[yi] * rbs;
				bsum += b[yi] * rbs;

				if (i > 0) {
					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];
				} else {
					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];
				}

				if (i < hm) {
					yp += w;
				}
			}
			yi = x;
			stackpointer = radius;
			for (y = 0; y < h; y++) {
				// Preserve alpha channel: ( 0xff000000 & pix[yi] )
				pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

				rsum -= routsum;
				gsum -= goutsum;
				bsum -= boutsum;

				stackstart = stackpointer - radius + div;
				sir = stack[stackstart % div];

				routsum -= sir[0];
				goutsum -= sir[1];
				boutsum -= sir[2];

				if (x == 0) {
					vmin[y] = Math.min(y + r1, hm) * w;
				}
				p = x + vmin[y];

				sir[0] = r[p];
				sir[1] = g[p];
				sir[2] = b[p];

				rinsum += sir[0];
				ginsum += sir[1];
				binsum += sir[2];

				rsum += rinsum;
				gsum += ginsum;
				bsum += binsum;

				stackpointer = (stackpointer + 1) % div;
				sir = stack[stackpointer];

				routsum += sir[0];
				goutsum += sir[1];
				boutsum += sir[2];

				rinsum -= sir[0];
				ginsum -= sir[1];
				binsum -= sir[2];

				yi += w;
			}
		}

		bitmap.setPixels(pix, 0, w, 0, 0, w, h);

		return (bitmap);
	}
}
