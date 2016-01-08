package com.eaglesakura.android.thread.async;

/**
 * 指定の方法でコールバックを行う
 * <p/>
 * 必要に応じ、キューイング等も行う
 */
public interface ITaskHandler {
    void request(Runnable runner);
}
