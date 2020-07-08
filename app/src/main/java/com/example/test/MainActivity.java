package com.example.test;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // реклама
    public static InterstitialAd mInterstitialAd;
    public static int ad_counter;
    public static boolean is_bought = false; // куплена ли фулл версия

    // буфер снизу
    private ConstraintLayout buffer_sheet;
    public static BottomSheetBehavior bottom_sheet_behavior;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    public static FloatingActionButton Clear_button;
    public static FloatingActionButton Send_button;
    public static Chip chip_buffer;
    public static TextView buf_title;
    private static Boolean Is_Buffer = false; // используется ли буфер для сохранения?
    public static ArrayList <String> buffer_container = new ArrayList<>();// храним треки в виде автор - песня

    // скроллеры
    private ViewPager viewPager;
    private ViewPager viewPager2;
    private AdapterForPagers adapterForPagers;
    public static ArrayList<ServiceCard> serviceCards = new ArrayList<>();
    private LinearLayout first_pager_nav;
    private LinearLayout second_pager_nav;

    // выпадающее меню
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView nav_view;

    private SwitchMaterial buffer_switcher;
    private SwitchMaterial shazam_switcher;
    private SwitchMaterial sender_switcher;
    private FloatingActionButton buffer_help_button;
    private FloatingActionButton shazam_help_button;
    private FloatingActionButton sender_help_button;

    // для сохранения
    private static SharedPreferences sPref;
    private static Integer open_state = 2; // значение скроллера для открытия
    public static Integer send_state = 2; // значение скроллера для закрытия
    private static boolean is_first = true; // первый ли раз открыта программа
    private static boolean special_shazam = false; // отправлять ли ссылки из шазама сразу на открытие в своем сервисе
    public static boolean use_last_sender = false;
    public static String last_sender_app; // последний сервис, куда отправляли

    String[] Track;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // загрузка
        sPref = getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        load();

        // получаем интент, вызвавший нас
        final Intent intent = getIntent();
        String url = intent.getDataString();

        // реклама
        {
            MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
            mInterstitialAd = new InterstitialAd(this);
            mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }
            });
        }

        // отправляем ссылку самому себе, чтобы открыть в другом приложении
        if (intent.getClipData()!=null && String.valueOf(intent.getClipData()).contains("Simila")){
            try_ad();
            // получаем URL из ссылки регуляркой
            String str = String.valueOf(intent.getClipData());
            Pattern p = Pattern.compile("http.*");
            Matcher m = p.matcher(str);
            String url1 = "";
            while(m.find())
                url1 = m.group().substring(0,(m.group().length()-35));

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url1));
            startActivity(browserIntent);
            this.finish();
        }
        else {
            // передача ссылки
            if (intent.getClipData() != null && url == null) {
                try_ad();
                // получаем URL из ссылки регуляркой
                String str = String.valueOf(intent.getClipData());
                Pattern p = Pattern.compile("http.*");
                Matcher m = p.matcher(str);
                String url1 = "";
                while (m.find())
                    url1 = m.group().substring(0, (m.group().length() - 3));

                // если ссылка из шазама, то открываем ее в своем приложении по умолчанию
                if (url1.contains("shazam") && special_shazam) useUrl();

                // получить данные о треке и генерируем новый юрл
                make_artist(url1);

                // сохраняем в буфер, если он выбран
                if(Is_Buffer) {
                    String cur_track = Track[0]+" - "+Track[1];
                    // если трек еще не в буфере
                    if (!buffer_container.contains(cur_track)) {
                        buffer_container.add(cur_track);
                        save();
                    }
                }

                // отправляем напрямую, если нет буфера
                else {
                    String newURL = make_url(send_state);

                    Intent share = new Intent(Intent.ACTION_SEND);

                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, newURL + " сгенерировано с помощью Simila");

                    if (use_last_sender && !last_sender_app.equals(""))
                        share.setPackage(last_sender_app);
                    else {
                        PendingIntent pi = PendingIntent.getBroadcast(this, 0,
                                new Intent(this, Receiver.class),
                                PendingIntent.FLAG_UPDATE_CURRENT);
                        share = Intent.createChooser(share, null, pi.getIntentSender());
                    }

                    startActivity(share);
                }
                this.finish();
            }
            else {
                // открываем само приложение
                if (url == null) {
                    // открываем основное окно
                    setContentView(R.layout.activity_main);

                    // обучалка при первом запуске
                    if (is_first) {
                        // tutorial start
                        is_first = false;
                        save();
                    }

                    // выпадающее меню
                    {
                        // инициализация
                        {
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
                        }


                        // кнопки и свитчеры
                        {
                            // буфер
                            {
                                buffer_help_button = (FloatingActionButton) nav_view.getMenu().findItem(R.id.use_buffer).getActionView().findViewById(R.id.buffer_help_button);
                                buffer_switcher = (SwitchMaterial) nav_view.getMenu().findItem(R.id.use_buffer).getActionView().findViewById(R.id.buffer_menu_switcher);
                                buffer_switcher.setChecked(Is_Buffer);
                                buffer_help_button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        PopupMenuCustomLayout popupMenu = new PopupMenuCustomLayout(MainActivity.this, R.layout.help_buffer_layout,
                                                new PopupMenuCustomLayout.PopupMenuCustomOnClickListener() {
                                                    @Override
                                                    public void onClick(int itemId) {
                                                        switch (itemId) {
                                                            case R.id.buffer_help_text:
                                                                break;
                                                        }
                                                    }
                                                });
                                        popupMenu.show(v, 94, -195);
                                    }
                                });
                                buffer_switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                                        Is_Buffer = isChecked;
                                        save();
                                        chip_buffer.setChecked(isChecked);
                                        if (isChecked) {
                                            chip_buffer.setText("Is Using");
                                            bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                        } else {
                                            chip_buffer.setText("Use It");
                                            bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                        }
                                    }
                                });
                            }

                            // шазам
                            {
                                shazam_help_button = (FloatingActionButton) nav_view.getMenu().findItem(R.id.special_shazam).getActionView().findViewById(R.id.shazam_help_button);
                                shazam_switcher = (SwitchMaterial) nav_view.getMenu().findItem(R.id.special_shazam).getActionView().findViewById(R.id.shazam_menu_switcher);
                                shazam_switcher.setChecked(special_shazam);
                                shazam_help_button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        PopupMenuCustomLayout popupMenu = new PopupMenuCustomLayout(MainActivity.this, R.layout.help_shazam_layout,
                                                new PopupMenuCustomLayout.PopupMenuCustomOnClickListener() {
                                                    @Override
                                                    public void onClick(int itemId) {
                                                        switch (itemId) {
                                                            case R.id.shazam_help_text:
                                                                break;
                                                        }
                                                    }
                                                });
                                        popupMenu.show(v, 94, -195);
                                    }
                                });
                                shazam_switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                                        special_shazam = isChecked;
                                        save();
                                    }
                                });
                            }

                            // средство отправки
                            {
                                sender_help_button = (FloatingActionButton) nav_view.getMenu().findItem(R.id.sender).getActionView().findViewById(R.id.sender_help_button);
                                sender_switcher = (SwitchMaterial) nav_view.getMenu().findItem(R.id.sender).getActionView().findViewById(R.id.sender_menu_switcher);
                                sender_switcher.setChecked(use_last_sender);
                                sender_help_button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        PopupMenuCustomLayout popupMenu = new PopupMenuCustomLayout(MainActivity.this, R.layout.help_sender_layout,
                                                new PopupMenuCustomLayout.PopupMenuCustomOnClickListener() {
                                                    @Override
                                                    public void onClick(int itemId) {
                                                        switch (itemId) {
                                                            case R.id.sender_help_text:
                                                                break;
                                                        }
                                                    }
                                                });
                                        popupMenu.show(v, 94, -195);
                                    }
                                });
                                sender_switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                                        use_last_sender = isChecked;
                                        last_sender_app = "";
                                        save();
                                    }
                                });
                            }
                        }
                    }

                    // буфер
                    {
                        // нижняя панель
                        {
                            buffer_sheet = findViewById(R.id.buffer_sheet);
                            Clear_button = findViewById(R.id.Clear_button);
                            Send_button = findViewById(R.id.Send_button);
                            buf_title = findViewById(R.id.title);
                            chip_buffer = (Chip) findViewById(R.id.chip_buffer);
                            recyclerView = (RecyclerView) findViewById(R.id.list_view);

                            bottom_sheet_behavior = BottomSheetBehavior.from(buffer_sheet);
                            bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            bottom_sheet_behavior.setHideable(false);
                            if (buffer_container.isEmpty()) {
                                buf_title.setText("Buffer is empty");
                                Clear_button.setVisibility(View.GONE);
                                Send_button.setVisibility(View.GONE);
                                bottom_sheet_behavior.setDraggable(false);
                            }
                            else {
                                buf_title.setText("Buffer");
                                chip_buffer.setVisibility(View.GONE);
                            }

                        }

                        // буфер чекбокс
                        {
                            chip_buffer.setChecked(Is_Buffer);
                            if (Is_Buffer) chip_buffer.setText("Is Using");
                            else chip_buffer.setText("Use It");

                            chip_buffer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                                    Is_Buffer = isChecked;
                                    save();
                                    buffer_switcher.setChecked(isChecked);
                                    if (isChecked) chip_buffer.setText("Is Using");
                                    else chip_buffer.setText("Use It");
                                }
                            });
                        }

                        // список
                        {
                            layoutManager = new LinearLayoutManager(this);
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setHasFixedSize(true);
                            mAdapter = new AdapterForBuffer(buffer_container);
                            recyclerView.setAdapter(mAdapter);
                        }

                        // кнопка "очистить"
                        {
                            View.OnClickListener clear_button_listener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    buffer_container.clear();
                                    save();
                                    mAdapter.notifyDataSetChanged();
                                    buf_title.setText("Buffer is empty");
                                    Clear_button.setVisibility(View.GONE);
                                    Send_button.setVisibility(View.GONE);
                                    chip_buffer.setVisibility(View.VISIBLE);
                                    bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                    bottom_sheet_behavior.setDraggable(false);
                                }
                            };
                            Clear_button.setOnClickListener(clear_button_listener);
                        }

                        // кнопка отправки
                        {
                            View.OnClickListener send_button_listener = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!buffer_container.isEmpty())
                                    {
                                        try_ad();

                                        String text_to_send = "";
                                        int count = 1;
                                        assert buffer_container != null;
                                        for (String i : buffer_container) {
                                            text_to_send += String.valueOf(count) + ") " + RequestForServer.make_url(i.split(" - "), serviceCards.get(send_state).flag) + "\n";
                                            count += 1;
                                        }

                                        Intent share = new Intent(Intent.ACTION_SEND);
                                        share.setType("text/plain");
                                        share.putExtra(Intent.EXTRA_TEXT, text_to_send + " сгенерировано с помощью Simila");

                                        if (use_last_sender && !last_sender_app.equals(""))
                                            share.setPackage(last_sender_app);
                                        else {
                                            PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0,
                                                    new Intent(MainActivity.this, Receiver.class),
                                                    PendingIntent.FLAG_UPDATE_CURRENT);
                                            share = Intent.createChooser(share, null, pi.getIntentSender());
                                        }

                                        startActivity(share);
                                    }
                                }
                            };
                            Send_button.setOnClickListener(send_button_listener);
                        }
                    }

                    // скроллеры
                    {
                        // заполняем список приложений для скроллеров
                        serviceCards.add(new ServiceCard(R.drawable.yandex, services.yandex));
                        serviceCards.add(new ServiceCard(R.drawable.vk, services.vk));
                        serviceCards.add(new ServiceCard(R.drawable.youtube, services.youtube));
                        serviceCards.add(new ServiceCard(R.drawable.shazam, services.shazam));
                        serviceCards.add(new ServiceCard(R.drawable.deezer, services.deezer));
                        serviceCards.add(new ServiceCard(R.drawable.apple, services.apple));
                        serviceCards.add(new ServiceCard(R.drawable.youtube_music, services.youtubemusic));

                        adapterForPagers = new AdapterForPagers(serviceCards, this);

                        viewPager = findViewById(R.id.viewPager);
                        viewPager2 = findViewById(R.id.viewPager2);
                        viewPager.setAdapter(adapterForPagers);
                        viewPager2.setAdapter(adapterForPagers);
                        first_pager_nav = findViewById(R.id.first_pager_nav);
                        second_pager_nav = findViewById(R.id.second_pager_nav);

                        // горизонтальные отступы между объектами скроллеров
                        viewPager.setPadding(200, 0, 200, 0);
                        viewPager2.setPadding(200, 0, 200, 0);

                        // создание навигации
                        fill_nav(first_pager_nav);
                        fill_nav(second_pager_nav);

                        // устанавливаем выбор на значения на момент закрытия
                        viewPager.setCurrentItem(open_state + serviceCards.size() * 50, false);
                        point_it(open_state, first_pager_nav);
                        viewPager2.setCurrentItem(send_state + serviceCards.size() * 50, false);
                        point_it(send_state, second_pager_nav);

                        // создание скроллеров
                        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                            @Override
                            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                            }

                            @Override
                            public void onPageSelected(int position) {
                                open_state = position % serviceCards.size();
                                point_it(open_state, first_pager_nav);
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
                                send_state = position % serviceCards.size();
                                point_it(send_state, second_pager_nav);
                                save();
                            }

                            @Override
                            public void onPageScrollStateChanged(int state) {
                            }
                        });

                    }

                }
                // открываем полученные ссылки
                else {
                    try_ad();
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
    public void make_artist(String url) {Track = RequestForServer.make_song(url);}

    // Функция: создаем требуемый url, обращаясь к классу UrlMaker
    public String make_url(int state) {
        return RequestForServer.make_url(Track, serviceCards.get(state).flag);
    }

    // плюсуем счетчик рекламы и запускаем ее при необходимости
    public static void try_ad() {
        if (!is_bought) {
            if (ad_counter > 2 && mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
                ad_counter = 0;
            }
            else ad_counter++;
            save();
        }
    }

    // Подсветка нужной точки навигации
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void point_it (int number, LinearLayout pager) {
        for (int i = 0; i < serviceCards.size(); i++) {
            ImageView cur_img = (ImageView) pager.getChildAt(i);
            cur_img.setImageResource(R.drawable.point);
            cur_img.setColorFilter(getColor(R.color.selectors_nav_point));
        }
        ImageView cur_img = (ImageView) pager.getChildAt(number);
        cur_img.setImageResource(R.drawable.active_point);
        cur_img.setColorFilter(getColor(R.color.selectors_nav_point_active));
    }

    // заполнение навигации по количеству сервисов в списке
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void fill_nav (LinearLayout pager) {
        for (int i = 0; i < serviceCards.size(); i++) {
            ImageView imageView = new ImageView(MainActivity.this);
            imageView.setImageResource(R.drawable.point);
            LinearLayout.LayoutParams imageViewLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f);
            imageView.setLayoutParams(imageViewLayoutParams);
            imageView.setScaleX(2);
            imageView.setScaleY(2);
            imageView.setColorFilter(getColor(R.color.selectors_nav_point));
            pager.addView(imageView);
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

        // загружаем значения настроек
        use_last_sender = sPref.getBoolean("use_last_sender", false);
        last_sender_app = sPref.getString("last_sender_app","");
        Is_Buffer = sPref.getBoolean("buf_state", false);
        is_bought = sPref.getBoolean("bought_state", false);
        is_first = sPref.getBoolean("first_state", true);
        special_shazam = sPref.getBoolean("special_shazam", true);
        ad_counter = sPref.getInt("add_state", 0);
    }

    // сохранение выбора для дальнейшего открытия через него по умолчанию
    public static void save() {
        SharedPreferences.Editor ed = sPref.edit();

        // сохраняем значение буфера
        String bufferset = "";
        for(int i = 0; i < buffer_container.size(); i++) {
            bufferset += buffer_container.get(i);
            bufferset += '|';
        }

        // сохраняем настройки
        ed.putBoolean("use_last_sender", use_last_sender);
        ed.putBoolean("buf_state", Is_Buffer);
        ed.putBoolean("bought_state", is_bought);
        ed.putBoolean("first_state", is_first);
        ed.putBoolean("special_shazam", special_shazam);
        ed.putInt("add_state", ad_counter);
        ed.putString("buffer_container", bufferset);
        ed.putString("last_sender_app", last_sender_app);
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
        mAdapter.notifyDataSetChanged();
        bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        if (!buffer_container.isEmpty()) {
            Log.w("check", "here");
            buf_title.setText("Buffer");
            Clear_button.setVisibility(View.VISIBLE);
            Send_button.setVisibility(View.VISIBLE);
            chip_buffer.setVisibility(View.GONE);
            bottom_sheet_behavior.setDraggable(true);
        }
    }

    @Override
    // нажатия на кнопки в меню
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // туториалы
            case R.id.tutorial_open:
                Log.w("check","tutorial_open clicked");
                return true;
            case R.id.tutorial_send:
                Log.w("check","tutorial_send clicked");
                return true;
            case R.id.tutorial_buffer:
                Log.w("check","tutorial_buffer clicked");
                return true;
            case R.id.tutorial_reopen:
                Log.w("check","tutorial_reopen clicked");
                return true;

            // о приложении
            case R.id.rate:
                Log.w("check","rate clicked");
                return true;
            case R.id.donate:
                Log.w("check","donate clicked");
                return true;
            case R.id.addblock:
                Log.w("check","addblock clicked");
                return true;
        }
        return true;
    }
}
