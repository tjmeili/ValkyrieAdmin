package com.meilinger.tj.admin.network;

import android.os.AsyncTask;
import android.util.Log;


import com.meilinger.tj.admin.NetworkDataHolder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TestConnectionAsyncTask extends AsyncTask<Void, Void, AsyncTaskResultListener.Result> {
    private static final String TAG = TestConnectionAsyncTask.class.getName();

    private AsyncTaskResultListener asyncTaskResultListener = null;
    private Socket socket;
    @Override
    protected AsyncTaskResultListener.Result doInBackground(Void... voids) {
        AsyncTaskResultListener.Result result;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(NetworkDataHolder.getServerIP(), NetworkDataHolder.getServerPort()), 10000);
            Log.i(TAG, "doInBackground: Testing connection to server...");
            Log.d(TAG, "doInBackground: " + NetworkDataHolder.getServerIP());
            ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            output.flush();
            ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            output.writeInt(0);
            output.flush();

            output.close();
            input.close();
            result = AsyncTaskResultListener.Result.SUCCESS;
            Log.i(TAG, "doInBackground: Connection successful.");
        } catch (IOException e) {
            result = AsyncTaskResultListener.Result.FAILED;
            Log.e(TAG, "doInBackground: Error connecting to server.", e);
        } finally {
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(AsyncTaskResultListener.Result result) {
        super.onPostExecute(result);
        if(asyncTaskResultListener != null){
            asyncTaskResultListener.onProcessFinished(result);
        }
    }

    public AsyncTaskResultListener getAsyncTaskResultListener() {
        return asyncTaskResultListener;
    }

    public void setAsyncTaskResultListener(AsyncTaskResultListener asyncTaskResultListener) {
        this.asyncTaskResultListener = asyncTaskResultListener;
    }

}
