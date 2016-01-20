package com.eaglesakura.android.net.cache;

import java.io.Closeable;
import java.io.IOException;

/**
 * キャッシュの登録を行うインターフェース
 * <p/>
 * キャッシュは巨大になる可能性を考慮し、OutputStreamに近い形態を取る。
 */
public interface ICacheWriter extends Closeable {
    /**
     * キャッシュ末尾にデータを追加する
     *
     * @param buffer 書き込み対象バッファ
     * @param offset バッファの使用位置
     * @param length バッファの長さ
     * @throws IOException
     */
    void write(byte[] buffer, int offset, int length) throws IOException;

    /**
     * キャッシュの登録を完了した
     *
     * @throws IOException
     */
    void commit() throws IOException;

    /**
     * キャッシュ登録中に問題が発生したため、エントリーの削除を行わせる
     *
     * @throws IOException
     */
    void abort() throws IOException;
}
