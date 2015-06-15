package com.eaglesakura.android.net;

import com.eaglesakura.android.dao.net.DbFileBlock;

import java.io.IOException;
import java.io.InputStream;

import de.greenrobot.dao.query.CloseableListIterator;

/**
 * DBからデータを読み出す
 */
public class BlockInputStream extends InputStream {
    NetworkConnector.CacheDatabase database;

    CloseableListIterator<DbFileBlock> blocks;

    /**
     * 読み込み対象のブロック
     */
    DbFileBlock block;

    /**
     * ブロックの残り容量
     */
    int blockCapacity;

    public BlockInputStream(NetworkConnector.CacheDatabase database, String url) throws IOException {
        this.database = database;
        database.open();
        blocks = database.listFileBlocks(url);
        nextBlock();
    }

    @Override
    public int available() throws IOException {
        return blockCapacity;
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        int sumReadBytes = 0;
        while (byteCount > 0) {
            int readSize = Math.min(blockCapacity, byteCount);
            System.arraycopy(block.getBody(), block.getBody().length - blockCapacity, buffer, byteOffset, readSize);

            sumReadBytes += readSize;
            blockCapacity -= readSize;
            byteCount -= readSize;

            if (blockCapacity == 0) {
                // 読み込み容量が無くなったらチェックする
                if (nextBlock()) {
                    // 終端に達したのでここまで
                    return sumReadBytes;
                }
            }
        }

        return sumReadBytes;
    }

    @Override
    public int read() throws IOException {
        byte[] temp = new byte[1];
        read(temp, 0, 1);
        return ((int) temp[0]) & 0xFF;
    }

    /**
     * 次のブロックを準備する
     *
     * @return 正常にファイル終端に達した場合trueを返却する
     */
    boolean nextBlock() throws IOException {
        if (!blocks.hasNext()) {
            if (block.getEof()) {
                // ブロックは正常にファイル終端に達した
                return true;
            } else {
                // ファイル終端に達する前にブロック終端に達した
                throw new IOException("Block EOF error");
            }
        }

        // 次のブロックを読み込む
        block = blocks.next();
        blockCapacity = block.getBody().length;
        return false;
    }

    @Override
    public void close() throws IOException {
        blocks.close();
        database.close();
    }
}
