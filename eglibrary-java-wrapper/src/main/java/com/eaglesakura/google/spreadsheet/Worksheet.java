package com.eaglesakura.google.spreadsheet;

import com.eaglesakura.google.spreadsheet.generic.Author;
import com.eaglesakura.google.spreadsheet.generic.Category;
import com.eaglesakura.google.spreadsheet.generic.Content;
import com.eaglesakura.google.spreadsheet.generic.Link;
import com.eaglesakura.google.spreadsheet.generic.StringField;
import com.eaglesakura.google.spreadsheet.generic.Title;
import com.eaglesakura.proguard.NonProguardModel;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Spreadsheet内の１シートを表す
 */
public class Worksheet extends NonProguardModel {

    public String encoding;

    public Feed feed;

    public String version;

    public static class Feed extends NonProguardModel {
        public List<Author> author = new ArrayList<Author>();

        public List<Category> category = new ArrayList<Category>();

        public List<Cell> entry = new ArrayList<Cell>();

        public Title title;

        public StringField updated;

        public String xmlns;

        @JsonProperty("xmlns$gs")
        public String xmlnsGs;

        @JsonProperty("xmlns$batch")
        public String xmlnsBatch;

        @JsonProperty("xmlns$openSearch")
        public String xmlnsOpenSearch;
    }


    public static class Cell extends NonProguardModel {

        public List<Category> category = new ArrayList<Category>();

        @JsonProperty("gs$cell")
        public CellValue value;

        public Content content;

        public StringField id;

        public List<Link> link = new ArrayList<Link>();

        public StringField updated;

        public Title title;
    }

    public static class CellValue extends NonProguardModel {
        @JsonProperty("$t")
        public String screen;

        public int col;

        public int row;

        public String inputValue;

        public int asInteger() {
            return Integer.parseInt(screen);
        }

        public float asFloat() {
            return Float.parseFloat(screen);
        }

        public double asDouble() {
            return Double.parseDouble(screen);
        }
    }

