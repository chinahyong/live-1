package tv.live.bx.common;

import tv.live.bx.FeizaoApp;

import android.util.Log;


/**
 * 打印日志
* @author Lisper  
* @date 2015-4-3 上午11:25:13 
* @version V1.0
 */
public class LogUtil {
	public static void i(String tag,String msg)
	{
		if (FeizaoApp.isDebug)
		{
			Log.i(tag,msg);
		}
	}
	public  static void i(String tag,long msg)
	{
		i(tag,String.valueOf(msg));
	}
	public  static void i(String tag,int msg)
	{
		i(tag,String.valueOf(msg));
	}
	public  static void i(String tag,boolean msg)
	{
		i(tag,String.valueOf(msg));
	}
}
