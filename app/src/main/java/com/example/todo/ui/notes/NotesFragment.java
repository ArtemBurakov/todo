package com.example.todo.ui.notes;

import static com.example.todo.MainActivity.createTaskToolbar;
import static com.example.todo.MainActivity.floatingActionButton;
import static com.example.todo.MainActivity.notesToolbar;
import static com.example.todo.MainActivity.selectedBoardToolbar;
import static com.example.todo.MainActivity.selectedTaskToolbar;
import static com.example.todo.MainActivity.settingsToolbar;
import static com.example.todo.MainActivity.tasksToolbar;
import static com.example.todo.MainActivity.workspacesToolbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.NotesAdapter;
import com.example.todo.database.TodoDatabaseHelper;
import com.example.todo.models.Note;

import java.util.ArrayList;

public class NotesFragment extends Fragment implements NotesAdapter.OnNoteListener {
    private static final String TAG = "NotesFragment";

    private Context context;
    private SwipeRefreshLayout swipeContainer;

    private NotesAdapter notesAdapter;
    private TodoDatabaseHelper todoDatabaseHelper;
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
        MainActivity.selectedWorkspace = null;

        return inflater.inflate(R.layout.fragment_notes, container, false);
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
                        Log.d(TAG, "Note FCM push " + intent.getAction());
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tasksToolbar.setVisibility(View.GONE);
        workspacesToolbar.setVisibility(View.GONE);
        notesToolbar.setVisibility(View.VISIBLE);
        settingsToolbar.setVisibility(View.GONE);
        selectedBoardToolbar.setVisibility(View.GONE);
        selectedTaskToolbar.setVisibility(View.GONE);
        createTaskToolbar.setVisibility(View.GONE);

        swipeContainer = view.findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MainActivity.startSync();
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
        floatingActionButton.extend();

        MainActivity.startSync();
        initRecyclerView();
    }

    private void initRecyclerView() {
        recyclerView = requireView().findViewById(R.id.notesRecyclerView);

        // Construct the data source
        todoDatabaseHelper = TodoDatabaseHelper.getInstance(context);
        ArrayList<Note> notesArray = todoDatabaseHelper.getActiveNotes();

        // Setting LayoutManager
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        // Create the adapter to convert the array to views
        notesAdapter = new NotesAdapter(notesArray, context, this);

        // Attach the adapter to a RecyclerView
        recyclerView.setAdapter(notesAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
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
        ArrayList<Note> newNotesArray = todoDatabaseHelper.getActiveNotes();
        notesAdapter.updateNotesArrayList(newNotesArray);
        if (!recyclerView.canScrollVertically(-1)) {
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.scrollToPositionWithOffset(0, 0);
            }
        }
    }

    public void onNoteClick(int position) {
        floatingActionButton.hide();
        MainActivity.selectedNote = todoDatabaseHelper.getActiveNotes().get(position);
        Navigation.findNavController(requireView()).navigate(R.id.navigation_task);
    }
}
