package com.example.todo.ui.home;

import static com.example.todo.MainActivity.createTaskToolbar;
import static com.example.todo.MainActivity.floatingActionButton;
import static com.example.todo.MainActivity.mainToolbar;
import static com.example.todo.MainActivity.selectedBoardToolbar;
import static com.example.todo.MainActivity.selectedTaskToolbar;

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

    private TabLayout taskTabLayout;
    private ViewPager taskViewPager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        taskViewPager = view.findViewById(R.id.task_view_pager);
        taskTabLayout = view.findViewById(R.id.task_tab_layout);
        selectedBoardToolbar.setVisibility(View.GONE);
        selectedTaskToolbar.setVisibility(View.GONE);
        createTaskToolbar.setVisibility(View.GONE);
        mainToolbar.setVisibility(View.VISIBLE);
        mainToolbar.setTitle("Tasks");

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
        taskViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        floatingActionButton.show();
                        return;
                    case 1:
                        floatingActionButton.hide();
                        return;
                    case 2:
                        floatingActionButton.hide();
                    case 3:
                        floatingActionButton.hide();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
