package com.eaglesakura.lib.list;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 内容をUniqueに保つリストを作成する
 *
 * @author TAKESHI YAMASHITA
 */
public class UniqueList<E> {
    List<E> data = new LinkedList<E>();
    Map<E, E> unique = new HashMap<E, E>();

    /**
     * 既にデータを保持していればtrue
     */
    public boolean containts(E e) {
        return unique.get(e) != null;
    }

    /**
     * データを追加する
     */
    public boolean add(E e) {
        if (!containts(e)) {
            data.add(e);
            unique.put(e, e);
            return true;
        }
        return false;
    }

    /**
     * 一意のリストを取得する
     */
    public List<E> getList() {
        return data;
    }

    /**
     * リストに格納されている数を取得する
     */
    public int size() {
        return data.size();
    }
}
