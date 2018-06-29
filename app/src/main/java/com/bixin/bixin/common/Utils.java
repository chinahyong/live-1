package com.bixin.bixin.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.bixin.bixin.App;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import junit.framework.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import tv.guojiang.baselib.image.listener.ImageLoadingListener;
import tv.guojiang.baselib.image.model.ImageSize;
import tv.live.bx.R;
import com.bixin.bixin.user.act.LoginActivity;
import com.bixin.bixin.activities.Register1Activity;
import com.bixin.bixin.config.AppConfig;
import com.bixin.bixin.imageloader.ImageLoaderUtil;
import com.bixin.bixin.ui.VerticalImageSpan;
import com.bixin.bixin.util.ActivityJumpUtil;
import com.bixin.bixin.util.UiHelper;


public class Utils {

    @SuppressWarnings("deprecation")
    public static int[] getScreenWH(Context poCotext) {
        WindowManager wm = (WindowManager) poCotext.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        return new int[]{width, height};
    }

    public static boolean isStrEmpty(String psStr) {
        return psStr == null || psStr.trim().length() == 0;
    }

    /**
     * 读取配置信息
     */
    public static String getCfg(Context poContext, String psKey) {
        SharedPreferences loCfg = poContext.getSharedPreferences(Constants.COMMON_SF_NAME, 0);
        return loCfg.getString(psKey, null);
    }

    /**
     * 读取配置信息
     */
    public static String getCfg(Context poContext, String psCfgName, String psKey) {
        SharedPreferences loCfg = poContext.getSharedPreferences(psCfgName, 0);
        return loCfg.getString(psKey, null);
    }

    /**
     * 读取配置信息
     */
    public static String getCfg(Context poContext, String psCfgName, String psKey,
        String defaultValue) {
        SharedPreferences loCfg = poContext.getSharedPreferences(psCfgName, 0);
        return loCfg.getString(psKey, defaultValue);
    }

    /**
     * 获取配置信息
     */
    public static Map<String, ? extends Object> getCfgMap(Context poContext, String psCfgName) {
        SharedPreferences loCfg = poContext.getSharedPreferences(psCfgName, 0);
        return loCfg.getAll();
    }

    /**
     * 判断一个字符串是否为"true"，不区分大小写
     */
    public static boolean strBool(String psBool) {
        return psBool != null && Boolean.parseBoolean(psBool);
    }

    /**
     * 设置配置信息
     */
    public static void setCfg(Context poContext, String psKey, String psValue) {
        setCfg(poContext, Constants.COMMON_SF_NAME, psKey, psValue);
    }

    /**
     * 设置配置信息
     */
    public static void setCfg(Context poContext, String psCfgName, String psKey, String psValue) {
        Map<String, String> lmCfg = new HashMap<>();
        lmCfg.put(psKey, psValue);
        setCfg(poContext, psCfgName, lmCfg);
    }

    /**
     * 设置配置信息
     */
    public static void setCfg(Context poContext, String psCfgName, Map<String, String> pmCfg) {
        SharedPreferences loCfg = poContext.getSharedPreferences(psCfgName, 0);
        Editor loEditor = loCfg.edit();
        Iterator<String> loIterator = pmCfg.keySet().iterator();
        while (loIterator.hasNext()) {
            String lsKey = loIterator.next();
            String lsValue = pmCfg.get(lsKey);
            if (lsValue != null) {
                loEditor.putString(lsKey, lsValue);
            } else {
                loEditor.remove(lsKey);
            }
        }

        loEditor.commit();
    }

    /**
     * 清空配置信息
     */
    public static void clearCfg(Context poContext, String psCfgName) {
        SharedPreferences loCfg = poContext.getSharedPreferences(psCfgName, 0);
        Editor loEditor = loCfg.edit();
        loEditor.clear();
        loEditor.commit();
    }

