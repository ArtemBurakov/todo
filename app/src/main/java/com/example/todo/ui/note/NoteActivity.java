package com.example.todo.ui.note;

import static com.example.todo.MainActivity.hideKeyboard;
import static com.example.todo.MainActivity.selectedNote;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.TasksInNoteAdapter;
import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.models.Note;
import com.example.todo.models.Task;
import com.example.todo.models.Workspace;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class NoteActivity extends AppCompatActivity {
    private TodoDatabaseHelper todoDatabaseHelper;

    private EditText noteNameView, noteTextView;
    private TextView addNewTaskTextView;

    private TasksInNoteAdapter tasksAdapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());
        recyclerView = findViewById(R.id.tasksInNoteRecyclerView);
        noteNameView = findViewById(R.id.noteNameEditText);
        noteTextView = findViewById(R.id.noteTextEditText);
        addNewTaskTextView = findViewById(R.id.addNewTaskTextView);
        noteNameView.setText(MainActivity.selectedNote.getName());
        if (selectedNote.getType() == 1) {
            checkboxes();
        } else {
            noteTextView.setText(MainActivity.selectedNote.getText());
        }
        MaterialToolbar selectedNoteToolbar = findViewById(R.id.selectedNoteToolbar);
        setSupportActionBar(selectedNoteToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        selectedNoteToolbar.setNavigationOnClickListener(view -> {
            attemptUpdateNote();
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.selectedNote = null;
        hideKeyboard(getApplicationContext(), this.getCurrentFocus());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.selected_note_app_bar, menu);
        MenuItem checkboxMenuItem = menu.findItem(R.id.checkboxes);
        if (selectedNote.getType() == 1) {
            checkboxMenuItem.setTitle("Note");
        } else {
            checkboxMenuItem.setTitle("Checkboxes");
        }
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.checkboxes:{
                if (selectedNote.getType() == 1) {
                    item.setTitle("Checkboxes");
                    taskToNote();
                    note();
                } else {
                    item.setTitle("Note");
                    noteToTask();
                    checkboxes();
                }
                return true;
            }
            case R.id.done:
                doneNote();
                return true;
            case R.id.delete:
                deleteNoteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initRecyclerView() {
        // Construct the data source
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());
        ArrayList<Task> tasksArray = todoDatabaseHelper.getNoteTasks(selectedNote.getId());

        // Setting LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        // Create the adapter to convert the array to views
        tasksAdapter = new TasksInNoteAdapter(tasksArray, getApplicationContext());

        // Attach the adapter to a RecyclerView
        recyclerView.setAdapter(tasksAdapter);
    }

    private void note() {
        noteTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        addNewTaskTextView.setVisibility(View.GONE);
    }

    private void checkboxes() {
        noteTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        addNewTaskTextView.setVisibility(View.VISIBLE);
        addNewTaskTextView.setOnClickListener(view -> attemptCreateTask());
        initRecyclerView();
    }

    private void attemptCreateTask() {
        Task newTask = new Task();
        newTask.setName("");
        newTask.setNote_id(selectedNote.getId());
        newTask.setStatus(TodoDatabaseHelper.statusActive);
        newTask.setSync_status(1);
        newTask.setCreated_at(0);
        newTask.setUpdated_at(0);
        tasksAdapter.addNewTask(todoDatabaseHelper.addTask(newTask));
        MainActivity.startSync();
    }

    private void taskToNote() {
        StringBuilder finalString = new StringBuilder();
        ArrayList<Task> tasksArray = todoDatabaseHelper.getNoteTasks(selectedNote.getId());
        Iterator<Task> iterator = tasksArray.iterator();
        for (Task task : tasksArray) {
            if (!iterator.hasNext())
                finalString.append(task.getName());
            else
                finalString.append(task.getName()).append("\n");
        }

        Note note = MainActivity.selectedNote;
        note.setType(0);
        note.setText(finalString.toString());
        noteTextView.setText(finalString.toString());
        note.setSync_status(1);
        todoDatabaseHelper.updateNote(note);
        MainActivity.startSync();
    }

    private void noteToTask() {
        String noteText = String.valueOf(noteTextView.getText());
        String[] splitNoteText = noteText.split("\n");
        ArrayList<Task> tasksArray = todoDatabaseHelper.getNoteTasks(selectedNote.getId());

        if (tasksArray.isEmpty()) {
            Log.e("Here", "Note does not have tasks.");
            for (String item : splitNoteText) {
                Task newTask = new Task();
                newTask.setName(item);
                newTask.setStatus(TodoDatabaseHelper.statusActive);
                newTask.setNote_id(selectedNote.getId());
                newTask.setSync_status(1);
                newTask.setCreated_at(0);
                newTask.setUpdated_at(0);
                todoDatabaseHelper.addTask(newTask);
            }
        } else {
            Log.e("Here", "Note have tasks.");
            todoDatabaseHelper.deleteNoteTasks(selectedNote.getId());
            for (String item : splitNoteText) {
                Task newTask = new Task();
                newTask.setName(item);
                newTask.setStatus(TodoDatabaseHelper.statusActive);
                newTask.setNote_id(selectedNote.getId());
                newTask.setSync_status(1);
                newTask.setCreated_at(0);
                newTask.setUpdated_at(0);
                todoDatabaseHelper.addTask(newTask);
            }
        }

        Note note = MainActivity.selectedNote;
        note.setText("");
        note.setType(1);
        note.setSync_status(1);
        todoDatabaseHelper.updateNote(note);
        MainActivity.startSync();
    }

    private void attemptUpdateNote() {
        Note note = MainActivity.selectedNote;
        note.setName(noteNameView.getText().toString());
        note.setText(noteTextView.getText().toString());
        note.setSync_status(1);
        todoDatabaseHelper.updateNote(note);
        MainActivity.startSync();
        finish();
    }

    private void deleteNoteDialog() {
        MaterialAlertDialogBuilder removeNoteBuilder = new MaterialAlertDialogBuilder(this);
        removeNoteBuilder.setTitle("Remove note?");
        removeNoteBuilder.setMessage("This note will be permanently deleted, but it can be restored.");
        removeNoteBuilder.setPositiveButton("Remove", (dialogInterface, i) -> {
            deleteNote();
            dialogInterface.dismiss();
        });
        removeNoteBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        removeNoteBuilder.show();
    }

    private void deleteNote() {
        Note note = MainActivity.selectedNote;
        note.setStatus(TodoDatabaseHelper.statusDeleted);
        note.setSync_status(1);
        todoDatabaseHelper.updateNote(note);
        MainActivity.startSync();
        finish();
    }

    private void doneNote() {
        Note note = MainActivity.selectedNote;
        note.setStatus(TodoDatabaseHelper.statusDone);
        note.setSync_status(1);
        todoDatabaseHelper.updateNote(note);
        MainActivity.startSync();
        finish();
    }
}
