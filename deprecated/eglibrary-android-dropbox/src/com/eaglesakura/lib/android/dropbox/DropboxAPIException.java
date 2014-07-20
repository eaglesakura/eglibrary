package com.eaglesakura.lib.android.dropbox;

import com.dropbox.client2.exception.DropboxSSLException;
import com.dropbox.client2.exception.DropboxServerException;

public class DropboxAPIException extends Exception {
    static final long serialVersionUID = 0x00000001L;

    public enum Type {
        /**
         * トークンがNULLだった
         */
        TokensIsNull,

        /**
         * ログインに失敗した
         */
        LoginFailed,

        /**
         * SSL接続失敗した
         */
        SSLError,

        /**
         * サーバーが正常に処理を完了できなかった
         */
        ServerError,

        /**
         * ファイルが見つからなかった
         */
        FileNotFound,

        /**
         * 不明なエラー
         */
        Unknown,
    }

    Type type;

    public DropboxAPIException(Exception base) {
        super(base.getMessage());
        this.type = toType(base);
    }

    public DropboxAPIException(String msg, Type type) {
        super(msg);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public static Type toType(Exception e) {
        if (e instanceof DropboxAPIException) {
            return ((DropboxAPIException) e).getType();
        }
        if (e instanceof DropboxSSLException) {
            return Type.SSLError;
        }
        if (e instanceof DropboxServerException) {
            return Type.ServerError;
        }
        return Type.Unknown;
    }
}
