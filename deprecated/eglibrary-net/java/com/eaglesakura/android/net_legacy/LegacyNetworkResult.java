package com.eaglesakura.android.net_legacy;

import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.Timer;
import com.eaglesakura.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * ネットワークの戻り値を管理する
 */
@Deprecated
public abstract class LegacyNetworkResult<T> {
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

    public LegacyNetworkResult(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public LegacyNetworkResult<T> timeout(long timeoutMs) {
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
     * 受信したhttpヘッダを取得する。
     * <p/>
     * ただし、対応してい場合は空のMapが返却される。
     *
     * @return
     */
    public Map<String, String> getReceivedHeaders() {
        return new HashMap<>();
    }

    /**
     * タイムアウトまでデータ待ちを行う
     *
     * @return
     * @throws IOException
     */
    public T await() throws IOException {
        AndroidThreadUtil.assertBackgroundThread();

        Timer timer = new Timer();

        long lastCheckedDownloadSize = 0;
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

            if (lastCheckedDownloadSize != downloadedDataSize) {
                // ダウンロードに進捗があれば、タイムアウトを引き伸ばす
                timer.start();
            }

            lastCheckedDownloadSize = downloadedDataSize;
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
                        listener.onDataReceived(LegacyNetworkResult.this);
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
                        listener.onError(LegacyNetworkResult.this);
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
        void onDataReceived(LegacyNetworkResult<T> sender);

        /**
         * データを
         *
         * @param sender
         */
        void onError(LegacyNetworkResult<T> sender);
    }

    /**
     * データの受け取りをハンドリングする
     *
     * @param <T>
     */
    public interface Listener2<T> extends Listener<T> {
        void onDownloadProgress(byte[] buffer, int size);
    }
}