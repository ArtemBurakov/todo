package com.example.todo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.todo.models.Task;

import java.util.ArrayList;

public class TasksDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "ToDo";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_TASKS = "tasks";

    // Post Table Columns
    private static final String KEY_TASK_ID = "id";
    private static final String KEY_TASK_NAME = "name";
    private static final String KEY_TASK_TEXT = "text";
    private static final String KEY_TASK_STATUS = "status";
    private static final String KEY_TASK_CREATED_AT = "created_at";
    private static final String KEY_TASK_UPDATED_AT = "updated_at";

    private static TasksDatabaseHelper sInstance;

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
                KEY_TASK_NAME + " TEXT," +
                KEY_TASK_TEXT + " TEXT," +
                KEY_TASK_STATUS + " int," +
                KEY_TASK_CREATED_AT + " int," +
                KEY_TASK_UPDATED_AT + " int" +
                ")";
        db.execSQL(CREATE_TASKS_TABLE);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
            onCreate(db);
        }
    }

    // Add task
    public Task addTask(String name, String text, int status, long created_at, long updated_at) {
        Task task = null;

        Log.e(TAG, "Adding task " + name + ", status which = " + status + ", to table " + TABLE_TASKS);

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TASK_NAME, name);
        values.put(KEY_TASK_TEXT, text);
        values.put(KEY_TASK_STATUS, status);
        values.put(KEY_TASK_CREATED_AT, created_at);
        values.put(KEY_TASK_UPDATED_AT, updated_at);

        long id = db.insert(TABLE_TASKS, null, values);
        if (id != -1) {
            task = getTask(id);
        }
        return task;
    }

    // Update task
    public Boolean updateTask(Task task) {
        Log.e(TAG, "Updating task " + task.getName() + ", id which  = " + task.getId() + ", status = " + task.getStatus() + " in table " + TABLE_TASKS);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_TASK_NAME, task.getName());
        values.put(KEY_TASK_TEXT, task.getText());
        values.put(KEY_TASK_STATUS, task.getStatus());
        values.put(KEY_TASK_UPDATED_AT, task.getUpdated_at());
        long result = db.update(TABLE_TASKS, values, KEY_TASK_ID + " = ?", new String[] {String.valueOf(task.getId())});
        return result != -1;
    }

    // Delete task
    public Boolean deleteTask(Task task) {
        Log.e(TAG, "Deleting task " + task.getName() + ", id which = " + task.getId() + " from " + TABLE_TASKS);

        SQLiteDatabase db = getWritableDatabase();
        long result = db.delete(TABLE_TASKS, KEY_TASK_ID + " = ?" , new String[] {String.valueOf(task.getId()) });
        return result != -1;
    }

    // Get task
    private Task getTask(long id) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where ID = ?", new String[] {String.valueOf(id)});

        try {
            if (cursor.moveToFirst()) {
                Task task = new Task();
                task.setId(cursor.getLong(cursor.getColumnIndex(KEY_TASK_ID)));
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

    // Get Active tasks
    public ArrayList<Task> getActiveTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_TASKS + " where " + KEY_TASK_STATUS + " = ? ", new String[] {String.valueOf(10)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    Task task = new Task();
                    task.setId(cursor.getLong(cursor.getColumnIndex(KEY_TASK_ID)));
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
                    task.setId(cursor.getLong(cursor.getColumnIndex(KEY_TASK_ID)));
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
                    task.setId(cursor.getLong(cursor.getColumnIndex(KEY_TASK_ID)));
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

    // Get amount of active tasks
    public int getTaskAmount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor= db.rawQuery("select COUNT (*) from " + TABLE_TASKS + " where " + KEY_TASK_STATUS + " = ?", new String[] {String.valueOf(10)});
        int count = 0;
        if (null != cursor) {
            if(cursor.getCount() > 0){
                cursor.moveToFirst();
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }
}