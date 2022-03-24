package com.example.todo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Board;
import com.example.todo.models.Task;
import com.example.todo.remote.ApiBoard;
import com.example.todo.remote.ApiService;
import com.example.todo.remote.ApiTask;
import com.example.todo.remote.ApiUtils;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

@SuppressLint("StaticFieldLeak")
public class ApiSync extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "ApiSync";

    private Context context;

    private LocalBroadcastManager broadcaster;
    private TasksDatabaseHelper tasksDatabaseHelper;

    private Boolean unauthorized = false;
    private final String apiUrl = "https://faf9-178-216-17-166.ngrok.io/v1/";
    private final ApiService apiService = ApiUtils.getAPIService(apiUrl);

    public ApiSync(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        broadcaster = LocalBroadcastManager.getInstance(context);
        tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);

        if (syncBoards()) {
            return syncTasks();
        }

        return false;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        MainActivity.apiSync = null;

        if (success) {
            Log.e(TAG, "Success");

            Intent intent = new Intent("updateRecyclerView");
            intent.putExtra("updateStatus", true);
            broadcaster.sendBroadcast(intent);
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
        MainActivity.apiSync = null;
    }

    private boolean syncBoards() {
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
                            board.setStatus(api_board.getStatus());
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
                        newBoard.setStatus(api_board.getStatus());
                        newBoard.setCreated_at(api_board.getCreated_at());
                        newBoard.setUpdated_at(api_board.getUpdated_at());
                        Board board = tasksDatabaseHelper.addBoard(newBoard);
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

    private Boolean syncBoard(Board board){
        ApiBoard apiBoard = new ApiBoard();
        if (board.getServer_id() != null) {
            apiBoard.setId(board.getServer_id());
        }
        apiBoard.setName(board.getName());
        apiBoard.setStatus(board.getStatus());
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
                board.setStatus(api_board.getStatus());
                board.setCreated_at(api_board.getCreated_at());
                board.setUpdated_at(api_board.getUpdated_at());
                board.setServer_id(api_board.getId());

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

    private boolean syncTasks() {
        // Sync local not synced tasks
        List<Task> tasksToSync = tasksDatabaseHelper.getNotSyncedTasks();
        for (Task taskToSync: tasksToSync){
            Log.e(TAG, "Sync tasks === ");
            if (!syncTask(taskToSync)){
                return false;
            }
        }

        // Sync with api tasks
        // Get latest update_at timestamp
        long updated_after = 0;
        List<Task> maxUpdatedAtRow = tasksDatabaseHelper.getTasksUpdateAfter();
        if (maxUpdatedAtRow.size() > 0) {
            updated_after = maxUpdatedAtRow.get(0).getUpdated_at();
        }

        Log.e(TAG, "Updated_after === " + updated_after);

        // Get tasks from API server
        try {
            Call<List<ApiTask>> call = apiService.getTodos(LoginActivity.getAuthToken(context), Long.toString(updated_after));
            Response<List<ApiTask>> response = call.execute();

            if (response.isSuccessful()) {

                List<ApiTask> api_tasks = response.body();
                Log.e(TAG, "Response_success === " + api_tasks.toString());

                // Save items to DB
                for (ApiTask api_task : api_tasks) {

                    List<Task> taskList = tasksDatabaseHelper.getTasks(api_task.getId());

                    if (taskList.size() > 0) {
                        Task task = taskList.get(0);

                        // Update
                        if (task != null && task.getUpdated_at() != api_task.getUpdated_at()) {
                            Log.e(TAG, "Updating task");

                            task.setServer_id(api_task.getId());
                            task.setSync_status(0);
                            task.setName(api_task.getName());
                            task.setText(api_task.getText());
                            task.setUpdated_at(api_task.getUpdated_at());
                            task.setStatus(api_task.getStatus());
                            tasksDatabaseHelper.updateTask(task);
                        }
                    }
                    // Insert
                    else {
                        Log.e(TAG, "Inserting task");

                        Task newTask = new Task();
                        newTask.setServer_id(api_task.getId());
                        if (api_task.getBoard_id() != null) {
                            Board board = tasksDatabaseHelper.getBoardByServerId(api_task.getBoard_id());
                            newTask.setBoard_id(board.getId());
                        }
                        newTask.setSync_status(0);
                        newTask.setName(api_task.getName());
                        newTask.setText(api_task.getText());
                        newTask.setCreated_at(api_task.getCreated_at());
                        newTask.setUpdated_at(api_task.getUpdated_at());
                        newTask.setStatus(api_task.getStatus());
                        Task task = tasksDatabaseHelper.addTask(newTask);
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

    private Boolean syncTask(Task task){
        ApiTask apiTask = new ApiTask();
        apiTask.setId(task.getServer_id());
        if (task.getBoard_id() != null) {
            apiTask.setBoard_id(tasksDatabaseHelper.getBoard(task.getBoard_id()).getServer_id());
        }
        apiTask.setName(task.getName());
        apiTask.setText(task.getText());
        apiTask.setCreated_at(task.getCreated_at());
        apiTask.setUpdated_at(task.getUpdated_at());
        apiTask.setStatus(task.getStatus());

        Call<ApiTask> call;

        try {
            // Update
            if (task.getServer_id() != null){
                Log.e(TAG, "Update === ");
                call = apiService.updateTask(LoginActivity.getAuthToken(context), task.getServer_id(), apiTask);
            }
            // Create
            else {
                Log.e(TAG, "Create === ");
                call = apiService.addTask(LoginActivity.getAuthToken(context), apiTask);
            }
            Response<ApiTask> response = call.execute();

            if (response.isSuccessful()) {
                Log.e(TAG, "Response_success === " + response.body().toString());

                // Update db task
                ApiTask api_task = response.body();
                task.setSync_status(0);
                task.setName(api_task.getName());
                task.setText(api_task.getText());
                task.setCreated_at(api_task.getCreated_at());
                task.setUpdated_at(api_task.getUpdated_at());
                task.setStatus(api_task.getStatus());

                if (task.getServer_id() == null) {
                    task.setServer_id(api_task.getId());
                }

                tasksDatabaseHelper.updateTask(task);

                Log.e(TAG, "Task has been synced successfully");
                return true;
            }
            // Unauthorized
            else if (response.code() == 401) {
                unauthorized = false;
                return false;
            }
            // Handle other responses
            else {
                Log.e(TAG, "TASK SYNC ERROR");
                return false;
            }
        } catch (IOException e) {
            Log.e("SYNC TASK ERROR: ", e.getMessage());
            return false;
        }
    }
}
