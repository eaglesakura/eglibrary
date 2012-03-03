/**
 * データの入力を補助する。
 * @author eagle.sakura
 * @version 2009/11/15 : 新規作成
 */
package com.eaglesakura.lib.io;

import java.io.IOException;
import java.io.InputStream;

import com.eaglesakura.lib.android.game.graphics.DisposableResource;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * データ入力を補助するクラス。
 *
 * @author eagle.sakura
 * @version 2009/11/15 : 新規作成
 */
public final class DataInputStream extends DisposableResource {
    /**
     * 読み取りに使用するリーダー。
     */
    private InputStream reader = null;

    /**
     *
     * @author eagle.sakura
     * @param is
     * @version 2010/06/23 : 新規作成
     */
    public DataInputStream(InputStream is) {
        reader = is;
    }

    /**
     * バッファから１バイト読み取る。
     *
     * @author eagle.sakura
     * @return
     * @version 2009/11/15 : 新規作成
     */
    public byte readS8() throws IOException {
        byte[] n = {
            0
        };
        reader.read(n, 0, n.length);
        return n[0];
    }

    /**
     * バッファから2バイト読み取る。
     *
     * @author eagle.sakura
     * @return
     * @version 2009/11/15 : 新規作成
     */
    public short readS16() throws IOException {
        byte[] n = {
                0, 0
        };
        reader.read(n, 0, n.length);

        int n0 = ((int) n[0] & 0xff);
        int n1 = ((int) n[1] & 0xff);

        return (short) ((n0 << 8) | (n1 << 0));
    }

    /**
     * バッファから3バイト読み取る。<BR>
     * 色情報等に利用可能。
     *
     * @author eagle.sakura
     * @return
     * @version 2009/11/15 : 新規作成
     */
    public int readS24() throws IOException {
        byte[] n = {
                0, 0, 0
        };
        reader.read(n, 0, n.length);

        return (int) (((((int) n[0]) & 0xff) << 16) | ((((int) n[1]) & 0xff) << 8) | ((((int) n[2]) & 0xff) << 0));
    }

    /**
     * １バイト整数を取得し、読み込み位置を１バイト進める。
     *
     * @author eagle.sakura
     * @return １バイト符号無整数。ただし、符号無を表現する関係上、戻りはint型となる。
     * @version 2009/08/28 : 新規作成
     */
    public int readU8() throws IOException {
        return (((int) readS8()) & 0xff);
    }

    /**
     * ２バイト整数を取得し、読み込み位置を２バイト進める。
     *
     * @author eagle.sakura
     * @return ２バイト符号無整数。ただし、符号無を表現する関係上、戻りはint型となる。
     * @version 2009/09/20 : 新規作成
     */
    public int readU16() throws IOException {
        return (((int) readS16()) & 0xffff);
    }

    /**
     * バッファから4バイト読み取る。
     *
     * @author eagle.sakura
     * @return
     * @version 2009/11/15 : 新規作成
     */
    public int readS32() throws IOException {
        byte[] n = {
                0, 0, 0, 0
        };
        reader.read(n, 0, n.length);

        int n0 = ((int) n[0] & 0xff);
        int n1 = ((int) n[1] & 0xff);
        int n2 = ((int) n[2] & 0xff);
        int n3 = ((int) n[3] & 0xff);

        return (n0 << 24) | (n1 << 16) | (n2 << 8) | (n3 << 0);
    }

    /**
     * バッファから８バイト整数を読み取る。
     *
     * @author eagle.sakura
     * @return
     * @version 2010/03/24 : 新規作成
     */
    public long readS64() throws IOException {
        byte[] n = {
                0, 0, 0, 0, 0, 0, 0, 0
        };
        reader.read(n, 0, n.length);

        long n0 = ((int) n[0] & 0xff);
        long n1 = ((int) n[1] & 0xff);
        long n2 = ((int) n[2] & 0xff);
        long n3 = ((int) n[3] & 0xff);

        long n4 = ((int) n[4] & 0xff);
        long n5 = ((int) n[5] & 0xff);
        long n6 = ((int) n[6] & 0xff);
        long n7 = ((int) n[7] & 0xff);

        return (((long) (n0 << 24) | (n1 << 16) | (n2 << 8) | (n3 << 0)) << 32)
                | ((long) (n4 << 24) | (n5 << 16) | (n6 << 8) | (n7 << 0));
    }

    /**
     * write64Arrayした配列を取り出す。
     * @return
     * @throws IOException
     */
    public long[] readS64Array() throws IOException {
        //! 配列数を取り出す
        final int length = readS32();
        final long[] result = new long[length];
        final byte[] buffer = readBuffer(result.length * 8);
        int ptr = 0;
        //! longに変換する。
        for (int i = 0; i < length; ++i) {
            result[i] |= (long) ((((long) buffer[ptr++]) & 0xff) << 56);
            result[i] |= (long) ((((long) buffer[ptr++]) & 0xff) << 48);
            result[i] |= (long) ((((long) buffer[ptr++]) & 0xff) << 40);
            result[i] |= (long) ((((long) buffer[ptr++]) & 0xff) << 32);
            result[i] |= (long) ((((long) buffer[ptr++]) & 0xff) << 24);
            result[i] |= (long) ((((long) buffer[ptr++]) & 0xff) << 16);
            result[i] |= (long) ((((long) buffer[ptr++]) & 0xff) << 8);
            result[i] |= (long) ((((long) buffer[ptr++]) & 0xff) << 0);
        }

        return result;
    }

