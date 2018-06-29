package com.bixin.bixin.user.presenter

import com.bixin.bixin.App
import com.bixin.bixin.base.presenter.BasePresenter
import com.bixin.bixin.common.Utils
import com.bixin.bixin.user.bean.UserBean
import com.bixin.bixin.user.service.UserService
import com.lib.common.network.http.RetrofitServiceManager
import rx.Observable

/**
 * Created by Admin
 */
open class UserPresenter : BasePresenter<UserService>(), UserService {
    init {
        mService = RetrofitServiceManager.getInstance().create(UserService::class.java)
    }

    override fun login(username: String?, password: String?): Observable<String> {
        return filterStatus(mService.login(username, Utils.rsaEncrypt(App.getCacheData("public_key") as String, password)))
    }

    override fun getUserInfo(): Observable<UserBean> {
        return filterStatus(mService.getUserInfo())
    }

    override fun getUserInfo(uid: Long): Observable<UserBean> {
        return filterStatus(mService.getUserInfo(uid))
    }


}