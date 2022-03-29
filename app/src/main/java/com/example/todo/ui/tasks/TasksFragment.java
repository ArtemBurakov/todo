package com.example.todo.ui.tasks;

import static com.example.todo.MainActivity.createTaskToolbar;
import static com.example.todo.MainActivity.floatingActionButton;
import static com.example.todo.MainActivity.hideKeyboard;
import static com.example.todo.MainActivity.notesToolbar;
import static com.example.todo.MainActivity.selectedBoardToolbar;
import static com.example.todo.MainActivity.selectedTaskToolbar;
import static com.example.todo.MainActivity.settingsToolbar;
import static com.example.todo.MainActivity.showKeyboard;
import static com.example.todo.MainActivity.tasksToolbar;
import static com.example.todo.MainActivity.workspacesToolbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.TasksAdapter;
import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.models.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

public class TasksFragment extends Fragment implements TasksAdapter.OnTaskListener {
    private static final String TAG = "TasksFragment";

    private Context context;
    private TextInputLayout taskNameView;
    private SwipeRefreshLayout swipeContainer;

    private TasksAdapter tasksAdapter;
    private TodoDatabaseHelper todoDatabaseHelper;
    private RecyclerView recyclerView;

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
        MainActivity.selectedWorkspace = null;

        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        swipeContainer.setRefreshing(false);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("fcmNotification")) {
                    String modelName = intent.getStringExtra("modelName");
                    if (modelName.equals("task")) {
                        Log.d(TAG, "Task FCM push " + intent.getAction());
                        MainActivity.startSync();
                    }
                } else if (action.equals("updateRecyclerView")) {
                    if (intent.getBooleanExtra("updateStatus", true)) {
                        updateRecyclerView();
                        swipeContainer.setRefreshing(false);
                    }
                }
            }
        }
    };

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tasksToolbar.setVisibility(View.VISIBLE);
        workspacesToolbar.setVisibility(View.GONE);
        notesToolbar.setVisibility(View.GONE);
        settingsToolbar.setVisibility(View.GONE);
        selectedBoardToolbar.setVisibility(View.GONE);
        selectedTaskToolbar.setVisibility(View.GONE);
        createTaskToolbar.setVisibility(View.GONE);

        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(MainActivity::startSync);

        floatingActionButton.setText("New task");
        floatingActionButton.setOnClickListener(v -> newTask());

        floatingActionButton.show();
        floatingActionButton.extend();

        MainActivity.startSync();
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView = requireView().findViewById(R.id.tasksRecyclerView);

        // Construct the data source
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(context);
        ArrayList<Task> tasksArray = todoDatabaseHelper.getActiveTasks();

        // Setting LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Create the adapter to convert the array to views
        tasksAdapter = new TasksAdapter(tasksArray, context, this);

        // Attach the adapter to a RecyclerView
        recyclerView.setAdapter(tasksAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(-1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    floatingActionButton.extend();
                } else {
                    floatingActionButton.shrink();
                }
            }
        });
    }

    public void updateRecyclerView() {
        ArrayList<Task> newTasksArray = todoDatabaseHelper.getActiveTasks();
        tasksAdapter.updateTasksArrayList(newTasksArray);
        if (!recyclerView.canScrollVertically(-1)) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        }
    }

    private void newTask() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("New task");
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.task_create_text_input, (ViewGroup) getView(), false);
        taskNameView = viewInflated.findViewById(R.id.taskNameEditText);
        builder.setView(viewInflated);
        builder.setPositiveButton("Create", (dialogInterface, i) -> {
            attemptCreateTask();
            hideKeyboard(context, taskNameView.getEditText());
            dialogInterface.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            hideKeyboard(context, taskNameView.getEditText());
            dialogInterface.cancel();
        });
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        Objects.requireNonNull(taskNameView.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(s));
            }
        });
        taskNameView.requestFocus();
        showKeyboard();
    }

    private void updateTask() {
        Task task = MainActivity.selectedTask;
        task.setName(taskNameView.getEditText().getText().toString());
        task.setSync_status(1);
        todoDatabaseHelper.updateTask(task);

        MainActivity.startSync();
        Navigation.findNavController(requireView()).navigate(R.id.navigation_tasks);
    }

    private void renameTask() {
        MaterialAlertDialogBuilder updateTaskBuilder = new MaterialAlertDialogBuilder(getActivity());
        updateTaskBuilder.setTitle("Rename task");
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.task_create_text_input, (ViewGroup) getView(), false);
        taskNameView = viewInflated.findViewById(R.id.taskNameEditText);
        Objects.requireNonNull(taskNameView.getEditText()).setText(MainActivity.selectedTask.getName());
        taskNameView.getEditText().setSelectAllOnFocus(true);
        updateTaskBuilder.setView(viewInflated);
        updateTaskBuilder.setPositiveButton("Rename", (dialogInterface, i) -> {
            updateTask();
            hideKeyboard(context, taskNameView.getEditText());
            dialogInterface.dismiss();
        });
        updateTaskBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            hideKeyboard(context, taskNameView.getEditText());
            dialogInterface.cancel();
        });

        final AlertDialog updateTaskDialog = updateTaskBuilder.create();
        updateTaskDialog.setCanceledOnTouchOutside(false);
        updateTaskDialog.show();

        updateTaskDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        Objects.requireNonNull(taskNameView.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateTaskDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(s));
            }
        });
        taskNameView.requestFocus();
        showKeyboard();
    }

    private void attemptCreateTask() {
        Task newTask = new Task();
        newTask.setName(taskNameView.getEditText().getText().toString());
        newTask.setStatus(TodoDatabaseHelper.statusActive);
        newTask.setSync_status(1);
        newTask.setCreated_at(0);
        newTask.setUpdated_at(0);
        todoDatabaseHelper.addTask(newTask);

        MainActivity.startSync();
        Navigation.findNavController(requireView()).navigate(R.id.navigation_tasks);
    }

    public void onTaskClick(int position) {
        MainActivity.selectedTask = todoDatabaseHelper.getActiveTasks().get(position);
        renameTask();
    }
}
