package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    // скроллеры
    ViewPager viewPager;
    ViewPager viewPager2;
    Adapter adapter;
    ArrayList<Model> models = new ArrayList<>();

    // выпадающее меню
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView nav_view;

    // для сохранения
    private static SharedPreferences sPref;
    private static Integer open_state = 2;
    public static Integer send_state = 2;

    // буфер
    CheckBox buffer;
    private static Boolean Is_Buffer = false; // используется ли буфер для сохранения?
    public static ArrayList <String> buffer_container = new ArrayList<>(); // храним треки в виде автор - песня
    Button Back_to_buffer; // кнопка возврата к буферу, если мы его свернули
    int buffer_response; // 2 - свернуть BufferActivity

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

        // ловим интент из буфера
        buffer_response = intent.getIntExtra("clear",3);

        // отправляем ссылку самому себе, чтобы открыть в другом приложении
        if (intent.getClipData()!=null && String.valueOf(intent.getClipData()).contains("Simila")){
            // получаем URL из ссылки регуляркой
            String str = String.valueOf(intent.getClipData());
            Pattern p = Pattern.compile("http.*");
            Matcher m = p.matcher(str);
            String url1 = "";
            while(m.find()){
                url1 = m.group().substring(0,(m.group().length()-35));
            }

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url1));
            startActivity(browserIntent);
            this.finish();
        }
        else {
            // передача ссылки
            if (intent.getClipData() != null && url == null) {
                // получаем URL из ссылки регуляркой
                String str = String.valueOf(intent.getClipData());
                Pattern p = Pattern.compile("http.*");
                Matcher m = p.matcher(str);
                String url1 = "";
                while (m.find()) {
                    url1 = m.group().substring(0, (m.group().length() - 3));
                }

                // получить данные о треке и генерируем новый юрл
                make_artist(url1);

                // если ссылка из шазама, то открываем ее в своем приложении по умолчанию
                if (url1.contains("shazam")) {
                    String newURL = make_url(open_state);
                    useUrl();
                }

                String newURL = make_url(send_state);

                // сохраняем в буфер, если он выбран
                if(Is_Buffer) {
                    buffer_container.add(Track[0]+" - "+Track[1]);
                    save();
                    this.finish();
                }

                // отправляем напрямую, если нет буфера
                else {
                    Intent intent2 = new Intent();
                    intent2.setAction(Intent.ACTION_SEND);
                    intent2.setType("text/plain");
                    intent2.putExtra(Intent.EXTRA_TEXT, newURL + " сгенерировано с помощью Simila");
                    startActivity(Intent.createChooser(intent2, "Share"));
                    this.finish();
                }
            }
            else {
                // открываем само приложение
                if (url == null) {
                    // открываем основное окно
                        setContentView(R.layout.activity_main);

                        // буфер чекбокс
                        buffer = findViewById(R.id.IS_BUFFER);
                        buffer.setChecked(Is_Buffer);
                        buffer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                Is_Buffer = isChecked;
                                save();
                            }
                        });

                        // выпадающее меню
                        toolbar = findViewById(R.id.toolbar);
                        drawerLayout = findViewById(R.id.drawer_layout);
                        nav_view = findViewById(R.id.nav_view);
                        setSupportActionBar(toolbar);

                        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                                this,
                                drawerLayout,
                                toolbar,
                                R.string.tutorial,
                                R.string.tutorial
                        );

                        drawerLayout.addDrawerListener(actionBarDrawerToggle);
                        actionBarDrawerToggle.syncState();
                        nav_view.setNavigationItemSelectedListener(this);

                        // заполняем список приложений для скроллеров
                        models.add(new Model(R.drawable.yandex));
                        models.add(new Model(R.drawable.vk));
                        models.add(new Model(R.drawable.youtube));
                        models.add(new Model(R.drawable.shazam));
                        models.add(new Model(R.drawable.deezer));
                        models.add(new Model(R.drawable.google));
                        models.add(new Model(R.drawable.apple));
                        models.add(new Model(R.drawable.youtube_music));

                        adapter = new Adapter(models, this);

                        viewPager = findViewById(R.id.viewPager);
                        viewPager2 = findViewById(R.id.viewPager2);
                        viewPager.setAdapter(adapter);
                        viewPager2.setAdapter(adapter);

                        // горизонтальные отступы между объектами скроллеров
                        viewPager.setPadding(200, 0, 200, 0);
                        viewPager2.setPadding(200, 0, 200, 0);

                        // устанавливаем выбор на значения на момент закрытия
                        viewPager.setCurrentItem(open_state + models.size() * 50, false);
                        point_it(open_state, 1);
                        viewPager2.setCurrentItem(send_state + models.size() * 50, false);
                        point_it(send_state, 2);

                        // создание скроллеров
                        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            }

                            @Override
                            public void onPageSelected(int position) {
                                open_state = position % models.size();
                                point_it(open_state, 1);
                                save();
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {
                            }
                        });
                        viewPager2.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            }

                            @Override
                            public void onPageSelected(int position) {
                                send_state = position % models.size();
                                point_it(send_state, 2);
                                save();
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {
                            }
                        });

                        // кнопка "вернуться к буферу"
                        Back_to_buffer = findViewById(R.id.buffer_button);
                        View.OnClickListener buffer_button_listener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent buffer = new Intent(MainActivity.this, BufferActivity.class);
                                Back_to_buffer.setVisibility(View.GONE);
                                startActivity(buffer);
                            }
                        };
                        Back_to_buffer.setOnClickListener(buffer_button_listener);

                        // открываем буфер
                        if ((!buffer_container.isEmpty()) && (buffer_response != 2))  {
                            Intent buffer = new Intent(MainActivity.this, BufferActivity.class);
                            startActivity(buffer);
                        }
                        // если буфер сворачивали
                        if (buffer_response == 2 && (!buffer_container.isEmpty())) Back_to_buffer.setVisibility(View.VISIBLE);
                }
                // открываем полученные ссылки
                else {
                    // получить данные о треке
                    make_artist(url);
                    // выполнить новую ссылку
                    useUrl();
                }
            }
        }
    }

    // Функция: открываем новый url способом по умолчанию
    void useUrl() {
        String newURL = make_url(open_state);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(newURL));
        startActivity(browserIntent);
        this.onStop();
    }

    // Функция: получаем данные о треке, обращаясь к классу ArtistMaker
    public void make_artist(String url){
        Track = ArtistMaker.make_artist(url);
    }

    // Функция: создаем требуемый url, обращаясь к классу UrlMaker
    public String make_url(int state) {
        return UrlMaker.make_url(state, Track);
    }

    // Подсветка нужной точки навигации
    public void point_it (int number, int pager) {
        number += 1;
        if (pager == 1) {
            ImageView first = findViewById(R.id.Img1Pager1);
            first.setImageResource(R.drawable.point);
            ImageView second = findViewById(R.id.Img2Pager1);
            second.setImageResource(R.drawable.point);
            ImageView third = findViewById(R.id.Img3Pager1);
            third.setImageResource(R.drawable.point);
            ImageView fourth = findViewById(R.id.Img4Pager1);
            fourth.setImageResource(R.drawable.point);
            ImageView fifth = findViewById(R.id.Img5Pager1);
            fifth.setImageResource(R.drawable.point);
            ImageView sixth = findViewById(R.id.Img6Pager1);
            sixth.setImageResource(R.drawable.point);
            ImageView seventh = findViewById(R.id.Img7Pager1);
            seventh.setImageResource(R.drawable.point);
            ImageView eight = findViewById(R.id.Img8Pager1);
            eight.setImageResource(R.drawable.point);
            switch (number) {
                case 1: {
                    first.setImageResource(R.drawable.active_point);
                    break;
                }
                case 2: {
                    second.setImageResource(R.drawable.active_point);
                    break;
                }
                case 3: {
                    third.setImageResource(R.drawable.active_point);
                    break;
                }
                case 4: {
                    fourth.setImageResource(R.drawable.active_point);
                    break;
                }
                case 5: {
                    fifth.setImageResource(R.drawable.active_point);
                    break;
                }
                case 6: {
                    sixth.setImageResource(R.drawable.active_point);
                    break;
                }
                case 7: {
                    seventh.setImageResource(R.drawable.active_point);
                    break;
                }
                case 8: {
                    eight.setImageResource(R.drawable.active_point);
                    break;
                }
            }
        }
        if (pager == 2) {
                ImageView first = findViewById(R.id.Img1Pager2);
                first.setImageResource(R.drawable.point);
                ImageView second = findViewById(R.id.Img2Pager2);
                second.setImageResource(R.drawable.point);
                ImageView third = findViewById(R.id.Img3Pager2);
                third.setImageResource(R.drawable.point);
                ImageView fourth = findViewById(R.id.Img4Pager2);
                fourth.setImageResource(R.drawable.point);
                ImageView fifth = findViewById(R.id.Img5Pager2);
                fifth.setImageResource(R.drawable.point);
                ImageView sixth = findViewById(R.id.Img6Pager2);
                sixth.setImageResource(R.drawable.point);
                ImageView seventh = findViewById(R.id.Img7Pager2);
                seventh.setImageResource(R.drawable.point);
                ImageView eight = findViewById(R.id.Img8Pager2);
                eight.setImageResource(R.drawable.point);
                switch (number) {
                    case 1: {
                        first.setImageResource(R.drawable.active_point);
                        break;
                    }
                    case 2: {
                        second.setImageResource(R.drawable.active_point);
                        break;
                    }
                    case 3: {
                        third.setImageResource(R.drawable.active_point);
                        break;
                    }
                    case 4: {
                        fourth.setImageResource(R.drawable.active_point);
                        break;
                    }
                    case 5: {
                        fifth.setImageResource(R.drawable.active_point);
                        break;
                    }
                    case 6: {
                        sixth.setImageResource(R.drawable.active_point);
                        break;
                    }
                    case 7: {
                        seventh.setImageResource(R.drawable.active_point);
                        break;
                    }
                    case 8: {
                        eight.setImageResource(R.drawable.active_point);
                        break;
                    }
                }
        }
    }

    // загрузка сохраненного выбора в локальный файл
    public void load() {
        buffer_container.clear();

        open_state = sPref.getInt("open_state", 2);
        send_state = sPref.getInt("send_state", 2);

        // загружаем буфер
        String buf = "";
        if (sPref.contains("buffer_container")) {
            String str = sPref.getString("buffer_container","");
            for (int i = 0; i < str.length(); i++ ) {
                if (str.charAt(i) == '|') {
                    buffer_container.add(buf);
                    buf = "";
                    if ( i != str.length()-1) i+= 1;
                }
                buf = buf + str.charAt(i);
            }
        }

        // загружаем значение чекбокса
        if(sPref.contains("buf_state")) {
            Is_Buffer = sPref.getBoolean("buf_state", false);
        }
    }

    // сохранение выбора для дальнейшего открытия через него по умолчанию
    public static void save() {
        SharedPreferences.Editor ed = sPref.edit();

        // при закрытии сохраняем значение буфера
        if(Is_Buffer) ed.putBoolean("buf_state", true);
        else ed.putBoolean("buf_state", false);

        String bufferset = "";
        for(int i = 0; i < buffer_container.size(); i++) {
            bufferset += buffer_container.get(i);
            bufferset += '|';
        }

        ed.putString("buffer_container", bufferset);
        ed.putInt("open_state", open_state);
        ed.putInt("send_state", send_state);
        ed.apply();
    }

    @Override
    // если мы передали ссылку в буфер, когда приложение было свернуто
    protected void onRestart() {
        super.onRestart();
        load();
        // открываем буфер
        if ((!buffer_container.isEmpty()))  {
            Intent buffer = new Intent(MainActivity.this, BufferActivity.class);
            startActivity(buffer);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}
