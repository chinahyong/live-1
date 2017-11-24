package tv.live.bx.library.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 画圆角图片帮助类
 * @author SJR
 */
public class BitmapHelper {
	/**
	 * 画圆角
	 * @param bitmap 要画圆角的bitmap
	 * @param radiusPx 圆角大小，值越大越圆
	 * @return 画完圆角的Bitmap
	 */
	public static Bitmap DrawRadius(Bitmap bitmap, float radiusPx) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff202020;
		final Paint paint = new Paint();
		// 下面的四个参数分别为：left,top,right,bottom
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);
		final float roundPx = radiusPx * bitmap.getWidth() / 2;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

	/**
	 * 　为指定图片增加阴影
	 * 
	 * @param map　图片
	 * @param radius　阴影的半径
	 * @return
	 */
	public static Bitmap DrawShadow(Bitmap map, int radius) {
		if (map == null)
			return null;

		BlurMaskFilter blurFilter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.SOLID);
		Paint shadowPaint = new Paint();
		shadowPaint.setMaskFilter(blurFilter);

		int[] offsetXY = new int[2];
		Bitmap shadowImage = map.extractAlpha(shadowPaint, offsetXY);
		shadowImage = shadowImage.copy(Config.ARGB_8888, true);
		Canvas c = new Canvas(shadowImage);

		Bitmap temimage = map.copy(Config.ARGB_8888, true);
		// Canvas canvas = new Canvas(temimage);
		// Rect rect = getRect(canvas);
		// Paint paint = new Paint();
		// paint.setColor(Color.WHITE);
		// paint.setStyle(Paint.Style.STROKE);
		// 画边框
		// canvas.drawRect(rect, paint);
		// canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width()/2,
		// paint);

		c.drawBitmap(temimage, -offsetXY[0] - 0.6f, -offsetXY[1] - 0.6f, null);
		return shadowImage;
	}

	/**
	 * 添加透明阴影，做淡化动画后由于两张图片会叠在一起造成阴影颜色加深，所以需要一张不带阴影但有同样规格的图片,该方法用于系统图片
	 * @param map
	 * @param radius radius为阴影圈的大小，因为在默认图片上使用，所以得转换一下，保证在不同分辨路的设备上阴影大小一致 [实际大小]
	 *            / [1.5f / getResources().getDisplayMetrics().density]
	 * @return
	 */
	public static Bitmap DrawTransparentShadow(Bitmap map, float radius) {
		if (map == null)
			return null;

		Bitmap temimage = Bitmap.createBitmap(map.getWidth(), map.getHeight(), Config.ARGB_8888);
		Canvas c = new Canvas(temimage);
		Rect src = new Rect(0, 0, map.getWidth(), map.getHeight());
		RectF dst = new RectF(radius, radius, map.getWidth() - radius + 0.6f, map.getHeight() - radius + 0.6f);
		c.drawBitmap(map, src, dst, null);
		return temimage;
	}

	/**
	 * 添加水印、透明阴影，做淡化动画后由于两张图片会叠在一起造成阴影颜色加深，所以需要一张不带阴影但有同样规格的图片,该方法用于系统图片
	 * @param watermark 水印
	 * @param image 需加水印原图片
	 * @param radius radius 为阴影圈的大小，因为在系统图片上使用，所以得转换一下，保证在不同分辨路的设备上阴影大小一致，[实际大小]
	 *            / [1.5f / getResources().getDisplayMetrics().density]
	 * @return
	 */
	public static Bitmap Draw_Watermark_TransparentShadow(Bitmap watermark, Bitmap image, float radius) {
		if (image == null)
			return null;

		Bitmap shadowImage = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Config.ARGB_8888);
		Canvas c = new Canvas(shadowImage);

		Bitmap temimage = image.copy(Config.ARGB_8888, true);

		if (watermark != null) {
			Canvas canvas = new Canvas(temimage);
			Rect src = new Rect(0, 0, watermark.getWidth(), watermark.getHeight());
			canvas.drawBitmap(watermark, src, src, null);
		}

		Rect src = new Rect(0, 0, image.getWidth(), image.getHeight());
		RectF dst = new RectF(radius, radius, image.getWidth() - radius + 0.6f, image.getHeight() - radius + 0.6f);
		c.drawBitmap(temimage, src, dst, null);
		return shadowImage;
	}

	/**
	 * 绘制水印、阴影，该方法适用于系统图片（水印图片跟原图片大小规格一致）
	 * @param watermark 水印
	 * @param iamge 需加载水印的原图
	 * @param radius 阴影范围 [实际大小] / [1.5f /
	 *            getResources().getDisplayMetrics().density]
	 * @return
	 */
	public static Bitmap Draw_Watermark_Shadow(Bitmap watermark, Bitmap iamge, int radius) {
		if (iamge == null)
			return null;

		BlurMaskFilter blurFilter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.SOLID);
		Paint shadowPaint = new Paint();
		shadowPaint.setMaskFilter(blurFilter);

		int[] offsetXY = new int[2];
		Bitmap shadowImage = iamge.extractAlpha(shadowPaint, offsetXY);
		shadowImage = shadowImage.copy(Config.ARGB_8888, true);
		Canvas c = new Canvas(shadowImage);

		Bitmap temimage = iamge.copy(Config.ARGB_8888, true);

		if (watermark != null) {
			Canvas canvas = new Canvas(temimage);
			Rect src = new Rect(0, 0, watermark.getWidth(), watermark.getHeight());
			Rect dst = new Rect(0, 0, watermark.getWidth(), watermark.getHeight());
			canvas.drawBitmap(watermark, src, dst, null);
		}
		c.drawBitmap(temimage, -offsetXY[0] - 0.6f, -offsetXY[1] - 0.6f, null);
		return shadowImage;
	}

	/**
	 * 绘制水印、阴影，该方法适用于网络图片（水印图片跟原图片大小规格不一致，系统图片会自动转换二网络图片没有，所以得转换一下）
	 * @param watermark 水印
	 * @param iamge 需加水印的原图片
	 * @param radius 阴影范围大小
	 * @param density 变化单位，为以固定值 [1.5f /
	 *            getResources().getDisplayMetrics().density]
	 * @return
	 */
	public static Bitmap Draw_Watermark_Shadow(Bitmap watermark, Bitmap iamge, int radius, float density) {
		if (iamge == null)
			return null;

		BlurMaskFilter blurFilter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.SOLID);
		Paint shadowPaint = new Paint();
		shadowPaint.setMaskFilter(blurFilter);

		int[] offsetXY = new int[2];
		Bitmap shadowImage = iamge.extractAlpha(shadowPaint, offsetXY);
		shadowImage = shadowImage.copy(Config.ARGB_8888, true);
		Canvas c = new Canvas(shadowImage);

		Bitmap temimage = iamge.copy(Config.ARGB_8888, true);

		if (watermark != null) {
			Canvas canvas = new Canvas(temimage);
			Rect src = new Rect(0, 0, watermark.getWidth(), watermark.getHeight());
			Rect dst = new Rect(0, 0, (int) (watermark.getWidth() * (density <= 0 ? 1 : density)),
					(int) (watermark.getHeight() * (density <= 0 ? 1 : density)));
			canvas.drawBitmap(watermark, src, dst, null);
		}
		c.drawBitmap(temimage, -offsetXY[0] - 0.6f, -offsetXY[1] - 0.6f, null);
		return shadowImage;
	}

	public static Rect getRect(Canvas canvas) {
		Rect rect = canvas.getClipBounds();
		rect.bottom -= 1;
		rect.right -= 1;
		// rect.left += 1;
		// rect.top += 1;
		return rect;
	}

	/**
	 * 画圆角带阴影图片
	 * @param bitmap
	 * @param radiusPx 圆角半径
	 * @param radius 阴影偏移
	 * @return
	 */
	public static Bitmap DrawRadiusAndShadow(Bitmap bitmap, float radiusPx, int radius) {

		// 画圆角
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		int color = 0xff424242;
		Paint paint = new Paint();
		// 下面的四个参数分别为：left,top,right,bottom
		Rect rect = new Rect(1, 0, bitmap.getWidth() - 1, bitmap.getHeight() - 1);
		RectF rectF = new RectF(rect);
		float roundPx = radiusPx;
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		// 画阴影
		BlurMaskFilter blurFilter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.SOLID);
		Paint shadowPaint = new Paint();
		shadowPaint.setMaskFilter(blurFilter);

		int[] offsetXY = new int[2];
		Bitmap shadowImage = output.extractAlpha(shadowPaint, offsetXY);
		shadowImage = shadowImage.copy(Config.ARGB_8888, true);
		canvas = new Canvas(shadowImage);

		canvas.drawBitmap(output, -offsetXY[0] - 0.6f, -offsetXY[1] - 0.6f, null);

		output.recycle();
		return shadowImage;
	}

	/**
	 * 以最省内存的方式读取本地资源的图片
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitMap(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * 转换图片名称格式
	 */
	public static String transformBitmapName(String bitmapName) {
		return transformBitmapName(bitmapName, "");
	}

	/**
	 * 使用该标签的对象用于需要回收的大图使用，一般是用完就回收的，与一般的缓存分开，避免出现回收了正在使用的图片
	 * @param bitmapName
	 * @return
	 */
	public static String transformBitmapName_NeedRecyle(String bitmapName) {
		return transformBitmapName(bitmapName, "recycle");
	}

	public static String transformBitmapName(String bitmapName, String tag) {
		String newName = null;
		if (bitmapName == null || bitmapName.trim().equals("")) {
			return null;
		}
		// Log.d("BitmapHelp", "bitmapName = " + bitmapName);
		// 排除特殊符号 / \ : * " < > | . 空格 jpg png 注意：先清掉特定的单词组
		newName = bitmapName.replaceAll("(\\.jpg|\\.png|\\.JPG|\\.PNG)|[//\\\\*:\"<>|\\s\\.]", "");
		newName = newName + tag;
		// Log.d("BitmapHelp", "newName = " + newName);
		return (newName.trim().equals("") ? null : newName.trim());
	}

	/**
	 * 从路径中获取出文件名称
	 * @param path
	 * @return 分离出来的文件名(图片不带图片后缀)
	 */
	public static String separateNameFromPath(String path, boolean transform) {
		String filename = "";
		String temppath = Pattern.compile("[\\/]").matcher(path).replaceAll(File.separator);
		int endIndex = temppath.lastIndexOf(File.separator);
		filename = endIndex + 1 <= path.length() ? temppath.substring(endIndex + 1) : "";
		return transform ? transformBitmapName(filename) : filename;
	}

	public static byte[] getRemoteBitmap(String strUrl) {
		if (strUrl == null || "".equals(strUrl)) {
			return null;
		}

		byte[] bitmapBuffer = null;
		byte[] receiveBuffer = new byte[1024];
		InputStream inputStream = null;
		ByteArrayOutputStream byteArrayOutputStream = null;
		try {
			URL url = new URL(strUrl);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				inputStream = httpURLConnection.getInputStream();
				byteArrayOutputStream = new ByteArrayOutputStream();
				int count = 0;
				while ((count = inputStream.read(receiveBuffer)) > 0) {
					byteArrayOutputStream.write(receiveBuffer, 0, count);
					byteArrayOutputStream.flush();
				}
				bitmapBuffer = byteArrayOutputStream.toByteArray();
				return bitmapBuffer;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}
				if (byteArrayOutputStream != null) {
					byteArrayOutputStream.close();
					byteArrayOutputStream = null;
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}

	enum Orientation {
		HORIZONTAL, VERTICAL
	}

	/**
	 * 将给定的{@link Bitmap}对象按照线性布局排列生成一张新的{@link Bitmap}
	 * ,图片可能等比缩放，所有的图片在排列的另一个方向上居中
	 * @param bmps 图片
	 * @param orientation 排列方向
	 * @param space 间隔
	 * @param size 横向排列时为高度，纵向排列时为宽度，若不大于0,则使用最大高度（宽度）
	 * @param rCorner 是否加圆角
	 * @return
	 */
	public static Bitmap makeBitmapArray(Bitmap[] bmps, Orientation orientation, int space, int size, int radius) {
		if (bmps == null || bmps.length == 0) {
			return null;
		}
		Bitmap result = null;
		Canvas canvas = null;
		final int bmpCount = bmps.length;
		space = Math.max(0, space);
		int heights[] = new int[bmpCount];
		int widths[] = new int[bmpCount];

		float scale = 0;
		int height = 0;
		int width = 0;
		Rect dstRect = new Rect();
		RectF rectF = new RectF();
		Paint rCornorPaint = null;
		if (radius > 0) {
			rCornorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			rCornorPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
			rCornorPaint.setColor(0xFF000000);
		}

		switch (orientation) {
		case HORIZONTAL:
			// 图片的最大高度
			int maxHeight = 0;
			int totalWidth = 0;
			for (int i = 0; i < bmpCount; i++) {
				Bitmap bitmap = bmps[i];
				int h = bitmap.getHeight();
				if (h > maxHeight) {
					maxHeight = h;
				}
				heights[i] = h;
				int w = bitmap.getWidth();
				totalWidth += w;
				widths[i] = w;
			}
			// 实际留给图片的高度
			height = size > 0 ? size : maxHeight;
			scale = ((float) height) / maxHeight;
			width = (int) (totalWidth * scale + space * (bmpCount - 1));

			result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			canvas = new Canvas(result);

			dstRect.left = 0;
			for (int i = 0; i < bmpCount; i++) {
				Bitmap bmp = bmps[i];
				int w = (int) (widths[i] * scale);
				int h = (int) (heights[i] * scale);
				dstRect.top = (height - h) / 2;
				dstRect.bottom = dstRect.top + h;
				dstRect.right = dstRect.left + w;
				canvas.drawBitmap(bmp, null, dstRect, null);
				if (radius > 0) {
					rectF.set(dstRect);
					canvas.drawRoundRect(new RectF(dstRect), radius, radius, rCornorPaint);
				}
				dstRect.left = dstRect.right + space;
			}
			break;
		case VERTICAL:
			int maxWidth = 0;
			int totalHeight = 0;
			for (int i = 0; i < bmpCount; i++) {
				Bitmap bitmap = bmps[i];
				int h = bitmap.getHeight();
				totalHeight += h;
				heights[i] = h;

				int w = bitmap.getWidth();
				if (w > maxWidth) {
					maxWidth = w;
				}
				widths[i] = w;
			}

			// 实际留给图片的高度
			width = size > 0 ? size : maxWidth;
			scale = ((float) width) / maxWidth;
			height = (int) (totalHeight * scale + space * (bmpCount - 1));

			result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			canvas = new Canvas(result);

			dstRect.top = 0;
			for (int i = 0; i < bmpCount; i++) {
				Bitmap bmp = bmps[i];
				int w = (int) (widths[i] * scale);
				int h = (int) (heights[i] * scale);
				dstRect.left = (width - w) / 2;
				dstRect.right = dstRect.left + w;
				dstRect.bottom = dstRect.top + h;
				canvas.drawBitmap(bmp, null, dstRect, null);
				if (radius > 0) {
					rectF.set(dstRect);
					canvas.drawRoundRect(new RectF(dstRect), radius, radius, rCornorPaint);
				}
				dstRect.top = dstRect.bottom + space;
			}

		default:
			break;
		}

		return result;
	}

	/**
	 * 生成一张指定大小的bitmap，包含给定的src位图和padding,在新的bitmap中，src居中且保持原始比例
	 * @param src
	 * @param width
	 * @param height
	 * @param padding
	 * @return
	 */
	public static Bitmap addFrame(Bitmap src, int width, int height, int padding, int bgColor) {
		if (src == null || width <= 0 || height <= 0) {
			return null;
		}
		padding = padding >= 0 ? padding : 0;
		Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		canvas.drawColor(bgColor);

		int dstW = width - padding * 2;
		int dstH = height - padding * 2;

		int srcH = src.getHeight();
		int srcW = src.getWidth();

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		if (srcH * dstW > srcW * dstH) {
			dstW = dstH * srcW / srcH;
			Rect dstRect = new Rect();
			dstRect.left = (width - dstW) / 2;
			dstRect.right = dstRect.left + dstW;
			dstRect.top = padding;
			dstRect.bottom = dstRect.top + dstH;
			canvas.drawBitmap(src, null, dstRect, paint);
		} else {
			dstH = dstW * srcH / srcW;
			Rect dstRect = new Rect();
			dstRect.left = padding;
			dstRect.right = dstRect.left + dstW;
			dstRect.top = (height - dstH) / 2;
			dstRect.bottom = dstRect.top + dstH;
			canvas.drawBitmap(src, null, dstRect, paint);
		}
		return result;
	}

	static final int[][] COMPOUND_BITMAP_SHAPES = { {},// 0
													   // 占位，没有实际用途，只是为了让个数和index对应
			{ 1 }, { 2 }, { 1, 2 }, { 2, 2 }, { 2, 3 }, { 3, 3 }, { 1, 3, 3 }, { 2, 3, 3 }, { 3, 3, 3 }, };

	/**
	 * 将一组头像组合成一张图片 假设：头像为大小相同的正方形位图
	 * @param bmps 图片数组
	 * @param size 最终生成图片的大小，像素值
	 * @param spaceRatio 图片之间的留白/图片大小
	 * @param radius 圆角半径/图片大小
	 * @param padding 留边，像素值
	 * @param bgColor 背景色
	 * @return
	 */
	public static Bitmap makeCompoundBitmap(Bitmap[] bmps, int size, float spaceRatio, float radius, int padding,
			int bgColor) {
		final int count = Math.min(9, bmps.length);
		if (count == 0) {
			return null;
		}
		if (count != bmps.length) {
			Bitmap[] newArray = new Bitmap[count];
			System.arraycopy(bmps, 0, newArray, 0, count);
			bmps = newArray;
		}

		if (count == 1) { // 只有1张图片，直接copy一张返回
			return bmps[0].copy(Config.ARGB_8888, false);
		}
		// 多张图片
		int[] shape = COMPOUND_BITMAP_SHAPES[count];

		// 0.在生成图片的过程中，中间产品不必使用原始大小，减小内存占用，这里需要计算一些尺寸值
		int rowCount = shape.length;
		// 找出最多列数，这个数决定了结果中图的大小
		int maxColumn = 0;
		for (int i = 0; i < rowCount; i++) {
			if (shape[i] > maxColumn) {
				maxColumn = rowCount;
			}
		}
		int maxArraySize = Math.max(maxColumn, rowCount);
		// 行高，及小图片的大小
		int rowH = (int) Math.ceil(((float) size - padding * 2) / (maxArraySize + (maxArraySize - 1) * spaceRatio));
		// 留白大小
		int space = (int) (rowH * spaceRatio);
		// 最宽的一行的大小
		int maxW = rowH * maxColumn + (maxColumn - 1) * space;

		// 1.生成行
		Bitmap[] bmpRows = new Bitmap[rowCount];
		int columnStart = 0;
		for (int i = 0; i < rowCount; i++) {
			int columnCount = shape[i];
			Bitmap[] bmpColumns = new Bitmap[columnCount];
			System.arraycopy(bmps, columnStart, bmpColumns, 0, columnCount);
			columnStart += columnCount;
			bmpRows[i] = makeBitmapArray(bmpColumns, Orientation.HORIZONTAL, space, rowH, (int) (rowH * radius));
		}

		// 2.组装行
		Bitmap noPaddingResult = null;
		if (rowCount == 1) {
			noPaddingResult = bmpRows[0];
		} else {
			noPaddingResult = makeBitmapArray(bmpRows, Orientation.VERTICAL, space, maxW, 0);
			for (Bitmap bitmap : bmpRows) {
				bitmap.recycle();
			}
		}

		// 3.加边框,统一大小
		Bitmap result = addFrame(noPaddingResult, size, size, padding, bgColor);
		noPaddingResult.recycle();
		return result;
	}

	public static void cacheBitmapFile(Bitmap bmp, CompressFormat format, int quality, String dst) {
		if (bmp == null) {
			return;
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		if (bmp.compress(format, quality, byteArrayOutputStream)) {
			cacheBitmapFile(byteArrayOutputStream.toByteArray(), dst);
		}
		try {
			byteArrayOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void cacheBitmapFile(byte[] bmpBuffer, String dst) {
		if (bmpBuffer == null || dst == null) {
			return;
		}
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(dst);
			fileOutputStream.write(bmpBuffer);
			fileOutputStream.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fileOutputStream = null;
			}
		}
	}

	/** 画扇形图片 */
	public static Bitmap getArcRadiuProgress(int width, int color, float pfPercent) {
		Bitmap loBmp = Bitmap.createBitmap(width, width, Config.ARGB_8888);
		Canvas loCvs = new Canvas(loBmp);
		Paint loPt = new Paint();
		// 画进度
		loPt.setColor(color);
		RectF loRf = new RectF(0, 0, loBmp.getWidth(), loBmp.getHeight());
		loCvs.drawArc(loRf, 0, 360 * pfPercent / 100, true, loPt);
		return loBmp;
	}

}
