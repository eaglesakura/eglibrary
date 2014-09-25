package com.eaglesakura.android.camera;

import android.content.Context;

import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * シーン情報
 */
public class SceneSpec {
    /**
     * API設定名
     */
    private final String apiSettingName;


    private static final Map<String, SceneSpec> gSceneSpecMap;

    /**
     * 自動設定
     */
    public final static SceneSpec SETTING_AUTO;

    /**
     * 人物撮影
     * ソフトスナップ by XperiaGX
     */
    public final static SceneSpec SETTING_PORTRAIT;

    /**
     * 風景
     */
    public final static SceneSpec SETTING_LANDSCAPE;

    /**
     * 夜景
     */
    public final static SceneSpec SETTING_NIGHT;

    /**
     * 夜景人物
     * 夜景＆人物 by XperiaGX
     */
    public final static SceneSpec SETTING_NIGHT_PORTRAIT;

    /**
     * ビーチ
     * ビーチ & スノー by XperiaGX
     */
    public final static SceneSpec SETTING_BEACH;

    /**
     * 雪景色
     * ビーチ & スノー by XperiaGX
     */
    public final static SceneSpec SETTING_SNOW;

    /**
     * スポーツ
     */
    public final static SceneSpec SETTING_SPORTS;

    /**
     * パーティ
     */
    public final static SceneSpec SETTING_PARTY;

    /**
     * 二値化/文字認識
     */
    public final static SceneSpec SETTING_DOCUMENT;

    static {
        gSceneSpecMap = new HashMap<String, SceneSpec>();
        SETTING_AUTO = fromName("auto");
        SETTING_PORTRAIT = fromName("portrait");
        SETTING_LANDSCAPE = fromName("landscape");
        SETTING_NIGHT = fromName("night");
        SETTING_NIGHT_PORTRAIT = fromName("night-portrait");
        SETTING_BEACH = fromName("beach");
        SETTING_SNOW = fromName("snow");
        SETTING_SPORTS = fromName("sports");
        SETTING_PARTY = fromName("party");
        SETTING_DOCUMENT = fromName("document");
    }

    SceneSpec(String apiSettingName) {
        this.apiSettingName = apiSettingName;
    }

    /**
     * API設定名を取得
     *
     * @return
     */
    public String getApiSettingName() {
        return apiSettingName;
    }

    /**
     * 設定名を取得する
     *
     * @param context
     * @return 日本語での設定名
     */
    public String name(Context context) {
        String result = ContextUtil.getStringFromIdName(context, String.format("Camera.Scene.%s", apiSettingName.replaceAll("-", "_")));
        if (StringUtil.isEmpty(result)) {
            return apiSettingName;
        } else {
            return result;
        }
    }

    /**
     * シーンを取得する
     *
     * @param mode
     * @return
     */
    public static SceneSpec fromName(String mode) {
        SceneSpec result = gSceneSpecMap.get(mode);
        if (result == null) {
            result = new SceneSpec(mode);
            gSceneSpecMap.put(mode, result);
        }
        return result;
    }

    /**
     * デバイス設定から取得する
     *
     * @param deviceSettings
     * @return シーン設定
     */
    public static List<SceneSpec> list(List<String> deviceSettings) {
        List<SceneSpec> result = new ArrayList<SceneSpec>();
        if (deviceSettings == null) {
            return result;
        }

        for (String mode : deviceSettings) {
            result.add(fromName(mode));
        }

        return result;
    }
}
