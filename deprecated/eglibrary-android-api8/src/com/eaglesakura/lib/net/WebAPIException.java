package com.eaglesakura.lib.net;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;

public class WebAPIException extends Exception {
    static final long serialVersionUID = 0x01;

    Exception base = null;

    Type type = Type.Unknown;

    int responce = -1;

    /**
     * 
     * @param baseException
     */
    public WebAPIException(Exception baseException) {
        super(toMessage(baseException));
        this.type = toExceptionType(baseException);
        this.base = baseException;
    }

    /**
     * 
     * @param baseException
     */
    public WebAPIException(int responce) {
        super("responce :: " + responce + " :: " + toExceptionType(responce).name());
        this.type = toExceptionType(responce);
        this.responce = responce;
    }

    /**
     * 
     * @param baseException
     */
    public WebAPIException(String message, WebAPIException.Type type) {
        super(message);
        this.type = type;
    }

    /**
     * 
     * @param baseException
     */
    public WebAPIException(String message, int responce, WebAPIException.Type type) {
        super(message);
        this.type = type;
        this.responce = responce;
    }

    /**
     * HTTP戻り値
     * @return
     */
    public int getResponceCode() {
        return responce;
    }

    /**
     * エラーの種類を取得する
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * 元になった例外を取得する
     * @return
     */
    public Exception getBase() {
        return base;
    }

    static String toMessage(Exception baseException) {
        return "error";
    }

    /**
     * エラーの種類
     * @author TAKESHI YAMASHITA
     *
     */
    public enum Type {
        /**
         * MD5の検証に失敗した
         */
        MD5ChecksumError,

        /**
         * ファイルが見つからなかった。
         */
        FileNotFound,

        /**
         * 認証に失敗した
         */
        AuthError,

        /**
         * ストリームへの書き込みに失敗した
         */
        StreamWriteError,

        /**
         * APIの戻りがおかしい
         */
        APIResponseError,

        /**
         * ファイルのレンジがおかしい
         */
        OutOfFileRange,

        /**
         * 接続にタイムアウトした
         */
        ConnectErrorTimeout,

        /**
         * ConnectionPoolTimeoutExceptionが発生した
         */
        ConnectPoolError,

        /**
         * 接続に失敗した
         */
        ConnectErrorUnknown,

        /**
         * ホストが見つからない
         */
        UnknownHost,

        /**
         * API呼び出しがおかしい
         */
        APICallError,

        /**
         * 何らかのエラーが発生した
         */
        Unknown,
    }

    public static WebAPIException.Type toExceptionType(int resp) {
        switch (resp) {
            case 500:
                return Type.APIResponseError;
            case 400:
                return Type.APICallError;
            case 401:
            case 403:
                return Type.AuthError;
            case 404:
                return Type.FileNotFound;
            case 416:
                return Type.OutOfFileRange;
        }

        return Type.Unknown;
    }

    /**
     * 例外タイプに変換する
     * @param base
     * @return
     */
    public static WebAPIException.Type toExceptionType(Exception base) {
        if (base instanceof ConnectTimeoutException) {
            return Type.ConnectErrorTimeout;
        }
        if (base instanceof ConnectionPoolTimeoutException) {
            return Type.ConnectPoolError;
        }
        if (base instanceof FileNotFoundException) {
            return Type.StreamWriteError;
        }
        if (base instanceof UnknownHostException) {
            return Type.UnknownHost;
        }
        if (base instanceof UnsupportedEncodingException) {
            return Type.APICallError;
        }
        if (base instanceof SocketTimeoutException) {
            return Type.ConnectErrorTimeout;
        }
        if (base instanceof SSLPeerUnverifiedException) {
            return Type.ConnectErrorUnknown;
        }
        if (base instanceof SSLException) {
            return Type.APIResponseError;
        }
        if (base instanceof ConnectException) {
            return Type.ConnectErrorUnknown;
        }

        return Type.Unknown;
    }
}
