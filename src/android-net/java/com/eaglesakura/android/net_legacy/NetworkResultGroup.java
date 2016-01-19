package com.eaglesakura.android.net_legacy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 複数のネットワーク戻り値を同時チェックする
 */
public class NetworkResultGroup {

    List<LegacyNetworkResult<?>> networkResults = new ArrayList<>();

    /**
     * リクエストを追加する
     *
     * @param networkResult
     * @return
     */
    public <T> LegacyNetworkResult<T> add(LegacyNetworkResult<T> networkResult) {
        networkResults.add(networkResult);
        return networkResult;
    }

    /**
     * 全てのデータを待つ
     *
     * @return データが一つでも更新されていたらtrueを返却する
     * @throws IOException
     */
    public boolean awaitAll() throws IOException {
        int modifiedCount = 0;
        for (LegacyNetworkResult<?> result : networkResults) {
            result.await();
            if (result.isDataModified()) {
                ++modifiedCount;
            }
        }

        return modifiedCount > 0;
    }

    /**
     * 一つでもデータが更新されたらtrue
     *
     * @return
     */
    public boolean isDataModified() {
        return getDataModifiedCount() > 0;
    }

    /**
     * 変更されたデータ数を数える
     *
     * @return
     */
    public int getDataModifiedCount() {

        int modifiedCount = 0;
        for (LegacyNetworkResult<?> result : networkResults) {
            if (result.isDataModified()) {
                ++modifiedCount;
            }
        }

        return modifiedCount;
    }
}
