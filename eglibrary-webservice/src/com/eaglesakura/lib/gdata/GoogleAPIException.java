package com.eaglesakura.lib.gdata;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;

import com.google.api.client.http.HttpResponseException;

public class GoogleAPIException extends Exception {
    static final long serialVersionUID = 0x01;

    Exception base = null;

    Type type = Type.Unknown;

    /**
     * 
     * @param baseException
     */
    public GoogleAPIException(Exception baseException) {
        super(toMessage(baseException));
        this.type = toExceptionType(baseException);
        this.base = baseException;
    }

    /**
     * 
     * @param baseException
     */
    public GoogleAPIException(GoogleOAuth2Helper.ErrorCode error, GoogleAPIException.Type type) {
        super(error.error);
        this.type = type;
    }

    /**
     * 
     * @param baseException
     */
    public GoogleAPIException(int responce) {
        super("responce :: " + responce + " :: " + toExceptionType(responce).name());
        this.type = toExceptionType(responce);
    }

    /**
     * 
     * @param baseException
     */
    public GoogleAPIException(String message, GoogleAPIException.Type type) {
        super(message);
        this.type = type;
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

    public static GoogleAPIException.Type toExceptionType(int resp) {
        switch (resp) {
            case 500:
                return Type.APIResponseError;
            case 400:
                return Type.APICallError;
            case 401:
            case 403:
                return Type.AuthError;
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
    public static GoogleAPIException.Type toExceptionType(Exception base) {
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
