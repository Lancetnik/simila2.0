package com.example.test;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private ArrayList<String> mDataset;

    MyAdapter(ArrayList<String> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.buffer_item;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForListItem, parent, false);

        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.BufferItemTrack.setText(mDataset.get(position).split(" - ")[1]);
        holder.BufferItemAutor.setText(mDataset.get(position).split(" - ")[0]);

        holder.DelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mDataset.remove(position);
                if (mDataset.isEmpty()) {
                    MainActivity.buf_title.setText("Buffer is empty");
                    MainActivity.bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    MainActivity.chip_buffer.setVisibility(View.VISIBLE);
                    MainActivity.Send_button.setVisibility(View.GONE);
                    MainActivity.Clear_button.setVisibility(View.GONE);
                }
                MainActivity.save();
                notifyItemRemoved(position);
                notifyDataSetChanged();
            }
        });
        holder.SendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] Track = new String[2];
                Track[0] = holder.BufferItemAutor.getText().toString();
                Track[1] = holder.BufferItemTrack.getText().toString();
                String newUrl = UrlMaker.make_url(MainActivity.send_state, Track);

                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_SEND);
                intent2.setType("text/plain");
                intent2.putExtra(Intent.EXTRA_TEXT, newUrl + " сгенерировано с помощью Simila");
                v.getContext().startActivity(Intent.createChooser(intent2, "Share"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView BufferItemTrack;
        TextView BufferItemAutor;
        FloatingActionButton DelButton;
        FloatingActionButton SendButton;

        MyViewHolder(View itemView) {
            super(itemView);
            BufferItemTrack = itemView.findViewById(R.id.buffer_item_track);
            BufferItemAutor = itemView.findViewById(R.id.buffer_item_autor);
            DelButton = itemView.findViewById(R.id.del_button);
            SendButton = itemView.findViewById(R.id.send_button);
        }
    }
}
