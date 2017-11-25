# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\components\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-dontoptimize
-dontpreverify
-dontwarn android.support.**
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-ignorewarning
-verbose
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/simplification/arithmetic,!field/*,!class/merging/*

-keepattributes *Annotation*
-keepattributes *JavascriptInterface*
-keep class android.support.v4.**{ *; }
-keep class android.support.multidex.**{ *; }
-keep public class * extends android.app.Application
-keep class vi.com.gdi.bgl.android.java.**{*;}
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.widget.BaseAdapter


-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep class com.sina.sso.**{*;}
-keep class com.baidu.**{ *; }
-keep class m.framework.**{ *; }
-keep class com.geogle.gson.**{ *; }
-keep class and.zhima.babymachine.index.config.**{ *; }
-keep class com.efeizao.feizao.model.**{ *; }
-keep class com.efeizao.feizao.live.model.**{ *; }
-keep class com.efeizao.feizao.live.model.http.**{ *; }
-keep class com.efeizao.feizao.adapters.**{ *; }
#此类需要通过json对应转换，不能被混淆
-keep class com.efeizao.feizao.database.model.PersonInfo{*;}
-keep class and.zhima.babymachine.network.websocket.model.**{*;}
-keep class and.zhima.babymachine.network.**{ *; }
-keep class and.zhima.babymachine.index.model.**{*;}
-keep class and.zhima.babymachine.index.model.http.**{*;}
-keep class and.zhima.babymachine.live.model.**{*;}
-keep class and.zhima.babymachine.live.model.http.**{*;}
-keep class and.zhima.babymachine.user.model.**{*;}
-keep class and.zhima.babymachine.user.model.http.**{*;}
-keep class and.zhima.babymachine.index.config.**{*;}
# 此GT工具类不能被混淆
-keep class com.efeizao.feizao.common.GTValidateRequest{*;}

-keep class com.lonzh.lib.network.**{ *; }

# 保持自定义控件类不被混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
# 所有native的方法不能去混淆.
-keepclasseswithmembernames class * {
    native <methods>;
}

# 所有run()的方法不能去混淆.
-keepclasseswithmembernames class * {
    public void run();
}

-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
# 友盟等需要通过了反射机制得到资源引用文件R.java
-keep public class com.efeizao.feizao.R$*{
	public static final int *;
}

# 友盟如果您使用5.0.0及以上版本的SDK
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep public class com.lonzh.lib.LZFFmpeg {
    <fields>;
    <methods>;
}

-keepclassmembers class com.efeizao.feizao.ui.MyWebView$WebChromeClient {
  public *;
}

#webveiw js调用本地方法，避免被混淆
-keepclassmembers class com.efeizao.feizao.activities.WebBindActivity$OnPay {
  public *;
}
-keepclassmembers class com.efeizao.feizao.activities.RechargeWebActivity$OnPay {
  public *;
}

-keepclassmembers class com.efeizao.feizao.activities.RechargeWebActivity$OnAliPay {
  public *;
}

-keepclassmembers class com.efeizao.feizao.activities.WebViewActivity$JsInvokeMainClass {
  public *;
}

-keepclassmembers class com.efeizao.feizao.activities.WebViewActivity$JsInvokeClass {
  public *;
}

-keepclassmembers class com.efeizao.feizao.live.activities.LiveBaseActivity$JsInvokeMainClass {
  public *;
}


-keep public class com.efeizao.feizao.common.Utils {
   public <methods>;
}

-keep public class * implements java.io.Serializable
-keep public class * implements org.apache.http.client.CookieStore

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*

# OrmLite uses reflection
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }

# com.nostra13.universalimageloader uses reflection
-keep class com.nostra13.universalimageloader.**
-keepclassmembers class com.nostra13.universalimageloader.** { *; }
-keep enum com.nostra13.universalimageloader.**
-keepclassmembers enum com.nostra13.universalimageloader.** { *; }
-keep interface com.nostra13.universalimageloader.**
-keepclassmembers interface com.nostra13.universalimageloader.** { *; }
#umeng开始
-dontusemixedcaseclassnames
-dontshrink
-dontoptimize
-dontwarn com.google.android.maps.**
-dontwarn android.webkit.WebView
-dontwarn com.umeng.**
-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.facebook.**
-keep public class javax.**
-keep public class android.webkit.**
-dontwarn android.support.v4.**
-keep enum com.facebook.**
-keepattributes Exceptions,InnerClasses,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

-keep public interface com.facebook.**
-keep public interface com.tencent.**
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**
-keep class com.android.dingtalk.share.ddsharemodule.** { *; }
-keep public class com.umeng.socialize.* {*;}

-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.umeng.scrshot.**
-keep public class com.tencent.** {*;}
-keep class com.umeng.socialize.sensor.**
-keep class com.umeng.socialize.handler.**
-keep class com.umeng.socialize.handler.*
-keep class com.umeng.weixin.handler.**
-keep class com.umeng.weixin.handler.*
-keep class com.umeng.qq.handler.**
-keep class com.umeng.qq.handler.*
-keep class UMMoreHandler{*;}
-keep class com.tencent.mm.sdk.modelmsg.WXMediaMessage {*;}
-keep class com.tencent.mm.sdk.modelmsg.** implements   com.tencent.mm.sdk.modelmsg.WXMediaMessage$IMediaObject {*;}
-keep class im.yixin.sdk.api.YXMessage {*;}
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{*;}
-keep class com.tencent.mm.sdk.** {
 *;
}
-keep class com.tencent.mm.opensdk.** {
*;
}
-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

-keep class com.tencent.** {*;}
-dontwarn com.tencent.**
-keep public class com.umeng.com.umeng.soexample.R$*{
public static final int *;
}
-keep public class com.linkedin.android.mobilesdk.R$*{
public static final int *;
    }
-keepclassmembers enum * {
public static **[] values();
public static ** valueOf(java.lang.String);
}

-keep class com.tencent.open.TDialog$*
-keep class com.tencent.open.TDialog$* {*;}
-keep class com.tencent.open.PKDialog
-keep class com.tencent.open.PKDialog {*;}
-keep class com.tencent.open.PKDialog$*
-keep class com.tencent.open.PKDialog$* {*;}

-keep class com.sina.** {*;}
-dontwarn com.sina.**
-keep class  com.alipay.share.sdk.** {
   *;
}
-keepnames class * implements android.os.Parcelable {
public static final ** CREATOR;
}

-keep class com.linkedin.** { *; }
-keepattributes Signature
#umeng结束

#直播间
-keep class org.videolan.** {*;}
-keep class io.vov.vitamio.** {*;}

#极光
-dontoptimize
-dontpreverify

-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }
#==================gson==========================
-dontwarn com.google.**
-keep class com.google.gson.** {*;}
#==================protobuf======================
-dontwarn com.google.**
-keep class com.google.protobuf.** {*;}

#tusdk的混淆
-keep class it.sephiroth.android.library.exif2.**{ *; }
-keep class org.lasque.tusdk.**{public *; protected *; }
-keep class org.lasque.tusdk.core.utils.image.GifHelper{ *; }

#支付宝支付
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}

#pldroid播放器
-keep class com.pili.pldroid.player.**{ *; }
-keep class tv.danmaku.ijk.media.player.**{ *; }
-keep class com.tencent.**{ *; }

# Addidional for x5.sdk classes for apps

-keep class com.tencent.smtt.export.external.**{
    *;
}

-keep class com.tencent.tbs.video.interfaces.IUserStateChangedListener {
	*;
}

-keep class com.tencent.smtt.sdk.CacheManager {
	public *;
}

-keep class com.tencent.smtt.sdk.CookieManager {
	public *;
}

-keep class com.tencent.smtt.sdk.WebHistoryItem {
	public *;
}

-keep class com.tencent.smtt.sdk.WebViewDatabase {
	public *;
}

-keep class com.tencent.smtt.sdk.WebBackForwardList {
	public *;
}

-keep public class com.tencent.smtt.sdk.WebView {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebView$HitTestResult {
	public static final <fields>;
	public java.lang.String getExtra();
	public int getType();
}

-keep public class com.tencent.smtt.sdk.WebView$WebViewTransport {
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebView$PictureListener {
	public <fields>;
	public <methods>;
}


-keepattributes InnerClasses

-keep public enum com.tencent.smtt.sdk.WebSettings$** {
    *;
}

-keep public class com.tencent.smtt.sdk.WebSettings {
    public *;
}


-keepattributes Signature
-keep public class com.tencent.smtt.sdk.ValueCallback {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebViewClient {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.DownloadListener {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebChromeClient {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebChromeClient$FileChooserParams {
	public <fields>;
	public <methods>;
}

-keep class com.tencent.smtt.sdk.SystemWebChromeClient{
	public *;
}
# 1. extension interfaces should be apparent
-keep public class com.tencent.smtt.export.external.extension.interfaces.* {
	public protected *;
}

# 2. interfaces should be apparent
-keep public class com.tencent.smtt.export.external.interfaces.* {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.WebViewCallbackClient {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.WebStorage$QuotaUpdater {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebIconDatabase {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.WebStorage {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.DownloadListener {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.QbSdk {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.QbSdk$PreInitCallback {
	public <fields>;
	public <methods>;
}
-keep public class com.tencent.smtt.sdk.CookieSyncManager {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.Tbs* {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.LogFileUtils {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.TbsLog {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.TbsLogClient {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.CookieSyncManager {
	public <fields>;
	public <methods>;
}

# Added for game demos
-keep public class com.tencent.smtt.sdk.TBSGamePlayer {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGamePlayerClient* {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGamePlayerClientExtension {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGamePlayerService* {
	public <fields>;
	public <methods>;
}

-keep public class com.tencent.smtt.utils.Apn {
	public <fields>;
	public <methods>;
}
# end


-keep public class com.tencent.smtt.export.external.extension.proxy.ProxyWebViewClientExtension {
	public <fields>;
	public <methods>;
}

-keep class MTT.ThirdAppInfoNew {
	*;
}

-keep class com.tencent.mtt.MttTraceEvent {
	*;
}

# Game related
-keep public class com.tencent.smtt.gamesdk.* {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.TBSGameBooter {
        public <fields>;
        public <methods>;
}

-keep public class com.tencent.smtt.sdk.TBSGameBaseActivity {
	public protected *;
}

-keep public class com.tencent.smtt.sdk.TBSGameBaseActivityProxy {
	public protected *;
}

-keep public class com.tencent.smtt.gamesdk.internal.TBSGameServiceClient {
	public *;
}
# x5 sdk end

#RongCloud start
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
 public *;
}
-keepattributes Exceptions,InnerClasses

-keepattributes Signature

-keepattributes *Annotation*

-keep class com.google.gson.examples.android.model.** { *; }

-keep class **$Properties
-dontwarn org.eclipse.jdt.annotation.**

-keep class io.rong.** {*;}
-keep class * implements io.rong.imlib.model.MessageContent{*;}
-keep class com.efeizao.feizao.rongcloud.fragment.**{*;}

-dontwarn io.rong.push.**
-dontnote com.xiaomi.**
-dontnote com.huawei.android.pushagent.**
-dontnote com.google.android.gms.gcm.**
-dontnote io.rong.**
#-ignorewarnings
#RongCloud end
#gif drawable start
-keep public class pl.droidsonroids.gif.GifIOException{<init>(int);}
-keep class pl.droidsonroids.gif.GifInfoHandle{<init>(long,int,int,int);}
#gif drawable end
#高德定位
-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}
#高德定位
#jackson
-keepattributes *EnclosingMethod*
-keep class org.codehaus.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepclassmembers public final enum org.codehaus.jackson.annotate.JsonAutoDetect$Visibility {
public static final org.codehaus.jackson.annotate.JsonAutoDetect$Visibility *; }

#TONGDUN
-dontwarn android.os.**
-dontwarn com.android.internal.**
-keep class cn.tongdun.android.**{*;}
#Album
-dontwarn com.yanzhenjie.album.**
-keep class com.yanzhenjie.album.**{*;}

#Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#极验验证
-keep class org.greenrobot.eventbus.**{*;}
-keep class com.example.sdk.**{*;}
