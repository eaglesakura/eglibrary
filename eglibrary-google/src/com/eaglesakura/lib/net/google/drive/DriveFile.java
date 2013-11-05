package com.eaglesakura.lib.net.google.drive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.eaglesakura.lib.android.game.io.BufferTargetOutputStream;
import com.eaglesakura.lib.android.game.util.FileUtil;
import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.net.WebAPIConnectorBase;
import com.eaglesakura.lib.net.WebAPIException;
import com.eaglesakura.lib.net.WebAPIException.Type;
import com.eaglesakura.lib.net.WebFileDownloader;
import com.eaglesakura.lib.net.google.drive.GoogleDriveAPIHelper.DriveItem;
import com.eaglesakura.lib.net.google.drive.GoogleDriveAPIHelper.ParentData;

/**
 * GDrive上のファイルを扱う。
 * @author TAKESHI YAMASHITA
 *
 */
public class DriveFile {
    GoogleDriveAPIHelper.DriveItem item;

    /**
     * 親ディレクトリ
     */
    DriveFile parent;

    public DriveFile(GoogleDriveAPIHelper.DriveItem item) {
        this.item = item;
    }

    /**
     * 配下にあるファイル一覧を取得する
     * @param connector
     * @return
     * @throws GoogleAPIException
     */
    public List<DriveFile> list(WebAPIConnectorBase connector) throws WebAPIException {
        if (!isDirectory()) {
            throw new WebAPIException("item is not direcotry :: " + item.title, Type.FileNotFound);
        }

        List<DriveFile> result = new LinkedList<DriveFile>();

        List<DriveItem> rawItems = GoogleDriveAPIHelper.ls(item, connector);

        for (DriveItem raw : rawItems) {
            DriveFile file = new DriveFile(raw);
            file.parent = this;
            result.add(file);
        }

        return result;
    }

    /**
     * ダウンローダーを作成する
     * @param connector
     * @return
     * @throws GoogleAPIException
     */
    public WebFileDownloader createDownloader(WebAPIConnectorBase connector) throws WebAPIException {
        if (!isFile() || item.downloadUrl == null) {
            throw new WebAPIException("item is not file :: " + item.title, Type.FileNotFound);
        }
        return new WebFileDownloader(connector, item.downloadUrl, getFileSize());
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
        boolean isCanceled();

        /**
         * ダウンロードを開始するタイミングで呼び出される
         * @param file
         */
        void onStart(DriveFile file);

        /**
         * ダウンロードの進捗が進むごとに呼び出される。
         * ファイルが小さい場合は呼び出されない場合もある
         * @param file
         * @param downloaded
         */
        void onUpdate(DriveFile file, long downloaded);
    }

