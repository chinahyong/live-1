/**
 * Project Name:feizao File Name:CacheConstants.java Package
 * Name:com.efeizao.feizao.cache Date:2015-6-16下午4:42:04
 */

package tv.live.bx.cache;

import tv.live.bx.FeizaoApp;
import tv.live.bx.common.Constants;
import tv.live.bx.library.util.FileUtil;

/**
 * ClassName:CacheConstants Function: TODO ADD FUNCTION. Reason: TODO ADD
 * REASON. Date: 2015-6-16 下午4:42:04
 * @author Live
 * @version 1.0
 */
public class CacheConstants {
	
	public static final int IMG_CACHE_LIMIT = 500; // 图片缓存大小
	public static final String IMG_CACHE_PATH = FileUtil.getDiskCachePath(
			FeizaoApp.mConctext, Constants.IMAGE_CACHE_DIR); // 图片缓存目录

	/** 默认线程池大小为4 */
	public static final int THREAD_POOL_SIZE = 4;
	/** 显示图片的最大宽度 */
	public static final int PIC_MAX_SHOW_SIZE = 720;
}
