package com.eaglesakura.android.thread.async;

import com.eaglesakura.android.thread.ui.UIHandler;

import java.util.ArrayList;
import java.util.List;

public class LifecycledTaskHandler implements ITaskHandler {
    boolean pending = true;

    List<Runnable> requests = new ArrayList<>();

    public LifecycledTaskHandler() {
    }

    public void onPause() {
        pending = true;
    }

    public void onResume() {
        pending = false;
        UIHandler.postUI(runnerImpl);
    }

    /**
     * destroyされた場合、もうリクエストを実行されることはない
     */
    public void onDestroy() {
        pending = true;
        synchronized (requests) {
            requests.clear();
        }
    }

    @Override
    public void request(Runnable runner) {
        synchronized (requests) {
            requests.add(runner);
        }
        UIHandler.postUIorRun(runnerImpl);
    }

    /**
     * 実際のUIスレッド実行を行う
     */
    private Runnable runnerImpl = new Runnable() {
        @Override
        public void run() {
            if (pending) {
                return;
            }

            List<Runnable> tasks;
            synchronized (requests) {
                tasks = new ArrayList<>(requests);
                requests.clear();
            }

            for (Runnable runner : tasks) {
                runner.run();
            }
        }
    };
}
