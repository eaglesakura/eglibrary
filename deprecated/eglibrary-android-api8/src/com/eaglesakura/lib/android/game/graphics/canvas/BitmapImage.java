package com.eaglesakura.lib.android.game.graphics.canvas;

import com.eaglesakura.lib.android.game.graphics.ImageBase;
import com.eaglesakura.lib.android.game.graphics.ImageCorrector;
import com.eaglesakura.lib.android.game.io.WebInputStream;
import com.eaglesakura.lib.android.game.resource.GarbageCollector;
import com.eaglesakura.lib.android.game.resource.IRawResource;
import com.eaglesakura.lib.android.game.resource.SharedRawResource;
import com.eaglesakura.lib.android.game.util.LogUtil;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TAKESHI YAMASHITA
 */
public class BitmapImage extends ImageBase {

    SharedRawResource sharedResource = null;
    BitmapResource bitmapResource = null;

    /**
     * 自動的にrecycleを呼び出す場合はtrue
     */
    boolean autoRecycle = true;

    public BitmapImage(GarbageCollector garbageCollector) {
        super(garbageCollector);
    }

    public BitmapImage() {
        this((GarbageCollector) null);
    }

    public BitmapImage(Bitmap image) {
        this((GarbageCollector) null);
        onLoad(image);
    }

    public BitmapImage(BitmapImage origin) {
        this(origin.getGarbageCollector());
        bitmapResource = origin.bitmapResource;
        sharedResource = origin.sharedResource;
        sharedResource.addRef();
    }

    /**
     * 自動でrecycleを呼び出す場合はtrue
     * デフォルトではTRUE
     */
    public BitmapImage setAutoRecycle(boolean set) {
        autoRecycle = set;
        return this;
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

        bitmapResource = new BitmapResource(image, autoRecycle);
        sharedResource = new SharedRawResource(bitmapResource);
        sharedResource.addRef();
        register();
    }

    /**
     * ファイルシステム上のファイルから読み込む
     */
    public BitmapImage loadFromFile(File file, LoadOption option) {
        Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
        onLoad(image);
        return this;
    }

    /**
     * Drawableから読み込む
     */
    public BitmapImage loadFromDrawable(Resources resources, int id, LoadOption option) {
        Bitmap image = BitmapFactory.decodeResource(resources, id);
        onLoad(image);
        return this;
    }

    /**
     * 画像ストリームから読み込む。
     */
    public BitmapImage loadFromStream(InputStream is) throws IOException {
        Bitmap image = BitmapFactory.decodeStream(is);
        onLoad(image);
        return this;
    }

    /**
     * Uriを指定して読み込む
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
     * 最大幅・高さを指定してリサイズする。
     */
    public BitmapImage fitting(int maxWidth, int maxHeight) {
        ImageCorrector corrector = new ImageCorrector();
        corrector.setRenderArea(0, 0, maxWidth, maxHeight);
        corrector.setImageAspect(getWidth(), getHeight());

        final int nWidth = (int) corrector.getImageAreaWidth();
        final int nHeight = (int) corrector.getImageAreaHeight();

        LogUtil.log("scaled = " + nWidth + "x" + nHeight);

        Bitmap nImage = Bitmap.createScaledBitmap(getBitmap(), nWidth, nHeight, true);

        // ロードを行う
        onLoad(nImage);
        return this;
    }

    /**
     * 空の画像を作成する
     */
    public BitmapImage create(int width, int height, Config config) {
        Bitmap image = Bitmap.createBitmap(width, height, config);
        onLoad(image);
        return this;
    }

    /**
     * 単一食で塗りつぶす
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
     */
    public Graphics getGraphics() {
        Graphics g = new Graphics();
        g.setCanvas(new Canvas(getBitmap()));
        return g;
    }

    /**
     * 管理対象のBitmapを取得する。
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
     *
     * @author TAKESHI YAMASHITA
     */
    static class BitmapResource implements IRawResource {
        Bitmap rawBitmap = null;

        boolean autoRecycle = false;

        public BitmapResource(Bitmap image, boolean autoRecycle) {
            this.rawBitmap = image;
            this.autoRecycle = autoRecycle;
        }

        @Override
        public void dispose() {
            if (rawBitmap != null && !rawBitmap.isRecycled()) {
                rawBitmap.recycle();
                rawBitmap = null;
            }
        }
    }

    /**
     * 読み込み時のオプション
     *
     * @author TAKESHI YAMASHITA
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
