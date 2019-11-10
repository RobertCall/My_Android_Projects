package ru.myprojects.diary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import static ru.myprojects.diary.MainActivity.TAG;

/*Класс для работы с БД*/
public class DB {

    //Константы для БД
    private static final String DB_NAME = "mydb";
    private static final int DB_VERSION = 1;

    //Имена столбцов для таблицы с месяцами
    private static final String DB_BASE_TABLE = "mytab";
    private static final String COLUMN_BASE_MONTH = "month";
    private static final String COLUMN_BASE_YEAR = "year";

    //Имена столбцов для таблиц записей
    private static final String[] months = {"January", "Fabruary", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_TXT = "txt";
    public static final String COLUMN_COLOR = "color";

    private final Context mCtx;

    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;
    static DB db;

    public DB(Context ctx) {
        mCtx = ctx;
    }

    // открыть подключение
    public void open() {
        mDBHelper = new DBHelper(mCtx, DB_NAME, null, DB_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    // закрыть подключение
    public void close() {
        if (mDBHelper!=null) mDBHelper.close();
    }

    // получить записи конкретного дня
    public Cursor getDayData(int month, int day) {
        String selection = COLUMN_DAY + " = ?";
        String[] selectionArgs = new String[] { "" + day };
        return mDB.query(months[month], null, selection, selectionArgs , null, null, null);
    }

    // добавить запись в DB_TABLE
    public void addRec(int month, int day, int time, String txt, int color) {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DAY, day);
        cv.put(COLUMN_TIME, time);
        cv.put(COLUMN_TXT, txt);
        cv.put(COLUMN_COLOR, color);
        mDB.insert(months[month], null, cv);
    }

    // удалить запись из DB_TABLE
    public void delRec(int month, long id) {
        mDB.delete(months[month], COLUMN_ID + " = " + id, null);
    }

    //Изменить цвет записи
    public void Change_Color(int month, long id, int color) {
        Cursor cursor = mDB.query(months[month],null, COLUMN_ID + " = " + id, null, null, null, null);
        cursor.moveToFirst();
        Log.d(TAG, "count = " + cursor.getCount());
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DAY, cursor.getInt(1));
        cv.put(COLUMN_TIME, cursor.getInt(2));
        cv.put(COLUMN_TXT, cursor.getString(3));
        cv.put(COLUMN_COLOR, color);
        mDB.update(months[month], cv, COLUMN_ID + " = " + id, null);
        cursor.close();
    }

    //Очистка устаревших таблиц
    public void Update_DB(int month, int year) {
        Cursor cursor = mDB.query(DB_BASE_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        for(int i = 0; i < 12; i++) {
            if(cursor.getInt(2) != year) {
                mDB.execSQL("delete from " + months[i]);
                ContentValues cv = new ContentValues();
                cv.put(COLUMN_BASE_MONTH, months[i]);
                cv.put(COLUMN_BASE_YEAR, year);
                mDB.update(DB_BASE_TABLE,cv, COLUMN_ID + " = " + (i + 1), null);
            }
            if(i == month) year--;
        }

        cursor.close();
    }

    // класс по созданию и управлению БД
    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        // создаем и заполняем БД
        @Override
        public void onCreate(SQLiteDatabase db) {

            //Команда для создания основной таблицы
            db.execSQL("create table " +
                    DB_BASE_TABLE + "(" +
                    COLUMN_ID + " integer primary key autoincrement, " +
                    COLUMN_BASE_MONTH + " text, " +
                    COLUMN_BASE_YEAR + " integer" +
                    ");");

            ContentValues cv = new ContentValues();
            for (int i = 0; i < 12; i++) {
                cv.put(COLUMN_BASE_MONTH, months[i]);
                cv.put(COLUMN_BASE_YEAR, 0);
                db.insert(DB_BASE_TABLE, null, cv);

                //Команда для создания таблиц с записями
                db.execSQL("create table " + months[i] + "(" +
                        COLUMN_ID + " integer primary key autoincrement, " +
                        COLUMN_DAY + " integer, " +
                        COLUMN_TIME + " integer, " +
                        COLUMN_TXT + " text, " +
                        COLUMN_COLOR + " integer" +
                        ");");
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}