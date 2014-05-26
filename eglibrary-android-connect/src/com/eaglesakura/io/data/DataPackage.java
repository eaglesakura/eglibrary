package com.eaglesakura.io.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.eaglesakura.io.DataInputStream;
import com.eaglesakura.io.DataOutputStream;
import com.eaglesakura.util.LogUtil;

/**
 * Bluetooth等を介して少量のデータ（インメモリに収まる程度）をやりとりするクラス
 */
public class DataPackage {

    static final byte[] MAGIC = new byte[] {
            3, 1, 0, 3
    };

    /**
     * 検証用コードを生成する
     * @return
     */
    public static byte[] createVerifyCode(byte[] buffer, int offset, int length) {
        byte[] result = new byte[2];

        {
            // すべての配列を加算する
            for (int i = 0; i < length; ++i) {
                result[0] += buffer[offset + i];
            }
        }
        {
            // すべての配列を乗算する
            int temp = 1;
            for (int i = 0; i < length; ++i) {
                temp *= (((int) buffer[offset + i]) | 0x01);
            }
            result[1] = (byte) (temp & 0xFF);
        }

        return result;
    }

    /**
     * エンコードを行う
     * @param userData
     * @return
     */
    public static byte[] encode(byte[] userData) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(os);

            // マジックナンバー
            dos.writeBuffer(MAGIC, 0, MAGIC.length);

            // データ本体
            dos.writeFile(userData);

            // 検証コード
            {
                byte[] verify = createVerifyCode(userData, 0, userData.length);
                dos.writeBuffer(verify, 0, verify.length);
            }

            return os.toByteArray();
        } catch (Exception e) {
            LogUtil.log(e);
            return null;
        }
    }

    /**
     * パッケージをデコードする
     * @param stream
     * @param streamTimeoutMs データ切断までの猶予
     * @return
     * @throws IOException
     */
    public static byte[] decode(InputStream stream, long streamTimeoutMs) throws IOException {
        DataInputStream dis = new DataInputStream(stream);
        dis.setDataWaitTimeMs(streamTimeoutMs);
        {
            byte[] magic = dis.readBuffer(MAGIC.length);
            for (int i = 0; i < magic.length; ++i) {
                if (magic[i] != MAGIC[i]) {
                    throw new IOException("Data Format Error");
                }
            }
        }
        {
            byte[] file = dis.readFile();
            byte[] fileVerify = createVerifyCode(file, 0, file.length);
            byte[] verify = dis.readBuffer(fileVerify.length);

            for (int i = 0; i < verify.length; ++i) {
                if (verify[i] != fileVerify[i]) {
                    throw new IOException("Verify Error");
                }
            }

            // ファイル本体を返す
            return file;
        }
    }
}
