package com.eaglesakura.android.net;

import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.time.Timer;
import com.eaglesakura.util.Util;

import java.io.IOException;

/**
 * ネットワークの戻り値を管理する
 */
public abstract class NetworkResult<T> {
    protected boolean canceled = false;

    /**
     * 発生した例外
     */
    protected Exception error;

    /**
     * 受け取ったデータを保持する
     */
    protected T receivedData;

    /**
     * データダウンロード待ちのタイムアウト
     * <br>
     * 標準で3分
     */
    protected long dataTimeoutMs = 1000 * 60 * 3;

    protected Listener<T> listener;

    protected final String url;

    /**
     * 古いデータのハッシュ値
     */
    protected String oldDataHash;

    /**
     * 新しいデータのハッシュ
     */
    protected String currentDataHash;

    /**
     * ダウンロード済みのデータサイズ
     */
    protected long downloadedDataSize = 0;

    public NetworkResult(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public NetworkResult<T> timeout(long timeoutMs) {
        this.dataTimeoutMs = timeoutMs;
        return this;
    }

    /**
     * データが変更されている場合trueを返却する
     *
     * @return
     */
    public boolean isDataModified() {
        if (oldDataHash == null) {
            return true;
        } else {
            return !oldDataHash.equals(currentDataHash);
        }
    }

    /**
     * タイムアウトまでデータ待ちを行う
     *
     * @return
     * @throws IOException
     */
    public T await() throws IOException {
        AndroidUtil.assertBackgroundThread();

        Timer timer = new Timer();
        while (receivedData == null) {
            if (error != null) {
                throw new IOException("Volley Error :: " + error.getMessage());
            } else if (canceled) {
                throw new ConnectCanceledException("canceled");
            } else if (timer.end() >= dataTimeoutMs) {
                // 時間切れは強制的に終了させる
                abortRequest();
                throw new IOException("data timeout");
            }
            Util.sleep(10);
        }

        return receivedData;
    }

    public long getDownloadedDataSize() {
        synchronized (this) {
            return downloadedDataSize;
        }
    }

    /**
     * リクエストのキャンセルを行う
     */
    public void cancel() {
        canceled = true;
    }

    /**
     * 既にキャンセル済みであればtrue
     *
     * @return
     */
    public boolean isCanceled() {
        return canceled;
    }

    public T getReceivedData() {
        return receivedData;
    }

    public Exception getError() {
        return error;
    }

    /**
     * データを正常に受け取った
     *
     * @param receivedData
     */
    void onReceived(T receivedData) {
        synchronized (this) {
            this.receivedData = receivedData;
            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    if (listener != null && !isCanceled()) {
                        listener.onDataReceived(NetworkResult.this);
                    }
                }
            });
        }
    }

    void onError(Exception e) {
        synchronized (this) {
            this.error = e;
            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    if (listener != null && !isCanceled()) {
                        listener.onError(NetworkResult.this);
                    }
                }
            });
        }
    }

    /**
     * リスナを設定する
     *
     * @param listener
     */
    public void setListener(Listener<T> listener) {
        synchronized (this) {
            this.listener = listener;

            if (error != null) {
                onError(error);
            } else if (receivedData != null) {
                onReceived(receivedData);
            }
        }
    }

    /**
     * バックグラウンド状態からダウンロードやキャッシュ処理を行わせる
     *
     * @return
     */
    abstract void startDownloadFromBackground();

    /**
     * リクエストをキャンセルする
     */
    abstract void abortRequest();

    /**
     * データの受け取りをハンドリングする
     *
     * @param <T>
     */
    public interface Listener<T> {
        void onDataReceived(NetworkResult<T> sender);

        /**
         * データを
         *
         * @param sender
         */
        void onError(NetworkResult<T> sender);
    }
}
