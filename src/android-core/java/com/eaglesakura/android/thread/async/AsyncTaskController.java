package com.eaglesakura.android.thread.async;

import com.eaglesakura.android.thread.ui.UIHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AsyncTaskController {
    final long KEEPALIVE_TIME_MS;

    /**
     * 並列実行されるタスクキュー
     * 並列度合いはスレッド数に依存する
     */
    final ThreadPoolExecutor threads;

    /**
     * 実行待ちのキュー
     */
    final List<AsyncTaskResult<?>> taskQueue = Collections.synchronizedList(new LinkedList<AsyncTaskResult<?>>());

    /**
     * 実行中
     */
    final List<AsyncTaskResult<?>> runningTasks = Collections.synchronizedList(new LinkedList<AsyncTaskResult<?>>());

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

    /**
     * スレッド数を指定してコントローラを生成する
     * <p/>
     * 標準では15秒のスレッドキープを行う
     *
     * @param threads
     */
    public AsyncTaskController(int threads) {
        this(threads, 1000 * 15);
    }

    /**
     * スレッド数とキープ時間を指定してコントローラを生成する
     *
     * @param threads
     * @param keepAliveTimeMs
     */
    public AsyncTaskController(int threads, long keepAliveTimeMs) {
        this.KEEPALIVE_TIME_MS = keepAliveTimeMs;
        this.threads = new ThreadPoolExecutor(1, threads, KEEPALIVE_TIME_MS, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    /**
     * ハンドリングクラスを指定する
     *
     * @param taskHandler
     */
    public void setTaskHandler(ITaskHandler taskHandler) {
        this.taskHandler = taskHandler;
    }

    /**
     * 現在の実行待ちキューを取得する
     *
     * @return
     */
    public List<AsyncTaskResult<?>> getTaskQueue() {
        return new ArrayList<>(taskQueue);
    }

    /**
     * 現在実行中のタスクを取得する
     *
     * @return
     */
    public List<AsyncTaskResult<?>> getRunningTasks() {
        return new ArrayList<>(runningTasks);
    }

    private synchronized <T> AsyncTaskResult<T> pushTask(boolean front, IAsyncTask<T> task) {
        AsyncTaskResult<T> result = new AsyncTaskResult<>(this);
        result.task = task;

        // タスクを追加する
        if (!disposed) {

            if (front) {
                taskQueue.add(0, result);
            } else {
                taskQueue.add(result);
            }

            threads.execute(runner);
        }
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
    public AsyncTaskResult<AsyncTaskController> pushBack(final Runnable task) {
        return pushBack(new IAsyncTask<AsyncTaskController>() {
            @Override
            public AsyncTaskController doInBackground(AsyncTaskResult<AsyncTaskController> result) throws Exception {
                task.run();
                return AsyncTaskController.this;
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
    public AsyncTaskResult<AsyncTaskController> pushFront(final Runnable task) {
        return pushFront(new IAsyncTask<AsyncTaskController>() {
            @Override
            public AsyncTaskController doInBackground(AsyncTaskResult<AsyncTaskController> result) throws Exception {
                task.run();
                return AsyncTaskController.this;
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
        runningTasks.add(taskResult);   // 実行中に登録
        {
            taskResult.execute();
        }
        runningTasks.remove(taskResult);    // 実行中から削除
    }

    final Runnable runner = new Runnable() {
        @Override
        public void run() {
            executeTask();
        }
    };
}
