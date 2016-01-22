package com.eaglesakura.lib.android.db;

import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.LogUtil;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLを利用した簡単なKVSを提供する。
 * insert時にはkey-valueと更新時刻が保存される。
 *
 * @author TAKESHI YAMASHITA
 */
public class BlobKeyValueStore extends DisposableResource {

    static final String CHARSET = "UTF-8";

    /**
     * ファイル名
     */
    File dbFile = null;

    /**
     * テーブル名
     */
    String tableName = null;

    /**
     * コンテキスト
     */
    Context context;

    /**
     *
     */
    Helper helper = null;

    /**
     * IO用のデータベース
     */
    SQLiteDatabase db = null;

    /**
     * キーとして入力する値
     */
    static final String DB_KEY = "_key";

    /**
     * valueとして入力する値
     */
    static final String DB_VALUE = "_value";

    /**
     * 保存した日付として入力する値
     */
    static final String DB_DATE = "_date";

    /**
     * テーブル削除用のSQL
     */
    String DELETE_TBL_SQL;

    /**
     * テーブル作成用のSQL
     */
    String CREATE_TBL_SQL;

    /**
     * DBのバージョン
     */
    int dbVersion = 0;

    /**
     * データベースの開き方を指定
     */
    DBType type;

    public BlobKeyValueStore(File dbFile, Context context, String tableName, DBType type, int dbVersion) {
        this.context = context;
        this.dbFile = dbFile;
        this.tableName = tableName;
        this.dbVersion = dbVersion;
        this.helper = new Helper();

        this.type = type;

        DELETE_TBL_SQL = "drop table if exists " + tableName;
        CREATE_TBL_SQL = "create table if not exists " + tableName + " (" + DB_KEY + " text primary key, " + DB_VALUE
                + " blob, " + DB_DATE + " integer )";
        db = type.open(helper);
        createTable();
    }

