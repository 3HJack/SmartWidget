package com.hhh.smartwidget.dialog.adapter;

import java.util.Collections;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.dialog.SmartDialog;

public class ListMultiAdapter extends RecyclerView.Adapter<DialogViewHolder> {

  private final SmartDialog.Builder mBuilder;

  public ListMultiAdapter(@NonNull SmartDialog.Builder builder) {
    mBuilder = builder;
  }

  @NonNull
  @Override
  public DialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(mBuilder.getListItemLayout(),
        parent, false);
    DialogViewHolder viewHolder = new DialogViewHolder(view);
    view.setOnClickListener(v -> {
      View indexView = v.findViewById(R.id.index);
      int position = viewHolder.getAdapterPosition();
      List<Integer> selectedIndices = mBuilder.getSelectedIndices();
      boolean shouldBeSelected = !selectedIndices.contains(position);
      if (shouldBeSelected) {
        selectedIndices.add(position);
        indexView.setSelected(true);
      } else {
        selectedIndices.remove(Integer.valueOf(position));
        indexView.setSelected(false);
      }
      if (mBuilder.isAlwaysCallMultiChoiceCallback()) {
        Collections.sort(selectedIndices);
        mBuilder.getListCallbackMultiChoice().onSelection(mBuilder.getDialog(), selectedIndices);
      }
    });
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull DialogViewHolder holder, int position) {
    ((TextView) holder.itemView.findViewById(R.id.item))
        .setText(mBuilder.getListItems().get(position));
    holder.itemView.findViewById(R.id.index)
        .setSelected(mBuilder.getSelectedIndices().contains(position));
  }

  @Override
  public int getItemCount() {
    return mBuilder.getListItems().size();
  }
}
