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

    public TextKeyValueStore(File dbFile, Context context, String tableName) {
        this.context = context;
        this.dbFile = dbFile;
        this.tableName = tableName;

        this.helper = new Helper();
    }

    /**
     * 書き込みの準備を行う
     */
    public void beginWrite() {
        db = helper.getWritableDatabase();
        db.beginTransaction();
    }

    /**
     * DBに値を新規登録する。
     * 登録済みの場合、上書きを行う。
     * @param key
     * @param value
     */
    public void insertOrCreate(String key, String value) {
        ContentValues values = new ContentValues();
        values.put(DB_KEY, key);
        values.put(DB_VALUE, value);
        try {
            db.insertOrThrow(tableName, null, values);
        } catch (Exception e) {
            db.delete(tableName, DB_KEY + "='" + key + "'", null);
            db.insert(tableName, null, values);
        }
    }

    /**
     * 書き込みの完了を行う
     */
    public void endWrite() {
        try {
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
            db = null;
        }
    }

    /**
     * 読み込み準備を行う
     */
    public void beginRead() {
        db = helper.getReadableDatabase();
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
        String selection = DB_KEY + "='" + key + "'";
        Cursor cursor = db.query(tableName, new String[] {
            DB_VALUE
        }, selection, null, null, null, null);

        cursor.moveToFirst();
        try {
            return cursor.getString(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 読み込みを終了する
     */
    public void endRead() {
        db.close();
        db = null;
    }

    class Helper extends SQLiteOpenHelper {
        public Helper() {
            super(context, dbFile.getAbsolutePath(), null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + tableName + " (" + DB_KEY + " text primary key, " + DB_VALUE + " text)");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
