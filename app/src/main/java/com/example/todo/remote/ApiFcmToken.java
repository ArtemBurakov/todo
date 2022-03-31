package com.example.todo.remote;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.example.todo.LoginActivity;
import com.example.todo.MainActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiFcmToken {

    private Long id;
    private String registration_token;
    private Integer created_at;
    private Integer updated_at;

    private static ApiService apiService;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistration_token() {
        return registration_token;
    }

    public void setRegistration_token(String registration_token) {
        this.registration_token = registration_token;
    }

    public int getCreated_at() {
        return created_at;
    }

    public int getUpdated_at() {
        return updated_at;
    }

    public static void createToken(String fcmToken){

        final Context syncContext = MainActivity.getContextOfApplication();

        String apiUrl = "https://0aca-91-211-138-182.ngrok.io/v1/";

        String auth_token_string = LoginActivity.getAuthToken(syncContext);

        if (fcmToken != null && auth_token_string != null) {

            apiService = ApiUtils.getAPIService(apiUrl);

            Call<ApiFcmToken> call = apiService.addToken(auth_token_string, fcmToken);
            call.enqueue(new Callback<ApiFcmToken>() {

                @Override
                public void onResponse(Call<ApiFcmToken> call, Response<ApiFcmToken> response) {

                    if (response.isSuccessful()){

                        Log.d("responce_success=====", response.body().toString());
                        Toast.makeText(syncContext, "Token has been sent successfully", Toast.LENGTH_SHORT).show();
                    }
                    // Unauthorized
                    else if (response.code() == 401) {

                        Log.e("ERROR: ", "unauthorized");

                        LoginActivity.deleteAuthToken(syncContext);

                        Intent intent = new Intent(syncContext, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        syncContext.startActivity(intent);
                    }
                    // Handle other responses
                    else {
                        Toast.makeText(syncContext, "Token send error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiFcmToken> call, Throwable t) {

                    Log.e("ERROR: ", t.getMessage());
                    Toast.makeText(syncContext, "Token send error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
