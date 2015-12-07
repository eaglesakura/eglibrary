// ICommandServerService.aidl
package com.eaglesakura.android.service.aidl;

import com.eaglesakura.android.service.aidl.ICommandClientCallback;

interface ICommandServerService {
    /**
     * Map<String, byte[]>のエンコードデータを受け取り、同じくそれを返す
     */
    byte[] postToServer(String cmd, in byte[] buffer);

    /**
     * コールバック登録を行う
     */
    void registerCallback(String id, ICommandClientCallback callback);

    /**
     * コールバック削除を行う
     */
    void unregisterCallback(ICommandClientCallback callback);
}
