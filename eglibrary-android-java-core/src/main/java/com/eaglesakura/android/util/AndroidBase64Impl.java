package com.eaglesakura.android.util;

import android.util.Base64;

import com.eaglesakura.util.StringUtil;

public class AndroidBase64Impl implements StringUtil.Base64Converter {
    private static final int FLAGS = Base64.NO_CLOSE | Base64.NO_PADDING | Base64.NO_WRAP | Base64.URL_SAFE;

    @Override
    public String encode(byte[] buffer) {
        return Base64.encodeToString(buffer, FLAGS);
    }

    @Override
    public byte[] decode(String base64) {
        return Base64.decode(base64, FLAGS);
    }
}
