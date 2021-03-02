package com.example.todo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;
import com.example.todo.TaskDiffUtilCallback;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Task;

import java.util.ArrayList;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {

    private static final String TAG = "TasksAdapter";

    private ArrayList<Task> tasksArrayList;
    private OnTaskListener mOnTaskListener;
    private TasksDatabaseHelper tasksDatabaseHelper;

    public TasksAdapter(ArrayList<Task> tasksArrayList, Context context, OnTaskListener onTaskListener) {
        this.tasksArrayList = tasksArrayList;
        this.mOnTaskListener = onTaskListener;
        this.tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view, mOnTaskListener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, final int position) {
        Log.e(TAG, "OnBindViewHolder=== " + position);

        final Task task = tasksArrayList.get(position);

        holder.taskName.setText(task.getName());
        holder.taskText.setText(task.getText());
        holder.doneTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove item from list
                tasksArrayList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, tasksArrayList.size());

                // Update task
                task.setStatus(TasksDatabaseHelper.statusDone);
                task.setSync_status(1);
                tasksDatabaseHelper.updateTask(task);
            }
        });
        holder.deleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove item from list
                tasksArrayList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, tasksArrayList.size());

                // Update task
                task.setStatus(TasksDatabaseHelper.statusDeleted);
                task.setSync_status(1);
                tasksDatabaseHelper.updateTask(task);
            }
        });
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Button doneTask, deleteTask;
        TextView taskName, taskText;
        OnTaskListener onTaskListener;

        public TaskViewHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);
            doneTask = itemView.findViewById(R.id.doneTaskButton);
            deleteTask = itemView.findViewById(R.id.deleteTaskButton);
            taskName = itemView.findViewById(R.id.taskName);
            taskText = itemView.findViewById(R.id.taskText);
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
}