    /**
     * 指定セルから特定範囲のみを抽出する
     *
     * @param cells
     * @param minXIndex 最小のX方向のインデックス。(col >= (minXIndex+1)) となったセルのみを返却する
     * @param maxXIndex 最大のX方向のインデックス。(col <= (maxXIndex+1)) となったセルのみを返却する
     * @return
     */
    public static List<Cell> filterColRange(List<Cell> cells, int minXIndex, int maxXIndex) {
        List<Cell> result = new ArrayList<Cell>();

        ++minXIndex;
        ++maxXIndex;

        Iterator<Cell> iterator = cells.iterator();
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            // 指定範囲に収まらないセルは排除する
            if (cell.value.col < minXIndex
                    || cell.value.col > maxXIndex) {
                iterator.remove();
            }
        }
        return result;
    }

    /**
     * 指定セルから特定範囲のみを抽出する
     *
     * @param cells
     * @param minYIndex 最小のY方向のインデックス。(row >= (minXIndex+1)) となったセルのみを返却する
     * @param maxYIndex 最大のY方向のインデックス。(row <= (maxXIndex+1)) となったセルのみを返却する
     * @return
     */
    public static List<Cell> filterRowRange(List<Cell> cells, int minYIndex, int maxYIndex) {
        List<Cell> result = new ArrayList<Cell>();

        ++minYIndex;
        ++maxYIndex;

        Iterator<Cell> iterator = cells.iterator();
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            // 指定範囲に収まらないセルは排除する
            if (cell.value.row < minYIndex
                    || cell.value.row > maxYIndex) {
                iterator.remove();
            }
        }
        return result;
    }

    /**
     * 指定した行を一括で取得する
     *
     * @param worksheet 読み込み対象のワークシート
     * @param xIndex    X方向のインデックス。0以上
     * @return 読み込んだセルリスト。何も読み込めなかったら要素が0となり、nullにはならない。
     */
    public static List<Cell> listCol(Worksheet worksheet, int xIndex) {
        ++xIndex;

        List<Cell> result = new ArrayList<Cell>();

        Iterator<Cell> iterator = worksheet.feed.entry.iterator();
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (cell.value.col == xIndex) {
                result.add(cell);
            }
        }

        return result;
    }

    /**
     * 指定した列を一括で取得する
     *
     * @param worksheet 読み込み対象のワークシート
     * @param yIndex    Y方向のインデックス。0以上
     * @return 読み込んだセルリスト。何も読み込めなかったら要素が0となり、nullにはならない。
     */
    public static List<Cell> listRow(Worksheet worksheet, int yIndex) {
        ++yIndex;

        List<Cell> result = new ArrayList<Cell>();

        Iterator<Cell> iterator = worksheet.feed.entry.iterator();
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (cell.value.row == yIndex) {
                result.add(cell);
            }
        }

        return result;
    }

    /**
     * 指定したX/Yインデックスの要素を探す
     *
     * @param worksheet 読み込み対象のワークシート
     * @param xIndex    X方向のインデックス。0以上
     * @param yIndex    Y方向のインデックス。0以上
     * @return 成功した場合セル、それ以外はnull
     */
    public static Cell find(Worksheet worksheet, int xIndex, int yIndex) {
        // セル番号は1から始まるため+1する
        ++xIndex;
        ++yIndex;

        Iterator<Cell> iterator = worksheet.feed.entry.iterator();
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (cell.value.col == xIndex && cell.value.row == yIndex) {
                return cell;
            }
        }

        // 発見できなかった
        return null;
    }

    /**
     * カラムを検索する
     *
     * @param cells  検索対象のセル
     * @param xIndex X方向のインデックス。0以上
     * @return 成功した場合セル、それ以外はnull
     */
    public static Cell findCol(List<Cell> cells, int xIndex) {
        ++xIndex;

        for (Cell cell : cells) {
            if (cell.value.col == xIndex) {
                return cell;
            }
        }

        return null;
    }

    /**
     * A1等のセル名称で検索する
     *
     * @param worksheet 検索対象のワークシート
     * @param cellName  検索対象のセル名称
     * @return 成功した場合セル、それ以外はnull
     */
    public static Cell find(Worksheet worksheet, String cellName) {

        Iterator<Cell> iterator = worksheet.feed.entry.iterator();
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            if (cell.title.name.equals(cellName)) {
                return cell;
            }
        }

        // 発見できなかった
        return null;
    }

    /**
     * 行優先でソートする
     *
     * @param cells セル一覧
     * @param up    昇順である場合true
     * @return
     */
    public static List<Cell> sortRow(List<Cell> cells, final boolean up) {
        Collections.sort(cells, new Comparator<Cell>() {
            @Override
            public int compare(Cell o1, Cell o2) {
                if (o1.value.row != o2.value.row) {
                    if (up) {
                        return o1.value.row - o2.value.row;
                    } else {
                        return o2.value.row - o1.value.row;
                    }
                } else {
                    if (up) {
                        return o1.value.col - o2.value.col;
                    } else {
                        return o2.value.col - o1.value.col;
                    }
                }
            }
        });
        return cells;
    }

    /**
     * 列優先でソートする
     *
     * @param cells セル一覧
     * @param up    昇順である場合true
     * @return
     */
    public static List<Cell> sortCol(List<Cell> cells, final boolean up) {
        Collections.sort(cells, new Comparator<Cell>() {
            @Override
            public int compare(Cell o1, Cell o2) {
                if (o1.value.col != o2.value.col) {
                    // 列ソート
                    if (up) {
                        return o1.value.col - o2.value.col;
                    } else {
                        return o2.value.col - o1.value.col;
                    }
                } else {
                    // 行ソート
                    if (up) {
                        return o1.value.row - o2.value.row;
                    } else {
                        return o2.value.row - o1.value.row;
                    }
                }
            }
        });
        return cells;
    }
}
