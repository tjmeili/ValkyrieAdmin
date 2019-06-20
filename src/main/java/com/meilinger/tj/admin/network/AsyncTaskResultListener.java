package com.meilinger.tj.admin.network;

public interface AsyncTaskResultListener {
    enum Result {
        SUCCESS, FAILED;
    }
    void onProcessFinished(Result result);
}
