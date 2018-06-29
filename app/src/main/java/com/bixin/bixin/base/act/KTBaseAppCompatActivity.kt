package com.bixin.bixin.base.act

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.WindowManager
import cn.jpush.android.api.JPushInterface
import com.lib.common.utils.LogUtil
import com.lib.common.utils.SystemBarTintUtil
import com.lib.common.widget.LoadingDialogBuilder
import com.umeng.analytics.MobclickAgent
import kotlinx.android.synthetic.main.a_common_top_bar.*
import tv.live.bx.R
import java.lang.ref.WeakReference

/**
 * @author Amin
 * @date 17/10/2017
 * @description com.qifeng.daguan.activity
 */

abstract class KTBaseAppCompatActivity : AppCompatActivity() {

    // Toast相关
    protected var TAG = "BaseFragmentActivity"
    protected var mActivity: Activity? = null
    protected var mInflater: LayoutInflater? = null
    protected var mHandler: Handler? = MyHandler(this)

    // 是否沉侵式模式
    protected var isSystemBarTint = true

    protected var moProgress: AlertDialog? = null        // loading

    val statusBarColor: Int
        get() = colorPrimary

    val colorPrimary: Int
        get() {
            val typedValue = TypedValue()
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
            return typedValue.data
        }

    /**
     * 标题栏相关变量 end
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isSystemBarTint) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                setTranslucentStatus(this, true)
            }
            val tintUtil = SystemBarTintUtil(this)
            tintUtil.isStatusBarTintEnabled = true
            // 使用颜色资源
            tintUtil.setStatusBarTintColor(statusBarColor)
        }
        if (getLayoutRes() > 0) setContentView(getLayoutRes())
        mInflater = LayoutInflater.from(applicationContext)
        mActivity = this
        TAG = javaClass.simpleName
        LogUtil.d(TAG, "onCreate")
        initData(savedInstanceState)
        initTitle()
        addListener()
    }

    /**
     * 初始化Activity 头部的信息，有些activity未使用标准头部布局a_common_top_bar.xml文件，不调用此方法
     */
    public fun initTitle() {
        initTitleData()
    }

    /**
     * 初始化标题信息
     */
    protected abstract fun initTitleData()

    /**
     * 初始化数据
     */
    protected abstract fun initData(savedInstanceState: Bundle?)

    protected abstract fun getLayoutRes(): Int

    /**
     * 设置监听器
     */
    protected abstract fun addListener()

    protected fun setTopBackIv(resId: Int) {
        this.iv_bar_left.setImageResource(resId)
    }

    override fun onStart() {
        super.onStart()
        LogUtil.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        LogUtil.d(TAG, "onResume")
        MobclickAgent.onPageStart(TAG)
        MobclickAgent.onResume(this)
        JPushInterface.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        LogUtil.d(TAG, "onPause")
        // 保证 onPageEnd 在onPause 之前调用,因为 onPause 中会保存信息
        MobclickAgent.onPageEnd(TAG)
        MobclickAgent.onPause(this)
        JPushInterface.onPause(this)
    }

    override fun onStop() {
        super.onStop()
        LogUtil.d(TAG, "onStop")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        LogUtil.d(TAG, "onNewIntent")
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d(TAG, "onDestroy")
        // 清除handler消息
        mHandler?.removeCallbacksAndMessages(null)
        mHandler = null
        dismissLoadingProgress()
    }

    override fun onBackPressed() {
        LogUtil.d(TAG, "onBackPressed")
        super.onBackPressed()
    }

    fun showLoadingProgress() {
        LoadingDialogBuilder.showDialog(this)
    }

    fun dismissLoadingProgress() {
        LoadingDialogBuilder.dismissDialog()
    }

    /**
     * 静态的Handler对象
     */
    private class MyHandler(activity: KTBaseAppCompatActivity) : Handler() {

        private var mActivity: WeakReference<KTBaseAppCompatActivity>? = null

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val activity = mActivity!!.get()
            activity?.handleMessage(msg)
        }
    }

    protected abstract fun handleMessage(msg: Message)

    companion object {
        protected var REQUEST_CODE_FLUSH_ACTIVITY = 0x100

        /**
         * activity不设置setContentView
         */
        protected val NO_SETTING_CONTENTVIEW = -1

        @TargetApi(19) private fun setTranslucentStatus(activity: Activity, on: Boolean) {
            val win = activity.window
            val winParams = win.attributes
            val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            win.attributes = winParams
        }
    }

}
