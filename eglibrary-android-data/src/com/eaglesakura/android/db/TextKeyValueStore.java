package com.eaglesakura.android.db;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.eaglesakura.andriders.dao.tkvs.DaoMaster;
import com.eaglesakura.andriders.dao.tkvs.DaoSession;
import com.eaglesakura.andriders.dao.tkvs.DbKeyValueData;
import com.eaglesakura.andriders.dao.tkvs.DbKeyValueDataDao.Properties;
import com.eaglesakura.util.StringUtil;

/**
 * Key-Valueのシンプルなデータベース
 */
public class TextKeyValueStore extends BaseDatabase<DaoSession> {

    private final File dbFilePath;

    public TextKeyValueStore(Context context, DaoMaster daoMasterClass, File file) {
        super(context, daoMasterClass);
        this.dbFilePath = file;
    }

    @Override
    protected SQLiteOpenHelper createHelper() {
        return new DaoMaster.DevOpenHelper(context, dbFilePath.getAbsolutePath(), null);
    }

    /**
     * 値を取得する
     * @param key
     * @param def
     * @return
     */
    public String get(String key, String def) {
        List<DbKeyValueData> list = session.queryBuilder(DbKeyValueData.class).where(Properties.Key.eq(key)).list();

        String result = def;
        if (list.size() == 1) {
            result = list.get(0).getValue();
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
}
