package com.example.todo.ui.workspace;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.TasksAdapter;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Board;
import com.example.todo.models.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

import static com.example.todo.MainActivity.createTaskToolbar;
import static com.example.todo.MainActivity.floatingActionButton;
import static com.example.todo.MainActivity.hideKeyboard;
import static com.example.todo.MainActivity.notesToolbar;
import static com.example.todo.MainActivity.selectedBoard;
import static com.example.todo.MainActivity.selectedBoardToolbar;
import static com.example.todo.MainActivity.selectedTaskToolbar;
import static com.example.todo.MainActivity.settingsToolbar;
import static com.example.todo.MainActivity.showKeyboard;
import static com.example.todo.MainActivity.tasksToolbar;
import static com.example.todo.MainActivity.workspacesToolbar;

public class WorkspaceFragment extends Fragment implements TasksAdapter.OnTaskListener {
    private static final String TAG = "WorkspaceFragment";

    private Context context;
    private TextInputLayout boardNameView;
    private TasksDatabaseHelper tasksDatabaseHelper;
    private TasksAdapter tasksAdapter;
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

        return inflater.inflate(R.layout.fragment_board, container, false);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("fcmNotification")) {
                    String modelName = intent.getStringExtra("modelName");
                    if (modelName.equals("todo")) {
                        Log.e(TAG, "Todo === " + intent.getAction());
                        MainActivity.startSync();
                    }
                } else if (action.equals("updateRecyclerView")) {
                    if (intent.getBooleanExtra("updateStatus", true)) {
                        updateRecyclerView();
                    }
                }
            }
        }
    };

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

        selectedBoardToolbar.setTitle(MainActivity.selectedBoard.getName());
        selectedBoardToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(requireView()).navigate(R.id.navigation_workspaces);
            }
        });
        selectedBoardToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
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
            }
        });

        floatingActionButton.setText("New note");
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionButton.hide();
                Navigation.findNavController(requireView()).navigate(R.id.navigation_create_task);
            }
        });
        floatingActionButton.show();

        MainActivity.startSync();
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView = requireView().findViewById(R.id.boardTasksRecyclerView);

        // Construct the data source
        tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);
        ArrayList<Task> tasksArray = tasksDatabaseHelper.getBoardTasks(MainActivity.selectedBoard.getId());

        // Setting LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Create the adapter to convert the array to views
        tasksAdapter = new TasksAdapter(tasksArray, context, this);

        // Attach the adapter to a RecyclerView
        recyclerView.setAdapter(tasksAdapter);
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
        ArrayList<Task> newTasksArray = tasksDatabaseHelper.getBoardTasks(MainActivity.selectedBoard.getId());
        tasksAdapter.updateTasksArrayList(newTasksArray);
        if (!recyclerView.canScrollVertically(-1)) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        }
    }

    private void renameWorkspace() {
        MaterialAlertDialogBuilder updateWorkspaceBuilder = new MaterialAlertDialogBuilder(getActivity());
        updateWorkspaceBuilder.setTitle("Rename workspace");
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.board_create_text_input, (ViewGroup) getView(), false);
        boardNameView = viewInflated.findViewById(R.id.boardNameEditText);
        boardNameView.getEditText().setText(selectedBoard.getName());
        boardNameView.getEditText().setSelectAllOnFocus(true);
        updateWorkspaceBuilder.setView(viewInflated);
        updateWorkspaceBuilder.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Board board = MainActivity.selectedBoard;
                board.setName(boardNameView.getEditText().getText().toString());
                board.setSync_status(1);
                tasksDatabaseHelper.updateBoard(board);

                hideKeyboard(context, boardNameView.getEditText());
                dialogInterface.dismiss();
                MainActivity.startSync();
                Navigation.findNavController(requireView()).navigate(R.id.navigation_board);
            }
        });
        updateWorkspaceBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                hideKeyboard(context, boardNameView.getEditText());
                dialogInterface.cancel();
            }
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
                if (TextUtils.isEmpty(s)) {
                    updateWorkspaceDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    updateWorkspaceDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
        boardNameView.requestFocus();
        showKeyboard();
    }

    private void deleteWorkspace() {
        MaterialAlertDialogBuilder removeWorkspaceBuilder = new MaterialAlertDialogBuilder(getActivity());
        removeWorkspaceBuilder.setTitle("Remove workspace?");
        removeWorkspaceBuilder.setMessage("This workspace will be temporarily deleted, but it can be restored with all related tasks.");
        removeWorkspaceBuilder.setPositiveButton("Remove", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Board deleted_board = MainActivity.selectedBoard;
                deleted_board.setStatus(TasksDatabaseHelper.statusDeleted);
                deleted_board.setSync_status(1);
                tasksDatabaseHelper.updateBoard(deleted_board);

                dialogInterface.dismiss();
                MainActivity.startSync();
                Navigation.findNavController(requireView()).navigate(R.id.navigation_workspaces);
            }
        });
        removeWorkspaceBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        removeWorkspaceBuilder.show();
    }

    public void onTaskClick(int position) {
        floatingActionButton.hide();
        MainActivity.selectedTask = tasksDatabaseHelper.getBoardTasks(MainActivity.selectedBoard.getId()).get(position);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_task);
    }
}
