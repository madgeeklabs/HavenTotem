package com.mgl.totem.interfaces;

import com.mgl.totem.models.User;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by goofyahead on 9/12/15.
 */
public interface TotemApiInterface {
    @GET("/api/user/{userId}")
    void getUserWithId(@Path("userId") String userId, Callback<User> user);

//    @POST("/payments/payment-methods")
//    void notifyCartTransaction(@Body CartAndNonce cart, Callback<String> response);
}