    /**
     * 固定小数をfloat変換して取得する。<BR>
     * GL仕様のため、符号1 整数15 小数16の固定小数を使用する。
     *
     * @author eagle.sakura
     * @return
     * @version 2009/11/23 : 新規作成
     */
    public float readGLFixedFloat() throws IOException {
        return ((float) readS32()) / (float) 0x10000;
    }

    /**
     * 固定小数をdouble変換して取得する。<BR>
     * GL仕様のため、符号1 整数47 小数16の固定小数を使用する。
     *
     * @author eagle.sakura
     * @return
     * @version 2010/03/24 : 新規作成
     */
    public double readGLFixedDouble() throws IOException {
        return ((double) readS64()) / (double) 0x10000;
    }

    /**
     * IEEE754形式のビット列をfloatに変換し、取得する。
     *
     * @author eagle.sakura
     * @return
     * @throws IOException
     * @version 2010/04/19 : 新規作成
     */
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readS32());
    }

    /**
     * IEEE754形式のビット列をdoubleに変換し、取得する。
     *
     * @author eagle.sakura
     * @return
     * @throws IOException
     * @version 2010/04/19 : 新規作成
     */
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readS64());
    }

    /**
     * 真偽の値を取得する。<BR>
     * 1byte読み取り、0ならfalse、それ以外ならtrueを返す。
     *
     * @author eagle.sakura
     * @return
     * @throws IOException
     * @version 2010/05/28 : 新規作成
     */
    public boolean readBoolean() throws IOException {
        return readS8() == 0 ? false : true;
    }

    /**
     * 文字列を読み取る。<BR>
     * エンコードはShiftJISである必要がある。<BR>
     * 頭2byteが文字数、後に文字配列が続く。
     *
     * @author eagle.sakura
     * @return
     * @version 2009/11/15 : 新規作成
     */
    public String readString() throws IOException {
        int len = readS16();
        if (len <= 0) {
            return "";
        }
        byte[] buf = new byte[len];
        readBuffer(buf, len);

        return new String(buf, "Shift_JIS");
    }

    /**
     * バッファを直接読み取る。
     *
     * @author eagle.sakura
     * @param length
     * @return
     * @version 2009/11/15 : 新規作成
     */
    public byte[] readBuffer(int length) throws IOException {
        byte[] ret = new byte[length];
        readBuffer(ret, length);
        return ret;
    }

    /**
     * ファイルを作成する。
     *
     * @author eagle.sakura
     * @return
     * @version 2010/02/22 : 新規作成
     */
    public byte[] readFile() throws IOException {
        int len = readS32();
        byte[] ret = readBuffer(len);

        return ret;
    }

    /**
     * バッファから必要な長さを読み取る。
     *
     * @author eagle.sakura
     * @param buf
     * @param length
     * @version 2009/11/15 : 新規作成
     */
    public void readBuffer(byte[] buf, int length) throws IOException {
        readBuffer(buf, 0, length);
    }

    /**
     *
     * @author eagle.sakura
     * @param buf
     * @param index
     * @param length
     * @version 2009/11/15 : 新規作成
     */
    public int readBuffer(byte[] buf, int index, int length) throws IOException {
        return reader.read(buf, index, length);
    }

    /**
     * 資源の解放を行う。<BR>
     * 内部管理する{@link #reader}のdispose()を行う。
     *
     * @author eagle.sakura
     * @version 2009/11/15 : 新規作成
     */
    public void dispose() {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
                LogUtil.log(e);
            }
            reader = null;
        }
    }

    /**
     *
     * @author eagle.sakura
     * @throws Throwable
     * @version 2010/07/12 : 新規作成
     */
    protected void finalize() throws Throwable {
        super.finalize();
        dispose();
    }

    /**
     * 読み取り位置を指定する。
     *
     * @author eagle.sakura
     * @param eSeekType
     * @param pos
     * @version 2009/11/15 : 新規作成
     */
    public void seek(SeekType type, int pos) throws IOException {
        type.set(reader, pos);
    }

    /**
     * シークの種類を定義する。
     * @author Takeshi
     *
     */
    public enum SeekType {
        /**
         * 現在位置を起点にする
         */
        Current {
            @Override
            public void set(InputStream stream, int pos) throws IOException {
                stream.skip(pos);
            }
        },

        /**
         * 直接指定する
         */
        Set {
            @Override
            public void set(InputStream stream, int pos) throws IOException {
                stream.reset();
                stream.skip(pos);
            }
        };

        public abstract void set(InputStream stream, int pos) throws IOException;
    }
}
