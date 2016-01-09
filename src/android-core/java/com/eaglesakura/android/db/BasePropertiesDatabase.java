package com.eaglesakura.android.db;

import android.content.Context;

import com.eaglesakura.android.thread.async.AsyncTaskController;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.util.StringUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 簡易設定用のプロパティを保持するためのクラス
 */
public abstract class BasePropertiesDatabase extends BaseProperties {

    /**
     * 保存用のデータベースファイル
     */
    protected File databaseFile;

    protected BasePropertiesDatabase(Context context, String dbName) {
        super(context);
        if (context != null) {
            this.context = context.getApplicationContext();
            if (!StringUtil.isEmpty(dbName)) {
                // 対象のDBが指定されている
                this.databaseFile = context.getDatabasePath(dbName);
            }
        }
    }

    public void setDatabaseFile(File databaseFile) {
        this.databaseFile = databaseFile;
    }

    /**
     * キャッシュをデータベースに保存する
     */
    public synchronized void commit() {
        final Map<String, String> commitValues = new HashMap<>();

        // Commitする内容を抽出する
        {
            Iterator<Map.Entry<String, Property>> iterator = propMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Property property = iterator.next().getValue();
                if (property.modified) {
                    commitValues.put(property.key, property.value);
                }
            }
        }

        // 不要であれば何もしない
        if (commitValues.isEmpty()) {
            return;
        }

        // 保存する
        TextKeyValueStore kvs = new TextKeyValueStore(context, databaseFile, TextKeyValueStore.TABLE_NAME_DEFAULT);
        try {
            kvs.open(DBType.Write);
            kvs.putInTx(commitValues);

            // コミットが成功したらmodified属性を元に戻す
            {
                Iterator<Map.Entry<String, Property>> iterator = propMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Property property = iterator.next().getValue();
                    property.modified = false;
                }
            }
        } finally {
            kvs.close();
        }
    }

    private static AsyncTaskController gTaskController = new AsyncTaskController(1);

    /**
     * 非同期で値を保存する。
     * その間、値を書き換えても値の保証はしない。
     */
    public AsyncTaskResult<AsyncTaskController> commitAsync() {
        return gTaskController.pushBack(new Runnable() {
            @Override
            public void run() {
                commit();
            }
        });
    }

    /**
     * 指定したキーのみをDBからロードする
     *
     * @param key
     */
    public void load(String key) {
        load(new String[]{key});
    }

    /**
     * 指定したキーのみをDBからロードする
     *
     * @param keys
     */
    public void load(String[] keys) {
        // Contextを持たないため読込が行えない
        if (context == null || databaseFile == null || keys.length == 0) {
            return;
        }

        TextKeyValueStore kvs = new TextKeyValueStore(context, databaseFile, TextKeyValueStore.TABLE_NAME_DEFAULT);
        try {
            kvs.open(DBType.Read);

            for (String key : keys) {
                Property property = propMap.get(key);
                if (property != null) {
                    property.value = kvs.get(property.key, property.defaultValue);
                    property.modified = false;
                }
            }

        } finally {
            kvs.close();
        }
    }

    /**
     * データをDBからロードする
     * <br>
     * 既存のキャッシュはクリーンされる
     */
    public void load() {
        // Contextを持たないため読込が行えない
        if (context == null || databaseFile == null) {
            return;
        }

        TextKeyValueStore kvs = new TextKeyValueStore(context, databaseFile, TextKeyValueStore.TABLE_NAME_DEFAULT);
        try {
            kvs.open(DBType.Read);

            Iterator<Map.Entry<String, Property>> iterator = propMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Property value = iterator.next().getValue();
                // リロードする。読み込めなかった場合は規定のデフォルト値を持たせる
                value.value = kvs.get(value.key, value.defaultValue);
                // sync直後なのでcommit対象ではない
                value.modified = false;
            }
        } finally {
            kvs.close();
        }
    }

    /**
     * 非同期でデータを読み込む
     */
    public AsyncTaskResult<AsyncTaskController> loadAsync() {
        return gTaskController.pushBack(new Runnable() {
            @Override
            public void run() {
                load();
            }
        });
    }

    /**
     * 全てのプロパティを最新に保つ
     */
    public void commitAndLoad() {
        // Contextを持たないため読込が行えない
        if (context == null || databaseFile == null) {
            return;
        }

        Map<String, String> commitValues = new HashMap<>();
        TextKeyValueStore kvs = new TextKeyValueStore(context, databaseFile, TextKeyValueStore.TABLE_NAME_DEFAULT);
        try {
            kvs.open(DBType.Read);

            Iterator<Map.Entry<String, Property>> iterator = propMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Property value = iterator.next().getValue();
                // リロードする。読み込めなかった場合は規定のデフォルト値を持たせる
                if (value.modified) {
                    // 変更がある値はDBへ反映リストに追加
                    commitValues.put(value.key, value.value);
                } else {
                    // 変更が無いならばDBから読み出す
                    value.value = kvs.get(value.key, value.defaultValue);
                }
                // sync直後なのでcommit対象ではない
                value.modified = false;
            }

            // 変更を一括更新
            kvs.putInTx(commitValues);
        } finally {
            kvs.close();
        }
    }

    /**
     * 非同期にコミット＆ロードを行い、設定を最新に保つ
     */
    public AsyncTaskResult<AsyncTaskController> commitAndLoadAsync() {
        return gTaskController.pushBack(new Runnable() {
            @Override
            public void run() {
                commitAndLoad();
            }
        });
    }

    /**
     * @param runnable
     */
    public static AsyncTaskResult<AsyncTaskController> runInTaskQueue(Runnable runnable) {
        return gTaskController.pushBack(runnable);
    }
}
