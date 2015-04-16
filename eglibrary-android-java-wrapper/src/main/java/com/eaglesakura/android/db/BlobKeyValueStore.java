package com.eaglesakura.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.eaglesakura.android.dao.bkvs.DaoMaster;
import com.eaglesakura.android.dao.bkvs.DaoSession;
import com.eaglesakura.android.dao.bkvs.DbKeyValueData;
import com.eaglesakura.android.dao.bkvs.DbKeyValueDataDao;
import com.eaglesakura.android.util.ImageUtil;
import com.eaglesakura.util.LogUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;

public class BlobKeyValueStore extends BaseDatabase<DaoSession> {

    private final File dbFilePath;

    private static final long NOT_TIMEOUT = 0x6FFFFFFFFFFFFFFFL;

    public BlobKeyValueStore(Context context, File file) {
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
     * 値を取得する
     *
     * @param key
     * @return
     */
    public byte[] get(String key) {
        return get(key, 0x6FFFFFFFFFFFFFFFL);
    }

    /**
     * 値を取得する。
     * <br>
     * ただし、データを保存してからの期間がタイムアウト時間を超えている場合、このメソッドはnullを返却する。
     *
     * @param key
     * @param timeoutMs
     * @return
     */
    public byte[] get(String key, long timeoutMs) {
        DbKeyValueData data = session.load(DbKeyValueData.class, key);
        if (data != null && (System.currentTimeMillis() - data.getDate().getTime()) < timeoutMs) {
            return data.getValue();
        } else {
            return null;
        }
    }

    /**
     * 画像として読みだす
     * <br>
     * ただし、データを保存してからの期間がタイムアウト時間を超えている場合、このメソッドはnullを返却する。
     *
     * @param key PNG画像のキー
     * @return 画像 or NULL
     */
    public Bitmap getImage(String key, long timeoutMs) {
        try {
            byte[] buffer = get(key, timeoutMs);
            return BitmapFactory.decodeStream(new ByteArrayInputStream(buffer));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 画像として読みだす
     *
     * @param key PNG画像のキー
     * @return 画像 or NULL
     */
    public Bitmap getImage(String key) {
        return getImage(key, NOT_TIMEOUT);
    }

    /**
     * DBから値を読み出し、アルファ合成を行って取得する
     *
     * @param key   読み出すイメージ
     * @param alpha アルファ合成用画像
     * @return 合成したイメージ
     */
    public Bitmap getImageWithAlphaBlend(String key, Bitmap alpha) {
        Bitmap origin = getImage(key);
        if (origin == null) {
            return null;
        }

        try {
            return ImageUtil.blendAlpha(origin, alpha);
        } finally {
            origin.recycle();
        }
    }

    /**
     * 値を保存する
     *
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
     * 画像を保存する
     *
     * @param key    キー
     * @param bitmap 保存する画像。PNG化される
     */
    public void put(String key, Bitmap bitmap) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

            put(key, os.toByteArray());
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    /**
     * 画像を指定されたフォーマットで保存する
     *
     * @param key    キー
     * @param bitmap 保存する画像。PNG化される
     */
    public void put(String key, Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            bitmap.compress(format, quality, os);
            os.flush();
            byte[] bytes = os.toByteArray();
            put(key, bytes);
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    /**
     * 値を保存する
     *
     * @param key
     * @param data
     */
    public void put(String key, com.google.protobuf.GeneratedMessage data) {
        put(key, data.toByteArray());
    }

    /**
     * protocol buffersとして取得する
     *
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
