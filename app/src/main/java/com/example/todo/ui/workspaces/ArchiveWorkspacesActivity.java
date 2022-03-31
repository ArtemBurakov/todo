package com.example.todo.ui.workspaces;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.WorkspacesAdapter;
import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.models.Workspace;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

public class ArchiveWorkspacesActivity extends AppCompatActivity implements WorkspacesAdapter.OnWorkspaceListener {
    private TodoDatabaseHelper todoDatabaseHelper;

    private WorkspacesAdapter workspacesAdapter;
    private RecyclerView workspaceRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_workspaces);
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());

        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getApplicationContext());
        lbm.registerReceiver(receiver, new IntentFilter("fcmNotification"));
        lbm.registerReceiver(receiver, new IntentFilter("updateRecyclerView"));

        swipeRefreshLayout = findViewById(R.id.swipeContainer);
        swipeRefreshLayout.setOnRefreshListener(MainActivity::startSync);

        MaterialToolbar archiveWorkspacesToolbar = findViewById(R.id.archiveWorkspacesToolbar);
        setSupportActionBar(archiveWorkspacesToolbar);
        archiveWorkspacesToolbar.setNavigationOnClickListener(view -> {
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
        initWorkspaceRecyclerView();
        MainActivity.startSync();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("fcmNotification")) {
                    String modelName = intent.getStringExtra("modelName");
                    if (modelName.equals("board")) MainActivity.startSync();
                }

                if (action.equals("updateRecyclerView"))
                    if (intent.getBooleanExtra("updateStatus", true)) {
                        updateRecyclerView();
                        swipeRefreshLayout.setRefreshing(false);
                    }
            }
        }
    };

    private void initWorkspaceRecyclerView() {
        workspaceRecyclerView = findViewById(R.id.archiveWorkspacesRecyclerView);

        // Construct the data source
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(getApplicationContext());
        ArrayList<Workspace> archiveWorkspacesArray = todoDatabaseHelper.getArchiveBoards();

        // Setting GridLayoutManager
        workspaceRecyclerView.setHasFixedSize(true);
        workspaceRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        // Create the adapter to convert the array to views
        workspacesAdapter = new WorkspacesAdapter(archiveWorkspacesArray, getApplicationContext(), this);

        // Attach the adapter to a RecyclerView
        workspaceRecyclerView.setAdapter(workspacesAdapter);
    }

    public void updateRecyclerView() {
        ArrayList<Workspace> newArchiveWorkspacesArray = todoDatabaseHelper.getArchiveBoards();
        workspacesAdapter.updateBoardsArrayList(newArchiveWorkspacesArray);
        if (!workspaceRecyclerView.canScrollVertically(-1)) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) workspaceRecyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        }
    }

    public void onWorkspaceClick(int position) {
        MainActivity.selectedWorkspace = todoDatabaseHelper.getArchiveBoards().get(position);
        restoreArchiveWorkspaceDialog();
    }

    private void restoreArchiveWorkspaceDialog() {
        MaterialAlertDialogBuilder restoreWorkspaceBuilder = new MaterialAlertDialogBuilder(this);
        restoreWorkspaceBuilder.setTitle("Restore workspace?");
        restoreWorkspaceBuilder.setMessage("This workspace will be restored from archive, all workspace notes will be restore too.");
        restoreWorkspaceBuilder.setPositiveButton("Restore", (dialogInterface, i) -> {
            restoreWorkspace();
            dialogInterface.dismiss();
        });
        restoreWorkspaceBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        restoreWorkspaceBuilder.show();
    }

    private void restoreWorkspace() {
        Workspace restore_workspace = MainActivity.selectedWorkspace;
        restore_workspace.setStatus(TodoDatabaseHelper.statusActive);
        restore_workspace.setSync_status(1);
        todoDatabaseHelper.updateBoard(restore_workspace);

        initWorkspaceRecyclerView();
        MainActivity.startSync();
    }
}
