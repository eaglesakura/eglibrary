package com.eaglesakura.lib.net;

import com.eaglesakura.lib.android.game.resource.DisposableResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

public class WebAPIConnection extends DisposableResource {

    HttpURLConnection connection;

    /**
     * 正常系ストリーム
     */
    InputStream input;

    /**
     * エラーストリーム
     */
    InputStream error;

    /**
     * ステータスコード
     */
    int responceCode;

    /**
     *
     * @param connection
     */
    public WebAPIConnection(int status, HttpURLConnection connection) {
        this.connection = connection;
        this.responceCode = status;
    }

    /**
     * HTTPのレスポンスコードを取得する
     */
    public int getResponceCode() {
        return responceCode;
    }

    /**
     * 入力ストリームを取得する
     */
    public InputStream getInput() throws IOException {
        if (input == null) {
            input = connection.getInputStream();
        }
        return input;
    }

    @Override
    public void dispose() {
        try {
            if (input != null) {
                input.close();
            }
        } catch (Exception e) {
        }
        try {
            if (error != null) {
                error.close();
            }
        } catch (Exception e) {
        }

        try {
            if (connection != null) {
                connection.disconnect();
            }
        } catch (Exception e) {
        }

        input = null;
        error = null;
        connection = null;
    }
}
