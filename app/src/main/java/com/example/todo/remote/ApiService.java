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

    @GET("tasks")
    Call<List<ApiTask>> getTasks(@Header("Authorization") String authorization, @Query("updated_after") String updated_after);

    @GET("notes")
    Call<List<ApiNote>> getNotes(@Header("Authorization") String authorization, @Query("updated_after") String updated_after);

    @GET("boards")
    Call<List<ApiWorkspace>> getBoards(@Header("Authorization") String authorization, @Query("updated_after") String updated_after);

    @Headers({
            "Content-Type: application/json"
    })
    @POST("tasks")
    Call<ApiTask> addTask(@Header("Authorization") String authorization, @Body ApiTask task);

    @POST("notes")
    Call<ApiNote> addNote(@Header("Authorization") String authorization, @Body ApiNote note);

    @POST("boards")
    Call<ApiWorkspace> addBoard(@Header("Authorization") String authorization, @Body ApiWorkspace board);

    @Headers({
            "Content-Type: application/json"
    })
    @PUT("tasks/{id}")
    Call<ApiTask> updateTask(@Header("Authorization") String authorization, @Path("id") long id, @Body ApiTask task);

    @PUT("notes/{id}")
    Call<ApiNote> updateNote(@Header("Authorization") String authorization, @Path("id") long id, @Body ApiNote note);

    @PUT("boards/{id}")
    Call<ApiWorkspace> updateBoard(@Header("Authorization") String authorization, @Path("id") long id, @Body ApiWorkspace board);
}
