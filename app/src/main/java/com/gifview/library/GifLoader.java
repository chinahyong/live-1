package com.gifview.library;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import pl.droidsonroids.gif.AnimationListener;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;
import tv.live.bx.R;
import tv.live.bx.library.file.load.AsyLoadTask;
import tv.live.bx.library.file.load.DiskCache;
import tv.live.bx.library.file.load.FileCache;
import tv.live.bx.library.file.load.LoadTask;
import tv.live.bx.library.file.load.LoadingListener;
import tv.live.bx.library.util.EvtLog;

/**
 * @author Live
 * @version 2016/11/2 ${VERSION}
 * @title ${CLASS_NAME} Description:
 */
public class GifLoader {

	private static String GIF_CACHE_FILE_DIR = "gif";
	private static GifLoader mInstance;

	protected GifLoader() {
	}

	protected GifLoader(Context context) {
		mDisplayViewMap = new ConcurrentHashMap<>();
		mGifCache = new DiskCache(context, GIF_CACHE_FILE_DIR);
		mLoadTask = new AsyLoadTask();
	}

	public static GifLoader getInstance(Context context) {
		if (mInstance == null) {
			synchronized (GifLoader.class) {
				if (mInstance == null) {
					mInstance = new GifLoader(context);
				}
			}
		}
		return mInstance;
	}

	// gif缓存
	private FileCache mGifCache;
	// gif文件加载
	private LoadTask mLoadTask;
	//防止同一个gif文件建立多个下载线程,url和imageView是一对多的关系,如果一个imageView建立了一次下载，那么其他请求这个url的imageView不需要重新开启一次新的下载，这几个imageView同时回调
	//为了防止内存泄漏，这个一对多的关系均使用LRU缓存
	public ConcurrentHashMap<String, ArrayList<ProgressViews>> mDisplayViewMap;

	public void setmGifCache(FileCache mGifCache) {
		this.mGifCache = mGifCache;
	}

	public void setmLoadTask(LoadTask mLoadTask) {
		this.mLoadTask = mLoadTask;
	}

	/**
	 * 显示gif图片
	 *
	 * @param url
	 * @param gifView
	 */
	public void displayGif(String url, GifImageView gifView) {
		displayGif(url, gifView, null, 0, null);
	}

	/**
	 * 显示gif图片
	 *
	 * @param url
	 * @param gifView
	 */
	public void displayGif(String url, GifImageView gifView, GifPlayListener gifPlayListener) {
		displayGif(url, gifView, null, 0, gifPlayListener);
	}

