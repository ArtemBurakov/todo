package com.example.todo.ui.note;

import static com.example.todo.MainActivity.hideKeyboard;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.models.Note;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

public class NoteActivity extends AppCompatActivity {
    private TodoDatabaseHelper todoDatabaseHelper;

    private View focusView;
    private String name, text;
    private EditText noteNameView, noteTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());
        noteNameView = findViewById(R.id.noteNameEditText);
        noteNameView.setText(MainActivity.selectedNote.getName());
        noteTextView = findViewById(R.id.noteTextEditText);
        noteTextView.setText(MainActivity.selectedNote.getText());

        MaterialToolbar selectedNoteToolbar = findViewById(R.id.selectedNoteToolbar);
        setSupportActionBar(selectedNoteToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        selectedNoteToolbar.setNavigationOnClickListener(view -> {
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
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update:
                attemptUpdateNote();
                return true;
            case R.id.done:
                doneNote();
                return true;
            case R.id.delete:
                deleteNoteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptUpdateNote() {
        Note note = MainActivity.selectedNote;

        if (validateInput()) {
            note.setName(name);
            note.setText(text);
            note.setSync_status(1);
            todoDatabaseHelper.updateNote(note);
            MainActivity.startSync();
            finish();
        } else {
            focusView.requestFocus();
        }
    }

    private boolean validateInput() {
        // Reset errors
        noteNameView.setError(null);
        noteTextView.setError(null);

        // Store values at the time of the create attempt
        name = noteNameView.getText().toString();
        text = noteTextView.getText().toString();

        focusView = null;

        // Check for a valid note name
        if (TextUtils.isEmpty(name)) {
            noteNameView.setError(getString(R.string.error_field_required));
            focusView = noteNameView;
            return false;
        }

        // Check for a valid note text
        if (TextUtils.isEmpty(text)) {
            noteTextView.setError(getString(R.string.error_field_required));
            focusView = noteTextView;
            return false;
        }

        return true;
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
