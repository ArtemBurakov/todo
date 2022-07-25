package com.example.todo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;
import com.example.todo.models.Task;

import java.util.ArrayList;

public class TasksInNotesAdapter extends RecyclerView.Adapter<TasksInNotesAdapter.TaskViewHolder> {
    public static ArrayList<Task> tasksArrayList;

    public TasksInNotesAdapter(ArrayList<Task> tasksArrayList) {
        TasksInNotesAdapter.tasksArrayList = tasksArrayList;
    }

    @NonNull
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task_in_notes, parent, false);
        return new TaskViewHolder(view);
    }

    public void onBindViewHolder(@NonNull TaskViewHolder holder, final int position) {
        final Task task = tasksArrayList.get(position);
        holder.taskName.setText(task.getName());
        if (task.getStatus() == 10)
            holder.radioButton.setChecked(false);
        else if (task.getStatus() == 20)
            holder.radioButton.setChecked(true);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final RadioButton radioButton;
        private final TextView taskName;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.radioButton);
            taskName = itemView.findViewById(R.id.taskName);
        }
    }

    public int getItemCount() {
        return tasksArrayList.size();
    }
}
