package com.eaglesakura.android.service;

import android.app.Service;
import android.os.IBinder;
import android.os.RemoteException;

import com.eaglesakura.android.service.aidl.ICommandClientCallback;
import com.eaglesakura.android.service.aidl.ICommandServerService;
import com.eaglesakura.android.thread.ui.UIHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CommandServer {
    private final Service service;

    private final ICommandServerService impl;

    private Map<String, ServiceClient> clients = new HashMap<>();

    public static final byte[] CLIENT_NOT_FOUND = new byte[0];

    public CommandServer(Service service) {
        this.service = service;
        this.impl = new ServerImpl();
    }

    /**
     * Serviceの実体を返す
     *
     * @return
     */
    public IBinder getBinder() {
        if (impl instanceof ICommandServerService.Stub) {
            return (ICommandServerService.Stub) impl;
        } else {
            return null;
        }
    }


    /**
     * 指定したクライアントに接続されていればtrue
     *
     * @param id
     * @return
     */
    public boolean hasClient(String id) {
        synchronized (clients) {
            return clients.containsKey(id);
        }
    }

    class ServerImpl extends ICommandServerService.Stub {
        @Override
        public byte[] postToServer(String cmd, byte[] buffer) throws RemoteException {
            return onReceivedDataFromClient(cmd, buffer);
        }

        @Override
        public void registerCallback(final String id, final ICommandClientCallback callback) throws RemoteException {
            synchronized (clients) {
                ServiceClient client = new ServiceClient(id, callback);
                clients.put(id, client);
            }

            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    onRegisterClient(id, callback);
                }
            });
        }

        @Override
        public void unregisterCallback(ICommandClientCallback callback) throws RemoteException {
            final List<String> idList = new ArrayList<>();
            synchronized (clients) {
                Iterator<Map.Entry<String, ServiceClient>> iterator = clients.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, ServiceClient> item = iterator.next();
                    if (callback == item.getValue().callback) {
                        idList.add(item.getKey());
                        iterator.remove();
                    }
                }
            }

            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    for (String id : idList) {
                        onUnregisterClient(id);
                    }
                }
            });

        }
    }

    /**
     * コールバック登録が行われた
     *
     * @param id
     * @param callback
     */
    protected void onRegisterClient(String id, ICommandClientCallback callback) {

    }

    /**
     * コールバックが削除された
     *
     * @param id
     */
    protected void onUnregisterClient(String id) {

    }

    protected byte[] postToClient(String id, String cmd, byte[] buffer) throws RemoteException {
        ServiceClient client;
        synchronized (clients) {
            client = clients.get(id);
        }

        if (client != null) {
            return client.callback.postToClient(cmd, buffer);
        } else {
            return CLIENT_NOT_FOUND;
        }
    }

    /**
     * データを送信し、戻り値は無視する
     *
     * @param cmd
     * @param buffer
     * @throws RemoteException
     */
    protected void broadcastToClientNoResults(String cmd, byte[] buffer) throws RemoteException {
        Map<String, ServiceClient> clients;

        synchronized (this.clients) {
            clients = new HashMap<>(this.clients);
        }

        Iterator<Map.Entry<String, ServiceClient>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ServiceClient> entry = iterator.next();
            entry.getValue().callback.postToClient(cmd, buffer);
        }
    }


    /**
     * データを送信し、戻り値一覧を取得する
     *
     * @param cmd
     * @param buffer
     * @return
     * @throws RemoteException
     */
    protected void broadcastToClient(String cmd, byte[] buffer, ClientResultCallback callback) throws RemoteException {
        Map<String, ServiceClient> clients;

        synchronized (this.clients) {
            clients = new HashMap<>(this.clients);
        }

        Iterator<Map.Entry<String, ServiceClient>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ServiceClient> entry = iterator.next();
            ServiceClient client = entry.getValue();
            byte[] clientResult = client.callback.postToClient(cmd, buffer);
            callback.onClientExecuted(client.id, client.callback, cmd, clientResult);
        }
    }

    /**
     * データを送信し、戻り値一覧を取得する
     *
     * @param cmd
     * @param buffer
     * @return
     * @throws RemoteException
     */
    protected Map<String, byte[]> broadcastToClient(String cmd, byte[] buffer) throws RemoteException {
        Map<String, ServiceClient> clients;

        synchronized (this.clients) {
            clients = new HashMap<>(this.clients);
        }

        Map<String, byte[]> results = new HashMap<>();
        Iterator<Map.Entry<String, ServiceClient>> iterator = clients.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ServiceClient> entry = iterator.next();
            results.put(entry.getKey(), entry.getValue().callback.postToClient(cmd, buffer));
        }

        return results;
    }

    protected byte[] onReceivedDataFromClient(String cmd, byte[] buffer) throws RemoteException {
        return null;
    }

    public interface ClientResultCallback {
        void onClientExecuted(String id, ICommandClientCallback client, String cmd, byte[] result);
    }

    class ServiceClient {
        final String id;
        final ICommandClientCallback callback;

        public ServiceClient(String id, ICommandClientCallback callback) {
            this.id = id;
            this.callback = callback;
        }
    }
}
