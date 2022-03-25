package com.example.todo.ui.workspace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.BoardsAdapter;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Board;

import java.util.ArrayList;

public class ArchiveBoardsFragment extends Fragment implements BoardsAdapter.OnBoardListener {

    private Context context;

    private BoardsAdapter boardsAdapter;
    private TasksDatabaseHelper tasksDatabaseHelper;
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

        View root = inflater.inflate(R.layout.fragment_archive_board, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        boardRecyclerView = requireView().findViewById(R.id.archiveBoardRecyclerView);

        // Construct the data source
        tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);
        ArrayList<Board> boardsArray = tasksDatabaseHelper.getArchiveBoards();

        // Setting GridLayoutManager
        boardRecyclerView.setHasFixedSize(true);
        boardRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));

        // Create the adapter to convert the array to views
        boardsAdapter = new BoardsAdapter(boardsArray, context, this);

        // Attach the adapter to a RecyclerView
        boardRecyclerView.setAdapter(boardsAdapter);
    }

    public void updateRecyclerView() {
        ArrayList<Board> newBoardsArray = tasksDatabaseHelper.getArchiveBoards();
        boardsAdapter.updateBoardsArrayList(newBoardsArray);
        LinearLayoutManager layoutManager = (LinearLayoutManager) boardRecyclerView.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void onBoardClick(int position) {
        MainActivity.selectedBoard = tasksDatabaseHelper.getArchiveBoards().get(position);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_board);
    }
}
