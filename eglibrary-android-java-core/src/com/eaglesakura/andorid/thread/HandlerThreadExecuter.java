package com.eaglesakura.andorid.thread;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

public class HandlerThreadExecuter {

    List<Runnable> runners = new ArrayList<Runnable>();

    Handler handler = UIHandler.getInstance();

    public HandlerThreadExecuter() {
    }

    public void add(Runnable runnable) {
        runners.add(runnable);
    }
    
    /**
     * 実行スレッドを指定する
     * @param handler
     */
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * キューを実行する
     */
    public void execute() {
        if (runners.isEmpty()) {
            return;
        }
        
        handler.post(new Runnable() {

            @Override
            public void run() {
                for (Runnable r : runners) {
                    r.run();
                }
            }
        });
    }
}
