package com.eaglesakura.lib.android.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public enum DBType {
    /**
     * 読み書きを行う
     */
    ReadWrite {
        @Override
        SQLiteDatabase open(SQLiteOpenHelper helper) {
            return helper.getWritableDatabase();
        }
    },
    /**
     * 書き込み専用
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
