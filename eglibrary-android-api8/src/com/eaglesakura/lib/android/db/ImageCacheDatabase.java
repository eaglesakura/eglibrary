package com.eaglesakura.lib.android.db;

import java.io.ByteArrayInputStream;
import java.io.File;

import android.content.Context;

import com.eaglesakura.lib.android.game.graphics.canvas.BitmapImage;
import com.eaglesakura.lib.android.game.resource.DisposableResource;
import com.eaglesakura.lib.android.game.resource.GarbageCollector;

/**
 * On Memoryで済む程度の小さな画像キャッシュを提供する。
 * @author TAKESHI YAMASHITA
 *
 */
public class ImageCacheDatabase extends DisposableResource {
    File file = null;

    Context context = null;

    static final String IMAGE_TABLE = "image_table";

    static final int DB_VERSION = 0x01;

    BlobKeyValueStore store = null;

    /**
     * キャッシング用のGCオブジェクト。
     */
    GarbageCollector garbageCollector = new GarbageCollector();

    public ImageCacheDatabase(File dbFile, Context context) {
        this.file = dbFile;
        this.context = context.getApplicationContext();
        store = new BlobKeyValueStore(dbFile, context, IMAGE_TABLE, DBType.ReadWrite, DB_VERSION);
    }

    /**
     * 
     * @param key
     * @param loader
     * @return
     */
    public BitmapImage get(String key, Loader loader) {
        try {
            BlobKeyValueStore.Data data = store.get(key);
            if (data == null) {
                byte[] blob = loader.load(this, key);
                BitmapImage bitmapImage = new BitmapImage().loadFromStream(new ByteArrayInputStream(blob));

                store.insert(key, blob);
                return bitmapImage;
            } else {
                BitmapImage image = new BitmapImage().loadFromStream(new ByteArrayInputStream(data.getValue()));
                if (loader.isExist(this, image, data.date)) {
                    // 利用を許可されたので、画像化して返す。
                    return image;
                } else {

                    // 画像をDBから削除して再度読み込みを試す。
                    image.dispose();
                    store.remove(key);
                    return get(key, loader);
                }
            }

        } catch (Exception e) {

        }
        return null;
    }

    public interface Loader {
        /**
         * キャッシュがヒットしなかった場合、画像バイナリを読み込む。
         * @param store
         * @param key
         * @return
         */
        public byte[] load(ImageCacheDatabase db, String key);

        /**
         * 読み出された画像を利用するならtrueを返す。
         * @param image
         * @param date
         * @return
         */
        public boolean isExist(ImageCacheDatabase db, BitmapImage image, long date);
    }

    public GarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

    /**
     * 不要なオブジェクトを削除する。
     */
    public void gc() {
        garbageCollector.gc();
    }

    @Override
    public void dispose() {
        garbageCollector.gc();
        if (store != null) {
            store.dispose();
            store = null;
        }
    }
}
