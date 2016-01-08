package com.eaglesakura.android.bluetooth.p2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.zip.DataFormatException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.eaglesakura.android.thread.async.AsyncAction;
import com.eaglesakura.android.thread.async.ThreadSyncRunnerBase;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.io.data.DataPackage;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

/**
 * P2Pコネクタの基底クラス
 * 処理を書いておく
 */
public abstract class BluetoothP2PConnector {

    //    protected static final UUID PROTOCOL_UUID = BluetoothLeUtil.createUUIDFromAssignedNumber("0x3103");

    UUID protocolUUID;

    /**
     * リスナ一覧
     */
    List<P2PConnectorListener> listeners = new ArrayList<>();

    /**
     * 接続対象デバイス
     */
    protected BluetoothDevice connectDevice;

    /**
     * 切断リクエスト中
     */
    private boolean requestDisconnect = false;

    ConnectorState inputState = ConnectorState.Stop;

    ConnectorState outputState = ConnectorState.Stop;

    boolean connecting = false;

    public enum ConnectorType {
        /**
         * 受信
         */
        Input,

        /**
         * 送信
         */
        Output,
    }

    public enum ConnectorState {
        /**
         * 停止中
         */
        Stop,

        /**
         * 接続待ち
         */
        Search,

        /**
         * 接続中
         */
        Connecting,

        /**
         * 接続完了
         */
        Connected,

        /**
         * 切断中
         */
        Disconnecting,

        /**
         * 切断完了
         */
        Disconnected,

        /**
         * 接続に失敗した
         */
        Failed,
    }

    /**
     * 送信受信時のスレッド待機時間
     */
    private long threadWaitTimeMs = 10;

    /**
     * 送受信のタイムアウト時間
     */
    private long connectorTimeoutMs = 1000 * 10;

    /**
     * ロックオブジェクト
     */
    protected final Object lock = new Object();

    /**
     * データ送信に使用するlock
     */
    protected final Object sendLock = new Object();

    /**
     * 送信データ一覧
     */
    protected final List<DataPackage> sendDataQueue = new ArrayList<DataPackage>();

    protected Thread inputThread;

    protected Thread outputThread;

    /**
     * 送受信時のスレッド待機時間を指定する
     *
     * @param threadWaitTimeMs
     */
    public void setThreadWaitTimeMs(long threadWaitTimeMs) {
        this.threadWaitTimeMs = threadWaitTimeMs;
    }

    /**
     * 送受信のタイムアウト時間を指定する
     *
     * @param connectorTimeoutMs
     */
    public void setConnectorTimeoutMs(long connectorTimeoutMs) {
        this.connectorTimeoutMs = connectorTimeoutMs;
    }

    public long getConnectorTimeoutMs() {
        return connectorTimeoutMs;
    }

    public boolean isRequestDisconnect() {
        return requestDisconnect;
    }

    /**
     * リスナを追加する
     *
     * @param listener
     */
    public void addListener(P2PConnectorListener listener) {
        Util.addUnique(listeners, listener);
    }

    public void removeListener(P2PConnectorListener listener) {
        listeners.remove(listener);
    }

