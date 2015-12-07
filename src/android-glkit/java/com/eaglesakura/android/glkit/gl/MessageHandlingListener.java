package com.eaglesakura.android.glkit.gl;

import com.eaglesakura.android.message.JointMessage;

/**
 * {@link JointMessage} のハンドリングが完了したことを通知する
 */
public interface MessageHandlingListener {
    /**
     * メッセージのハンドリングが完了したことを通知する
     *
     * @param what 処理されたメッセージ
     * @param data 処理されたデータ
     */
    void onMessageHandleCompleted(String what, Object data);
}
