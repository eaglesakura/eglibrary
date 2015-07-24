package com.eaglesakura.android.net;

import com.eaglesakura.android.dao.net.DbNetCache;
import com.eaglesakura.android.thread.MultiRunningTasks;
import com.eaglesakura.io.IOUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Map;

/**
 * 巨大なファイルを取得する
 */
public class LargeNetworkResult<T> extends NetworkResult<T> {

    final HttpRequest request;

    final NetworkConnector.RequestParser<T> parser;

    final NetworkConnector connector;

    final NetworkConnector.CacheDatabase database;

    final long cacheTimeoutMs;

    final Map<String, String> params;

    boolean aborted;

    final File downloadFile;

    final File cacheFile;

    public LargeNetworkResult(String url, NetworkConnector connector, HttpRequest request, long cacheTimeoutMs, NetworkConnector.RequestParser<T> parser, Map<String, String> params, File downloadFile) {
        super(url);
        this.parser = parser;
        this.request = request;
        this.connector = connector;
        this.cacheTimeoutMs = cacheTimeoutMs;
        this.params = params;
        this.database = connector.getCacheDatabase();
        this.downloadFile = downloadFile;
        if (downloadFile == null) {
            this.cacheFile = null;
        } else {
            this.cacheFile = new File(downloadFile.getAbsolutePath() + "." + System.currentTimeMillis() + ".escache");
        }
    }

    @Override
    void startDownloadFromBackground() {

        try {
            if (loadFromCache()) {
                // キャッシュからの読み込みに成功したらそこで終了
                return;
            } else {
                try {
                    database.open();
                    // 読み込みに失敗したらキャッシュを廃棄する
                    database.cleanFileBlock(url);
                } finally {
                    database.close();
                }
            }
        } catch (Exception e) {
            LogUtil.log(e);
        }

        if (isCanceled()) {
            return;
        }

        loadFromNetwork();
    }

    void loadFromNetwork() {
        NetworkConnector.netDownloadTask.pushBack(downloadTask);
        NetworkConnector.netDownloadTask.start();
    }

    InputStream openInputStream(String url) throws IOException {
        if (downloadFile != null) {
            return new FileInputStream(downloadFile);
        } else {
            return new BlockInputStream(connector.getCacheDatabase(), url);
        }
    }

    boolean loadFromCache() throws Exception {
        DbNetCache cache = connector.loadCache(url);

        if (cache == null || cache.getBodySize() != cache.getDownloadedSize()) {
            // cacheが無効か、ダウンロードが完了していない
            return false;
        }

        // キャッシュDrop前にハッシュを保存する
        oldDataHash = cache.getHash();

        if (connector.dropTimeoutCache(cache, cacheTimeoutMs)) {
            return false;
        }

        InputStream is = openInputStream(url);
        T parse;
        try {
            parse = parser.parse(this, is);
        } finally {
            IOUtil.close(is);
        }

        if (parse != null) {
            downloadedDataSize = cache.getBodySize();
            currentDataHash = oldDataHash;
            onReceived(parse);
            return true;
        } else {
            return false;
        }
    }

    NetworkResult<T> self() {
        return this;
    }

    @Override
    void abortRequest() {
        aborted = true;
    }


    OutputStream openOutputStream() throws IOException {
        if (downloadFile != null) {
            return new FileOutputStream(cacheFile);
        } else {
            return new BlockOutputStream(database, url, 0);
        }
    }

