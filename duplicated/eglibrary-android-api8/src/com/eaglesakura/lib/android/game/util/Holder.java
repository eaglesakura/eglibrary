package com.eaglesakura.lib.android.game.util;

/**
 * 何らかの値を保持するためのホルダ。
 * マルチスレッドでデータ受け渡し等に利用する。
 * @author TAKESHI YAMASHITA
 *
 * @param <T>
 */
public class Holder<T> {

    private T value = null;

    public Holder() {

    }

    /**
     * パラメータを取得する。
     * @return
     */
    public T get() {
        return value;
    }

    /**
     * パラメータをセットする。
     * @param value
     */
    public void set(T value) {
        this.value = value;
    }

    /**
     * valueがnull以外になるまでアクセスをロックして値を返す。
     * @return
     */
    public T getWithWait() {
        while (value == null) {
            GameUtil.sleep(1);
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
        final Timer timer = new Timer();
        while (value == null) {
            GameUtil.sleep(1);
            if (timer.end() > timeout) {
                throw new IllegalStateException("value is null!!");
            }
        }
        return value;
    }
}
