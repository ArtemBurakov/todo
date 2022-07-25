package com.example.todo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.diffUtilCallback.TaskDiffUtilCallback;
import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.models.Task;

import java.util.ArrayList;

public class TasksInNoteAdapter extends RecyclerView.Adapter<TasksInNoteAdapter.TaskViewHolder> {
    public static ArrayList<Task> tasksArrayList;
    private static TodoDatabaseHelper todoDatabaseHelper;

    public TasksInNoteAdapter(ArrayList<Task> tasksArrayList, Context context) {
        TasksInNoteAdapter.tasksArrayList = tasksArrayList;
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(context);
    }

    @NonNull
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_in_note, parent, false);
        return new TaskViewHolder(view);
    }

    public void onBindViewHolder(@NonNull TaskViewHolder holder, final int position) {
        final Task task = tasksArrayList.get(position);
        holder.taskName.setText(task.getName());
        if (task.getStatus() == 10)
            holder.radioButton.setChecked(false);
        else if (task.getStatus() == 20)
            holder.radioButton.setChecked(true);

        holder.radioButton.setOnClickListener(v -> {
            if (task.getStatus() == 10) {
                holder.radioButton.setChecked(true);
                holder.radioButton.setSelected(true);
                doneTask(task);
            } else {
                holder.radioButton.setChecked(false);
                holder.radioButton.setSelected(false);
                unDoneTask(task);
            }
        });
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final RadioButton radioButton;
        private final TextView taskName;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radioButton);
            taskName = itemView.findViewById(R.id.taskName);
            taskName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    tasksArrayList.get(getAdapterPosition()).setName(taskName.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    Task updatedTask = tasksArrayList.get(getAdapterPosition());
                    updatedTask.setName(taskName.getText().toString());
                    updatedTask.setSync_status(1);
                    todoDatabaseHelper.updateTask(updatedTask);
                    MainActivity.startSync();
                }
            });
        }
    }

    public int getItemCount() {
        return tasksArrayList.size();
    }

    public void updateTasksArrayList(final ArrayList<Task> newTasksArrayList) {
        // Attach differences to adapter
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                new TaskDiffUtilCallback(newTasksArrayList, tasksArrayList));
        tasksArrayList = newTasksArrayList;
        result.dispatchUpdatesTo(TasksInNoteAdapter.this);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void addNewTask(Task task) {
        tasksArrayList.add(task);
        notifyDataSetChanged();
    }

    private void doneTask(Task task) {
        task.setStatus(TodoDatabaseHelper.statusDone);
        task.setSync_status(1);
        todoDatabaseHelper.updateTask(task);
        MainActivity.startSync();
    }

    private void unDoneTask(Task task) {
        task.setStatus(TodoDatabaseHelper.statusActive);
        task.setSync_status(1);
        todoDatabaseHelper.updateTask(task);
        MainActivity.startSync();
    }
}
