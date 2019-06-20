package com.meilinger.tj.admin.network;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.meilinger.tj.admin.LateEmployeeNotification;
import com.meilinger.tj.admin.NetworkDataHolder;
import com.meilinger.tj.admin.SharedPreferencesHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import server.data.Employee;

public class ServerService extends Service {
    private static final String TAG = ServerService.class.getName();

    private ServerLateEmployee server = null;
    public static boolean isRunning = false;

    public static final int MSG_REGISTER    = 1;
    public static final int MSG_UNREGISTER  = 0;
    public static final int MSG_SERVER_INFO_UPDATED = 901;

    private Messenger clientMessenger = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private static final int NOTIFY_SERVER_INFO = 901;
    private static final int NOTIFY_LATE_EMPLOYEES = 303;

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(server == null){
            server = new ServerLateEmployee(NetworkDataHolder.getDevicePort());
            new Thread(server).start();
        }
        if(!isRunning){
            isRunning = true;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Server stopped.");
        if(server != null){
            server.endServer();
            server = null;
        }
        if(isRunning){
            isRunning = false;
        }
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_REGISTER:
                    clientMessenger = msg.replyTo;
                    break;
                case MSG_UNREGISTER:
                    clientMessenger = null;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void notifyLateEmployees(@Nullable ArrayList<Employee> lateEmployees){
        if(lateEmployees != null){
            if(lateEmployees.size() > 0){
                Log.d(TAG, "notifyLateEmployees: NOTIFY LATE EMPLOYEES");
                new LateEmployeeNotification(this, lateEmployees).show();
            }
        }
    }

    private void notifyServerInfoUpdated(String serverIP, int serverPort){
        SharedPreferencesHandler prefsHandler = new SharedPreferencesHandler(getApplicationContext());
        prefsHandler.saveServerIp(serverIP);
        prefsHandler.saveServerPort(serverPort);
        Log.i(TAG, "Server network information updated.");

        if(clientMessenger != null){
            Message msg = Message.obtain(null, MSG_SERVER_INFO_UPDATED);
            Bundle data = new Bundle();
            data.putString("serverIP", serverIP);
            data.putInt("serverPort", serverPort);
            msg.setData(data);
            try {
                clientMessenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public class ServerLateEmployee implements Runnable{
        protected int devicePort = 1122;
        protected ServerSocket serverSocket = null;
        protected boolean      isStopped    = false;

        public ServerLateEmployee(int devicePort) {
            this.devicePort = devicePort;
        }

        @Override
        public void run() {
            startServer();
            while(!isStopped){
                Socket receivingSocket = null;
                try {
                    logd("Starting connection...");
                    receivingSocket = serverSocket.accept();
                    ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(receivingSocket.getOutputStream()));
                    output.flush();
                    ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(receivingSocket.getInputStream()));

                    int request = input.readInt();
                    switch (request){
                        case NOTIFY_LATE_EMPLOYEES:
                            ArrayList<Employee> lateEmployees = null;
                            try {
                                lateEmployees = (ArrayList<Employee>) input.readObject();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            notifyLateEmployees(lateEmployees);
                            break;
                        case NOTIFY_SERVER_INFO:
                            String serverIP = input.readUTF();
                            int serverPort = input.readInt();
                            notifyServerInfoUpdated(serverIP, serverPort);
                            break;
                    }
                    input.close();
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(receivingSocket != null){
                        try {
                            receivingSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            logd("Server stopped.");
            endServer();
        }
        private void startServer(){
            try {
                serverSocket = new ServerSocket(this.devicePort);
                isStopped = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void endServer(){
            if(serverSocket != null){
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void logd(String s){
        Log.d(TAG, s);
    }
}
