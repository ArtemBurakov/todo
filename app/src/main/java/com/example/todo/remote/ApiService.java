package com.example.todo.remote;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("users/authorize")
    Call<ApiUser> authUser(@Field("username") String username, @Field("password") String password);
}
