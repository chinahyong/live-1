package com.bixin.bixin.user.view

import com.bixin.bixin.base.view.IBaseView
import com.bixin.bixin.user.bean.UserBean

/**
 * Created by Admin
 */
interface IUserView : IBaseView {
    fun iLogin(succ: Boolean)

    fun iUserInfo(succ: Boolean, userBean: UserBean)
}