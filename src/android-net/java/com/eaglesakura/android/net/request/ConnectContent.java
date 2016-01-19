package com.eaglesakura.android.net.request;

import java.io.IOException;
import java.io.InputStream;

/**
 * 通信のPOST/PUTに使用されるコンテンツを定義する。
 * <p/>
 * リトライ等により、複数回処理が呼び出される可能性がある。
 */
public abstract class ConnectContent {

    /**
     * コンテンツの長さを取得する。
     * <p/>
     * 0の場合、このコンテンツは無視される。
     *
     * @return
     */
    public abstract long getLength();

    /**
     * コンテンツを読み込むためのストリームを開く。
     * <p/>
     * closeは自動的に行われる。
     * <p/>
     * リトライによって、複数回呼び出される可能性がある。
     *
     * @return
     * @throws IOException
     */
    public abstract InputStream openStream() throws IOException;

    /**
     * MIMEを取得する。
     * <p/>
     * applicatin/json等
     *
     * @return
     */
    public abstract String getContentType();
}
