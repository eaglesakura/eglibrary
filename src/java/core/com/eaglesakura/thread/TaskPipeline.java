package com.eaglesakura.thread;

import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.time.Timer;
import com.eaglesakura.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * タスクのパイプライン処理を補助する
 * <p/>
 * パイプラインはMainStreamとSubStreamに分かれており、メインストリームは実行・終了順が保証される。
 * サブストリームは実行開始順が保証されるが、終了順は保証されない。
 * <p/>
 * 標準ではすべてのTaskPipelineオブジェクトが1つのスレッドを共有するが、必要に応じてデタッチすることが可能。
 * 既にタスクが登録されているかつデタッチが行われた場合はスレッド実行順を保証しない。
 */
@Deprecated
public class TaskPipeline {
    static final MultiRunningTasks globalMainTaskQueue = new MultiRunningTasks(1);
    static final MultiRunningTasks globalSubTaskQueue = new MultiRunningTasks(3);

    static {
        globalMainTaskQueue.setAutoStart(true);
        globalMainTaskQueue.setThreadPoolMode(false);
        globalMainTaskQueue.setThreadName("Pipeline:GlobalMain");

        globalSubTaskQueue.setAutoStart(true);
        globalSubTaskQueue.setThreadPoolMode(false);
        globalSubTaskQueue.setThreadName("Pipeline:GlobalSub");
    }

    MultiRunningTasks mainStream = globalMainTaskQueue;

    MultiRunningTasks subStreams = globalSubTaskQueue;

    /**
     * グルーピングされたタスク情報
     */
    Map<Object, TaskGroup> taskGroups = new HashMap<>();

    /**
     * パイプラインをすべてキャンセルしている場合true
     */
    boolean aborted = false;

    public TaskPipeline() {
    }

    /**
     * パイプラインを開放し、残っているタスクを削除する
     */
    public void abort() {
        aborted = true;
        mainStream.clear();
        subStreams.clear();
    }

    /**
     * メインストリーム処理をローカルスレッドとして立ち上げる。
     */
    public void startLocalMainStream(String threadName) {
        mainStream = new MultiRunningTasks(1);
        mainStream.setThreadPoolMode(false);
        mainStream.setThreadPoolMode(false);
        mainStream.setThreadName(threadName);
    }

    /**
     * サブストリーム処理をローカルスレッドとして立ち上げる
     *
     * @param threadName
     * @param streamNum
     */
    public void startLocalSubStreams(String threadName, int streamNum) {
        subStreams = new MultiRunningTasks(streamNum);
        subStreams.setThreadPoolMode(false);
        subStreams.setThreadPoolMode(false);
        subStreams.setThreadName(threadName);
    }

    private TaskImpl wrap(Runnable taskMain, Object groupId, TaskCallback callback) {
        TaskImpl result = null;
        if (groupId != null) {
            result = new GroupTaskImpl(groupId);
        } else {
            result = new TaskImpl();
        }
        result.runnable = taskMain;
        result.callback = callback;
        if (result.callback == null) {
            result.callback = new TaskCallback() {
                @Override
                public CallbackThread getCallbackThread() {
                    return CallbackThread.CurrentThread;
                }

                @Override
                public boolean isTaskStart(Runnable task) {
                    return true;
                }

                @Override
                public void onTaskFinished(Runnable task) {

                }
            };
        }
        return result;
    }

    /**
     * メインストリームに処理を追加する
     *
     * @param task
     * @return
     */
    public TaskPipeline pushBackMain(Runnable task) {
        mainStream.pushBack(task);
        return this;
    }

    /**
     * メインストリームに処理を追加する
     *
     * @param task
     * @param callback
     * @return
     */
    public TaskPipeline pushBackMain(Runnable task, TaskCallback callback) {
        mainStream.pushBack(wrap(task, null, callback));
        return this;
    }

    /**
     * メインストリームに処理を割りこませる
     *
     * @param task
     * @return
     */
    public TaskPipeline pushFrontMain(Runnable task) {
        mainStream.pushFront(task);
        return this;
    }

    /**
     * メインストリームに処理を割りこませる
     *
     * @param task
     * @param callback
     * @return
     */
    public TaskPipeline pushFrontMain(Runnable task, TaskCallback callback) {
        mainStream.pushFront(wrap(task, null, callback));
        return this;
    }

    /**
     * サブストリームに処理を追加する
     *
     * @param task
     * @return
     */
    public TaskPipeline pushBackSub(Runnable task) {
        subStreams.pushBack(task);
        return this;
    }

    public TaskPipeline pushBackSub(Runnable task, TaskCallback callback) {
        subStreams.pushBack(wrap(task, null, callback));
        return this;
    }

    /**
     * サブストリームに処理を割りこませる
     *
     * @param task
     * @return
     */
    public TaskPipeline pushFrontSub(Runnable task) {
        subStreams.pushFront(task);
        return this;
    }

    /**
     * サブストリームに処理を割りこませる
     *
     * @param task
     * @param callback
     * @return
     */
    public TaskPipeline pushFrontSub(Runnable task, TaskCallback callback) {
        subStreams.pushFront(wrap(task, null, callback));
        return this;
    }

    private void insert(GroupTaskImpl task) {
        synchronized (taskGroups) {
            TaskGroup group = taskGroups.get(task.id);
            if (group == null) {
                group = new TaskGroup();
                taskGroups.put(task.id, group);
            }
            group.tasks.add(task);
        }
    }

