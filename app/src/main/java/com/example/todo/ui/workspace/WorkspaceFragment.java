package com.example.todo.ui.workspace;

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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.NotesAdapter;
import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.models.Workspace;
import com.example.todo.models.Note;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Objects;

import static com.example.todo.MainActivity.createTaskToolbar;
import static com.example.todo.MainActivity.floatingActionButton;
import static com.example.todo.MainActivity.hideKeyboard;
import static com.example.todo.MainActivity.notesToolbar;
import static com.example.todo.MainActivity.selectedWorkspace;
import static com.example.todo.MainActivity.selectedBoardToolbar;
import static com.example.todo.MainActivity.selectedTaskToolbar;
import static com.example.todo.MainActivity.settingsToolbar;
import static com.example.todo.MainActivity.showKeyboard;
import static com.example.todo.MainActivity.tasksToolbar;
import static com.example.todo.MainActivity.workspacesToolbar;

public class WorkspaceFragment extends Fragment implements NotesAdapter.OnNoteListener {
    private static final String TAG = "WorkspaceFragment";

    private Context context;
    private TextInputLayout boardNameView;
    private SwipeRefreshLayout swipeContainer;

    private TodoDatabaseHelper todoDatabaseHelper;
    private NotesAdapter notesAdapter;
    private RecyclerView recyclerView;

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

        return inflater.inflate(R.layout.fragment_workspace, container, false);
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
                    if (modelName.equals("note")) {
                        Log.d(TAG, "Note FCM push in workspace " + intent.getAction());
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tasksToolbar.setVisibility(View.GONE);
        workspacesToolbar.setVisibility(View.GONE);
        notesToolbar.setVisibility(View.GONE);
        settingsToolbar.setVisibility(View.GONE);
        selectedBoardToolbar.setVisibility(View.VISIBLE);
        selectedTaskToolbar.setVisibility(View.GONE);
        createTaskToolbar.setVisibility(View.GONE);

        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(MainActivity::startSync);

        selectedBoardToolbar.setTitle(MainActivity.selectedWorkspace.getName());
        selectedBoardToolbar.setNavigationOnClickListener(view1 -> Navigation.findNavController(requireView()).navigate(R.id.navigation_workspaces));
        selectedBoardToolbar.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.rename:
                    renameWorkspace();
                    return true;
                case R.id.favourite:
//                        Board board = MainActivity.selectedBoard;
//                        board.setStatus(TasksDatabaseHelper.statusFavourite);
//                        board.setSync_status(1);
//                        tasksDatabaseHelper.updateBoard(board);
                    return true;
                case R.id.delete:
                   deleteWorkspace();
            }
            return false;
        });

        floatingActionButton.setText("New note");
        floatingActionButton.setOnClickListener(v -> {
            floatingActionButton.hide();
            Navigation.findNavController(requireView()).navigate(R.id.navigation_create_task);
        });
        floatingActionButton.show();

        MainActivity.startSync();
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView = requireView().findViewById(R.id.boardNotesRecyclerView);

        // Construct the data source
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(context);
        ArrayList<Note> notesArray = todoDatabaseHelper.getBoardNotes(MainActivity.selectedWorkspace.getId());

        // Setting LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        // Create the adapter to convert the array to views
        notesAdapter = new NotesAdapter(notesArray, context, this);

        // Attach the adapter to a RecyclerView
        recyclerView.setAdapter(notesAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        ArrayList<Note> newNotesArray = todoDatabaseHelper.getBoardNotes(MainActivity.selectedWorkspace.getId());
        notesAdapter.updateNotesArrayList(newNotesArray);
        if (!recyclerView.canScrollVertically(-1)) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        }
    }

    private void renameWorkspace() {
        MaterialAlertDialogBuilder updateWorkspaceBuilder = new MaterialAlertDialogBuilder(getActivity());
        updateWorkspaceBuilder.setTitle("Rename workspace");
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.workspace_create_text_input, (ViewGroup) getView(), false);
        boardNameView = viewInflated.findViewById(R.id.boardNameEditText);
        boardNameView.getEditText().setText(selectedWorkspace.getName());
        boardNameView.getEditText().setSelectAllOnFocus(true);
        updateWorkspaceBuilder.setView(viewInflated);
        updateWorkspaceBuilder.setPositiveButton("Rename", (dialogInterface, i) -> {
            Workspace workspace = MainActivity.selectedWorkspace;
            workspace.setName(boardNameView.getEditText().getText().toString());
            workspace.setSync_status(1);
            todoDatabaseHelper.updateBoard(workspace);

            hideKeyboard(context, boardNameView.getEditText());
            dialogInterface.dismiss();
            MainActivity.startSync();
            Navigation.findNavController(requireView()).navigate(R.id.navigation_board);
        });
        updateWorkspaceBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            hideKeyboard(context, boardNameView.getEditText());
            dialogInterface.cancel();
        });

        final AlertDialog updateWorkspaceDialog = updateWorkspaceBuilder.create();
        updateWorkspaceDialog.setCanceledOnTouchOutside(false);
        updateWorkspaceDialog.show();

        updateWorkspaceDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        boardNameView.getEditText().addTextChangedListener(new TextWatcher() {
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
        boardNameView.requestFocus();
        showKeyboard();
    }

    private void deleteWorkspace() {
        MaterialAlertDialogBuilder removeWorkspaceBuilder = new MaterialAlertDialogBuilder(getActivity());
        removeWorkspaceBuilder.setTitle("Remove workspace?");
        removeWorkspaceBuilder.setMessage("This workspace will be temporarily deleted, but it can be restored with all related tasks.");
        removeWorkspaceBuilder.setPositiveButton("Remove", (dialogInterface, i) -> {
            Workspace deleted_workspace = MainActivity.selectedWorkspace;
            deleted_workspace.setStatus(TodoDatabaseHelper.statusDeleted);
            deleted_workspace.setSync_status(1);
            todoDatabaseHelper.updateBoard(deleted_workspace);

            dialogInterface.dismiss();
            MainActivity.startSync();
            Navigation.findNavController(requireView()).navigate(R.id.navigation_workspaces);
        });
        removeWorkspaceBuilder.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel());
        removeWorkspaceBuilder.show();
    }

    public void onNoteClick(int position) {
        floatingActionButton.hide();
        MainActivity.selectedNote = todoDatabaseHelper.getBoardNotes(MainActivity.selectedWorkspace.getId()).get(position);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_task);
    }
}
