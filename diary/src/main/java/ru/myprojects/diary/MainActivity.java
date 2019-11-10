package ru.myprojects.diary;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.loader.app.LoaderManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static ru.myprojects.diary.DB.db;

public class MainActivity extends FragmentActivity{

    static int last_time = 0;

    //Общие константы для всего приложения
    static final int PAGE_COUNT = 8;
    static final String TAG = "LOG";

    //Страничка и слушатель для её изменения
    ViewPager pager;
    PagerAdapter pagerAdapter;
    int current_page;
    ViewPager.OnPageChangeListener PCListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            current_page = position;
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    GoToFragment gtFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Нахожу страничку и устанавливаю её адаптер
        pager = findViewById(R.id.pager);
        pagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);

        //Устанавливаю слушателя изменений странички
        pager.addOnPageChangeListener(PCListener);
        pager.setCurrentItem(7,false);

        //БД
        db = new DB(this);
        db.open();
        db.Update_DB(Calendar.getInstance().get(Calendar.MONTH),Calendar.getInstance().get(Calendar.YEAR));
    }

    //Вызыватся перед уничтожением приложения
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Снимаю слушателя странички
        pager.removeOnPageChangeListener(PCListener);
        db.close();
    }

    //Класс-адаптер для странички
    private class MyFragmentPagerAdapter extends FragmentPagerAdapter {

        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        //Новая страничка
        @NonNull
        @Override
        public Fragment getItem(int position) {
            if(position == 0) {
                gtFragment = new GoToFragment();
                return gtFragment;
            }else {
                return PageListFragment.newInstance(position);
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        //Заголовок странички
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return "Перейти";
                case 7: return "Сегодня";
                case 6: return "Вчера";
                default:
                    Calendar c = Calendar.getInstance();
                    c.add(Calendar.DAY_OF_MONTH, -7 + position);
                    switch (c.get(Calendar.DAY_OF_WEEK)) {
                        case Calendar.MONDAY: return "Понедельник";
                        case Calendar.TUESDAY: return "Вторник";
                        case Calendar.WEDNESDAY: return "Среда";
                        case Calendar.THURSDAY: return "Четверг";
                        case Calendar.FRIDAY: return "Пятница";
                        case Calendar.SATURDAY: return "Суббота";
                        case Calendar.SUNDAY: return "Воскресенье";
                    }
            }
            return "";
        }
    }

    //обработчик нажатия кнопки назад
    @Override
    public void onBackPressed() {
        if(gtFragment != null && current_page == 0 && gtFragment.OnBackPressed()) return;
        super.onBackPressed();
    }

    //слушатель нажатия на кнопку "новая запись"
    public void btn_new_onClick(View view) {
        Intent intent = new Intent(this, Activity_New.class);
        intent.putExtra("time", last_time);
        startActivityForResult(intent, 1);
    }

    //слушатель на результат новой записи
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
            LoaderManager.getInstance(this).getLoader(7).forceLoad();
    }
}
