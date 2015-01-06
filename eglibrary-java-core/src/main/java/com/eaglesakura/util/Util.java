package com.eaglesakura.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Util {

    public static int getInt(Integer value, int defValue) {
        if (value == null) {
            return defValue;
        } else {
            return value;
        }
    }

    public static double getDouble(Double value, double defValue) {
        if (value == null) {
            return defValue;
        } else {
            return value;
        }
    }

    /**
     * 今日の0時0分を取得する
     */
    public static Date getTodayStart() {
        long oneDay = 1000 * 60 * 24;
        long now = System.currentTimeMillis();

        return new Date(now / oneDay * oneDay);
    }

    /**
     * 今日の23時59分59秒....を取得する
     */
    public static Date getTodayEnd() {
        return new Date(getTodayStart().getTime() + (1000 * 60 * 24) - 1);
    }

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
     * アイテムを追加し、追加したインデックスを返す
     *
     * @param list
     * @param item
     * @param <T>
     * @return
     */
    public static <T> int addUniqueRequestIndex(List<T> list, T item) {
        addUnique(list, item);
        return list.indexOf(item);
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
