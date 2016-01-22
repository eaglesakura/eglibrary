package com.eaglesakura.lib.android.game.io;

/**
 * ストレージデバイスを示す。 Rootフォルダやネットワークの接続先は問わず、抽象的に扱える。
 * 基本的にはUIスレッドから扱わず、裏スレッドで扱う必要がある。 主にリソースの読み出しを行う。書き込みは別なクラスを利用する。
 *
 * @author TAKESHI YAMASHITA
 */
public interface IStorageDevice {
    /**
     * ルートディレクトリを取得する。
     */
    IFile getRootDirectory();

    /**
     * 新規のインスタンスを作成する。
     */
    IFile newInstance(IFile directory, String name, boolean isDirectory);

}
