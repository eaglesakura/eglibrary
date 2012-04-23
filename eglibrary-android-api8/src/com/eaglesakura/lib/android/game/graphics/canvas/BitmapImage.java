package com.eaglesakura.lib.android.game.graphics.canvas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.eaglesakura.lib.android.game.graphics.ImageBase;
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

        bitmapResource = new BitmapResource(image);
        sharedResource = new SharedRawResource(bitmapResource);
        sharedResource.addRef();
        register();
    }

    public BitmapImage loadFile(File file, LoadOption option) {
        Bitmap image = BitmapFactory.decodeFile(file.getAbsolutePath());
        onLoad(image);
        return this;
    }

    public BitmapImage loadDrawable(Resources resources, int id, LoadOption option) {
        Bitmap image = BitmapFactory.decodeResource(resources, id);
        onLoad(image);
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
