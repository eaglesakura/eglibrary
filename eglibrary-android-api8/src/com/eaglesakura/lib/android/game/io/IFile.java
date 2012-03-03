package com.eaglesakura.lib.android.game.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * プロジェクト内で利用するファイルリソースを抽象化する。 接続先はデバッグ時・実行時の環境に属する。
 * 
 * @author Takeshi
 * 
 */
public interface IFile {
    /**
     * ディレクトリを指している場合はtrue
     * 
     * @return
     */
    boolean isDirectory();

    /**
     * ファイルを指している場合はtrue
     * 
     * @return
     */
    boolean isFile();

    /**
     * ファイルが存在している場合はtrueを返す。
     * 
     * @return
     */
    boolean exists();

    /**
     * ファイル名を取得する。
     * 
     * @return
     */
    String getName();

    /**
     * ディレクトリの場合、以下の階層をリスト化する。 {@link File#list()}相当。
     * 
     * @return
     */
    List<IFile> list();

    /**
     * 入力ストリームを開く。
     * 
     * @return
     * @throws IOException
     */
    InputStream openReadable() throws IOException;

    /**
     * 出力ストリームを開く。
     * 
     * @return
     * @throws IOException
     */
    OutputStream openWritable() throws IOException;
}
