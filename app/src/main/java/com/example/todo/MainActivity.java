package com.example.todo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.todo.models.Board;
import com.example.todo.models.Task;
import com.example.todo.remote.ApiFcmToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static Context context;
    public static MaterialToolbar mainToolbar, createTaskToolbar, selectedBoardToolbar, selectedTaskToolbar;

    public static Task selectedTask;
    public static Board selectedBoard;
    public static ApiSync apiSync;
    public ApiFcmTokenSendTask syncFcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);

        mainToolbar = findViewById(R.id.main_toolbar);
        createTaskToolbar = findViewById(R.id.create_task_toolbar);
        selectedTaskToolbar = findViewById(R.id.selected_task_toolbar);
        selectedBoardToolbar = findViewById(R.id.selected_board_toolbar);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);

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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    public static void startSync() {
        if (apiSync == null) {
            apiSync = new ApiSync(context);
            apiSync.execute();
        }
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
