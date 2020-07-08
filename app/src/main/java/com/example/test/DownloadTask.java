package com.example.test;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class DownloadTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
        String html = null;
        int responce = 0;
        try {
            String url1 = params[0];

            URL url = new URL(url1);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            responce = con.getResponseCode();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            html = in.readLine();
            in.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.w("rez", String.valueOf(e));
        }
        return html;
    }
}