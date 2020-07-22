package com.example.todo.remote;

public class ApiUtils {

    private ApiUtils() {
    }

    public static ApiService getAPIService(String apiUrl) {

        return RetrofitClient.getClient(apiUrl).create(ApiService.class);
    }
}
