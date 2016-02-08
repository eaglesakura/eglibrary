/**
 *
 */
package com.eaglesakura.lib.android.web.amazon;

import com.eaglesakura.lib.android.game.io.WebInputStream;
import com.eaglesakura.lib.android.game.util.GameUtil;
import com.eaglesakura.lib.android.game.util.LogUtil;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Amazonで扱っている商品を示す。
 * 商品情報は基本的にケータイ端末用のデータを参照するため、画像解像度は低い。
 */
public class Commodity {

    /**
     * ASINコードを示す。
     */
    String ASIN = null;
    /**
     * ISBN-10コードを示す。
     */
    String ISBN10 = null;

    /**
     * ISBN-13コードを示す。
     */
    String ISBN13 = null;

    /**
     * 商品名。
     */
    String name = null;

    /**
     * 画像URL
     */
    String imageUrl = null;

    /**
     * 商品URL
     */
    String url = null;

    /**
     * 発売日
     */
    String releaseDate = null;

    /**
     * バーコード番号
     */
    String barcode = null;

    /**
     * 出版社
     */
    String publisher = null;

    /**
     * ゲームプラットフォームを示す。
     * Sony PSP, DS...
     */
    String gamePlatform = null;

    /**
     * 価格
     */
    Integer price = null;

    /**
     * 著者、製作者
     */
    List<String> authors = null;

    /**
     * 直接は作成できない。
     */
    protected Commodity() {

    }

    /**
     * ASINコードを取得する。
     */
    public String getASIN() {
        return ASIN;
    }

    /**
     * ISBN番号を取得する。<BR>
     * ISBNが見つからなかった場合、nullを返す。
     */
    public String getISBN10() {
        return ISBN10;
    }

    /**
     * ISBN番号を取得する。<BR>
     * 通常、こちらは利用しない。
     */
    public String getISBN13() {
        return ISBN13;
    }

    /**
     * 書籍名取得
     */
    public String getName() {
        return name;
    }

    /**
     * 画像URLを取得。<BR>
     * ただし、このURLはケータイ用の縮小画像である。
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * 商品のURLを取得する。
     */
    public String getUrl() {
        return url;
    }

    /**
     * 広告URLを取得する。
     */
    public String getAdURL(String developerId) {
        return url + developerId + "/ref=nosim";
    }

    /**
     * 発売日を取得する。
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * 出版社を取得する。
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * 検索用バーコードを取得する。
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * 書籍の場合、trueを返す。
     */
    public boolean isBook() {
        return ISBN10 != null && ISBN13 != null;
    }

    /**
     * ゲームの場合、プラットフォームを取得する。
     */
    public String getGamePlatform() {
        return gamePlatform;
    }

    /**
     * ゲームの場合、trueを返す。
     */
    public boolean isGame() {
        return gamePlatform != null;
    }

    /**
     * 著者・製作者を取得する。
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * 価格を取得する。
     */
    public Integer getPrice() {
        return price;
    }

    public void printInfomation() {

        LogUtil.log("---------------------------------------------");
        LogUtil.log("ASIN : " + this.getASIN());
        LogUtil.log("価格 : " + this.getPrice());
        LogUtil.log("バーコード : " + this.getBarcode());
        LogUtil.log("画像URL : " + this.getImageUrl());
        LogUtil.log("ISBN-10 : " + this.getISBN10());
        LogUtil.log("ISBN-13 : " + this.getISBN13());
        LogUtil.log("商品名 : " + this.getName());
        for (String s : authors) {
            LogUtil.log("製作者 : " + s);
        }
        LogUtil.log("商品URL : " + this.getUrl());
        LogUtil.log("発売日 : " + this.getReleaseDate());
        LogUtil.log("出版社 : " + this.getPublisher());
        LogUtil.log("ゲームプラットフォーム : " + this.getGamePlatform());
        LogUtil.log("---------------------------------------------");
    }

