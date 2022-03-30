package com.example.todo.diffUtilCallback;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.todo.models.Task;

import java.util.ArrayList;

public class TaskDiffUtilCallback extends DiffUtil.Callback {
    ArrayList<Task> newList;
    ArrayList<Task> oldList;

    public TaskDiffUtilCallback(ArrayList<Task> newList, ArrayList<Task> oldList) {
        this.newList = newList;
        this.oldList = oldList;
    }

    @Override
    public int getOldListSize() {
        return oldList != null ? oldList.size() : 0 ;
    }

    @Override
    public int getNewListSize() {
        return newList != null ? newList.size() : 0 ;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Task oldTask = oldList.get(oldItemPosition);
        Task newTask = newList.get(newItemPosition);

        return oldTask.getName().equals(newTask.getName())
                && oldTask.getStatus().equals(newTask.getStatus());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
