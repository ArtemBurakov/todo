package com.example.todo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Board;

import java.util.ArrayList;

public class BoardsAdapter extends RecyclerView.Adapter<BoardsAdapter.BoardViewHolder> {

    private static final String TAG = "BoardsAdapter";

    private ArrayList<Board> boardsArrayList;
    private OnBoardListener mOnBoardListener;
    private TasksDatabaseHelper tasksDatabaseHelper;

    public BoardsAdapter(ArrayList<Board> boardsArrayList, Context context, OnBoardListener onBoardListener) {
        this.boardsArrayList = boardsArrayList;
        this.mOnBoardListener = onBoardListener;
        this.tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false);
        return new BoardViewHolder(view, mOnBoardListener);
    }

    @Override
    public void onBindViewHolder(@NonNull BoardViewHolder holder, final int position) {
        Log.e(TAG, "OnBindViewHolder=== " + position);

        final Board board = boardsArrayList.get(position);

        holder.boardName.setText(board.getName());
    }

    public static class BoardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView boardName;
        OnBoardListener onBoardListener;

        public BoardViewHolder(@NonNull View itemView, OnBoardListener onBoardListener) {
            super(itemView);

            boardName = itemView.findViewById(R.id.boardName);
            this.onBoardListener = onBoardListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onBoardListener.onBoardClick(getAdapterPosition());
        }
    }

    public interface OnBoardListener{
        void onBoardClick(int position);
    }

    @Override
    public int getItemCount() {
        return boardsArrayList.size();
    }
}