    /**
     * 商品URLから商品情報を取得する。
     */
    static Commodity load(String _url, OnCommodityLoadListener listen) {
        try {
            Commodity result = new Commodity();
            result.url = _url;

            //            long start = System.currentTimeMillis();
            //! 商品名を取得
            {
                byte[] buffer = GameUtil.toByteArray(WebInputStream.get(result.url, 1000 * 10));
                String html = new String(buffer, GameUtil.SHIT_JIS);
                //                LogUtil.log(html);
                result.name = getTitle(html);
                result.imageUrl = getImageURL(html);
                result.releaseDate = getReleaseDate(html);
                result.publisher = getPublisher(html);
                result.ISBN10 = getISBN10(html);
                result.ISBN13 = getISBN13(html);
                result.ASIN = getASIN(html, result.ISBN10);
                result.url = getAsinURL(result.ASIN);
                result.gamePlatform = getGamePlatform(html);
                result.authors = getAuthors(html);
                result.price = getPrice(html);

                if (listen != null && !listen.onLoadComplete(html, result)) {
                    return null;
                }
            }
            //            LogUtil.log("Time : " + (System.currentTimeMillis() - start) + "ms");
            return result;

        } catch (Exception e) {
            LogUtil.log(e);
        }

        return null;
    }

    /**
     * バーコードからAmazon商品情報を検索する。
     */
    public static Commodity searchBarcode(String barcode, OnCommodityLoadListener listener) {
        try {
            String url = "http://www.amazon.co.jp/s/ref=nb_sb_noss?__mk_ja_JP=%E3%82%AB%E3%82%BF%E3%82%AB%E3%83%8A&url=search-alias%3Daps&field-keywords="
                    + barcode;

            Commodity result = null;
            //! 商品URLを取得
            {
                byte[] buffer = GameUtil.toByteArray(WebInputStream.get(url, 1000 * 10));
                String html = new String(buffer, GameUtil.SHIT_JIS);
                //                LogUtil.log(html);
                url = get1stURL(html);
                LogUtil.log("Target URL : " + url);
                result = load(url, listener);

            }
            result.barcode = barcode;
            return result;

        } catch (Exception e) {
            LogUtil.log(e);
        }

        return null;
    }

    /**
     * 検索カテゴリなし（すべてのカテゴリ）
     */
    public static final String eSearchCategoryAll = null;

    /**
     * 検索カテゴリ（本）
     */
    public static final String eSearchCategoryBook = "BOOK";
    /**
     * 検索カテゴリ（ゲーム）
     */
    public static final String eSearchCategoryGame = "GAME";

    /**
     * 商品情報のローディングに関するメッセージを受信する。
     */
    public interface OnCommodityLoadListener {

        /**
         * 商品のロードを終了した場合に呼び出される。<BR>
         * falseを返した場合、resultの商品一覧に含まない。
         */
        boolean onLoadComplete(String html, Commodity item);
    }

