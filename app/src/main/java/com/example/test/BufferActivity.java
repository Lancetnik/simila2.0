package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class BufferActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ExtendedFloatingActionButton Clear_button;
    private ExtendedFloatingActionButton Cut_button;
    private ExtendedFloatingActionButton Send_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buffer_layout);

        // список
        recyclerView = (RecyclerView) findViewById(R.id.list_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        mAdapter = new MyAdapter(MainActivity.buffer_container);
        recyclerView.setAdapter(mAdapter);

        // кнопка "очистить"
        Clear_button = findViewById(R.id.Clear_button);
        View.OnClickListener clear_button_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.buffer_container.clear();
                MainActivity.save();
                BufferActivity.this.finish();
            }
        };
        Clear_button.setOnClickListener(clear_button_listener);

        // кнопка "свернуть"
        Cut_button = findViewById(R.id.Cut_button);
        View.OnClickListener cut_button_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BufferActivity.this, MainActivity.class).putExtra("clear",2));
            }
        };
        Cut_button.setOnClickListener(cut_button_listener);

        // кнопка отправки
        Send_button = findViewById(R.id.Send_button);
        View.OnClickListener send_button_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_to_send = "";
                int count = 1;
                assert MainActivity.buffer_container != null;
                for (String i : MainActivity.buffer_container) {
                    text_to_send += String.valueOf(count) +") " + UrlMaker.make_url(MainActivity.send_state,i.split(" - ")) + "\n";
                    count += 1;
                }

                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_SEND);
                intent2.setType("text/plain");
                intent2.putExtra(Intent.EXTRA_TEXT, text_to_send + " сгенерировано с помощью Simila");
                startActivity(Intent.createChooser(intent2, "Share"));
            }
        };
        Send_button.setOnClickListener(send_button_listener);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        recyclerView = (RecyclerView) findViewById(R.id.list_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        mAdapter = new MyAdapter(MainActivity.buffer_container);
        recyclerView.setAdapter(mAdapter);
    }
}

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
                String newUrl = UrlMaker.make_url(MainActivity.send_state,Track);

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


