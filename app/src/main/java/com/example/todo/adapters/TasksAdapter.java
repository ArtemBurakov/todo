package com.example.todo.adapters;

import android.content.Context;
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

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {
    private ArrayList<Task> tasksArrayList;
    private final OnTaskListener mOnTaskListener;
    private final TodoDatabaseHelper todoDatabaseHelper;

    public TasksAdapter(ArrayList<Task> tasksArrayList, Context context, OnTaskListener onTaskListener) {
        this.tasksArrayList = tasksArrayList;
        this.mOnTaskListener = onTaskListener;
        this.todoDatabaseHelper = TodoDatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view, mOnTaskListener);
    }

    @Override
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

    public static class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final RadioButton radioButton;
        private final TextView taskName;
        private final OnTaskListener onTaskListener;

        public TaskViewHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radioButton);
            taskName = itemView.findViewById(R.id.taskName);
            this.onTaskListener = onTaskListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onTaskListener.onTaskClick(getAdapterPosition());
        }
    }

    public interface OnTaskListener{
        void onTaskClick(int position);
    }

    @Override
    public int getItemCount() {
        return tasksArrayList.size();
    }

    public void updateTasksArrayList(final ArrayList<Task> newTasksArrayList) {
        // Attach differences to adapter
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                new TaskDiffUtilCallback(newTasksArrayList, tasksArrayList));
        tasksArrayList = newTasksArrayList;
        result.dispatchUpdatesTo(TasksAdapter.this);
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
