package com.example.todo.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.LoginActivity;
import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.BoardsAdapter;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Board;
import com.example.todo.models.Task;
import com.example.todo.remote.ApiBoard;
import com.example.todo.remote.ApiService;
import com.example.todo.remote.ApiUtils;
import com.example.todo.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class DashboardFragment extends Fragment implements BoardsAdapter.OnBoardListener {

    private static final String TAG = "DashboardFragment";

    private Context context;

    private ApiBoardSync syncBoard = null;
    private Boolean unauthorized = false;

    private BoardsAdapter boardsAdapter;
    private TasksDatabaseHelper tasksDatabaseHelper;

    public ExtendedFloatingActionButton extendedFab;

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);
        this.context = context;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.registerReceiver(receiver, new IntentFilter("fcmNotification"));

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        extendedFab = getActivity().findViewById(R.id.extended_fab);
        extendedFab.setText("Create Board");
        extendedFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(requireView()).navigate(R.id.navigation_create_board);
                extendedFab.hide();
            }
        });

        extendedFab.show();
        extendedFab.extend();

        initBoardRecyclerView();
        startSync();
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
            if (intent != null) {
                String modelName = intent.getStringExtra("modelName");
                if (modelName.equals("board")) {
                    Log.e(TAG, "Board === " + intent.getAction());
                    startSync();
                }
            }
        }
    };

    public void startSync(){
        if (syncBoard == null) {
            syncBoard = new ApiBoardSync();
            syncBoard.execute();
        }
    }

    private void initBoardRecyclerView() {
        RecyclerView boardRecyclerView = requireView().findViewById(R.id.boardRecyclerView);

        // Construct the data source
        tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);
        ArrayList<Board> boardsArray = tasksDatabaseHelper.getBoards();

        // Setting LayoutManager
        boardRecyclerView.setHasFixedSize(true);
        boardRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        // Create the adapter to convert the array to views
        boardsAdapter = new BoardsAdapter(boardsArray, context, this);

        // Attach the adapter to a RecyclerView
        boardRecyclerView.setAdapter(boardsAdapter);
    }

    @SuppressLint("StaticFieldLeak")
    public class ApiBoardSync extends AsyncTask<Void, Void, Boolean> {

        String apiUrl = "http://10.0.2.2/todo/backend/web/v1/";
        ApiService apiService = ApiUtils.getAPIService(apiUrl);

        @Override
        protected Boolean doInBackground(Void... params) {
            // Sync local not synced boards
            List<Board> boardsToSync = tasksDatabaseHelper.getNotSyncedBoards();
            for (Board boardToSync: boardsToSync){
                Log.e(TAG, "Sync boards === ");
                if (!syncBoard(boardToSync)){
                    return false;
                }
            }

            // Sync with api boards
            // Get latest update_at timestamp
            long updated_after = 0;
            List<Board> maxUpdatedAtRow = tasksDatabaseHelper.getBoardsUpdateAfter();
            if (maxUpdatedAtRow.size() > 0) {
                updated_after = maxUpdatedAtRow.get(0).getUpdated_at();
            }

            Log.e(TAG, "Boards Updated_after === " + updated_after);

            // Get boards from API server
            try {
                Call<List<ApiBoard>> call = apiService.getBoards(LoginActivity.getAuthToken(context), Long.toString(updated_after));
                Response<List<ApiBoard>> response = call.execute();

                if (response.isSuccessful()) {

                    List<ApiBoard> api_boards = response.body();
                    Log.e(TAG, "Boards Response_success === " + api_boards.toString());

                    // Save items to DB
                    for (ApiBoard api_board : api_boards) {

                        List<Board> boardList = tasksDatabaseHelper.getBoardsByServerId(api_board.getId());

                        if (boardList.size() > 0) {
                            Board board = boardList.get(0);

                            // Update
                            if (board != null && board.getUpdated_at() != api_board.getUpdated_at()) {
                                Log.e(TAG, "Updating task");

                                board.setServer_id(api_board.getId());
                                board.setSync_status(0);
                                board.setName(api_board.getName());
                                board.setUpdated_at(api_board.getUpdated_at());
                                tasksDatabaseHelper.updateBoard(board);
                            }
                        }
                        // Insert
                        else {
                            Log.e(TAG, "Inserting board");

                            Board newBoard = new Board();
                            newBoard.setServer_id(api_board.getId());
                            newBoard.setSync_status(0);
                            newBoard.setName(api_board.getName());
                            newBoard.setCreated_at(api_board.getCreated_at());
                            newBoard.setUpdated_at(api_board.getUpdated_at());
                            tasksDatabaseHelper.addBoard(newBoard);
                        }
                    }
                    return true;
                }
                // Unauthorized
                else if (response.code() == 401) {
                    unauthorized = true;
                    return false;
                }
                else{
                    Log.e("GET ARTICLES ERROR: ", "response is not successful");
                    return false;
                }

            } catch (IOException e) {
                Log.e("GET ARTICLES ERROR: ", e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            syncBoard = null;

            if (success) {
                Log.e(TAG, "Success");
                updateRecyclerView();
            }
            else {
                if (unauthorized){
                    Log.e("SYNC ITEM ERROR: ", "unauthorized");
                    LoginActivity.deleteAuthToken(context);

                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        }

        @Override
        protected void onCancelled() {
            syncBoard = null;
        }

        private Boolean syncBoard(Board board){
            Log.e(TAG, "Here === ");

            ApiBoard apiBoard = new ApiBoard();
            apiBoard.setId(board.getServer_id());
            apiBoard.setName(board.getName());
            apiBoard.setCreated_at(board.getCreated_at());
            apiBoard.setUpdated_at(board.getUpdated_at());

            Call<ApiBoard> call;

            try {
                // Update
                if (board.getServer_id() != null){
                    Log.e(TAG, "Update Board === ");
                    call = apiService.updateBoard(LoginActivity.getAuthToken(context), board.getServer_id(), apiBoard);
                }
                // Create
                else {
                    Log.e(TAG, "Create Board === ");
                    call = apiService.addBoard(LoginActivity.getAuthToken(context), apiBoard);
                }
                Response<ApiBoard> response = call.execute();

                if (response.isSuccessful()) {
                    Log.e(TAG, "Response_success === " + response.body().toString());

                    // Update db board
                    ApiBoard api_board = response.body();
                    board.setSync_status(0);
                    board.setName(api_board.getName());
                    board.setCreated_at(api_board.getCreated_at());
                    board.setUpdated_at(api_board.getUpdated_at());

                    if (board.getServer_id() == null) {
                        board.setServer_id(api_board.getId());
                    }

                    tasksDatabaseHelper.updateBoard(board);

                    Log.e(TAG, "Board has been synced successfully");
                    return true;
                }
                // Unauthorized
                else if (response.code() == 401) {
                    unauthorized = false;
                    return false;
                }
                // Handle other responses
                else {
                    Log.e(TAG, "BOARD SYNC ERROR");
                    return false;
                }
            } catch (IOException e) {
                Log.e("SYNC BOARD ERROR: ", e.getMessage());
                return false;
            }
        }
    }

    public void updateRecyclerView() {
        Log.e(TAG, "Updating recycler view");

        // Get new tasks from DB, update adapter
        ArrayList<Board> newBoardsArray = tasksDatabaseHelper.getBoards();
        boardsAdapter.updateBoardsArrayList(newBoardsArray);
    }

    public void onBoardClick(int position) {
        extendedFab.hide();

        MainActivity.selectedBoard = tasksDatabaseHelper.getBoards().get(position);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_board);
    }
}