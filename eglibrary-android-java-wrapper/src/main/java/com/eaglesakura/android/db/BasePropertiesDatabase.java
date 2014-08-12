package com.eaglesakura.android.db;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.eaglesakura.json.JSON;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessage;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
        this.databaseFile = context.getDatabasePath(dbName);
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
        return Boolean.parseBoolean(getStringProperty(key));
    }

    public double getDoubleProperty(String key) {
        return Double.parseDouble(getStringProperty(key));
    }

    public <T> T getJsonProperty(String key, Class<T> pojo) {
        return JSON.decodeOrNull(getStringProperty(key), pojo);
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
     * キャッシュをデータベースに保存する
     */
    public void commit() {
        Map<String, String> commitValues = new HashMap<String, String>();

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
            kvs.open();
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
        (new Thread() {
            @Override
            public void run() {
                commit();
            }
        }).start();
    }

    /**
     * データをDBからロードする
     * <p/>
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
        new Thread() {
            @Override
            public void run() {
                commitAndLoad();
            }
        }.start();
    }

}
