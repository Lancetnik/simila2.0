package com.example.test;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;


public class BufferActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buffer_layout);

        // кнопка "очистить"
        Button Clear_button = findViewById(R.id.Clear_button);
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
        Button Cut_button = findViewById(R.id.Cut_button);
        View.OnClickListener cut_button_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(BufferActivity.this, MainActivity.class).putExtra("clear",2));
            }
        };
        Cut_button.setOnClickListener(cut_button_listener);

        // кнопка отправки
        Button Send_button = findViewById(R.id.Send_button);
        View.OnClickListener send_button_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text_to_send = "";
                int count = 1;
                assert MainActivity.buffer_container != null;
                for (String i : MainActivity.buffer_container) {
                    text_to_send += String.valueOf(count) +") " + i + "\n";
                    count += 1;
                }
                MainActivity.buffer_container.clear();
                MainActivity.save();

                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_SEND);
                intent2.setType("text/plain");
                intent2.putExtra(Intent.EXTRA_TEXT, text_to_send + " сгенерировано с помощью Simila");
                startActivity(Intent.createChooser(intent2, "Share"));

                BufferActivity.this.finish();
            }
        };
        Send_button.setOnClickListener(send_button_listener);

        // список
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_view);
    }
}



