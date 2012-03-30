package com.eaglesakura.lib.android.game.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * Webからストリームを得るために利用する。
 * 
 * @author TAKESHI YAMASHITA
 * 
 */
public class WebInputStream extends InputStream {
    InputStream stream;
    String url = null;
    String auth = null;
    String method = null;
    int responceCode = 0;

    protected WebInputStream() {

    }

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return stream.read(b);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return stream.read(buffer, offset, length);
    }

    @Override
    public synchronized void reset() throws IOException {
        stream.reset();
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public void mark(int readlimit) {
        stream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return stream.skip(byteCount);
    }

    /**
     * ストリームを全てbyte配列化し、close()する。
     * 
     * @return
     */
    public byte[] toByteArray() throws IOException {
        return GameUtil.toByteArray(stream);
    }

    /**
     * レスポンスコードを取得する。
     * 
     * @return
     */
    public int getResponceCode() {
        return responceCode;
    }

    /**
     * ステータスが200ならtrueを返す。
     * 
     * @return
     */
    public boolean isStatusOK() {
        return responceCode == 200;
    }

    /**
     * getメソッドで指定URLを開く。
     * 
     * @param url
     * @param timeout
     * @return
     * @throws IOException
     */
    public static WebInputStream get(String url, int timeout) throws IOException {
        WebInputStream webInputStream = new WebInputStream();

        webInputStream.url = url;
        webInputStream.method = "GET";

        {
            HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);

            connection.setConnectTimeout(timeout);
            connection.connect();
            webInputStream.responceCode = connection.getResponseCode();
            try {
                webInputStream.stream = connection.getInputStream();
            } catch (Exception e) {
                webInputStream.stream = connection.getErrorStream();
            }
        }

        return webInputStream;
    }

    public static int dataToLocal(String url, int timeout, int maxRetry, int retryIntervalMilliSec, File local)
            throws IOException {

        //! 一時的にキャッシュに貯めておく
        final File cache = new File(local.getParentFile(), "." + GameUtil.genMD5(local.getAbsolutePath().getBytes())
                + UUID.randomUUID().hashCode() + ".cache");

        int retryNum = 0;
        cache.delete();
        while (retryNum < maxRetry) {
            try {
                GameUtil.copyTo(WebInputStream.get(url, timeout), new FileOutputStream(cache));

                //! キャッシュを実ファイルにリネームする
                local.delete();
                cache.renameTo(local);

                return retryNum;
            } catch (Exception e) {
                //! 指定時間止める。
                LogUtil.log(e);
                GameUtil.sleep(retryIntervalMilliSec);
                ++retryNum;
                cache.delete();
            }
        }

        throw new IOException("retry num over!!");
    }
}