    /**
     * 显示进度弹出框
     */
    @SuppressLint("InflateParams")
    public static AlertDialog showProgress(Activity poActivity) {
        final AlertDialog loDialog = new AlertDialog.Builder(poActivity).create();
        LayoutInflater loInflater = poActivity.getLayoutInflater();
        loDialog.setView(loInflater.inflate(R.layout.dialog_progress, null));
        loDialog.show();
        loDialog.getWindow().setContentView(R.layout.dialog_progress);
        loDialog.setCanceledOnTouchOutside(false);
        loDialog.setCancelable(false);
        ImageView loIv = (ImageView) loDialog.findViewById(R.id.dialog_progress_iv);
        Animation loAnimRotate = AnimationUtils.loadAnimation(poActivity, R.anim.rotate_clockwise);
        LinearInterpolator loLin = new LinearInterpolator();
        loAnimRotate.setInterpolator(loLin);
        loIv.startAnimation(loAnimRotate);
        return loDialog;
    }

    /**
     * 获取版本名称
     */
    public static String getVersionName(Context poContext) {
        PackageManager loPm = poContext.getPackageManager();
        PackageInfo loPi;
        try {
            loPi = loPm.getPackageInfo(poContext.getPackageName(), 0);
            return loPi.versionName;
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 获取版本号
     */
    public static int getVersionCode(Context poContext) {
        PackageManager loPm = poContext.getPackageManager();
        PackageInfo loPi;
        try {
            loPi = loPm.getPackageInfo(poContext.getPackageName(), 0);
            return loPi.versionCode;
        } catch (NameNotFoundException e) {
            return 0;
        }
    }

    /**
     * 判断当前网络是否可用
     */
    public static boolean isNetAvailable(Context poContext) {
        ConnectivityManager loConnMgr = (ConnectivityManager) poContext
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo loNetInfo = loConnMgr.getActiveNetworkInfo();
        if (loNetInfo != null) {
            return loNetInfo.isAvailable();
        }
        return false;
    }

    /**
     * 解析表情文字
     */
    public static SpannableString parseEmotionText(Context poContext, CharSequence psText) {
        Pattern loPtn = Pattern.compile("(\\{#\\d+#\\}|\\{花\\})");
        Matcher loMatcher = loPtn.matcher(psText);
        SpannableString loSs = new SpannableString(psText);
        while (loMatcher.find()) {
            Integer liRes = Consts.TEXT_EMOTION_MAP.get(loMatcher.group());
            if (liRes != null) {
                Drawable loDrawable = poContext.getResources().getDrawable(liRes);
                loDrawable
                    .setBounds(0, 0, Utils.dip2px(poContext, 26), Utils.dip2px(poContext, 26));
                VerticalImageSpan loIs = new VerticalImageSpan(loDrawable);
                loSs.setSpan(loIs, loMatcher.start(), loMatcher.end(),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return loSs;
    }

    /**
     * 换一种方式显示表情,试试效果是否好一些
     */
    public static SpannableStringBuilder replaceEmotionText(Context poContext, CharSequence text) {
        Pattern loPtn = Pattern.compile("(\\{#\\d+#\\}|\\{花\\})");
        Matcher matcher = loPtn.matcher(text);
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        try {
            while (matcher.find()) {
                Integer id = Consts.TEXT_EMOTION_MAP.get(matcher.group());
                Bitmap bitmap = BitmapFactory.decodeResource(poContext.getResources(), id);
                if (bitmap != null) {
                    ImageSpan span = new ImageSpan(poContext, bitmap);
                    builder.setSpan(span, matcher.start(), matcher.end(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

        } catch (Exception e) {
        }
        return builder;
    }

    /**
     * 获取图片的id
     * @param type,id 组成图片的名称
     */
    public static int getFiledDrawable(String type, String id) {
        try {
            // 根据随机产生的1至9的整数从R.drawable类中获得相应资源ID（静态变量）的Field对象
            Field field = R.drawable.class.getDeclaredField(type + id);
            // 获得资源ID的值，也就是静态变量的值
            int resourceId = Integer.parseInt(field.get(null).toString());
            return resourceId;

        } catch (Exception e) {
        }
        return -1;
    }

    /**
     * EditText左侧清空控件
     */
    public static void initEtClearView(final EditText poEt, final View poVClear) {
        poEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    poVClear.setVisibility(View.VISIBLE);
                } else {
                    poVClear.setVisibility(View.INVISIBLE);
                }
            }
        });

        poVClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                poEt.getText().clear();
            }
        });
    }

    /**
     * @param psResponse
     * @return
     * @throws JSONException
     */
    public static Map<String, Object> parseResponseHead(int piResponseType, String psResponse)
        throws JSONException {
        JSONObject loJsonObj = new JSONObject(psResponse);
        Map<String, Object> lmResult = new HashMap<String, Object>();
        int liErrCode = loJsonObj.getInt("errno");
        if (liErrCode == 0) { // 成功
            lmResult.put("success", true);
            switch (piResponseType) {
                case Business.RESPONSE_TYPE_MULTI:
                case Business.RESPONSE_TYPE_MULTI_IN_MULTI:
                case Business.RESPONSE_TYPE_SINGLE_IN_MULTI:
                    lmResult.put("result", loJsonObj.getJSONArray("data"));
                    break;
                case Business.RESPONSE_TYPE_SINGLE:
                case Business.RESPONSE_TYPE_MULTI_IN_SINGLE:
                    lmResult.put("result", loJsonObj.getJSONObject("data"));
                    break;
            }
        } else { // 失败
            lmResult.put("success", false);
            lmResult.put("msg", loJsonObj.getString("msg"));
            lmResult.put("errno", liErrCode);
        }
        return lmResult;
    }

    @SuppressLint({"TrulyRandom", "NewApi"})
    public static String rsaEncrypt(String psPubKey, String psText)
        throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 1 生成公钥
        X509EncodedKeySpec loPubKeySpec = new X509EncodedKeySpec(
            Base64.decode(psPubKey, Base64.NO_WRAP));
        KeyFactory loKeyFactory = KeyFactory.getInstance("RSA");
        PublicKey loPubKey = loKeyFactory.generatePublic(loPubKeySpec);

        // 2 RSA加密
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, loPubKey);
        byte[] laSecByte = cipher.doFinal(psText.getBytes());
        return Base64.encodeToString(laSecByte, Base64.NO_WRAP).trim();
    }

