package com.eaglesakura.lib.android.web.google.docs;

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
}