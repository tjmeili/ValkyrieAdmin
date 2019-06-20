package com.meilinger.tj.admin.network;

import android.os.AsyncTask;

import com.meilinger.tj.admin.NetworkDataHolder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import server.data.Day;

public class ScheduleAsyncTask extends AsyncTask<Void, Void, ArrayList<Day>> {

    private int port = 9696;
    private String host;
    private Socket socket;

    private static final int REQUEST_SCHEDULE           = 107;

    public ScheduleAsyncTask() {
        this.port = NetworkDataHolder.getServerPort();
        this.host = NetworkDataHolder.getServerIP();
    }

    public interface ScheduleAsyncTaskListener {
        void onScheduleRecieved(ArrayList<Day> schedule);
    }

    public ScheduleAsyncTaskListener delegate = null;

    @Override
    protected ArrayList<Day> doInBackground(Void... voids) {
        ArrayList<Day> schedule = null;

        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), 5000);
            System.out.println("Opening Streams...");
            ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            output.flush();
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            output.writeInt(REQUEST_SCHEDULE);
            output.flush();
            try {
                schedule = (ArrayList<Day>) input.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            input.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return schedule;
    }

    @Override
    protected void onPostExecute(ArrayList<Day> schedule) {
        super.onPostExecute(schedule);
        if(delegate != null){
            delegate.onScheduleRecieved(schedule);
        }
    }
}
