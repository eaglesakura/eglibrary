package com.eaglesakura.android.camera;

import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.StringUtil;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * フォーカス状態の設定を行う
 */
public class FocusModeSpec {

    private static final Map<String, FocusModeSpec> gFocusModeSpecMap;

    /**
     * 自動設定
     */
    public static final FocusModeSpec SETTING_AUTO;
    /**
     * 無限遠
     */
    public static final FocusModeSpec SETTING_INFINITY;
    /**
     * マクロ
     */
    public static final FocusModeSpec SETTING_MACRO;
    /**
     * 写真自動
     */
    public static final FocusModeSpec SETTING_CONTINUOUS_PICTURE;
    /**
     * ビデオ自動
     */
    public static final FocusModeSpec SETTING_CONTINUOUS_VIDEO;

    static {
        gFocusModeSpecMap = new HashMap<String, FocusModeSpec>();

        SETTING_AUTO = fromName("auto");
        SETTING_INFINITY = fromName("infinity");
        SETTING_MACRO = fromName("macro");
        SETTING_CONTINUOUS_PICTURE = fromName("continuous-picture");
        SETTING_CONTINUOUS_VIDEO = fromName("continuous-video");
    }

    /**
     * API設定名
     */
    private final String apiSettingName;

    FocusModeSpec(String apiSettingName) {
        this.apiSettingName = apiSettingName;
    }

    public String getApiSettingName() {
        return apiSettingName;
    }

    /**
     * 設定名を取得する
     *
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

    /**
     * フォーカス設定モードを取得する
     */
    public static FocusModeSpec fromName(String mode) {
        FocusModeSpec result = gFocusModeSpecMap.get(mode);
        if (result == null) {
            result = new FocusModeSpec(mode);
            gFocusModeSpecMap.put(mode, result);
        }
        return result;
    }

    /**
     * デバイス設定から取得する
     *
     * @return フォーカス設定
     */
    public static List<FocusModeSpec> list(List<String> deviceSettings) {
        List<FocusModeSpec> result = new ArrayList<FocusModeSpec>();
        if (deviceSettings == null) {
            return result;
        }

        for (String mode : deviceSettings) {
            result.add(fromName(mode));
        }

        return result;
    }
}
