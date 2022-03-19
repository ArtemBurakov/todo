package com.example.todo.ui.dashboard;

import static com.example.todo.MainActivity.createTaskToolbar;
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
import com.example.todo.ui.board.ActiveBoardsFragment;
import com.example.todo.ui.board.ArchiveBoardsFragment;
import com.example.todo.ui.board.FavouriteBoardsFragment;
import com.google.android.material.tabs.TabLayout;

public class DashboardFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);
        selectedBoardToolbar.setVisibility(View.GONE);
        selectedTaskToolbar.setVisibility(View.GONE);
        createTaskToolbar.setVisibility(View.GONE);
        mainToolbar.setVisibility(View.VISIBLE);
        mainToolbar.setTitle("Boards");

        ActiveBoardsFragment activeBoardsFragment = new ActiveBoardsFragment();
        ArchiveBoardsFragment archiveBoardsFragment = new ArchiveBoardsFragment();
        FavouriteBoardsFragment favouriteBoardsFragment = new FavouriteBoardsFragment();

        tabLayout.setupWithViewPager(viewPager);
        DashboardPagerAdapter dashboardPagerAdapter = new DashboardPagerAdapter(getChildFragmentManager(), 0);
        dashboardPagerAdapter.addFragment(activeBoardsFragment, "Active");
        dashboardPagerAdapter.addFragment(favouriteBoardsFragment, "Favourite");
        dashboardPagerAdapter.addFragment(archiveBoardsFragment, "Archive");
        viewPager.setAdapter(dashboardPagerAdapter);

//        tabLayout.getTabAt(0).setIcon(R.drawable.ic_baseline_emoji_objects_24);
//        tabLayout.getTabAt(1).setIcon(R.drawable.ic_baseline_favorite_24);
//        tabLayout.getTabAt(2).setIcon(R.drawable.ic_baseline_delete_24);
    }
}
