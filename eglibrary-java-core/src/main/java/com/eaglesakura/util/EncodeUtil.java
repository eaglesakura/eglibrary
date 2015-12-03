package com.eaglesakura.util;

import com.eaglesakura.io.DataInputStream;
import com.eaglesakura.io.DataOutputStream;
import com.eaglesakura.io.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 暗号化・指紋サポートを行う
 */
public class EncodeUtil {

    /**
     * byte配列からMD5を求める
     *
     * @param buffer
     * @return
     */
    public static String genMD5(byte[] buffer) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer);
            byte[] digest = md.digest();

            StringBuffer sBuffer = new StringBuffer(digest.length * 2);
            for (byte b : digest) {
                String s = Integer.toHexString(((int) b) & 0xff);

                if (s.length() == 1) {
                    sBuffer.append('0');
                }
                sBuffer.append(s);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * byte配列からMD5を求める
     *
     * @return
     */
    public static String genMD5(InputStream is) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            {
                byte[] buffer = new byte[1024 * 8];
                int readed = 0;
                while ((readed = is.read(buffer)) > 0) {
                    md.update(buffer, 0, readed);
                }
            }

            byte[] digest = md.digest();

            StringBuffer sBuffer = new StringBuffer(digest.length * 2);
            for (byte b : digest) {
                String s = Integer.toHexString(((int) b) & 0xff);

                if (s.length() == 1) {
                    sBuffer.append('0');
                }
                sBuffer.append(s);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * byte配列からMD5を求める
     *
     * @param buffer
     * @return
     */
    public static String genSHA1(byte[] buffer) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(buffer);
            byte[] digest = md.digest();

            StringBuffer sBuffer = new StringBuffer(digest.length * 2);
            for (byte b : digest) {
                String s = Integer.toHexString(((int) b) & 0xff);

                if (s.length() == 1) {
                    sBuffer.append('0');
                }
                sBuffer.append(s);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Key-Valueデータを圧縮する
     *
     * @param data
     * @return
     */
    public static byte[] toByteArray(Map<String, byte[]> data) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
            DataOutputStream dos = new DataOutputStream(os, false);

            dos.writeS32((short) data.size());

            Iterator<Map.Entry<String, byte[]>> iterator = data.entrySet().iterator();

            // すべてのKey-Valueを単純にシリアライズする
            while (iterator.hasNext()) {
                Map.Entry<String, byte[]> entry = iterator.next();
                dos.writeString(entry.getKey());
                dos.writeFile(entry.getValue());
            }

            return os.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException();
        }
    }

    public static Map<String, byte[]> toKeyValue(byte[] buffer) {
        return toKeyValue(buffer, 0, buffer.length);
    }

    public static Map<String, byte[]> toKeyValue(byte[] buffer, int offset, int length) {
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(buffer, offset, length);
            DataInputStream dis = new DataInputStream(is, false);

            Map<String, byte[]> result = new HashMap<>();

            final int numData = dis.readS32();
            for (int i = 0; i < numData; ++i) {
                String key = dis.readString();
                byte[] value = dis.readFile();

                result.put(key, value);
            }

            return result;
        } catch (Exception e) {
            LogUtil.log(e);
            throw new IllegalArgumentException("FormatError");
        }
    }

    public static byte[] compressOrRaw(byte[] buffer) {
        if (buffer.length > 1024) {
            // ある程度データが大きくないと非効率的である
            byte[] resultBuffer = IOUtil.compressGzip(buffer);
            // データを比較し、もし圧縮率が高いようだったら圧縮した方を送信する
            if (resultBuffer.length < buffer.length) {
                return resultBuffer;
            } else {
                return buffer;
            }
        } else {
            return buffer;
        }
    }

    /**
     * GZIPバッファをデコードする。失敗したらnullを返却する。
     *
     * @param buffer 解凍対象バッファ
     * @return 解凍したバッファ
     */
    public static byte[] decompressOrRaw(byte[] buffer) {
        if (IOUtil.isGzip(buffer)) {
            byte[] resultBuffer = IOUtil.decompressGzipOrNull(buffer);
            if (resultBuffer == null) {
                return buffer;
            }

            LogUtil.log("decompress gzip(%d bytes) -> raw(%d bytes) %.2f compress", buffer.length, resultBuffer.length, (float) buffer.length / (float) resultBuffer.length);
            return resultBuffer;
        } else {
            return buffer;
        }
    }

}
