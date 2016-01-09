package com.eaglesakura.android.graphics;

import android.graphics.Bitmap;

import com.eaglesakura.util.LogUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class DecodedImage {
    /**
     * 幅
     */
    int width = 0;

    /**
     * 高さ
     */
    int height = 0;

    /**
     * ピクセルデータ
     */
    Buffer pixels;

    private DecodedImage() {
    }

    /**
     * 画像幅を取得する
     *
     * @return
     */
    public int getWidth() {
        return width;
    }

    /**
     * 画像高を取得する
     *
     * @return
     */
    public int getHeight() {
        return height;
    }

    /**
     * ピクセルバッファを取得する
     *
     * @return
     */
    public Buffer getPixels() {
        return pixels;
    }

    /**
     * 画像からデコードを行う
     * 画像は自動ではrecycleされないため、呼び出し元で行うこと。
     *
     * @param image
     * @return
     */
    public static DecodedImage decodeFromBitmap(Bitmap image, boolean recycle) {
        if (image == null) {
            return null;
        }

        final int image_width = image.getWidth();
        final int image_height = image.getHeight();
        DecodedImage result = new DecodedImage();

        // ピクセル情報の格納先を確保
        IntBuffer pixelBuffer = ByteBuffer.allocateDirect(image_width * image_height * 4)
                .order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        {
            result.width = image_width;
            result.height = image_height;
            result.pixels = pixelBuffer;
        }

        LogUtil.log("image size(%d x %d)", image_width, image_height);

        final int ONCE_READ_LINE = 128;
        final int[] temp = new int[image_width * ONCE_READ_LINE];

        int readHeight = image_height;
        while (readHeight > 0) {
            // 1ラインずつ読み込む
            final int readLine = Math.min(readHeight, ONCE_READ_LINE);
            image.getPixels(temp, 0, image_width, 0, image_height - readHeight, image_width, readLine);
            pixelBuffer.put(temp, 0, image_width * readLine);

            readHeight -= readLine;
        }

        // 書き込み位置をリセットする
        pixelBuffer.position(0);

        if (recycle) {
            image.recycle();
        }
        return result;
    }
}
