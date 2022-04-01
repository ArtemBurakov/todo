package com.example.todo.diffUtilCallback;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.todo.models.Workspace;

import java.util.ArrayList;

public class WorkspaceDiffUtilCallback extends DiffUtil.Callback {
    ArrayList<Workspace> newList;
    ArrayList<Workspace> oldList;

    public WorkspaceDiffUtilCallback(ArrayList<Workspace> newList, ArrayList<Workspace> oldList) {
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
        Workspace oldWorkspace = oldList.get(oldItemPosition);
        Workspace newWorkspace = newList.get(newItemPosition);

        return oldWorkspace.getName().equals(newWorkspace.getName());
                //&& oldWorkspace.getServer_id().equals(newWorkspace.getServer_id());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
