package com.eaglesakura.android.camera;

import android.content.Context;

import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ホワイトバランス設定
 */
public class FlashModeSpec {
    private static final Map<String, FlashModeSpec> gFlashSpecMap;

    /**
     * 自動設定
     */
    public static final FlashModeSpec SETTING_AUTO;
    /**
     * オフ
     */
    public static final FlashModeSpec SETTING_OFF;
    /**
     * オン
     */
    public static final FlashModeSpec SETTING_ON;
    /**
     * 赤目補正
     */
    public static final FlashModeSpec SETTING_RED_EYE;
    /**
     * 常時
     */
    public static final FlashModeSpec SETTING_TORCH;

    static {
        gFlashSpecMap = new HashMap<String, FlashModeSpec>();

        SETTING_AUTO = fromName("auto");
        SETTING_OFF = fromName("off");
        SETTING_ON = fromName("on");
        SETTING_RED_EYE = fromName("red-eye");
        SETTING_TORCH = fromName("torch");
    }

    /**
     * API設定名
     */
    private final String apiSettingName;

    FlashModeSpec(String apiSettingName) {
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
        String result = ContextUtil.getStringFromIdName(context, String.format("Camera.FlashMode.%s", apiSettingName.replaceAll("-", "_")));
        if (StringUtil.isEmpty(result)) {
            return apiSettingName;
        } else {
            return result;
        }
    }

    /**
     * フラッシュ設定モードを取得する
     *
     * @param mode
     * @return
     */
    public static FlashModeSpec fromName(String mode) {
        FlashModeSpec result = gFlashSpecMap.get(mode);
        if (result == null) {
            result = new FlashModeSpec(mode);
            gFlashSpecMap.put(mode, result);
        }
        return result;
    }

    /**
     * デバイス設定から取得する
     *
     * @param deviceSettings
     * @return ホワイトバランス設定
     */
    public static List<FlashModeSpec> list(List<String> deviceSettings) {
        List<FlashModeSpec> result = new ArrayList<FlashModeSpec>();
        if (deviceSettings == null) {
            return result;
        }

        for (String mode : deviceSettings) {
            result.add(fromName(mode));
        }

        return result;
    }
}
