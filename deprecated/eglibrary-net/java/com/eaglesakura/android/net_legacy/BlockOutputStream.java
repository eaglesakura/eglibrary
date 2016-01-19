package com.eaglesakura.android.net_legacy;

import com.eaglesakura.android.dao.net.DbFileBlock;

import java.io.IOException;
import java.io.OutputStream;

public class BlockOutputStream extends OutputStream {
    final String url;

    int currentBlockIndex = 0;

    byte[] blockBuffer = new byte[LegacyNetworkConnector.BLOCK_SIZE];

    int bufferCapacity = LegacyNetworkConnector.BLOCK_SIZE;

    LegacyNetworkConnector.CacheDatabase database;

    public BlockOutputStream(LegacyNetworkConnector.CacheDatabase database, String url, int startIndex) {
        this.database = database;
        this.currentBlockIndex = startIndex;
        this.url = url;

        database.open();
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        while (count > 0) {
            int writeSize = Math.min(bufferCapacity, count);

            if (writeSize > 0) {
                // バッファをコピーする
                System.arraycopy(buffer, offset, blockBuffer, blockBuffer.length - bufferCapacity, writeSize);

                // キャパシティをリセットする
                bufferCapacity -= writeSize;
                count -= writeSize;
                offset += writeSize;
            }

            // キャパシティが無かったらコミットする
            if (bufferCapacity == 0) {
                commit(false);
            }
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
            block.setBody(blockBuffer);
        } else {
            byte[] temp = new byte[blockBuffer.length - bufferCapacity];
            System.arraycopy(blockBuffer, 0, temp, 0, temp.length);
            block.setBody(temp);
        }
        block.setEof(eof);
        block.setNumber(currentBlockIndex);

        database.put(block);

        // キャパシティを戻してインデックスを進める
        ++currentBlockIndex;
        bufferCapacity = blockBuffer.length;
    }
}
