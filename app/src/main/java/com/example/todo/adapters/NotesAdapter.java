package com.example.todo.adapters;

import static com.example.todo.adapters.NotesAdapter.NoteViewHolder.recyclerView;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.diffUtilCallback.NoteDiffUtilCallback;
import com.example.todo.models.Note;
import com.example.todo.models.Task;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private ArrayList<Note> notesArrayList;
    private final OnNoteListener mOnNoteListener;
    private TasksInNotesAdapter tasksAdapter;

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
        if (note.getType() == 1) {
            holder.noteText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            initRecyclerView(note);
            recyclerView.setAdapter(tasksAdapter);
        } else {
            holder.noteText.setText(note.getText());
        }
        holder.noteDate.setText(DateFormat.format("MMM d", note.getCreated_at() * 1000).toString());
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView noteName, noteText, noteDate;
        private final OnNoteListener onNoteListener;
        public static RecyclerView recyclerView;

        public NoteViewHolder(@NonNull View itemView, OnNoteListener onNoteListener) {
            super(itemView);
            noteName = itemView.findViewById(R.id.noteName);
            noteText = itemView.findViewById(R.id.noteText);
            recyclerView = itemView.findViewById(R.id.tasksInNoteRecyclerView);
            noteDate = itemView.findViewById(R.id.noteDate);
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

    private void initRecyclerView(Note note) {
        // Construct the data source
        TodoDatabaseHelper todoDatabaseHelper = TodoDatabaseHelper.getInstance(MainActivity.context);
        ArrayList<Task> tasksArray = todoDatabaseHelper.getNoteTasksForView(note.getId());
        // Setting LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.context));
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return true;
            }
        });
        // Create the adapter to convert the array to views
        tasksAdapter = new TasksInNotesAdapter(tasksArray);
    }
}
