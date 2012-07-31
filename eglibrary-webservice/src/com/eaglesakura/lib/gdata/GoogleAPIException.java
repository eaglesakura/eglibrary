package com.eaglesakura.lib.gdata;

public class GoogleAPIException extends Exception {
    static final long serialVersionUID = 0x01;

    Exception base;

    /**
     * 
     * @param baseException
     */
    public GoogleAPIException(Exception baseException) {
        super(toMessage(baseException));
        this.base = baseException;
    }

    /**
     * 
     * @param baseException
     */
    public GoogleAPIException(GoogleOAuth2Helper.ErrorCode error) {
        super(error.error);
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
}
