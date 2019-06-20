package com.meilinger.tj.admin.network;

import android.os.AsyncTask;
import android.util.Log;

import com.meilinger.tj.admin.MainActivity;
import com.meilinger.tj.admin.NetworkDataHolder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by TJ on 3/14/2018.
 */

public class ClientAsyncTask extends AsyncTask<String, Void, String>{
    private static final String TAG = ClientAsyncTask.class.getName();
    private int port = 9696;
    private Socket socket;
    private String host;
    private int request = -1;

    public static final int REGISTER_SCHEDULER_DEVICE  = 102;
    public static final int UPDATE_SCHEDULE            = 201;
    public static final int ADD_EMPLOYEE               = 202;

    public interface AsyncResponseListener {
        void connectionStatus(String status);
    }

    public AsyncResponseListener delegate = null;

    public ClientAsyncTask(int request) {
        this.port = NetworkDataHolder.getServerPort();
        this.host = NetworkDataHolder.getServerIP();
        this.request = request;
    }

    @Override
    protected String doInBackground(String[] strings) {

        String result = "";
        try{
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            System.out.println("Opening Streams...");
            ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            output.flush();
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            switch (request){
                case UPDATE_SCHEDULE:
                    output.writeInt(UPDATE_SCHEDULE);
                    output.flush();
                    output.writeObject(MainActivity.schedule);
                    output.flush();
                    result = "Schedule updated.";
                    break;
                case ADD_EMPLOYEE:
                    Log.d(TAG, "doInBackground: Sending employee.");
                    String firstName = (String) strings[0];
                    String lastName = (String) strings[1];
                    output.writeInt(ADD_EMPLOYEE);
                    output.flush();
                    output.writeUTF(firstName);
                    output.flush();
                    output.writeUTF(lastName);
                    output.flush();
                    Log.d(TAG, "doInBackground: Employee Sent.");
                    result = "Employee added.";
                    break;
                case REGISTER_SCHEDULER_DEVICE:
                    output.writeInt(REGISTER_SCHEDULER_DEVICE);
                    output.flush();
                    output.writeInt(NetworkDataHolder.getDevicePort());
                    output.flush();
                    break;
            }

            input.close();
            output.close();
        } catch(IOException e){
            result = "Could not connect to server.";
            e.printStackTrace();
        } finally{
            if(socket != null){
                try{
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if(delegate != null){
            delegate.connectionStatus(result);
        }
    }

}
