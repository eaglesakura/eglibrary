package com.eaglesakura.lib.gdata.drive;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.gdata.GoogleAPIConnector;
import com.eaglesakura.lib.net.WebAPIConnection;
import com.eaglesakura.lib.net.WebAPIException;
import com.eaglesakura.lib.net.WebAPIException.Type;

/**
 * ファイル用のダウンローダー
 * @author TAKESHI YAMASHITA
 *
 */
public class DriveFileDownloader extends DisposableResource {

    GoogleAPIConnector connector = null;
    WebAPIConnection connection = null;

    GoogleDriveAPIHelper.DriveItem item;

    byte[] buffer = new byte[1024 * 16];

    /**
     * 
     * @param connector
     * @param item
     * @throws GoogleAPIException
     */
    public DriveFileDownloader(GoogleAPIConnector connector, GoogleDriveAPIHelper.DriveItem item)
            throws WebAPIException {

        if (!GoogleDriveAPIHelper.isFile(item) || item.downloadUrl == null) {
            throw new WebAPIException("item is not file :: " + item.title, Type.FileNotFound);
        }

        this.connector = connector;
        this.item = item;
    }

    /**
     * レジュームを開始する
     * @param dstFile
     */
    public void resume(File dstFile) throws WebAPIException {
        connection = connector.download(item.downloadUrl, dstFile.length(), item.fileSize);
    }

    /**
     * レンジを指定して開始する
     * @param rangeStart
     * @param rangeEnd
     */
    public void start(long rangeStart, long rangeEnd) throws WebAPIException {
        connection = connector.download(item.downloadUrl, rangeStart, rangeEnd);
    }

    /**
     * 先頭から開始する
     */
    public void start() throws WebAPIException {
        start(-1, -1);
    }

    /**
     * 指定バイト数をDLする
     * @param os
     * @param length
     * @return 読み込みが終了したらtrue
     * @throws GoogleAPIException
     */
    public boolean nextDownload(OutputStream os, int length) throws WebAPIException {
        try {
            while (length > 0) {
                // 必要なサイズだけ読み込む
                int readed = connection.getInput().read(buffer, 0, Math.min(length, buffer.length));

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
            throw new WebAPIException(e);
        }
    }

    @Override
    public void dispose() {
        if (connection != null) {
            connection.dispose();
            connection = null;
        }
    }
}
