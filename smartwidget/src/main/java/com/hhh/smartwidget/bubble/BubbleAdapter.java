package com.hhh.smartwidget.bubble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hhh.smartwidget.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BubbleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private final Bubble.Builder mBuilder;

  public BubbleAdapter(Bubble.Builder builder) {
    mBuilder = builder;
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(mBuilder.getListItemLayout(),
        parent, false);
    RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(view) {};
    if (mBuilder.getListCallback() != null) {
      view.setOnClickListener(v -> {
        int position = viewHolder.getAdapterPosition();
        mBuilder.getListCallback().onSelection(mBuilder.getBubble(), v, position);
      });
    }
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    BubbleInterface.BubbleItem bubbleItem = mBuilder.getBubbleItems().get(position);
    ((TextView) holder.itemView.findViewById(R.id.item)).setText(bubbleItem.mText);
    ((ImageView) holder.itemView.findViewById(R.id.icon)).setImageDrawable(bubbleItem.mIcon);
  }

  @Override
  public int getItemCount() {
    return mBuilder.getBubbleItems().size();
  }
}