    /**
     * dstFileに対し、rangeBegin-rangeEndの範囲をダウンロードする。
     * dstFileは常に上書きが行われるため、レジュームは期待しないこと。
     * @param connector
     * @param dstFile
     * @param rangeBegin
     * @param rangeEnd
     * @param callback
     * @return
     */
    public boolean downloadRange(WebAPIConnectorBase connector, File dstFile, int rangeBegin, int rangeEnd,
            DownloadCallback callback) throws WebAPIException {
        if (!isFile()) {
            return false;
        }

        WebFileDownloader downloader = createDownloader(connector);

        // 親ディレクトリを作成する
        FileUtil.mkdir(dstFile.getParentFile());

        downloader.start(rangeBegin, rangeEnd);

        callback.onStart(this);

        try {
            BufferTargetOutputStream tempStream = new BufferTargetOutputStream(new byte[1024 * 16]);
            FileOutputStream output = new FileOutputStream(dstFile, dstFile.length() > 0);
            try {

                while (!downloader.nextDownload(tempStream, tempStream.getBufferSize())) {
                    if (callback.isCanceled()) {
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

                if (!callback.isCanceled()) {
                    // 実ファイルへ書き込む
                    output.write(tempStream.getBuffer(), 0, tempStream.getWriteIndex());
                }

                // 正常に完了した
                return true;
            } finally {
                tempStream.close();
                output.close();
            }

        } catch (IOException e) {
            throw new WebAPIException(e);
        }
    }

    /**
     * ファイルにダウンロードを行う。
     * ダウンロード中の制御はコールバックを通じて行う。
     * dstFileのサイズが0より大きい場合、自動的にレジュームが行われる
     * @param connector
     * @param dstFile
     * @param callback
     * @return
     * @throws GoogleAPIException
     */
    public boolean download(WebAPIConnectorBase connector, File dstFile, DownloadCallback callback)
            throws WebAPIException {

        if (!isFile()) {
            return false;
        }

        WebFileDownloader downloader = createDownloader(connector);

        // 親ディレクトリを作成する
        FileUtil.mkdir(dstFile.getParentFile());

        if (dstFile.length() > 0) {
            downloader.resume(dstFile);
        } else {
            downloader.start();
        }

        callback.onStart(this);

        try {
            BufferTargetOutputStream tempStream = new BufferTargetOutputStream(new byte[1024 * 256]);
            FileOutputStream output = new FileOutputStream(dstFile, dstFile.length() > 0);
            try {

                while (!downloader.nextDownload(tempStream, tempStream.getBufferSize())) {
                    if (callback.isCanceled()) {
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

                if (!callback.isCanceled()) {
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
            throw new WebAPIException(e);
        }
    }

    /**
     * 親ディレクトリを取得する
     * @param connector
     * @return
     * @throws GoogleAPIException
     */
    public DriveFile getParent(WebAPIConnectorBase connector) throws WebAPIException {
        if (isRoot()) {
            return null;
        }
        // 親を持っていなかったら問い合わせる
        if (parent == null && GoogleDriveAPIHelper.hasParent(item)) {
            DriveItem raw = GoogleDriveAPIHelper.getParent(item, connector);
            if (raw != null) {
                parent = new DriveFile(raw);
            }
        }

        return parent;
    }

    /**
     * ファイルタイトルを取得する
     * @return
     */
    public String getTitle() {
        return item.title;
    }

    /**
     * MD5チェックサムを取得する
     * @return
     */
    public String getMD5() {
        return item.md5Checksum;
    }

    /**
     * ファイルサイズを取得する
     * @return
     */
    public long getFileSize() {
        return item.fileSize;
    }

    /**
     * 更新日を取得する
     * @return
     */
    public Date getModifiDate() {
        if (item.modifiedDate == null) {
            return getCreatedDate();
        }

        try {
            SimpleDateFormat simpleDataFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS");
            return simpleDataFormat.parse(item.modifiedDate);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 作成日を取得する
     * @return
     */
    public Date getCreatedDate() {
        try {
            SimpleDateFormat simpleDataFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SS");
            return simpleDataFormat.parse(item.createdDate);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ファイルの場合はtrue
     * @return
     */
    public boolean isFile() {
        return GoogleDriveAPIHelper.isFile(item);
    }

    /**
     * 空ファイルの場合trueを設定する
     * @return
     */
    public boolean isEmptyFile() {
        return isFile() && getFileSize() == 0;
    }

    /**
     * ディレクトリの場合はtrue
     * @return
     */
    public boolean isDirectory() {
        return GoogleDriveAPIHelper.isDirectory(item);
    }

    /**
     * ルートディレクトリの場合はtrueを返す。
     * @return
     */
    public boolean isRoot() {
        return !GoogleDriveAPIHelper.hasParent(item);
    }

    public String getId() {
        return item.id;
    }

    /**
     * 強制的に親として認識させる
     * @param parent
     */
    public void setParentForcing(DriveFile parent) {
        this.parent = parent;
        ParentData data = new ParentData();
        data.id = parent.getId();
        data.isRoot = parent.isRoot();
        data.kind = parent.item.kind;
        data.parentLink = parent.item.selfLink;
        data.selfLink = parent.item.selfLink;

        this.item.parents = new ParentData[] {
            data
        };

    }

    /**
     * 親との接続情報を設定する
     * @param parent
     * @return
     */
    public boolean setParent(DriveFile parent) {
        if (isRoot()) {
            return false;
        }

        for (ParentData pRaw : item.parents) {
            if (parent.getId().equals(pRaw.id)) {
                // IDが一致したから、親として認識する
                this.parent = parent;
                return true;
            }
        }

        return false;
    }

    /**
     * 親ディレクトリ一覧を取得する
     * @return
     */
    public List<String> getParentIds() {
        List<String> parents = new LinkedList<String>();
        for (int i = 0; i < item.parents.length; ++i) {
            parents.add(item.parents[i].id);
        }
        return parents;
    }

    /**
     * 直近の親IDを取得する
     * @return
     */
    public String getParentId() {
        List<String> ids = getParentIds();
        if (ids.size() > 0) {
            return ids.get(0);
        } else {
            return null;
        }
    }

    /**
     * ファイルの内容をサーバーへアップロードし、内部データを更新する。
     * @param conn
     * @param buffer
     * @throws GoogleAPIException
     */
    public void upload(WebAPIConnectorBase conn, byte[] buffer) throws WebAPIException {
        if (!isFile()) {
            throw new WebAPIException("this is not file...", Type.APICallError);
        }

        item = GoogleDriveAPIHelper.upload(conn, item, buffer);
    }

    /**
     * 絶対パスを取得する
     * @return
     */
    public String getAbsolutePath(WebAPIConnectorBase conn) throws WebAPIException {
        if (isRoot()) {
            return "/";
        }
        DriveFile current = this;
        String result = "";

        do {
            if (GameUtil.isEmpty(result)) {
                result = current.getTitle();
            } else {
                result = current.getTitle() + "/" + result;
            }
            current = current.getParent(conn);
        } while ((!current.isRoot()));

        return "/" + result;
    }

    /**
     * ルートディレクトリを取得する。
     * 一切ファイルが入っていない場合、nullを返す。
     * @param connector
     * @return
     */
    public static DriveFile root(WebAPIConnectorBase connector) throws WebAPIException {
        DriveItem raw = GoogleDriveAPIHelper.rootDirectory(connector);
        return new DriveFile(raw);
    }

    /**
     * Google Driveからファイルを検索して取得する。
     * ただし、ファイル名が一意に決まることが前提となる。
     * @param connector
     * @param fileName
     * @return
     * @throws GoogleAPIException
     */
    public static DriveFile get(WebAPIConnectorBase connector, String fileName) throws WebAPIException {
        List<DriveItem> search = GoogleDriveAPIHelper.search(connector,
                GoogleDriveAPIHelper.createQueryFullTextContains(fileName), 1000);
        if (search.isEmpty()) {
            throw new WebAPIException(fileName + " not found", Type.FileNotFound);
        }
        for (DriveItem item : search) {
            if (item.title.equals(fileName)) {
                return new DriveFile(item);
            }
        }
        throw new WebAPIException(fileName + " not found", Type.FileNotFound);
    }

    /**
     * Google Driveからファイルを検索して取得する。
     * もしファイルが見つからなかった場合、新しい空ファイルを作成して返す。
     * @param connector
     * @param fileName
     * @param mimeType
     * @return
     * @throws GoogleAPIException
     */
    public static DriveFile getOrNewfile(WebAPIConnectorBase connector, String fileName, String mimeType)
            throws WebAPIException {
        DriveFile result = null;

        try {
            // まずは通常ファイルを検索する
            result = get(connector, fileName);
        } catch (WebAPIException e) {
            if (e.getType() == Type.FileNotFound) {
                // ファイルが見つからなかったら、新規ファイルを作成してしまう
                DriveItem item = new DriveItem();
                item.title = fileName;
                item.mimeType = mimeType;

                // 新しいファイルに入れ替える
                item = GoogleDriveAPIHelper.newFile(connector, item);

                result = new DriveFile(item);
            } else {
                // それ以外のエラーは死んでもらう
                throw e;
            }
        }

        return result;
    }

    /**
     * オブジェクトのラッピングを行う
     * @param list
     * @param result
     * @return
     */
    public static List<DriveFile> wrap(List<DriveItem> list, List<DriveFile> result) {
        for (DriveItem item : list) {
            result.add(new DriveFile(item));
        }
        return result;
    }

    public enum Sorter {
        /**
         * 名前順にソートする
         */
        Name {
            @Override
            public void sort(List<DriveFile> files) {
                Collections.sort(files, new Comparator<DriveFile>() {
                    @Override
                    public int compare(DriveFile lhs, DriveFile rhs) {
                        return lhs.getTitle().compareTo(rhs.getTitle());
                    }
                });
            }
        };

        /**
         * 指定の順番でソートする
         * @param files
         */
        public abstract void sort(List<DriveFile> files);
    }
}
