package com.eaglesakura.lib.android.game.io.fs;

import com.eaglesakura.lib.android.game.io.IFile;
import com.eaglesakura.lib.android.game.io.IStorageDevice;

import java.io.File;

/**
 * ファイルシステムを利用したストレージ
 *
 * @author TAKESHI YAMASHITA
 */
public class ExternalStorage implements IStorageDevice {
    File root;

    /**
     *
     * @param root
     */
    public ExternalStorage(File root) {
        this.root = root;
    }

    /**
     *
     */
    @Override
    public IFile getRootDirectory() {
        return new FSFile(root);
    }

    /**
     * 新規インスタンスを作成する。
     */
    @Override
    public IFile newInstance(IFile directory, String name, boolean isDirectory) {
        return new FSFile(new File(((FSFile) directory).file, name));
    }
}
