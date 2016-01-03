package com.eaglesakura.android.async;

import com.eaglesakura.android.thread.UIHandler;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AsyncTaskController {
    static final long KEEPALIVE_TIME_MS = 1000 * 15;

    /**
     * 並列実行されるタスクキュー
     * 並列度合いはスレッド数に依存する
     */
    final ThreadPoolExecutor threads;

    /**
     * サブパイプラインに投入されるキュー
     */
    final List<AsyncTaskResult<?>> taskQueue = Collections.synchronizedList(new LinkedList<AsyncTaskResult<?>>());

    boolean disposed;

    ITaskHandler taskHandler = new ITaskHandler() {
        @Override
        public void request(final Runnable runner) {
            UIHandler.postUIorRun(new Runnable() {
                @Override
                public void run() {
                    runner.run();
                }
            });
        }
    };

    public AsyncTaskController(int threads) {
        this.threads = new ThreadPoolExecutor(1, threads, KEEPALIVE_TIME_MS, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    public void setTaskHandler(ITaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    private synchronized <T> AsyncTaskResult<T> pushTask(boolean front, IAsyncTask<T> task) {
        AsyncTaskResult<T> result = new AsyncTaskResult<>(this);
        result.task = task;

        if (front) {
            taskQueue.add(0, result);
        } else {
            taskQueue.add(result);
        }

        // タスクを追加する
        threads.execute(runner);
        return result;
    }

    /**
     * タスクを末尾に追加する
     *
     * @param task
     * @param <T>
     * @return
     */
    public <T> AsyncTaskResult<T> pushBack(IAsyncTask<T> task) {
        return pushTask(false, task);
    }

    /**
     * @param task
     * @return
     */
    public AsyncTaskResult<Object> pushBack(final Runnable task) {
        return pushBack(new IAsyncTask<Object>() {
            @Override
            public Object doInBackground(AsyncTaskController controller) {
                task.run();
                return this;
            }
        });
    }

    /**
     * タスクを先頭に追加する
     *
     * @param task
     * @param <T>
     * @return
     */
    public <T> AsyncTaskResult<T> pushFront(IAsyncTask<T> task) {
        return pushTask(true, task);
    }

    /**
     * タスクを末尾に追加する
     *
     * @param task
     * @return
     */
    public AsyncTaskResult<Object> pushFront(final Runnable task) {
        return pushFront(new IAsyncTask<Object>() {
            @Override
            public Object doInBackground(AsyncTaskController controller) throws Exception {
                task.run();
                return this;
            }
        });
    }

    /**
     * すべての未実行タスクをして資源を解放する
     */
    public void dispose() {
        disposed = true;
        taskQueue.clear();
        threads.shutdown();
    }

    /**
     * タスクを実行する
     */
    private void executeTask() {
        if (taskQueue.isEmpty()) {
            return;
        }

        AsyncTaskResult<?> taskResult = taskQueue.remove(0);
        taskResult.execute();
    }

    final Runnable runner = new Runnable() {
        @Override
        public void run() {
            executeTask();
        }
    };
}
