package com.bixin.bixin.user.service;


import android.support.annotation.Nullable;

import com.bixin.bixin.user.bean.HttpUserConstants;
import com.bixin.bixin.user.bean.UserBean;

import io.reactivex.Observable;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Admin
 */

public interface UserService {
	@POST(HttpUserConstants.LOGIN_URL)
	Observable<String> login(@Query("username") String username,
							 @Query("password") String password);

	@POST(HttpUserConstants.GET_USER_INFO_URL)
	Observable<UserBean> getUserInfo(@Nullable @Query("uid") long uid);

	@POST(HttpUserConstants.GET_USER_INFO_URL)
	Observable<UserBean> getUserInfo();
}
