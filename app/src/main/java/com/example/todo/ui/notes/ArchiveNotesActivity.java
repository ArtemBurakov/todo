package com.example.todo.ui.notes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.NotesAdapter;
import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.models.Note;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class ArchiveNotesActivity extends AppCompatActivity implements NotesAdapter.OnNoteListener {
    private TodoDatabaseHelper todoDatabaseHelper;

    private NotesAdapter notesAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_notes);
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.registerReceiver(receiver, new IntentFilter("fcmNotification"));
        lbm.registerReceiver(receiver, new IntentFilter("updateRecyclerView"));

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(MainActivity::startSync);

        MaterialToolbar archiveNotesToolbar = findViewById(R.id.archiveNotesToolbar);
        setSupportActionBar(archiveNotesToolbar);
        archiveNotesToolbar.setNavigationOnClickListener(view -> {
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecyclerView();
        MainActivity.startSync();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("fcmNotification")) {
                    String modelName = intent.getStringExtra("modelName");
                    if (modelName.equals("note")) MainActivity.startSync();
                }

                if (action.equals("updateRecyclerView"))
                    if (intent.getBooleanExtra("updateStatus", true)) {
                        updateRecyclerView();
                        swipeRefreshLayout.setRefreshing(false);
                    }
            }
        }
    };

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.archiveNotesRecyclerView);

        // Construct the data source
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());
        ArrayList<Note> notesArray = todoDatabaseHelper.getArchiveNotes();

        // Setting LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        // Create the adapter to convert the array to views
        notesAdapter = new NotesAdapter(notesArray, getApplicationContext(), this);

        // Attach the adapter to a RecyclerView
        recyclerView.setAdapter(notesAdapter);
    }

    public void updateRecyclerView() {
        ArrayList<Note> newNotesArray = todoDatabaseHelper.getArchiveNotes();
        notesAdapter.updateNotesArrayList(newNotesArray);
        if (!recyclerView.canScrollVertically(-1)) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        }
    }

    public void onNoteClick(int position) {
        MainActivity.selectedNote = todoDatabaseHelper.getArchiveNotes().get(position);
        restoreArchiveNoteDialog();
    }

    private void restoreArchiveNoteDialog() {
        MaterialAlertDialogBuilder restoreNoteBuilder = new MaterialAlertDialogBuilder(this);
        restoreNoteBuilder.setTitle("Restore note?");
        restoreNoteBuilder.setMessage("This note will be restored from archive.");
        restoreNoteBuilder.setPositiveButton("Restore", (dialogInterface, i) -> {
            restoreNote();
            dialogInterface.dismiss();
        });
        restoreNoteBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        restoreNoteBuilder.show();
    }

    private void restoreNote() {
        Note restore_note = MainActivity.selectedNote;
        restore_note.setStatus(TodoDatabaseHelper.statusActive);
        restore_note.setSync_status(1);
        todoDatabaseHelper.updateNote(restore_note);

        initRecyclerView();
        MainActivity.startSync();
    }
}
