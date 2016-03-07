package com.eaglesakura.android.net;

import com.eaglesakura.android.device.external.StorageInfo;
import com.eaglesakura.android.net.parser.BitmapParser;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.android.net.request.SimpleHttpRequest;
import com.eaglesakura.android.util.AndroidThreadUtil;
import com.eaglesakura.util.IOUtil;

import android.graphics.Bitmap;
import android.test.AndroidTestCase;

import java.io.File;

public class NetworkConnectorTest extends AndroidTestCase {

    File mCacheDirectory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mCacheDirectory = new File(StorageInfo.getExternalStorageRoot(getContext()), ".cache");
        IOUtil.mkdirs(mCacheDirectory);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        IOUtil.delete(mCacheDirectory);
    }

    public void test_get操作で200が返却される() throws Exception {
        AndroidThreadUtil.assertBackgroundThread();


        NetworkConnector connector = NetworkConnector.createBinaryApi(getContext(), mCacheDirectory);
        SimpleHttpRequest request = new SimpleHttpRequest(ConnectRequest.Method.GET);
        request.setUrl("https://http.cat/200", null);
        request.setReadTimeoutMs(1000 * 30);
        request.setConnectTimeoutMs(1000 * 30);
        request.getCachePolicy().setCacheLimitTimeMs(1000 * 60);

        // 初回はキャッシュがないのでダイレクトに取得できる
        {
            Result<Bitmap> connect = connector.connect(request, new BitmapParser(), it -> false);
            assertNotNull(connect.getResult());

            Bitmap image = connect.getResult();
            assertEquals(image.getWidth(), 750);
            assertEquals(image.getHeight(), 600);
            assertNull(connect.getCacheDigest());
            assertNotNull(connect.getContentDigest());

            // キャッシュが一致している
//            String cacheMD5 = IOUtil.genMD5(mCacheDirectory.listFiles()[0]);
//            assertEquals(connect.getContentDigest(), cacheMD5);
        }

        // 2回めはキャッシュが働く
        {
            Result<Bitmap> connect = connector.connect(request, new BitmapParser(), it -> false);
            assertNotNull(connect.getResult());

            Bitmap image = connect.getResult();
            assertEquals(image.getWidth(), 750);
            assertEquals(image.getHeight(), 600);

            // キャッシュがあり、ネット経由のコンテンツは無い
            assertNotNull(connect.getCacheDigest());
            assertNull(connect.getContentDigest());
        }
    }
}
