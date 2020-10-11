package com.example.todo.remote;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @FormUrlEncoded
    @POST("users/authorize")
    Call<ApiUser> authUser(@Field("username") String username, @Field("password") String password);

    @FormUrlEncoded
    @POST("users/sign-up")
    Call<ResponseBody> signUpUser(@Field("username") String username, @Field("email") String userEmail, @Field("password") String password);

    @FormUrlEncoded
    @POST("user-fcm-tokens")
    Call<ApiFcmToken> addToken(@Header("Authorization") String authorization, @Field("registration_token") String fcmToken);

    @GET("todos")
    Call<List<ApiTask>> getTodos(@Header("Authorization") String authorization, @Query("updated_after") String updated_after);

    @Headers({
            "Content-Type: application/json"
    })
    @POST("todos")
    Call<ApiTask> addTask(@Header("Authorization") String authorization, @Body ApiTask task);

    @Headers({
            "Content-Type: application/json"
    })
    @PUT("todos/{id}")
    Call<ApiTask> updateTask(@Header("Authorization") String authorization, @Path("id") long id, @Body ApiTask task);
}