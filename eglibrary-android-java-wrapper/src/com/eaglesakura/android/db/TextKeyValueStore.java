package com.eaglesakura.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.eaglesakura.android.dao.tkvs.DaoMaster;
import com.eaglesakura.android.dao.tkvs.DaoSession;
import com.eaglesakura.android.dao.tkvs.DbKeyValueData;
import com.eaglesakura.android.dao.tkvs.DbKeyValueDataDao;
import com.eaglesakura.json.JSON;
import com.eaglesakura.util.StringUtil;

import java.io.File;
import java.util.Date;

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
     * 指定したkeyの情報を持っているならば、trueを返す
     *
     * @param key 調べるkey
     */
    public boolean hasValue(String key) {
        return session.queryBuilder(DbKeyValueData.class).where(DbKeyValueDataDao.Properties.Key.eq(key)).count() == 1;
    }

    /**
     * データの更新を行う
     *
     * @param key
     * @param value
     */
    public void put(String key, byte[] value) {
        put(key, StringUtil.toString(value));
    }

    /**
     * データの更新を行う
     *
     * @param key
     * @param value
     */
    public void put(String key, long value) {
        put(key, String.valueOf(value));
    }

    /**
     * データの更新を行う
     *
     * @param key
     * @param value
     */
    public void put(String key, double value) {
        put(key, String.valueOf(value));
    }

    /**
     * データの更新を行う
     *
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
     *
     * @param key
     * @param obj
     */
    public <T> void putJson(String key, T obj) {
        put(key, JSON.encodeOrNull(obj));
    }

    /**
     * 値を取得する
     *
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
     * int値を取得する
     *
     * @param key
     * @param def
     * @return
     */
    public int getInteger(String key, int def) {
        String value = get(key, null);
        if (!StringUtil.isEmpty(value)) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
            }
        }

        return def;
    }

    /**
     * long値を取得する
     *
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
     *
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
     *
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
     *
     * @param key
     * @param clz
     * @return
     */
    public <T> T getJson(String key, Class<T> clz) {
        return JSON.decodeOrNull(get(key, null), clz);
    }
}
