package com.eaglesakura.android.db;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.eaglesakura.andriders.dao.bkvs.DaoMaster;
import com.eaglesakura.andriders.dao.bkvs.DaoSession;
import com.eaglesakura.andriders.dao.bkvs.DbKeyValueData;
import com.eaglesakura.util.LogUtil;

public class BlobKeyValueStore extends BaseDatabase<DaoSession> {

    private final File dbFilePath;

    public BlobKeyValueStore(Context context, File file) {
        super(context, DaoMaster.class);
        this.dbFilePath = file;
    }

    @Override
    protected SQLiteOpenHelper createHelper() {
        return new DaoMaster.DevOpenHelper(context, dbFilePath.getAbsolutePath(), null);
    }

    /**
     * 値を取得する
     * @param key
     * @return
     */
    public byte[] get(String key) {
        DbKeyValueData data = session.load(DbKeyValueData.class, key);
        if (data != null) {
            return data.getValue();
        } else {
            return null;
        }
    }

    /**
     * 値を保存する
     * @param key
     * @param buffer
     */
    public void put(String key, byte[] buffer) {
        DbKeyValueData db = new DbKeyValueData();
        db.setKey(key);
        db.setDate(new Date());
        db.setValue(buffer);

        insertOrUpdate(db, session.getDbKeyValueDataDao());
    }

    /**
     * 値を保存する
     * @param key
     * @param data
     */
    public void put(String key, com.google.protobuf.GeneratedMessage data) {
        put(key, data.toByteArray());
    }

    /**
     * protocol buffersとして取得する
     * @param key
     * @param clz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends com.google.protobuf.GeneratedMessage> T getProtocolBuffer(String key, Class<T> clz) {

        try {
            byte[] raw = get(key);
            // データが取得できない
            if (raw == null) {
                return null;
            }

            Method parseFrom = clz.getMethod("parseFrom", byte[].class);
            T object = (T) parseFrom.invoke(clz, raw);

            return object;
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return null;
    }
}
