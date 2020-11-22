package com.example.todo.ui.settings;

import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.todo.InitApplicationTheme;
import com.example.todo.LoginActivity;
import com.example.todo.R;
import com.kyleduo.switchbutton.SwitchButton;

public class SettingsFragment extends Fragment {

    private Context context;
    private LayoutInflater inflater;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = container.getContext();
        this.inflater = inflater;
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView logout = requireView().findViewById(R.id.logoutTextView);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Log.e("Account Fragment: ", "Logout");

            LoginActivity.deleteAuthToken(context);

            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            getActivity().finish();
            }
        });

        final SwitchButton dayNightSwitch = (SwitchButton) requireView().findViewById(R.id.day_night_switch);

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            dayNightSwitch.setCheckedImmediatelyNoEvent(true);
        }

        dayNightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    InitApplicationTheme.setNightMode(true, context);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    InitApplicationTheme.setNightMode(false, context);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        });
    }
}
