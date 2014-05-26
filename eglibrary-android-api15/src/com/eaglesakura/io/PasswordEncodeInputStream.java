package com.eaglesakura.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * パスワード指定してビットシフトを行う。
 */
public class PasswordEncodeInputStream extends InputStream {
    InputStream is = null;
    byte[] encodeBits = null;
    int current = 0;

    public PasswordEncodeInputStream(InputStream is, String password) {
        if (password.length() == 0) {
            throw new IllegalArgumentException("password lengh error");
        }
        this.is = is;
        encodeBits = password.getBytes();
    }

    public PasswordEncodeInputStream(InputStream is, byte[] password) {
        if (password.length == 0) {
            throw new IllegalArgumentException("password lengh error");
        }
        this.is = is;
        encodeBits = password;
    }

    @Override
    public int read() throws IOException {
        final int bits = encodeBits[current % encodeBits.length];
        final int n = is.read();
        if (n >= 0) {
            ++current;
            return ((n ^ bits) & 0xff);
        } else {
            return n;
        }
    }

    @Override
    public long skip(long n) throws IOException {
        current += (int) n;
        return is.skip(n);
    }

    @Override
    public synchronized void reset() throws IOException {
        current = 0;
        is.reset();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int offset, int length) throws IOException {
        int result = is.read(b, offset, length);
        //! 読み込めた分だけデコードを行う
        for (int i = 0; i < result; ++i) {
            final int bits = encodeBits[current % encodeBits.length];
            b[offset + i] = (byte) (((int) b[offset + i] ^ bits) & 0xff);
            ++current;
        }
        return result;
    }

    @Override
    public void close() throws IOException {
        is.close();
    }
}
