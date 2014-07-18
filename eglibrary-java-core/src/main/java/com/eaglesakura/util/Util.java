package com.eaglesakura.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Util {

    /**
     * 単純にsleepさせる。
     *
     * @param timems
     */
    public static void sleep(long timems) {
        if (timems <= 0) {
            // sleep時間が0ならば何もする必要はない
            return;
        }

        try {
            Thread.sleep(timems);
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    /**
     * itemが重複しないようにaddする
     *
     * @param list
     * @param item
     * @return
     */
    public static <T> boolean addUnique(List<T> list, T item) {
        if (!list.contains(item)) {
            list.add(item);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @see Collection#toArray(Object[])
     */
    public static <T> T[] convert(Collection<T> c, T[] array) {
        return c.toArray(array);
    }

    /**
     * @see ArrayList(Collection)
     * @see Arrays#asList(Object...)
     */
    public static <T> List<T> convert(T[] array) {
        return new ArrayList<T>(Arrays.asList(array));
    }

    /**
     * @see Collection
     */
    public static <T> List<T> copy(Collection<T> c) {
        return new ArrayList<T>(c);
    }

    /**
     * @see Arrays#copyOf(Object[], int)
     */
    public static <T> T[] copy(T[] array) {
        return Arrays.copyOf(array, array.length);
    }

}