	/**
	 * 显示gif图片
	 *
	 * @param url     图片/gif路径
	 * @param gifView 显示控件
	 */
	public void displayGif(final String url, GifImageView gifView, TextView tvProgress, int displayWidth, final GifPlayListener gifPlayListener) {
		try {
			EvtLog.i("AlexGIF", "显示 displayGif url：" + url);
			final Handler handler = new Handler();
			File cacheFile = mGifCache.get(url);
			//如果本地已经有了这个gif的缓存
			if (cacheFile.exists()) {
				EvtLog.i("AlexGIF", "本图片有缓存");
				GifDrawable gifFrom = new GifDrawable(cacheFile);
				int raw_height = gifFrom.getIntrinsicHeight();
				int raw_width = gifFrom.getIntrinsicWidth();
				//如果缓存文件可以使用，则直接显示
				if (!(raw_width < 1 || raw_height < 1)) {
					if (tvProgress != null) {
						tvProgress.setVisibility(View.GONE);
					}
					displyGifDrawable(gifFrom, gifView, displayWidth, gifPlayListener);
					return;
				}
			}
			//为了防止activity被finish了但是还有很多gif还没有加载完成，导致activity没有及时被内存回收导致内存泄漏，这里使用弱引用
			final WeakReference<GifImageView> imageViewWait = new WeakReference<GifImageView>(gifView);
			final WeakReference<TextView> textViewWait = new WeakReference<TextView>(tvProgress);
//		gifView.setImageResource(R.drawable.bg_user_default);//设置没有下载完成前的默认图片
			//如果以前有别的imageView加载过
			if (mDisplayViewMap != null && mDisplayViewMap.get(url) != null) {
				EvtLog.i("AlexGIF", "以前有别的ImageView申请加载过该gif" + url);
				//可以借用以前的下载进度，不需要新建一个下载线程了
				mDisplayViewMap.get(url).add(new ProgressViews(imageViewWait, textViewWait, displayWidth));
				return;
			}
			if (mDisplayViewMap.get(url) == null) {
				mDisplayViewMap.put(url, new ArrayList<ProgressViews>());
			}
			//将现在申请加载的这个imageView放到缓存里，防止重复加载
			mDisplayViewMap.get(url).add(new ProgressViews(imageViewWait, textViewWait, displayWidth));

			final String cacheFileAbsPath = cacheFile.getAbsolutePath() + ".tmp";
			mLoadTask.loadTask(url, cacheFileAbsPath, new LoadingListener() {

				@Override
				public void onLoadingProcess(final long total, final long current) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							int progress = 0;
							//得到要下载文件的大小，是通过http报文的header的Content-Length获得的，如果获取不到就是-1
							if (total > 0) progress = (int) (current * 100 / total);
							EvtLog.i("AlexGIF", "下载gif的进度是" + progress + "%" + "    现在大小" + current + "   总大小" + total);
							ArrayList<ProgressViews> viewses = mDisplayViewMap.get(url);
							if (viewses == null) return;
							EvtLog.i("AlexGIF", "该gif的请求数量是" + viewses.size());
							for (ProgressViews vs : viewses) {//遍历所有的进度条，修改同一个url请求的进度显示
								TextView tvProgress = vs.textViewWeakReference.get();
								if (tvProgress != null) tvProgress.setText(progress + "%");
								//显示第一帧直到全部下载完之后开始动画
								getFirstPicOfGIF(new File(cacheFileAbsPath), vs.gifImageViewWeakReference.get());
							}
						}
					});
				}

