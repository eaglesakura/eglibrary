package com.eaglesakura.android.message;

import android.os.Bundle;

import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.jc.annotation.JCMethod;
import com.eaglesakura.util.StringUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Native側に投げるメッセージ
 */
@JCClass(cppNamespace = "es.glkit")
public class JointMessage {
    /**
     * メッセージの文字列
     */
    String message;

    /**
     * 引数
     */
    Bundle argments;

    public JointMessage(String message, Bundle argments) {
        this.message = message;
        this.argments = argments;
    }

    @JCMethod
    public String getMessage() {
        return message;
    }

    public Bundle getArgments() {
        return argments;
    }

    /**
     * Nativeで利用可能なBytesとして返す
     *
     * @param key
     * @return
     */
    @JCMethod
    public Buffer getNativeByteArray(String key) {
        byte[] array = argments.getByteArray(key);
        if (array != null) {
            return ByteBuffer.allocateDirect(array.length).put(array).position(0);
        } else {
            return null;
        }
    }

    @JCMethod
    public int getInt32(String key, int defValue) {
        return argments.getInt(key, defValue);
    }

    @JCMethod
    public long getInt64(String key, long defValue) {
        return argments.getLong(key, defValue);
    }

    @JCMethod
    public String getString(String key, String defValue) {
        String result = argments.getString(key);
        if (StringUtil.isEmpty(result)) {
            return defValue;
        } else {
            return key;
        }
    }

    @JCMethod
    public float getFloat(String key, float defValue) {
        return argments.getFloat(key, defValue);
    }

    @JCMethod
    public double getDouble(String key, double defValue) {
        return argments.getDouble(key, defValue);
    }


    /**
     * UTF16 LE形式のバッファをJava/NDKで利用しやすいようにUTF-8に変換する
     *
     * @param buffer
     * @return
     */
    @JCMethod
    public static byte[] utf16toString(byte[] buffer) {
        try {
            return new String(buffer, Charset.forName("UTF-16LE")).getBytes(Charset.forName("UTF-8"));
        } catch (Exception e) {
            return buffer;
        }
    }
}
