package com.eaglesakura.lib.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.eaglesakura.lib.android.game.io.BufferTargetOutputStream;
import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.FileUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * ファイル用のダウンローダー
 * @author TAKESHI YAMASHITA
 *
 */
public class WebFileDownloader extends DisposableResource {

    WebAPIConnectorBase connector = null;
    WebAPIConnection connection = null;

    String downloadUrl = null;
    long contentLength = 0;

    byte[] buffer = new byte[1024 * 16];

    /**
     * 
     * @param connector
     * @param item
     * @throws GoogleAPIException
     */
    public WebFileDownloader(WebAPIConnectorBase connector, String downloadUrl, long contentLength)
            throws WebAPIException {
        this.connector = connector;
        this.downloadUrl = downloadUrl;
        this.contentLength = contentLength;
    }

    /**
     * レジュームを開始する
     * @param dstFile
     * @return レジューム操作を開始したらtrue
     */
    public boolean resume(File dstFile) throws WebAPIException {
        if (contentLength > 0) {
            connection = connector.download(downloadUrl, dstFile.length(), contentLength);
            return true;
        } else {
            start();
            return false;
        }
    }

    /**
     * レンジを指定して開始する
     * @param rangeStart
     * @param rangeEnd
     */
    public void start(long rangeStart, long rangeEnd) throws WebAPIException {
        connection = connector.download(downloadUrl, rangeStart, rangeEnd);
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

    /**
     * ダウンロード中の制御を行わせる。
     *
     */
    public interface DownloadCallback {
        /**
         * ダウンロードをキャンセルする場合はtrueを返す
         * @return
         */
        boolean isCanceled(WebFileDownloader downloader);

        /**
         * ダウンロードを開始するタイミングで呼び出される
         * @param file
         */
        void onStart(WebFileDownloader downloader, File dstFile);

        /**
         * ダウンロードの進捗が進むごとに呼び出される。
         * ファイルが小さい場合は呼び出されない場合もある
         * @param file
         * @param downloaded
         */
        void onUpdate(WebFileDownloader downloader, File file, long downloaded);
    }

    /**
     * ダウンロードを行わせる。
     * @param downloader
     * @param dstFile
     * @param callback
     */
    public static boolean download(WebFileDownloader downloader, File dstFile, DownloadCallback callback)
            throws WebAPIException {
        // 親ディレクトリを作成する
        FileUtil.mkdir(dstFile.getParentFile());

        if (dstFile.length() > 0) {
            downloader.resume(dstFile);
        } else {
            downloader.start();
        }
        callback.onStart(downloader, dstFile);

        try {
            BufferTargetOutputStream tempStream = new BufferTargetOutputStream(new byte[1024 * 64]);
            FileOutputStream output = new FileOutputStream(dstFile, dstFile.length() > 0);
            try {

                while (!downloader.nextDownload(tempStream, tempStream.getBufferSize())) {
                    if (callback.isCanceled(downloader)) {
                        // キャンセルされているなら書き込まずに終了する
                        return false;
                    } else {
                        // 実ファイルへ書き込む
                        output.write(tempStream.getBuffer(), 0, tempStream.getWriteIndex());
                        output.flush();
                        callback.onUpdate(downloader, dstFile, dstFile.length());
                    }

                    // 書き込み位置を戻す
                    tempStream.reset();
                }

                if (!callback.isCanceled(downloader)) {
                    // 実ファイルへ書き込む
                    output.write(tempStream.getBuffer(), 0, tempStream.getWriteIndex());
                }

                // 正常に完了した
                return true;
            } finally {
                tempStream.close();
                output.flush();
                output.close();
            }

        } catch (IOException e) {
            throw new WebAPIException(e);
        }
    }
}
