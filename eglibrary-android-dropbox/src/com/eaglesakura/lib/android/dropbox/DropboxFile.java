package com.eaglesakura.lib.android.dropbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.dropbox.client2.DropboxAPI.Entry;
import com.eaglesakura.lib.android.dropbox.DropboxAPIException.Type;
import com.eaglesakura.lib.android.game.io.BufferTargetOutputStream;
import com.eaglesakura.lib.android.game.util.FileUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * Dropboxのファイル情報を隠蔽する
 * @author TAKESHI YAMASHITA
 *
 */
public class DropboxFile {
    Entry entry;

    public DropboxFile(Entry entry) {
        this.entry = entry;
    }

    /**
     * ファイル名を取得する
     * @return
     */
    public String getTitle() {
        return entry.fileName();
    }

    /**
     * ファイルならtrue
     * @return
     */
    public boolean isFile() {
        return !entry.isDir;
    }

    /**
     * 詳細情報を同期する。lsを行うために必要。
     * @param helper
     * @throws DropboxAPIException
     */
    public void syncDetails(DropboxAPIHelper helper) throws DropboxAPIException {
        entry = helper.metadata(entry.path);
    }

    /**
     * ファイル一覧を返す。
     * コレがディレクトリでない場合はnullを返す。
     * @param helper
     * @return
     * @throws DropboxAPIException
     */
    public List<DropboxFile> listFiles(DropboxAPIHelper helper) throws DropboxAPIException {
        if (isFile()) {
            // fileはlist化できない。
            return null;
        }

        List<DropboxFile> result = new LinkedList<DropboxFile>();

        if (entry.contents == null || entry.contents.size() == 0) {
            // サブディレクトリのコンテンツを取得する
            syncDetails(helper);
        }

        for (Entry sub : entry.contents) {
            result.add(new DropboxFile(sub));
        }

        return result;
    }

    /**
     * ファイルのパス情報を取得する
     * @return
     */
    public String getAbsolutePath() {
        return entry.path;
    }

    /**
     * リビジョンを取得する
     * @return
     */
    public String getRev() {
        return entry.rev;
    }

    /**
     * ファイルサイズを取得する
     * @return
     */
    public long getFileSize() {
        return entry.bytes;
    }

    /**
     * ダウンロード補助クラスを作成する
     * @param helper
     * @return
     */
    public DropboxDownloader createDownloader(DropboxAPIHelper helper) {
        return new DropboxDownloader(helper, this);
    }

    /**
     * ダウンロードを直接行う。
     * このメソッドが戻った時点でダウンロードの成否が確定している。
     * @param helper
     * @param dstFile
     * @param callback
     */
    public boolean download(DropboxAPIHelper helper, File dstFile, DownloadCallback callback)
            throws DropboxAPIException {
        if (!isFile()) {
            return false;
        }

        DropboxDownloader downloader = createDownloader(helper);

        // 親ディレクトリを作成する
        FileUtil.mkdir(dstFile.getParentFile());

        if (dstFile.length() > 0) {
            //            downloader.resume(dstFile);
            throw new UnsupportedOperationException("resume not support");
        } else {
            downloader.start();
        }

        callback.onStart(this);

        try {
            BufferTargetOutputStream tempStream = new BufferTargetOutputStream(new byte[1024 * 256]);
            FileOutputStream output = new FileOutputStream(dstFile, dstFile.length() > 0);
            try {

                while (!downloader.nextDownload(tempStream, tempStream.getBufferSize())) {
                    if (callback.isCanceled(this)) {
                        // キャンセルされているなら書き込まずに終了する
                        return false;
                    } else {
                        // 実ファイルへ書き込む
                        output.write(tempStream.getBuffer(), 0, tempStream.getWriteIndex());
                        output.flush();
                        callback.onUpdate(this, dstFile.length());
                    }

                    // 書き込み位置を戻す
                    tempStream.reset();
                }

                if (!callback.isCanceled(this)) {
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
            LogUtil.log(e);
            throw new DropboxAPIException(e);
        }
    }

    /**
     * Dropboxからファイルを取得する
     * @param helper
     * @param path
     * @return
     * @throws DropboxAPIException
     */
    public static DropboxFile get(DropboxAPIHelper helper, String path) throws DropboxAPIException {
        Entry entry = helper.metadata(path);
        if (entry == null) {
            throw new DropboxAPIException(path + " not found", Type.FileNotFound);
        }
        return new DropboxFile(entry);
    }

    /**
     * ファイルを検索して結果を返す
     * @param helper
     * @param path
     * @param query
     * @return
     * @throws DropboxAPIException
     */
    public static List<DropboxFile> search(DropboxAPIHelper helper, String path, String query)
            throws DropboxAPIException {
        return search(helper, path, query, -1);
    }

    /**
     * ファイルを検索して結果を返す
     * @param helper
     * @param path
     * @param query
     * @return
     * @throws DropboxAPIException
     */
    public static List<DropboxFile> search(DropboxAPIHelper helper, String path, String query, int limit)
            throws DropboxAPIException {
        try {
            List<Entry> search = helper.getAPI().search(path, query, limit, false);
            List<DropboxFile> result = new LinkedList<DropboxFile>();
            {
                for (Entry entry : search) {
                    result.add(new DropboxFile(entry));
                }
            }
            return result;
        } catch (Exception e) {
            LogUtil.log(e);
            throw new DropboxAPIException(e);
        }
    }

    /**
     * ダウンロード進捗を受け取る
     * @author TAKESHI YAMASHITA
     *
     */
    public interface DownloadCallback {
        /**
         * キャンセルを行う場合はtrue
         * @param file
         * @return
         */
        public boolean isCanceled(DropboxFile file);

        /**
         * ダウンロードを開始する
         * @param file
         */
        public void onStart(DropboxFile file);

        /**
         * ダウンロード進捗を受け取る
         * @param file
         * @param downloaded
         */
        public void onUpdate(DropboxFile file, long downloaded);
    }
}
