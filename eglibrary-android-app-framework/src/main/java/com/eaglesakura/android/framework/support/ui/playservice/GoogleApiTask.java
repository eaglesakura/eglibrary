package com.eaglesakura.android.framework.support.ui.playservice;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Google Api実行タスク
 */
public interface GoogleApiTask<T> {
    /**
     * タスクを実行する
     *
     * @param lockedToken
     * @param client
     * @throws Exception
     */
    T executeTask(GooleApiClientToken lockedToken, GoogleApiClient client) throws Exception;

    /**
     * コネクションに失敗した
     *
     * @param lockedToken
     * @param client
     */
    T connectedFailed(GooleApiClientToken lockedToken, GoogleApiClient client);

    /**
     * キャンセル済みであればtrue
     *
     * @return
     */
    boolean isCanceled();
}
