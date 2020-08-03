package com.example.todo.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.TasksAdapter;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Task;

import java.time.Instant;
import java.util.ArrayList;

public class HomeFragment extends Fragment implements TasksAdapter.OnTaskListener {

    private static final String TAG = "HomeFragment";

    private Context context;
    private static TasksDatabaseHelper tasksDatabaseHelper;
    private static TasksAdapter tasksAdapter;
    private ArrayList<Task> activeTasks;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = requireView().findViewById(R.id.taskRecyclerView);

        // Construct the data source
        tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);
        activeTasks = tasksDatabaseHelper.getActiveTasks();

        // Setting LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Create the adapter to convert the array to views
        tasksAdapter = new TasksAdapter(activeTasks, context, this);

        // Attach the adapter to a RecyclerView
        recyclerView.setAdapter(tasksAdapter);
    }

    public static void addTask(String name, String text, long time) {
        Task task = tasksDatabaseHelper.addTask(name, text, 10, time, time);
        if (task != null) {
            Log.e(TAG, "Task created successful");
        }
    }

    public static void updateTask(Task task) {
        long time = Instant.now().getEpochSecond();

        if (task != null) {
            task.setUpdated_at(time);
            if (tasksDatabaseHelper.updateTask(task)){
                Log.e(TAG, "Task updated successful");
            }
        }
    }

    public static void deleteTask(Task task) {
        if (task != null) {
            if (tasksDatabaseHelper.deleteTask(task)) {
                Log.e(TAG, "Task deleted successful");
            }
        }
    }

    public void onTaskClick(int position) {
        MainActivity.selectedTask = activeTasks.get(position);

        // Navigate to home fragment
        Navigation.findNavController(getView()).navigate(R.id.navigation_task);
    }
}