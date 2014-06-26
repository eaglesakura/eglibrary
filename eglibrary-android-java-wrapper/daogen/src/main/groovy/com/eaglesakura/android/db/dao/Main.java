package com.eaglesakura.android.db.dao;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class Main {

    private static final String PACKAGE_NAME = "com.eaglesakura.android.dao";

    private static final String OUTPUT_PATH = "../dao";

    public static void main(String[] args) {
        try {
            genTextKeyValueStoreDao();
            genBlobKeyValueStoreDao();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * シンプルなKey-Value-Storeの内容
     * @throws Exception
     */
    static void genTextKeyValueStoreDao() throws Exception {
        Schema scheme = new Schema(0x01, PACKAGE_NAME + ".tkvs");
        {
            Entity entity = scheme.addEntity("DbKeyValueData");
            // uniqueId
            entity.addStringProperty("key").notNull().unique().primaryKey();
            // value
            entity.addStringProperty("value");
            // 更新日時
            entity.addDateProperty("date").notNull();
        }

        new DaoGenerator().generateAll(scheme, OUTPUT_PATH);
    }

    /**
     * シンプルなKey-Value-Storeの内容
     * @throws Exception
     */
    static void genBlobKeyValueStoreDao() throws Exception {
        Schema scheme = new Schema(0x01, PACKAGE_NAME + ".bkvs");
        {
            Entity entity = scheme.addEntity("DbKeyValueData");
            // uniqueId
            entity.addStringProperty("key").notNull().unique().primaryKey();
            // value
            entity.addByteArrayProperty("value");
            // 更新日時
            entity.addDateProperty("date").notNull();
        }

        new DaoGenerator().generateAll(scheme, OUTPUT_PATH);
    }
}
