package com.eaglesakura.android.db;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.eaglesakura.json.JSON;
import com.eaglesakura.thread.MultiRunningTasks;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;
import com.google.protobuf.GeneratedMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 簡易設定用のプロパティを保持するためのクラス
 */
public class BasePropertiesDatabase {
    /**
     * 保存用のデータベースファイル
     */
    protected File databaseFile;

    /**
     * app context
     */
    protected Context context;

    /**
     * ロード済みのプロパティ
     */
    Map<String, Property> propMap = new HashMap<String, Property>();

    class Property {
        /**
         * 現在の値
         */
        String value;

        /**
         * デフォルト値
         */
        final String defaultValue;

        /**
         * データベース用のkey
         */
        final String key;

        /**
         * 読み込み後、値を更新していたらtrue
         */
        boolean modified = false;

        Property(String key, String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }
    }

    protected BasePropertiesDatabase() {

    }

    protected BasePropertiesDatabase(Context context, String dbName) {
        this.context = context.getApplicationContext();
        if (!StringUtil.isEmpty(dbName)) {
            // 対象のDBが指定されている
            this.databaseFile = context.getDatabasePath(dbName);
        }
    }

    public void setDatabaseFile(File databaseFile) {
        this.databaseFile = databaseFile;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getStringProperty(String key) {
        return propMap.get(key).value;
    }

    public int getIntProperty(String key) {
        return Integer.parseInt(getStringProperty(key));
    }

    public long getLongProperty(String key) {
        return Long.parseLong(getStringProperty(key));
    }

    public Date getDateProperty(String key) {
        return new Date(getLongProperty(key));
    }

    public float getFloatProperty(String key) {
        return Float.parseFloat(getStringProperty(key));
    }

    public boolean getBooleanProperty(String key) {
        String value = getStringProperty(key);

        // 保存速度を向上するため、0|1判定にも対応する
        if ("0".equals(value)) {
            return false;
        } else if ("1".equals(value)) {
            return true;
        }
        return Boolean.parseBoolean(getStringProperty(key));
    }

    public double getDoubleProperty(String key) {
        return Double.parseDouble(getStringProperty(key));
    }

    public <T> T getJsonProperty(String key, Class<T> pojo) {
        return JSON.decodeOrNull(getStringProperty(key), pojo);
    }

    /**
     * PNG形式で保存してあるBitmapを取得する
     *
     * @param key
     * @return
     */
    public Bitmap getBitmapProperty(String key) {
        byte[] pngFile = getByteArrayProperty(key);
        if (pngFile == null) {
            return null;
        }

        try {
            return BitmapFactory.decodeStream(new ByteArrayInputStream(pngFile));
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * base64エンコードオブジェクトを取得する
     *
     * @param key
     * @return
     */
    public byte[] getByteArrayProperty(String key) {
        try {
            return StringUtil.toByteArray(getStringProperty(key));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * protocol buffersオブジェクトを取得する
     *
     * @param key
     * @param proto
     * @param <T>
     */
    public <T extends com.google.protobuf.GeneratedMessage> T getProtobufProperty(String key, Class<T> proto) {
        try {
            byte[] prop = getByteArrayProperty(key);
            Method method = proto.getMethod("parseFrom", byte[].class);
            return (T) method.invoke(proto, prop);
        } catch (Exception e) {
            LogUtil.log(e);
            return null;
        }
    }

    /**
     * Viewの値からpropertyを指定する
     *
     * @param key
     * @param view
     */
    public void setPropertyFromView(String key, View view) {
        String value = null;
        if (view instanceof RadioGroup) {
            RadioGroup group = (RadioGroup) view;
            View selectedView = group.findViewById(group.getCheckedRadioButtonId());
            // Tagを指定する
            if (selectedView != null) {
                value = selectedView.getTag().toString();
            } else {
                value = "";
            }
        } else if (view instanceof EditText) {
            value = ((EditText) view).getText().toString();
        } else if (view instanceof CompoundButton) {
            value = String.valueOf(((CompoundButton) view).isChecked());
        } else if (view instanceof TextView) {
            value = ((TextView) view).getText().toString();
        }

        if (value != null) {
            setProperty(key, (Object) value);
        }
    }

    /**
     * プロパティを保存する
     *
     * @param key   プロパティのキー値
     * @param value プロパティの値
     */
    public void setProperty(String key, Object value) {
        Property prop = propMap.get(key);

        // protobuf
        if (value instanceof GeneratedMessage) {
            value = ((com.google.protobuf.GeneratedMessage) value).toByteArray();
        } else if (value instanceof Enum) {
            value = ((Enum) value).name();
        } else if (value instanceof Bitmap) {
            try {
                Bitmap bmp = (Bitmap) value;
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.PNG, 100, os);

                value = os.toByteArray();
            } catch (Exception e) {
                value = null;
            }
        } else if (value instanceof Boolean) {
            // trueならば"1"、falseならば"0"としてしまう
            value = Boolean.TRUE.equals(value) ? "1" : "0";
        }

        if (value instanceof byte[]) {
            prop.value = StringUtil.toString((byte[]) value);
        } else {
            prop.value = value.toString();
        }
        prop.modified = true;
    }

    /**
     * プロパティを追加する
     *
     * @param key
     */
    protected void addProperty(String key, String defaultValue) {
        propMap.put(key, new Property(key, defaultValue));
    }

    /**
     * Bundleに変換する
     *
     * @param compress なるべく容量を少なくする場合はtrue
     * @return
     */
    public Bundle toBundle(boolean compress) {
        Bundle bundle = new Bundle(context.getClassLoader());
        Iterator<Map.Entry<String, Property>> itr = propMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Property> entry = itr.next();
            Property prop = entry.getValue();

            // 圧縮しない、もしくはデフォルトと異なっている場合は変換対象
            if (!compress || !prop.value.equals(prop.defaultValue)) {
                bundle.putString(prop.key, prop.value);
            }
        }
        return bundle;
    }

    /**
     * Bundleから値を復元する
     *
     * @param bundle 復元するテーブル
     */
    public void fromBundle(Bundle bundle) {
        Iterator<Map.Entry<String, Property>> itr = propMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Property> entry = itr.next();
            Property prop = entry.getValue();

            String bunValue = bundle.getString(prop.key);
            if (bunValue != null) {
                // 値が書き込まれていた場合はそちらを優先
                prop.value = bunValue;
            } else {
                // 値が書き込まれていないので、デフォルトを復元
                prop.value = prop.defaultValue;
            }
        }
    }

    /**
     * キャッシュをデータベースに保存する
     */
    public synchronized void commit() {
        Map<String, String> commitValues = new HashMap<>();

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
        TextKeyValueStore kvs = new TextKeyValueStore(context, databaseFile);
        try {
            kvs.open(false);
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

    /**
     * 非同期で値を保存する。
     * その間、値を書き換えても値の保証はしない。
     */
    public void commitAsync() {
        commitAsync(null);
    }

    private static MultiRunningTasks gTaskQueue = new MultiRunningTasks(1);

    static {
        gTaskQueue.setThreadPoolMode(false);
        gTaskQueue.setThreadName("Prop Commit");
    }

    public void commitAsync(final PropsAsyncListener listener) {
        gTaskQueue.pushBack(new Runnable() {
            @Override
            public void run() {
                commit();
                if (listener != null) {
                    listener.onAsyncCompleted(BasePropertiesDatabase.this);
                }
            }
        }).start();
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

        TextKeyValueStore kvs = new TextKeyValueStore(context, databaseFile);
        try {
            kvs.open();

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

        TextKeyValueStore kvs = new TextKeyValueStore(context, databaseFile);
        try {
            kvs.open();

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
    public void loadAsync() {
        (new Thread() {
            @Override
            public void run() {
                load();
            }
        }).start();
    }

    public void loadAsync(final PropsAsyncListener listener) {
        (new Thread() {
            @Override
            public void run() {
                load();
                listener.onAsyncCompleted(BasePropertiesDatabase.this);
            }
        }).start();
    }

    public interface PropsAsyncListener {
        public void onAsyncCompleted(BasePropertiesDatabase database);
    }

    /**
     * 全てのプロパティを最新に保つ
     */
    public void commitAndLoad() {
        // Contextを持たないため読込が行えない
        if (context == null || databaseFile == null) {
            return;
        }

        Map<String, String> commitValues = new HashMap<String, String>();
        TextKeyValueStore kvs = new TextKeyValueStore(context, databaseFile);
        try {
            kvs.open();

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
    public void commitAndLoadAsync() {
        gTaskQueue.pushBack(new Runnable() {
            @Override
            public void run() {
                commitAndLoad();
            }
        }).start();
    }

    /**
     * 値を全てデフォルト化する
     */
    public void clear() {
        Iterator<Map.Entry<String, Property>> iterator = propMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Property> entry = iterator.next();
            Property prop = entry.getValue();
            if (prop.value != null && !prop.value.equals(prop.defaultValue)) {
                prop.modified = true;
            }
            prop.value = prop.defaultValue;
        }
    }
}
