package com.eaglesakura.lib.android.game.util;

import com.eaglesakura.lib.android.game.math.Vector2;

import android.graphics.RectF;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GameUtil {

    public static final String SHIT_JIS = "Shift_JIS";

    /**
     * 単純にsleepさせる。
     */
    public static void sleep(long timems) {
        try {
            Thread.sleep(timems);
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    /**
     * centerから見たpositionが何度になるか360度系で返す。
     * １２時の方向が0度で、反時計回りに角度を進ませる。
     */
    public static float getAngleDegree2D(Vector2 center, Vector2 position) {
        float result = 0;

        Vector2 temp = new Vector2(position.x - center.x, center.y - position.y);
        if (temp.length() == 0) {
            return 0;
        }
        temp.normalize();

        result = (float) (Math.atan2(temp.y, temp.x) / Math.PI);
        result /= 2;
        result -= 0.25f;

        return normalizeDegree(result * 360.0f);
    }

    /**
     * UIスレッドだったらtrueを返す。
     */
    public static boolean isUIThread() {
        return Thread.currentThread().equals(Looper.getMainLooper().getThread());
    }

    /**
     * UIスレッドでなければ例外を投げる。
     */
    public static void assertUIThread() {
        if (!isUIThread()) {
            throw new IllegalStateException("is not ui thread!!");
        }
    }

    /**
     * InputStreamを全てメモリ上に展開する。 isの長さがOOMにならないように調整すること。
     */
    public static byte[] toByteArray(InputStream is) throws IOException {
        byte[] result = null;

        //! 1kbずつ読み込む。
        byte[] tempBuffer = new byte[1024 * 5];
        //! 元ストリームを読み取り
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int n = 0;
            while ((n = is.read(tempBuffer)) > 0) {
                baos.write(tempBuffer, 0, n);
            }
            result = baos.toByteArray();
            is.close();
        }

        return result;
    }

    /**
     * InputStreamを全てメモリ上に展開する。 isの長さがOOMにならないように調整すること。
     */
    public static byte[] toByteArray(InputStream is, boolean close) throws IOException {
        byte[] result = null;

        //! 1kbずつ読み込む。
        byte[] tempBuffer = new byte[1024 * 5];
        //! 元ストリームを読み取り
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int n = 0;
            while ((n = is.read(tempBuffer)) > 0) {
                baos.write(tempBuffer, 0, n);
            }
            result = baos.toByteArray();
            if (close) {
                is.close();
            }
        }

        return result;
    }

    /**
     * inputのバッファを全てoutputへコピーする。 完了した時点でストリームはcloseされる。
     */
    public static void copyTo(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024 * 128];
        int length = 0;

        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        input.close();
        output.close();
    }

    /**
     * inputのバッファを全てoutputへコピーする。
     * close=trueの場合、完了した時点でストリームはcloseされる。
     */
    public static void copyTo(InputStream input, OutputStream output, boolean close) throws IOException {
        byte[] buffer = new byte[1024 * 128];
        int length = 0;

        while ((length = input.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }

        if (close) {
            input.close();
            output.close();
        }
    }

    /**
     * RectFを四捨五入で丸める。
     */
    public static void round(RectF rect) {
        rect.left = Math.round(rect.left);
        rect.right = Math.round(rect.right);
        rect.top = Math.round(rect.top);
        rect.bottom = Math.round(rect.bottom);
    }

    /**
     * min <= result <= maxとなるようにnowを補正する。
     */
    public static final int minmax(int min, int max, int now) {
        if (now < min)
            return min;
        if (now > max)
            return max;
        return now;
    }

    /**
     * min <= result <= maxとなるようにnowを補正する。
     */
    public static final float minmax(float min, float max, float now) {
        if (now < min)
            return min;
        if (now > max)
            return max;
        return now;
    }

    /**
     * 360度系の正規化を行う。
     */
    public static final float normalizeDegree(float now) {
        while (now < 0.0f) {
            now += 360.0f;
        }

        while (now >= 360.0f) {
            now -= 360.0f;
        }

        return now;
    }

    /**
     * 特定のビットフラグが立っていることを検証する。
     */
    public static final boolean isFlagOn(int flg, int check) {
        return (flg & check) != 0;
    }

    /**
     * 特定のビットフラグがすべて立っていることを検証する。
     */
    public static final boolean isFlagOnAll(int flg, int check) {
        return (flg & check) == 0;
    }

    /**
     * フラグ情報を設定する。
     *
     * @param is ビットを立てる場合はtrue、下げる場合はfalse
     */
    public static final int setFlag(int flg, int check, boolean is) {
        if (is)
            return flg | check;
        else
            return flg & (~check);
    }

    /**
     * 全角英数を半角英数に変換する
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
     * 係数ブレンドを行い、結果を返す。
     * 1.0に近いほどaに近い値となる。
     * blend == 1 -> a
     * blend == 0 -> b
     *
     * @param blend aのブレンド値
     */
    public static float blendValue(float a, float b, float blend) {
        return a * blend + b * (1.0f - blend);
    }

    /**
     *
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
     */
    public static int compareString(String a, String b) {
        a = zenkakuHiraganaToZenkakuKatakana(a.toLowerCase());
        a = zenkakuEngToHankakuEng(a);
        b = zenkakuHiraganaToZenkakuKatakana(b.toLowerCase());
        b = zenkakuEngToHankakuEng(b);

        return a.compareTo(b);
    }

    /**
     * 目標数値へ移動する。
     */
    public static final int targetMove(int now, int offset, int target) {
        offset = Math.abs(offset);
        if (Math.abs(target - now) <= offset) {
            return target;
        } else if (target > now) {
            return now + offset;
        } else {
            return now - offset;
        }
    }

    /**
     * 目標数値へ移動する。
     */
    public static final float targetMove(float now, float offset, float target) {
        offset = Math.abs(offset);
        if (Math.abs(target - now) <= offset) {
            return target;
        } else if (target > now) {
            return now + offset;
        } else {
            return now - offset;
        }
    }

    /**
     * Byte配列に変換する。
     */
    public static final byte[] toByteArray(int[] array) {
        byte[] result = new byte[array.length * 4];
        return toByteArray(array, result);
    }

    public static final byte[] toByteArray(int[] array, byte[] result) {
        for (int i = 0; i < array.length; ++i) {
            result[i * 4 + 0] = (byte) ((array[i] >> 24) & 0xff);
            result[i * 4 + 1] = (byte) ((array[i] >> 16) & 0xff);
            result[i * 4 + 2] = (byte) ((array[i] >> 8) & 0xff);
            result[i * 4 + 3] = (byte) ((array[i] >> 0) & 0xff);
        }
        return result;
    }

    /**
     * 文字列がnullか空文字だったらtrueを返す。
     */
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }

        return str.length() == 0;
    }

    /**
     * strがnullかemptyだったらnullを返す。
     */
    public static String emptyToNull(String str) {
        return isEmpty(str) ? null : str;
    }
}
