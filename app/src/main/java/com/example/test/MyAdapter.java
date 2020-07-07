package com.example.test;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
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
                MainActivity.buffer_container.remove(position);
                MainActivity.save();
                notifyDataSetChanged();
                if (mDataset.isEmpty())
                    MainActivity.Clear_button.callOnClick();
            }
        });

        holder.SendButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(View v) {
                MainActivity.try_ad();

                String[] Track = new String[2];
                Track[0] = holder.BufferItemAutor.getText().toString();
                Track[1] = holder.BufferItemTrack.getText().toString();
                String newUrl = UrlMaker.make_url(MainActivity.models.get(MainActivity.send_state).flag, Track);

                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT, newUrl + " сгенерировано с помощью Simila");

                if (MainActivity.use_last_sender && !MainActivity.last_sender_app.equals(""))
                    share.setPackage(MainActivity.last_sender_app);
                else {
                    PendingIntent pi = PendingIntent.getBroadcast(v.getContext(), 0,
                            new Intent(v.getContext(), Receiver.class),
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    share = Intent.createChooser(share, null, pi.getIntentSender());
                }

                v.getContext().startActivity(share);
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
