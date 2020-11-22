package com.example.todo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.example.todo.models.Task;
import com.example.todo.remote.ApiFcmToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static Context context;

    public static Task selectedTask;
    public ApiFcmTokenSendTask syncFcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        if (LoginActivity.getAuthToken(this) == null){
            Intent intent = new Intent(this, LoginActivity.class);
            this.startActivity(intent);
            finish();
        } else {
            FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String fcmToken = task.getResult().getToken();
                        if (!fcmToken.equals(LoginActivity.getFcmToken(getApplicationContext()))) {
                            startApiFcmTokenSendTask(fcmToken);
                        }
                    }
                });
        }

        if (InitApplicationTheme.isNightModeEnabled(getApplicationContext())) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.DarkTheme);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.LightTheme);
        }

        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setItemIconTintList(null);

        // Passing each menu ID as a set of Ids because each
        // Menu should be considered as top level destinations
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_settings, R.id.navigation_task)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    public static Context getContextOfApplication() {
        return context;
    }

    public void startApiFcmTokenSendTask(String token){
        if (syncFcmToken == null) {
            syncFcmToken = new ApiFcmTokenSendTask(token);
            syncFcmToken.execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class ApiFcmTokenSendTask extends AsyncTask<Void, Void, Boolean> {

        private final String token;

        public ApiFcmTokenSendTask(String token) {
            this.token = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ApiFcmToken.createToken(token);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            syncFcmToken = null;

            if (success) {
                Log.e(TAG, "Success");
                LoginActivity.setFcmToken("fcm_token", this.token, getApplicationContext());
            }
        }

        @Override
        protected void onCancelled() {
            syncFcmToken = null;
        }
    }
}
