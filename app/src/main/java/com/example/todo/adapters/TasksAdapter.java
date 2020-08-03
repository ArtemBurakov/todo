package com.example.todo.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.models.Task;
import com.example.todo.ui.home.HomeFragment;

import java.util.ArrayList;
import java.util.Objects;

import static com.google.android.material.internal.ContextUtils.getActivity;

public class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TaskViewHolder> {

    private static final String TAG = "TasksAdapter";

    private ArrayList<Task> tasksArrayList;
    private Context mContext;
    private OnTaskListener mOnTaskListener;

    public TasksAdapter(ArrayList<Task> tasksArrayList, Context context, OnTaskListener onTaskListener) {
        this.tasksArrayList = tasksArrayList;
        this.mContext = context;
        this.mOnTaskListener = onTaskListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view, mOnTaskListener);
    }

    public void onBindViewHolder(@NonNull TaskViewHolder holder, final int position) {
        final Task task = tasksArrayList.get(position);

        holder.taskName.setText(task.getName());
        holder.taskText.setText(task.getText());

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                if (((CompoundButton) view).isChecked()) {
                    Log.e(TAG, "Checked");

                    task.setStatus(20);
                    HomeFragment.updateTask(task);
                    removeItemAtPosition(position);
                    ((CompoundButton) view).setChecked(false);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasksArrayList.size();
    }

    public void removeItemAtPosition(int position) {
        tasksArrayList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tasksArrayList.size());
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CheckBox checkBox;
        TextView taskName, taskText;
        OnTaskListener onTaskListener;

        public TaskViewHolder(@NonNull View itemView, OnTaskListener onTaskListener) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
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
}