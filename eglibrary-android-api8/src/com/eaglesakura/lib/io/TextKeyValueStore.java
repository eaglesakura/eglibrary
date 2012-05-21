package com.eaglesakura.lib.io;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLを利用した簡単なKVSを提供する。
 * @author TAKESHI YAMASHITA
 *
 */
public class TextKeyValueStore {

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
        CREATE_TBL_SQL = "create table " + tableName + " (" + DB_KEY + " text primary key, " + DB_VALUE + " text)";
        db = type.open(this);
    }

    /**
     * 書き込みの準備を行う
     */
    public void beginTransaction() {
        db.beginTransaction();
    }

    /**
     * DBに値を新規登録する。
     * 登録済みの場合、上書きを行う。
     * @param key
     * @param value
     */
    public void insertOrUpdate(String key, String value) {
        ContentValues values = new ContentValues();
        values.put(DB_KEY, key);
        values.put(DB_VALUE, value);
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
        ContentValues values = new ContentValues();
        values.put(DB_KEY, key);
        values.put(DB_VALUE, value);
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
     * テーブルの内容を破棄する
     */
    public void dropTable() {
        db.execSQL(DELETE_TBL_SQL);
        db.execSQL(CREATE_TBL_SQL);
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
}
