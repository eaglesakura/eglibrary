package com.eaglesakura.lib.gdata.drive;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.eaglesakura.lib.gdata.GoogleAPIConnector;
import com.eaglesakura.lib.gdata.GoogleAPIException;
import com.eaglesakura.lib.gdata.GoogleAPIException.Type;
import com.eaglesakura.lib.gdata.drive.GoogleDriveAPIHelper.DriveItem;
import com.eaglesakura.lib.gdata.drive.GoogleDriveAPIHelper.ParentData;

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
    public List<DriveFile> list(GoogleAPIConnector connector) throws GoogleAPIException {
        if (!isDirectory()) {
            throw new GoogleAPIException("item is not direcotry :: " + item.title, Type.FileNotFound);
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
    public DriveFileDownloader createDownloader(GoogleAPIConnector connector) throws GoogleAPIException {
        if (!isFile()) {
            throw new GoogleAPIException("item is not file :: " + item.title, Type.FileNotFound);
        }
        return new DriveFileDownloader(connector, item);
    }

    /**
     * 親ディレクトリを取得する
     * @param connector
     * @return
     * @throws GoogleAPIException
     */
    public DriveFile getParent(GoogleAPIConnector connector) throws GoogleAPIException {
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
     * 絶対パスを取得する
     * @return
     */
    public String getAbsolutePath(GoogleAPIConnector conn) throws GoogleAPIException {
        if (isRoot()) {
            return "/";
        }
        DriveFile current = this;
        String result = "";

        do {
            if (result.isEmpty()) {
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
    public static DriveFile root(GoogleAPIConnector connector) {
        try {
            DriveItem raw = GoogleDriveAPIHelper.rootDirectory(connector);
            return new DriveFile(raw);
        } catch (Exception e) {
            return null;
        }
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
}
