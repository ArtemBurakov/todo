package com.example.todo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.models.Workspace;
import com.example.todo.models.Note;
import com.example.todo.models.Task;
import com.example.todo.remote.ApiWorkspace;
import com.example.todo.remote.ApiService;
import com.example.todo.remote.ApiNote;
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
    private TodoDatabaseHelper todoDatabaseHelper;

    private Boolean unauthorized = false;
    private final String apiUrl = "http://192.168.88.23/php-yii2-todo/backend/web/v1/";
    private final ApiService apiService = ApiUtils.getAPIService(apiUrl);

    public ApiSync(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        broadcaster = LocalBroadcastManager.getInstance(context);
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(context);

        if (syncBoards()) {
            syncNotes();
            return syncTasks();
        }

        return false;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        MainActivity.apiSync = null;

        if (success) {
            Log.d(TAG, "Synchronizing is successful!");
            Intent intent = new Intent("updateRecyclerView");
            intent.putExtra("updateStatus", true);
            broadcaster.sendBroadcast(intent);
        }
        else {
            if (unauthorized){
                Log.e(TAG, "SYNC ITEM ERROR: unauthorized");
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
        List<Workspace> boardsToSync = todoDatabaseHelper.getNotSyncedBoards();
        for (Workspace workspaceToSync : boardsToSync){
            Log.d(TAG, "Syncing local boards...");
            if (!syncBoard(workspaceToSync)){
                return false;
            }
        }

        // Sync with api boards
        // Get latest update_at timestamp
        long updated_after = 0;
        List<Workspace> maxUpdatedAtRow = todoDatabaseHelper.getBoardsUpdateAfter();
        if (maxUpdatedAtRow.size() > 0) {
            updated_after = maxUpdatedAtRow.get(0).getUpdated_at();
        }

        // Get boards from API server
        try {
            Call<List<ApiWorkspace>> call = apiService.getBoards(LoginActivity.getAuthToken(context), Long.toString(updated_after));
            Response<List<ApiWorkspace>> response = call.execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Getting BOARDS from API is successful...");
                List<ApiWorkspace> api_boards = response.body();

                // Save items to DB
                for (ApiWorkspace api_board : api_boards) {

                    List<Workspace> workspaceList = todoDatabaseHelper.getBoardsByServerId(api_board.getId());

                    if (workspaceList.size() > 0) {
                        Workspace workspace = workspaceList.get(0);

                        // Update
                        if (workspace != null && workspace.getUpdated_at() != api_board.getUpdated_at()) {
                            workspace.setServer_id(api_board.getId());
                            workspace.setSync_status(0);
                            workspace.setName(api_board.getName());
                            workspace.setStatus(api_board.getStatus());
                            workspace.setUpdated_at(api_board.getUpdated_at());
                            todoDatabaseHelper.updateBoard(workspace);
                        }
                    }
                    // Insert
                    else {
                        Workspace newWorkspace = new Workspace();
                        newWorkspace.setServer_id(api_board.getId());
                        newWorkspace.setSync_status(0);
                        newWorkspace.setName(api_board.getName());
                        newWorkspace.setStatus(api_board.getStatus());
                        newWorkspace.setCreated_at(api_board.getCreated_at());
                        newWorkspace.setUpdated_at(api_board.getUpdated_at());
                        Workspace workspace = todoDatabaseHelper.addBoard(newWorkspace);
                    }
                }
                return true;
            }
            // Unauthorized
            else if (response.code() == 401) {
                Log.e(TAG, "Getting BOARDS from API is not successful -> unauthorized.");
                unauthorized = true;
                return false;
            }
            else{
                Log.e(TAG, "GET ARTICLES ERROR: response is not successful");
                return false;
            }

        } catch (IOException e) {
            Log.e(TAG, "GET ARTICLES ERROR: " + e.getMessage());
            return false;
        }
    }

    private Boolean syncBoard(Workspace workspace){
        ApiWorkspace apiWorkspace = new ApiWorkspace();
        if (workspace.getServer_id() != null) {
            apiWorkspace.setId(workspace.getServer_id());
        }
        apiWorkspace.setName(workspace.getName());
        apiWorkspace.setStatus(workspace.getStatus());
        apiWorkspace.setCreated_at(workspace.getCreated_at());
        apiWorkspace.setUpdated_at(workspace.getUpdated_at());

        Call<ApiWorkspace> call;

        try {
            // Update
            if (workspace.getServer_id() != null){
                call = apiService.updateBoard(LoginActivity.getAuthToken(context), workspace.getServer_id(), apiWorkspace);
            }
            // Create
            else {
                call = apiService.addBoard(LoginActivity.getAuthToken(context), apiWorkspace);
            }
            Response<ApiWorkspace> response = call.execute();

            if (response.isSuccessful()) {
                // Update db board
                ApiWorkspace api_board = response.body();
                workspace.setSync_status(0);
                workspace.setName(api_board.getName());
                workspace.setStatus(api_board.getStatus());
                workspace.setCreated_at(api_board.getCreated_at());
                workspace.setUpdated_at(api_board.getUpdated_at());
                workspace.setServer_id(api_board.getId());

                todoDatabaseHelper.updateBoard(workspace);
                return true;
            }
            // Unauthorized
            else if (response.code() == 401) {
                Log.e(TAG, "Syncing BOARD from API is not successful -> unauthorized.");
                unauthorized = false;
                return false;
            }
            // Handle other responses
            else {
                Log.e(TAG, "BOARD SYNC ERROR");
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "SYNC BOARD ERROR: " + e.getMessage());
            return false;
        }
    }

    private boolean syncNotes() {
        // Sync local not synced notes
        List<Note> notesToSync = todoDatabaseHelper.getNotSyncedNotes();
        for (Note noteToSync : notesToSync){
            Log.d(TAG, "Syncing local notes...");
            if (!syncNote(noteToSync)){
                return false;
            }
        }

        // Sync with api notes
        // Get latest update_at timestamp
        long updated_after = 0;
        List<Note> maxUpdatedAtRow = todoDatabaseHelper.getNotesUpdateAfter();
        if (maxUpdatedAtRow.size() > 0) {
            updated_after = maxUpdatedAtRow.get(0).getUpdated_at();
        }

        // Get note from API server
        try {
            Call<List<ApiNote>> call = apiService.getNotes(LoginActivity.getAuthToken(context), Long.toString(updated_after));
            Response<List<ApiNote>> response = call.execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Getting NOTES from API is successful...");
                List<ApiNote> api_notes = response.body();

                // Save items to DB
                for (ApiNote api_note : api_notes) {
                    List<Note> noteList = todoDatabaseHelper.getNotes(api_note.getId());

                    if (noteList.size() > 0) {
                        Note note = noteList.get(0);

                        // Update
                        if (note != null && note.getUpdated_at() != api_note.getUpdated_at()) {
                            note.setServer_id(api_note.getId());
                            note.setSync_status(0);
                            note.setName(api_note.getName());
                            note.setText(api_note.getText());
                            note.setUpdated_at(api_note.getUpdated_at());
                            note.setStatus(api_note.getStatus());
                            todoDatabaseHelper.updateNote(note);
                        }
                    }
                    // Insert
                    else {
                        Note newNote = new Note();
                        newNote.setServer_id(api_note.getId());
                        if (api_note.getBoard_id() != null) {
                            Workspace workspace = todoDatabaseHelper.getBoardByServerId(api_note.getBoard_id());
                            newNote.setBoard_id(workspace.getId());
                        }
                        newNote.setSync_status(0);
                        newNote.setName(api_note.getName());
                        newNote.setText(api_note.getText());
                        newNote.setCreated_at(api_note.getCreated_at());
                        newNote.setUpdated_at(api_note.getUpdated_at());
                        newNote.setStatus(api_note.getStatus());
                        Note note = todoDatabaseHelper.addNote(newNote);
                    }
                }
                return true;
            }
            // Unauthorized
            else if (response.code() == 401) {
                Log.e(TAG, "Getting NOTES from API is not successful -> unauthorized.");
                unauthorized = true;
                return false;
            }
            else{
                Log.e(TAG, "GET ARTICLES ERROR: response is not successful");
                return false;
            }

        } catch (IOException e) {
            Log.e(TAG, "GET ARTICLES ERROR: " + e.getMessage());
            return false;
        }
    }

    private Boolean syncNote(Note note){
        ApiNote apiNote = new ApiNote();
        apiNote.setId(note.getServer_id());
        if (note.getBoard_id() != null) {
            apiNote.setBoard_id(todoDatabaseHelper.getBoard(note.getBoard_id()).getServer_id());
        }
        apiNote.setName(note.getName());
        apiNote.setText(note.getText());
        apiNote.setCreated_at(note.getCreated_at());
        apiNote.setUpdated_at(note.getUpdated_at());
        apiNote.setStatus(note.getStatus());

        Call<ApiNote> call;

        try {
            // Update
            if (note.getServer_id() != null){
                call = apiService.updateNote(LoginActivity.getAuthToken(context), note.getServer_id(), apiNote);
            }
            // Create
            else {
                call = apiService.addNote(LoginActivity.getAuthToken(context), apiNote);
            }
            Response<ApiNote> response = call.execute();

            if (response.isSuccessful()) {
                // Update db task
                ApiNote api_note = response.body();
                note.setSync_status(0);
                note.setName(api_note.getName());
                note.setText(api_note.getText());
                note.setCreated_at(api_note.getCreated_at());
                note.setUpdated_at(api_note.getUpdated_at());
                note.setStatus(api_note.getStatus());

                if (note.getServer_id() == null) {
                    note.setServer_id(api_note.getId());
                }

                todoDatabaseHelper.updateNote(note);
                return true;
            }
            // Unauthorized
            else if (response.code() == 401) {
                Log.e(TAG, "Syncing NOTE from API is not successful -> unauthorized.");
                unauthorized = false;
                return false;
            }
            // Handle other responses
            else {
                Log.e(TAG, "NOTE SYNC ERROR");
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "SYNC NOTE ERROR: " + e.getMessage());
            return false;
        }
    }

    private boolean syncTasks() {
        // Sync local not synced tasks
        List<Task> tasksToSync = todoDatabaseHelper.getNotSyncedTasks();
        for (Task taskToSync : tasksToSync){
            Log.d(TAG, "Syncing local tasks...");
            if (!syncTask(taskToSync)){
                return false;
            }
        }

        // Sync with api tasks
        // Get latest update_at timestamp
        long updated_after = 0;
        List<Task> maxUpdatedAtRow = todoDatabaseHelper.getTasksUpdateAfter();
        if (maxUpdatedAtRow.size() > 0) {
            updated_after = maxUpdatedAtRow.get(0).getUpdated_at();
        }

        // Get tasks from API server
        try {
            Call<List<ApiTask>> call = apiService.getTasks(LoginActivity.getAuthToken(context), Long.toString(updated_after));
            Response<List<ApiTask>> response = call.execute();

            if (response.isSuccessful()) {
                Log.d(TAG, "Getting TASKS from API is successful...");

                List<ApiTask> api_tasks = response.body();

                // Save items to DB
                for (ApiTask api_task : api_tasks) {
                    List<Task> taskList = todoDatabaseHelper.getTasks(api_task.getId());

                    if (taskList.size() > 0) {
                        Task task = taskList.get(0);

                        // Update
                        if (task != null && task.getUpdated_at() != api_task.getUpdated_at()) {
                            task.setServer_id(api_task.getId());
                            task.setSync_status(0);
                            task.setName(api_task.getName());
                            task.setUpdated_at(api_task.getUpdated_at());
                            task.setStatus(api_task.getStatus());
                            todoDatabaseHelper.updateTask(task);
                        }
                    }
                    // Insert
                    else {
                        Task newTask = new Task();
                        newTask.setServer_id(api_task.getId());
                        newTask.setSync_status(0);
                        newTask.setName(api_task.getName());
                        newTask.setCreated_at(api_task.getCreated_at());
                        newTask.setUpdated_at(api_task.getUpdated_at());
                        newTask.setStatus(api_task.getStatus());
                        Task task = todoDatabaseHelper.addTask(newTask);
                    }
                }
                return true;
            }
            // Unauthorized
            else if (response.code() == 401) {
                Log.e(TAG, "Getting TASKS from API is not successful -> unauthorized.");
                unauthorized = true;
                return false;
            }
            else{
                Log.e(TAG, "GET ARTICLES ERROR: response is not successful");
                return false;
            }

        } catch (IOException e) {
            Log.e(TAG, "GET ARTICLES ERROR: " + e.getMessage());
            return false;
        }
    }

    private Boolean syncTask(Task task){
        ApiTask apiTask = new ApiTask();
        apiTask.setId(task.getServer_id());
        apiTask.setName(task.getName());
        apiTask.setCreated_at(task.getCreated_at());
        apiTask.setUpdated_at(task.getUpdated_at());
        apiTask.setStatus(task.getStatus());

        Call<ApiTask> call;

        try {
            // Update
            if (task.getServer_id() != null){
                call = apiService.updateTask(LoginActivity.getAuthToken(context), task.getServer_id(), apiTask);
            }
            // Create
            else {
                call = apiService.addTask(LoginActivity.getAuthToken(context), apiTask);
            }
            Response<ApiTask> response = call.execute();

            if (response.isSuccessful()) {
                // Update db task
                ApiTask api_task = response.body();
                task.setSync_status(0);
                task.setName(api_task.getName());
                task.setCreated_at(api_task.getCreated_at());
                task.setUpdated_at(api_task.getUpdated_at());
                task.setStatus(api_task.getStatus());

                if (task.getServer_id() == null) {
                    task.setServer_id(api_task.getId());
                }

                todoDatabaseHelper.updateTask(task);
                return true;
            }
            // Unauthorized
            else if (response.code() == 401) {
                Log.e(TAG, "Syncing TASK from API is not successful -> unauthorized.");
                unauthorized = false;
                return false;
            }
            // Handle other responses
            else {
                Log.e(TAG, "TASK SYNC ERROR");
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "SYNC TASK ERROR: " + e.getMessage());
            return false;
        }
    }
}
