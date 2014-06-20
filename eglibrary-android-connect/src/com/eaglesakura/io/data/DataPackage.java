package com.eaglesakura.io.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.zip.DataFormatException;

import com.eaglesakura.io.DataInputStream;
import com.eaglesakura.io.DataOutputStream;
import com.eaglesakura.util.LogUtil;

/**
 * Bluetooth等を介して少量のデータ（インメモリに収まる程度）をやりとりするクラス
 * bluetooth/Wi-Fi-Direct/socket通信等で使用する
 * 受け取ったデータが壊れている場合は適宜dropする
 */
public class DataPackage {

    /**
     * データの内容を一意に識別するためのID
     * 自由に指定可能で、デフォルトではランダムで生成される
     * ユーザー指定のマジックナンバー等を想定
     */
    String uniqueId;

    /**
     * エラー検証コードとヘッダを付与したデータ
     */
    byte[] packedBuffer;

    /**
     * 
     * @param uniqueId
     * @param buffer
     */
    public DataPackage(String uniqueId, byte[] buffer) {
        this.uniqueId = uniqueId;
        this.packedBuffer = pack(buffer);
    }

    /**
     * パッケージを生成する
     * @param buffer
     */
    public DataPackage(byte[] buffer) {
        this.uniqueId = UUID.randomUUID().toString();
        this.packedBuffer = pack(buffer);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * パッキングされた送信用データを取得する
     * このbufferにUniqueIDのデータは含まれない
     * @return
     */
    public byte[] getPackedBuffer() {
        return packedBuffer;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DataPackage) {
            DataPackage other = (DataPackage) o;
            return this.uniqueId.equals(other.uniqueId);
        } else {
            return false;
        }
    }

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
    public static byte[] pack(byte[] userData) {
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
    public static byte[] unpack(InputStream stream, long streamTimeoutMs) throws IOException, DataFormatException {
        DataInputStream dis = new DataInputStream(stream, false);
        dis.setDataWaitTimeMs(streamTimeoutMs);

        {
            byte[] magic = dis.readBuffer(MAGIC.length);
            for (int i = 0; i < magic.length; ++i) {
                if (magic[i] != MAGIC[i]) {
                    throw new DataFormatException("Data Format Error");
                }
            }
        }

        {
            byte[] file = dis.readFile();
            byte[] fileVerify = createVerifyCode(file, 0, file.length);
            byte[] verify = dis.readBuffer(fileVerify.length);

            for (int i = 0; i < verify.length; ++i) {
                if (verify[i] != fileVerify[i]) {
                    LogUtil.log("verify :: " + fileVerify[0] + "/" + fileVerify[1]);
                    LogUtil.log("readed verify :: " + verify[0] + "/" + verify[1]);
                    throw new DataFormatException("Verify Error");
                }
            }

            // ファイル本体を返す
            return file;
        }
    }
}
