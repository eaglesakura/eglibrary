package com.eaglesakura.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.ExifInterface;

import com.eaglesakura.android.graphics.Graphics;
import com.eaglesakura.io.IOUtil;
import com.eaglesakura.math.MathUtil;
import com.eaglesakura.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

public class ImageUtil {

    /**
     * ある特定の色の補色を計算する
     * <p/>
     * 計算はIllustrator方式で行う
     * <p/>
     * http://appakumaturi.hatenablog.com/entry/20120121/1327143125
     *
     * @param xrgb
     * @return
     */
    public static int getComplementaryColor(int xrgb) {
        int r = Color.red(xrgb);
        int g = Color.green(xrgb);
        int b = Color.blue(xrgb);

        int maxValue = Math.max(Math.max(r, g), b);
        int minValue = Math.min(Math.min(r, g), b);

        int sum = maxValue + minValue;

        return Color.rgb(MathUtil.minmax(0, 255, sum - r), MathUtil.minmax(0, 255, sum - g), MathUtil.minmax(0, 255, sum - b));
    }

    /**
     * 反転色を取得する
     *
     * @param xrgb
     * @return
     */
    public static int getNegaColor(int xrgb) {
        int r = Color.red(xrgb);
        int g = Color.green(xrgb);
        int b = Color.blue(xrgb);

        return Color.rgb(255 - r, 255 - g, 255 - b);
    }

    /**
     * 画像からSHA1指紋を作成する。
     *
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
                md.update(IOUtil.toByteArray(pixels, src));
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

    /**
     * Bitmapからサムネイルを読み込む
     *
     * @param pathName
     * @return
     */
    public static Bitmap decodeThumbnail(String pathName) {
        try {
            ExifInterface exif = new ExifInterface(pathName);
            return decode(exif.getThumbnail());
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return null;
    }

    /**
     * image bufferからデコードする
     *
     * @param imageFile
     * @return
     */
    public static Bitmap decode(byte[] imageFile) {
        if (imageFile == null) {
            return null;
        }

        try {
            return BitmapFactory.decodeByteArray(imageFile, 0, imageFile.length);
        } catch (Exception e) {
            LogUtil.log(e);

        }
        return null;
    }

    public static Bitmap decode(Context context, int drawableId) {
        try {
            return BitmapFactory.decodeResource(context.getResources(), drawableId);
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return null;
    }

    /**
     * 画像ファイルから直接デコードする
     *
     * @param pathName
     * @return
     */
    public static Bitmap decode(String pathName) {
        try {
            return BitmapFactory.decodeFile(pathName);
        } catch (Exception e) {
            LogUtil.log(e);
        }
        return null;
    }

    /**
     * PNG画像にエンコードする
     *
     * @param bitmap
     * @return
     */
    public static byte[] encodePng(Bitmap bitmap) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            bitmap.compress(CompressFormat.PNG, 100, os);
            return os.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Jpeg画像にエンコードする
     *
     * @param bitmap
     * @param quality
     * @return
     */
    public static byte[] encodeJpeg(Bitmap bitmap, int quality) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            bitmap.compress(CompressFormat.JPEG, quality, os);
            return os.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * アルファ値をブレンドした新たな画像を生成する
     *
     * @param src
     * @param alpha
     * @return
     */
    public static Bitmap blendAlpha(Bitmap src, Bitmap alpha) {
        // アルファ成分をコピーして作成
        Bitmap image = Bitmap.createScaledBitmap(alpha, src.getWidth(), src.getHeight(), true);
        Canvas canvas = new Canvas(image);

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);

        // 転送モードを工夫してから
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(src, 0, 0, paint);

        // 生成した画像を返す
        return image;
    }

    /**
     * 指定した最大サイズに収まるようにスケーリングする
     * <p/>
     * 元のsrcオブジェクトをそのまま返す場合があるので注意。
     *
     * @param src
     * @param maxWidth
     * @param maxHeight
     * @return
     */
    public static Bitmap toScaledImage(Bitmap src, int maxWidth, int maxHeight) {
        int originWidth = src.getWidth();
        int originHeight = src.getHeight();
        if (originWidth <= maxWidth && originHeight <= maxHeight) {
            return src;
        }

        double scaleWidth = (double) maxWidth / (double) originWidth;
        double scaleHeight = (double) maxHeight / (double) originHeight;

        int newWidth;
        int newHeight;
        if (scaleWidth < scaleHeight) {
            // 幅に合わせる
            newWidth = maxWidth;
            newHeight = (int) (scaleWidth * originHeight);
        } else {
            // 高さに合わせる
            newWidth = (int) (scaleHeight * originWidth);
            newHeight = maxHeight;
        }

        // スケーリングしたBitmapを返す
        return Bitmap.createScaledBitmap(src, newWidth, newHeight, true);
    }

    /**
     * 正方形画像へ変形する。
     * <p/>
     * 強制的に変形を行うため、srcは破棄して問題ない。
     *
     * @param src  元画像
     * @param size 出力サイズ
     * @return
     */
    public static Bitmap toSquareImage(Bitmap src, int size) {
        if (src.getWidth() == src.getHeight()) {
            if (src.getWidth() == size) {
                // 既に同じサイズであるため、コピーのみでOK
                return Bitmap.createBitmap(src);
            } else {
                // 既に正方形である場合、縮小のみで行える
                return Bitmap.createScaledBitmap(src, size, size, true);
            }
        } else {
            double scale;
            // 短編に合わせてスケーリングし、長辺ははみ出す
            if (src.getWidth() < src.getHeight()) {
                scale = (double) size / (double) src.getWidth();
            } else {
                scale = (double) size / (double) src.getHeight();
            }

            int srcWidth = (int) (src.getWidth() * scale + 0.9);
            int srcHeight = (int) (src.getHeight() * scale + 0.9);

            Bitmap dst = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(dst);
            Graphics graphics = new Graphics(canvas);
            graphics.setAntiAlias(true);
            graphics.drawBitmap(src, (size - srcWidth) / 2, (size - srcHeight) / 2, srcWidth, srcHeight);

            return dst;
        }
    }
}
