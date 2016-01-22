package com.eaglesakura.lib.android.game.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * プロジェクト内で利用するファイルリソースを抽象化する。 接続先はデバッグ時・実行時の環境に属する。
 *
 * @author TAKESHI YAMASHITA
 */
public interface IFile {
    /**
     * ディレクトリを指している場合はtrue
     */
    boolean isDirectory();

    /**
     * ファイルを指している場合はtrue
     */
    boolean isFile();

    /**
     * ファイルが存在している場合はtrueを返す。
     */
    boolean exists();

    /**
     * ファイル名を取得する。
     */
    String getName();

    /**
     * ディレクトリの場合、以下の階層をリスト化する。 {@link File#list()}相当。
     */
    List<IFile> list();

    /**
     * ファイルの長さを返す。
     * 不定の場合は-1を返す。
     */
    long length();

    /**
     * 入力ストリームを開く。
     */
    InputStream openReadable() throws IOException;

    /**
     * 出力ストリームを開く。
     */
    OutputStream openWritable() throws IOException;
}
