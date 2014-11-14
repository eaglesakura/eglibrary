package com.eaglesakura.android.framework.support.ui.playservice;

/**
 * 現在のGoogle Play Serviceの状態を示す
 */
public enum ServiceState {
    /**
     * 未接続
     */
    NotConnected,

    /**
     * ネットワーク処理中
     */
    Pending,

    /**
     * 接続済み
     */
    Connected,

    /**
     * 何らかの問題が発生した
     */
    Error,
}
