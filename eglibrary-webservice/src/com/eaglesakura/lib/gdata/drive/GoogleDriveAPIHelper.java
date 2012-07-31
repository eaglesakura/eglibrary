package com.eaglesakura.lib.gdata.drive;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import net.arnx.jsonic.JSON;

import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.gdata.GoogleAPIConnector;
import com.eaglesakura.lib.gdata.GoogleAPIConnector.GoogleConnection;
import com.eaglesakura.lib.gdata.GoogleAPIException;
import com.eaglesakura.lib.gdata.GoogleAPIException.Type;

/**
 * Google DriveのAPI呼び出しを行う
 * @author TAKESHI YAMASHITA
 *
 */
public class GoogleDriveAPIHelper {

    /**
     * リンク情報にしたがってアップデートを続ける
     * @param nextLink
     * @return
     * @throws GoogleAPIException
     */
    private static List<DriveItem> listup(GoogleAPIConnector connector, String baseURL, int maxResults)
            throws GoogleAPIException {

        if (maxResults < 1) {
            maxResults = 999999;
        }
        List<DriveItem> result = new LinkedList<GoogleDriveAPIHelper.DriveItem>();

        String nextLink = baseURL;
        do {
            DriveItemList parsed = null;
            // 次のURLからダウンロードする
            GoogleConnection connection = connector.get(nextLink, null);
            try {
                if (connection.getResponceCode() != 200) {
                    throw new GoogleAPIException(connection.getResponceCode());
                }

                // パースを行う
                parsed = JSON.decode(connection.getInput(), DriveItemList.class);

                // パースができなければ何もしない
                if (parsed == null) {
                    return result;
                }

                // パース結果を格納する
                if (parsed.items != null) {
                    for (DriveItem item : parsed.items) {
                        if (result.size() < maxResults) {
                            result.add(item);
                        } else {
                            // 十分な数に達した
                            return result;
                        }
                        //                        LogUtil.log("title :: " + item.title);
                    }
                }

            } catch (IOException e) {
                throw new GoogleAPIException(e);
            } finally {
                connection.dispose();
            }

            if (parsed != null && parsed.nextPageToken != null) {
                if (baseURL.indexOf('?') > 0) {
                    // 既に引数が設定されている場合はANDでつなげる
                    nextLink = (baseURL + "&pageToken=" + parsed.nextPageToken);
                } else {
                    // 引数が設定されていない場合は"?"でつなげる
                    nextLink = (baseURL + "?pageToken=" + parsed.nextPageToken);
                }
                //                nextLink = parsed.nextLink;
            } else {
                nextLink = null;
            }
            //            nextLink = parsed != null ? parsed.nextLink : null;
            LogUtil.log("nextLink = " + nextLink);
            LogUtil.log("items :: " + result.size());
        } while (nextLink != null);

        return result;
    }

    /**
     * キーワードを含んだ検索を行う
     * @param keyword
     * @return
     */
    public static String createQueryFullTextContains(String keyword) {
        return createQuery("fullText contains '" + keyword + "' and trashed = false");
    }

    /**
     * キーワードを含んだ検索を行う
     * @param keyword
     * @return
     */
    public static String createQueryTitleContains(String keyword) {
        return createQuery("fullText contains '" + keyword + "' and trashed = false");
    }

