package com.example.todo.ui.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.ArchiveTasksAdapter;
import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.models.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class ArchiveTasksActivity extends AppCompatActivity implements ArchiveTasksAdapter.OnTaskListener {
    private TodoDatabaseHelper todoDatabaseHelper;

    private ArchiveTasksAdapter tasksAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_tasks);
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.registerReceiver(receiver, new IntentFilter("fcmNotification"));
        lbm.registerReceiver(receiver, new IntentFilter("updateRecyclerView"));

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(MainActivity::startSync);

        MaterialToolbar archiveTasksToolbar = findViewById(R.id.archiveTasksToolbar);
        setSupportActionBar(archiveTasksToolbar);
        archiveTasksToolbar.setNavigationOnClickListener(view -> {
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecyclerView();
        MainActivity.startSync();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("fcmNotification")) {
                    String modelName = intent.getStringExtra("modelName");
                    if (modelName.equals("task")) MainActivity.startSync();
                }

                if (action.equals("updateRecyclerView"))
                    if (intent.getBooleanExtra("updateStatus", true)) {
                        updateRecyclerView();
                        swipeRefreshLayout.setRefreshing(false);
                    }
            }
        }
    };

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.archiveTasksRecyclerView);

        // Construct the data source
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());
        ArrayList<Task> tasksArray = todoDatabaseHelper.getArchiveTasks();

        // Setting LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        // Create the adapter to convert the array to views
        tasksAdapter = new ArchiveTasksAdapter(tasksArray, this);

        // Attach the adapter to a RecyclerView
        recyclerView.setAdapter(tasksAdapter);
    }

    public void updateRecyclerView() {
        ArrayList<Task> newTasksArray = todoDatabaseHelper.getArchiveTasks();
        tasksAdapter.updateTasksArrayList(newTasksArray);
        if (!recyclerView.canScrollVertically(-1)) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        }
    }

    public void onTaskClick(int position) {
        MainActivity.selectedTask = todoDatabaseHelper.getArchiveTasks().get(position);
        restoreArchiveTaskDialog();
    }

    private void restoreArchiveTaskDialog() {
        MaterialAlertDialogBuilder restoreTaskBuilder = new MaterialAlertDialogBuilder(this);
        restoreTaskBuilder.setTitle("Restore task?");
        restoreTaskBuilder.setMessage("This task will be restored from archive.");
        restoreTaskBuilder.setPositiveButton("Restore", (dialogInterface, i) -> {
            restoreTask();
            dialogInterface.dismiss();
        });
        restoreTaskBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        restoreTaskBuilder.show();
    }

    private void restoreTask() {
        Task restore_task = MainActivity.selectedTask;
        restore_task.setStatus(TodoDatabaseHelper.statusActive);
        restore_task.setSync_status(1);
        todoDatabaseHelper.updateTask(restore_task);

        initRecyclerView();
        MainActivity.startSync();
    }
}