    /**
     * 処理を開始する
     */
    public synchronized final void start(final UUID protocol) {
        if (connecting) {
            // 既に接続リクエストが発行されているので、何もしない
            return;
        }

        requestDisconnect = false;
        connecting = true;

        // 制御Threadを立てる
        new AsyncAction("BluetoothP2P-Ctrl") {
            @Override
            protected Object onBackgroundAction() throws Exception {

                if (!requestConnecting(protocol)) {
                    // 正常に接続できなかった
                    throw new IllegalStateException("P2P Connect Failed");
                }

                boolean connectedHandling = false;

                // Input/Outputが正常に動いていることを確認する
                while (!requestDisconnect && inputState != ConnectorState.Disconnecting && outputState != ConnectorState.Disconnecting) {
                    if (!connectedHandling && inputState == ConnectorState.Connected && outputState == ConnectorState.Connected) {
                        connectedHandling = true;
                        // ハンドリングを行う
                        new ThreadSyncRunnerBase<Void>(UIHandler.getInstance()) {
                            @Override
                            public Void onOtherThreadRun() throws Exception {
                                for (P2PConnectorListener listener : listeners) {
                                    listener.onConnectorStateChanged(BluetoothP2PConnector.this, ConnectorState.Connected);
                                }
                                return null;
                            }
                        }.run();
                    }

                    // 精度は適当
                    Util.sleep(threadWaitTimeMs);
                }

                LogUtil.log("Disconnecting input(%s) output(%s)", inputState.toString(), outputState.toString());

                // どちらかがdisconnectingになったら、Threadを停止させる
                requestDisconnect = true;
                // Socketの停止を行わせる
                requestDisconnecting();

                LogUtil.log("requestDisconnecting() finished");

                // input/outputがrequestDisconnectによって自然停止するのを待つ
                if (inputThread != null) {
                    try {
                        inputThread.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    inputThread = null;
                }

                LogUtil.log("inputThread / join");

                if (outputThread != null) {
                    try {
                        outputThread.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    outputThread = null;
                }

                LogUtil.log("outputThread / join");

                return null;
            }

            @Override
            protected void onFinalize() {
                super.onFinalize();
                inputState = ConnectorState.Stop;
                outputState = ConnectorState.Stop;
                connecting = false;
            }

            @Override
            protected void onSuccess(Object object) {
                for (P2PConnectorListener listener : listeners) {
                    listener.onConnectorStateChanged(BluetoothP2PConnector.this, ConnectorState.Disconnected);
                }
            }

            @Override
            protected void onFailure(Exception exception) {
                for (P2PConnectorListener listener : listeners) {
                    listener.onConnectorStateChanged(BluetoothP2PConnector.this, ConnectorState.Failed);
                }
            }
        }.start();
    }

    /**
     * 処理を停止する
     * 同期的には止まらず、コールバックに {@link ConnectorState#Disconnected} が来るのを待つ必要がある
     */
    public final void stop() {
        requestDisconnect = true;
    }

    /**
     * 接続をリクエストする
     */
    protected abstract boolean requestConnecting(UUID protocol);

    /**
     * 切断をリクエストする
     */
    protected abstract void requestDisconnecting();

    /**
     * 受信処理用スレッドを開始する
     *
     * @param socket
     */
    protected void startInputThread(final BluetoothSocket socket) {
        synchronized (lock) {
            if (inputThread != null) {
                return;
            }

            inputThread = new AsyncAction("BluetoothP2P-Input") {
                @Override
                protected Object onBackgroundAction() throws Exception {
                    inputState = ConnectorState.Connecting;
                    InputStream stream = null;
                    try {
                        stream = socket.getInputStream();
                        inputState = ConnectorState.Connected;

                        while (!requestDisconnect) {
                            // データパックを受信する
                            while (stream.available() > 0) {
                                try {
                                    byte[] buffer = DataPackage.unpack(stream, connectorTimeoutMs);
                                    synchronized (lock) {
                                        for (P2PConnectorListener listener : listeners) {
                                            listener.onDataReceived(BluetoothP2PConnector.this, buffer);
                                        }
                                    }
                                } catch (DataFormatException e) {
                                    LogUtil.d("drop packets");
                                    e.printStackTrace();
                                }
                            }
                            // sleep
                            Util.sleep(threadWaitTimeMs);
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        inputState = ConnectorState.Disconnecting;
                    }
                    return null;
                }

                @Override
                protected void onSuccess(Object object) {

                }

                @Override
                protected void onFailure(Exception exception) {

                }
            };
            inputThread.start();
        }
    }

    /**
     * 送信処理用スレッドを開始する
     *
     * @param socket
     */
    protected void startOutputThread(final BluetoothSocket socket) {

        synchronized (lock) {
            if (outputThread != null) {
                return;
            }

            outputThread = new AsyncAction() {
                @Override
                protected Object onBackgroundAction() throws Exception {
                    outputState = ConnectorState.Connecting;

                    OutputStream stream = null;
                    try {
                        stream = socket.getOutputStream();
                        outputState = ConnectorState.Connected;

                        while (!requestDisconnect) {
                            // データパックを送信する
                            DataPackage data;

                            int numWriteBytes = 0;
                            // 適当なbyte数ごとに送信する
                            while ((data = popSendData()) != null && numWriteBytes < (512 * 1024)) {
                                byte[] buffer = data.getPackedBuffer();
                                stream.write(buffer);
                                numWriteBytes += buffer.length;

                                // 送信通知を行う
                                synchronized (lock) {
                                    for (P2PConnectorListener listener : listeners) {
                                        listener.onDataSended(BluetoothP2PConnector.this, data);
                                    }
                                }
                            }
                            if (numWriteBytes > 0) {
                                // 必要であればflushする
                                stream.flush();
                            }

                            // sleep
                            Util.sleep(threadWaitTimeMs);
                        }

                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    outputState = ConnectorState.Disconnecting;
                    return null;
                }

                @Override
                protected void onSuccess(Object object) {

                }

                @Override
                protected void onFailure(Exception exception) {

                }
            };
            outputThread.start();
        }
    }

    /**
     * 送信データを一つ取得する
     *
     * @return
     */
    public DataPackage popSendData() {
        synchronized (sendLock) {
            if (sendDataQueue.isEmpty()) {
                return null;
            }
            return sendDataQueue.remove(0);
        }
    }

    /**
     * データの送信リクエストを行う
     *
     * @param pack
     */
    public void requestSendData(DataPackage pack) {
        synchronized (sendLock) {
            sendDataQueue.add(pack);
        }
    }

    /**
     * 送信リクエストを削除する
     *
     * @param uniqueId
     */
    public void removeSendRequest(String uniqueId) {
        synchronized (sendLock) {
            Iterator<DataPackage> iterator = sendDataQueue.iterator();
            while (iterator.hasNext()) {
                DataPackage dataPackage = iterator.next();
                if (dataPackage.getUniqueId().equals(uniqueId)) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 送信リクエストをクリアーする
     */
    public void clearSendRequest() {
        synchronized (sendLock) {
            sendDataQueue.clear();
        }
    }

    /**
     * 指定個数のキャッシュを超えた場合にクリアしてしまう
     *
     * @param maxRequest
     */
    public void clearSendRequestOver(int maxRequest) {
        synchronized (sendLock) {
            while (sendDataQueue.size() > maxRequest) {
                sendDataQueue.remove(0);
            }
        }
    }

    public BluetoothDevice getConnectDevice() {
        return connectDevice;
    }

    /**
     *
     */
    public interface P2PConnectorListener {
        /**
         * データを受信した
         *
         * @param self
         * @param buffer
         */
        void onDataReceived(BluetoothP2PConnector self, byte[] buffer);

        /**
         * データの送信が完了した
         *
         * @param self
         * @param pack
         */
        void onDataSended(BluetoothP2PConnector self, DataPackage pack);

        /**
         * ステートが変更された
         *
         * @param self
         * @param state
         */
        void onConnectorStateChanged(BluetoothP2PConnector self, ConnectorState state);
    }
}
