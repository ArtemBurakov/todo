package com.example.todo.ui.note;

import static com.example.todo.MainActivity.hideKeyboard;

import android.content.Intent;
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

import java.util.Objects;

public class CreateNoteActivity extends AppCompatActivity  {
    private TodoDatabaseHelper todoDatabaseHelper;

    private View focusView;
    private String name, text;
    private EditText noteNameView, noteTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());
        noteNameView = findViewById(R.id.noteNameEditText);
        noteTextView = findViewById(R.id.noteTextEditText);

        MaterialToolbar createNoteToolbar = findViewById(R.id.createNoteToolbar);
        setSupportActionBar(createNoteToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        createNoteToolbar.setNavigationOnClickListener(view -> {
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideKeyboard(getApplicationContext(), this.getCurrentFocus());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_note_app_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.save) attemptCreateNote();
        return super.onOptionsItemSelected(item);
    }

    private void attemptCreateNote() {
        Note newNote = new Note();

        if (validateInput()) {
            newNote.setName(name);
            newNote.setText(text);
            newNote.setStatus(TodoDatabaseHelper.statusActive);
            newNote.setSync_status(1);
            if (MainActivity.selectedWorkspace != null) {
                newNote.setBoard_id(MainActivity.selectedWorkspace.getId());
            }
            newNote.setCreated_at(0);
            newNote.setUpdated_at(0);
            MainActivity.selectedNote = todoDatabaseHelper.addNote(newNote);
            MainActivity.startSync();

            Intent intent = new Intent(this, NoteActivity.class);
            startActivity(intent);
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
}
