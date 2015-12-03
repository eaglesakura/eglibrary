package com.eaglesakura.android.service;

import android.app.Service;
import android.os.IBinder;
import android.os.RemoteException;

import com.eaglesakura.android.service.aidl.ICommandClientCallback;
import com.eaglesakura.android.service.aidl.ICommandServerService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CommandServer {
    private final Service service;

    private final ICommandServerService impl;

    static final int COMMAND_DEFAULT = 0x31033150;

    private Map<String, ServiceClient> clients = new HashMap<>();

    public CommandServer(IBinder binder, Service service) {
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

    class ServerImpl extends ICommandServerService.Stub {
        @Override
        public byte[] postToServer(String cmd, byte[] buffer) throws RemoteException {
            return onReceivedDataFromClient(cmd, buffer);
        }

        @Override
        public void registerCallback(String id, ICommandClientCallback callback) throws RemoteException {
            synchronized (clients) {
                ServiceClient client = new ServiceClient(id, callback);
                clients.put(id, client);
            }
        }

        @Override
        public void unregisterCallback(ICommandClientCallback callback) throws RemoteException {
            synchronized (clients) {
                Iterator<Map.Entry<String, ServiceClient>> iterator = clients.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, ServiceClient> item = iterator.next();
                    if (callback == item.getValue().callback) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    protected byte[] onReceivedDataFromClient(String cmd, byte[] buffer) throws RemoteException {
        return null;
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
