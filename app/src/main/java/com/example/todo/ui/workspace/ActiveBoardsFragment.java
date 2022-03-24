package com.example.todo.ui.workspace;

import static com.example.todo.MainActivity.floatingActionButton;
import static com.example.todo.MainActivity.hideKeyboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.BoardsAdapter;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Board;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class ActiveBoardsFragment extends Fragment implements BoardsAdapter.OnBoardListener {

    private Context context;

    private TextInputLayout boardNameView;
    private BoardsAdapter boardsAdapter;
    private TasksDatabaseHelper tasksDatabaseHelper;

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

        View root = inflater.inflate(R.layout.fragment_active_board, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                builder.setTitle("New workspace");
                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.board_create_text_input, (ViewGroup) getView(), false);
                boardNameView = viewInflated.findViewById(R.id.boardNameEditText);
                builder.setView(viewInflated);
                builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        attemptCreateBoard();
                        dialogInterface.dismiss();
                        boardNameView.clearFocus();
                        hideKeyboard(context, getActivity().getCurrentFocus());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        hideKeyboard(context, getActivity().getCurrentFocus());
                    }
                });
                final AlertDialog dialog = builder.create();
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        hideKeyboard(context, getActivity().getCurrentFocus());
                    }
                });
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
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
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        } else {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        }
                    }
                });
                boardNameView.requestFocus();
                //showKeyboard();
            }
        });

        initBoardRecyclerView();
        MainActivity.startSync();
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
                    if (modelName.equals("board")) {
                        MainActivity.startSync();
                    } else if (modelName.equals("todo")) {
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

    private void initBoardRecyclerView() {
        RecyclerView boardRecyclerView = requireView().findViewById(R.id.activeBoardRecyclerView);

        // Construct the data source
        tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);
        ArrayList<Board> boardsArray = tasksDatabaseHelper.getActiveBoards();

        // Setting GridLayoutManager
        boardRecyclerView.setHasFixedSize(true);
        boardRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        // Create the adapter to convert the array to views
        boardsAdapter = new BoardsAdapter(boardsArray, context, this);
        boardRecyclerView.setAdapter(boardsAdapter);
        boardRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx,int dy){
                super.onScrolled(recyclerView, dx, dy);
                if (dy >0) {
                    if (floatingActionButton.isShown()) {
                        floatingActionButton.hide();
                    }
                }
                else if (dy <0) {
                    if (!floatingActionButton.isShown()) {
                        floatingActionButton.show();
                    }
                }
            }
        });
    }

    public void updateRecyclerView() {
        ArrayList<Board> newBoardsArray = tasksDatabaseHelper.getActiveBoards();
        boardsAdapter.updateBoardsArrayList(newBoardsArray);
    }

    private void attemptCreateBoard() {
        Board newBoard = new Board();
        newBoard.setName(boardNameView.getEditText().getText().toString());
        newBoard.setStatus(TasksDatabaseHelper.statusActive);
        newBoard.setSync_status(1);
        newBoard.setCreated_at(0);
        newBoard.setUpdated_at(0);
        MainActivity.selectedBoard = tasksDatabaseHelper.addBoard(newBoard);

        initBoardRecyclerView();
        MainActivity.startSync();
    }

    public void onBoardClick(int position) {
        floatingActionButton.hide();
        MainActivity.selectedBoard = tasksDatabaseHelper.getActiveBoards().get(position);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_board);
    }
}