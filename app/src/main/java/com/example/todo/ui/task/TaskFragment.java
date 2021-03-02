package com.example.todo.ui.task;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Task;
import com.google.android.material.textfield.TextInputLayout;

public class TaskFragment extends Fragment {

    private static final String TAG = "TaskFragment";

    private TextInputLayout taskNameView, taskTextView;
    private View focusView;

    private String name;
    private String text;

    private Context context;
    private TasksDatabaseHelper tasksDatabaseHelper;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(MainActivity.selectedTask.getName());
        tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);

        taskNameView = requireView().findViewById(R.id.taskNameEditText);
        taskTextView = requireView().findViewById(R.id.taskTextEditText);

        Button createTaskButton = requireView().findViewById(R.id.createTaskButton);
        createTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreateTask();
            }
        });

        Button updateTaskButton = requireView().findViewById(R.id.updateTaskButton);
        updateTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptUpdateTask();
            }
        });

        Button deleteTaskButton = requireView().findViewById(R.id.deleteTaskButton);
        deleteTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Task task = MainActivity.selectedTask;
            task.setStatus(TasksDatabaseHelper.statusDeleted);
            task.setSync_status(1);
            tasksDatabaseHelper.updateTask(task);

            navigateHome();
            }
        });

        if (MainActivity.selectedTask != null) {
            taskNameView.getEditText().setText(MainActivity.selectedTask.getName());
            taskTextView.getEditText().setText(MainActivity.selectedTask.getText());
            createTaskButton.setVisibility(View.GONE);
            updateTaskButton.setVisibility(View.VISIBLE);
            deleteTaskButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.selectedTask = null;
        hideKeyboard();
    }

    private void attemptCreateTask() {
        Task newTask = new Task();

        if (validateInput()) {
            // Create new task
            newTask.setName(name);
            newTask.setText(text);
            newTask.setStatus(TasksDatabaseHelper.statusActive);
            newTask.setSync_status(1);
            if (MainActivity.selectedBoard != null) {
                newTask.setBoard_id(MainActivity.selectedBoard.getId());
            }
            newTask.setCreated_at(0);
            newTask.setUpdated_at(0);
            tasksDatabaseHelper.addTask(newTask);

            navigateHome();
        } else {
            // Error; don't attempt to create task
            focusView.requestFocus();
        }
    }

    private void attemptUpdateTask() {
        Task task = MainActivity.selectedTask;

        if (validateInput()) {
            // Update task
            task.setName(name);
            task.setText(text);
            task.setSync_status(1);
            tasksDatabaseHelper.updateTask(task);

            navigateHome();
        } else {
            // Error; don't attempt to update task
            focusView.requestFocus();
        }
    }

    private boolean validateInput() {
        // Reset errors
        taskNameView.setError(null);
        taskTextView.setError(null);

        // Store values at the time of the create attempt
        name = taskNameView.getEditText().getText().toString();
        text = taskTextView.getEditText().getText().toString();

        focusView = null;

        // Check for a valid task name
        if (TextUtils.isEmpty(name)) {
            taskNameView.setError(getString(R.string.error_field_required));
            focusView = taskNameView;
            return false;
        }

        // Check for a valid task text
        if (TextUtils.isEmpty(text)) {
            taskTextView.setError(getString(R.string.error_field_required));
            focusView = taskTextView;
            return false;
        }

        return true;
    }

    private void hideKeyboard() {
        // Close keyboard
        if (getActivity().getCurrentFocus() == null) {
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    private void navigateHome() {
        // Navigate to home fragment
        Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
    }
}
