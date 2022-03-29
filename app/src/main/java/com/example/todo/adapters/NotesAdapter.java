package com.example.todo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;
import com.example.todo.diffUtilCallback.NoteDiffUtilCallback;
import com.example.todo.models.Note;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private ArrayList<Note> notesArrayList;
    private final OnNoteListener mOnNoteListener;

    public NotesAdapter(ArrayList<Note> notesArrayList, Context context, OnNoteListener onNoteListener) {
        this.notesArrayList = notesArrayList;
        this.mOnNoteListener = onNoteListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view, mOnNoteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, final int position) {
        final Note note = notesArrayList.get(position);
        holder.noteName.setText(note.getName());
        holder.noteText.setText(note.getText());
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView noteName, noteText;
        OnNoteListener onNoteListener;

        public NoteViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            noteName = itemView.findViewById(R.id.noteName);
            noteText = itemView.findViewById(R.id.noteText);
            this.onNoteListener = onNoteListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onNoteListener.onNoteClick(getAdapterPosition());
        }
    }

    public interface OnNoteListener{
        void onNoteClick(int position);
    }

    @Override
    public int getItemCount() {
        return notesArrayList.size();
    }

    public void updateNotesArrayList(final ArrayList<Note> newNotesArrayList) {
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                new NoteDiffUtilCallback(newNotesArrayList, notesArrayList));
        notesArrayList = newNotesArrayList;
        result.dispatchUpdatesTo(NotesAdapter.this);
    }
}
