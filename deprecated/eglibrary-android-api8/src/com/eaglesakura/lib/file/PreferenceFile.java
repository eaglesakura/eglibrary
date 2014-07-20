package com.eaglesakura.lib.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 下記のような形式で書かれているkey-valueな設定ファイルを読み込む。
 * Key=Value
 * Key=Value
 * ...
 * @author TAKESHI YAMASHITA
 *
 */
public class PreferenceFile {

    Map<String, String> keyValue = new HashMap<String, String>();

    void parse(String line) {
        if (line.startsWith("#")) {
            return;
        }
        line.replaceAll(" ", "");

        int eq = line.indexOf('=');
        if (eq < 0) {
            return;
        }

        String key = line.substring(0, eq);
        String value = line.substring(eq + 1);
        System.out.println(key + " = " + value);

        keyValue.put(key, value);
    }

    public PreferenceFile(File file) {
        try {
            FileInputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = reader.readLine()) != null) {
                try {
                    parse(line);
                } catch (Exception e) {

                }
            }

            is.close();
        } catch (Exception e) {

        }
    }

    /**
     * 文字列を読み込む
     * @param key
     * @param def
     * @return
     */
    public String getString(String key, String def) {
        String result = keyValue.get(key);
        if (result == null) {
            return def;
        }
        return result;
    }

    /**
     * 数値を読み込む
     * @param key
     * @param def
     * @return
     */
    public int getInteger(String key, int def) {
        try {
            return Integer.parseInt(keyValue.get(key));
        } catch (Exception e) {
            return def;
        }
    }
}
