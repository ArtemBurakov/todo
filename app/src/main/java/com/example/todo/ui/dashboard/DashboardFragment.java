package com.example.todo.ui.dashboard;

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

    private ActiveBoardsFragment activeBoardsFragment;
    private ArchiveBoardsFragment archiveBoardsFragment;
    private FavouriteBoardsFragment favouriteBoardsFragment;

    TabLayout tabLayout;
    ViewPager viewPager;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        activeBoardsFragment = new ActiveBoardsFragment();
        archiveBoardsFragment = new ArchiveBoardsFragment();
        favouriteBoardsFragment = new FavouriteBoardsFragment();

        tabLayout.setupWithViewPager(viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(), 0);
        viewPagerAdapter.addFragment(activeBoardsFragment, "Active");
        viewPagerAdapter.addFragment(favouriteBoardsFragment, "Favourite");
        viewPagerAdapter.addFragment(archiveBoardsFragment, "Archive");
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_baseline_bolt_24);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_baseline_favorite_24);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_baseline_delete_24);
    }
}
