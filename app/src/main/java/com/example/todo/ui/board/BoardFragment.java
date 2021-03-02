package com.example.todo.ui.board;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.adapters.TasksAdapter;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

import static com.example.todo.MainActivity.context;

public class BoardFragment extends Fragment implements TasksAdapter.OnTaskListener {

    private static final String TAG = "BoardFragment";

    private TasksAdapter tasksAdapter;
    private TasksDatabaseHelper tasksDatabaseHelper;

    private ExtendedFloatingActionButton extendedFab;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_board, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(MainActivity.selectedBoard.getName());
        extendedFab = getActivity().findViewById(R.id.extended_fab);
        extendedFab.setText("Create Task");
        extendedFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(requireView()).navigate(R.id.navigation_task);
                extendedFab.hide();
            }
        });

        extendedFab.show();
        extendedFab.extend();

        initRecyclerView();
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = requireView().findViewById(R.id.boardTasksRecyclerView);

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
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(-1) && newState==RecyclerView.SCROLL_STATE_IDLE) {
                    extendedFab.extend();
                } else {
                    extendedFab.shrink();
                }
            }
        });
    }

    public void onTaskClick(int position) {
        MainActivity.selectedTask = tasksDatabaseHelper.getBoardTasks(MainActivity.selectedBoard.getId()).get(position);

        // Navigate to task fragment
        Navigation.findNavController(requireView()).navigate(R.id.navigation_task);
    }
}
