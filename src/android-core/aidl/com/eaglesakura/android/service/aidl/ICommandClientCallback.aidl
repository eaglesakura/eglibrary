// ICommandClientCallback.aidl
package com.eaglesakura.android.service.aidl;

interface ICommandClientCallback {
    /**
     * エンコードデータを受け取り、同じくそれを返す
     */
    byte[] postToClient(String cmd, in byte[] buffer);
}
