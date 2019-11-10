package ru.myprojects.diary;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.util.Calendar;

/*Активити для создания новой записи*/
public class Activity_New extends AppCompatActivity {

    //работающие элементы
    Button btn_time, btn_color;
    EditText et;
    Button btn_ok, btn_cancel;

    //текущее время
    int current_hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    int current_minute = Calendar.getInstance().get(Calendar.MINUTE);

    //Данные для новой записи
    int last_time;
    int time = 0;
    String text;
    int color;

    //Слушатель для выбора времени
    TimePickerDialog.OnTimeSetListener myCallBack = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            time = hourOfDay*60 + minute;
            //Проверка на корректное время, нельзя создать запись раньше предыдущей
            if(time > last_time && time < current_hour*60 + current_minute)
                btn_time_setText(hourOfDay + ":" + minute);
            else {
                Toast.makeText(getBaseContext(), "Wrong time", Toast.LENGTH_SHORT).show();
                time = 0;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_element);

        //Получение времени последней записи
        last_time = getIntent().getIntExtra("time", 0);

        btn_time = findViewById(R.id.btn_time);
        btn_color = findViewById(R.id.btn_color);
        et = findViewById(R.id.et_new);
        btn_ok = findViewById(R.id.btn_ok);
        btn_cancel = findViewById(R.id.btn_cancel);
    }

    private void btn_time_setText(String text) {
        btn_time.setText(text);
    }

    private void btn_color_setBackgroundColor(int color) {
        btn_color.setBackgroundColor(color);
    }

    //Обработчики нажатий на кнопки
    public void btn_time_onClick(View view) {
        TimePickerDialog tpd = new TimePickerDialog(this, myCallBack, current_hour, current_minute, true);
        tpd.show();
    }

    public void btn_color_onClick(View view) {
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(Color.WHITE)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        color = selectedColor;
                        btn_color_setBackgroundColor(selectedColor);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    public void btn_ok_onClick(View view) {
        text = et.getText().toString();
        if(text.equals(""))
            Toast.makeText(this,"Введите текст", Toast.LENGTH_SHORT).show();
        else {
            if(time == 0) time = current_hour*60 + current_minute;
            DB.db.addRec(Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH),time,text,color);
            setResult(RESULT_OK);
            finish();
        }
    }

    public void btn_cancel_onClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
