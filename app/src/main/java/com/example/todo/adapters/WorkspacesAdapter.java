package com.example.todo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.diffUtilCallback.WorkspaceDiffUtilCallback;
import com.example.todo.R;
import com.example.todo.models.Workspace;

import java.util.ArrayList;

public class WorkspacesAdapter extends RecyclerView.Adapter<WorkspacesAdapter.WorkspaceViewHolder> {
    private ArrayList<Workspace> workspacesArrayList;
    private final OnWorkspaceListener mOnWorkspaceListener;

    public WorkspacesAdapter(ArrayList<Workspace> workspacesArrayList, Context context, OnWorkspaceListener OnWorkspaceListener) {
        this.workspacesArrayList = workspacesArrayList;
        this.mOnWorkspaceListener = OnWorkspaceListener;
    }

    @NonNull
    @Override
    public WorkspaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workspace, parent, false);
        return new WorkspaceViewHolder(view, mOnWorkspaceListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkspaceViewHolder holder, final int position) {
        final Workspace workspace = workspacesArrayList.get(position);
        holder.workspaceName.setText(workspace.getName());
    }

    public static class WorkspaceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView workspaceName;
        OnWorkspaceListener OnWorkspaceListener;

        public WorkspaceViewHolder(@NonNull View itemView, OnWorkspaceListener OnWorkspaceListener) {
            super(itemView);
            workspaceName = itemView.findViewById(R.id.workspaceName);
            this.OnWorkspaceListener = OnWorkspaceListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            OnWorkspaceListener.onWorkspaceClick(getAdapterPosition());
        }
    }

    public interface OnWorkspaceListener{
        void onWorkspaceClick(int position);
    }

    @Override
    public int getItemCount() {
        return workspacesArrayList.size();
    }

    public void updateBoardsArrayList(final ArrayList<Workspace> newWorkspacesArrayList) {
        // Attach differences to adapter
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                new WorkspaceDiffUtilCallback(newWorkspacesArrayList, workspacesArrayList));
        workspacesArrayList = newWorkspacesArrayList;
        result.dispatchUpdatesTo(WorkspacesAdapter.this);
    }
}
