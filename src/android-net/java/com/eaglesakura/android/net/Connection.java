package com.eaglesakura.android.net;

import com.eaglesakura.android.net.cache.ICacheController;
import com.eaglesakura.android.net.request.ConnectRequest;
import com.eaglesakura.android.thread.async.IAsyncTask;
import com.eaglesakura.util.StringUtil;

public abstract class Connection<T> implements IAsyncTask<T> {
    /**
     * キャッシュの指紋を取得する
     *
     * @return
     */
    public abstract String getCacheDigest();

    /**
     * コンテンツの指紋を取得する
     *
     * @return
     */
    public abstract String getContentDigest();

    /**
     * キャッシュ制御を取得する
     *
     * @return
     */
    public abstract ICacheController getCacheController();

    /**
     * リクエスト情報を取得する
     *
     * @return
     */
    public abstract ConnectRequest getRequest();

    /**
     * キャッシュを取得済みであればtrue
     */
    public boolean hasCache() {
        return !StringUtil.isEmpty(getCacheDigest());
    }

    /**
     * コンテンツを何らかの手段で取得済みであればtrue
     *
     * キャッシュロードした場合もtrueを返却する。
     *
     * @return
     */
    public boolean hasContent() {
        return getContentDigest() != null || getCacheDigest() != null;
    }

    /**
     * コンテンツがキャッシュと切り替わっていたらtrueを返却する
     *
     * @return
     */
    public boolean isContentModified() {
        String cache = getCacheDigest();
        String content = getContentDigest();
        if (cache == null) {
            // キャッシュが無いなら強制的に書き換わり
            return true;
        } else if (content != null) {
            // コンテンツがあるならば、指紋チェックし、一致しなければコンテンツの入れ替わりである
            return !content.equals(cache);
        } else {
            return true;
        }
    }
}
