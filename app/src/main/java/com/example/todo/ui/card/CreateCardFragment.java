package com.example.todo.ui.card;

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
import androidx.navigation.Navigation;

import com.example.todo.R;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Card;
import com.example.todo.models.Task;

public class CreateCardFragment extends Fragment {

    private EditText cardNameView;
    private Button createCard;
    private String name;
    private View focusView;

    private Context context;
    private TasksDatabaseHelper tasksDatabaseHelper;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        return inflater.inflate(R.layout.fragment_create_card, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);

        cardNameView = requireView().findViewById(R.id.cardNameEditText);
        createCard = requireView().findViewById(R.id.createCardButton);
        createCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptCreateCard();
            }
        });
    }

    private void attemptCreateCard() {
        Card newCard = new Card();

        if (validateInput()) {
            // Create new task
            newCard.setName(name);
            newCard.setCreated_at(0);
            newCard.setUpdated_at(0);
            tasksDatabaseHelper.addCard(newCard);

            navigateHome();
        } else {
            // Error; don't attempt to create task
            focusView.requestFocus();
        }
    }

    private boolean validateInput() {
        // Reset errors
        cardNameView.setError(null);

        // Store values at the time of the create attempt
        name = cardNameView.getText().toString();

        focusView = null;

        // Check for a valid task name
        if (TextUtils.isEmpty(name)) {
            cardNameView.setError(getString(R.string.error_field_required));
            focusView = cardNameView;
            return false;
        }

        return true;
    }

    private void navigateHome() {
        // Navigate to home fragment
        Navigation.findNavController(requireView()).navigate(R.id.navigation_home);
    }
}
