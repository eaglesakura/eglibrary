package com.eaglesakura.lib.android.game.io.web;

import com.eaglesakura.lib.android.game.io.IFile;
import com.eaglesakura.lib.android.game.io.IStorageDevice;

/**
 * MacのＷｅｂ共有（Apache）を利用したストレージ。 基本的にRead-Only
 *
 * @author TAKESHI YAMASHITA
 */
public class WebShareStoreage implements IStorageDevice {

    int connectTimeout = 1000 * 10;

    String rootUrl;

    public WebShareStoreage(String rootUrl) {
        this.rootUrl = rootUrl;
    }

    /**
     * 接続タイムアウトをミリ秒単位で取得する。
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public IFile getRootDirectory() {
        return new WebFile(this, rootUrl);
    }

    static String toName(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        url = url.substring(url.lastIndexOf("/") + 1);
        return url;
    }

    @Override
    public IFile newInstance(IFile directory, String name, boolean isDirectory) {
        if (isDirectory) {
            if (!name.endsWith("/")) {
                name = (name + "/");
            }
        }
        return new WebFile(this, ((WebFile) directory).uri + name);
    }
}