    /**
     * 書き込みの準備を行う
     */
    public void beginTransaction() {
        db.beginTransaction();
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    protected ContentValues createValues(String key, byte[] value) {
        ContentValues result = new ContentValues(3);
        result.put(DB_KEY, key);
        result.put(DB_VALUE, value);
        result.put(DB_DATE, System.currentTimeMillis());
        return result;
    }

    /**
     * DBに値を新規登録する。
     * 登録済みの場合、上書きを行う。
     */
    public void insertOrUpdate(String key, byte[] value) {
        final ContentValues values = createValues(key, value);
        try {
            db.insertOrThrow(tableName, null, values);
        } catch (Exception e) {
            remove(key);
            db.insert(tableName, null, values);
        }
    }

    /**
     * DBに値を新規登録する。
     * 失敗した場合は何も行わない。
     */
    public void insert(String key, byte[] value) {
        final ContentValues values = createValues(key, value);
        try {
            db.insert(tableName, null, values);
        } catch (Exception e) {

        }
    }

    /**
     * DBに書き込み済みの値を削除する
     */
    public void remove(String key) {
        try {
            db.delete(tableName, DB_KEY + "='" + key + "'", null);
        } catch (Exception e) {

        }
    }

    /**
     * 書き込みの完了を行う
     */
    public void endTransaction() {
        try {
            db.setTransactionSuccessful();
        } catch (Exception e) {

        } finally {
            db.endTransaction();
        }
    }

    /**
     *
     * @param key
     * @param _def
     * @return
     */
    public byte[] get(String key, byte[] _def) {
        byte[] result = getOrNull(key);
        if (result == null) {
            return _def;
        } else {
            return result;
        }
    }

    /**
     * キーに関連するデータを取得する。
     * 取得に失敗した場合、nullを返す。
     */
    public Data get(String key) {
        Cursor cursor = null;
        try {
            String selection = DB_KEY + "='" + key + "'";
            cursor = db.query(tableName, cursorDatas, selection, null, null, null, null);
            cursor.moveToFirst();
            return new Data(cursor);
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 内部の管理データを全て返す。
     */
    public List<Data> list() {
        Cursor cursor = null;
        List<Data> result = new ArrayList<BlobKeyValueStore.Data>();
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
     * 値を取得する
     */
    public byte[] getOrNull(String key) {
        Cursor cursor = null;
        try {
            String selection = DB_KEY + "='" + key + "'";
            cursor = db.query(tableName, new String[]{
                    DB_VALUE
            }, selection, null, null, null, null);

            cursor.moveToFirst();
            return cursor.getBlob(0);
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
     */
    public boolean exists(String key) {
        Cursor cursor = null;
        try {
            String selection = DB_KEY + "='" + key + "'";
            cursor = db.query(tableName, new String[]{
                    DB_KEY
            }, selection, null, null, null, null);

            if (cursor.moveToFirst()) {
                return false;
            }
            return cursor.getString(0) != null;
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * テーブルの内容を破棄する
     */
    public void dropTable() {
        db.execSQL(DELETE_TBL_SQL);
        createTable();
    }

    /**
     * テーブルを作成する
     */
    public void createTable() {
        db.execSQL(CREATE_TBL_SQL);
    }

    /**
     * 値を挿入する。
     */
    private void _insert(String key, byte[] insertValue, long insertDate, InsertFilter filter) throws Exception {
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

            final byte[] currentValue = cursor.getBlob(0);
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
            cursor.close();
        }

    }

    /**
     * insertDBをこのオブジェクトが管理するDBに結合する。
     * データが競合した場合、どちらを優先するかはfilterによって確定される。
     */
    public void insertTo(final BlobKeyValueStore insertDB, InsertFilter filter) {
        Cursor cursor = insertDB.db.query(tableName, new String[]{
                DB_KEY, DB_VALUE, DB_DATE,
        }, null, null, null, null, null);
        try {
            cursor.moveToFirst();
            do {
                String key = cursor.getString(0);
                byte[] value = cursor.getBlob(1);
                long date = cursor.getLong(2);
                try {
                    _insert(key, value, date, filter);
                } catch (Exception e) {
                    LogUtil.log(e);
                }
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
        }
    }

    public void print() {
        String selection = null;
        Cursor cursor = db.query(tableName, new String[]{
                DB_KEY, DB_VALUE
        }, selection, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String key = cursor.getString(0);
                byte[] value = cursor.getBlob(1);
                LogUtil.log(key + " :: " + value.length + "bytes");
            } while (cursor.moveToNext());
        }
    }

    /**
     * 管理しているDBを解放する。
     */
    @Override
    public void dispose() {
        if (db != null) {
            db.close();
            db = null;
        }
        if (helper != null) {
            helper.close();
        }
    }

    class Helper extends SQLiteOpenHelper {
        public Helper() {
            super(context, dbFile.getAbsolutePath(), null, dbVersion);
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
     *
     * @author TAKESHI YAMASHITA
     */
    public interface InsertFilter {
        /**
         * insertValueで古い値を上書きするかどうかを確定する。
         * trueを返した場合、insertValueで値を上書きする。
         */
        public boolean isOverwrite(String key, byte[] currentValue, long currentDate, byte[] insertValue,
                                   long insertDate);
    }

    private static final String[] cursorDatas = {
            DB_KEY, DB_VALUE, DB_DATE
    };

    /**
     * KVS内の1データを管理する。
     *
     * @author TAKESHI YAMASHITA
     */
    public class Data {
        long date = 0;
        byte[] value = null;
        String key = null;

        public Data(Cursor cursor) {
            key = cursor.getString(0);
            value = cursor.getBlob(1);
            date = cursor.getLong(2);
        }

        /**
         * 保存された日付を取得する。
         */
        public long getDate() {
            return date;
        }

        /**
         * key本体を取得する。
         */
        public String getKey() {
            return key;
        }

        /**
         * value本体を取得する。
         */
        public byte[] getValue() {
            return value;
        }

        /**
         * valueを文字列として取得する。
         */
        public String getValueText() {
            try {
                return new String(value, CHARSET);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
