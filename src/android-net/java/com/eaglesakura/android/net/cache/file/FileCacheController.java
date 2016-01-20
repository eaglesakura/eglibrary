package com.eaglesakura.android.net.cache.file;

import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.net.cache.CachePolicy;
import com.eaglesakura.android.net.cache.ICacheController;
import com.eaglesakura.android.net.cache.ICacheWriter;
import com.eaglesakura.android.net.cache.file.FileCacheWriter;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.util.EncodeUtil;
import com.eaglesakura.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 指定ディレクトリ配下でファイルとしてキャッシュを制御する
 */
public class FileCacheController implements ICacheController {
    File dir;

    String ext = "cache";

    public FileCacheController(File dir) throws IOException {
        this.dir = IOUtil.mkdirs(dir);
        if (!dir.isDirectory()) {
            throw new FileNotFoundException("Directory not found");
        }
    }

    /**
     * キャッシュ拡張子を指定する。
     * <p/>
     * 拡張子は"."を除いた文字列を指定する
     *
     * @param ext
     */
    public void setExt(String ext) {
        this.ext = ext;
    }

    private File getFile(String key) {
        String fileName = EncodeUtil.genSHA1(key.getBytes()) + "." + ext;
        return new File(dir, fileName);
    }

    @Override
    public ICacheWriter newCacheWriter(ConnectRequest request, HttpHeader respHeader) throws IOException {
        CachePolicy policy = request.getCachePolicy();
        if (CachePolicy.getCacheLimitTimeMs(policy) < 1000) {
            return null;
        }

        File local = getFile(policy.getCacheKey(request));
        if (!local.isFile()) {
            return null;
        }

        return new FileCacheWriter(local);
    }

    @Override
    public InputStream openCache(ConnectRequest request) throws IOException {
        CachePolicy policy = request.getCachePolicy();
        if (CachePolicy.getCacheLimitTimeMs(policy) < 1000) {
            return null;
        }

        File local = getFile(policy.getCacheKey(request));
        // キャッシュの限界時間を超えているため、ファイルを削除する
        if (System.currentTimeMillis() > (local.lastModified() + policy.getCacheLimitTimeMs())) {
            local.delete();
            throw new FileNotFoundException();
        }

        return new FileInputStream(local);
    }
}
