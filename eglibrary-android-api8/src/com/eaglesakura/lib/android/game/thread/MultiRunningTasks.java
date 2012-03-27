package com.eaglesakura.lib.android.game.thread;

import java.util.ArrayList;
import java.util.List;

import com.eaglesakura.lib.android.game.util.GameUtil;

/**
 * 並列的に複数のタスクの実行を行う。<BR>
 * タスク数に制限はないが、限界は考えて使ったほうがいい。<BR>
 * タスクが開始される順番は確定されるが、終了する順番は保証されない。
 * 
 * @author Takeshi
 * 
 */
public class MultiRunningTasks {
    public interface Task {
        /**
         * 開始時に呼ばれる。<BR>
         * この処理は同時に複数呼ばれることはない。
         * 
         * @param runnner
         * @return falseを返した場合、タスクの実行を行わない。
         */
        public boolean begin(MultiRunningTasks runnner);

        /**
         * 実際の処理を行わせる。
         * 
         * @param runner
         */
        public void run(MultiRunningTasks runner);

        /**
         * 終了時に呼ばれる。<BR>
         * この処理は同時に複数呼ばれることはない。
         * 
         * @param runner
         */
        public void finish(MultiRunningTasks runner);
    }

    List<Thread> threads = new ArrayList<Thread>();
    List<Task> tasks = new ArrayList<MultiRunningTasks.Task>();

    int maxThreads = 1;

    boolean exit = false;

    public MultiRunningTasks(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    /**
     * タスクを後ろに追加する。
     * 
     * @param task
     */
    public synchronized void pushBack(Task task) {
        synchronized (this) {
            tasks.add(task);
        }
    }

    /**
     * タスクを前に追加する。
     * 
     * @param task
     */
    public synchronized void pushFront(Task task) {
        synchronized (this) {
            tasks.add(0, task);
        }
    }

    /**
     * 全てのタスクが終わるのを明示的に待つ。<BR>
     * メソッドを抜けた次点で全てのタスクが完了している。
     */
    public void waitTaskFinished() {
        setThreadPoolMode(false);

        while (threads.size() > 0) {
            GameUtil.sleep(10);
        }
    }

    /**
     * スレッドを常にプールする場合はtrue、不要なスレッドを廃棄する場合はfalse
     * @param pool
     */
    public void setThreadPoolMode(boolean pool) {
        this.exit = !pool;
    }

    /**
     * タスクを実行する。
     */
    protected void runTask(final Thread thread) {
        Task _task = null;

        //! 次のタスクを受け取る。
        do {
            while ((_task = nextTask()) != null) {
                //!     タスクの実行
                _task.run(MultiRunningTasks.this);
                finish(_task);
            }
            GameUtil.sleep(100);
        } while (!exit || !tasks.isEmpty());

        //! 実行中タスクを削除する
        synchronized (threads) {
            threads.remove(thread);
        }
    }

    /**
     * 一意のスレッド番号を設定する。
     */
    private static int threadId = 0;

    /**
     * スレッドを開始させる。<BR>
     * {@link #exit()} or {@link #waitTaskFinished()}させない限りスレッドは待機し続ける。
     */
    public void start() {
        synchronized (threads) {
            while (threads.size() < maxThreads) {
                final Thread thread = (new Thread() {
                    @Override
                    public void run() {
                        runTask(this);
                    };
                });
                threads.add(thread);
                thread.setName(MultiRunningTasks.class.getName() + " :: ID " + threadId++);
                thread.start();
            }
        }
    }

    /**
     * 次処理すべきタスクを取得する。
     * 
     * @return
     */
    private synchronized Task nextTask() {
        synchronized (this) {
            do {
                if (tasks.isEmpty()) {
                    return null;
                }

                final Task next = tasks.get(0);
                tasks.remove(0);

                //! beginに成功した場合のみ返す。
                if (next.begin(this)) {
                    return next;
                }
            } while (true);
        }
    }

    /**
     * タスクを終了させる。
     * 
     * @param task
     */
    synchronized void finish(Task task) {
        task.finish(this);
    }

    /**
     * 残タスク数を取得する。
     * 
     * @return
     */
    public int getTaskCount() {
        return tasks.size() + threads.size();
    }
}