    /**
     * Amazonの検索結果URLを指定し、URLを取得する。
     */
    public static List<Commodity> searchUrl(String url, int limit, OnCommodityLoadListener listener) {

        List<Commodity> result = new ArrayList<Commodity>();

        try {
            LogUtil.log(url);

            //! 商品URLを取得
            {
                byte[] buffer = GameUtil.toByteArray(WebInputStream.get(url, 1000 * 10));
                String html = new String(buffer, GameUtil.SHIT_JIS);
                //                LogUtil.log(html);
                List<String> urlList = enumrateURL(html);
                if (limit > 0) {
                    while (urlList.size() > limit) {
                        urlList.remove(urlList.size() - 1);
                    }
                }

                for (String _url : urlList) {
                    //                    LogUtil.log(_url);
                    Commodity data = load(_url, listener);
                    if (data != null) {
                        result.add(data);
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return result;
    }

    /**
     * Amazonからキーワード検索し、結果を返す。
     */
    public static List<Commodity> searchKeyword(String keyword, String category, int limit,
                                                OnCommodityLoadListener listener) {

        List<Commodity> result = new ArrayList<Commodity>();

        try {
            String url = "http://www.amazon.co.jp/s/ref=nb_sb_noss?__mk_ja_JP=%83J%83%5E%83J%83i&url=search-alias%3Daps&field-keywords=";

            if (eSearchCategoryBook.equals(category)) {
                url = "http://www.amazon.co.jp/s/ref=nb_sb_noss?__mk_ja_JP=%83J%83%5E%83J%83i&url=search-alias%3Dstripbooks&field-keywords=";
            } else if (eSearchCategoryGame.equals(category)) {
                url = "http://www.amazon.co.jp/s/ref=nb_sb_noss?__mk_ja_JP=%83J%83%5E%83J%83i&url=search-alias%3Dvideogames&field-keywords=";
            }

            url += URLEncoder.encode(keyword, GameUtil.SHIT_JIS);

            return searchUrl(url, limit, listener);
        } catch (Exception e) {

        }
        return result;
    }

    /**
     * 検索キーワードを検索用に最適化する。<BR>
     * ECB向け。適当実装。
     */
    public static String collectSearchKeyword(final String origin) {
        try {

            String result = origin.replace('　', ' ');
            result = result.replace('（', '(');
            result = result.replace('）', ')');

            {
                String[] words = result.split(" ");

                for (int i = 0; i < words.length; ++i) {
                    String word = words[i];
                    try {
                        if (i == (words.length - 1)) {
                            if (word.startsWith("第")) {
                                word = word.substring(1);
                            }
                            if (word.endsWith("巻")) {
                                word = word.substring(0, word.length() - 1);
                            }
                        }
                        Integer n = Integer.parseInt(word);
                        word = n.toString();
                    } catch (Exception e) {

                    }
                    words[i] = word;
                }

                //! 数字で終わっていたら数字を削除
                try {
                    Integer.parseInt(words[words.length - 1]);
                    words[words.length - 1] = "";
                } catch (Exception e) {

                }

                result = "";
                for (String word : words) {
                    if (result.length() > 0) {
                        result += " ";
                    }
                    result += word;
                }

                while (result.endsWith(" ")) {
                    result = result.substring(0, result.length() - 1);
                }
            }

            return result;

        } catch (Exception e) {
        }
        return origin;
    }

    /**
     * Amazonの検索結果HTMLから商品一覧を取得する。
     */
    static List<String> enumrateURL(final String _html) {
        List<String> result = new ArrayList<String>();

        try {
            int num = 0;
            while (true) {
                String keyword = "<div id=\"srNum_" + num + "\" class=\"number\">";
                int index = _html.indexOf(keyword);
                if (index < 0 && num != 0) {
                    return result;
                }

                String html = _html.substring(index);
                html = html.substring(html.indexOf("<div class=\"image\">"));
                html = html.substring(html.indexOf("http"));
                html = html.substring(0, html.indexOf("\""));
                result.add(html);
                ++num;
            }
        } catch (Exception e) {

        }

        if (result.size() == 0) {
            String url = get1stURL(_html);
            if (url != null) {
                result.add(url);
            }
        }

        return result;
    }

    /**
     * Amazonの検索結果HTMLから商品URLを取り出す。
     */
    static String get1stURL(final String _html) {
        try {
            String html = _html;
            html = html.substring(html.indexOf("class=\"dataColumn\""));
            html = html.substring(html.indexOf("http"));
            html = html.substring(0, html.indexOf("\""));

            return html;
        } catch (Exception e) {

            try {
                String html = _html;
                html = html.substring(html.indexOf("<div class=\"image\">"));
                html = html.substring(html.indexOf("http"));
                html = html.substring(0, html.indexOf("\""));
                return html;
            } catch (Exception _e) {
                String html = _html;
                html = html.substring(html.indexOf("class=\"productTitle\">"));
                html = html.substring(html.indexOf("http"));
                html = html.substring(0, html.indexOf("\""));
                return html;
            }
        }
    }

    static String getAsinURL(String asin) {
        return "http://www.amazon.co.jp/o/ASIN/" + asin + "/";
    }

    /**
     * Amazonの検索URLから商品URLを取り出す。
     */
    static String getASIN(final String htmlBase, String keyword) {
        try {
            String html = htmlBase.substring(htmlBase.indexOf("asin="));
            html = html.substring("asin=".length(), html.indexOf("&"));
            if (html != null && html.length() > 0) {
                return html;
            }
        } catch (Exception e) {
        }

        return keyword;
    }

    static int getDateIndex(String html) {
        int length = html.length();
        for (int i = 0; i < length; ++i) {
            char c = html.charAt(i);
            if (c >= '0' && c <= '9') {
                return i;
            }
        }
        return -1;
    }

    static int _getHeader(String html) {
        int length = html.length();
        for (int i = 0; i < length; ++i) {
            char c = html.charAt(i);
            if (c != ' ' && c != '　') {
                return i;
            }
        }
        return -1;
    }

    static String getPublisher(final String htmlBase) {

        try {
            String header = "出版社:&#160;";
            int index = htmlBase.indexOf(header);
            if (index < 0) {
                return null;
            }
            String html = htmlBase.substring(index + header.length());
            html = html.substring(_getHeader(html), html.indexOf("<br"));
            return html;
        } catch (Exception e) {

        }
        try {
            String header = "出版社";
            int index = htmlBase.indexOf(header);
            if (index < 0) {
                return null;
            }
            String html = htmlBase.substring(index + header.length() + 1);
            html = html.substring(_getHeader(html), html.indexOf("<br"));
            return html;
        } catch (Exception e) {

        }
        try {
            String header = "Publisher";
            int index = htmlBase.indexOf(header);
            if (index < 0) {
                return null;
            }
            String html = htmlBase.substring(index + header.length() + 1);
            html = html.substring(_getHeader(html), html.indexOf("<br"));
            return html;
        } catch (Exception e) {

        }
        return null;
    }

    static String getReleaseDate(final String htmlBase) {

        try {
            String html = htmlBase.substring(htmlBase.indexOf("発売日"));
            html = html.substring(getDateIndex(html), html.indexOf("<br"));
            return html;
        } catch (Exception e) {

        }
        try {
            String html = htmlBase.substring(htmlBase.indexOf("出版日"));
            html = html.substring(getDateIndex(html), html.indexOf("<br"));
            return html;
        } catch (Exception e) {

        }
        return null;
    }

    static List<String> getAuthors(String htmlBase) {
        List<String> result = new ArrayList<String>();

        try {
            while (true) {
                String header = "/gp/aw/s/ref=aw_";
                int index = htmlBase.indexOf(header);
                if (index < 0) {
                    return result;
                }
                htmlBase = htmlBase.substring(index);
                header = "\">";
                index = htmlBase.indexOf(header);
                String html = htmlBase.substring(index + header.length());
                int end = html.indexOf("</a");
                html = html.substring(0, end);
                if (html.length() > 0) {
                    result.remove(html); //!<    同じ名前が登録されないようにする
                    result.add(html);
                } else {
                    return result;
                }
                htmlBase = htmlBase.substring(end);
            }
        } catch (Exception e) {

        }

        return result;
    }

    static String getISBN10(final String htmlBase) {

        try {
            String header = "ISBN-10";
            int index = htmlBase.indexOf(header);
            if (index < 0) {
                return null;
            }
            String html = htmlBase.substring(index + header.length() + 1);
            html = html.substring(_getHeader(html), html.indexOf("<br"));
            return html;
        } catch (Exception e) {

        }
        return null;
    }

    static String getISBN13(final String htmlBase) {

        try {
            String header = "ISBN-13";
            int index = htmlBase.indexOf(header);
            if (index < 0) {
                return null;
            }
            String html = htmlBase.substring(index + header.length() + 1);
            html = html.substring(_getHeader(html), html.indexOf("<br"));
            return html;
        } catch (Exception e) {

        }
        return null;
    }

    static Integer getPrice(final String htmlBase) {
        try {
            String header = "価格:&nbsp;<font";
            int index = htmlBase.indexOf(header);
            if (index < 0) {
                return null;
            }
            String html = htmlBase.substring(index + header.length() + 1);
            html = html.substring(getDateIndex(html));
            html = html.substring(0, html.indexOf("</"));
            html = html.replaceAll(",", "");
            return Integer.parseInt(html);
        } catch (Exception e) {

        }
        return null;
    }

    static String getGamePlatform(final String htmlBase) {

        try {
            String header = "プラットフォーム:</b>";
            int index = htmlBase.indexOf(header);
            if (index < 0) {
                return null;
            }
            String html = htmlBase.substring(index + header.length() + 1);
            html = html.substring(_getHeader(html), html.indexOf("<br"));
            return html;
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * Amazonの検索URLから商品URLを取り出す。
     */
    static String getTitle(final String _html) {

        try {
            String html = _html;
            html = html.substring(html.indexOf("<title>") + "<title>".length());
            html = html.substring(0, html.indexOf("アマゾントップ") - 1);

            if (html.startsWith("Amazon.co.jp： ")) {
                html = html.substring(html.indexOf("：") + 2);
            }
            return html;
        } catch (Exception e) {
            String html = _html;
            html = html.substring(html.indexOf("<meta name=\"title\" content=\""));
            html = html.substring(html.indexOf("content=\""));
            html = html.substring(html.indexOf("\"") + 1);
            html = html.substring(0, html.indexOf("\""));

            if (html.startsWith("Amazon.co.jp： ")) {
                html = html.substring(html.indexOf("：") + 2);
            }
            return html;
        }

    }

    /**
     * Amazonの検索URLから商品URLを取り出す。
     */
    static String getImageURL(String html) {

        try {
            html = html.substring(html.indexOf("img src=\"http://ec"));
            html = html.substring(html.indexOf("http://"), html.indexOf(".jpg") + ".jpg".length());

            return html;
        } catch (Exception e) {

        }
        return null;
    }

}
