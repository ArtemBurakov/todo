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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.models.Task;
import com.example.todo.ui.home.HomeFragment;

import java.time.Instant;

public class TaskFragment extends Fragment {

    private EditText taskNameView, taskTextView;
    private View focusView;
    private long time;
    private String name, text;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                HomeFragment.deleteTask(MainActivity.selectedTask);
                navigateHome();
            }
        });

        if (MainActivity.selectedTask != null) {
            taskNameView.setText(MainActivity.selectedTask.getName());
            taskTextView.setText(MainActivity.selectedTask.getText());
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
        if (validateInput()) {
            HomeFragment.addTask(name, text, time);
            navigateHome();
        } else {
            // There was an error; don't attempt to create task
            focusView.requestFocus();
        }
    }

    private void attemptUpdateTask() {
        final Task task = MainActivity.selectedTask;

        if (validateInput()) {
            task.setName(name);
            task.setText(text);
            HomeFragment.updateTask(task);
            navigateHome();
        } else {
            // There was an error; don't attempt to create task
            focusView.requestFocus();
        }
    }

    private boolean validateInput() {
        // Reset errors
        taskNameView.setError(null);
        taskTextView.setError(null);

        // Store values at the time of the create attempt
        time = Instant.now().getEpochSecond();
        name = taskNameView.getText().toString();
        text = taskTextView.getText().toString();

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
        Navigation.findNavController(getView()).navigate(R.id.navigation_home);
    }
}
