package com.meilinger.tj.admin;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;

import server.data.Employee;

/**
 * Created by TJ on 4/3/2018.
 */

public class LateEmployeeNotification {



    private String title = "Late Employees", content = "";
    private Context context;
    private int notificationID = 1473;
    private NotificationCompat.Builder mBuilder;
    private ArrayList<Employee> lateEmployees;

    public LateEmployeeNotification(Context context, ArrayList<Employee> lateEmployees) {
        this.context = context;
        this.lateEmployees = lateEmployees;
        init();
    }

    private void init(){
        PendingIntent pendingIntent = null;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        StringBuilder builder = new StringBuilder();
        for(Employee e : lateEmployees){
            builder.append(" " + e.getFirstName() + " " + e.getLastName());
        }
        content = builder.toString();


        long[] vib = {500, 500, 500};
        mBuilder = new NotificationCompat.Builder(context, NotificationCompat.CATEGORY_MESSAGE)
                .setSmallIcon(R.drawable.icon_valkyrie_master)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setVibrate(vib);
    }

    public void show(){
        NotificationManagerCompat nm = NotificationManagerCompat.from(context);
        nm.notify(notificationID, mBuilder.build());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getNotificationID() {
        return notificationID;
    }

    public void setNotificationID(int notificationID) {
        this.notificationID = notificationID;
    }
}
