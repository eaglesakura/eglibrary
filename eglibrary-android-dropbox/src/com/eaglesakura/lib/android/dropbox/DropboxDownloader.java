package com.eaglesakura.lib.android.dropbox;

import java.io.IOException;
import java.io.OutputStream;

import com.dropbox.client2.DropboxAPI.DropboxInputStream;
import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * ダウンロードの補助クラス
 * @author TAKESHI YAMASHITA
 *
 */
public class DropboxDownloader extends DisposableResource {
    /**
     * ダウンロード元のファイル
     */
    DropboxFile file;

    /**
     * APIヘルパ
     */
    DropboxAPIHelper helper;

    /**
     * 読み込みストリーム
     */
    DropboxInputStream stream = null;

    /**
     * 一時的なバッファ
     */
    byte[] buffer = new byte[1024 * 16];

    public DropboxDownloader(DropboxAPIHelper helper, DropboxFile file) {
        this.file = file;
        this.helper = helper;
    }

    /**
     * ダウンロードを開始する
     * @throws DropboxAPIException
     */
    public void start() throws DropboxAPIException {
        try {
            stream = helper.getAPI().getFileStream(file.getAbsolutePath(), file.getRev());
        } catch (Exception e) {
            LogUtil.log(e);
            throw new DropboxAPIException(e);
        }
    }

    /**
     * 指定バイト数を読み込む。
     * 読み込み終わったらtrueを返す
     * @param os
     * @param length
     * @return
     * @throws DropboxAPIException
     */
    public boolean nextDownload(OutputStream os, int length) throws DropboxAPIException {
        try {
            while (length > 0) {
                // 必要なサイズだけ読み込む
                int readed = stream.read(buffer, 0, Math.min(length, buffer.length));

                // 読み込んだサイズだけ書き込む
                if (readed > 0) {
                    os.write(buffer, 0, readed);
                    // 書き込んだサイズだけ必要サイズを減らす
                    length -= readed;
                } else /* if (readed < 0) */{
                    // 読み込むべきサイズが無くなった
                    return true;
                }
            }

            // まだ多分読み込める
            return false;
        } catch (IOException e) {
            LogUtil.log(e);
            throw new DropboxAPIException(e);
        }
    }

    @Override
    public void dispose() {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e) {

        }
    }
}
