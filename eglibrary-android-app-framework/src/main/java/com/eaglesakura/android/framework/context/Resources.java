package com.eaglesakura.android.framework.context;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.io.IOUtil;
import com.eaglesakura.util.LogUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * APKが持つリソースを管理する
 */
public class Resources {
    public static android.content.res.Resources app() {
        return FrameworkCentral.getApplication().getResources();
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
}
