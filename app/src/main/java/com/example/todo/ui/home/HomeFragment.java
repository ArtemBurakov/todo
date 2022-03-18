package com.example.todo.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.todo.R;
import com.example.todo.ui.task.ActiveTasksFragment;
import com.example.todo.ui.task.ArchiveTasksFragment;
import com.example.todo.ui.task.CompletedTasksFragment;
import com.example.todo.ui.task.FavouriteTasksFragment;
import com.google.android.material.tabs.TabLayout;

public class HomeFragment extends Fragment {

    TabLayout taskTabLayout;
    ViewPager taskViewPager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskViewPager = view.findViewById(R.id.task_view_pager);
        taskTabLayout = view.findViewById(R.id.task_tab_layout);

        ActiveTasksFragment activeTasksFragment = new ActiveTasksFragment();
        ArchiveTasksFragment archiveTasksFragment = new ArchiveTasksFragment();
        FavouriteTasksFragment favouriteTasksFragment = new FavouriteTasksFragment();
        CompletedTasksFragment completedTasksFragment = new CompletedTasksFragment();

        taskTabLayout.setupWithViewPager(taskViewPager);
        HomePagerAdapter homePagerAdapter = new HomePagerAdapter(getChildFragmentManager(), 0);
        homePagerAdapter.addFragment(activeTasksFragment, "Active");
        homePagerAdapter.addFragment(favouriteTasksFragment, "Favourite");
        homePagerAdapter.addFragment(completedTasksFragment, "Completed");
        homePagerAdapter.addFragment(archiveTasksFragment, "Archive");
        taskViewPager.setAdapter(homePagerAdapter);

        taskTabLayout.getTabAt(0).setIcon(R.drawable.ic_baseline_emoji_objects_24);
        taskTabLayout.getTabAt(1).setIcon(R.drawable.ic_baseline_favorite_24);
        taskTabLayout.getTabAt(2).setIcon(R.drawable.ic_baseline_assignment_turned_in_24);
        taskTabLayout.getTabAt(3).setIcon(R.drawable.ic_baseline_delete_24);
    }
}
