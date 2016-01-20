package com.eaglesakura.android.net.cache.file;

import com.eaglesakura.android.net.cache.ICacheWriter;
import com.eaglesakura.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * ファイルに直接キャッシュを書き込む
 */
public class FileCacheWriter implements ICacheWriter {
    final File file;

    final File temp;

    final FileOutputStream stream;

    public FileCacheWriter(File file) throws IOException {
        IOUtil.mkdirs(file.getParentFile());
        this.file = file;
        this.temp = new File(file.getAbsolutePath() + "." + System.currentTimeMillis());
        this.stream = new FileOutputStream(temp);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        stream.write(buffer, offset, length);
    }

    /**
     * 正常に書き込めたら、一時ファイルを正式ファイルにリネームする
     *
     * @throws IOException
     */
    @Override
    public void commit() throws IOException {
        stream.close();
        if (file.isFile()) {
            file.delete();
        }
        file.renameTo(temp);
    }

    /**
     * 廃棄するなら、tempファイルを削除する
     *
     * @throws IOException
     */
    @Override
    public void abort() throws IOException {
        IOUtil.close(stream);
        temp.delete();
    }

    @Override
    public void close() throws IOException {

    }
}
