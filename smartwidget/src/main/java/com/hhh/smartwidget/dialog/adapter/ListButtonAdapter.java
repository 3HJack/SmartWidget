package com.hhh.smartwidget.dialog.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hhh.smartwidget.R;
import com.hhh.smartwidget.dialog.SmartDialog;
import com.hhh.smartwidget.popup.PopupInterface;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ListButtonAdapter extends RecyclerView.Adapter<DialogViewHolder> {

  private final SmartDialog.Builder mBuilder;

  public ListButtonAdapter(SmartDialog.Builder builder) {
    mBuilder = builder;
  }

  @NonNull
  @Override
  public DialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(mBuilder.getListItemLayout(),
        parent, false);
    DialogViewHolder viewHolder = new DialogViewHolder(view);
    view.setOnClickListener(v -> {
      int position = viewHolder.getAdapterPosition();
      SmartDialog dialog = mBuilder.getDialog();
      mBuilder.setSelectedIndex(position);
      mBuilder.getListCallback().onSelection(dialog, v, position);
      dialog.dismiss(PopupInterface.CLOSE_TYPE_POSITIVE);
    });
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull DialogViewHolder holder, int position) {
    TextView itemTextView = holder.itemView.findViewById(R.id.item);
    itemTextView.setText(mBuilder.getListItems().get(position));
    itemTextView.setSelected(position == mBuilder.getSelectedIndex());
  }

  @Override
  public int getItemCount() {
    return mBuilder.getListItems().size();
  }
}
