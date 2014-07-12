package com.eaglesakura.util;

import java.lang.reflect.Method;

/**
 * ログ出力を制御する。
 *
 * @author TAKESHI YAMASHITA
 */
public final class LogUtil {
    static String tag = "eglib";
    static boolean output = true;
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
                logger = new AndroidLogger(Class.forName("android.util.Log"));
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

        public void d(String msg);
    }

    /**
     * Android用Logger
     */
    static class AndroidLogger implements Logger {
        Class<?> clazz;
        Method i;
        Method d;

        AndroidLogger(Class<?> logClass) {
            this.clazz = logClass;
            try {
                this.i = clazz.getMethod("i", String.class, String.class);
                this.d = clazz.getMethod("d", String.class, String.class);
            } catch (Exception e) {

            }
        }

        @Override
        public void i(String msg) {
            try {
                i.invoke(clazz, tag, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void d(String msg) {
            try {
                d.invoke(clazz, tag, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * その他のシステム用標準Logger
     */
    static class BasicLogger implements Logger {
        @Override
        public void i(String msg) {
            System.out.println(msg);
        }

        @Override
        public void d(String msg) {
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
     *
     * @param logger
     */
    public static void setLogger(Logger logger) {
        LogUtil.logger = logger;
    }

    /**
     * 実際に出力する場合はtrueを設定する
     *
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
     *
     * @param message
     */
    public static void d(String message) {
        if (output) {
            initLogger();
            logger.d("" + message);
        }
    }

    public static void d(Exception e) {
        if (output) {
            e.printStackTrace();
            d(e.getMessage());
        }
    }

    public static void log(Exception e) {
        if (output) {
            e.printStackTrace();
            log(e.getMessage());
        }
    }
}