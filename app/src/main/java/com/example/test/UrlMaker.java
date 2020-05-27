package com.example.test;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlMaker {

    static String make_url (Integer open_state, String[] Track) {
        String newUrl = null;
        if (open_state == 0)  newUrl = MakeYandexUrl(Track);
        if (open_state == 1)  newUrl = MakeVkUrl(Track);
        if (open_state == 2)  newUrl = MakeYoutubeUrl(Track);
        //if (open_state == 3)  newUrl = MakeShazamUrl(Track);
        if (open_state == 4)  newUrl = MakeDeezerUrl(Track);
        //if (open_state == 5)  newUrl = MakeGoogleUrl(Track);
        return newUrl;
    }

    // создание ссылки Дизера
    static String MakeDeezerUrl(String[] Input) {
        char[] songName = Input[1].toCharArray();
        char[] Artist = Input[0].toCharArray();
        String ArtistOut = "";
        String songNameOut = "";

        for (int i = 0; i < songName.length; i++) {
            if (songName[i] != ' ') songNameOut += songName[i];
            else songNameOut += "%20";
        }

        for (int i = 0; i < Artist.length; i++) {
            if (Artist[i] != ' ') ArtistOut += Artist[i];
            else ArtistOut += "%20";
        }

        String newURL = "https://www.deezer.com/search/" + ArtistOut + "%20-%20" + songNameOut + "/track";

        Document html = Parse(newURL);
        Pattern p =Pattern.compile("SNG_ID\":\"[^\"\\r\\n]*\"");
        String pars_idtreka = "";
        Matcher m =p.matcher(html.toString());
        while (m.find()){
            pars_idtreka = m.group();
        }
        String idtreka = pars_idtreka.substring(9, (pars_idtreka.length()-1));
        String finalURL = "https://www.deezer.com/track/" + idtreka;
        return finalURL;
    }

    // создание ссылки в ВК
    static String MakeVkUrl(String[] Input) {
        char[] songName = Input[1].toCharArray();
        char[] Artist = Input[0].toCharArray();
        String ArtistOut = "";
        String songNameOut = "";

        for (int i = 0; i < songName.length; i++) {
            if (songName[i] != ' ') songNameOut += songName[i];
            else songNameOut += "%20";
        }

        for (int i = 0; i < Artist.length; i++) {
            if (Artist[i] != ' ') ArtistOut += Artist[i];
            else ArtistOut += "%20";
        }

        String newURL = "https://vk.com/audio?q=" + ArtistOut + "%20-%20" + songNameOut;

        return newURL;
    }

    // создание ссылки в Ютуб
    static String MakeYoutubeUrl(String[] Input) {
        char[] songName = Input[1].toCharArray();
        char[] Artist = Input[0].toCharArray();
        String ArtistOut = "";
        String songNameOut = "";

        for (int i = 0; i < songName.length; i++) {
            if (songName[i] != ' ') songNameOut += songName[i];
            else songNameOut += "+";
        }

        for (int i = 0; i < Artist.length; i++) {
            if (Artist[i] != ' ') ArtistOut += Artist[i];
            else ArtistOut += "+";
        }

        String newURL = "https://www.youtube.com/results?search_query=" + ArtistOut + "+" + songNameOut;
        return newURL;
    }

    // создание ссылки в Яндекс через 2-ой парсинг.
    // Получаем название и артиста, формируем поисковую ссылку Яндекса
    // Затем парсим поисковую ссылку Яндекса и получаем оттуда их внутренний id трека
    static String MakeYandexUrl(String[] Input) {
            char[] songName = Input[1].toCharArray();
            char[] Artist = Input[0].toCharArray();
            String ArtistOut = "";
            String songNameOut = "";

            for (int i = 0; i < songName.length; i++) {
                if (songName[i] != ' ') songNameOut += songName[i];
                else songNameOut += "%20";
            }

            for (int i = 0; i < Artist.length; i++) {
                if (Artist[i] != ' ') ArtistOut += Artist[i];
                else ArtistOut += "%20";
            }

            // формируем поисковую ссылку
            String newURL = "https://music.yandex.ru/search?text=" + ArtistOut + "%20-%20" + songNameOut;

            // парсим ID трека
            Document html = Parse(newURL);
            String str = html.getElementsByAttribute("href").toString();
            int pos = str.indexOf("/album/");
            String Output = "";
            while (str.toCharArray()[pos] != '\"') {
                Output += str.toCharArray()[pos];
                pos++;
            }
            String out = "https://music.yandex.ru" + Output;

            return out;
    }

    static Document Parse (String url) {
        Document html = null;
        DownloadTask downloadTask = new DownloadTask();
        try {
            // спарсить
            html = downloadTask.execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.w("Parse", String.valueOf(html));
        return html;
    };
}