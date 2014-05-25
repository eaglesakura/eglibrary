package com.eaglesakura.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import android.graphics.Bitmap;

public class Util {

    public static final String SHIT_JIS = "Shift_JIS";

    /**
     * 単純にsleepさせる。
     * 
     * @param timems
     */
    public static void sleep(long timems) {
        try {
            Thread.sleep(timems);
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    /**
     * InputStreamを全てメモリ上に展開する。 isの長さがOOMにならないように調整すること。
     * 
     * @param is
     * @return
     * @throws IOException
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
     * フラグ情報を設定する。
     * 
     * 
     * @param flg
     * @param check
     * @param is
     *            ビットを立てる場合はtrue、下げる場合はfalse
     * @return
     */
    public static final int setFlag(int flg, int check, boolean is) {
        if (is)
            return flg | check;
        else
            return flg & (~check);
    }

    /**
     * byte配列からMD5を求める
     * 
     * @param buffer
     * @return
     */
    public static String genMD5(byte[] buffer) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer);
            byte[] digest = md.digest();

            StringBuffer sBuffer = new StringBuffer(digest.length * 2);
            for (byte b : digest) {
                String s = Integer.toHexString(((int) b) & 0xff);

                if (s.length() == 1) {
                    sBuffer.append('0');
                }
                sBuffer.append(s);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * byte配列からMD5を求める
     * 
     * @param buffer
     * @return
     */
    public static String genSHA1(byte[] buffer) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(buffer);
            byte[] digest = md.digest();

            StringBuffer sBuffer = new StringBuffer(digest.length * 2);
            for (byte b : digest) {
                String s = Integer.toHexString(((int) b) & 0xff);

                if (s.length() == 1) {
                    sBuffer.append('0');
                }
                sBuffer.append(s);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 全角英数を半角英数に変換する
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
     * 係数ブレンドを行い、結果を返す。
     * 1.0に近いほどaに近い値となる。
     * blend == 1 -> a
     * blend == 0 -> b
     * @param a
     * @param b
     * @param blend aのブレンド値
     * @return
     */
    public static float blendValue(float a, float b, float blend) {
        return a * blend + b * (1.0f - blend);
    }

    /**
     * 日本語を意識してJavaの辞書順に並び替える
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

    /**
     * 目標数値へ移動する。
     *
     * 
     * @param now
     * @param offset
     * @param target
     * @return
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
     *
     * 
     * @param now
     * @param offset
     * @param target
     * @return
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
     * @param array
     * @return
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
     * 画像からSHA1指紋を作成する。
     * @param bitmap
     * @return
     */
    public static String genSHA1(Bitmap bitmap) {
        final int[] pixels = new int[bitmap.getWidth()];
        final byte[] src = new byte[pixels.length * 4];
        final int height = bitmap.getHeight();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");

            for (int i = 0; i < height; ++i) {
                bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, i, bitmap.getWidth(), 1);
                md.update(Util.toByteArray(pixels, src));

            }

            byte[] digest = md.digest();

            StringBuffer sBuffer = new StringBuffer(digest.length * 2);
            for (byte b : digest) {
                String s = Integer.toHexString(((int) b) & 0xff);

                if (s.length() == 1) {
                    sBuffer.append('0');
                }
                sBuffer.append(s);
            }
            return sBuffer.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
