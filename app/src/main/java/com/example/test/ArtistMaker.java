package com.example.test;

import android.util.Log;

import org.jsoup.nodes.Document;

import java.util.concurrent.ExecutionException;

public class ArtistMaker {
    // Функция: получаем исполнителя + название с помощью соответсвующих методов
    static String[] make_artist(String url) {
        String[] Track = null;
        Document html = null;

        if (url.contains("yandex.ru/album")) {
            html = Parse(url);
            Track = ArtistFromYandex(html);
        }
        else if (url.contains("deezer")) {
            if (url.contains("search")) Track = ArtistFromDeezerSearch(url);
            else {
                if (!url.contains("https"))
                    url = "https://www.deezer.com/ru/track/" + url.substring(url.length() - 9);
                html = Parse(url);
                Track = ArtistFromDeezer(html);
            }
        }
        else if (url.contains("apple")) {
            html = Parse(url);
            Track = ArtistFromApple(html);
        }
        else if (url.contains("yandex.ru/search")) {
            html = Parse(url);
            Track = ArtistFromYSearch(html);
        }
        else if (url.contains("google")) {
            html = Parse(url);
            Track = ArtistFromGoogle(html);
        }
        // шо там с ВК
        else if (url.contains("vk.com")) {
            html = Parse(url);
            Track = ArtistFromVK(html);
            Log.w("Sh", String.valueOf(html));
        }
        // шо там с шазамом
        else if (url.contains("shazam")) {
            html = Parse(url);
            Track = ArtistFromShazam(html);
            Log.w("Sh",  String.valueOf(html));
        }
        // шо там с youtube
        else if (url.contains("youtu")) {
            html = Parse(url);
            Track = ArtistFromYoutube(html);
            Log.w("Sh", String.valueOf(html));
        }

        return Track;
    }

    // получить из Ютуба
    // сделать
    static String[] ArtistFromYoutube (Document html){
        String[] Output = new String[2];
        Output[0] = null;
        Output[1] = null;
        return Output;
    }

    // получить из VK
    //не работает, непонятно откуда получить данные
    static String[] ArtistFromVK (Document html){
        String[] Output = new String[2];
        Output[0] = null;
        Output[1] = null;
        return Output;
    }

    // получить из Shazam
    // сделать
    static String[] ArtistFromShazam (Document html){
        String[] Output = new String[2];
        Output[0] = null;
        Output[1] = null;
        return Output;
    }

    // получить из Google
    static String[] ArtistFromGoogle (Document html){
        String[] data = html.getElementsByAttribute("itemprop").text().split("-");
        return data;
    }

    // получить из прямой ссылки Яндекса
    static String[] ArtistFromYandex (Document html){
        char[] data = html.text().toCharArray();
        String songName = "";
        String Artist = "";
        int i = 0;
        while (data[i] != '—') {
            songName += data[i];
            i++;
        }
        i = i + 2;
        while (data[i] != '.') {
            Artist += data[i];
            i++;
        }
        String[] Output = new String[2];
        Output[0] = Artist;
        Output[1] = songName;
        return Output;
    }

    // получение из поисковой ссылки Яндекса
    static String[] ArtistFromYSearch (Document html){
        char[] data = html.text().toCharArray();
        String songName = "";
        String Artist = "";
        int i = 0;
        while (data[i] != '-') {
            Artist += data[i];
            i++;
        }
        i = i + 2;
        while (data[i] != ':') {
            songName += data[i];
            i++;
        }
        String[] Output = new String[2];
        Output[0] = Artist;
        Output[1] = songName;
        return Output;
    }

    // получить из Дизера
    static String[] ArtistFromDeezer (Document html){
        char[] data = html.text().toCharArray();

        String songName = "";
        String Artist = "";
        int i = 0;
        while (data[i] != '—') {
            Artist += data[i];
            i++;
        }
        i = i + 2;
        while (data[i] != '—') {
            songName += data[i];
            i++;
        }
        String[] Output = new String[2];
        Output[0] = Artist;
        Output[1] = songName;
        return Output;
    }

    // получить из поиска Дизера
    static String[] ArtistFromDeezerSearch (String url){
        String[] Output = new String[2];
        String songName = "";
        String Artist = "";

        url = url.replaceAll("%20", " ");
        url = url.replace("https://www.deezer.com/search/", "");
        url = url.replace("/track", "");
        Artist = url.split("-", 2)[0];
        songName = url.split("-", 2)[1];

        Output[0] = Artist;
        Output[1] = songName;
        return Output;
    }

    // получить из Эпла
    static String[] ArtistFromApple (Document html){
        char[] data = html.text().toCharArray();
        String songName = "";
        String Artist = "";
        int i = 0;

        while (data[i] != '«') {
            i++;
        }
        i++;
        while (data[i] != '»') {
            Artist += data[i];
            i++;
        }
        i += 3;
        while (data[i] != ')') {
            songName += data[i];
            i++;
        }

        String[] Output = new String[2];
        Output[0] = Artist;
        Output[1] = songName;
        return Output;
    }

    // из поисковых ссылок Apple, Google, VK, разных ссылрк Youtub'a

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
