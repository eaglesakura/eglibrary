package com.eaglesakura.google.spreadsheet;

import com.eaglesakura.google.spreadsheet.generic.Author;
import com.eaglesakura.google.spreadsheet.generic.Content;
import com.eaglesakura.google.spreadsheet.generic.IntegerField;
import com.eaglesakura.google.spreadsheet.generic.Link;
import com.eaglesakura.google.spreadsheet.generic.StringField;
import com.eaglesakura.google.spreadsheet.generic.Title;
import com.eaglesakura.proguard.NonProguardModel;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 1つのSpreadsheetファイルを示す
 */
public class Spreadsheet extends NonProguardModel {

    public String encoding;

    public Feed feed;

    public String version;

    public static class Feed extends NonProguardModel {
        public List<Author> author = new ArrayList<Author>();

        public List<WorksheetEntry> entry = new ArrayList<WorksheetEntry>();

        public Title title;

        public StringField updated;

        public String xmlns;

        @JsonProperty("xmlns$gs")
        public String xmlnsGs;

        @JsonProperty("xmlns$openSearch")
        public String xmlnsOpenSearch;
    }


    public static class WorksheetEntry extends NonProguardModel {
        public Content content;

        @JsonProperty("gs$colCount")
        public IntegerField colCount;

        @JsonProperty("gs$rowCount")
        public IntegerField rowCount;

        public StringField id;

        public List<Link> link = new ArrayList<Link>();

        public Title title;

        public StringField updated;
    }


    /**
     * タイトルを元にワークシートを検索する
     *
     * @param sheet 検索対象ファイル
     * @param title タイトル
     * @return 見つかった場合はワークシート情報、それ以外はnull
     */
    public static WorksheetEntry findWorksheet(Spreadsheet sheet, String title) {
        Iterator<WorksheetEntry> iterator = sheet.feed.entry.iterator();
        while (iterator.hasNext()) {
            WorksheetEntry work = iterator.next();
            if (work.title.name.equals(title)) {
                return work;
            }
        }

        return null;
    }

    /**
     * Spreadsheet APIアクセス用URLを生成する
     * <p/>
     * JSONが基本となる
     *
     * @param key Spreadsheetキー
     * @return APIアクセス用URL
     */
    public static String createSpreadsheetUrl(String key) {
        return String.format("https://spreadsheets.google.com/feeds/worksheets/%s/private/full?alt=json", key);
    }

    /**
     * Worksheet API用のURLを生成する
     *
     * @param spreadsheetKey Spreadsheetキー
     * @param worksheetKey   Worksheetキー
     * @return APIアクセス用URL
     */
    public static String createWorksheetUrl(String spreadsheetKey, String worksheetKey) {
        return String.format("https://spreadsheets.google.com/feeds/cells/%s/%s/private/full?alt=json", spreadsheetKey, worksheetKey);
    }

    /**
     * ワークシートキーを取得する
     *
     * @param entry ワークシートデータ
     * @return ワークシートキー
     */
    public static String parseWorksheetKey(WorksheetEntry entry) {
        final String id = entry.id.value;
        return id.substring(id.lastIndexOf('/') + 1);
    }
}
