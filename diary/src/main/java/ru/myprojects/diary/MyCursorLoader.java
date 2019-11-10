package ru.myprojects.diary;

import android.content.Context;
import android.database.Cursor;

import androidx.loader.content.CursorLoader;

import static ru.myprojects.diary.DB.db;

public class MyCursorLoader extends CursorLoader {

    private int day;
    private int month;

    public MyCursorLoader(Context context, int month, int day) {
        super(context);
        this.day = day;
        this.month = month;
    }

    @Override
    public Cursor loadInBackground() {
        return db.getDayData(month, day);
    }

    public void ChangeDate(int month, int day) {
        this.month = month;
        this.day = day;
    }
}
