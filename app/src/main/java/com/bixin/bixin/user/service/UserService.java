package com.bixin.bixin.user.service;


import com.bixin.bixin.user.bean.HttpUserConstants;
import com.bixin.bixin.user.bean.UserBean;

import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by Admin
 */

public interface UserService {
	@POST(HttpUserConstants.LOGIN_URL)
	Observable<String> login(@Query("username") String username,
							 @Query("password") String password);

	@POST(HttpUserConstants.GET_USER_INFO_URL)
	Observable<UserBean> getUserInfo();

	@POST(HttpUserConstants.GET_USER_INFO_URL)
	Observable<UserBean> getUserInfo(@Query("uid") long uid);
}
