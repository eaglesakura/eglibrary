package com.eaglesakura.lib.io;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.util.LogUtil;

/**
 * SQLを利用した簡単なKVSを提供する。
 * insert時にはkey-valueと更新時刻が保存される。
 * @author TAKESHI YAMASHITA
 *
 */
public class TextKeyValueStore extends DisposableResource {

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
     * DBのバージョン
     */
    static final int DB_VERSION = 0x0001;

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
     * データベースの開き方を指定
     */
    DBType type;

    public enum DBType {
        /**
         * 読み書きを行う
         */
        ReadWrite {
            @Override
            SQLiteDatabase open(TextKeyValueStore kvs) {
                return kvs.helper.getWritableDatabase();
            }
        },
        /**
         * 書き込み専用
         */
        Write {
            @Override
            SQLiteDatabase open(TextKeyValueStore kvs) {
                return kvs.helper.getWritableDatabase();
            }
        },

        /**
         * 読み込み専用
         */
        Read {
            @Override
            SQLiteDatabase open(TextKeyValueStore kvs) {
                return kvs.helper.getReadableDatabase();
            }
        };

        abstract SQLiteDatabase open(TextKeyValueStore kvs);
    }

    public TextKeyValueStore(File dbFile, Context context, String tableName, DBType type) {
        this.context = context;
        this.dbFile = dbFile;
        this.tableName = tableName;

        this.helper = new Helper();

        this.type = type;

        DELETE_TBL_SQL = "drop table if exists " + tableName;
        CREATE_TBL_SQL = "create table if not exists " + tableName + " (" + DB_KEY + " text primary key, " + DB_VALUE
                + " text, " + DB_DATE + " integer )";
        db = type.open(this);
    }

    /**
     * 書き込みの準備を行う
     */
    public void beginTransaction() {
        db.beginTransaction();
    }

    protected ContentValues createValues(String key, String value) {
        ContentValues result = new ContentValues(3);
        result.put(DB_KEY, key);
        result.put(DB_VALUE, value);
        result.put(DB_DATE, System.currentTimeMillis());
        return result;
    }

    /**
     * DBに値を新規登録する。
     * 登録済みの場合、上書きを行う。
     * @param key
     * @param value
     */
    public void insertOrUpdate(String key, String value) {
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
     * @param key
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
    public String get(String key, String _def) {
        String result = getOrNull(key);
        if (result == null) {
            return _def;
        } else {
            return result;
        }
    }

    /**
     * 値を取得する
     * @param key
     * @return
     */
    public String getOrNull(String key) {
        try {
            String selection = DB_KEY + "='" + key + "'";
            Cursor cursor = db.query(tableName, new String[] {
                DB_VALUE
            }, selection, null, null, null, null);

            cursor.moveToFirst();
            return cursor.getString(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * キーが存在したらtrue
     * @param key
     * @return
     */
    public boolean exists(String key) {
        return get(key, null) != null;
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
     * @param key
     * @param value
     * @param date
     * @param filter
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
        Cursor cursor = db.query(tableName, new String[] {
                DB_VALUE, DB_DATE,
        }, DB_KEY + "='" + key + "'", null, null, null, null);
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
    }

    /**
     * insertDBをこのオブジェクトが管理するDBに結合する。
     * データが競合した場合、どちらを優先するかはfilterによって確定される。
     * @param insertDB
     */
    public void insertTo(final TextKeyValueStore insertDB, InsertFilter filter) {
        Cursor cursor = insertDB.db.query(tableName, new String[] {
                DB_KEY, DB_VALUE, DB_DATE,
        }, null, null, null, null, null);

        cursor.moveToFirst();
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

    public void print() {
        String selection = null;
        Cursor cursor = db.query(tableName, new String[] {
                DB_KEY, DB_VALUE
        }, selection, null, null, null, null);

        cursor.moveToFirst();
        do {
            String key = cursor.getString(0);
            String value = cursor.getString(1);
            LogUtil.log(key + " :: " + value);
        } while (cursor.moveToNext());
    }

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
            super(context, dbFile.getAbsolutePath(), null, DB_VERSION);
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
     * @author TAKESHI YAMASHITA
     *
     */
    public interface InsertFilter {
        /**
         * insertValueで古い値を上書きするかどうかを確定する。
         * trueを返した場合、insertValueで値を上書きする。
         * @param key
         * @param currentValue
         * @param currentDate
         * @param insertValue
         * @param insertDate
         * @return
         */
        public boolean isOverwrite(String key, String currentValue, long currentDate, String insertValue,
                long insertDate);
    }
}
