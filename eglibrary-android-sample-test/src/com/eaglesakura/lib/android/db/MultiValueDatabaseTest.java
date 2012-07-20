package com.eaglesakura.lib.android.db;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.os.Environment;
import android.test.AndroidTestCase;

import com.eaglesakura.lib.android.db.MultiValueDatabase.Column;
import com.eaglesakura.lib.android.game.util.LogUtil;

public class MultiValueDatabaseTest extends AndroidTestCase {
    MultiValueDatabase.ValueList valueList = new MultiValueDatabase.ValueList("_tbl");
    final File DB_FILE = new File(Environment.getExternalStorageDirectory(), "test.db");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LogUtil.setTag("gamelib");
        LogUtil.setOutput(true);

        valueList.setPrimary(new Column(DBValueType.Text, "_shf"));
        valueList.addColmn(new Column(DBValueType.Text, "_bh"));

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
}
