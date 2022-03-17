package com.example.todo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.todo.remote.ApiService;
import com.example.todo.remote.ApiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserSignUpTask registerTask = null;

    // UI references.
    private EditText usernameView;
    private EditText userEmailView;
    private EditText passwordView;
    private View progressView;
    private View signUpFormView;
    private RelativeLayout footerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameView = (EditText) findViewById(R.id.editTextUsername);
        userEmailView = (EditText) findViewById(R.id.editTextEmail);

        passwordView = (EditText) findViewById(R.id.editTextPassword);
        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptSignup();
                    return true;
                }
                return false;
            }
        });

        Button mSignUpButton = (Button) findViewById(R.id.username_sign_up_button);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });

        TextView mLogInButton = (TextView) findViewById(R.id.log_in_text_view);
        mLogInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent_name = new Intent();
                intent_name.setClass(getApplicationContext(), LoginActivity.class);
                startActivity(intent_name);
                finish();
            }
        });

        signUpFormView = findViewById(R.id.mainLayout);
        footerLayout = findViewById(R.id.footerLayout);
        progressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptSignup() {
        if (registerTask != null) {
            return;
        }

        // Reset errors.
        usernameView.setError(null);
        userEmailView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String username = usernameView.getText().toString();
        String email = userEmailView.getText().toString();
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

        // Check for a valid email.
        if (TextUtils.isEmpty(email)) {
            userEmailView.setError(getString(R.string.error_field_required));
            focusView = userEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            userEmailView.setError(getString(R.string.error_invalid_email));
            focusView = userEmailView;
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
            registerTask = new UserSignUpTask(username, email, password);
            registerTask.execute((Void) null);
        }
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() > 10;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        Log.e("SignUpActivity", "Verifying email");
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
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

        signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        signUpFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                signUpFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        footerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        footerLayout.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                footerLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });;

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
     * Represents an asynchronous sign up task used to register
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    public class UserSignUpTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mEmail;
        private final String mPassword;

        ApiService apiService;

        UserSignUpTask(String username, String email, String password) {
            mUsername = username;
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            // Attempt registration against a network service.
            String apiUrl = "https://3d79-178-216-17-166.ngrok.io/v1/";
            apiService = ApiUtils.getAPIService(apiUrl);
            try {
                Call<ResponseBody> call = apiService.signUpUser(mUsername, mEmail, mPassword);
                Response response = call.execute();

                if (response.isSuccessful()){

                    Log.d("responce_success===== ", response.toString());
                    return true;
                }
                else {
                    if (response.code() == 422) {
                        try {
                            String errorBody = response.errorBody().string();
                            JSONArray jsonArray = new JSONArray(errorBody);

                            for (int i = 0; i < jsonArray.length(); i++) {

                                JSONObject object = jsonArray.getJSONObject(i);

                                if (object.get("field").equals("username")) {
                                    usernameView.setError(object.get("message").toString());
                                } else if (object.get("field").equals("email")) {
                                    userEmailView.setError(object.get("message").toString());
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                    else {
                        Log.e("ERROR: ", "incorrect auth response");
                        return false;
                    }
                    return false;
                }
            } catch (IOException e) {
                Log.e("ERROR: ", Objects.requireNonNull(e.getMessage()));
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            registerTask = null;
            showProgress(false);

            if (success) {
                // Start Main activity
                Intent intent_name = new Intent();
                intent_name.setClass(getApplicationContext(), LoginActivity.class);
                startActivity(intent_name);
                finish();
            } else {
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
                userEmailView.requestFocus();
                usernameView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            registerTask = null;
            showProgress(false);
        }
    }
}
