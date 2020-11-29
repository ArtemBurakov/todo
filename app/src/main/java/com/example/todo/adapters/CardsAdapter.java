package com.example.todo.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todo.R;
import com.example.todo.database.TasksDatabaseHelper;
import com.example.todo.models.Card;

import java.util.ArrayList;

public class CardsAdapter extends RecyclerView.Adapter<CardsAdapter.CardViewHolder> {

    private static final String TAG = "CardsAdapter";

    private ArrayList<Card> cardsArrayList;
    private OnCardListener mOnCardListener;
    private TasksDatabaseHelper tasksDatabaseHelper;

    public CardsAdapter(ArrayList<Card> cardsArrayList, Context context, OnCardListener onCardListener) {
        this.cardsArrayList = cardsArrayList;
        this.mOnCardListener = onCardListener;
        this.tasksDatabaseHelper = TasksDatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view, mOnCardListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, final int position) {
        Log.e(TAG, "OnBindViewHolder=== " + position);

        final Card card = cardsArrayList.get(position);

        holder.cardName.setText(card.getName());
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView cardName;
        OnCardListener onCardListener;

        public CardViewHolder(@NonNull View itemView, OnCardListener onCardListener) {
            super(itemView);

            cardName = itemView.findViewById(R.id.cardName);
            this.onCardListener = onCardListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onCardListener.onCardClick(getAdapterPosition());
        }
    }

    public interface OnCardListener{
        void onCardClick(int position);
    }

    @Override
    public int getItemCount() {
        return cardsArrayList.size();
    }
}