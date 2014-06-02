package com.eaglesakura.lib.android.db;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Environment;
import android.test.AndroidTestCase;

import com.eaglesakura.lib.android.db.MultiValueDatabase.Column;
import com.eaglesakura.lib.android.db.MultiValueDatabase.Data;
import com.eaglesakura.lib.android.game.util.LogUtil;

public class MultiValueDatabaseTest extends AndroidTestCase {
    MultiValueDatabase.ValueList valueList = new MultiValueDatabase.ValueList("_tbl");
    final File DB_FILE = new File(Environment.getExternalStorageDirectory(), "test.db");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LogUtil.setTag("gamelib");
        LogUtil.setOutput(true);

        //      valueList.setPrimary(new Column(DBValueType.Text, "_shf"));
        valueList.addColmn(new Column(DBValueType.Text, "_shf"));
        valueList.addColmn(new Column(DBValueType.Text, "_bh"));
        valueList.addColmn(new Column(DBValueType.Text, "_cmt"));

        DB_FILE.delete();
    }

    public void sqlCreateTest() {
        LogUtil.log("begin");
        LogUtil.log("sql = " + valueList.sqlCreateCommand());
        LogUtil.log("sql = " + valueList.sqlDropCommand());
    }

    public void insertTest() {
        MultiValueDatabase db = new MultiValueDatabase(getContext(), DB_FILE, DBType.ReadWrite, valueList);

        // データを新規追加する
        {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("_shf", "shelf");
            values.put("_bh", "bookhash");

            assertTrue(db.insertOrUpdate(values));

            // データが正常に取得できなければならない
            MultiValueDatabase.Data data = db.getOrNull("shelf");
            assertNotNull(data);
            assertEquals(data.getText("_bh", null), "bookhash");
        }

        // データを更新する
        {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("_shf", "shelf");
            values.put("_bh", "bookhash-update");

            assertTrue(db.insertOrUpdate(values));

            // データが正常に取得できなければならない
            MultiValueDatabase.Data data = db.getOrNull("shelf");
            assertNotNull(data);
            assertEquals(data.getText("_bh", null), "bookhash-update");
        }
    }

    public void insertTestNoBH() {
        MultiValueDatabase db = new MultiValueDatabase(getContext(), DB_FILE, DBType.ReadWrite, valueList);

        // データを新規追加する
        {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("_shf", "shelf");

            assertTrue(db.insertOrUpdate(values));

            // データが正常に取得できなければならない
            MultiValueDatabase.Data data = db.getOrNull("shelf");
            assertNotNull(data);
            assertEquals(data.getText("_bh", null), null);
        }

        // データを更新する
        {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put("_shf", "shelf");
            values.put("_bh", "bookhash-update");

            assertTrue(db.insertOrUpdate(values));

            // データが正常に取得できなければならない
            MultiValueDatabase.Data data = db.getOrNull("shelf");
            assertNotNull(data);
            assertEquals(data.getText("_bh", null), "bookhash-update");
        }
    }

    public void listTest() {

        MultiValueDatabase db = new MultiValueDatabase(getContext(), DB_FILE, DBType.ReadWrite, valueList);

        // データを新規追加する
        {
            {
                Map<String, Object> values = new HashMap<String, Object>();
                values.put("_shf", "shelf0");
                values.put("_bh", "bookhash");
                values.put("_cmt", "こめんと");
                assertTrue(db.insertOrUpdate(values));
            }
            {
                Map<String, Object> values = new HashMap<String, Object>();
                values.put("_shf", "shelf1");
                values.put("_bh", "bookhash");
                values.put("_cmt", "こめんと2");
                assertTrue(db.insertOrUpdate(values));
            }
            {
                Map<String, Object> values = new HashMap<String, Object>();
                values.put("_shf", "shelf2");
                values.put("_bh", "bookhash");
                values.put("_cmt", "こめんと4");
                assertTrue(db.insertOrUpdate(values));
            }
        }

        List<Data> list = db.list("_bh", "bookhash");
        assertTrue(list.size() == 3);
        for (Data d : list) {
            assertEquals(d.getText("_bh", null), "bookhash");
        }
    }
}
