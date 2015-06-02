package com.eaglesakura.thread;

import com.eaglesakura.time.Timer;
import com.eaglesakura.util.Util;

/**
 * 何らかの値を保持するためのホルダ。
 * マルチスレッドでデータ受け渡し等に利用する。
 *
 * @param <T>
 */
public class Holder<T> {

    private T value = null;

    private Object lock = new Object();

    public Holder() {

    }

    /**
     * パラメータを取得する。
     *
     * @return
     */
    public T get() {
        return value;
    }

    /**
     * パラメータをセットする。
     *
     * @param value
     */
    public void set(T value) {
        this.value = value;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    /**
     * valueがnull以外になるまでアクセスをロックして値を返す。
     *
     * @return
     */
    public T getWithWait() {
        synchronized (lock) {
            if (value != null) {
                return value;
            }

            try {
                lock.wait();
            } catch (Exception e) {

            }
        }
        return value;
    }

    /**
     * valueがnull以外になるまでアクセスをロックして値を返す。
     *
     * @param timeout この時間以上に時間がかかったら例外を吐く。
     * @return
     */
    public T getWithWait(final long timeout) {
        synchronized (lock) {
            final Timer timer = new Timer();

            if (value != null) {
                return value;
            }

            try {
                lock.wait(timeout);
            } catch (Exception e) {

            }
        }
        if (value == null) {
            throw new IllegalStateException("value is null!!");
        } else {
            return value;
        }
    }
}
