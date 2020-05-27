package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    // скроллеры
    ViewPager viewPager;
    ViewPager viewPager2;
    Adapter adapter;
    List<Model> models = new ArrayList<>();
    Button btn1;

    // для сохранения
    private SharedPreferences sPref;
    Integer open_state = 2;
    Integer send_state = 2;
    ArrayDeque<String> history = new ArrayDeque<String>();
    // определяет размер истории, менять тут
    Integer history_size = 20;

    String[] Track;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // загрузка
        sPref = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        load();

        // получаем интент, вызвавший нас
        final Intent intent = getIntent();
        String url = intent.getDataString();

        // передача ссылки
        if (intent.getClipData()!=null && url==null) {
            // получаем URL из ссылки регуляркой
            String str = String.valueOf(intent.getClipData());
            Pattern p = Pattern.compile("http.*");
            Matcher m = p.matcher(str);
            String url1 = "";
            while(m.find()){
                url1 = m.group().substring(0,(m.group().length()-3));
            }

            // получить данные о треке и генерируем новый юрл
            make_artist(url1);
            String newURL = make_url(send_state);
            add_in_history("отправлено",Track[0],Track[1]);

            Intent intent2 = new Intent();
            intent2.setAction(Intent.ACTION_SEND);
            intent2.setType("text/plain");
            intent2.putExtra(Intent.EXTRA_TEXT, newURL + " сгенерировано с помощью Simila");
            startActivity(Intent.createChooser(intent2, "Share"));
            this.finish();
        }

        // открываем само приложение
        if (url == null) {
            setContentView(R.layout.activity_main);
            btn1 = (Button) findViewById(R.id.btnDefault);
            RelativeLayout relativeLayout = findViewById(R.id.layout);

//
            models.add(new Model(R.drawable.yandex));
            models.add(new Model(R.drawable.vk));
            models.add(new Model(R.drawable.youtube));
            models.add(new Model(R.drawable.shazam));
            models.add(new Model(R.drawable.deezer));
            models.add(new Model(R.drawable.google));
            models.add(new Model(R.drawable.apple));


            adapter = new Adapter(models, this);

            viewPager = findViewById(R.id.viewPager);
            viewPager2 = findViewById(R.id.viewPager2);
            viewPager.setAdapter(adapter);
            viewPager2.setAdapter(adapter);

            viewPager.setPadding(200, 0, 200, 0);
            viewPager2.setPadding(200, 0, 200, 0);

            // устанавливаем выбор на значения на момент закрытия
            viewPager.setCurrentItem(open_state);
            viewPager2.setCurrentItem(send_state);

            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (position < (adapter.getCount() - 1)) {

                    }
                }

                @Override
                public void onPageSelected(int position) {
                    int j = position % models.size();
                    open_state = j;
                    final int childCount = viewPager.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        final View child = viewPager.getChildAt(i);
                        child.setPadding(0,0,0,0);
                        final ViewPager.LayoutParams lp = (ViewPager.LayoutParams) child.getLayoutParams();
                        int position1 = 0;
                        try {
                            Field f = lp.getClass().getDeclaredField("position");
                            f.setAccessible(true);
                            position1 = f.getInt(lp); //IllegalAccessException
                        } catch (NoSuchFieldException | IllegalAccessException ex) {ex.printStackTrace();}
                        if (position1 == viewPager.getCurrentItem()  ) {
                            child.setPadding(0,100,0,0);
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            viewPager2.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (position < (adapter.getCount() - 1)) {

                    }
                }

                @Override
                public void onPageSelected(int position) {
                    int j = position % models.size();
                    send_state = j;
                    final int childCount = viewPager2.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        final View child = viewPager2.getChildAt(i);
                        child.setPadding(0,0,0,0);
                        final ViewPager.LayoutParams lp = (ViewPager.LayoutParams) child.getLayoutParams();
                        int position1 = 0;
                        try {
                            Field f = lp.getClass().getDeclaredField("position");
                            f.setAccessible(true);
                            position1 = f.getInt(lp); //IllegalAccessException
                        } catch (NoSuchFieldException | IllegalAccessException ex) {ex.printStackTrace();}
                        if (position1 == viewPager2.getCurrentItem()  ) {
                            child.setPadding(0,0,0,100);
                        }
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
        }

        // открываем полученные ссылки
        else {
            // получить данные о треке
            make_artist(url);
            add_in_history("получено",Track[0],Track[1]);
            // выполнить новую ссылку
            useUrl();
        }

        // сохранение истории и выбранных параметров
        save();
    }

    // Функция: открываем новый url способом по умолчанию
    void useUrl() {
        String newURL = make_url(open_state);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
        startActivity(browserIntent);
        this.finish();
    }

    // Функция: получаем данные о треке, обращаясь к классу ArtistMaker
    public void make_artist(String url){
        Track = ArtistMaker.make_artist(url);
    }

    // Функция: создаем требуемый url, обращаясь к классу UrlMaker
    public String make_url(int state) {
        String newURL = UrlMaker.make_url(state, Track);
        return newURL;
    }

    // загрузка сохраненного выбора в локальный файл
    public void load() {
        open_state = sPref.getInt("open_state", 2);
        send_state = sPref.getInt("send_state", 2);
        if (sPref.contains("str")) {
            String str = sPref.getString("str","");
            String buf = "";
            for (int i = 0; i < str.length(); i++ ) {
                buf = buf + str.charAt(i);
                if (str.charAt(i) == '\n') {
                    history.add(buf);
                    buf = "";
                }
            }
        }
    }

    // сохранение выбора для дальнейшего открытия через него по умолчанию
    public void save() {
        SharedPreferences.Editor ed = sPref.edit();

        String historyset = new String();
        for(String pq : history) {
            historyset += pq;
        }
//
        Log.w("Send", String.valueOf(send_state));
        ed.putString("str", historyset);
        ed.putInt("open_state", open_state);
        ed.putInt("send_state", send_state);
        ed.commit();
    }

    public void  add_in_history (String what_is_it, String artist_name, String song_name) {
        history.addLast(what_is_it + " " + artist_name + " " + song_name + "\n");
        if (history.size() > history_size)
            history.removeFirst();
    }

    @Override
    // при остановке приложения сохраняем выбор
    protected void onStop() {
        super.onStop();
        save();
    }
}
