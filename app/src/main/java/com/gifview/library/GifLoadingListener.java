package com.gifview.library;

/**
 * Created by Administrator on 2016/11/11.
 */

import java.io.File;

/**
 * gif加载监听类
 */
public interface GifLoadingListener {
	void onStart();

	void onLoadingProcess(long total, long current);

	void onLoadingSuccess(File target);

	void onLoadingFailure(Throwable e);
}
