package com.meilinger.tj.admin;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.meilinger.tj.admin.network.ClientAsyncTask;
import com.meilinger.tj.admin.network.ScheduleAsyncTask;
import com.meilinger.tj.admin.network.ServerService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import server.data.Day;

public class MainActivity extends AppCompatActivity implements ClientAsyncTask.AsyncResponseListener, AddEmployeeDialogFragment.AddEmployeeListener, ScheduleAsyncTask.ScheduleAsyncTaskListener{

    private LinearLayout widgetsLinearLayout;
    private Button addEmployeeButton, updateButton;

    public static int currentDayOfWeek;

    private final String key_schedule = "key_schedule", key_prev_started = "key_prev_started";
    public static ArrayList<Day> schedule;
    private ArrayList<DayView> dayViews;

    private boolean mBound = false;
    private Messenger mService = null;
    private final Messenger mMessenger = new Messenger(new IncomingHandler());
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = new Messenger(service);
            mBound = true;

            Message msg = Message.obtain(null, ServerService.MSG_REGISTER);
            msg.replyTo = mMessenger;

            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Message msg = Message.obtain(null, ServerService.MSG_UNREGISTER);
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null){
            schedule = (ArrayList<Day>) savedInstanceState.getSerializable(key_schedule);
        } else {
            initSchedule();
        }

        Calendar c = Calendar.getInstance();
        c.setFirstDayOfWeek(Calendar.WEDNESDAY);
        currentDayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        ((ImageButton) findViewById(R.id.imgBtnSettings)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        initViews();

        checkPrevStarted();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLateEmployeeService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound){
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopService(new Intent(MainActivity.this, ServerService.class));
    }

    private void checkPrevStarted(){
        SharedPreferencesHandler prefsHandler = new SharedPreferencesHandler(getApplicationContext());
        if(!prefsHandler.getPreviouslyStarted()){
            prefsHandler.saveServerIp(NetworkDataHolder.getServerIP());
            prefsHandler.saveServerPort(NetworkDataHolder.getServerPort());
            new ClientAsyncTask(ClientAsyncTask.REGISTER_SCHEDULER_DEVICE).execute();
        } else {
            NetworkDataHolder.setServerIP(prefsHandler.getServerIP());
            NetworkDataHolder.setServerPort(prefsHandler.getServerPort());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(key_schedule, schedule);
    }

    private void startLateEmployeeService(){
        Intent intent = new Intent(MainActivity.this, ServerService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_IMPORTANT);
    }

    private void initSchedule(){
        if(schedule == null){
            schedule = new ArrayList<>();
            for(int i = 0; i < 7; i++){
                Calendar c = Calendar.getInstance();
                c.setFirstDayOfWeek(Calendar.WEDNESDAY);
                c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                c.add(Calendar.DAY_OF_WEEK, i);
                Day d = new Day(c.getTime(), true);
                d.setDefaultTimes();
                schedule.add(d);
            }
        }
    }

    private void getScheduleFromServer(){
        log("Requesting schedule from server...");
        ScheduleAsyncTask scheduleAsyncTask = new ScheduleAsyncTask();
        scheduleAsyncTask.delegate = this;
        scheduleAsyncTask.execute();
    }

    private void initViews(){
        widgetsLinearLayout = (LinearLayout) findViewById(R.id.widgetsLinearLayout);
        addEmployeeButton = (Button) findViewById(R.id.addEmployeeButton);
        updateButton = (Button) findViewById(R.id.updateButton);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lp.weight = 1;

        dayViews = new ArrayList<>();
        for (int i = 0; i < schedule.size(); i++){
            LinearLayout l = new LinearLayout(getApplicationContext());
            l.setLayoutParams(lp);
            DayView dv = new DayView(this, l, i);
            dv.setDayViewListener(new DayViewListener() {
                @Override
                public void onActiveChanged(int dayOfWeek, boolean isActive) {
                    log("Active Changed:\t" + dayOfWeek + "\t" + isActive);
                    schedule.get(dayOfWeek).setActive(isActive);
                }

                @Override
                public void onStartTimeChanged(int dayOfWeek, Date startTime) {
                    log("Start Time Changed:\t" + dayOfWeek + "\t" + startTime.toString());
                    schedule.get(dayOfWeek).setStartTime(startTime);
                }

                @Override
                public void onEndTimeChanged(int dayOfWeek, Date endTime) {
                    log("End Time Changed:\t" + dayOfWeek + "\t" + endTime.toString());
                    schedule.get(dayOfWeek).setEndTime(endTime);
                }
            });
            dayViews.add(dv);
            widgetsLinearLayout.addView(l);
        }
        refreshViews();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientAsyncTask clientAsyncTask = new ClientAsyncTask(ClientAsyncTask.UPDATE_SCHEDULE);
                clientAsyncTask.delegate = MainActivity.this;
                clientAsyncTask.execute();
            }
        });

        addEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("AddEmployeeDialog");
                if(prev != null){
                    ft.remove(prev);
                }

                AddEmployeeDialogFragment frag = AddEmployeeDialogFragment.newInstance();
                frag.show(ft, "AddEmployeeDialog");
            }
        });

    }
    private void refreshViews(){
        for (DayView dv : dayViews){
            dv.refreshViews();
        }
    }

    public static void printSchedule(int day){
        Day d = schedule.get(day);
        SimpleDateFormat f = new SimpleDateFormat("h:mm.ss a");
        System.out.println("****** " + day + " ******");
        System.out.println(f.format(d.getStartTime()));
        System.out.println(f.format(d.getEndTime()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        getScheduleFromServer();
    }

    @Override
    public void connectionStatus(String status) {
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onScheduleRecieved(ArrayList<Day> schedule) {
        if(schedule != null){
            createToast("Schedule received.");
            MainActivity.schedule = schedule;
            refreshViews();
        } else {
            createToast("Could not connect to server.");
        }

    }

    @Override
    public void addEmployee(String firstName, String lastName) {
        System.out.println(firstName + " " + lastName);
        ClientAsyncTask clientAsyncTask = new ClientAsyncTask(ClientAsyncTask.ADD_EMPLOYEE);
        clientAsyncTask.delegate = this;
        clientAsyncTask.execute(firstName, lastName);
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ServerService.MSG_SERVER_INFO_UPDATED:
                    createToast("Server information updated.");
                    NetworkDataHolder.setServerIP(msg.getData().getString("serverIP"));
                    NetworkDataHolder.setServerPort(msg.getData().getInt("serverPort"));
                    break;
            }
        }
    }

    private void createToast(String content){
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    private static void log(String log){
        System.out.println(log);
    }
}
