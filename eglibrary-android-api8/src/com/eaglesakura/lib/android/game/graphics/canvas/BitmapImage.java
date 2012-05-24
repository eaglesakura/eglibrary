package com.eaglesakura.lib.android.game.graphics.canvas;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;

import com.eaglesakura.lib.android.game.graphics.ImageBase;
import com.eaglesakura.lib.android.game.io.WebInputStream;
import com.eaglesakura.lib.android.game.resource.GarbageCollector;
import com.eaglesakura.lib.android.game.resource.IRawResource;
import com.eaglesakura.lib.android.game.resource.SharedRawResource;

/**
 * 
 * @author TAKESHI YAMASHITA
 *
 */
public class BitmapImage extends ImageBase {

    SharedRawResource sharedResource = null;
    BitmapResource bitmapResource = null;

    public BitmapImage(GarbageCollector garbageCollector) {
        super(garbageCollector);
    }

    public BitmapImage() {
        this((GarbageCollector) null);
    }

    public BitmapImage(BitmapImage origin) {
        this(origin.getGarbageCollector());
        bitmapResource = origin.bitmapResource;
        sharedResource = origin.sharedResource;
        sharedResource.addRef();
    }

    protected void onLoad(Bitmap image) {
        {
            if (sharedResource != null) {
                sharedResource.dispose();
            }
            sharedResource = null;
            bitmapResource = null;
        }

        if (image == null) {
            throw new IllegalArgumentException("Bitmap File load Error!!");
        }

        bitmapResource = new BitmapResource(image);
        sharedResource = new SharedRawResource(bitmapResource);
        sharedResource.addRef();
        register();
    }

    /**
     * ファイルシステム上のファイルから読み込む
     * @param file
     * @param option
     * @return
     */
    public BitmapImage loadFromFile(File file, LoadOption option) {
        Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
        onLoad(image);
        return this;
    }

    /**
     * Drawableから読み込む
     * @param resources
     * @param id
     * @param option
     * @return
     */
    public BitmapImage loadFromDrawable(Resources resources, int id, LoadOption option) {
        Bitmap image = BitmapFactory.decodeResource(resources, id);
        onLoad(image);
        return this;
    }

    /**
     * 画像ストリームから読み込む。
     * @param is
     * @return
     * @throws IOException
     */
    public BitmapImage loadFromStream(InputStream is) throws IOException {
        Bitmap image = BitmapFactory.decodeStream(is);
        onLoad(image);
        return this;
    }

    /**
     * Uriを指定して読み込む
     * @param context
     * @param uri
     * @param option
     * @return
     * @throws IOException
     */
    public BitmapImage loadFromUri(Context context, Uri uri, int timeout, LoadOption option) throws IOException {
        InputStream is = null;
        try {
            if (uri.toString().startsWith("http")) {
                is = WebInputStream.get(uri.toString(), timeout);
            } else {
                is = context.getContentResolver().openInputStream(uri);
            }
            Bitmap image = BitmapFactory.decodeStream(is);
            onLoad(image);
            return this;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {

                }
            }
        }
    }

    /**
     * 空の画像を作成する
     * @param width
     * @param height
     * @param config
     * @return
     */
    public BitmapImage create(int width, int height, Config config) {
        Bitmap image = Bitmap.createBitmap(width, height, config);
        onLoad(image);
        return this;
    }

    /**
     * 単一食で塗りつぶす
     * @param colorARGB
     * @return
     */
    public BitmapImage clear(int colorARGB) {
        Graphics g = getGraphics();
        g.setColorRGBA(Color.red(colorARGB), Color.green(colorARGB), Color.blue(colorARGB), Color.alpha(colorARGB));
        g.clearRGBA(Color.red(colorARGB), Color.green(colorARGB), Color.blue(colorARGB), Color.alpha(colorARGB));
        g.fillRect(0, 0, g.getWidth(), g.getHeight());
        return this;
    }

    @Override
    public int getWidth() {
        return bitmapResource.rawBitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return bitmapResource.rawBitmap.getHeight();
    }

    /**
     * 操作用のGraphicsを取得する。
     * @return
     */
    public Graphics getGraphics() {
        Graphics g = new Graphics();
        g.setCanvas(new Canvas(getBitmap()));
        return g;
    }

    /**
     * 管理対象のBitmapを取得する。
     * @return
     */
    public Bitmap getBitmap() {
        return bitmapResource.rawBitmap;
    }

    @Override
    public List<IRawResource> getRawResources() {
        List<IRawResource> result = new ArrayList<IRawResource>();
        result.add(sharedResource);
        return result;
    }

    @Override
    protected void onDispose() {
    }

    /**
     * 管理リソース
     * @author TAKESHI YAMASHITA
     *
     */
    static class BitmapResource implements IRawResource {
        Bitmap rawBitmap = null;

        public BitmapResource(Bitmap image) {
            this.rawBitmap = image;
        }

        @Override
        public void dispose() {
            rawBitmap.recycle();
            rawBitmap = null;
        }
    }

    /**
     * 読み込み時のオプション
     * @author TAKESHI YAMASHITA
     *
     */
    public static class LoadOption {
        /**
         * 最大幅
         */
        int maxHeight = -1;

        /**
         * 最大高さ
         */
        int maxWidth = -1;

        /**
         * Bitmap読み込み時オプション
         */
        Options option = null;
    }
}
