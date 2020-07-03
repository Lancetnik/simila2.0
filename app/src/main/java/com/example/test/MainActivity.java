package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
    private Adapter adapter;
    private ArrayList<Model> models = new ArrayList<>();

    // выпадающее меню
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView nav_view;

    private SwitchMaterial buffer_switcher;
    private SwitchMaterial shazam_switcher;
    private FloatingActionButton buffer_help_button;
    private FloatingActionButton shazam_help_button;

    // для сохранения
    private static SharedPreferences sPref;
    private static Integer open_state = 2; // значение скроллера для открытия
    public static Integer send_state = 2; // значение скроллера для закрытия
    private static boolean is_first = true; // первый ли раз открыта программа
    private static boolean is_bought = false; // куплена ли фулл версия
    private static int add_counter; // счетчик для октрытия рекламы
    private static boolean special_shazam = false; // отправлять ли ссылки из шазама сразу на открытие в своем сервисе

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


        // отправляем ссылку самому себе, чтобы открыть в другом приложении
        if (intent.getClipData()!=null && String.valueOf(intent.getClipData()).contains("Simila")){
            try_add();
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
                try_add();
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
                            buffer_help_button = (FloatingActionButton) nav_view.getMenu().findItem(R.id.use_buffer).getActionView().findViewById(R.id.buffer_help_button);
                            buffer_switcher = (SwitchMaterial) nav_view.getMenu().findItem(R.id.use_buffer).getActionView().findViewById(R.id.buffer_menu_switcher);
                            buffer_switcher.setChecked(Is_Buffer);
                            buffer_help_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.w("check", "buffer_help_button pressed");
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
                                    }
                                    else {
                                        chip_buffer.setText("Use It");
                                        bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                    }
                                }
                            });

                            shazam_help_button = (FloatingActionButton) nav_view.getMenu().findItem(R.id.special_shazam).getActionView().findViewById(R.id.shazam_help_button);
                            shazam_switcher = (SwitchMaterial) nav_view.getMenu().findItem(R.id.special_shazam).getActionView().findViewById(R.id.shazam_menu_switcher);
                            shazam_switcher.setChecked(special_shazam);
                            shazam_help_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.w("check", "shazam_help_button pressed");
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
                            bottom_sheet_behavior.setHideable(false);
                            bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            if (buffer_container.isEmpty()) {
                                buf_title.setText("Buffer is empty");
                                Clear_button.setVisibility(View.GONE);
                                Send_button.setVisibility(View.GONE);
                            }
                            else {
                                buf_title.setText("Buffer");
                                chip_buffer.setVisibility(View.GONE);
                            }
                            bottom_sheet_behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                                @Override
                                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                                    if (buffer_container.isEmpty()) {
                                        if (newState == BottomSheetBehavior.STATE_EXPANDED)
                                            bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    }
                                }

                                @Override
                                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                                }
                            });
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
                                    if (isChecked) {
                                        chip_buffer.setText("Is Using");
                                        bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                    }
                                    else {
                                        chip_buffer.setText("Use It");
                                        bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                    }
                                }
                            });
                        }

                        // список
                        {
                            layoutManager = new LinearLayoutManager(this);
                            recyclerView.setLayoutManager(layoutManager);
                            recyclerView.setHasFixedSize(true);
                            mAdapter = new MyAdapter(buffer_container);
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
                                    bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    buf_title.setText("Buffer is empty");
                                    Clear_button.setVisibility(View.GONE);
                                    Send_button.setVisibility(View.GONE);
                                    chip_buffer.setVisibility(View.VISIBLE);
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
                                        String text_to_send = "";
                                        int count = 1;
                                        assert buffer_container != null;
                                        for (String i : buffer_container) {
                                            text_to_send += String.valueOf(count) + ") " + UrlMaker.make_url(send_state, i.split(" - ")) + "\n";
                                            count += 1;
                                        }

                                        Intent intent2 = new Intent();
                                        intent2.setAction(Intent.ACTION_SEND);
                                        intent2.setType("text/plain");
                                        intent2.putExtra(Intent.EXTRA_TEXT, text_to_send + " сгенерировано с помощью Simila");
                                        startActivity(Intent.createChooser(intent2, "Share"));

                                        bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    }
                                }
                            };
                            Send_button.setOnClickListener(send_button_listener);
                        }
                    }

                        // скроллеры
                    {
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
                    }

                }
                // открываем полученные ссылки
                else {
                    try_add();
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
            first.setColorFilter(getColor(R.color.selectors_nav_point));
            ImageView second = findViewById(R.id.Img2Pager1);
            second.setImageResource(R.drawable.point);
            second.setColorFilter(getColor(R.color.selectors_nav_point));
            ImageView third = findViewById(R.id.Img3Pager1);
            third.setImageResource(R.drawable.point);
            third.setColorFilter(getColor(R.color.selectors_nav_point));
            ImageView fourth = findViewById(R.id.Img4Pager1);
            fourth.setImageResource(R.drawable.point);
            fourth.setColorFilter(getColor(R.color.selectors_nav_point));
            ImageView fifth = findViewById(R.id.Img5Pager1);
            fifth.setImageResource(R.drawable.point);
            fifth.setColorFilter(getColor(R.color.selectors_nav_point));
            ImageView sixth = findViewById(R.id.Img6Pager1);
            sixth.setImageResource(R.drawable.point);
            sixth.setColorFilter(getColor(R.color.selectors_nav_point));
            ImageView seventh = findViewById(R.id.Img7Pager1);
            seventh.setImageResource(R.drawable.point);
            seventh.setColorFilter(getColor(R.color.selectors_nav_point));
            ImageView eight = findViewById(R.id.Img8Pager1);
            eight.setImageResource(R.drawable.point);
            eight.setColorFilter(getColor(R.color.selectors_nav_point));
            switch (number) {
                case 1: {
                    first.setImageResource(R.drawable.active_point);
                    first.setColorFilter(getColor(R.color.selectors_nav_point_active));
                    break;
                }
                case 2: {
                    second.setImageResource(R.drawable.active_point);
                    second.setColorFilter(getColor(R.color.selectors_nav_point_active));
                    break;
                }
                case 3: {
                    third.setImageResource(R.drawable.active_point);
                    third.setColorFilter(getColor(R.color.selectors_nav_point_active));
                    break;
                }
                case 4: {
                    fourth.setImageResource(R.drawable.active_point);
                    fourth.setColorFilter(getColor(R.color.selectors_nav_point_active));
                    break;
                }
                case 5: {
                    fifth.setImageResource(R.drawable.active_point);
                    fifth.setColorFilter(getColor(R.color.selectors_nav_point_active));
                    break;
                }
                case 6: {
                    sixth.setImageResource(R.drawable.active_point);
                    sixth.setColorFilter(getColor(R.color.selectors_nav_point_active));
                    break;
                }
                case 7: {
                    seventh.setImageResource(R.drawable.active_point);
                    seventh.setColorFilter(getColor(R.color.selectors_nav_point_active));
                    break;
                }
                case 8: {
                    eight.setImageResource(R.drawable.active_point);
                    eight.setColorFilter(getColor(R.color.selectors_nav_point_active));
                    break;
                }
            }
        }
        if (pager == 2) {
                ImageView first = findViewById(R.id.Img1Pager2);
                first.setImageResource(R.drawable.point);
                first.setColorFilter(getColor(R.color.selectors_nav_point));
                ImageView second = findViewById(R.id.Img2Pager2);
                second.setImageResource(R.drawable.point);
                second.setColorFilter(getColor(R.color.selectors_nav_point));
                ImageView third = findViewById(R.id.Img3Pager2);
                third.setImageResource(R.drawable.point);
                third.setColorFilter(getColor(R.color.selectors_nav_point));
                ImageView fourth = findViewById(R.id.Img4Pager2);
                fourth.setImageResource(R.drawable.point);
                fourth.setColorFilter(getColor(R.color.selectors_nav_point));
                ImageView fifth = findViewById(R.id.Img5Pager2);
                fifth.setImageResource(R.drawable.point);
                fifth.setColorFilter(getColor(R.color.selectors_nav_point));
                ImageView sixth = findViewById(R.id.Img6Pager2);
                sixth.setImageResource(R.drawable.point);
                sixth.setColorFilter(getColor(R.color.selectors_nav_point));
                ImageView seventh = findViewById(R.id.Img7Pager2);
                seventh.setImageResource(R.drawable.point);
                seventh.setColorFilter(getColor(R.color.selectors_nav_point));
                ImageView eight = findViewById(R.id.Img8Pager2);
                eight.setImageResource(R.drawable.point);
                eight.setColorFilter(getColor(R.color.selectors_nav_point));
                switch (number) {
                    case 1: {
                        first.setImageResource(R.drawable.active_point);
                        first.setColorFilter(getColor(R.color.selectors_nav_point_active));
                        break;
                    }
                    case 2: {
                        second.setImageResource(R.drawable.active_point);
                        second.setColorFilter(getColor(R.color.selectors_nav_point_active));
                        break;
                    }
                    case 3: {
                        third.setImageResource(R.drawable.active_point);
                        third.setColorFilter(getColor(R.color.selectors_nav_point_active));
                        break;
                    }
                    case 4: {
                        fourth.setImageResource(R.drawable.active_point);
                        fourth.setColorFilter(getColor(R.color.selectors_nav_point_active));
                        break;
                    }
                    case 5: {
                        fifth.setImageResource(R.drawable.active_point);
                        fifth.setColorFilter(getColor(R.color.selectors_nav_point_active));
                        break;
                    }
                    case 6: {
                        sixth.setImageResource(R.drawable.active_point);
                        sixth.setColorFilter(getColor(R.color.selectors_nav_point_active));
                        break;
                    }
                    case 7: {
                        seventh.setImageResource(R.drawable.active_point);
                        seventh.setColorFilter(getColor(R.color.selectors_nav_point_active));
                        break;
                    }
                    case 8: {
                        eight.setImageResource(R.drawable.active_point);
                        eight.setColorFilter(getColor(R.color.selectors_nav_point_active));
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

        // загружаем значения настроек
        Is_Buffer = sPref.getBoolean("buf_state", false);
        is_bought = sPref.getBoolean("bought_state", false);
        is_first = sPref.getBoolean("first_state", true);
        special_shazam = sPref.getBoolean("special_shazam", true);
        add_counter = sPref.getInt("add_state", 0);
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
        ed.putBoolean("buf_state", Is_Buffer);
        ed.putBoolean("bought_state", is_bought);
        ed.putBoolean("first_state", is_first);
        ed.putBoolean("special_shazam", special_shazam);
        ed.putInt("add_state", add_counter);
        ed.putString("buffer_container", bufferset);
        ed.putInt("open_state", open_state);
        ed.putInt("send_state", send_state);
        ed.apply();
    }

    // обработка рекламы
    void try_add() {
        if (!is_bought) {
            add_counter++;
            save();
            if (add_counter == 10) {
                //add.show();
                add_counter = 0;
            }
        }
    }

    @Override
    // если мы передали ссылку в буфер, когда приложение было свернуто
    protected void onRestart() {
        super.onRestart();
        load();
        // открываем буфер
        mAdapter.notifyDataSetChanged();
        bottom_sheet_behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        if (buffer_container.isEmpty()) {
            buf_title.setText("Buffer is empty");
            Clear_button.setVisibility(View.GONE);
            Send_button.setVisibility(View.GONE);
            chip_buffer.setVisibility(View.VISIBLE);
        }
        else {
            buf_title.setText("Buffer");
            Clear_button.setVisibility(View.VISIBLE);
            Send_button.setVisibility(View.VISIBLE);
            chip_buffer.setVisibility(View.GONE);
        }
    }

    @Override
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

            // настройки
            /*case R.id.special_shazam:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                special_shazam = item.isChecked();
                save();
                return false;
            case R.id.use_buffer:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                Is_Buffer = item.isChecked();
                chip_buffer.setChecked(item.isChecked());
                save();
                return false;*/

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

    /*void find_apps () {
        Intent intent = new Intent(null, dataUri);
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
    }*/
}
