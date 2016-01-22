package com.eaglesakura.lib.android.game.io;

import java.io.IOException;
import java.io.OutputStream;

public class BufferTargetOutputStream extends OutputStream {
    /**
     * 書き込み対象のバッファ
     */
    byte[] buffer;

    /**
     *
     */
    int pointer;

    public BufferTargetOutputStream(byte[] dst) {
        buffer = dst;
    }

    @Override
    public void write(int oneByte) throws IOException {
        buffer[pointer] = (byte) oneByte;
        ++pointer;
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        write(buffer, 0, buffer.length);
    }

    @Override
    public void write(byte[] writeBuffer, int offset, int count) throws IOException {
        System.arraycopy(writeBuffer, offset, this.buffer, pointer, count);
        pointer += count;
    }

    /**
     * 書き込み位置をリセットする
     */
    public void reset() {
        pointer = 0;
    }

    /**
     * 書き込み対象のバッファを取得する
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * 書き込みポインタを取得する
     */
    public int getWriteIndex() {
        return pointer;
    }

    /**
     * 書き込みバッファの最大サイズを取得する
     */
    public int getBufferSize() {
        return buffer.length;
    }
}
