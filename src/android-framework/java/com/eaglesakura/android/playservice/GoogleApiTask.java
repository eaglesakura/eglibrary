package com.eaglesakura.android.playservice;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Google Api実行タスク
 */
public interface GoogleApiTask<T> {
    /**
     * タスクを実行する
     *
     * @param client
     * @throws Exception
     */
    T executeTask(GoogleApiClient client) throws Exception;

    /**
     * コネクションに失敗した
     */
    T connectedFailed(GoogleApiClient client, ConnectionResult connectionResult);

    /**
     * キャンセル済みであればtrue
     *
     * @return
     */
    boolean isCanceled();
}
