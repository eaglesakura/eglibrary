package com.eaglesakura.lib.android.game.io;

/**
 * ストレージデバイスを示す。 Rootフォルダやネットワークの接続先は問わず、抽象的に扱える。
 * 基本的にはUIスレッドから扱わず、裏スレッドで扱う必要がある。 主にリソースの読み出しを行う。書き込みは別なクラスを利用する。
 * 
 * @author Takeshi
 * 
 */
public interface IStrageDevice {
    /**
     * ルートディレクトリを取得する。
     * 
     * @return
     */
    IFile getRootDirectory();

    /**
     * 新規のインスタンスを作成する。
     * 
     * @param directory
     * @param name
     * @return
     */
    IFile newInstance(IFile directory, String name, boolean isDirectory);

}
