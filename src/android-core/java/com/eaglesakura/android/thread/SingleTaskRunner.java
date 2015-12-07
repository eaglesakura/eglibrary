package com.eaglesakura.android.thread;

import android.content.Context;

import com.eaglesakura.thread.MultiRunningTasks;
import com.eaglesakura.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 1Contextに対し、重複したタスクを実行しないようにするRunner
 * <br>
 * 重複チェックはrunの時点で行われ、既に同一タスクが実行中である場合は何もしない
 */
public abstract class SingleTaskRunner implements MultiRunningTasks.Task {

    static final Object lock = new Object();

    static final HashSet<ContextCache> gRunningTasks = new HashSet<>();

    static synchronized ContextCache getCache(Context context) {
        Iterator<ContextCache> iterator = gRunningTasks.iterator();
        while (iterator.hasNext()) {
            ContextCache next = iterator.next();
            if (!next.exist()) {
                iterator.remove();
            } else if (next.getContext().equals(context)) {
                return next;
            }
        }

        // 見つからない
        ContextCache cache = new ContextCache(context);
        LogUtil.log("new task context(%s)", context.toString());
        gRunningTasks.add(cache);
        return cache;
    }

    final String taskId;

    final ContextCache contextCache;

    final Context context;

    public SingleTaskRunner(Context context, String taskId) {
        this.taskId = taskId;
        this.contextCache = getCache(context);
        this.context = context;
    }

    @Override
    public final boolean begin(MultiRunningTasks runnner) {
        synchronized (contextCache) {
            if (contextCache.hasTask(this)) {
                return false;
            }

            // タスクを追加
            contextCache.addRunning(this);
        }
        return true;
    }

    @Override
    public final void finish(MultiRunningTasks runner) {
        synchronized (contextCache) {
            contextCache.removeRunning(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SingleTaskRunner that = (SingleTaskRunner) o;

        if (context != null ? !context.equals(that.context) : that.context != null) return false;
        if (taskId != null ? !taskId.equals(that.taskId) : that.taskId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = taskId != null ? taskId.hashCode() : 0;
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }

    static class ContextCache {
        WeakReference<Context> context;

        HashSet<MultiRunningTasks.Task> tasks = new HashSet<>();

        ContextCache(Context context) {
            this.context = new WeakReference<Context>(context);
        }

        void addRunning(MultiRunningTasks.Task task) {
            this.tasks.add(task);
        }

        void removeRunning(MultiRunningTasks.Task task) {
            this.tasks.remove(task);
        }

        boolean hasTask(MultiRunningTasks.Task task) {
            return this.tasks.contains(task);
        }

        boolean exist() {
            return context.get() != null;
        }

        Context getContext() {
            return context.get();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Context otherContext = ((ContextCache) o).context.get();
            Context selfContext = context.get();
            if (selfContext == null || otherContext == null) {
                return false;
            }

            return selfContext.equals(otherContext);
        }

        @Override
        public int hashCode() {
            if (!exist()) {
                return 0;
            }

            int result = context != null ? context.hashCode() : 0;
            result = 31 * result + (tasks != null ? tasks.hashCode() : 0);
            return result;
        }
    }
}
