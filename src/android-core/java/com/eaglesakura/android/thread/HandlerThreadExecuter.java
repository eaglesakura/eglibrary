package com.eaglesakura.android.thread;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

import com.eaglesakura.android.thread.ui.UIHandler;

public class HandlerThreadExecuter {

    List<Runnable> runners = new ArrayList<Runnable>();

    Handler handler = UIHandler.getInstance();

    public HandlerThreadExecuter() {
    }

    public void add(Runnable runnable) {
        synchronized (runners) {
            runners.add(runnable);
        }
    }

    /**
     * 実行スレッドを指定する
     *
     * @param handler
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Handler getHandler() {
        return handler;
    }

    /**
     * キューを実行する
     */
    public void execute() {
        synchronized (runners) {
            if (runners.isEmpty()) {
                return;
            }
        }

        handler.post(new Runnable() {

            @Override
            public void run() {
                synchronized (runners) {
                    for (Runnable r : runners) {
                        r.run();
                    }
                    runners.clear();
                }
            }
        });
    }
}
