package com.eaglesakura.android.camera;

import android.content.Context;

import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.jc.annotation.JCClass;
import com.eaglesakura.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ホワイトバランス設定
 */
@JCClass(cppNamespace = "es")
public final class WhiteBaranceSpec {
    private static final Map<String, WhiteBaranceSpec> gWhiteBaranceSpecMap;

    /**
     * 自動設定
     */
    public static final WhiteBaranceSpec SETTING_AUTO;
    /**
     * 白熱灯
     */
    public static final WhiteBaranceSpec SETTING_INCANDESCENT;
    /**
     * 蛍光灯
     */
    public static final WhiteBaranceSpec SETTING_FLUORESCENT;
    /**
     * 晴天
     */
    public static final WhiteBaranceSpec SETTING_DAYLIGHT;
    /**
     * 曇り
     */
    public static final WhiteBaranceSpec SETTING_CLOUDY_DAYLIGHT;

    static {
        gWhiteBaranceSpecMap = new HashMap<String, WhiteBaranceSpec>();

        SETTING_AUTO = fromName("auto");
        SETTING_INCANDESCENT = fromName("incandescent");
        SETTING_FLUORESCENT = fromName("fluorescent");
        SETTING_DAYLIGHT = fromName("daylight");
        SETTING_CLOUDY_DAYLIGHT = fromName("cloudy-daylight");
    }

    /**
     * API設定名
     */
    private final String apiSettingName;

    WhiteBaranceSpec(String apiSettingName) {
        this.apiSettingName = apiSettingName;
    }

    /**
     * API設定名を取得する
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
        String result = ContextUtil.getStringFromIdName(context, String.format("Camera.WhiteBarance.%s", apiSettingName.replaceAll("-", "_")));
        if (StringUtil.isEmpty(result)) {
            return apiSettingName;
        } else {
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WhiteBaranceSpec)) {
            return false;
        }

        return ((WhiteBaranceSpec) o).apiSettingName.equals(apiSettingName);
    }

    @Override
    public int hashCode() {
        return apiSettingName.hashCode();
    }

    /**
     * ホワイトバランス設定モードを取得する
     *
     * @param mode
     * @return
     */
    public static WhiteBaranceSpec fromName(String mode) {
        WhiteBaranceSpec result = gWhiteBaranceSpecMap.get(mode);
        if (result == null) {
            result = new WhiteBaranceSpec(mode);
            gWhiteBaranceSpecMap.put(mode, result);
        }
        return result;
    }

    /**
     * デバイス設定から取得する
     *
     * @param deviceSettings
     * @return ホワイトバランス設定
     */
    public static List<WhiteBaranceSpec> list(List<String> deviceSettings) {
        List<WhiteBaranceSpec> result = new ArrayList<WhiteBaranceSpec>();
        if (deviceSettings == null) {
            return result;
        }

        for (String mode : deviceSettings) {
            result.add(fromName(mode));
        }

        return result;
    }
}
