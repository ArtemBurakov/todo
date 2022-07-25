package com.example.todo.ui.workspace;

import static com.example.todo.MainActivity.hideKeyboard;
import static com.example.todo.MainActivity.selectedWorkspace;
import static com.example.todo.MainActivity.showKeyboard;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.example.todo.models.Workspace;
import com.example.todo.ui.note.NoteActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

public class WorkspaceActivity extends AppCompatActivity implements NotesAdapter.OnNoteListener {
    private TodoDatabaseHelper todoDatabaseHelper;

    private NotesAdapter notesAdapter;
    private RecyclerView recyclerView;
    private TextInputLayout workspaceNameView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ExtendedFloatingActionButton extendedFloatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workspace);
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.registerReceiver(receiver, new IntentFilter("fcmNotification"));
        lbm.registerReceiver(receiver, new IntentFilter("updateRecyclerView"));

        MaterialToolbar selectedWorkspaceToolbar = findViewById(R.id.selectedWorkspaceToolbar);
        selectedWorkspaceToolbar.setTitle(MainActivity.selectedWorkspace.getName());
        setSupportActionBar(selectedWorkspaceToolbar);
        selectedWorkspaceToolbar.setNavigationOnClickListener(view -> {
            finish();
        });

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(MainActivity::startSync);

        extendedFloatingActionButton = findViewById(R.id.workspace_extended_fab);
        extendedFloatingActionButton.setText("New note");
        extendedFloatingActionButton.setOnClickListener(view -> {
            onNoteCreate();
            Intent intent = new Intent(this, NoteActivity.class);
            startActivity(intent);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.selected_workspace_app_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rename:
                renameWorkspaceDialog();
                return true;
            case R.id.delete:
                deleteWorkspaceDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        recyclerView = findViewById(R.id.workspaceNotesRecyclerView);

        // Construct the data source
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());
        ArrayList<Note> notesArray = todoDatabaseHelper.getWorkspaceNotes(MainActivity.selectedWorkspace.getId());

        // Setting LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        // Create the adapter to convert the array to views
        notesAdapter = new NotesAdapter(notesArray, getApplicationContext(), this);

        // Attach the adapter to a RecyclerView
        recyclerView.setAdapter(notesAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(-1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    extendedFloatingActionButton.extend();
                } else {
                    extendedFloatingActionButton.shrink();
                }
            }
        });
    }

    public void updateRecyclerView() {
        ArrayList<Note> newNotesArray = todoDatabaseHelper.getWorkspaceNotes(MainActivity.selectedWorkspace.getId());
        notesAdapter.updateNotesArrayList(newNotesArray);
        if (!recyclerView.canScrollVertically(-1)) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        }
    }

    private void renameWorkspaceDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.workspace_create_text_input, null);

        MaterialAlertDialogBuilder updateWorkspaceBuilder = new MaterialAlertDialogBuilder(this);
        updateWorkspaceBuilder.setTitle("Rename workspace");
        updateWorkspaceBuilder.setView(dialogView);
        workspaceNameView = dialogView.findViewById(R.id.workspaceNameEditText);
        Objects.requireNonNull(workspaceNameView.getEditText()).setText(selectedWorkspace.getName());
        workspaceNameView.getEditText().setSelectAllOnFocus(true);
        updateWorkspaceBuilder.setPositiveButton("Rename", (dialogInterface, i) -> {
            renameWorkspace();
            dialogInterface.dismiss();
        });
        updateWorkspaceBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            hideKeyboard(getApplicationContext(), workspaceNameView.getEditText());
            dialogInterface.cancel();
        });

        final AlertDialog updateWorkspaceDialog = updateWorkspaceBuilder.create();
        updateWorkspaceDialog.setCanceledOnTouchOutside(false);
        updateWorkspaceDialog.show();

        updateWorkspaceDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        workspaceNameView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateWorkspaceDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(s));
            }
        });
        workspaceNameView.requestFocus();
        showKeyboard();
    }

    private void renameWorkspace() {
        Workspace workspace = MainActivity.selectedWorkspace;
        workspace.setName(Objects.requireNonNull(workspaceNameView.getEditText()).getText().toString());
        workspace.setSync_status(1);
        todoDatabaseHelper.updateBoard(workspace);

        hideKeyboard(getApplicationContext(), workspaceNameView.getEditText());
        MainActivity.startSync();
        Intent intent = new Intent(this, WorkspaceActivity.class);
        startActivity(intent);
        finish();
    }

    private void deleteWorkspaceDialog() {
        MaterialAlertDialogBuilder removeWorkspaceBuilder = new MaterialAlertDialogBuilder(this);
        removeWorkspaceBuilder.setTitle("Remove workspace?");
        removeWorkspaceBuilder.setMessage("This workspace will be temporarily deleted, but it can be restored with all related tasks.");
        removeWorkspaceBuilder.setPositiveButton("Remove", (dialogInterface, i) -> {
            deleteWorkspace();
            dialogInterface.dismiss();
        });
        removeWorkspaceBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        removeWorkspaceBuilder.show();
    }

    private void deleteWorkspace() {
        Workspace deleted_workspace = MainActivity.selectedWorkspace;
        deleted_workspace.setStatus(TodoDatabaseHelper.statusDeleted);
        deleted_workspace.setSync_status(1);
        todoDatabaseHelper.updateBoard(deleted_workspace);

        MainActivity.startSync();
        finish();
    }

    private void onNoteCreate() {
        Note newNote = new Note();
        newNote.setName("");
        newNote.setText("");
        newNote.setType(TodoDatabaseHelper.typeNote);
        newNote.setStatus(TodoDatabaseHelper.statusActive);
        newNote.setSync_status(1);
        if (MainActivity.selectedWorkspace != null) {
            newNote.setBoard_id(MainActivity.selectedWorkspace.getId());
        }
        newNote.setCreated_at(0);
        newNote.setUpdated_at(0);
        MainActivity.selectedNote = todoDatabaseHelper.addNote(newNote);
        MainActivity.startSync();
    }

    public void onNoteClick(int position) {
        MainActivity.selectedNote = todoDatabaseHelper.getWorkspaceNotes(MainActivity.selectedWorkspace.getId()).get(position);
        Intent intent = new Intent(this, NoteActivity.class);
        startActivity(intent);
    }
}
