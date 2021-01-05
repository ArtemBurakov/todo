package com.example.todo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.todo.models.Board;
import com.example.todo.models.Task;

import java.util.ArrayList;

public class TasksDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "ToDo";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_BOARDS = "boards";

    // Post Table Columns
    private static final String KEY_TASK_ID = "id";
    private static final String KEY_TASK_SERVER_ID = "server_id";
    private static final String KEY_TASK_BOARD_ID = "board_id";
    private static final String KEY_TASK_SYNC_STATUS = "sync_status";
    private static final String KEY_TASK_NAME = "name";
    private static final String KEY_TASK_TEXT = "text";
    private static final String KEY_TASK_STATUS = "status";
    private static final String KEY_TASK_CREATED_AT = "created_at";
    private static final String KEY_TASK_UPDATED_AT = "updated_at";

    // Boards Table Columns
    private static final String KEY_BOARD_ID = "id";
    private static final String KEY_BOARD_NAME = "name";
    private static final String KEY_BOARD_CREATED_AT = "created_at";
    private static final String KEY_BOARD_UPDATED_AT = "updated_at";

    private static TasksDatabaseHelper sInstance;

    public static int statusDeleted = 0;
    public static int statusActive = 10;
    public static int statusDone = 20;

    public static synchronized TasksDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TasksDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private TasksDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS +
                "(" +
                KEY_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Define a primary key
                KEY_TASK_SERVER_ID + " INTEGER," +
                KEY_TASK_BOARD_ID + " INTEGER," +
                KEY_TASK_SYNC_STATUS + " INTEGER," +
                KEY_TASK_NAME + " TEXT," +
                KEY_TASK_TEXT + " TEXT," +
                KEY_TASK_STATUS + " INTEGER," +
                KEY_TASK_CREATED_AT + " int," +
                KEY_TASK_UPDATED_AT + " int" +
                ")";
        db.execSQL(CREATE_TASKS_TABLE);

        // Boards
        String CREATE_BOARDS_TABLE = "CREATE TABLE " + TABLE_BOARDS +
                "(" +
                KEY_BOARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Define a primary key
                KEY_BOARD_NAME + " TEXT," +
                KEY_BOARD_CREATED_AT + " int," +
                KEY_BOARD_UPDATED_AT + " int" +
                ")";
        db.execSQL(CREATE_BOARDS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOARDS);
            onCreate(db);
        }
    }

    // Delete all tasks from table tasks
    public static void deleteAllTasks(Context context) {
        Log.e(TAG, "Deleting all tasks from table " + TABLE_TASKS);

        TasksDatabaseHelper tasksDatabaseHelper = new TasksDatabaseHelper(context);
        SQLiteDatabase db = tasksDatabaseHelper.getWritableDatabase();

        try {
            db.execSQL("DELETE FROM tasks");
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_TASKS + "'");
            db.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    // Add task
    public void addTask(Task task) {
        Log.e(TAG, "Adding task " + task.getName() + ", sync status which = " + task.getSync_status() + " server_id = " + task.getServer_id() + ", to table " + TABLE_TASKS);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TASK_SYNC_STATUS, task.getSync_status());
        values.put(KEY_TASK_BOARD_ID, task.getBoard_id());
        values.put(KEY_TASK_NAME, task.getName());
        values.put(KEY_TASK_TEXT, task.getText());
        values.put(KEY_TASK_STATUS, task.getStatus());
        if (task.getCreated_at() > 0) {
            values.put(KEY_TASK_CREATED_AT, task.getCreated_at());
        }
        if (task.getUpdated_at() > 0) {
            values.put(KEY_TASK_UPDATED_AT, task.getUpdated_at());
        }
        if (task.getServer_id() != null) {
            values.put(KEY_TASK_SERVER_ID, task.getServer_id());
        }
        long id = db.insert(TABLE_TASKS, null, values);
        if (id != -1) {
            task = getTask(id);
        }
    }

    // Update task
    public void updateTask(Task task) {
        Log.e(TAG, "Updating task " + task.getName() + ", sync status = " + task.getSync_status() + ", server_id = " + task.getServer_id() + ", status = " + task.getStatus() + " in table " + TABLE_TASKS);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (task.getServer_id() != null) {
            values.put(KEY_TASK_SERVER_ID, task.getServer_id());
        }
        values.put(KEY_TASK_SYNC_STATUS, task.getSync_status());
        values.put(KEY_TASK_NAME, task.getName());
        values.put(KEY_TASK_TEXT, task.getText());
        values.put(KEY_TASK_STATUS, task.getStatus());
        values.put(KEY_TASK_CREATED_AT, task.getCreated_at());
        values.put(KEY_TASK_UPDATED_AT, task.getUpdated_at());
        long result = db.update(TABLE_TASKS, values, KEY_TASK_ID + " = ?", new String[] {String.valueOf(task.getId())});
    }

    // Get task
    public Task getTask(long id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where ID = ?", new String[] {String.valueOf(id)});

        try {
            if (cursor.moveToFirst()) {
                Task task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID)));
                task.setName(cursor.getString(cursor.getColumnIndex(KEY_TASK_NAME)));
                task.setText(cursor.getString(cursor.getColumnIndex(KEY_TASK_TEXT)));
                task.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_TASK_STATUS)));
                task.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_CREATED_AT)));
                task.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_UPDATED_AT)));
                return task;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get task from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    // Get board
    public Board getBoard(long id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_BOARDS + " where ID = ?", new String[] {String.valueOf(id)});

        try {
            if (cursor.moveToFirst()) {
                Board board = new Board();
                board.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_ID)));
                board.setName(cursor.getString(cursor.getColumnIndex(KEY_BOARD_NAME)));
                board.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_CREATED_AT)));
                board.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_UPDATED_AT)));
                return board;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get board from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    // Get boards
    public ArrayList<Board> getBoards() {
        ArrayList<Board> boards = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_BOARDS, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Board board = new Board();
                    board.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_ID)));
                    board.setName(cursor.getString(cursor.getColumnIndex(KEY_BOARD_NAME)));
                    board.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_CREATED_AT)));
                    board.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_UPDATED_AT)));

                    boards.add(board);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get boards from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return boards;
    }

    // Add board
    public Board addBoard(Board board) {
        Log.e(TAG, "Adding board " + board.getName() + " to table " + TABLE_BOARDS);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BOARD_NAME, board.getName());
        if (board.getCreated_at() > 0) {
            values.put(KEY_BOARD_CREATED_AT, board.getCreated_at());
        }
        if (board.getUpdated_at() > 0) {
            values.put(KEY_BOARD_UPDATED_AT, board.getUpdated_at());
        }
        long id = db.insert(TABLE_BOARDS, null, values);
        if (id != -1) {
            board = getBoard(id);
        }

        return board;
    }

    // Get Completed tasks
    public ArrayList<Task> getBoardTasks(Integer board_id) {
        ArrayList<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where BOARD_ID = ?", new String[] {String.valueOf(board_id)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID)));
                    task.setName(cursor.getString(cursor.getColumnIndex(KEY_TASK_NAME)));
                    task.setText(cursor.getString(cursor.getColumnIndex(KEY_TASK_TEXT)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_TASK_STATUS)));
                    task.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_CREATED_AT)));
                    task.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_UPDATED_AT)));
                    tasks.add(task);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get tasks from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }

    // Get Active tasks
    public ArrayList<Task> getActiveTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where " + KEY_TASK_STATUS + " = ? AND " + KEY_TASK_BOARD_ID + " IS NULL ", new String[] {String.valueOf(10)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    if (!cursor.isNull(cursor.getColumnIndex(KEY_TASK_SERVER_ID))) {
                        task.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_TASK_SERVER_ID)));
                    }
                    task.setId(cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID)));
                    task.setName(cursor.getString(cursor.getColumnIndex(KEY_TASK_NAME)));
                    task.setText(cursor.getString(cursor.getColumnIndex(KEY_TASK_TEXT)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_TASK_STATUS)));
                    task.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_CREATED_AT)));
                    task.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_UPDATED_AT)));
                    tasks.add(task);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get tasks from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }

    // Get Completed tasks
    public ArrayList<Task> getCompletedTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where STATUS = ?", new String[] {String.valueOf(20)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID)));
                    task.setName(cursor.getString(cursor.getColumnIndex(KEY_TASK_NAME)));
                    task.setText(cursor.getString(cursor.getColumnIndex(KEY_TASK_TEXT)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_TASK_STATUS)));
                    task.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_CREATED_AT)));
                    task.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_UPDATED_AT)));
                    tasks.add(task);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get tasks from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }

    // Get Archive tasks
    public ArrayList<Task> getArchiveTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where " + KEY_TASK_STATUS + " = ?", new String[] {String.valueOf(0)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID)));
                    task.setName(cursor.getString(cursor.getColumnIndex(KEY_TASK_NAME)));
                    task.setText(cursor.getString(cursor.getColumnIndex(KEY_TASK_TEXT)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_TASK_STATUS)));
                    task.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_CREATED_AT)));
                    task.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_UPDATED_AT)));
                    tasks.add(task);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get tasks from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }

    // Get not synced tasks
    public ArrayList<Task> getNotSyncedTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where " + KEY_TASK_SYNC_STATUS + " = ?", new String[] {String.valueOf(1)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    if (cursor.isNull(cursor.getColumnIndex(KEY_TASK_SERVER_ID))) {
                        task.setServer_id(null);
                    } else {
                        task.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_TASK_SERVER_ID)));
                    }
                    task.setId(cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID)));
                    task.setSync_status(cursor.getInt(cursor.getColumnIndex(KEY_TASK_SYNC_STATUS)));
                    task.setName(cursor.getString(cursor.getColumnIndex(KEY_TASK_NAME)));
                    task.setText(cursor.getString(cursor.getColumnIndex(KEY_TASK_TEXT)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_TASK_STATUS)));
                    task.setCreated_at(cursor.getLong(cursor.getColumnIndex(KEY_TASK_CREATED_AT)));
                    task.setUpdated_at(cursor.getLong(cursor.getColumnIndex(KEY_TASK_UPDATED_AT)));
                    tasks.add(task);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get tasks from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }

    // Get tasks
    public ArrayList<Task> getTasks(Integer server_id) {
        ArrayList<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where " + KEY_TASK_SERVER_ID + " = ?", new String[] {String.valueOf(server_id)});

        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID)));
                    task.setName(cursor.getString(cursor.getColumnIndex(KEY_TASK_NAME)));
                    task.setText(cursor.getString(cursor.getColumnIndex(KEY_TASK_TEXT)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_TASK_STATUS)));
                    task.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_CREATED_AT)));
                    task.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_UPDATED_AT)));
                    tasks.add(task);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get tasks from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }

    // Get latest update_at timestamp
    public ArrayList<Task> getTasksUpdateAfter() {
        ArrayList<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select max(updated_at) as updated_at from tasks", null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    task.setUpdated_at(cursor.getInt(cursor.getColumnIndex("updated_at")));
                    tasks.add(task);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get tasks from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }
}