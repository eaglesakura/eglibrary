package com.eaglesakura.lib.io;

import java.io.IOException;
import java.io.OutputStream;

public class PasswordEncodeOutputStream extends OutputStream {
    OutputStream os = null;
    byte[] encodeBits = null;
    int current = 0;

    public PasswordEncodeOutputStream(OutputStream os, String password) {
        if (password.length() == 0) {
            throw new IllegalArgumentException("password lengh error");
        }
        this.os = os;
        encodeBits = password.getBytes();
    }

    public PasswordEncodeOutputStream(OutputStream os, byte[] password) {
        if (password.length == 0) {
            throw new IllegalArgumentException("password lengh error");
        }
        this.os = os;
        encodeBits = password;
    }

    @Override
    public void write(int oneByte) throws IOException {
        int bits = encodeBits[current % encodeBits.length];
        os.write((oneByte ^ bits) & 0xff);
        ++current;
    }

    @Override
    public void close() throws IOException {
        os.close();
    }
}
