package com.example.todo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

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
//        holder.doneTask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                // Remove item from list
//                tasksArrayList.remove(position);
//                notifyItemRemoved(position);
//                notifyItemRangeChanged(position, tasksArrayList.size());
//
//                // Update task
//                task.setStatus(TodoDatabaseHelper.statusDone);
//                task.setSync_status(1);
//                todoDatabaseHelper.updateTask(task);
//            }
//        });
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView taskName;
        OnTaskListener onTaskListener;

        public TaskViewHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);
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
}
