package com.eaglesakura.android.debug.window.log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 1画面のデバッグ情報を管理するレンダラ
 */
public class DebugItemGroup {

    /**
     * リクエストされているデバッグ表示アイテム一覧
     */
    List<DebugRenderingItem> items = new ArrayList<>();

    public DebugItemGroup() {
    }

    public DebugItemGroup(List<DebugRenderingItem> items) {
        this.items = items;
    }

    public List<DebugRenderingItem> getItems() {
        return items;
    }

    public void add(DebugRenderingItem item) {
        this.items.add(item);
    }

    /**
     * 指定したフラグを全て持つアイテムを取得する
     */
    public List<DebugRenderingItem> listFlagItems(int flags) {
        List<DebugRenderingItem> result = new ArrayList<>();
        for (DebugRenderingItem temp : items) {
            if (temp.hasFlags(flags)) {
                result.add(temp);
            }
        }

        return result;
    }

    /**
     * 不要なアイテムを削除する
     */
    public void clean() {
        Iterator<DebugRenderingItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            DebugRenderingItem item = iterator.next();
        }
    }

    /**
     * 描画を継続したいアイテムを取得する
     */
    public List<DebugRenderingItem> listNonDropItems() {
        List<DebugRenderingItem> result = new ArrayList<>();

        for (DebugRenderingItem temp : items) {
            if (!temp.isDropMessage()) {
                result.add(temp);
            }
        }

        return result;
    }
}