    /*****************************
     * QQ登陆
     ********************************/

    private static final String TAG = "SDK_Sample.Utils";
    private static Dialog mProgressDialog;
    private static Toast mToast;

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase(Locale.CHINA);
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /* 16进制数字字符集 */
    private static String hexString = "0123456789ABCDEF";

    /* 将字符串编码成16进制数字,适用于所有字符（包括中文） */
    public static String toHexString(String str) {
        // 根据默认编码获取字节数组
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 将字节数组中每个字节拆解成2位16进制整数
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    // 转换十六进制编码为字符串
    public static String hexToString(String s) {
        if ("0x".equals(s.substring(0, 2))) {
            s = s.substring(2);
        }
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");// UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] getHtmlByteArray(final String url) {
        URL htmlUrl = null;
        InputStream inStream = null;
        try {
            htmlUrl = new URL(url);
            URLConnection connection = htmlUrl.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inStream = httpConnection.getInputStream();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] data = inputStreamToByte(inStream);
        return data;
    }

    public static byte[] inputStreamToByte(InputStream is) {
        try {
            ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
            int ch;
            while ((ch = is.read()) != -1) {
                bytestream.write(ch);
            }
            byte imgdata[] = bytestream.toByteArray();
            bytestream.close();
            return imgdata;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] readFromFile(String fileName, int offset, int len) {
        if (fileName == null) {
            return null;
        }

        File file = new File(fileName);
        if (!file.exists()) {
            Log.i(TAG, "readFromFile: file not found");
            return null;
        }
        if (len == -1) {
            len = (int) file.length();
        }
        Log.d(TAG,
            "readFromFile : offset = " + offset + " len = " + len + " offset + len = " + (offset
                + len));
        if (offset < 0) {
            Log.e(TAG, "readFromFile invalid offset:" + offset);
            return null;
        }
        if (len <= 0) {
            Log.e(TAG, "readFromFile invalid len:" + len);
            return null;
        }
        if (offset + len > (int) file.length()) {
            Log.e(TAG, "readFromFile invalid file len:" + file.length());
            return null;
        }
        byte[] b = null;
        try {
            RandomAccessFile in = new RandomAccessFile(fileName, "r");
            b = new byte[len];
            in.seek(offset);
            in.readFully(b);
            in.close();
        } catch (Exception e) {
            Log.e(TAG, "readFromFile : errMsg = " + e.getMessage());
            e.printStackTrace();
        }
        return b;
    }

    public static int computeSampleSize(BitmapFactory.Options options,

        int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength,
        int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;
        int lowerBound =
            (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128
            : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }

    /**
     * 以最省内存的方式读取图片
     */
    public static Bitmap readBitmap(final String path) {
        try {
            FileInputStream stream = new FileInputStream(new File(path + "test.jpg"));
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 8;
            opts.inPurgeable = true;
            opts.inInputShareable = true;
            Bitmap bitmap = BitmapFactory.decodeStream(stream, null, opts);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private static final int MAX_DECODE_PICTURE_SIZE = 1920 * 1440;

    public static Bitmap extractThumbNail(final String path, final int height, final int width,
        final boolean crop) {
        Assert.assertTrue(path != null && !path.equals("") && height > 0 && width > 0);
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            options.inJustDecodeBounds = true;
            Bitmap tmp = BitmapFactory.decodeFile(path, options);
            if (tmp != null) {
                tmp.recycle();
                tmp = null;
            }
            Log.d(TAG, "extractThumbNail: round=" + width + "x" + height + ", crop=" + crop);
            final double beY = options.outHeight * 1.0 / height;
            final double beX = options.outWidth * 1.0 / width;
            Log.d(TAG, "extractThumbNail: extract beX = " + beX + ", beY = " + beY);
            options.inSampleSize = (int) (crop ? (beY > beX ? beX : beY) : (beY < beX ? beX : beY));
            if (options.inSampleSize <= 1) {
                options.inSampleSize = 1;
            }

            // NOTE: out of memory error
            while (options.outHeight * options.outWidth / options.inSampleSize
                > MAX_DECODE_PICTURE_SIZE) {
                options.inSampleSize++;
            }
            int newHeight = height;
            int newWidth = width;
            if (crop) {
                if (beY > beX) {
                    newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
                } else {
                    newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
                }
            } else {
                if (beY < beX) {
                    newHeight = (int) (newWidth * 1.0 * options.outHeight / options.outWidth);
                } else {
                    newWidth = (int) (newHeight * 1.0 * options.outWidth / options.outHeight);
                }
            }
            options.inJustDecodeBounds = false;
            Log.i(TAG,
                "bitmap required size=" + newWidth + "x" + newHeight + ", orig=" + options.outWidth
                    + "x" + options.outHeight + ", sample=" + options.inSampleSize);
            Bitmap bm = BitmapFactory.decodeFile(path, options);
            if (bm == null) {
                Log.e(TAG, "bitmap decode failed");
                return null;
            }
            Log.i(TAG, "bitmap decoded size=" + bm.getWidth() + "x" + bm.getHeight());
            final Bitmap scale = Bitmap.createScaledBitmap(bm, newWidth, newHeight, true);
            if (scale != null) {
                bm.recycle();
                bm = scale;
            }

            if (crop) {
                final Bitmap cropped = Bitmap
                    .createBitmap(bm, (bm.getWidth() - width) >> 1, (bm.getHeight() - height) >> 1,
                        width, height);
                if (cropped == null) {
                    return bm;
                }
                bm.recycle();
                bm = cropped;
                Log.i(TAG, "bitmap croped size=" + bm.getWidth() + "x" + bm.getHeight());
            }
            return bm;
        } catch (final OutOfMemoryError e) {
            Log.e(TAG, "decode bitmap failed: " + e.getMessage());
            options = null;
        }
        return null;
    }

    public static final void showResultDialog(Context context, String msg, String title) {
        if (msg == null) {
            return;
        }
		/*
		 * String rmsg = msg.replace(",", "\n"); Log.d("Utils", rmsg); new
		 * AlertDialog.Builder(context).setTitle(title).setMessage(rmsg)
		 * .setNegativeButton("知道了", null).create().show();
		 */
    }

    public static final void showProgressDialog(Context context, String title, String message) {
        dismissDialog();
        if (TextUtils.isEmpty(title)) {
            title = "请稍候";
        }
        if (TextUtils.isEmpty(message)) {
            message = "正在加载...";
        }
        mProgressDialog = ProgressDialog.show(context, title, message);
    }

    public static final void dismissDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    // /**
    // * 弹出确认框
    // *
    // * @param poActivity
    // * @param psContent
    // * @param psOkText
    // * @param psCancelText
    // * @param poOnClick
    // * @return
    // */
    // @SuppressLint("InflateParams")
    // public static AlertDialog alertConfirmDialog(Activity poActivity, String
    // psContent, String psLeftBtnText,
    // String psRightBtnText, final OnConfirmBtnClick poOnClick) {
    // final AlertDialog loDialog = new
    // AlertDialog.Builder(poActivity).create();
    // LayoutInflater loInflater = poActivity.getLayoutInflater();
    // loDialog.setView(loInflater.inflate(R.layout.dialog_confirm, null));
    // loDialog.show();
    // loDialog.getWindow().setContentView(R.layout.dialog_confirm);
    // loDialog.setCanceledOnTouchOutside(true);
    // loDialog.setCancelable(true);
    // ((TextView)
    // loDialog.findViewById(R.id.dialog_confirm_tv_content)).setText(psContent);
    // if (psLeftBtnText != null)
    // ((Button)
    // loDialog.findViewById(R.id.dialog_confirm_btn_left)).setText(psLeftBtnText);
    // if (psRightBtnText != null)
    // ((Button)
    // loDialog.findViewById(R.id.dialog_confirm_btn_right)).setText(psRightBtnText);
    // ((Button)
    // loDialog.findViewById(R.id.dialog_confirm_btn_left)).setOnClickListener(new
    // OnClickListener() {
    // @Override
    // public void onClick(View arg0) {
    // loDialog.dismiss();
    // if (poOnClick != null)
    // poOnClick.onClick((Button) arg0);
    // }
    // });
    // ((Button)
    // loDialog.findViewById(R.id.dialog_confirm_btn_right)).setOnClickListener(new
    // OnClickListener() {
    // @Override
    // public void onClick(View arg0) {
    // loDialog.dismiss();
    // if (poOnClick != null)
    // poOnClick.onClick((Button) arg0);
    // }
    // });
    //
    // return loDialog;
    // }

    /**
     * 打印消息并且用Toast显示消息
     * @param logLevel 填d, w, e分别代表debug, warn, error; 默认是debug
     */
    public static final void toastMessage(final Activity activity, final String message,
        String logLevel) {
        if ("w".equals(logLevel)) {
            Log.w("sdkDemo", message);
        } else if ("e".equals(logLevel)) {
            Log.e("sdkDemo", message);
        } else {
            Log.d("sdkDemo", message);
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                mToast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }

    /**
     * 打印消息并且用Toast显示消息
     */
    public static final void toastMessage(final Activity activity, final String message) {
        toastMessage(activity, message, null);
    }

    /**
     * 根据一个网络连接(String)获取bitmap图像
     */
    public static Bitmap getbitmap(String imageUri) {
        Log.v(TAG, "getbitmap:" + imageUri);
        // 显示网络上的图片
        Bitmap bitmap = null;
        try {
            URL myFileUrl = new URL(imageUri);
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
            Log.v(TAG, "image download finished." + imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, "getbitmap bmp fail---");
            return null;
        }
        return bitmap;
    }

    /**
     * 显示版本更新对话框
     */
    @SuppressLint("InflateParams")
    public static void showUpdateWindow(final Activity poActivity, View poVParent,
        final String psVerName, String psContent, final String psType, final String psUrl) {
        final LayoutInflater inflater = (LayoutInflater) poActivity
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View vPopupWindow = inflater.inflate(R.layout.activity_update_news, null, false);

        final PopupWindow pw = new PopupWindow(vPopupWindow, 800, 1000, true);
        ColorDrawable cd = new ColorDrawable(0x000000);
        WindowManager.LayoutParams lp = poActivity.getWindow().getAttributes();
        lp.alpha = 0.4f;
        poActivity.getWindow().setAttributes(lp);
        pw.setBackgroundDrawable(cd);
        pw.setOutsideTouchable(false);
        pw.showAtLocation(poVParent, Gravity.CENTER, 0, 0);
        pw.setFocusable(true);
        pw.update();
        pw.setOnDismissListener(new OnDismissListener() {
            public void onDismiss() {
                WindowManager.LayoutParams lp = poActivity.getWindow().getAttributes();
                lp.alpha = 1f;
                poActivity.getWindow().setAttributes(lp);
            }
        });
        TextView moTvTitle = (TextView) vPopupWindow.findViewById(R.id.update_news_tv_prompt);
        TextView moTvType = (TextView) vPopupWindow.findViewById(R.id.update_tv_type);
        TextView moTvContent = (TextView) vPopupWindow.findViewById(R.id.update_tv_content);
        moTvTitle.setText("发现新版本v" + psVerName);
        moTvType.setText("更新类型：" + (psType.equals("1") ? "必选更新" : "可选更新"));
        moTvContent.setText(psContent);
        LinearLayout loLlRb = (LinearLayout) vPopupWindow.findViewById(R.id.update_ll_radio);
        final RadioButton loRb = (RadioButton) vPopupWindow.findViewById(R.id.update_rb_notice);
        if (psType.equals("1")) {
            loLlRb.setVisibility(View.GONE);
        }
        Button moBtnCancel = (Button) vPopupWindow.findViewById(R.id.update_news_btn_cancel);
        moBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
                if (psType.equals("1")) {
                    System.exit(0);
                } else if (loRb.isChecked()) {
                    Utils.setCfg(poActivity, "notice_ver", psVerName);
                }
            }
        });
        Button moBtnUpdate = (Button) vPopupWindow.findViewById(R.id.update_news_btn_update);
        moBtnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pw.dismiss();
                Intent loIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(psUrl));
                poActivity.startActivity(loIntent);
            }
        });
    }

    /**
     * 去除字符串中的汉字
     */
    public static String deleteChinese(String str) {
        return str.substring(0, str.indexOf("("));
    }

    /**
     * 请求登录或注册
     * @param requestCode REQUEST_CODE_LOGIN
     */
    public static void requestLoginOrRegister(final Activity activity, String message,
        final int requestCode) {
        UiHelper.showConfirmDialog(activity, message, R.string.login, R.string.register,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityJumpUtil
                        .gotoActivityForResult(activity, LoginActivity.class, requestCode, null,
                            null);
                }
            }, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityJumpUtil
                        .gotoActivityForResult(activity, Register1Activity.class, requestCode, null,
                            null);
                }
            });
    }

    /**
     * 获取用户等级图标
     * @param data 用户信息
     * @param isPriorityShowAnchorLevel true 优先获取主播等级；false 获取用户等级
     */
    public static String getLevelImageResourceUri(Map<String, ?> data,
        boolean isPriorityShowAnchorLevel) {
        if (isPriorityShowAnchorLevel) {
            if (!TextUtils.isEmpty((String) data.get("moderatorLevel"))) {
                return getLevelImageResourceUri(Constants.USER_ANCHOR_LEVEL_PIX,
                    (String) data.get("moderatorLevel"));
            } else {
                return getLevelImageResourceUri(Constants.USER_LEVEL_PIX,
                    (String) data.get("level"));
            }
        } else {
            return getLevelImageResourceUri(Constants.USER_LEVEL_PIX, (String) data.get("level"));
        }
    }

    /**
     * 获取等级图片资源地址url
     * @return String
     */
    public static String getLevelImageResourceUri(String type, String id) {
        return AppConfig.getInstance().mLevelConfigInfo.get(type + id);
    }

    /**
     * 通过ID + imagebase 拼接model勋章的url
     */
    public static String getModelUri(String id) {
        String url = AppConfig.getInstance().usermodel_base + id + ".png";
        return url;
    }

    public static SpannableString getImageToSpannableString(String imageUrl) {
        return getImageToSpannableString(null, imageUrl);
    }

    public static SpannableString getImageToSpannableString(TextView textView, String imageUrl) {
        return getImageToSpannableString(textView, imageUrl, 0);
    }

    public static SpannableString getImageToSpannableString(String imageUrl, int size) {
        return getImageToSpannableString(null, imageUrl, size);
    }

    /**
     * 根据图片地址，生成SpannableString
     * @param textView 显示SpannableString的View
     */
    public static SpannableString getImageToSpannableString(final TextView textView,
        String imageUrl, final int height) {
        final LevelListDrawable mDrawable = new LevelListDrawable();
        mDrawable.addLevel(0, 0, null);
        mDrawable.setBounds(0, 0, height, height);
        ImageLoaderUtil.getInstance()
            .loadImage(imageUrl, ImageSize.SIZE_ORIGINAL, ImageSize.SIZE_ORIGINAL,
                new ImageLoadingListener() {
                    @Override
                    public void onLoadStarted(Drawable drawable) {

                    }

                    @Override
                    public void onLoadFailed(Drawable drawable) {

                    }

                    @Override
                    public void onLoadingComplete(Drawable resource) {
                        mDrawable.addLevel(1, 1, resource);
                        int resWidth = Utils
                            .px2px(App.mContext, resource.getIntrinsicWidth());
                        int resHeigth = Utils
                            .px2px(App.mContext, resource.getIntrinsicHeight());
                        if (height == 0) {
                            mDrawable.setBounds(0, 0, resWidth, resHeigth);
                        } else {
                            float scale = resHeigth / (float) height;
                            mDrawable.setBounds(0, 0, (int) (resWidth / scale), height);
                        }
                        mDrawable.setLevel(1);
                        if (textView != null) {
                            CharSequence charSequence = textView.getText();
                            textView.setText(charSequence);
                        }
                    }

                    @Override
                    public void onLoadCleared(Drawable drawable) {

                    }
                });

        SpannableString spannableString = new SpannableString("a");
        VerticalImageSpan loIs = new VerticalImageSpan(mDrawable);
        spannableString.setSpan(loIs, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public static SpannableString getImageToSpannableString(int resouceId) {
        return getImageToSpannableString(resouceId, 0);
    }

    /**
     * 根据图片资源id，生成SpannableString
     */
    public static SpannableString getImageToSpannableString(int resouceId, int height) {
        try {
            Drawable drawable = App.mContext.getResources().getDrawable(resouceId);
            if (height == 0) {
                int width = Utils.px2px(App.mContext, drawable.getIntrinsicWidth());
                int heigth = Utils.px2px(App.mContext, drawable.getIntrinsicHeight());
                drawable.setBounds(0, 0, width, heigth);
            } else {
                float scale = drawable.getIntrinsicHeight() / (float) height;
                drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() / scale), height);
            }
            SpannableString spannableString = new SpannableString("a");
            VerticalImageSpan loIs = new VerticalImageSpan(drawable);
            spannableString.setSpan(loIs, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            return spannableString;
        } catch (Exception e) {
        }
        return new SpannableString("");
    }

    public static String getImageHtml(String path) {
        return "<img src='" + path + "' type='pic'/>";

    }

    /**
     * 判断服务器返回是否为True
     * wheels那边接口参数做了修改
     * 添加此方法用于直接兼容两种服务器发送过来的形式
     * 1) true、false
     * 2) 1、0
     */
    public static boolean getBooleanFlag(Object val) {
        String value = String.valueOf(val);
        return Constants.COMMON_TRUE.equalsIgnoreCase(value) || Constants.COMMON_TRUE_NUM
            .equalsIgnoreCase(value);
    }

    /**
     * 解析Str，不成功给予默认值
     */
    public static int getInteger(String val, int defaultVal) {
        int returnVal = defaultVal;
        try {
            returnVal = Integer.valueOf(val);
        } catch (NumberFormatException e) {
        }
        return returnVal;
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     * @return true 表示开启
     */
    public static final boolean isOPenLocation(final Context context) {
        LocationManager locationManager = (LocationManager) context
            .getSystemService(Context.LOCATION_SERVICE);
        ConnectivityManager conManager = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean gps = false;
        boolean network = false;
        if (locationManager != null) {
            // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
            gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        if (conManager != null && conManager.getActiveNetworkInfo() != null) {
            network = conManager.getActiveNetworkInfo().isAvailable();
        }
        return gps || network;
    }

    public static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dip2px(Context context, float dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int dpToPx(float dipValue) {
        return (int) (App.metrics.density * dipValue + 0.5f);
    }

    /**
     * 标准1080p像素 转换 当前屏幕像素
     */
    public static int px2px(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue * scale / 3);

    }

    /**
     * 程序是否在前台运行
     */
    public static boolean isAppOnForeground() {
        // Returns a list of application processes that are running on the
        // device
        ActivityManager activityManager = (ActivityManager) App.mContext
            .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = App.mContext.getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
            .getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName) && appProcess.importance
                == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }


    public static String to9PngPath(String imageUrl) {
        if (!TextUtils.isEmpty(imageUrl)) {
            return imageUrl.replace(".png", ".9.png");
        } else {
            return "";
        }
    }

    /**
     * 判断是否快速点击
     */
    private static long lastClickTime = 0;

    public static boolean isFastDoubleClick(long... threshold) {
        long current = SystemClock.elapsedRealtime();
        long distance = 400;
        if (threshold.length >= 1) {
            distance = threshold[0];
        }
        if (lastClickTime != 0 && current - lastClickTime < distance) {
            lastClickTime = current;
            return true;
        } else {
            lastClickTime = current;
            return false;
        }
    }

    private static long lastPrivateClick = 0;

    /**
     * 判断私播是否在一段时间内频繁点击了
     */
    public static boolean isPrivateClickFreq(long... threshold) {
        long current = SystemClock.elapsedRealtime();
        long distance = 5000;
        if (threshold.length >= 1) {
            distance = threshold[0];
        }
        if (lastPrivateClick != 0 && current - lastPrivateClick < distance) {
            lastPrivateClick = current;
            return true;
        } else {
            lastPrivateClick = current;
            return false;
        }
    }

    /**
     * 当前版本是否大于 某个版本（主要用于版本兼容判断）
     */
    public static boolean greaterThanNowSDKVersion(int sdkVersion) {
        return Build.VERSION.SDK_INT >= sdkVersion;
    }

    /**
     * 是否显示推荐页
     */
    public static boolean isRecommendShow() {
        //		if("and-vivo-2".equals(ChannelUtil.getChannel(App.mContext))){
        //			return false;
        //		}
        return true;
    }
}