    private MultiRunningTasks.Task downloadTask = new MultiRunningTasks.Task() {
        @Override
        public boolean begin(MultiRunningTasks runnner) {
            return !isCanceled();
        }

        @Override
        public void run(MultiRunningTasks runner) {

            try {
                database.open();

                byte[] buffer = new byte[1024 * 32];
                request.setConnectTimeout(1000 * 60);
                request.setFollowRedirects(true);

                HttpResponse resp = request.execute();
                InputStream content = resp.getContent();
                HttpHeaders headers = resp.getHeaders();
                int code = resp.getStatusCode();
                LogUtil.log("Resp url(%s) status(%d) headers(%s)", url, code, "" + headers);

                final MessageDigest md = MessageDigest.getInstance("SHA-1");
                OutputStream os = openOutputStream();
                int readSize = 0;

                boolean completed = false;

                try {
                    if ((code / 100) != 2) {
                        LogUtil.log("Status Code(%d) != success", code);
                        throw new IOException(String.format("StatusCode Error(%d)", code));
                    }


                    while ((readSize = content.read(buffer)) >= 0) {
                        if (isCanceled() || aborted) {
                            throw new IllegalStateException("Abort || Cancel Download");
                        }

                        if (readSize > 0) {
                            os.write(buffer, 0, readSize);
                            md.update(buffer, 0, readSize);
                            synchronized (this) {
                                downloadedDataSize += readSize;
                            }
                            if (listener instanceof Listener2) {
                                ((Listener2) listener).onDownloadProgress(buffer, readSize);
                            }
                        }

                    }

                    // 正常終了したため、finish
                    if (!isCanceled()) {
                        completed = true;
                    }
                } finally {
                    if (completed && os instanceof BlockOutputStream) {
                        ((BlockOutputStream) os).onCompleted();
                    } else {
                        database.cleanFileBlock(getUrl());
                    }
                    os.close();
                    resp.disconnect();


                    if (completed && os instanceof FileOutputStream) {
                        // キャッシュから本番に移す
                        if (downloadFile.isFile()) {
                            downloadFile.delete();
                        }

                        // mvする
                        cacheFile.renameTo(downloadFile);
                    }

                    // cacheは例外なく削除する
                    if (cacheFile != null) {
                        cacheFile.delete();
                    }
                }

                currentDataHash = StringUtil.toHexString(md.digest());
                LogUtil.log("data(%s) hash old(%s) -> new(%s) modified(%s)",
                        url,
                        oldDataHash, currentDataHash,
                        "" + isDataModified()
                );

                // 読み取ったデータをパーサーにかける
                InputStream is = openInputStream(url);
                T parse;
                try {
                    parse = parser.parse(self(), is);
                } finally {
                    IOUtil.close(is);
                }

                if (parse != null) {
//                    currentDataHash = oldDataHash;
                    onReceived(parse);
                    // キャッシュに追加する
                    if (cacheTimeoutMs > 10 && !isCanceled()) {
                        putCache(url, headers, request.getRequestMethod(), cacheTimeoutMs);
                    }
                } else {
                    throw new IOException("DataParse Error");
                }
            } catch (Exception e) {
                LogUtil.log(e);
                onError(e);
            } finally {
                database.close();
            }

        }

        @Override
        public void finish(MultiRunningTasks runner) {
        }
    };

    /**
     * URLを指定してキャッシュとして登録する
     *
     * @param url
     * @param timeoutMs
     */
    protected void putCache(final String url, final HttpHeaders headers, final String method, final long timeoutMs) {
        if (downloadedDataSize == 0) {
            return;
        }

        Runnable task = new Runnable() {
            @Override
            public void run() {
                NetworkConnector.CacheDatabase db = database;

                DbNetCache cache = new DbNetCache();
                cache.setUrl(url);
                cache.setBodySize((int) downloadedDataSize);
                cache.setDownloadedSize((int) downloadedDataSize);
                cache.setBlockSize(NetworkConnector.BLOCK_SIZE);
                cache.setCacheType(NetworkConnector.CACHETYPE_DB);
                cache.setMethod(method.toUpperCase());
                cache.setCacheTime(new Date());
                cache.setCacheLimit(new Date(System.currentTimeMillis() + timeoutMs));

                if (headers != null && headers.get("ETag") != null) {
                    cache.setEtag(headers.get("ETag").toString());
                }
                cache.setHash(currentDataHash);

                try {
                    db.open();
                    db.put(cache);
                } finally {
                    db.close();
                }
            }
        };
        task.run();
//        NetworkConnector.cacheWorkTask.pushFront();
//        NetworkConnector.cacheWorkTask.start();
    }
}
