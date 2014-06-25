package com.eaglesakura.android.db;

import java.io.File;
import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.eaglesakura.andriders.dao.tkvs.DaoMaster;
import com.eaglesakura.andriders.dao.tkvs.DaoSession;
import com.eaglesakura.andriders.dao.tkvs.DbKeyValueData;
import com.eaglesakura.json.JSON;
import com.eaglesakura.util.StringUtil;

/**
 * Key-Valueのシンプルなデータベース
 */
public class TextKeyValueStore extends BaseDatabase<DaoSession> {

    private final File dbFilePath;

    public TextKeyValueStore(Context context, File file) {
        super(context, DaoMaster.class);
        this.dbFilePath = file;
    }

    @Override
    protected SQLiteOpenHelper createHelper() {
        return new DaoMaster.DevOpenHelper(context, dbFilePath.getAbsolutePath(), null);
    }

    /**
     * データの更新を行う
     * @param key
     * @param value
     */
    public void put(String key, byte[] value) {
        put(key, StringUtil.toString(value));
    }

    /**
     * データの更新を行う
     * @param key
     * @param value
     */
    public void put(String key, long value) {
        put(key, String.valueOf(value));
    }

    /**
     * データの更新を行う
     * @param key
     * @param value
     */
    public void put(String key, double value) {
        put(key, String.valueOf(value));
    }

    /**
     * データの更新を行う
     * @param key
     * @param value
     */
    public void put(String key, String value) {
        DbKeyValueData db = new DbKeyValueData();
        db.setDate(new Date());
        db.setKey(key);
        db.setValue(value);

        insertOrUpdate(db, session.getDbKeyValueDataDao());
    }

    /**
     * データの更新を行う
     * @param key
     * @param obj
     */
    public <T> void putJson(String key, T obj) {
        put(key, JSON.encodeOrNull(obj));
    }

    /**
     * 値を取得する
     * @param key
     * @param def
     * @return
     */
    public String get(String key, String def) {
        DbKeyValueData data = session.load(DbKeyValueData.class, key);

        String result = def;
        if (data != null) {
            result = data.getValue();
        }

        return result;
    }

    /**
     * long値を取得する
     * @param key
     * @param def
     * @return
     */
    public long getLong(String key, long def) {
        String value = get(key, null);
        if (!StringUtil.isEmpty(value)) {
            try {
                return Long.parseLong(value);
            } catch (Exception e) {
            }
        }

        return def;
    }

    /**
     * double値を取得する
     * @param key
     * @param def
     * @return
     */
    public double getDouble(String key, double def) {
        String value = get(key, null);
        if (!StringUtil.isEmpty(value)) {
            try {
                return Double.parseDouble(value);
            } catch (Exception e) {
            }
        }

        return def;
    }

    /**
     * バイト配列値を取得する
     * @param key
     * @return
     */
    public byte[] getByteArray(String key) {
        String value = get(key, null);
        if (StringUtil.isEmpty(value)) {
            return null;
        } else {
            return StringUtil.toByteArray(value);
        }
    }

    /**
     * JSONとして取得する
     * @param key
     * @param clz
     * @return
     */
    public <T> T getJson(String key, Class<T> clz) {
        return JSON.decodeOrNull(get(key, null), clz);
    }
}