    /**
     * タスクグループを登録する
     * <p/>
     * このタスクは強制的にサブストリームに登録される。
     *
     * @param id
     * @param task
     * @return
     */
    public TaskPipeline pushBackGroup(Object id, Runnable task) {
        GroupTaskImpl groupTask = (GroupTaskImpl) wrap(task, id, null);
        insert(groupTask);
        subStreams.pushBack(groupTask);
        return this;
    }

    /**
     * タスクグループを割り込み登録する
     * <p/>
     * このタスクは強制的にサブストリームに登録される。
     *
     * @param id
     * @param task
     * @return
     */
    public TaskPipeline pushFrontGroup(Object id, Runnable task) {
        GroupTaskImpl groupTask = (GroupTaskImpl) wrap(task, id, null);
        insert(groupTask);
        subStreams.pushFront(groupTask);
        return this;
    }

    public interface CancelSignal {
        boolean isCancel();
    }

    /**
     * メインタスクの終了待ちを行う
     */
    public void await() {
        final Object lock = new Object();
        synchronized (lock) {
            try {
                pushBackMain(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                });
                lock.wait(1000 * 30);
            } catch (Exception e) {
            }
        }
    }

    /**
     * 特定のグループが処理終了するまで待機する
     * <p/>
     * これは主にメインスレッドやメインストリーム側から呼び出す。
     *
     * @param id
     * @param waitTimeMs
     * @param cancelSignal
     */
    public boolean await(Object id, long waitTimeMs, CancelSignal cancelSignal) {
        Timer timer = new Timer();
        while (timer.end() < waitTimeMs) {
            synchronized (taskGroups) {
                if (!taskGroups.containsKey(id)) {
                    // キーが見つからなかったので、タスクはすべて終了している
                    return true;
                }
            }

            if (cancelSignal.isCancel()) {
                return false;
            }

            // 適当な時間だけ待機する
            Util.sleep(100);
        }

        // 時間切れ
        return false;
    }

    /**
     * サブスレッドで実行されるタスクグループ
     */
    class TaskGroup {
        List<MultiRunningTasks.Task> tasks;
    }

    class TaskImpl implements MultiRunningTasks.Task {
        Runnable runnable;

        TaskCallback callback;

        private void runCallback(MultiRunningTasks runnner, final Runnable task) {
            CallbackThread thread = callback.getCallbackThread();
            MultiRunningTasks targetTasks;
            final Holder<Object> sync = new Holder<>();

            switch (thread) {
                case CurrentThread:
                    callback.isTaskStart(runnable);
                    return;
                case UiThread:
                    UIHandler.postUIorRun(new Runnable() {
                        @Override
                        public void run() {
                            task.run();
                            sync.set(this);
                        }
                    });
                    break;
                case MainStream:
                    if (runnner == mainStream) {
                        task.run();
                        return;
                    } else {
                        mainStream.pushFront(new Runnable() {
                            @Override
                            public void run() {
                                task.run();
                                sync.set(this);
                            }
                        });
                    }
                    break;
                case SubStream:
                    if (runnner == subStreams) {
                        task.run();
                        return;
                    } else {
                        subStreams.pushFront(new Runnable() {
                            @Override
                            public void run() {
                                task.run();
                                sync.set(this);
                            }
                        });
                    }
                default:
                    throw new IllegalStateException();
            }

            sync.getWithWait(1000 * 30);
        }

        @Override
        public boolean begin(MultiRunningTasks runnner) {
            if (aborted) {
                return false;
            }

            final Holder<Boolean> holder = new Holder<>();
            runCallback(runnner, new Runnable() {
                @Override
                public void run() {
                    holder.set(callback.isTaskStart(runnable));
                }
            });
            return holder.get() && !aborted;
        }

        @Override
        public void run(MultiRunningTasks runner) {
            if (aborted) {
                return;
            } else {
                runnable.run();
            }
        }

        @Override
        public void finish(MultiRunningTasks runner) {
            runCallback(runner, new Runnable() {
                @Override
                public void run() {
                    callback.onTaskFinished(runnable);
                }
            });
        }
    }

    class GroupTaskImpl extends TaskImpl {
        Object id;

        public GroupTaskImpl(Object id) {
        }

        @Override
        public void finish(MultiRunningTasks runner) {
            super.finish(runner);

            synchronized (taskGroups) {
                TaskGroup group = taskGroups.get(id);
                if (group != null) {
                    group.tasks.remove(this);
                    // タスクがなくなったらグループのホルダ自体も削除
                    if (group.tasks.isEmpty()) {
                        taskGroups.remove(id);
                    }
                }
            }
        }
    }

    /**
     * コールバック対象のスレッドを指定する
     */
    public enum CallbackThread {
        /**
         * 現在のスレッドでそのまま処理する
         */
        CurrentThread,

        /**
         * メインストリームに投げ直す
         */
        MainStream,

        /**
         * サブストリームに投げ直す
         */
        SubStream,

        /**
         * UIスレッドに投げ直す
         */
        UiThread,
    }

    public interface TaskCallback {
        /**
         * 終了時に通知されるスレッドを指定する
         *
         * @return
         */
        CallbackThread getCallbackThread();

        /**
         * タスクの開始を知らせる
         * <p/>
         * trueを返却した場合はそのまま実行、falseを返却した場合は実行をキャンセルする
         *
         * @param task
         */
        boolean isTaskStart(Runnable task);

        /**
         * タスクの完了を知らせる
         * <p/>
         * 呼び出し対象のスレッドは CallbackThread で指定される。
         */
        void onTaskFinished(Runnable task);
    }
}
