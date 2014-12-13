package com.eaglesakura.android.framework.support.ui.playservice;

import com.eaglesakura.android.thread.MultiRunningTasks;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Google APIを実行させる
 */
public class GoogleApiTaskRunner {
    MultiRunningTasks tasks;

    final GooleApiClientToken token;

    GoogleApiClient apiClient;

    public GoogleApiTaskRunner(GooleApiClientToken token, int maxTasks) {
        this.token = token;
        tasks = new MultiRunningTasks(maxTasks);
    }

    public void onResume() {
        apiClient = token.lock();
    }

    public void onPause() {
        apiClient = null;
        token.unlock();
    }
}
