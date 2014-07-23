package com.eaglesakura.lib.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

public enum DBValueType {
    /**
     * 整数型
     */
    Integer {
        @Override
        public void toBundle(Bundle result, String key, Cursor cursor, int dataIndex) {
            result.putLong(key, cursor.getLong(dataIndex));
        }

        @Override
        public void toValue(ContentValues result, String key, Object origin) {
            result.put(key, java.lang.Long.valueOf(origin.toString()));
        }
    },

    /**
     * 文字列型
     */
    Text {
        @Override
        public void toBundle(Bundle result, String key, Cursor cursor, int dataIndex) {
            result.putString(key, cursor.getString(dataIndex));
        }

        @Override
        public void toValue(ContentValues result, String key, Object origin) {
            result.put(key, origin.toString());
        }
    },

    /**
     * Blob型
     */
    Blob {
        @Override
        public void toBundle(Bundle result, String key, Cursor cursor, int dataIndex) {
            result.putByteArray(key, cursor.getBlob(dataIndex));
        }

        @Override
        public void toValue(ContentValues result, String key, Object origin) {
            result.put(key, (byte[]) origin);
        }
    },

    /**
     * Real型
     */
    Real {
        @Override
        public void toBundle(Bundle result, String key, Cursor cursor, int dataIndex) {
            result.putDouble(key, cursor.getDouble(dataIndex));
        }

        @Override
        public void toValue(ContentValues result, String key, Object origin) {
            result.put(key, Double.valueOf(origin.toString()));
        }
    };

    /**
     * SQL命令上の型名を取得する
     * @return
     */
    public String getSqlValueType() {
        return name().toLowerCase();
    }

    public abstract void toBundle(Bundle result, String key, Cursor cursor, int dataIndex);

    /**
     * valuesに突っ込む
     * @param result
     * @param origin
     */
    public abstract void toValue(ContentValues result, String key, Object origin);
}
