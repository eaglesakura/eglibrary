package com.eaglesakura.android.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public enum DBType {
    /**
     * 書き込み可能
     */
    Write {
        @Override
        SQLiteDatabase open(SQLiteOpenHelper helper) {
            return helper.getWritableDatabase();
        }
    },

    /**
     * 読み込み専用
     */
    Read {
        @Override
        SQLiteDatabase open(SQLiteOpenHelper helper) {
            return helper.getReadableDatabase();
        }
    };

    abstract SQLiteDatabase open(SQLiteOpenHelper helper);
}
