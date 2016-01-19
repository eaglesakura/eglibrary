package com.eaglesakura.android.net.cache;

import com.eaglesakura.android.net.HttpHeader;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.util.EncodeUtil;
import com.eaglesakura.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 指定ディレクトリ配下でファイルとしてキャッシュを制御する
 */
public class FileCacheController implements CacheController {
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
    public byte[] getCacheOrNull(ConnectRequest request) throws IOException {
        CachePolicy policy = request.getCachePolicy();
        if (policy == null) {
            return null;
        }

        File local = getFile(policy.getCacheKey(request));
        return IOUtil.toByteArrayOrNull(local);
    }

    @Override
    public void putCache(ConnectRequest request, HttpHeader respHeader, byte[] buffer) {
        CachePolicy policy = request.getCachePolicy();
        if (policy == null) {
            return;
        }

        long timeMs = policy.getCacheLimitTimeMs();
        if (timeMs < 1000 || buffer.length > policy.getMaxItemBytes()) {
            // 適当なしきい値以下のタイムアウトは実質的に無視すべき
            // もしくは最大キャッシュサイズを超えていたら何もしない
            return;
        }

        OutputStream os = null;
        try {
            File file = getFile(policy.getCacheKey(request));
            os = new FileOutputStream(file);
            IOUtil.copyTo(new ByteArrayInputStream(buffer), true, os, false);
        } catch (Exception e) {
            IOUtil.close(os);
        }
    }
}
