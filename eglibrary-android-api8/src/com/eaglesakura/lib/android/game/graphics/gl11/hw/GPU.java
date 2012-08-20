package com.eaglesakura.lib.android.game.graphics.gl11.hw;

public class GPU {
    /**
     * GPU資源のロックオブジェクト。
     * GPUは単一資源のため、すべての処理は排他的に行われなければならない。
     */
    static Object gpu_lock = new Object();
}
