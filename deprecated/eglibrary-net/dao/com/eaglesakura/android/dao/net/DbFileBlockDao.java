package com.eaglesakura.android.dao.net;

import com.eaglesakura.android.dao.net.DbFileBlock;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table DB_FILE_BLOCK.
 */
public class DbFileBlockDao extends AbstractDao<DbFileBlock, Void> {

    public static final String TABLENAME = "DB_FILE_BLOCK";

    /**
     * Properties of entity DbFileBlock.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Url = new Property(0, String.class, "url", false, "URL");
        public final static Property Number = new Property(1, int.class, "number", false, "NUMBER");
        public final static Property Body = new Property(2, byte[].class, "body", false, "BODY");
        public final static Property Eof = new Property(3, boolean.class, "eof", false, "EOF");
    }

    ;


    public DbFileBlockDao(DaoConfig config) {
        super(config);
    }

    public DbFileBlockDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "'DB_FILE_BLOCK' (" + //
                "'URL' TEXT NOT NULL ," + // 0: url
                "'NUMBER' INTEGER NOT NULL ," + // 1: number
                "'BODY' BLOB NOT NULL ," + // 2: body
                "'EOF' INTEGER NOT NULL );"); // 3: eof
        // Add Indexes
        db.execSQL("CREATE INDEX " + constraint + "IDX_DB_FILE_BLOCK_URL ON DB_FILE_BLOCK" +
                " (URL);");
        db.execSQL("CREATE INDEX " + constraint + "IDX_DB_FILE_BLOCK_NUMBER ON DB_FILE_BLOCK" +
                " (NUMBER);");
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'DB_FILE_BLOCK'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, DbFileBlock entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getUrl());
        stmt.bindLong(2, entity.getNumber());
        stmt.bindBlob(3, entity.getBody());
        stmt.bindLong(4, entity.getEof() ? 1l : 0l);
    }

    /** @inheritdoc */
    @Override
    public Void readKey(Cursor cursor, int offset) {
        return null;
    }

    /** @inheritdoc */
    @Override
    public DbFileBlock readEntity(Cursor cursor, int offset) {
        DbFileBlock entity = new DbFileBlock( //
                cursor.getString(offset + 0), // url
                cursor.getInt(offset + 1), // number
                cursor.getBlob(offset + 2), // body
                cursor.getShort(offset + 3) != 0 // eof
        );
        return entity;
    }

    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, DbFileBlock entity, int offset) {
        entity.setUrl(cursor.getString(offset + 0));
        entity.setNumber(cursor.getInt(offset + 1));
        entity.setBody(cursor.getBlob(offset + 2));
        entity.setEof(cursor.getShort(offset + 3) != 0);
    }

    /** @inheritdoc */
    @Override
    protected Void updateKeyAfterInsert(DbFileBlock entity, long rowId) {
        // Unsupported or missing PK type
        return null;
    }

    /** @inheritdoc */
    @Override
    public Void getKey(DbFileBlock entity) {
        return null;
    }

    /** @inheritdoc */
    @Override
    protected boolean isEntityUpdateable() {
        return true;
    }

}
