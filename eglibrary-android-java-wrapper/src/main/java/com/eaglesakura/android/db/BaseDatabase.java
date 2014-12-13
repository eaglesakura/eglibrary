package com.eaglesakura.android.db;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.eaglesakura.util.LogUtil;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoMaster;
import de.greenrobot.dao.AbstractDaoSession;

public abstract class BaseDatabase<SessionClass extends AbstractDaoSession> {

    protected final Context context;

    protected final Class<? extends AbstractDaoMaster> daoMasterClass;

    protected AbstractDaoMaster daoMaster;

    protected SessionClass session;

    final private Object refsLock = new Object();

    private int refs = 0;

    /**
     * 新規に生成する
     *
     * @param context
     * @param daoMasterClass
     */
    public BaseDatabase(Context context, Class<? extends AbstractDaoMaster> daoMasterClass) {
        this.context = context.getApplicationContext();
        this.daoMasterClass = daoMasterClass;
    }

    /**
     * DaoMasterを指定して生成する
     *
     * @param context
     * @param daoMaster
     */
    public BaseDatabase(Context context, AbstractDaoMaster daoMaster) {
        this.context = context.getApplicationContext();
        this.daoMaster = daoMaster;
        this.daoMasterClass = daoMaster.getClass();
    }

    protected abstract SQLiteOpenHelper createHelper();

    @SuppressWarnings("unchecked")
    public void open() {
        synchronized (refsLock) {
            if (daoMaster == null) {
                SQLiteOpenHelper helper = createHelper();
                SQLiteDatabase db = helper.getWritableDatabase();

                try {
                    Constructor<? extends AbstractDaoMaster> constructor = daoMasterClass.getConstructor(SQLiteDatabase.class);
                    daoMaster = constructor.newInstance(db);
                    session = (SessionClass) daoMaster.newSession();
                } catch (Exception e) {
                    LogUtil.d(e);
                    throw new IllegalStateException();
                }
            }
            ++refs;
        }
    }

    /**
     * Sessionを取得する
     * queryを投げるのに使う。
     *
     * @return
     */
    public SessionClass getSession() {
        return session;
    }

    /**
     *
     */
    public void close() {
        synchronized (refsLock) {
            --refs;
            if (refs > 0) {
                // まだ開いているセッションがあるため、閉じる必要はない
                return;
            }

            if (daoMaster != null) {
                daoMaster.getDatabase().close();
                daoMaster = null;
            }
        }
    }

    /**
     * データベースを削除する
     */
    public void drop() {
        try {
            Method dropAllTables = daoMasterClass.getMethod("dropAllTables", SQLiteDatabase.class, boolean.class);
            dropAllTables.invoke(daoMaster, daoMaster.getDatabase(), true);

            Method createAllTables = daoMasterClass.getMethod("createAllTables", SQLiteDatabase.class, boolean.class);
            createAllTables.invoke(daoMaster, daoMaster.getDatabase(), true);
        } catch (Exception e) {
            LogUtil.d(e);
        }
    }

    /**
     * insertを試行し、失敗したらupdateを行う
     *
     * @param entity
     * @param session
     */
    protected <T, K> void insertOrUpdate(T entity, AbstractDao<T, K> session) {
        try {
            session.insertOrReplace(entity);
        } catch (SQLiteConstraintException e) {
            session.update(entity);
        }
    }

    /**
     * ラップしたオブジェクトを返す
     *
     * @param origin
     * @param originClass
     * @param convertClass
     * @return
     */
    public static <T, K> List<K> wrap(List<T> origin, Class<T> originClass, Class<K> convertClass) {
        try {
            Constructor<K> constructor = convertClass.getConstructor(originClass);
            List<K> result = new ArrayList<K>();

            for (T org : origin) {
                result.add(constructor.newInstance(org));
            }

            return result;
        } catch (Exception e) {
            LogUtil.d(e);
            return null;
        }
    }
}
