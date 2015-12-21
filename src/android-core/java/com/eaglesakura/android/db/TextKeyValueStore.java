package com.eaglesakura.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import com.eaglesakura.util.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SQLを利用した簡単なKVSを提供する。
 * insert時にはkey-valueと更新時刻が保存される。
 */
public class TextKeyValueStore {

    /**
     * デフォルトのテーブル名
     */
    public static final String TABLE_NAME_DEFAULT = "DB_KEY_VALUE_DATA";

    /**
     * ファイル名
     */
    final File dbFile;

    /**
     * テーブル名
     */
    final String tableName;

    /**
     * コンテキスト
     */
    final Context context;

    /**
     *
     */
    Helper helper;

    /**
     * IO用のデータベース
     */
    SQLiteDatabase db = null;

    /**
     * キーとして入力する値
     */
    static final String DB_KEY = "KEY";

    /**
     * valueとして入力する値
     */
    static final String DB_VALUE = "VALUE";

    /**
     * 保存した日付として入力する値
     */
    static final String DB_DATE = "DATE";

    /**
     * テーブル削除用のSQL
     */
    final String DELETE_TBL_SQL;

    /**
     * テーブル作成用のSQL
     */
    final String CREATE_TBL_SQL;

    final int SUPPORT_DB_VERSION = 1;

    /**
     * データベースの参照数
     */
    AtomicInteger refs = new AtomicInteger(0);

    public TextKeyValueStore(Context context, File dbFile, String tableName) {
        this.context = context;
        this.dbFile = dbFile;
        this.tableName = tableName;

        DELETE_TBL_SQL = "DROP TABLE IF EXISTS " + tableName;
        CREATE_TBL_SQL = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + DB_KEY + " TEXT PRIMARY KEY NOT NULL UNIQUE , " + DB_VALUE
                + " TEXT, " + DB_DATE + " INTEGER )";
    }

    /**
     * DBを開く
     */
    public void open(DBType type) {
        if (refs.incrementAndGet() == 1) {
            helper = new Helper();
            db = type.open(helper);
            createTable();
        }
    }

    /**
     * DBを閉じる
     */
    public void close() {
        if (refs.decrementAndGet() == 0) {
            if (helper != null) {
                helper.close();
                helper = null;
            }
        }
    }

    /**
     * トランザクション内で処理を行う
     *
     * @param runnable
     */
    public synchronized void runInTx(Runnable runnable) {
        try {
            db.beginTransaction();
            runnable.run();
        } finally {
            try {
                db.setTransactionSuccessful();
            } catch (Exception e) {
            } finally {
                db.endTransaction();
            }
        }
    }


