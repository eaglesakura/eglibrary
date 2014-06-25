package com.eaglesakura.util;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringUtil {

    public static final String SHIT_JIS = "Shift_JIS";

    /**
     * 文字列がnullか空文字だったらtrueを返す。
     *
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }

        return str.length() == 0;
    }

    /**
     * strがnullかemptyだったらnullを返す。
     *
     * @param str
     * @return
     */
    public static String emptyToNull(String str) {
        return isEmpty(str) ? null : str;
    }

    /**
     * 全角英数を半角英数に変換する
     *
     * @param s
     * @return
     */
    public static String zenkakuEngToHankakuEng(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c >= 'Ａ' && c <= 'Ｚ') {
                c = (char) (c - 'Ａ' + 'A');
            } else if (c >= '０' && c <= '９') {
                c = (char) (c - '０' + '0');
            } else {
                switch (c) {
                    case '＜':
                        c = '<';
                        break;
                    case '＞':
                        c = '>';
                        break;
                    case '　':
                        c = ' ';
                        break;
                    case '／':
                        c = '/';
                        break;
                    case '！':
                        c = '!';
                        break;
                    case '？':
                        c = '?';
                        break;
                    case '．':
                        c = '.';
                        break;
                }
            }

            sb.setCharAt(i, c);
        }

        return sb.toString();
    }

    /**
     * 全角文字を半角文字に変更する
     *
     * @param s
     * @return
     */
    public static String zenkakuHiraganaToZenkakuKatakana(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c >= 'ァ' && c <= 'ン') {
                sb.setCharAt(i, (char) (c - 'ァ' + 'ぁ'));
            } else if (c == 'ヵ') {
                sb.setCharAt(i, 'か');
            } else if (c == 'ヶ') {
                sb.setCharAt(i, 'け');
            } else if (c == 'ヴ') {
                sb.setCharAt(i, 'う');
                sb.insert(i + 1, '゛');
                i++;
            }
        }

        return sb.toString();
    }

    /**
     * @param str
     * @return
     */
    public static String macStringToWinString(String str) {
        final int indexOffsetDakuten = ('が' - 'か');
        final int indexOffsetHandakuten = ('ぱ' - 'は');
        final int dakuten = '゙';
        final int handakuten = '゚';

        StringBuffer sb = new StringBuffer(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (i < (str.length() - 1)) {
                char cNext = str.charAt(i + 1);
                if (cNext == dakuten) {
                    // 特殊な濁点補正
                    switch (c) {
                        case 'う':
                            c = 'ゔ';
                            break;
                        case 'ウ':
                            c = 'ヴ';
                            break;
                        default:
                            c += indexOffsetDakuten;
                            break;
                    }
                } else if (cNext == handakuten) {
                    c += indexOffsetHandakuten;
                }
            }

            if (c != dakuten && c != handakuten) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 日本語を意識してJavaの辞書順に並び替える
     *
     * @param a
     * @param b
     * @return
     */
    public static int compareString(String a, String b) {
        a = zenkakuHiraganaToZenkakuKatakana(a.toLowerCase());
        a = zenkakuEngToHankakuEng(a);
        b = zenkakuHiraganaToZenkakuKatakana(b.toLowerCase());
        b = zenkakuEngToHankakuEng(b);

        return a.compareTo(b);
    }

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-hh:mm:ss.SS");

    /**
     * 指定時刻を文字列に変換する
     * 内容はyyyyMMdd-hh:mm:ss.SSとなる。
     *
     * @param date
     * @return
     */
    public static String toString(Date date) {
        return formatter.format(date);
    }

    /**
     * yyyyMMdd-hh:mm:ss.SSフォーマットの文字列をDateに変換する
     *
     * @param date
     * @return
     */
    public static Date toDate(String date) {
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * base64 encode/decode
     */
    private static Class base64Class = null;

    /**
     * base64 encode
     */
    private static Method base64Encode = null;

    /**
     * base64 decode
     */
    private static Method base64Decode = null;

    private synchronized static void initializeBase64Method() {
        if (base64Class != null) {
            return;
        }

        try {
            // for Android
            base64Class = Class.forName("android.util.Base64");
            base64Encode = base64Class.getMethod("encodeToString", byte[].class, int.class);
            base64Decode = base64Class.getMethod("decode", byte[].class, int.class);
        } catch (Exception e) {
            LogUtil.log(e);
        }

        if (base64Class != null) {
            return;
        }


        if (base64Class == null) {
            try {
                // for Commons Codec Java
                base64Class = Class.forName("org.apache.commons.codec.binary.Base64");
                base64Encode = base64Class.getMethod("encodeBase64", byte[].class);
                base64Decode = base64Class.getMethod("decodeBase64", byte[].class);
            } catch (Exception e) {

            }
        }
    }


    /**
     * base64エンコードする
     *
     * @param buffer
     * @return
     */
    public static String toString(byte[] buffer) {
        initializeBase64Method();

        try {
            return (String) base64Encode.invoke(base64Class, buffer, 0 /* Base64.Default */);
        } catch (Exception e) {
            LogUtil.log(e);
            return null;
        }
    }

    /**
     * base64文字列をバイト配列へ変換する
     *
     * @param base64
     * @return
     */
    public static byte[] toByteArray(String base64) {
        initializeBase64Method();

        try {
            return (byte[]) base64Decode.invoke(base64Class, base64, 0 /* Base64.Default */);
        } catch (Exception e) {
            LogUtil.log(e);
            return null;
        }
    }
}
