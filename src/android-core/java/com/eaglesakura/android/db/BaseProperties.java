package com.eaglesakura.android.db;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.eaglesakura.util.EncodeUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.SerializeUtil;
import com.eaglesakura.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 簡易設定用のプロパティを保持するためのクラス
 */
public abstract class BaseProperties {
    /**
     * app context
     */
    protected Context context;

    /**
     * ロード済みのプロパティ
     */
    protected final Map<String, Property> propMap = new HashMap<String, Property>();

    public BaseProperties(Context context) {
        this.context = context;
    }

    /**
     * このコンストラクタは互換性のためにあるため、dbNameは無視される。
     *
     * @param context
     * @param dbName
     */
    public BaseProperties(Context context, String dbName) {
        this(context);
    }

    protected class Property {
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

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
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
     * このLoadメソッドはサブクラスとの互換性のためにあるため、このクラスでは何も行わない。
     */
    public void load() {
        // Stub!!
    }

//    /**
//     * Viewの値からpropertyを指定する
//     *
//     * @param key
//     * @param view
//     */
//    public void setPropertyFromView(String key, View view) {
//        String value = null;
//        if (view instanceof RadioGroup) {
//            RadioGroup group = (RadioGroup) view;
//            View selectedView = group.findViewById(group.getCheckedRadioButtonId());
//            // Tagを指定する
//            if (selectedView != null) {
//                value = selectedView.getTag().toString();
//            } else {
//                value = "";
//            }
//        } else if (view instanceof EditText) {
//            value = ((EditText) view).getText().toString();
//        } else if (view instanceof CompoundButton) {
//            value = String.valueOf(((CompoundButton) view).isChecked());
//        } else if (view instanceof TextView) {
//            value = ((TextView) view).getText().toString();
//        }
//
//        if (value != null) {
//            setProperty(key, (Object) value);
//        }
//    }

    /**
     * プロパティを保存する
     *
     * @param key   プロパティのキー値
     * @param value プロパティの値
     */
    public void setProperty(String key, Object value) {
        Property prop = propMap.get(key);

        if (value instanceof Enum) {
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

    public byte[] toByteArray() {
        Map<String, byte[]> datas = new HashMap<>();
        Iterator<Map.Entry<String, Property>> itr = propMap.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Property> entry = itr.next();
            Property prop = entry.getValue();
            if (!StringUtil.isEmpty(prop.value)) {
                datas.put(prop.key, prop.value.getBytes());
            }
        }
        return SerializeUtil.toByteArray(datas);
    }

    public void fromByteArray(byte[] buffer) throws IOException {
        try {
            Map<String, byte[]> datas = SerializeUtil.toKeyValue(buffer);

            Iterator<Map.Entry<String, Property>> itr = propMap.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, Property> entry = itr.next();
                Property prop = entry.getValue();

                byte[] value = datas.get(prop.key);
                if (value != null) {
                    // 値が書き込まれていた場合はそちらを優先
                    prop.value = new String(value);
                } else {
                    // 値が書き込まれていないので、デフォルトを復元
                    prop.value = prop.defaultValue;
                }
            }
        } catch (Exception e) {
            LogUtil.log(e);
            throw new IOException("Format Error");
        }
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

    /**
     * 配列をシリアライズする
     *
     * @param datas
     * @param <T>
     * @return
     */
    public static <T extends BaseProperties> byte[] serialize(List<T> datas) {
        List<byte[]> serializeArray = new ArrayList<>();
        for (T item : datas) {
            serializeArray.add(item.toByteArray());
        }

        return EncodeUtil.compressOrRaw(SerializeUtil.toByteArray(serializeArray));
    }

    /**
     * Key-Valueマップをシリアライズする
     *
     * @param datas
     * @return
     */
    public static byte[] serialize(Map<String, BaseProperties> datas) {
        Map<String, byte[]> serializeMap = new HashMap<>();

        Iterator<Map.Entry<String, BaseProperties>> iterator = datas.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, BaseProperties> entry = iterator.next();
            serializeMap.put(entry.getKey(), entry.getValue().toByteArray());
        }
        return EncodeUtil.compressOrRaw(SerializeUtil.toByteArray(serializeMap));
    }

    /**
     * シリアライズされたバッファからクラスを復元する
     *
     * @param context
     * @param clazz
     * @param buffer
     * @param <T>
     * @return
     */
    public static <T extends BaseProperties> List<T> deserializeToArray(Context context, Class<T> clazz, byte[] buffer) {
        buffer = EncodeUtil.decompressOrRaw(buffer);

        List<byte[]> deserializeArray = SerializeUtil.toByteArrayList(buffer);
        List<T> result = new ArrayList<>();
        try {
            Constructor<T> constructor = clazz.getConstructor(Context.class, String.class);
            for (byte[] data : deserializeArray) {
                T instance = constructor.newInstance(context, null);
                instance.fromByteArray(data);
                result.add(instance);
            }

            return result;
        } catch (Exception e) {
            LogUtil.log(e);
            return null;
        }
    }

    /**
     * Key-Valueからデシリアライズする。
     * デシリアライズ先は不明であるため、byte[]までの解凍に留める。
     *
     * @param context
     * @param buffer
     * @return
     */
    public static Map<String, byte[]> deserializeToSerializedMap(Context context, byte[] buffer) {
        buffer = EncodeUtil.decompressOrRaw(buffer);
        return SerializeUtil.toKeyValue(buffer);
    }

    /**
     * 生成されたbyte[]からインスタンスを復元する
     *
     * @param context
     * @param clazz
     * @param data
     * @param <T>
     * @return
     */
    public static <T extends BaseProperties> T deserializeInstance(Context context, Class<T> clazz, byte[] data) {
        try {
            Constructor<T> constructor = clazz.getConstructor(Context.class, String.class);
            T instance = constructor.newInstance(context, null);
            instance.fromByteArray(data);

            return instance;
        } catch (Exception e) {
            LogUtil.log(e);
            return null;
        }
    }

}