    /**
     * 値の挿入/更新を行う
     *
     * @param values
     */
    public void putInTx(final Map<String, String> values) {
        runInTx(new Runnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<String, String>> iterator = values.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    insertOrUpdate(entry.getKey(), entry.getValue());
                }
            }
        });
    }

    private ContentValues createValues(String key, String value) {
        if (value == null) {
            value = "";
        }
        ContentValues result = new ContentValues(3);
        result.put(DB_KEY, key);
        result.put(DB_VALUE, value);
        result.put(DB_DATE, System.currentTimeMillis());
        return result;
    }

    /**
     * 簡易的に値を挿入する
     *
     * @param key
     * @param value
     */
    public void putDirect(final String key, final String value) {
        runInTx(new Runnable() {
            @Override
            public void run() {
                insertOrUpdate(key, value);
            }
        });
    }

    /**
     * DBに値を新規登録する。
     * 登録済みの場合、上書きを行う。
     *
     * @param key
     * @param value
     */
    public void insertOrUpdate(String key, String value) {
        final ContentValues values = createValues(key, value);
        try {
            db.insertOrThrow(tableName, null, values);
        } catch (Exception e) {
            //            remove(key);
            //            db.insert(tableName, null, values);
            db.update(tableName, values, DB_KEY + "='" + key + "'", null);
        }
    }

    /**
     * 値の更新を行う
     *
     * @param key
     * @param value
     */
    public void update(String key, String value) {
        final ContentValues values = createValues(key, value);
        db.update(tableName, values, DB_KEY + "='" + key + "'", null);
    }

    private String toString(byte[] buffer) {
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }

    /**
     * byte配列を書き込む。
     * ただし、Base64エンコードされるため見た目上のデータは大きくなる。
     *
     * @param buffer
     */
    public void insert(String key, byte[] buffer) {
        insert(key, toString(buffer));
    }

    /**
     * byte配列を挿入もしくは更新する。
     *
     * @param key
     * @param buffer
     */
    public void insertOrUpdate(String key, byte[] buffer) {
        insertOrUpdate(key, toString(buffer));
    }

    /**
     * DBに値を新規登録する。
     * 失敗した場合は何も行わない。
     *
     * @param key
     */
    public void insert(String key, String value) {
        final ContentValues values = createValues(key, value);
        try {
            db.insert(tableName, null, values);
        } catch (Exception e) {

        }
    }

    /**
     * DBに書き込み済みの値を削除する
     *
     * @param key
     */
    public void remove(String key) {
        try {
            db.delete(tableName, DB_KEY + "='" + key + "'", null);
        } catch (Exception e) {

        }
    }

    /**
     * 詳細なデータを取得する。
     *
     * @param key
     * @return
     */
    public Data get(String key) {
        Cursor cursor = null;
        try {
            String selection = DB_KEY + "='" + key + "'";
            cursor = db.query(tableName, cursorDatas, selection, null, null, null, null);

            if (cursor.moveToFirst()) {
                return new Data(cursor);
            }

            cursor.close();
        } catch (Exception e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * 管理しているデータ一覧を返す。
     *
     * @return
     */
    public List<Data> list() {
        Cursor cursor = null;
        List<Data> result = new ArrayList<Data>();
        try {
            cursor = db.query(tableName, cursorDatas, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    result.add(new Data(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 同じvalueを持つデータ一覧を返す。
     *
     * @param value
     * @return
     */
    public List<Data> listValues(String value) {
        Cursor cursor = null;
        List<Data> result = new ArrayList<Data>();
        try {
            String selection = DB_VALUE + "='" + value + "'";
            cursor = db.query(tableName, cursorDatas, selection, null, null, null, null);

            if (cursor.moveToFirst()) {
                do {
                    result.add(new Data(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * 日付がdate以降で、上位max件を取得する。
     *
     * @param date
     * @param max
     * @return
     */
    public List<Data> listTimesUpToDate(long date, int max) {
        List<Data> result = new LinkedList<Data>();
        Cursor cursor = null;
        try {
            String selection = DB_DATE + ">=" + date;
            String order = DB_DATE + " desc";
            cursor = db.query(tableName, cursorDatas, selection, null, null, null, order, "" + max);

            if (cursor.moveToFirst()) {
                do {
                    result.add(new Data(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /**
     * @param key
     * @param _def
     * @return
     */
    public String get(String key, String _def) {
        String result = getOrNull(key);
        if (result == null) {
            return _def;
        } else {
            return result;
        }
    }

    /**
     * 文字列をint変換して取得する
     *
     * @param key
     * @param def
     * @return
     */
    public int getInteger(String key, int def) {
        return (int) getLong(key, def);
    }

    /**
     * 文字列をlong変換して取得する
     *
     * @param key
     * @param def
     * @return
     */
    public long getLong(String key, long def) {
        String value = get(key, null);
        if (value != null) {
            return Long.parseLong(value);
        } else {
            return def;
        }
    }

    /**
     * 文字列をlong変換して取得する
     *
     * @param key
     * @param def
     * @return
     */
    public float getFloat(String key, float def) {
        String value = get(key, null);
        if (value != null) {
            return Float.parseFloat(value);
        } else {
            return def;
        }
    }

    /**
     * 値を取得する
     *
     * @param key
     * @return
     */
    public String getOrNull(String key) {
        Cursor cursor = null;
        try {
            String selection = DB_KEY + "='" + key + "'";
            cursor = db.query(tableName, new String[]{
                    DB_VALUE
            }, selection, null, null, null, null);

            cursor.moveToFirst();
            return cursor.getString(0);
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * キーが存在したらtrue
     *
     * @param key
     * @return
     */
    public boolean exists(String key) {
        return get(key, null) != null;
    }

    /**
     * テーブルの内容を破棄する
     */
    private void dropTable() {
        db.execSQL(DELETE_TBL_SQL);
        createTable();
    }

    /**
     * テーブルを作成する
     */
    private void createTable() {
        db.execSQL(CREATE_TBL_SQL);
    }

    /**
     * 値を挿入する。
     */
    private void _insert(String key, String insertValue, long insertDate, InsertFilter filter) throws Exception {
        // 存在しないから、挿入して終了
        final ContentValues values = createValues(key, insertValue);
        values.put(DB_DATE, insertDate);

        // 古い値が存在する？
        if (!this.exists(key)) {
            db.insert(tableName, null, values);
            return;
        }

        // 存在するなら、値を取得する
        Cursor cursor = db.query(tableName, new String[]{
                DB_VALUE, DB_DATE,
        }, DB_KEY + "='" + key + "'", null, null, null, null);

        try {
            cursor.moveToFirst();

            final String currentValue = cursor.getString(0);
            final long currentDate = cursor.getLong(1);

            // どちらを優先するかはフィルタに任せる
            if (filter.isOverwrite(key, currentValue, currentDate, insertValue, insertDate)) {
                // 上書きを行う
                remove(key);
                db.insert(tableName, null, values);
            } else {
                // 上書きを行わない。
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * insertDBをこのオブジェクトが管理するDBに結合する。
     * データが競合した場合、どちらを優先するかはfilterによって確定される。
     *
     * @param insertDB
     */
    public void insertTo(final TextKeyValueStore insertDB, final InsertFilter filter) {
        runInTx(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = insertDB.db.query(tableName, new String[]{
                        DB_KEY, DB_VALUE, DB_DATE,
                }, null, null, null, null, null);

                try {
                    if (cursor.moveToFirst()) {
                        do {
                            String key = cursor.getString(0);
                            String value = cursor.getString(1);
                            long date = cursor.getLong(2);
                            try {
                                _insert(key, value, date, filter);
                            } catch (Exception e) {
                                LogUtil.log(e);
                            }
                        } while (cursor.moveToNext());
                    }

                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }

    @Deprecated
    public void print() {
        String selection = null;
        Cursor cursor = db.query(tableName, new String[]{
                DB_KEY, DB_VALUE
        }, selection, null, null, null, null);

        cursor.moveToFirst();
        do {
            String key = cursor.getString(0);
            String value = cursor.getString(1);
            LogUtil.log(key + " :: " + value);
        } while (cursor.moveToNext());
    }

    class Helper extends SQLiteOpenHelper {
        public Helper() {
            super(context, dbFile != null ? dbFile.getAbsolutePath() : null, null, SUPPORT_DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TBL_SQL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DELETE_TBL_SQL);
            db.execSQL(CREATE_TBL_SQL);
        }
    }

    /**
     * ２つのテーブルを結合する際に呼び出される。
     */
    public interface InsertFilter {
        /**
         * insertValueで古い値を上書きするかどうかを確定する。
         * trueを返した場合、insertValueで値を上書きする。
         *
         * @param key
         * @param currentValue
         * @param currentDate
         * @param insertValue
         * @param insertDate
         * @return
         */
        boolean isOverwrite(String key, String currentValue, long currentDate, String insertValue,
                            long insertDate);
    }

    /**
     * 新しいデータを優先するフィルタ
     */
    public static final InsertFilter FILTER_NEWDATA = new InsertFilter() {
        @Override
        public boolean isOverwrite(String key, String currentValue, long currentDate, String insertValue,
                                   long insertDate) {
            return insertDate > currentDate;
        }
    };

    /**
     * 古いデータを優先するフィルタ
     */
    public static final InsertFilter FILTER_OLDDATA = new InsertFilter() {
        @Override
        public boolean isOverwrite(String key, String currentValue, long currentDate, String insertValue,
                                   long insertDate) {
            return insertDate < currentDate;
        }
    };

    /**
     * 常に上書きを行うフィルタ
     */
    public static final InsertFilter FILTER_ALWAYS_OVERWRITE = new InsertFilter() {
        @Override
        public boolean isOverwrite(String key, String currentValue, long currentDate, String insertValue,
                                   long insertDate) {
            return true;
        }
    };

    /**
     * 常に上書きを行わないフィルタ
     */
    public static final InsertFilter FILTER_AYWAYS_NOT_OVERWRITE = new InsertFilter() {

        @Override
        public boolean isOverwrite(String key, String currentValue, long currentDate, String insertValue,
                                   long insertDate) {
            return false;
        }
    };

    static final String[] cursorDatas = {
            DB_KEY, DB_VALUE, DB_DATE
    };

    public static class Data {
        public final String value;
        public final long date;
        public final String key;

        private Data(Cursor cursor) {
            this.key = cursor.getString(0);
            this.value = cursor.getString(1);
            this.date = cursor.getLong(2);
        }

        /**
         * データをbyte配列として取得する
         * ただし、insert()時にbyte[]で挿入したデータだけが対象。
         *
         * @return
         */
        public byte[] valueToByteArray() {
            return Base64.decode(value, Base64.DEFAULT);
        }
    }
}
