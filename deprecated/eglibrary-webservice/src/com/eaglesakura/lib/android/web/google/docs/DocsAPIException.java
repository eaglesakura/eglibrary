package com.eaglesakura.lib.android.web.google.docs;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;

import com.google.api.client.http.HttpResponseException;

public class DocsAPIException extends Exception {
    public static final long serialVersionUID = 0x01;

    String infomation = "";
    Type type = Type.Unknown;
    Exception baseException = null;

    public enum Type {

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

    public DocsAPIException(Type type, String info) {
        this.type = type;
        this.infomation = info;
    }

    public DocsAPIException(Type type, Exception base) {
        this(type, base.getMessage());

        if (base instanceof UnknownHostException) {
            type = Type.UnknownHost;
        }

        this.baseException = base;
    }

    public Type getType() {
        return type;
    }

    public String getInfomation() {
        return infomation;
    }

    public Exception getBaseException() {
        return baseException;
    }

    /**
     * 例外タイプに変換する
     * @param base
     * @return
     */
    public static DocsAPIException.Type toExceptionType(Exception base) {
        if (base instanceof HttpResponseException) {
            HttpResponseException hre = (HttpResponseException) base;
            switch (hre.response.statusCode) {
                case 401:
                case 403:
                    return Type.AuthError;
            }
            return Type.APIResponseError;
        }
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

        return Type.Unknown;
    }
}