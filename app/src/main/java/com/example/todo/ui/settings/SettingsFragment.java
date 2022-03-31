package com.example.todo.ui.settings;

import static com.example.todo.MainActivity.context;
import static com.example.todo.MainActivity.floatingActionButton;
import static com.example.todo.MainActivity.notesToolbar;
import static com.example.todo.MainActivity.settingsToolbar;
import static com.example.todo.MainActivity.tasksToolbar;
import static com.example.todo.MainActivity.workspacesToolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.todo.InitApplicationTheme;
import com.example.todo.LoginActivity;
import com.example.todo.R;
import com.example.todo.ui.notes.ArchiveNotesActivity;
import com.example.todo.ui.tasks.ArchiveTasksActivity;
import com.example.todo.ui.workspaces.ArchiveWorkspacesActivity;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tasksToolbar.setVisibility(View.GONE);
        workspacesToolbar.setVisibility(View.GONE);
        notesToolbar.setVisibility(View.GONE);
        settingsToolbar.setVisibility(View.VISIBLE);
        floatingActionButton.hide();

        Preference username = findPreference("username");
        assert username != null;
        username.setSummary(LoginActivity.getUsername(context));
        Preference email = findPreference("email");
        assert email != null;
        email.setSummary(LoginActivity.getUserEmail(context));

        ListPreference theme = findPreference("theme");
        assert theme != null;
        theme.setOnPreferenceChangeListener((preference, newValue) -> {
            if ("DARK_MODE".equals(newValue)) {
                InitApplicationTheme.setThemeMode("DARK_MODE", context);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                return true;
            } else if ("LIGHT_MODE".equals(newValue)) {
                InitApplicationTheme.setThemeMode("LIGHT_MODE", context);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                return true;
            } else if ("SYSTEM_DEFAULT_MODE".equals(newValue)) {
                InitApplicationTheme.setThemeMode("SYSTEM_DEFAULT_MODE", context);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                return true;
            }
            return false;
        });

        Preference archive_tasks = findPreference("archive_tasks");
        assert archive_tasks != null;
        archive_tasks.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), ArchiveTasksActivity.class);
            startActivity(intent);
            return false;
        });
        Preference archive_workspaces = findPreference("archive_workspaces");
        assert archive_workspaces != null;
        archive_workspaces.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), ArchiveWorkspacesActivity.class);
            startActivity(intent);
            return false;
        });
        Preference archive_notes = findPreference("archive_notes");
        assert archive_notes != null;
        archive_notes.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(getActivity(), ArchiveNotesActivity.class);
            startActivity(intent);
            return false;
        });

        Preference logout = findPreference("logout");
        assert logout != null;
        logout.setOnPreferenceClickListener(preference -> {
            LoginActivity.deleteAuthToken(context);
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            getActivity().finish();
            return false;
        });
    }
}
