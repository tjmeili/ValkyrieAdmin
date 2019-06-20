package com.meilinger.tj.admin;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import server.data.Day;

/**
 * Created by TJ on 3/23/2018.
 */

public class DayView extends View {

    private ToggleButton dayToggleButton;
    private ViewGroup root;
    private TextView tvStartTime, tvEndTime;
    private TextView tvDayName;
    //private RowDayBinding binding;
    private int dayOfWeek = -1;
    private SimpleDateFormat timeFormatter, dayNameFormatter;

    public DayViewListener listener;


    public DayView(Context context, ViewGroup root, int dayOfWeek) {
        super(context);
        this.root = root;
        this.dayOfWeek = dayOfWeek;
        this.listener = null;
        init();
    }

    public void setDayViewListener(DayViewListener listener){
        this.listener = listener;
    }


    private void init(){
        inflate(getContext(), R.layout.row_day, root);
        timeFormatter  = new SimpleDateFormat("h:mm a");
        dayNameFormatter = new SimpleDateFormat("EEE - M/d/yy");
        this.dayToggleButton = (ToggleButton) root.findViewById(R.id.dayToggleButton);
        this.tvStartTime = (TextView) root.findViewById(R.id.tvStartTime);
        this.tvEndTime = (TextView) root.findViewById(R.id.tvEndTime);
        this.tvDayName = (TextView) root.findViewById(R.id.tvDayName);

        Day d = MainActivity.schedule.get(dayOfWeek);
        tvStartTime.setText(timeFormatter.format(d.getStartTime()));
        tvEndTime.setText(timeFormatter.format(d.getEndTime()));
        tvDayName.setText(dayNameFormatter.format(d.getDate()));

        tvStartTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minute = Calendar.getInstance().get(Calendar.MINUTE);
                TimePickerDialog dialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar c = Calendar.getInstance();
                        c.setFirstDayOfWeek(Calendar.WEDNESDAY);
                        c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        if(listener != null){
                            listener.onStartTimeChanged(dayOfWeek, c.getTime());
                        }
                        tvStartTime.setText(timeFormatter.format(c.getTime()));
                    }
                }, 6, 0, false);
                dialog.setTitle("Start Time");
                dialog.show();
            }
        });

        tvEndTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                int minute = Calendar.getInstance().get(Calendar.MINUTE);
                TimePickerDialog dialog = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar c = Calendar.getInstance();
                        c.setFirstDayOfWeek(Calendar.WEDNESDAY);
                        c.set(Calendar.DAY_OF_WEEK, dayOfWeek);
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        if(listener != null){
                            listener.onEndTimeChanged(dayOfWeek, c.getTime());
                        }
                        tvEndTime.setText(timeFormatter.format(c.getTime()));

                    }
                }, 14, 0, false);
                dialog.setTitle("Start Time");
                dialog.show();
            }
        });

        dayToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(listener != null){
                    listener.onActiveChanged(dayOfWeek, isChecked);
                }
            }
        });
    }

    public void refreshViews(){
        Day d = MainActivity.schedule.get(dayOfWeek);
        tvStartTime.setText(timeFormatter.format(d.getStartTime()));
        tvEndTime.setText(timeFormatter.format(d.getEndTime()));
        dayToggleButton.setChecked(d.isActive());
        tvDayName.setText(dayNameFormatter.format(d.getDate()));
    }

    private void createToast(){

    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }
}
