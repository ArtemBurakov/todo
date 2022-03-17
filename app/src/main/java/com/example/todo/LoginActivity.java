package com.example.todo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.remote.ApiService;
import com.example.todo.remote.ApiUser;
import com.example.todo.remote.ApiUtils;

import java.io.IOException;
import java.util.Objects;

import retrofit2.Call;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask authTask = null;

    // UI references.
    private EditText usernameView;
    private EditText passwordView;
    private View progressView;
    private View loginFormView;
    private RelativeLayout footerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        usernameView = (EditText) findViewById(R.id.editTextUsername);

        passwordView = (EditText) findViewById(R.id.editTextPassword);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.username_log_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        loginFormView = findViewById(R.id.mainLayout);
        footerLayout = findViewById(R.id.footerLayout);
        progressView = findViewById(R.id.progressBar);

        TextView signUp = findViewById(R.id.sign_up);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });
    }

    private void signUp() {
        Intent intent_name = new Intent();
        intent_name.setClass(this, SignUpActivity.class);
        startActivity(intent_name);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (authTask != null) {
            return;
        }

        // Reset errors.
        usernameView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid username address.
        if (TextUtils.isEmpty(username)) {
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            usernameView.setError(getString(R.string.error_invalid_username));
            focusView = usernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            authTask = new UserLoginTask(username, password);
            authTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 10;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        footerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        footerLayout.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                footerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;

        ApiService apiService;

        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Attempt authentication against a network service.
            ApiUser response;
            String apiUrl = "https://3d79-178-216-17-166.ngrok.io/v1/";
            apiService = ApiUtils.getAPIService(apiUrl);
            try {
                Call<ApiUser> call = apiService.authUser(mUsername, mPassword);
                response = call.execute().body();

                if (response != null){

                    Log.d("responce_success===== ", response.toString());
                    String access_token = response.getAccessToken();
                    Integer userId = response.getUserId();

                    if (access_token != null){

                        setAuthToken(access_token, getApplicationContext());
                        if (userId != null){
                            setUserId(userId);
                        }
                        return true;
                    }
                    else{
                        Log.e("ERROR: ", "empty access token");
                        return false;
                    }
                }
                else{
                    Log.e("ERROR: ", "incorrect auth response");
                    return false;
                }

            } catch (IOException e) {
                Log.e("ERROR: ", Objects.requireNonNull(e.getMessage()));
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            authTask = null;
            showProgress(false);

            if (success) {
                // Check if new user or not
                String savedUsername = getUsername(getApplicationContext());

                if (savedUsername != null && !savedUsername.equals(mUsername)) {
                    Log.e("LoginActivity", "New user====");

                    // Delete all tasks from db
                    TasksDatabaseHelper.deleteAllTasks(getApplicationContext());
                }

                // Save username
                setUsername(mUsername);

                // Start Main activity
                Intent intent_name = new Intent();
                intent_name.setClass(getApplicationContext(), MainActivity.class);
                startActivity(intent_name);
                finish();
            } else {
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            authTask = null;
            showProgress(false);
        }
    }

    public static String getAuthToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        return sharedPreferences.getString("access_token", null);
    }

    public static void setAuthToken(String access_token, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
        editor.putString("access_token", "Bearer " + access_token);
        editor.apply();
    }

    public static void deleteAuthToken(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
        editor.remove("access_token").apply();
    }


    public void setUserId(Integer userId) {
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();

        editor.putLong("userId", userId);
        editor.apply();
    }

    public void setUsername(String username) {
        SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();

        editor.putString("username", username);
        editor.apply();
    }

    public static String getUsername(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        return sharedPreferences.getString("username", null);
    }

    public static void setFcmToken(String fcm_token, String token, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
        editor.putString(fcm_token, token);
        editor.apply();
    }

    public static String getFcmToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        return sharedPreferences.getString("fcm_token", null);
    }
}
