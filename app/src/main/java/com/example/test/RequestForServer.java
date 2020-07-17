package com.example.test;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutionException;


public class RequestForServer {
    public static String[] make_song(String url) {
        String[] Track = new String[2];
        String newURL = "http://84.201.137.121/url_to_song?url=" + url;
        JSONObject response = Parse(newURL);

        int error = 0;
        try {
            error = response.getInt("error");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (error == 0) {
            String author = null;
            String name = null;
            try {
                author = response.getJSONObject("song").getString("author");
                name = response.getJSONObject("song").getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Track[0] = author;
            Track[1] = name;
        }
        else {
            Track[0] = "error";
            Track[1] = String.valueOf(error);
        }

        return Track;
    }

    public static String make_url(String[] Track, int target) {
        services service = MainActivity.serviceCards.get(target).flag;
        String newURL = "http://84.201.137.121/song_to_url?author="+ URLEncoder.encode(Track[0])
                +"/&song="+URLEncoder.encode(Track[1])
                +"/&target_service="+service;
        JSONObject response = Parse(newURL);

        int error = 0;
        try {
            error = response.getInt("error");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String new_url = null;
        if (error == 0) {
            try {
                new_url = response.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new_url.replace("\\", "");
        }
        else {
            new_url = String.valueOf(error);
        }

        return new_url;
    }

    public static String url_to_url (String url, int target) {
        services service = MainActivity.serviceCards.get(target).flag;
        String newURL = "http://84.201.137.121/convert_url?url="+url+"/&target_service="+service;
        JSONObject response = Parse(newURL);

        int error = 0;
        try {
            error = response.getInt("error");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String new_url = null;
        if (error == 0) {
            try {
                new_url = response.getString("url");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new_url.replace("\\", "");
        }
        else {
            new_url = String.valueOf(error);
        }

        return new_url;
    }

    // функция запроса, возвращает html
    static JSONObject Parse (String url) {
        String response = null;
        JSONObject jsonObject = null;
        DownloadTask downloadTask = new DownloadTask();
        try {
            response = downloadTask.execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // обработка ошибок
        try {
            jsonObject = new JSONObject(response);
        }catch (JSONException err){
            Log.d("Error", err.toString());
        }

        return jsonObject;
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
            html = "{ error:" + String.valueOf(responce)+"}";
        }
        Log.w("rez", html);
        return html;
    }
}

