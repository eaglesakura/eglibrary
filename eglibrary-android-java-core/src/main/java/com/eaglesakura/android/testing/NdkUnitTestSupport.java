package com.eaglesakura.android.testing;

import android.content.Context;

public class NdkUnitTestSupport {

    /**
     * gtest用のtestmainを実行する
     * <p/>
     * testmainは int (*mainfunc_ptr)(int, char**)である必要がある。
     *
     * @param moduleName
     * @param functionName
     * @param outputXmlPath
     * @return
     */
    public static int testmain(Context context, String moduleName, String functionName, String outputXmlPath) {
        // デバッガの接続待ちを行う
        android.os.Debug.waitForDebugger();

        String modulePath = String.format("/data/data/%s/lib/%s.so", context.getPackageName(), moduleName);

        return ndkTestMain(modulePath, functionName, outputXmlPath);
    }

    static native int ndkTestMain(String modulePath, String functionName, String outputXmlPath);
}
