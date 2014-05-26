package com.eaglesakura.lib.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * RandomAccessFileをInputStreamから利用する。
 */
public class RandomAccessFileInputStream extends InputStream {
    private RandomAccessFile file = null;
    private int header = 0;
    private int size = 0;

    /**
     *
     * @param raf
     */
    public RandomAccessFileInputStream(RandomAccessFile raf) {
        file = raf;
        try {
            size = (int) file.length();
        } catch (Exception e) {

        }
    }

    /**
     *
     * @param raf
     */
    public RandomAccessFileInputStream(RandomAccessFile raf, int header, int length) {
        file = raf;
        this.header = header;
        this.size = length;
    }

    @Override
    public int read() throws IOException {
        return file.read();

    }

    @Override
    public int read(byte[] b) throws IOException {
        return file.read(b);
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        return file.read(b, offset, length);
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readlimit) {
    }

    @Override
    public int available() throws IOException {
        // return 1024 * 200;
        int result = (int) (size - (file.getFilePointer() - header));
        return result;
    }

    @Override
    public long skip(long byteCount) throws IOException {

        long start = file.getFilePointer();
        file.skipBytes((int) byteCount);

        return file.getFilePointer() - start;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public synchronized void reset() throws IOException {
        file.seek(header);
    }
}
