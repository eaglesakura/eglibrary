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
    public final void start(final UUID protocol) {
        requestDisconnect = false;

        new Thread() {
            @Override
            public void run() {
                requestConnecting(protocol);
            }
        }.start();
    }

    /**
     * 処理を停止する
     * 同期的には止まらず、コールバックに {@link ConnectorState#Disconnected} が来るのを待つ必要がある
     */
    public final void stop() {
        synchronized (lock) {
            if (requestDisconnect) {
                return;
            }
            requestDisconnect = true;
        }

        new Thread() {
            @Override
            public void run() {
                if (inputThread != null) {
                    try {
                        inputThread.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    inputThread = null;
                }

                if (outputThread != null) {
                    try {
                        outputThread.join();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    outputThread = null;
                }

                requestDisconnecting();

                synchronized (lock) {
                    for (P2PConnectorListener listener : listeners) {
                        listener.onConnectorStateChanged(BluetoothP2PConnector.this, ConnectorType.Output, ConnectorState.Disconnected);
                        listener.onConnectorStateChanged(BluetoothP2PConnector.this, ConnectorType.Input, ConnectorState.Disconnected);
                        listener.onConnectorStateChanged(BluetoothP2PConnector.this, null, ConnectorState.Disconnected);
                    }
                }
            }
        }.start();
    }

    /**
     * 接続をリクエストする
     */
    protected abstract void requestConnecting(UUID protocol);

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

            inputThread = new Thread() {
                @Override
                public void run() {
                    synchronized (lock) {
                        for (P2PConnectorListener listener : listeners) {
                            listener.onConnectorStateChanged(BluetoothP2PConnector.this, ConnectorType.Input, ConnectorState.Connecting);
                        }
                    }

                    InputStream stream = null;
                    try {
                        stream = socket.getInputStream();
                        synchronized (lock) {
                            for (P2PConnectorListener listener : listeners) {
                                listener.onConnectorStateChanged(BluetoothP2PConnector.this, ConnectorType.Input, ConnectorState.Connected);
                            }
                        }

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
                    }

                    synchronized (lock) {
                        for (P2PConnectorListener listener : listeners) {
                            listener.onConnectorStateChanged(BluetoothP2PConnector.this, ConnectorType.Input, ConnectorState.Disconnecting);
                        }
                    }

                    BluetoothP2PConnector.this.stop();
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

            outputThread = new Thread() {
                @Override
                public void run() {
                    synchronized (lock) {
                        for (P2PConnectorListener listener : listeners) {
                            listener.onConnectorStateChanged(BluetoothP2PConnector.this, ConnectorType.Output, ConnectorState.Connecting);
                        }
                    }

                    OutputStream stream = null;
                    try {
                        stream = socket.getOutputStream();
                        synchronized (lock) {
                            for (P2PConnectorListener listener : listeners) {
                                listener.onConnectorStateChanged(BluetoothP2PConnector.this, ConnectorType.Output, ConnectorState.Connected);
                            }
                        }

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

                    synchronized (lock) {
                        for (P2PConnectorListener listener : listeners) {
                            listener.onConnectorStateChanged(BluetoothP2PConnector.this, ConnectorType.Output, ConnectorState.Disconnecting);
                        }
                    }

                    BluetoothP2PConnector.this.stop();
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
        void onConnectorStateChanged(BluetoothP2PConnector self, ConnectorType type, ConnectorState state);
    }
}
