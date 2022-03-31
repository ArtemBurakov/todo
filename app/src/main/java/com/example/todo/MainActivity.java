package com.example.todo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.todo.models.Workspace;
import com.example.todo.models.Note;
import com.example.todo.models.Task;
import com.example.todo.remote.ApiFcmToken;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @SuppressLint("StaticFieldLeak")
    public static Context context;
    @SuppressLint("StaticFieldLeak")
    public static MaterialToolbar
            tasksToolbar, workspacesToolbar, notesToolbar, settingsToolbar;
    public static ExtendedFloatingActionButton floatingActionButton;

    public static Task selectedTask;
    public static Note selectedNote;
    public static Workspace selectedWorkspace;
    @SuppressLint("StaticFieldLeak")
    public static ApiSync apiSync;
    public ApiFcmTokenSendTask syncFcmToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        setContentView(R.layout.activity_main);

        tasksToolbar = findViewById(R.id.tasks_toolbar);
        workspacesToolbar = findViewById(R.id.workspaces_toolbar);
        notesToolbar = findViewById(R.id.notes_toolbar);
        settingsToolbar = findViewById(R.id.settings_toolbar);
        floatingActionButton = findViewById(R.id.extended_fab);

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

    public static void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