				public void onLoadingSuccess(final String tartetPath) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							if (tartetPath == null) return;
							if (tartetPath == null || tartetPath.length() < 5) return;
							File downloadFile = new File(tartetPath);
							File renameFile = new File(tartetPath.substring(0, tartetPath.length() - 4));
							if (tartetPath.endsWith(".tmp"))
								downloadFile.renameTo(renameFile);//将.tmp后缀去掉
							EvtLog.i("AlexGIF", "下载GIf成功,文件路径是" + tartetPath + " 重命名之后是" + renameFile.getAbsolutePath());
							ArrayList<ProgressViews> viewArr = mDisplayViewMap.get(url);
							if (viewArr == null || viewArr.size() == 0) return;
							for (ProgressViews ws : viewArr) {//遍历所有的进度条和imageView，同时修改所有请求同一个url的进度
								//显示imageView
								GifImageView gifImageView = ws.gifImageViewWeakReference.get();
								if (gifImageView != null) {
									try {
										displyGifDrawable(new GifDrawable(renameFile), gifImageView, ws.displayWidth, gifPlayListener);
									} catch (IOException e) {
										e.printStackTrace();
										if (gifPlayListener != null) {
											gifPlayListener.onPlayFailed();
										}
									}
								}
								//修改进度条
								TextView tvProgress = ws.textViewWeakReference.get();
								if (tvProgress != null) tvProgress.setVisibility(View.GONE);
							}
							EvtLog.i("AlexGIF", url + "的imageView已经全部加载完毕，共有" + viewArr.size() + "个");
							mDisplayViewMap.remove(url);//这个url的全部关联imageView都已经显示完毕，清除缓存记录
						}
					});
				}

				@Override
				public void onLoadingFailure(Throwable e) {
					EvtLog.i("Alex", e.toString());
					handler.post(new Runnable() {
						@Override
						public void run() {
							TextView tvProgress = textViewWait.get();
							if (tvProgress != null) tvProgress.setText("image download failed");
							if (mDisplayViewMap != null) mDisplayViewMap.remove(url);//下载失败移除所有的弱引用
							if (gifPlayListener != null) {
								gifPlayListener.onPlayFailed();
							}
						}
					});

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			if (gifPlayListener != null) {
				gifPlayListener.onPlayFailed();
			}
		}
	}

	/**
	 * 根据本地gif文件，显示图片到GifImageView
	 *
	 * @param gifFrom      gif 动画
	 * @param gifImageView imageView控件的宽度，用于根据gif的实际高度重设控件的高度来保证完整显示，
	 * @param displayWidth 传0表示不缩放gif的大小，显示原始尺寸
	 */
	private void displyGifDrawable(GifDrawable gifFrom, GifImageView gifImageView, int displayWidth, final GifPlayListener gifPlayListener) {
		if (gifFrom == null)
			throw new NullPointerException("GifDrawable is null");
		int raw_height = gifFrom.getIntrinsicHeight();
		int raw_width = gifFrom.getIntrinsicWidth();
		//如果缓存文件可以使用，则直接显示
		if ((raw_width < 1 || raw_height < 1)) {
			throw new IllegalStateException("GifDrawable no available");
		}
		gifFrom.addAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationCompleted(int loopNumber) {
				if (gifPlayListener != null) {
					gifPlayListener.onPlayComplete();
				}
			}
		});
		if (gifPlayListener != null) {
			gifPlayListener.onPlayStarted();
		}
		if (gifImageView.getScaleType() != ImageView.ScaleType.CENTER_CROP && gifImageView.getScaleType() != ImageView.ScaleType.FIT_XY) {
			int imageViewWidth = displayWidth;
			if (imageViewWidth < 1)
				imageViewWidth = gifFrom.getIntrinsicWidth();//当传来的控件宽度小于1就显示控件宽度
			int imageViewHeight = imageViewWidth;    // 高度 = 宽度
			EvtLog.i("AlexGIF", "缩放完的gif是" + imageViewWidth + " X " + imageViewHeight);
			ViewGroup.LayoutParams params = gifImageView.getLayoutParams();
			if (params != null) {
				params.height = imageViewHeight;
				params.width = imageViewWidth;
			}
		} else {
			EvtLog.i("AlexGIF", "按照固定大小进行显示");
		}
		gifImageView.setImageDrawable(gifFrom);
	}

	/**
	 * 加载gif的第一帧图像，用于下载完成前占位
	 *
	 * @param gifFile
	 * @param imageView
	 */
	public void getFirstPicOfGIF(File gifFile, GifImageView imageView) {
		if (imageView == null) return;
		if (imageView.getTag(R.id.tag_first) instanceof Integer) return;//之前已经显示过第一帧了，就不用再显示了
		try {
			GifDrawable gifFromFile = new GifDrawable(gifFile);
			boolean canSeekForward = gifFromFile.canSeekForward();
			if (!canSeekForward) return;
			EvtLog.i("AlexGIF", "是否能显示第一帧图片" + canSeekForward);
			//下面是一些其他有用的信息
//            int frames = gifFromFile.getNumberOfFrames();
//            Log.i("AlexGIF","已经下载完多少帧"+frames);
//            int bytecount = gifFromFile.getFrameByteCount();
//            Log.i("AlexGIF","一帧至少多少字节"+bytecount);
//            long memoryCost = gifFromFile.getAllocationByteCount();
//            Log.i("AlexGIF","内存开销是"+memoryCost);
			gifFromFile.seekToFrame(0);
			gifFromFile.pause();//静止在该帧
			imageView.setImageDrawable(gifFromFile);
			imageView.setTag(R.id.tag_first, 1);//标记该imageView已经显示过第一帧了
		} catch (IOException e) {
			EvtLog.i("AlexGIF", "获取gif信息出现异常" + e);
		}
	}

	public class ProgressViews {
		public ProgressViews(WeakReference<GifImageView> gifImageViewWeakReference, WeakReference<TextView> textViewWeakReference, int displayWidth) {
			this.gifImageViewWeakReference = gifImageViewWeakReference;
			this.textViewWeakReference = textViewWeakReference;
			this.displayWidth = displayWidth;
		}

		public WeakReference<GifImageView> gifImageViewWeakReference;//gif显示控件
		public WeakReference<TextView> textViewWeakReference;//用来显示当前进度的文本框
		public int displayWidth;//imageView的控件宽度
	}
}
