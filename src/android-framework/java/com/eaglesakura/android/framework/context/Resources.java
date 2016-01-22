package com.eaglesakura.android.framework.context;

import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.IOUtil;
import com.eaglesakura.util.LogUtil;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;

/**
 * APKが持つリソースを管理する
 */
public class Resources {
    private static int[] displaySize = null;

    private static android.content.res.Resources app() {
        return FrameworkCentral.getApplication().getResources();
    }

    /**
     * 画面サイズを取得する
     */
    public static int[] displaySize() {
//        if (displaySize == null) {
//        }
        displaySize = ContextUtil.getDisplaySize(FrameworkCentral.getApplication());
        return displaySize;
    }

    public static int displayWidth() {
        return displaySize()[0];
    }

    public static int displayHeight() {
        return displaySize()[1];
    }

    public static AssetManager assets() {
        return app().getAssets();
    }

    public static InputStream assets(String assetsPath) throws IOException {
        return assets().open(assetsPath);
    }

    public static byte[] assetsAsByteArray(String assetsPath) throws IOException {
        return IOUtil.toByteArray(assets(assetsPath), true);
    }

    public static String assetsAsString(String assetsPath) throws IOException {
        return new String(assetsAsByteArray(assetsPath));
    }

    public static byte[] assetsAsByteArrayOrNull(String assetsPath) {
        try {
            return assetsAsByteArray(assetsPath);
        } catch (Exception e) {
            LogUtil.log(e);
            return null;
        }
    }

    public static String assetsAsString(String assetsPath, String defaultValue) {
        try {
            return assetsAsString(assetsPath);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int argb(int colorId) {
        return app().getColor(colorId);
    }

    public static int integer(int integerId) {
        return app().getInteger(integerId);
    }

    public static double dimen(int dimenId) {
        return app().getDimension(dimenId);
    }

    public static String string(int stringId) {
        return app().getString(stringId);
    }

    public static Drawable drawable(int drawableId) {
        return app().getDrawable(drawableId);
    }

    public static byte[] raw(int rawId) {
        try {
            return IOUtil.toByteArray(app().openRawResource(rawId), true);
        } catch (Exception e) {
            return null;
        }
    }
}
