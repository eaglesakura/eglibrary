package com.eaglesakura.android.net;

import com.eaglesakura.android.dao.net.DbFileBlock;

import java.io.IOException;
import java.io.OutputStream;

public class BlockOutputStream extends OutputStream {
    final String url;

    int currentBlockIndex = 0;

    byte[] buffer = new byte[NetworkConnector.BLOCK_SIZE];

    int bufferCapacity = NetworkConnector.BLOCK_SIZE;

    NetworkConnector.CacheDatabase database;

    /**
     * データのDLが正常終了した場合に呼び出す
     */
    boolean completed = false;

    public BlockOutputStream(NetworkConnector.CacheDatabase database, String url, int startIndex) {
        this.database = database;
        this.currentBlockIndex = startIndex;
        this.url = url;

        database.open();
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        while (count > 0) {
            // キャパシティが無かったらコミットする
            if (bufferCapacity == 0) {
                commit(false);
            }

            int writeSize = Math.min(bufferCapacity, count);
            // バッファをコピーする
            System.arraycopy(buffer, offset, buffer, buffer.length - bufferCapacity, writeSize);

            // キャパシティをリセットする
            bufferCapacity -= writeSize;
            count -= writeSize;
        }
    }

    /**
     * データのDLが正常完了した場合に呼び出す
     */
    public void onCompleted() {
        commit(true);
    }

    @Override
    public void write(int oneByte) throws IOException {
        this.write(new byte[]{(byte) oneByte}, 0, 1);
    }

    @Override
    public void close() throws IOException {
        database.close();
    }

    void commit(boolean eof) {
        // データをコミットする
        DbFileBlock block = new DbFileBlock();
        block.setUrl(url);
        if (bufferCapacity == 0) {
            block.setBody(buffer);
        } else {
            byte[] temp = new byte[buffer.length - bufferCapacity];
            System.arraycopy(block, 0, temp, 0, temp.length);
            block.setBody(temp);
        }
        block.setEof(eof);
        block.setIndex(currentBlockIndex);

        database.put(block);

        // キャパシティを戻してインデックスを進める
        ++currentBlockIndex;
        bufferCapacity = buffer.length;
    }
}
