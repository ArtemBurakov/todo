package com.example.todo.ui.task;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Board;
import com.example.todo.models.Task;
import com.google.android.material.textfield.TextInputLayout;

public class CreateTaskFragment extends Fragment {

    private TextInputLayout taskNameView, taskTextView;
    private String name, text;
    private View focusView;

    private Context context;
    private TasksDatabaseHelper tasksDatabaseHelper;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        return inflater.inflate(R.layout.fragment_create_task, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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

    private void navigateHome() {
        // Navigate to home fragment
        Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
    }
}
