package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


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
                BufferActivity.this.finish();
            }
        };
        Clear_button.setOnClickListener(clear_button_listener);

        // кнопка отправки
        Button Send_button = findViewById(R.id.Send_button);
        View.OnClickListener send_button_listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent();
                intent2.setAction(Intent.ACTION_SEND);
                intent2.setType("text/plain");

                String text_to_send = "";
                int count = 1;
                for (String i : MainActivity.buffer_container) {
                    text_to_send += String.valueOf(count) +") " + i + "\n";
                    count += 1;
                }

                intent2.putExtra(Intent.EXTRA_TEXT, text_to_send + " сгенерировано с помощью Simila");
                startActivity(Intent.createChooser(intent2, "Share"));

                MainActivity.buffer_container.clear();
                BufferActivity.this.finish();
            }
        };
        Send_button.setOnClickListener(send_button_listener);

        // список
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list_view);
    }
}
