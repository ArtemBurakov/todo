package com.example.todo.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.todo.models.Workspace;
import com.example.todo.models.Note;
import com.example.todo.models.Task;

import java.util.ArrayList;

public class TodoDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "ToDo";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_NOTES = "notes";
    private static final String TABLE_BOARDS = "boards";

    // TASK Table Columns
    private static final String KEY_TASK_ID = "id";
    private static final String KEY_TASK_SERVER_ID = "server_id";
    private static final String KEY_TASK_SYNC_STATUS = "sync_status";
    private static final String KEY_TASK_NAME = "name";
    private static final String KEY_TASK_STATUS = "status";
    private static final String KEY_TASK_CREATED_AT = "created_at";
    private static final String KEY_TASK_UPDATED_AT = "updated_at";

    // NOTE Table Columns
    private static final String KEY_NOTE_ID = "id";
    private static final String KEY_NOTE_SERVER_ID = "server_id";
    private static final String KEY_NOTE_BOARD_ID = "board_id";
    private static final String KEY_NOTE_SYNC_STATUS = "sync_status";
    private static final String KEY_NOTE_NAME = "name";
    private static final String KEY_NOTE_TEXT = "text";
    private static final String KEY_NOTE_STATUS = "status";
    private static final String KEY_NOTE_CREATED_AT = "created_at";
    private static final String KEY_NOTE_UPDATED_AT = "updated_at";

    // Boards Table Columns
    private static final String KEY_BOARD_ID = "id";
    private static final String KEY_BOARD_SERVER_ID = "server_id";
    private static final String KEY_BOARD_SYNC_STATUS = "sync_status";
    private static final String KEY_BOARD_NAME = "name";
    private static final String KEY_BOARD_STATUS = "status";
    private static final String KEY_BOARD_CREATED_AT = "created_at";
    private static final String KEY_BOARD_UPDATED_AT = "updated_at";

    private static TodoDatabaseHelper sInstance;

    public static int statusDeleted = 0;
    public static int statusActive = 10;
    public static int statusDone = 20;
    public static int statusFavourite = 30;

    public static synchronized TodoDatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new TodoDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private TodoDatabaseHelper(Context context) {
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
                KEY_TASK_SYNC_STATUS + " INTEGER," +
                KEY_TASK_NAME + " TEXT," +
                KEY_TASK_STATUS + " INTEGER," +
                KEY_TASK_CREATED_AT + " int," +
                KEY_TASK_UPDATED_AT + " int" +
                ")";
        db.execSQL(CREATE_TASKS_TABLE);

        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES +
                "(" +
                KEY_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Define a primary key
                KEY_NOTE_SERVER_ID + " INTEGER," +
                KEY_NOTE_BOARD_ID + " INTEGER," +
                KEY_NOTE_SYNC_STATUS + " INTEGER," +
                KEY_NOTE_NAME + " TEXT," +
                KEY_NOTE_TEXT + " TEXT," +
                KEY_NOTE_STATUS + " INTEGER," +
                KEY_NOTE_CREATED_AT + " int," +
                KEY_NOTE_UPDATED_AT + " int" +
                ")";
        db.execSQL(CREATE_NOTES_TABLE);

        // Boards
        String CREATE_BOARDS_TABLE = "CREATE TABLE " + TABLE_BOARDS +
                "(" +
                KEY_BOARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Define a primary key
                KEY_BOARD_SERVER_ID + " INTEGER," +
                KEY_BOARD_SYNC_STATUS + " INTEGER," +
                KEY_BOARD_NAME + " TEXT," +
                KEY_NOTE_STATUS + " INTEGER," +
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
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOARDS);
            onCreate(db);
        }
    }

    // Delete all tasks from table tasks
    public static void deleteAllTasks(Context context) {
        TodoDatabaseHelper todoDatabaseHelper = new TodoDatabaseHelper(context);
        SQLiteDatabase db = todoDatabaseHelper.getWritableDatabase();

        try {
            db.execSQL("DELETE FROM tasks");
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_TASKS + "'");
            db.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    // Delete all notes from table notes
    public static void deleteAllNotes(Context context) {
        Log.e(TAG, "Deleting all NOTEs from table " + TABLE_NOTES);

        TodoDatabaseHelper todoDatabaseHelper = new TodoDatabaseHelper(context);
        SQLiteDatabase db = todoDatabaseHelper.getWritableDatabase();

        try {
            db.execSQL("DELETE FROM notes");
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_NOTES + "'");
            db.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    // Delete all boards from table boards
    public static void deleteAllBoards(Context context) {
        Log.e(TAG, "Deleting all boards from table " + TABLE_BOARDS);

        TodoDatabaseHelper todoDatabaseHelper = new TodoDatabaseHelper(context);
        SQLiteDatabase db = todoDatabaseHelper.getWritableDatabase();

        try {
            db.execSQL("DELETE FROM boards");
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_BOARDS + "'");
            db.close();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    // Add task
    public Task addTask(Task task) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TASK_SYNC_STATUS, task.getSync_status());
        values.put(KEY_TASK_NAME, task.getName());
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

        return task;
    }

    // Add note
    public Note addNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOTE_SYNC_STATUS, note.getSync_status());
        values.put(KEY_NOTE_NAME, note.getName());
        values.put(KEY_NOTE_TEXT, note.getText());
        values.put(KEY_NOTE_STATUS, note.getStatus());
        if (note.getCreated_at() > 0) {
            values.put(KEY_NOTE_CREATED_AT, note.getCreated_at());
        }
        if (note.getUpdated_at() > 0) {
            values.put(KEY_NOTE_UPDATED_AT, note.getUpdated_at());
        }
        if (note.getServer_id() != null) {
            values.put(KEY_NOTE_SERVER_ID, note.getServer_id());
        }
        if (note.getBoard_id() != null) {
            values.put(KEY_NOTE_BOARD_ID, note.getBoard_id());
        }
        long id = db.insert(TABLE_NOTES, null, values);
        if (id != -1) {
            note = getNote(id);
        }

        return note;
    }

    // Add board
    public Workspace addBoard(Workspace workspace) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BOARD_NAME, workspace.getName());
        values.put(KEY_BOARD_SYNC_STATUS, workspace.getSync_status());
        values.put(KEY_BOARD_STATUS, workspace.getStatus());
        values.put(KEY_BOARD_SERVER_ID, workspace.getServer_id());
        if (workspace.getCreated_at() > 0) {
            values.put(KEY_BOARD_CREATED_AT, workspace.getCreated_at());
        }
        if (workspace.getUpdated_at() > 0) {
            values.put(KEY_BOARD_UPDATED_AT, workspace.getUpdated_at());
        }
        long id = db.insert(TABLE_BOARDS, null, values);
        if (id != -1) {
            workspace = getBoard(id);
        }

        return workspace;
    }

    // Update task
    public void updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (task.getServer_id() != null) {
            values.put(KEY_TASK_SERVER_ID, task.getServer_id());
        }
        values.put(KEY_TASK_SYNC_STATUS, task.getSync_status());
        values.put(KEY_TASK_NAME, task.getName());
        values.put(KEY_TASK_STATUS, task.getStatus());
        values.put(KEY_TASK_CREATED_AT, task.getCreated_at());
        values.put(KEY_TASK_UPDATED_AT, task.getUpdated_at());
        long result = db.update(TABLE_TASKS, values, KEY_TASK_ID + " = ?", new String[] {String.valueOf(task.getId())});
    }

    // Update note
    public void updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (note.getServer_id() != null) {
            values.put(KEY_NOTE_SERVER_ID, note.getServer_id());
        }
        values.put(KEY_NOTE_SYNC_STATUS, note.getSync_status());
        values.put(KEY_NOTE_NAME, note.getName());
        values.put(KEY_NOTE_TEXT, note.getText());
        values.put(KEY_NOTE_STATUS, note.getStatus());
        values.put(KEY_NOTE_CREATED_AT, note.getCreated_at());
        values.put(KEY_NOTE_UPDATED_AT, note.getUpdated_at());
        long result = db.update(TABLE_NOTES, values, KEY_NOTE_ID + " = ?", new String[] {String.valueOf(note.getId())});
    }

    // Update board
    public Workspace updateBoard(Workspace workspace) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (workspace.getServer_id() != null) {
            values.put(KEY_BOARD_SERVER_ID, workspace.getServer_id());
        }
        values.put(KEY_BOARD_SYNC_STATUS, workspace.getSync_status());
        values.put(KEY_BOARD_NAME, workspace.getName());
        values.put(KEY_BOARD_STATUS, workspace.getStatus());
        values.put(KEY_BOARD_CREATED_AT, workspace.getCreated_at());
        values.put(KEY_BOARD_UPDATED_AT, workspace.getUpdated_at());
        long id = db.update(TABLE_BOARDS, values, KEY_BOARD_ID + " = ?", new String[] {String.valueOf(workspace.getId())});

        if (id != -1) {
            workspace = getBoard(id);
        }

        return workspace;
    }

    // Get task
    @SuppressLint("Range")
    public Task getTask(long id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where ID = ?", new String[] {String.valueOf(id)});

        try {
            if (cursor.moveToFirst()) {
                Task task = new Task();
                task.setId(cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID)));
                task.setName(cursor.getString(cursor.getColumnIndex(KEY_TASK_NAME)));
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

    // Get note
    @SuppressLint("Range")
    public Note getNote(long id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NOTES + " where ID = ?", new String[] {String.valueOf(id)});

        try {
            if (cursor.moveToFirst()) {
                Note note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_ID)));
                note.setName(cursor.getString(cursor.getColumnIndex(KEY_NOTE_NAME)));
                note.setText(cursor.getString(cursor.getColumnIndex(KEY_NOTE_TEXT)));
                note.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_STATUS)));
                note.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_CREATED_AT)));
                note.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_UPDATED_AT)));
                return note;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get NOTE from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return null;
    }

    // Get board
    @SuppressLint("Range")
    public Workspace getBoard(long id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_BOARDS + " where ID = ?", new String[] {String.valueOf(id)});

        try {
            if (cursor.moveToFirst()) {
                Workspace workspace = new Workspace();
                workspace.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_ID)));
                workspace.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_SERVER_ID)));
                workspace.setName(cursor.getString(cursor.getColumnIndex(KEY_BOARD_NAME)));
                workspace.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_STATUS)));
                workspace.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_CREATED_AT)));
                workspace.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_UPDATED_AT)));
                return workspace;
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

    // Get board
    @SuppressLint("Range")
    public Workspace getBoardByServerId(Integer id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_BOARDS + " where " + KEY_BOARD_SERVER_ID + " = ? ", new String[] {String.valueOf(id)});

        try {
            if (cursor.moveToFirst()) {
                Workspace workspace = new Workspace();
                workspace.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_ID)));
                workspace.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_SERVER_ID)));
                workspace.setName(cursor.getString(cursor.getColumnIndex(KEY_BOARD_NAME)));
                workspace.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_STATUS)));
                workspace.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_CREATED_AT)));
                workspace.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_UPDATED_AT)));
                return workspace;
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

    // Get board notes
    @SuppressLint("Range")
    public ArrayList<Note> getWorkspaceNotes(Integer board_id) {
        ArrayList<Note> notes = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NOTES + " where " + KEY_NOTE_BOARD_ID + " = ? AND " + KEY_NOTE_STATUS + " = ? order by `name` asc", new String[] {String.valueOf(board_id), String.valueOf(10)}, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    note.setId(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_ID)));
                    note.setBoard_id(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_BOARD_ID)));
                    note.setName(cursor.getString(cursor.getColumnIndex(KEY_NOTE_NAME)));
                    note.setText(cursor.getString(cursor.getColumnIndex(KEY_NOTE_TEXT)));
                    note.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_STATUS)));
                    note.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_CREATED_AT)));
                    note.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_UPDATED_AT)));
                    notes.add(note);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get NOTEs from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return notes;
    }

    // Get Active tasks
    @SuppressLint("Range")
    public ArrayList<Task> getActiveAndDoneTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where " + KEY_TASK_STATUS + " in (" + statusActive + ", " + statusDone + ") order by `status` ASC, `name` asc", null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    if (!cursor.isNull(cursor.getColumnIndex(KEY_TASK_SERVER_ID))) {
                        task.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_TASK_SERVER_ID)));
                    }
                    task.setId(cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID)));
                    task.setName(cursor.getString(cursor.getColumnIndex(KEY_TASK_NAME)));
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
    @SuppressLint("Range")
    public ArrayList<Task> getArchiveTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where " + KEY_TASK_STATUS + " = ? ", new String[] {String.valueOf(0)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    if (!cursor.isNull(cursor.getColumnIndex(KEY_TASK_SERVER_ID))) {
                        task.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_TASK_SERVER_ID)));
                    }
                    task.setId(cursor.getInt(cursor.getColumnIndex(KEY_TASK_ID)));
                    task.setName(cursor.getString(cursor.getColumnIndex(KEY_TASK_NAME)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_TASK_STATUS)));
                    task.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_CREATED_AT)));
                    task.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_TASK_UPDATED_AT)));
                    tasks.add(task);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get archive tasks from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }

    // Get Active notes
    @SuppressLint("Range")
    public ArrayList<Note> getActiveNotes() {
        ArrayList<Note> notes = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NOTES + " where " + KEY_NOTE_STATUS + " = ? and " + KEY_NOTE_BOARD_ID + " is null order by `name` asc", new String[] {String.valueOf(10)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    if (!cursor.isNull(cursor.getColumnIndex(KEY_NOTE_SERVER_ID))) {
                        note.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_SERVER_ID)));
                    }
                    note.setId(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_ID)));
                    note.setName(cursor.getString(cursor.getColumnIndex(KEY_NOTE_NAME)));
                    note.setText(cursor.getString(cursor.getColumnIndex(KEY_NOTE_TEXT)));
                    note.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_STATUS)));
                    note.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_CREATED_AT)));
                    note.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_UPDATED_AT)));
                    notes.add(note);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get NOTEs from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return notes;
    }

    // Get Favourite notes
    @SuppressLint("Range")
    public ArrayList<Note> getFavouriteNotes() {
        ArrayList<Note> notes = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NOTES + " where " + KEY_NOTE_STATUS + " = ? AND " + KEY_NOTE_BOARD_ID + " IS NULL ", new String[] {String.valueOf(30)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    if (!cursor.isNull(cursor.getColumnIndex(KEY_NOTE_SERVER_ID))) {
                        note.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_SERVER_ID)));
                    }
                    note.setId(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_ID)));
                    note.setName(cursor.getString(cursor.getColumnIndex(KEY_NOTE_NAME)));
                    note.setText(cursor.getString(cursor.getColumnIndex(KEY_NOTE_TEXT)));
                    note.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_STATUS)));
                    note.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_CREATED_AT)));
                    note.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_UPDATED_AT)));
                    notes.add(note);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get NOTEs from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return notes;
    }

    // Get Completed notes
    @SuppressLint("Range")
    public ArrayList<Note> getCompletedNotes() {
        ArrayList<Note> notes = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NOTES + " where " + KEY_NOTE_STATUS + " = ?", new String[] {String.valueOf(20)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    note.setId(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_ID)));
                    note.setName(cursor.getString(cursor.getColumnIndex(KEY_NOTE_NAME)));
                    note.setText(cursor.getString(cursor.getColumnIndex(KEY_NOTE_TEXT)));
                    note.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_STATUS)));
                    note.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_CREATED_AT)));
                    note.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_UPDATED_AT)));
                    notes.add(note);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get NOTEs from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return notes;
    }

    // Get Archive notes
    @SuppressLint("Range")
    public ArrayList<Note> getArchiveNotes() {
        ArrayList<Note> notes = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NOTES + " where " + KEY_NOTE_STATUS + " = ?", new String[] {String.valueOf(0)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    note.setId(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_ID)));
                    note.setName(cursor.getString(cursor.getColumnIndex(KEY_NOTE_NAME)));
                    note.setText(cursor.getString(cursor.getColumnIndex(KEY_NOTE_TEXT)));
                    note.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_STATUS)));
                    note.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_CREATED_AT)));
                    note.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_UPDATED_AT)));
                    notes.add(note);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get NOTEs from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return notes;
    }

    // Get not synced tasks
    @SuppressLint("Range")
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

    // Get not synced notes
    @SuppressLint("Range")
    public ArrayList<Note> getNotSyncedNotes() {
        ArrayList<Note> notes = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NOTES + " where " + KEY_NOTE_SYNC_STATUS + " = ?", new String[] {String.valueOf(1)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    if (cursor.isNull(cursor.getColumnIndex(KEY_NOTE_SERVER_ID))) {
                        note.setServer_id(null);
                    } else {
                        note.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_SERVER_ID)));
                    }
                    if (cursor.isNull(cursor.getColumnIndex(KEY_NOTE_BOARD_ID))) {
                        note.setBoard_id(null);
                    } else {
                        note.setBoard_id(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_BOARD_ID)));
                    }
                    note.setId(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_ID)));
                    note.setSync_status(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_SYNC_STATUS)));
                    note.setName(cursor.getString(cursor.getColumnIndex(KEY_NOTE_NAME)));
                    note.setText(cursor.getString(cursor.getColumnIndex(KEY_NOTE_TEXT)));
                    note.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_STATUS)));
                    note.setCreated_at(cursor.getLong(cursor.getColumnIndex(KEY_NOTE_CREATED_AT)));
                    note.setUpdated_at(cursor.getLong(cursor.getColumnIndex(KEY_NOTE_UPDATED_AT)));
                    notes.add(note);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get NOTEs from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return notes;
    }

    // Get not synced boards
    @SuppressLint("Range")
    public ArrayList<Workspace> getNotSyncedBoards() {
        ArrayList<Workspace> workspaces = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_BOARDS + " where " + KEY_BOARD_SYNC_STATUS + " = ?", new String[] {String.valueOf(1)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Workspace workspace = new Workspace();
                    if (cursor.isNull(cursor.getColumnIndex(KEY_BOARD_SERVER_ID))) {
                        workspace.setServer_id(null);
                    } else {
                        workspace.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_SERVER_ID)));
                    }
                    workspace.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_ID)));
                    workspace.setSync_status(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_SYNC_STATUS)));
                    workspace.setName(cursor.getString(cursor.getColumnIndex(KEY_BOARD_NAME)));
                    workspace.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_STATUS)));
                    workspace.setCreated_at(cursor.getLong(cursor.getColumnIndex(KEY_BOARD_CREATED_AT)));
                    workspace.setUpdated_at(cursor.getLong(cursor.getColumnIndex(KEY_BOARD_UPDATED_AT)));
                    workspaces.add(workspace);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get boards from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return workspaces;
    }

    // Get Tasks
    @SuppressLint("Range")
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

    // Get Notes
    @SuppressLint("Range")
    public ArrayList<Note> getNotes(Integer server_id) {
        ArrayList<Note> notes = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NOTES + " where " + KEY_NOTE_SERVER_ID + " = ?", new String[] {String.valueOf(server_id)});

        try {
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    note.setId(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_ID)));
                    note.setName(cursor.getString(cursor.getColumnIndex(KEY_NOTE_NAME)));
                    note.setText(cursor.getString(cursor.getColumnIndex(KEY_NOTE_TEXT)));
                    note.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_STATUS)));
                    note.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_CREATED_AT)));
                    note.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_NOTE_UPDATED_AT)));
                    notes.add(note);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get NOTEs from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return notes;
    }

    // Get active boards
    @SuppressLint("Range")
    public ArrayList<Workspace> getActiveBoards() {
        ArrayList<Workspace> workspaces = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_BOARDS + " where " + KEY_BOARD_STATUS + " = ? order by `name` asc", new String[] {String.valueOf(10)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Workspace workspace = new Workspace();
                    workspace.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_ID)));
                    workspace.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_SERVER_ID)));
                    workspace.setName(cursor.getString(cursor.getColumnIndex(KEY_BOARD_NAME)));
                    workspace.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_STATUS)));
                    workspace.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_CREATED_AT)));
                    workspace.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_UPDATED_AT)));
                    workspaces.add(workspace);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get active boards from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return workspaces;
    }

    // Get number of notes inside workspace
    @SuppressLint("Range")
    public int getNumberOfNotes(Integer workspace_id) {
        int numberOfNotes = 0;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select count(id) from " + TABLE_NOTES + " where " + KEY_NOTE_STATUS + " = ? and " + KEY_NOTE_BOARD_ID + " = ?", new String[] {String.valueOf(10), String.valueOf(workspace_id)});
        try {
            if (cursor.moveToFirst()) {
                numberOfNotes = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get number of active notes in workspace from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return numberOfNotes;
    }

    // Get archive boards
    @SuppressLint("Range")
    public ArrayList<Workspace> getArchiveBoards() {
        ArrayList<Workspace> workspaces = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_BOARDS + " where " + KEY_BOARD_STATUS + " = ?", new String[] {String.valueOf(0)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Workspace workspace = new Workspace();
                    workspace.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_ID)));
                    workspace.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_SERVER_ID)));
                    workspace.setName(cursor.getString(cursor.getColumnIndex(KEY_BOARD_NAME)));
                    workspace.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_STATUS)));
                    workspace.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_CREATED_AT)));
                    workspace.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_UPDATED_AT)));
                    workspaces.add(workspace);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get archive boards from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return workspaces;
    }

    // Get favourite boards
    @SuppressLint("Range")
    public ArrayList<Workspace> getFavouriteBoards() {
        ArrayList<Workspace> workspaces = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_BOARDS + " where " + KEY_BOARD_STATUS + " = ?", new String[] {String.valueOf(30)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Workspace workspace = new Workspace();
                    workspace.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_ID)));
                    workspace.setServer_id(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_SERVER_ID)));
                    workspace.setName(cursor.getString(cursor.getColumnIndex(KEY_BOARD_NAME)));
                    workspace.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_STATUS)));
                    workspace.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_CREATED_AT)));
                    workspace.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_UPDATED_AT)));
                    workspaces.add(workspace);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get favourite boards from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return workspaces;
    }

    // Get boards by server_id
    @SuppressLint("Range")
    public ArrayList<Workspace> getBoardsByServerId(Integer server_id) {
        ArrayList<Workspace> workspaces = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_BOARDS + " where " + KEY_BOARD_SERVER_ID + " = ?", new String[] {String.valueOf(server_id)});

        try {
            if (cursor.moveToFirst()) {
                do {
                    Workspace workspace = new Workspace();
                    workspace.setId(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_ID)));
                    workspace.setName(cursor.getString(cursor.getColumnIndex(KEY_BOARD_NAME)));
                    workspace.setStatus(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_STATUS)));
                    workspace.setCreated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_CREATED_AT)));
                    workspace.setUpdated_at(cursor.getInt(cursor.getColumnIndex(KEY_BOARD_UPDATED_AT)));
                    workspaces.add(workspace);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get boards from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return workspaces;
    }

    // Get latest update_at timestamp for task
    @SuppressLint("Range")
    public ArrayList<Task> getTasksUpdateAfter() {
        ArrayList<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select max(updated_at) as updated_at from " + TABLE_TASKS + " where " + KEY_TASK_SYNC_STATUS + " = ?", new String[] {String.valueOf(1)});

        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    task.setUpdated_at(cursor.getInt(cursor.getColumnIndex("updated_at")));
                    tasks.add(task);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get task from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return tasks;
    }

    // Get latest update_at timestamp for note
    @SuppressLint("Range")
    public ArrayList<Note> getNotesUpdateAfter() {
        ArrayList<Note> notes = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select max(updated_at) as updated_at from " + TABLE_NOTES + " where " + KEY_NOTE_SYNC_STATUS + " = ?", new String[] {String.valueOf(1)});

        try {
            if (cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    note.setUpdated_at(cursor.getInt(cursor.getColumnIndex("updated_at")));
                    notes.add(note);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get NOTE from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return notes;
    }

    // Get latest update_at timestamp for board
    @SuppressLint("Range")
    public ArrayList<Workspace> getBoardsUpdateAfter() {
        ArrayList<Workspace> workspaces = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select max(updated_at) as updated_at from " + TABLE_BOARDS + " where " + KEY_BOARD_SYNC_STATUS + " = ?", new String[] {String.valueOf(1)});

        try {
            if (cursor.moveToFirst()) {
                do {
                    Workspace workspace = new Workspace();
                    workspace.setUpdated_at(cursor.getInt(cursor.getColumnIndex("updated_at")));
                    workspaces.add(workspace);
                } while(cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to get board from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return workspaces;
    }
}
