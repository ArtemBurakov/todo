package com.example.todo.ui.home;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.LoginActivity;
import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.TasksAdapter;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Task;
import com.example.todo.remote.ApiService;
import com.example.todo.remote.ApiTask;
import com.example.todo.remote.ApiUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class HomeFragment extends Fragment implements TasksAdapter.OnTaskListener {

    private static final String TAG = "HomeFragment";

    private Context context;

    private ApiTaskSync syncTask = null;
    private Boolean unauthorized = false;

    private TasksAdapter tasksAdapter;
    private TasksDatabaseHelper tasksDatabaseHelper;

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        this.context = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.registerReceiver(receiver, new IntentFilter("fcmNotification"));

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                Log.e(TAG, "==="+intent.getAction());
                startSync();
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        startSync();
    }

    public void startSync(){
        if (syncTask == null) {
            syncTask = new ApiTaskSync();
            syncTask.execute();
        }
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = requireView().findViewById(R.id.taskRecyclerView);

        // Construct the data source
        tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);
        ArrayList<Task> tasksArray = tasksDatabaseHelper.getActiveTasks();

        // Setting LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Create the adapter to convert the array to views
        tasksAdapter = new TasksAdapter(tasksArray, context, this);

        // Attach the adapter to a RecyclerView
        recyclerView.setAdapter(tasksAdapter);
    }

    @SuppressLint("StaticFieldLeak")
    public class ApiTaskSync extends AsyncTask<Void, Void, Boolean> {

        String apiUrl = "http://10.0.2.2/todo/backend/web/v1/";
        ApiService apiService = ApiUtils.getAPIService(apiUrl);

        @Override
        protected Boolean doInBackground(Void... params) {
            // Sync local not synced tasks
            List<Task> tasksToSync = tasksDatabaseHelper.getNotSyncedTasks();
            for (Task taskToSync: tasksToSync){
                Log.e(TAG, "Sync tasks === ");
                if (!syncTask(taskToSync)){
                    return false;
                }
            }

            // Sync with api tasks
            // Get latest update_at timestamp
            long updated_after = 0;
            List<Task> maxUpdatedAtRow = tasksDatabaseHelper.getTasksUpdateAfter();
            if (maxUpdatedAtRow.size() > 0) {
                updated_after = maxUpdatedAtRow.get(0).getUpdated_at();
            }

            Log.e(TAG, "Updated_after === " + updated_after);

            // Get tasks from API server
            try {
                Call<List<ApiTask>> call = apiService.getTodos(LoginActivity.getAuthToken(context), Long.toString(updated_after));
                Response<List<ApiTask>> response = call.execute();

                if (response.isSuccessful()) {

                    List<ApiTask> api_tasks = response.body();
                    Log.e(TAG, "Response_success === " + api_tasks.toString());

                    // Save items to DB
                    for (ApiTask api_task : api_tasks) {

                        List<Task> taskList = tasksDatabaseHelper.getTasks(api_task.getId());

                        if (taskList.size() > 0) {
                            Task task = taskList.get(0);

                            // Update
                            if (task != null && task.getUpdated_at() != api_task.getUpdated_at()) {
                                Log.e(TAG, "Updating task");

                                task.setServer_id(api_task.getId());
                                task.setSync_status(0);
                                task.setName(api_task.getName());
                                task.setText(api_task.getText());
                                task.setUpdated_at(api_task.getUpdated_at());
                                task.setStatus(api_task.getStatus());
                                tasksDatabaseHelper.updateTask(task);
                            }
                        }
                        // Insert
                        else {
                            Log.e(TAG, "Inserting task");

                            Task newTask = new Task();
                            newTask.setServer_id(api_task.getId());
                            newTask.setSync_status(0);
                            newTask.setName(api_task.getName());
                            newTask.setText(api_task.getText());
                            newTask.setCreated_at(api_task.getCreated_at());
                            newTask.setUpdated_at(api_task.getUpdated_at());
                            newTask.setStatus(api_task.getStatus());
                            tasksDatabaseHelper.addTask(newTask);
                        }
                    }
                    return true;
                }
                // Unauthorized
                else if (response.code() == 401) {
                    unauthorized = true;
                    return false;
                }
                else{
                    Log.e("GET ARTICLES ERROR: ", "response is not successful");
                    return false;
                }

            } catch (IOException e) {
                Log.e("GET ARTICLES ERROR: ", e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            syncTask = null;

            if (success) {
                Log.e(TAG, "Success");
                updateRecyclerView();
            }
            else {
                if (unauthorized){
                    Log.e("SYNC ITEM ERROR: ", "unauthorized");
                    LoginActivity.deleteAuthToken(context);

                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }

        @Override
        protected void onCancelled() {
            syncTask = null;
        }

        private Boolean syncTask(Task task){
            ApiTask apiTask = new ApiTask();
            apiTask.setId(task.getServer_id());
            apiTask.setName(task.getName());
            apiTask.setText(task.getText());
            apiTask.setCreated_at(task.getCreated_at());
            apiTask.setUpdated_at(task.getUpdated_at());
            apiTask.setStatus(task.getStatus());

            Call<ApiTask> call;

            try {
                // Update
                if (task.getServer_id() != null){
                    Log.e(TAG, "Update === ");
                    call = apiService.updateTask(LoginActivity.getAuthToken(context), task.getServer_id(), apiTask);
                }
                // Create
                else {
                    Log.e(TAG, "Create === ");
                    call = apiService.addTask(LoginActivity.getAuthToken(context), apiTask);
                }
                Response<ApiTask> response = call.execute();

                if (response.isSuccessful()) {
                    Log.e(TAG, "Response_success === " + response.body().toString());

                    // Update db task
                    ApiTask api_task = response.body();
                    task.setSync_status(0);
                    task.setName(api_task.getName());
                    task.setText(api_task.getText());
                    task.setCreated_at(api_task.getCreated_at());
                    task.setUpdated_at(api_task.getUpdated_at());
                    task.setStatus(api_task.getStatus());

                    if (task.getServer_id() == null) {
                        task.setServer_id(api_task.getId());
                    }

                    tasksDatabaseHelper.updateTask(task);

                    Log.e(TAG, "Task has been synced successfully");
                    return true;
                }
                // Unauthorized
                else if (response.code() == 401) {
                    unauthorized = false;
                    return false;
                }
                // Handle other responses
                else {
                    Log.e(TAG, "TASK SYNC ERROR");
                    return false;
                }
            } catch (IOException e) {
                Log.e("SYNC TASK ERROR: ", e.getMessage());
                return false;
            }
        }
    }

    public void updateRecyclerView() {
        Log.e(TAG, "Updating recycler view");

        // Get new tasks from DB, update adapter
        ArrayList<Task> newTasksArray = tasksDatabaseHelper.getActiveTasks();
        tasksAdapter.updateTasksArrayList(newTasksArray);
    }

    public void onTaskClick(int position) {
        MainActivity.selectedTask = tasksDatabaseHelper.getActiveTasks().get(position);

        // Navigate to task fragment
        Navigation.findNavController(requireView()).navigate(R.id.navigation_task);
    }
}
