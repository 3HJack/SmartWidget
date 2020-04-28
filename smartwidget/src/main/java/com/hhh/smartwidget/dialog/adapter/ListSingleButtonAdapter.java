package com.hhh.smartwidget.dialog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.dialog.SmartDialog;

public class ListSingleButtonAdapter extends RecyclerView.Adapter<DialogViewHolder> {

  private final SmartDialog.Builder mBuilder;

  public ListSingleButtonAdapter(SmartDialog.Builder builder) {
    mBuilder = builder;
  }

  @NonNull
  @Override
  public DialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(mBuilder.getListItemLayout(),
        parent, false);
    DialogViewHolder viewHolder = new DialogViewHolder(view);
    view.setOnClickListener(v -> {
      v.findViewById(R.id.index).setSelected(true);
      int oldSelected = mBuilder.getSelectedIndex();
      mBuilder.setSelectedIndex(viewHolder.getAdapterPosition());
      mBuilder.getAdapter().notifyItemChanged(oldSelected);
    });
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull DialogViewHolder holder, int position) {
    ((TextView) holder.itemView.findViewById(R.id.item))
        .setText(mBuilder.getListItems().get(position));
    holder.itemView.findViewById(R.id.index).setSelected(position == mBuilder.getSelectedIndex());
  }

  @Override
  public int getItemCount() {
    return mBuilder.getListItems().size();
  }
}
