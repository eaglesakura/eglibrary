package com.eaglesakura.android.message;

/**
 * {@link JointMessage} のハンドリングが完了したことを通知する
 */
public interface MessageHandlingListener {
    /**
     * メッセージのハンドリングが完了したことを通知する
     *
     * @param message 処理されたメッセージ
     */
    void onMessageHandleCompleted(JointMessage message);
}
