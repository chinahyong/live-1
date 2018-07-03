package com.bixin.bixin.user.presenter

import cn.jpush.android.api.JPushInterface
import com.bixin.bixin.App
import com.bixin.bixin.base.presenter.BasePresenter
import com.bixin.bixin.common.Utils
import com.bixin.bixin.common.config.AppConfig
import com.bixin.bixin.common.config.UserInfoConfig
import com.bixin.bixin.library.util.EvtLog
import com.bixin.bixin.user.bean.UserBean
import com.bixin.bixin.user.service.UserService
import com.bixin.bixin.user.view.IUserView
import com.lib.common.network.http.RetrofitServiceManager
import com.lib.common.network.http.subscriber.RxObserver
import com.lonzh.lib.network.HttpSession
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Admin
 */
open class UserPresenter(view: IUserView) : BasePresenter<UserService, IUserView>(view) {
    init {
        mService = RetrofitServiceManager.instance.create(UserService::class.java)
    }

    // 登录
    fun login(username: String?, password: String?) {
        var observable = filterStatus(mService.login(username, Utils.rsaEncrypt(App.getCacheData("public_key") as String, password)))
        observable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(object : RxObserver<String>(mView.getContext()) {
                    override fun onNext(s: String) {
                        AppConfig.getInstance().updateLoginStatus(true)
                        val lsUid = HttpSession.getInstance(App.mContext).getCookie("uid")
                        EvtLog.e("LoginPhoneActivity", "lsUid:" + lsUid!!)
                        UserInfoConfig.getInstance().updateUserId(lsUid)
                        JPushInterface.setAlias(mView.getContext(), 0, lsUid)
                        mView.iLogin(true)
                        super.onNext(s)
                    }
                })
    }

    // 获取用户信息
    fun getUserInfo(uid: Long) {
        var observable: Observable<UserBean>
        if (uid > 0) {
            observable = filterStatus(mService.getUserInfo(uid))
        } else {
            observable = filterStatus(mService.userInfo)
        }
        observable.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(object : RxObserver<UserBean>(mView.getContext()) {
                    override fun onNext(userBean: UserBean) {
                        super.onNext(userBean)
                    }
                })
    }
}