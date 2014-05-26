package com.eaglesakura.util;

import android.util.Log;

/**
 * ログ出力を制御する。
 * 
 * @author TAKESHI YAMASHITA
 * 
 */
public class LogUtil {
    static String tag = "eglib";
    static boolean output = false;
    static Logger logger = null;

    /**
     * Androidのpackageが存在したら、Android用ロガーを利用する
     */
    static {
        initLogger();
    }

    static void initLogger() {
        if (logger == null) {
            try {
                Class.forName("android.util.Log");
                logger = new AndroidLogger();
            } catch (Exception e) {
                logger = new BasicLogger();
            }
        }
        if (tag == null) {
            tag = "lib";
        }
    }

    public interface Logger {
        public void i(String msg);
    }

    static class AndroidLogger implements Logger {
        @Override
        public void i(String msg) {
            Log.d(tag, msg);
        }
    }

    static class BasicLogger implements Logger {
        @Override
        public void i(String msg) {
            System.out.println(msg);
        }
    }

    /**
     * ログ出力時に使用するタグを設定する。
     * 
     * @param tag
     */
    public static void setTag(String tag) {
        LogUtil.tag = tag;
    }

    /**
     * ロガーを設定する。
     * @param logger
     */
    public static void setLogger(Logger logger) {
        LogUtil.logger = logger;
    }

    /**
     * 実際に出力する場合はtrueを設定する
     * @param output
     */
    public static void setOutput(boolean output) {
        LogUtil.output = output;
    }

    /**
     * ログ出力を行う。
     * 
     * @param message
     */
    public static void log(String message) {
        if (output) {
            initLogger();
            logger.i("" + message);
        }
    }

    /**
     * デバッグログ出力を行う
     * @param message
     */
    public static void d(String message) {
        if (output) {
            initLogger();
            logger.i("" + message);
        }
    }

    public static void log(Exception e) {
        if (output) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }
}
