package com.eaglesakura.util;

import java.util.List;

public class Util {

    /**
     * 単純にsleepさせる。
     * 
     * @param timems
     */
    public static void sleep(long timems) {
        try {
            Thread.sleep(timems);
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    /**
     * itemが重複しないようにaddする
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

}
