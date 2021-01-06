package com.example.todo.ui.board;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.Navigation;

import com.example.todo.MainActivity;
import com.example.todo.R;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Board;

public class CreateBoardFragment extends Fragment {

    private EditText boardNameView;
    private Button createBoard;
    private String name;
    private View focusView;

    private Context context;
    private TasksDatabaseHelper tasksDatabaseHelper;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        return inflater.inflate(R.layout.fragment_create_board, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);

        boardNameView = requireView().findViewById(R.id.boardNameEditText);
        createBoard = requireView().findViewById(R.id.createBoardButton);
        createBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptCreateBoard();
            }
        });
    }

    private void attemptCreateBoard() {
        Board newBoard = new Board();

        if (validateInput()) {
            // Create new board
            newBoard.setName(name);
            newBoard.setSync_status(1);
            newBoard.setCreated_at(0);
            newBoard.setUpdated_at(0);
            MainActivity.selectedBoard = tasksDatabaseHelper.addBoard(newBoard);

            navigateDashboard();
        } else {
            // Error; don't attempt to create board
            focusView.requestFocus();
        }
    }

    private boolean validateInput() {
        // Reset errors
        boardNameView.setError(null);

        // Store values at the time of the create attempt
        name = boardNameView.getText().toString();

        focusView = null;

        // Check for a valid board name
        if (TextUtils.isEmpty(name)) {
            boardNameView.setError(getString(R.string.error_field_required));
            focusView = boardNameView;
            return false;
        }

        return true;
    }

    private void navigateDashboard() {
        // Navigate to dashboard fragment
        Navigation.findNavController(requireView()).navigate(R.id.navigation_dashboard);
    }
}
