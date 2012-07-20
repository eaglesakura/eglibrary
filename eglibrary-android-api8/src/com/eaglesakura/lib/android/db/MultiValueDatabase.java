package com.eaglesakura.lib.android.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.LogUtil;

public class MultiValueDatabase extends DisposableResource {

    Context context;

    File dbFile;

    /**
     * DBを構築しているデータリスト
     */
    ValueList valueList;

    /**
     * DBを開くためのヘルパ
     */
    Helper helper;

    /**
     * 開いたDB本体
     */
    SQLiteDatabase db;

    public MultiValueDatabase(Context context, File dbFile, DBType type, ValueList valueList) {
        this.context = context;
        this.dbFile = dbFile;
        this.valueList = valueList;

        helper = new Helper();
        db = type.open(helper);
    }

    @Override
    public void dispose() {
        if (helper != null) {
            helper.close();
            helper = null;
        }
    }

    /**
     * プライマリキーがkey一致するオブジェクトを取得する
     * @param key
     * @return
     */
    public Data getOrNull(Object key) {
        Cursor cursor = null;
        try {
            final String selection = valueList.primary.createSelection(key);
            cursor = db.query(valueList.tableName, valueList.fullColmnList(), selection, null, null, null, null);
            if (cursor.moveToFirst()) {
                Data data = new Data(cursor, valueList);
                return data;
            }
        } catch (Exception e) {
            LogUtil.log(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * keyName = valueに一致する値を列挙する
     * @param keyName
     * @param value
     * @return
     */
    public List<Data> list(String keyName, Object value) {
        return list(keyName, value, valueList.fullColmnList());
    }

    /**
     * keyName = valueに一致する値を列挙する
     * colmnsに指定したカラムのみを取得する
     * @param keyName
     * @param value
     * @param colmns
     * @return
     */
    public List<Data> list(String keyName, Object value, String[] colmns) {
        List<Data> result = new LinkedList<MultiValueDatabase.Data>();
        Cursor cursor = null;
        try {
            final String selection = valueList.getColmun(keyName).createSelection(value);
            cursor = db.query(valueList.tableName, colmns, selection, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    MultiValueDatabase.Data data = new Data(cursor, valueList);
                    result.add(data);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            LogUtil.log(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * データの挿入を行う。
     * @param key
     * @param values
     */
    public boolean insertOrUpdate(Map<String, Object> values) {
        try {
            final ContentValues contentValues = valueList.toContentValues(values);
            try {
                db.insertOrThrow(valueList.tableName, null, contentValues);
            } catch (Exception e) {
                try {
                    db.update(valueList.tableName, contentValues, null, null);
                } catch (Exception ee) {
                    LogUtil.log(ee);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return false;
    }

    /**
     * ユーザーに返すデータ。
     * @author TAKESHI YAMASHITA
     *
     */
    public static class Data {
        Bundle bundle;

        /**
         * 
         * @param cursor
         * @param valueList
         */
        public Data(Cursor cursor, ValueList valueList) {
            bundle = valueList.toBundle(cursor);
        }

        /**
         * 
         * @param key
         * @param def
         * @return
         */
        public int getInteger(String key, int def) {
            return bundle.getInt(key, def);
        }

        /**
         * テキストを取得する
         * @param key
         * @param def
         * @return
         */
        public String getText(String key, String def) {
            String result = bundle.getString(key);
            if (result == null) {
                return def;
            } else {
                return result;
            }
        }

        /**
         * 実数値を取得する
         * @param key
         * @param def
         * @return
         */
        public double getReal(String key, double def) {
            return bundle.getDouble(key, def);
        }

        /**
         * blob値を取得する。
         * @param key
         * @return
         */
        public byte[] getBlob(String key, byte[] def) {
            byte[] result = bundle.getByteArray(key);
            if (result == null) {
                return def;
            } else {
                return result;
            }
        }
    }

    /**
     * データ情報
     * @author TAKESHI YAMASHITA
     *
     */
    public static class Column {
        /**
         * 型の種類
         */
        DBValueType type;

        /**
         * 型名
         */
        String name;

        public Column(DBValueType type, String name) {
            this.type = type;
            this.name = name;
        }

        /**
         * SQL命令を作成する
         * @return
         */
        public String sql() {
            return name + " " + type.getSqlValueType();
        }

        /**
         * Select文用のキーワードを作成する
         * @param value
         * @return
         */
        public String createSelection(Object value) {
            if (type == DBValueType.Text) {
                return name + "='" + value.toString() + "'";
            } else {
                return name + "=" + value.toString();
            }
        }
    }

    /**
     * 値のリストを作成する
     * @author TAKESHI YAMASHITA
     *
     */
    public static class ValueList {
        /**
         * DBのバージョンを示す
         */
        int dbVersion = 1;

        Column primary = null;

        String tableName = null;

        List<Column> columns = new ArrayList<MultiValueDatabase.Column>();

        /**
         * プライマリを指定して生成する。
         * @param primary
         */
        public ValueList(String tableName) {
            this.tableName = tableName;
        }

        /**
         * プライマリキーを指定する
         * @param primary
         */
        public void setPrimary(Column primary) {
            this.primary = primary;
        }

        /**
         * カラムを追加する。
         * @param c
         */
        public void addColmn(Column c) {
            columns.add(c);
        }

        /*
         * データベースバージョンを指定する。
         */
        public void setDbVersion(int dbVersion) {
            this.dbVersion = dbVersion;
        }

        /**
         * create命令を作成する。
         * @return
         */
        public String sqlCreateCommand() {
            StringBuffer buffer = new StringBuffer();

            buffer.append("create table if not exists ").append(tableName).append(" ( ");

            if (primary != null) {
                buffer.append(primary.sql()).append(" primary key");
                if (!columns.isEmpty()) {
                    buffer.append(", ");
                }

                int index = 0;
                for (Column c : columns) {
                    buffer.append(c.sql());
                    // ラストじゃなければケツにカンマをつける
                    if (index < (columns.size() - 1)) {
                        buffer.append(", ");
                    }
                    ++index;
                }
            }

            buffer.append(" )");

            return buffer.toString();
        }

        /**
         * Drop命令を返す。
         * @param tableName
         * @return
         */
        public String sqlDropCommand() {
            return "drop table if exists " + tableName;
        }

        private void add(Bundle result, Cursor cursor, Column c) {
            if (c == null) {
                return;
            }

            int index = cursor.getColumnIndex(c.name);
            if (index >= 0) {
                c.type.toBundle(result, c.name, cursor, index);
            }
        }

        /**
         * Bundleに変換する
         * @param cursor
         * @return
         */
        public Bundle toBundle(Cursor cursor) {
            Bundle result = new Bundle();
            add(result, cursor, primary);

            for (Column c : columns) {
                add(result, cursor, c);
            }

            return result;
        }

        /**
         * 全カラムを返す。
         * @return
         */
        public String[] fullColmnList() {
            int num = (primary != null ? 1 : 0) + columns.size();
            String[] result = new String[num];
            int index = 0;
            if (primary != null) {
                result[index++] = primary.name;
            }
            for (Column c : columns) {
                result[index++] = c.name;
            }
            return result;
        }

        Column getColmun(String name) {
            if (primary != null) {
                if (primary.name.equals(name)) {
                    return primary;
                }
            }
            for (Column c : columns) {
                if (c.name.equals(name)) {
                    return c;
                }
            }
            return null;
        }

        /**
         * コンテンツ格納用のValuesを取得する。
         * @param primary
         * @param datas
         * @return
         */
        public ContentValues toContentValues(Map<String, Object> datas) {
            ContentValues result = new ContentValues();
            Iterator<Entry<String, Object>> iterator = datas.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Object> next = iterator.next();
                Column c = getColmun(next.getKey());
                if (c != null) {
                    c.type.toValue(result, c.name, next.getValue());
                }
            }
            return result;
        }

    }

    class Helper extends SQLiteOpenHelper {
        public Helper() {
            super(context, dbFile.getAbsolutePath(), null, valueList.dbVersion);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(valueList.sqlCreateCommand());
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
