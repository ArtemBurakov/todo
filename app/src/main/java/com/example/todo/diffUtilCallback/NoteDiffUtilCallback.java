package com.example.todo.diffUtilCallback;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.todo.models.Note;

import java.util.ArrayList;

public class NoteDiffUtilCallback extends DiffUtil.Callback {
    ArrayList<Note> newList;
    ArrayList<Note> oldList;

    public NoteDiffUtilCallback(ArrayList<Note> newList, ArrayList<Note> oldList) {
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
        Note oldNote = oldList.get(oldItemPosition);
        Note newNote = newList.get(newItemPosition);

        return oldNote.getName().equals(newNote.getName())
                && oldNote.getText().equals(newNote.getText());
                //&& oldTask.getServer_id().equals(newTask.getServer_id());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
