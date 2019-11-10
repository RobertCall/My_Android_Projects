package ru.myprojects.diary;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static ru.myprojects.diary.DB.db;
import static ru.myprojects.diary.MainActivity.TAG;


public class PageListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    //Константы для каждой странички
    private static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    private static final String ARGUMENT_MOUNTH = "arg_mounth";
    private static final String ARGUMENT_DAY = "arg_day";

    private int pageNumber;

    private int month;
    private int day;

    private SimpleCursorAdapter scAdapter;

    private static final int CM_DELETE_ID = 1;
    private static final int CM_CHANGE_ID = 2;

    //Новая страничка
    static PageListFragment newInstance(int page) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -7 + page);

        PageListFragment plf = new PageListFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        arguments.putInt(ARGUMENT_MOUNTH, c.get(Calendar.MONTH));
        arguments.putInt(ARGUMENT_DAY, c.get(Calendar.DAY_OF_MONTH));
        plf.setArguments(arguments);
        return plf;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER, 0);
        month = getArguments().getInt(ARGUMENT_MOUNTH);
        day = getArguments().getInt(ARGUMENT_DAY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int resoure = (pageNumber == 7)?R.layout.page_today:R.layout.page;
        View view = inflater.inflate(resoure, null);

        // формируем столбцы сопоставления
        String[] from = new String[] { DB.COLUMN_TIME, DB.COLUMN_TXT, DB.COLUMN_COLOR };
        int[] to = new int[] { R.id.tvTime, R.id.tvText, R.id.tvText };

        // создаем адаптер
        scAdapter = new SimpleCursorAdapter(getContext(), R.layout.item, null, from, to, 0);
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
        setListAdapter(scAdapter);

        //создаём лоадер и говорим ему загрузить данные
        LoaderManager.getInstance(getActivity()).initLoader(pageNumber,null, this);
        LoaderManager.getInstance(getActivity()).getLoader(pageNumber).forceLoad();

        registerForContextMenu(view.findViewById(android.R.id.list));

        return view;
    }

    //три метода для LoaderCallbacks
    @Override @NonNull
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        return new MyCursorLoader(getContext(), month, day);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if(pageNumber == 7 && cursor.getCount() != 0) {
            cursor.moveToLast();
            MainActivity.last_time = cursor.getInt(cursor.getColumnIndex(DB.COLUMN_TIME));
            cursor.moveToFirst();
        }
        scAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    //Метод для создания меню
    public void onCreateContextMenu(@NonNull ContextMenu menu,@NonNull View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CM_DELETE_ID, 0, R.string.Delete_record);
        menu.add(0, CM_CHANGE_ID, 0, R.string.Change_color);
    }

    //Слушатель клика на контекстное меню
    public boolean onContextItemSelected(MenuItem item) {
        // получаем из пункта контекстного меню данные по пункту списка
        final AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
            case CM_DELETE_ID:
                // извлекаем id записи и удаляем соответствующую запись в БД
                db.delRec(month, acmi.id);
                // получаем новый курсор с данными
                LoaderManager.getInstance(getActivity()).getLoader(pageNumber).forceLoad();
                return true;
            case CM_CHANGE_ID:

                ColorPickerDialogBuilder
                        .with(getContext())
                        .setTitle("Choose color")
                        .initialColor(((ColorDrawable)acmi.targetView.findViewById(R.id.tvTime).getBackground()).getColor())
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                db.Change_Color(month, acmi.id, selectedColor);
                                acmi.targetView.findViewById(R.id.tvText).setBackgroundColor(selectedColor);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
                // получаем новый курсор с данными
                LoaderManager.getInstance(getActivity()).getLoader(pageNumber).forceLoad();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
