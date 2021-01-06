package com.example.todo;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.example.todo.models.Board;

import java.util.ArrayList;

public class BoardDiffUtilCallback extends DiffUtil.Callback {

    ArrayList<Board> newList;
    ArrayList<Board> oldList;

    public BoardDiffUtilCallback(ArrayList<Board> newList, ArrayList<Board> oldList) {
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
        Board oldBoard = oldList.get(oldItemPosition);
        Board newBoard = newList.get(newItemPosition);

        return oldBoard.getName().equals(newBoard.getName());
                //&& oldBoard.getServer_id().equals(newBoard.getServer_id());
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
