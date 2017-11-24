package tv.live.bx.common;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.AppOpsManagerCompat;

/**
 * Created by Live on 2017/6/20.
 *
 * @descrition: 权限判断/申请
 */

public class PermissionUtil {
	public static final int REQUEST_PERMISSION_CAMERA = 0x1001;
	public static final int REQUEST_PERMISSION_RECORD_AUDIO = 0x1002;
	public static final int REQUEST_PERMISSION_CONNECT = 0x1003;
	public static final int REQUEST_PERMISSION_RECEIVE = 0x1004;
	//	public static final int REQUEST_PERMISSION_RECORD_AUDIO_RECEIVE = 0x1005;
	public static final String[] PERMISSION_LIVES = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};

	// 判断权限集合
	public static boolean permissionIsGranted(Context context, String... permissions) {
		for (String permission : permissions) {
			if (!permissionIsGranted(context, permission)) {
				return false;
			} else {
				// 如果权限允许（这种情况是有问题的）
				// 在判断一次是否被允许，如果被允许，判断下一个权限
				if (hasOpsPermission(context, permission)) {
					continue;
				} else {
					// 如果不被允许，直接回调
					return false;
				}
			}
		}
		return true;
	}

	// 判断是否缺少权限
	private static boolean permissionIsGranted(Context context, String permission) {
		return ActivityCompat.checkSelfPermission(context, permission)
				== PackageManager.PERMISSION_GRANTED;
	}

	@TargetApi(Build.VERSION_CODES.M)
	public static void requestPermissions(Activity context, int requestCode, String... permissions) {
		context.requestPermissions(permissions, requestCode);
	}

	/**
	 * 获取权限，如果没有权限，则去申请
	 *
	 * @param context
	 * @param requestCode
	 * @param permissions
	 */
	public static boolean permissionGrantedAndRequest(Activity context, int requestCode, String... permissions) {
		boolean flag = permissionIsGranted(context, permissions);
		if (!flag) {
			requestPermissions(context, requestCode, permissions);
		}
		return flag;
	}

	/**
	 * Android6.0权限申请后再判断原生的权限是否真的被授权--适配部分国产机型（小米、华为、vivo、oppo等）
	 *
	 * @param context
	 * @param permissions
	 * @return
	 */
	private static boolean hasOpsPermission(@NonNull Context context, @NonNull String... permissions) {
		for (String permission : permissions) {
			String op = AppOpsManagerCompat.permissionToOp(permission);
			int result = AppOpsManagerCompat.noteProxyOp(context, op, context.getPackageName());
			if (result == AppOpsManagerCompat.MODE_ALLOWED) return true;
		}
		return false;
	}
}
