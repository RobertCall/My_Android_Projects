package ru.myprojects.diary;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static ru.myprojects.diary.DB.db;
import static ru.myprojects.diary.MainActivity.TAG;

/*Фрагмент для перехода на выбранную дату*/
public class GoToFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor>{
    //Текущая дата
    private int year_current = Calendar.getInstance().get(Calendar.YEAR);
    private int month_current = Calendar.getInstance().get(Calendar.MONTH);
    private int day_current = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

    //Выбранная дата
    private int month_selected, day_selected;

    //Слушатель DatePicker'а на выбор даты
    private DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            //проверка на год
            if(year == year_current || (year == year_current - 1 && month > month_current))
                getDay(view, month, dayOfMonth);
            else
                Toast.makeText(getContext(), "Неверная дата", Toast.LENGTH_SHORT).show();
        }
    };

    //Рабочие элементы
    private Button btn;
    private ListView lv;
    private TextView tv;

    //Адаптер для курсора и загрузчик
    private SimpleCursorAdapter scAdapter;
    private MyCursorLoader mcLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.goto_fragment, null); //я тут поправил null на context

        btn = view.findViewById(R.id.goto_button);
        btn.setOnClickListener(this);
        lv = view.findViewById(R.id.goto_list);
        tv = view.findViewById(R.id.goto_empty);

        return view;
    }

    //Обработчик нажатия на кнопку
    @Override
    public void onClick(View view) {
        DatePickerDialog dpd = new DatePickerDialog(view.getContext(), myCallBack, year_current, month_current, day_current);
        dpd.show();
    }

    //Метод для возвращения к кнопке
    public boolean OnBackPressed() {
        if(btn.getVisibility() == View.GONE) {
            btn.setEnabled(true);
            btn.setVisibility(View.VISIBLE);
            if(lv.getVisibility() == View.VISIBLE)
                lv.setVisibility(View.GONE);
            else
                tv.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    //три метода для LoaderCallbacks
    @NonNull @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        mcLoader = new MyCursorLoader(getContext(), month_selected, day_selected);
        return mcLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if(cursor.getCount() == 0) {
            tv.setVisibility(View.VISIBLE);
            lv.setVisibility(View.GONE);
        } else
            scAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mcLoader.ChangeDate(month_selected, day_selected);
    }

    //Получение записей конкретного дня
    private void getDay(View view, int month, int day) {
        month_selected = month;
        day_selected = day;

        //Скрываю/показываю нужные элементы
        btn.setEnabled(false);
        btn.setVisibility(View.GONE);
        lv.setVisibility(View.VISIBLE);

        // формируем столбцы сопоставления
        String[] from = new String[] { DB.COLUMN_TIME, DB.COLUMN_TXT, DB.COLUMN_COLOR };
        int[] to = new int[] { R.id.tvTime, R.id.tvText, R.id.tvText };

        // создаем адаптер
        scAdapter = new SimpleCursorAdapter(view.getContext(), R.layout.item, null, from, to, 0);
        //Правила сопоставления
        SimpleCursorAdapter.ViewBinder vb = new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                switch (columnIndex) {
                    case 2:
                        if(view instanceof TextView) {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                            ((TextView)view).setText(sdf.format(new Date(0,0,0, cursor.getInt(columnIndex)/60, cursor.getInt(columnIndex)%60)));
                            return true;
                        }
                    case 3:
                        if(view instanceof TextView) {
                            ((TextView)view).setText(cursor.getString(columnIndex));
                            return true;
                        }
                    case 4:
                        if(view instanceof TextView) {
                            view.setBackgroundColor(cursor.getInt(columnIndex));
                            return true;
                        }
                }
                return false;
            }
        };
        scAdapter.setViewBinder(vb);
        lv.setAdapter(scAdapter);

        //создаём лоадер и говорим ему загрузить данные
        LoaderManager.getInstance(getActivity()).restartLoader(8,null, this);//поменял getActivity на this
        LoaderManager.getInstance(getActivity()).getLoader(8).forceLoad();
    }
}
