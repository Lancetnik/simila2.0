package com.example.test;

import android.util.Log;

import org.jsoup.nodes.Document;

import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlMaker {

    static String make_url (Integer open_state, String[] Track) {
        String newUrl = null;
        if (open_state == 0)  newUrl = MakeYandexUrl(Track);
        if (open_state == 1)  newUrl = MakeVkUrl(Track); // not working as well
        if (open_state == 2)  newUrl = MakeYoutubeUrl(Track);
        if (open_state == 3)  newUrl = MakeShazamUrl(Track); // not working
        if (open_state == 4)  newUrl = MakeDeezerUrl(Track);
        if (open_state == 5)  newUrl = MakeAppleUrl(Track); // not working as well
        return newUrl;
    }

    // создание ссылки Гугла
    // научиться парсить поисковый запрос
    static  String MakeGoogleUrl(String[] Input){
        // Поисковая ссылка https://play.google.com/music/listen?u=0#/sr/Arctic+Monkeys+-+Red+Right+Hand
        // Прямая ссылка https://play.google.com/music/m/Trkthdqnc5gfwnjemyffociu7iq?t=Im_Ready_-_Sam_Smith
        String newUrl = "https://play.google.com/music/listen?u=0#/sr/"+Input[0].replace(" ","+")+"+-+"+Input[1].replace(" ","+");
        // нужно из поисковой сделать прямую
        Document html = Parse(newUrl);
        Log.w("Google", String.valueOf(html));
        return  newUrl;
    }

    // создание ссылки Apple
    // научиться парсить поисковый запрос
    static  String MakeAppleUrl(String[] Input){
        // Прямая ссылка https://music.apple.com/ru/album/music-lovers/1501555269?i=1501555274
        // Поисковая ссылка https://music.apple.com/ru/search?term=порнофильмы%20-%20%дядя%20володя
        // Прямая ссылка String newUrl = "https://music.apple.com/ru/album/" + Input[1].replace(" ","-") /* + научиться доставать ID трека*/ ;
        String newUrl = " https://music.apple.com/ru/search?term=" + Input[0].replace(" ","%20") + "%20-%20" +  Input[1].replace(" ","%20");
        // нужно из поисковой сделать прямую
        Document html = Parse(newUrl);
        Log.w("Apple", String.valueOf(html));
        return  newUrl;
    }

    // создание ссылки в ВК
    // научиться парсить поисковый запрос
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

        Document html = Parse(newURL);
        Log.w("VK", String.valueOf(html));

        return newURL;
    }

    // создание ссылки Shazam
    // сделать
    static  String MakeShazamUrl(String[] Input){
        // https://www.shazam.com/ru/track/376666093/я-так-соскучился
        String newUrl = "https://www.shazam.com/ru/track/" /*+ научиться доставать ID трека*/ + "/"+Input[1].replace(" ","-");
        Log.w("Shazam", newUrl);
        return  newUrl;
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

    // создание ссылки в Яндекс
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