package com.example.todo.ui.workspaces;

import static com.example.todo.MainActivity.createTaskToolbar;
import static com.example.todo.MainActivity.floatingActionButton;
import static com.example.todo.MainActivity.hideKeyboard;
import static com.example.todo.MainActivity.notesToolbar;
import static com.example.todo.MainActivity.selectedBoardToolbar;
import static com.example.todo.MainActivity.selectedTaskToolbar;
import static com.example.todo.MainActivity.settingsToolbar;
import static com.example.todo.MainActivity.showKeyboard;
import static com.example.todo.MainActivity.tasksToolbar;
import static com.example.todo.MainActivity.workspacesToolbar;

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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.WorkspacesAdapter;
import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.models.Workspace;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

public class WorkspacesFragment extends Fragment implements WorkspacesAdapter.OnWorkspaceListener {
    private static final String TAG = "WorkspacesFragment";

    private Context context;
    private TextInputLayout workspaceNameView;
    private SwipeRefreshLayout swipeContainer;

    private WorkspacesAdapter workspacesAdapter;
    private TodoDatabaseHelper todoDatabaseHelper;
    private RecyclerView boardRecyclerView;

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        this.context = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.registerReceiver(receiver, new IntentFilter("fcmNotification"));
        lbm.registerReceiver(receiver, new IntentFilter("updateRecyclerView"));

        return inflater.inflate(R.layout.fragment_workspaces, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tasksToolbar.setVisibility(View.GONE);
        workspacesToolbar.setVisibility(View.VISIBLE);
        notesToolbar.setVisibility(View.GONE);
        settingsToolbar.setVisibility(View.GONE);
        selectedBoardToolbar.setVisibility(View.GONE);
        selectedTaskToolbar.setVisibility(View.GONE);
        createTaskToolbar.setVisibility(View.GONE);

        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(MainActivity::startSync);

        floatingActionButton.setText("New workspace");
        floatingActionButton.setOnClickListener(v -> newWorkspace());

        floatingActionButton.show();
        floatingActionButton.extend();

        MainActivity.startSync();
        initBoardRecyclerView();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        swipeContainer.setRefreshing(false);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("fcmNotification")) {
                    String modelName = intent.getStringExtra("modelName");
                    if (modelName.equals("board")) {
                        Log.d(TAG, "Board FCM push " + intent.getAction());
                        MainActivity.startSync();
                    }
                } else if (action.equals("updateRecyclerView")) {
                    if (intent.getBooleanExtra("updateStatus", true)) {
                        updateRecyclerView();
                        swipeContainer.setRefreshing(false);
                    }
                }
            }
        }
    };

    private void initBoardRecyclerView() {
        boardRecyclerView = requireView().findViewById(R.id.boardRecyclerView);

        // Construct the data source
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(context);
        ArrayList<Workspace> boardsArray = todoDatabaseHelper.getActiveBoards();

        // Setting GridLayoutManager
        boardRecyclerView.setHasFixedSize(true);
        boardRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Create the adapter to convert the array to views
        workspacesAdapter = new WorkspacesAdapter(boardsArray, context, this);

        // Attach the adapter to a RecyclerView
        boardRecyclerView.setAdapter(workspacesAdapter);

        boardRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(-1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    floatingActionButton.extend();
                } else {
                    floatingActionButton.shrink();
                }
            }
        });
    }

    public void updateRecyclerView() {
        ArrayList<Workspace> newBoardsArray = todoDatabaseHelper.getActiveBoards();
        workspacesAdapter.updateBoardsArrayList(newBoardsArray);
        if (!boardRecyclerView.canScrollVertically(-1)) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) boardRecyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        }
    }

    public void onWorkspaceClick(int position) {
        floatingActionButton.hide();
        MainActivity.selectedWorkspace = todoDatabaseHelper.getActiveBoards().get(position);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_board);
    }

    private void newWorkspace() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
        builder.setTitle("New workspace");
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.workspace_create_text_input, (ViewGroup) getView(), false);
        workspaceNameView = viewInflated.findViewById(R.id.boardNameEditText);
        builder.setView(viewInflated);
        builder.setPositiveButton("Create", (dialogInterface, i) -> {
            attemptCreateWorkspace();
            hideKeyboard(context, workspaceNameView.getEditText());
            dialogInterface.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            hideKeyboard(context, workspaceNameView.getEditText());
            dialogInterface.cancel();
        });
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        workspaceNameView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(s));
            }
        });
        workspaceNameView.requestFocus();
        showKeyboard();
    }

    private void attemptCreateWorkspace() {
        Workspace newWorkspace = new Workspace();
        newWorkspace.setName(workspaceNameView.getEditText().getText().toString());
        newWorkspace.setStatus(TodoDatabaseHelper.statusActive);
        newWorkspace.setSync_status(1);
        newWorkspace.setCreated_at(0);
        newWorkspace.setUpdated_at(0);
        MainActivity.selectedWorkspace = todoDatabaseHelper.addBoard(newWorkspace);

        MainActivity.startSync();
        Navigation.findNavController(requireView()).navigate(R.id.navigation_board);
    }
}
