package com.example.test;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestForServer {
    public static String[] make_song(String url) {
        String[] Track = new String[2];
        String newURL = "http://84.201.137.121/url_to_song?url=" + url;
        String response = Parse(newURL);

        Pattern author_regex = Pattern.compile("\"author\":\"(.*?)\"");
        Matcher m = author_regex.matcher(response);
        String author = "not found";
        while(m.find())
            author = m.group(1);

        Pattern name_regex = Pattern.compile("\"name\":\"(.*?)\"");
        Matcher m2 = name_regex.matcher(response);
        String name = "not found";
        while(m2.find())
            name = m2.group(1);

        Track[0] = author;
        Track[1] = name;

        return Track;
    }

    public static String make_url(String[] Track, services target) {
        String newURL = "http://84.201.137.121/song_to_url?author="+ URLEncoder.encode(Track[0])
                +"/&song="+URLEncoder.encode(Track[1])
                +"/&target_service="+target;
        String response = Parse(newURL);

        Pattern url_regex = Pattern.compile("\"url\":\"(.*?)\"");
        Matcher m = url_regex.matcher(response);
        String res_url = "not found";
        while(m.find())
            res_url = m.group(1);

        return res_url;
    }

    // функция запроса, возвращает html
    static String Parse (String url) {
        String response = null;
        DownloadTask downloadTask = new DownloadTask();
        try {
            // спарсить
            response = downloadTask.execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    };
}

// класс для запросов в инет
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

