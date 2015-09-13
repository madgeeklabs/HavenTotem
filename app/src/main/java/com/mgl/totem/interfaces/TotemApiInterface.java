package com.mgl.totem.interfaces;

import com.mgl.totem.activities.RegisterActivity;
import com.mgl.totem.models.Registration;
import com.mgl.totem.models.User;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by goofyahead on 9/12/15.
 */
public interface TotemApiInterface {
    @GET("/api/user/{userId}")
    void getUserWithId(@Path("userId") String userId, Callback<Registration> user);

    @POST("/api/registration")
    void postUser(@Body Registration user, Callback<String> response);
}
