package com.hhh.smartwidget.dialog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.dialog.SmartDialog;

public class ListSimpleAdapter extends RecyclerView.Adapter<DialogViewHolder> {

  private final SmartDialog.Builder mBuilder;

  public ListSimpleAdapter(@NonNull SmartDialog.Builder builder) {
    mBuilder = builder;
  }

  @NonNull
  @Override
  public DialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(mBuilder.getListItemLayout(),
        parent, false);
    DialogViewHolder viewHolder = new DialogViewHolder(view);
    if (mBuilder.getListCallback() != null) {
      view.setOnClickListener(v -> {
        int position = viewHolder.getAdapterPosition();
        mBuilder.setSelectedIndex(position);
        mBuilder.getListCallback().onSelection(mBuilder.getDialog(), v, position);
      });
    }
    if (mBuilder.getListLongCallback() != null) {
      view.setOnLongClickListener(v -> {
        int position = viewHolder.getAdapterPosition();
        mBuilder.setSelectedIndex(position);
        mBuilder.getListLongCallback().onSelection(mBuilder.getDialog(), v, position);
        return true;
      });
    }
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull DialogViewHolder holder, int position) {
    ((TextView) holder.itemView.findViewById(R.id.item))
        .setText(mBuilder.getListItems().get(position));
    ((TextView) holder.itemView.findViewById(R.id.index)).setText((position + 1) + ".");
  }

  @Override
  public int getItemCount() {
    return mBuilder.getListItems().size();
  }
}
