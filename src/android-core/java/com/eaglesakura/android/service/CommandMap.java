package com.eaglesakura.android.service;

import android.annotation.SuppressLint;
import android.os.RemoteException;

import com.eaglesakura.android.db.BaseProperties;
import com.eaglesakura.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

public class CommandMap {

    private Map<String, Action> actions = new HashMap<>();

    public void addAction(String cmd, Action action) {
        synchronized (this) {
            actions.put(cmd, action);
        }
    }

    @SuppressLint("NewApi")
    public byte[] execute(Object sender, String cmd, byte[] buffer) throws RemoteException {
        Action action;
        synchronized (this) {
            action = actions.get(cmd);
        }

        try {
            if (action != null) {
                return action.execute(sender, cmd, buffer);
            } else {
                return null;
            }
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            LogUtil.log(e);
            throw new RemoteException(e.getMessage());
        }
    }

    public interface Action {
        byte[] execute(Object sender, String cmd, byte[] buffer) throws Exception;
    }
}
