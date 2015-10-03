package com.eaglesakura.json;

import com.eaglesakura.util.LogUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JSON {
    /**
     * クラスを文字列へエンコードする
     */
    public static void encode(OutputStream os, Object obj) throws JsonProcessingException, IOException {
        new ObjectMapper().writeValue(os, obj);
    }

    /**
     * クラスを文字列へエンコードする
     */
    public static String encode(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    /**
     * クラスを文字列へエンコードするx
     */
    public static String encodeOrNull(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            LogUtil.log(e);

            return null;
        }
    }

    /**
     * 文字列をクラスへデコードする
     */
    public static <T> T decode(String json, Class<T> clazz) throws JsonProcessingException, IOException {
        return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).readValue(json, clazz);
    }

    /**
     * 文字列をクラスへデコードする
     */
    public static <T> T decodeOrNull(String json, Class<T> clazz) {
        try {
            return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).readValue(json, clazz);
        } catch (Throwable e) {
            //            LogUtil.log(e);
            return null;
        }
    }

    /**
     * 文字列をクラスへデコードする
     */
    public static <T> T decodeOrNull(InputStream json, Class<T> clazz) {
        try {
            return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).readValue(json, clazz);
        } catch (Throwable e) {
            //            LogUtil.log(e);
            return null;
        }
    }

    /**
     * 文字列をクラスへデコードする
     */
    public static <T> T decode(InputStream is, Class<T> clazz) throws JsonProcessingException, IOException {
        return new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).readValue(is, clazz);
    }

    /**
     * JSONモデルを送信用byte[]に変換する
     */
    public static byte[] model2bytes(Object model) {
        return encodeOrNull(model).getBytes();
    }
}
