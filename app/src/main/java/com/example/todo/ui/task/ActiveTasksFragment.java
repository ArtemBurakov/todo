package com.example.todo.ui.task;

import static com.example.todo.MainActivity.floatingActionButton;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.TasksAdapter;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Task;

import java.util.ArrayList;

public class ActiveTasksFragment extends Fragment implements TasksAdapter.OnTaskListener {

    private Context context;

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
        lbm.registerReceiver(receiver, new IntentFilter("updateRecyclerView"));
        MainActivity.selectedBoard = null;

        return inflater.inflate(R.layout.fragment_active_task, container, false);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("fcmNotification")) {
                    String modelName = intent.getStringExtra("modelName");
                    if (modelName.equals("todo")) {
                        MainActivity.startSync();
                    }
                } else if (action.equals("updateRecyclerView")) {
                    if (intent.getBooleanExtra("updateStatus", true)) {
                        updateRecyclerView();
                    }
                }
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(requireView()).navigate(R.id.navigation_create_task);
                floatingActionButton.hide();
            }
        });
        floatingActionButton.show();

        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = requireView().findViewById(R.id.activeTaskRecyclerView);

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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx,int dy){
                super.onScrolled(recyclerView, dx, dy);
                if (dy >0) {
                    if (floatingActionButton.isShown()) {
                        floatingActionButton.hide();
                    }
                }
                else if (dy <0) {
                    if (!floatingActionButton.isShown()) {
                        floatingActionButton.show();
                    }
                }
            }
        });
    }

    public void updateRecyclerView() {
        // Get new tasks from DB, update adapter
        ArrayList<Task> newTasksArray = tasksDatabaseHelper.getActiveTasks();
        tasksAdapter.updateTasksArrayList(newTasksArray);
    }

    public void onTaskClick(int position) {
        floatingActionButton.hide();
        MainActivity.selectedTask = tasksDatabaseHelper.getActiveTasks().get(position);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_task);
    }
}