    /**
     * キーワードを含んだ検索を行う
     * @param keyword
     * @return
     */
    public static String createQuery(String raw) {
        // "mimeType = 'application/vnd.google-apps.folder'"
        try {
            return URLEncoder.encode(raw, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException("encode fauled");
        }
    }

    /**
     * リンクを指定して直接インフォメーションを取得する
     * @param url
     * @return
     * @throws GoogleAPIException
     */
    public static DriveItem infoFromLink(GoogleAPIConnector connector, String url) throws GoogleAPIException {
        GoogleConnection connection = connector.get(url, null);

        try {
            if (connection.getResponceCode() != 200) {
                throw new GoogleAPIException(connection.getResponceCode());
            }

            InputStream is = connection.getInput();

            DriveItem item = JSON.decode(is, DriveItem.class);

            if (item == null) {
                throw new GoogleAPIException("item == null", Type.APIResponseError);
            }

            return item;
        } catch (IOException e) {
            throw new GoogleAPIException(e);
        } finally {
            if (connection != null) {
                connection.dispose();
            }
        }
    }

    /**
     * 親を持っている場合はtrue
     * @param item
     * @return
     */
    public static boolean hasParent(DriveItem item) {
        if (item.parents != null && item.parents.length > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * MD5が存在したらファイル
     * @param item
     * @return
     */
    public static boolean isFile(DriveItem item) {
        return item.md5Checksum != null;
    }

    /**
     * 親オブジェクトを取得する
     * @param connector
     * @return
     * @throws IOException
     */
    public static DriveItem getParent(DriveItem item, GoogleAPIConnector connector) throws GoogleAPIException {
        if (!hasParent(item)) {
            throw new GoogleAPIException("item is root :: " + item.title, Type.FileNotFound);
        }
        return infoFromLink(connector, item.parents[0].parentLink);
    }

    /**
     * 特定のディレクトリ配下を取得する
     * @param item
     * @param connector
     * @return
     * @throws GoogleAPIException
     */
    public static List<DriveItem> ls(DriveItem item, GoogleAPIConnector connector) throws GoogleAPIException {
        if (isFile(item)) {
            // ファイルに対してlsは行えない
            throw new GoogleAPIException("item is file", Type.FileNotFound);
        }
        String query = createQuery("'" + item.id + "' in parents and trashed = false");
        return search(connector, query);
    }

    /**
     * ルートディレクトリを取得する
     * @param connector
     * @return
     */
    public static DriveItem rootDirectory(GoogleAPIConnector connector) throws GoogleAPIException {
        DriveItem item = pick(connector);
        if (item == null) {
            throw new GoogleAPIException("root not found", Type.FileNotFound);
        }

        // 親があるなら親をたどる
        while (hasParent(item)) {
            item = getParent(item, connector);
        }

        return item;

    }

    /**
     * 検索を行う
     * @param connector
     * @return
     * @throws GoogleAPIException
     */
    public static List<DriveItem> search(GoogleAPIConnector connector, String query) throws GoogleAPIException {
        String nextLink = "https://www.googleapis.com/drive/v2/files?q=" + query;
        return listup(connector, nextLink, -1);
    }

    /**
     * 一覧を取得する
     * @param connector
     * @return
     * @throws GoogleAPIException
     */
    public static List<DriveItem> list(GoogleAPIConnector connector) throws GoogleAPIException {
        String nextLink = "https://www.googleapis.com/drive/v2/files";
        return listup(connector, nextLink, -1);
    }

    /**
     * 適当な１アイテムを取得する。
     * @param connector
     * @return
     * @throws GoogleAPIException
     */
    public static DriveItem pick(GoogleAPIConnector connector) throws GoogleAPIException {
        String nextLink = "https://www.googleapis.com/drive/v2/files?maxResults=1";
        List<DriveItem> result = listup(connector, nextLink, 1);

        if (result.size() == 1) {
            return result.get(0);
        } else {
            return null;
        }
    }

    /**
     * Drive上のデータリスト
     * @author TAKESHI YAMASHITA
     *
     */
    public static class DriveItemList {
        public String kind = null;
        public String etag = null;
        public String selfLink = null;

        /**
         * 次のページを検索するためのトークン
         */
        public String nextPageToken = null;

        /**
         * 次のページへのリンク
         */
        public String nextLink = null;

        public DriveItem[] items = null;
    }

    /**
     * Drive上でパースするJSONデータ
     * @author TAKESHI YAMASHITA
     *
     */
    public static class DriveItem {
        /**
         * 種類
         */
        public String kind = null;

        /**
         * ファイルのID
         */
        public String id = null;

        /**
         * ファイルのetag
         */
        public String etag = null;

        /**
         * このファイル情報へのリンク
         */
        public String selfLink = null;

        public String alternateLink = null;

        /**
         * ファイル／フォルダ名
         */
        public String title = null;

        /**
         * mime情報
         */
        public String mimeType = null;

        /**
         * ラベル情報
         */
        public LabelData labels = null;

        /**
         * 作成日
         */
        public String createdDate = null;

        /**
         * 変更日
         */
        public String modifiedDate = null;

        /**
         * 
         */
        public String modifiedByMeDate = null;

        /**
         * 親情報
         */
        public ParentData[] parents = null;

        /**
         * データダウンロード用URL
         */
        public String downloadUrl = null;

        public String originalFilename = null;

        public String fileExtension = null;

        /**
         * ファイルのチェックサム
         */
        public String md5Checksum = null;

        /**
         * ファイルのサイズ
         */
        public Long fileSize = null;

        public String quotaBytesUsed = null;

        public String[] ownerNames = null;
    }

    /**
     * ラベル情報
     * @author TAKESHI YAMASHITA
     *
     */
    public static class LabelData {
        public Boolean starred = null;
        public Boolean hidden = null;
        public Boolean trashed = null;
        public Boolean restricted = null;
        public Boolean viewed = null;
    }

    /**
     * 親情報
     */
    public static class ParentData {
        public String kind = null;
        public String id = null;
        public String selfLink = null;
        public String parentLink = null;
        public Boolean isRoot = null;
    }
}
