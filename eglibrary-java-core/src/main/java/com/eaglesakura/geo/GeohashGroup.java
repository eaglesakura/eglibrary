package com.eaglesakura.geo;

import com.eaglesakura.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ジオハッシュの中心＋周囲8グリッドを1グループとして管理する
 */
public class GeohashGroup {

    /**
     * 判断を行うジオハッシュの長さ
     */
    int geohashLength = 7;

    /**
     * 中心のジオハッシュ値
     */
    private String centerGeohash;

    /**
     * 隣接ジオハッシュ
     */
    private List<String> adjustGeohash = new ArrayList<String>();

    private Set<GeohashGroupListener> listeners = new HashSet<GeohashGroupListener>();

    public GeohashGroup() {
    }

    /**
     * ジオハッシュに変換する
     *
     * @param lat
     * @param lng
     * @return
     */
    public String toGeohash(double lat, double lng) {
        return Geohash.encode(lat, lng).substring(0, geohashLength);
    }

    /**
     * 隣接ジオハッシュを求める
     *
     * @param base
     * @param direction
     * @return
     */
    public String calculateAdjacent(String base, int direction) {
        return Geohash.calculateAdjacent(base, direction).substring(0, geohashLength);
    }

    /**
     * 隣接するジオハッシュを計算する
     */
    void calcAdjustGeohash() {
        adjustGeohash.clear();

        final String top = calculateAdjacent(centerGeohash, Geohash.TOP);
        final String topLeft = calculateAdjacent(top, Geohash.LEFT);
        final String topRight = calculateAdjacent(top, Geohash.RIGHT);
        final String left = calculateAdjacent(centerGeohash, Geohash.LEFT);
        final String right = calculateAdjacent(centerGeohash, Geohash.RIGHT);
        final String bottom = calculateAdjacent(centerGeohash, Geohash.BOTTOM);
        final String bottomLeft = calculateAdjacent(bottom, Geohash.LEFT);
        final String bottomRight = calculateAdjacent(bottom, Geohash.RIGHT);

        adjustGeohash.add(topLeft);
        adjustGeohash.add(top);
        adjustGeohash.add(topRight);
        adjustGeohash.add(left);
        adjustGeohash.add(right);
        adjustGeohash.add(bottomLeft);
        adjustGeohash.add(bottom);
        adjustGeohash.add(bottomRight);
    }

    /**
     * 位置を更新する
     *
     * @param lat 北緯
     * @param lng 東経
     * @return ジオハッシュが更新されたらtrue
     */
    public boolean updateLocation(double lat, double lng) {
        String newGeohash = toGeohash(lat, lng);
        String oldGeohash = centerGeohash;

        // ジオハッシュが一致したら何もしない
        if (!StringUtil.isEmpty(oldGeohash) && oldGeohash.equals(newGeohash)) {
            return false;
        }

        this.centerGeohash = newGeohash;
        // 隣接ジオハッシュを計算させる
        calcAdjustGeohash();

        for (GeohashGroupListener listener : listeners) {
            listener.onGeohashChanged(this, oldGeohash, newGeohash);
        }
        return true;
    }

    /**
     * 中心点のジオハッシュ
     *
     * @return
     */
    public String getCenterGeohash() {
        return centerGeohash;
    }

    /**
     * 中央に隣接するジオハッシュを取得する
     * 0, 1, 2
     * 3, C, 4,
     * 5, 6, 7
     *
     * @return
     */
    public List<String> getAdjustGeohash() {
        return adjustGeohash;
    }

    /**
     * 全てのジオハッシュ値を取得する
     *
     * @return
     */
    public List<String> allGeohash() {
        List<String> result = new ArrayList<String>();
        result.addAll(adjustGeohash);
        if (centerGeohash != null) {
            result.add(centerGeohash);
        }
        return result;
    }

    /**
     * 指定したジオハッシュを内包している場合true
     *
     * @param geohash
     * @return
     */
    public boolean hasGeohash(String geohash) {
        if (geohash.equals(centerGeohash)) {
            return true;
        }

        return adjustGeohash.contains(geohash);
    }


    /**
     * ジオハッシュの計算精度を指定する
     *
     * @param geohashLength
     */
    public void setGeohashLength(int geohashLength) {
        this.geohashLength = geohashLength;
    }

    public int getGeohashLength() {
        return geohashLength;
    }

    /**
     *
     */
    public interface GeohashGroupListener {
        /**
         * ジオハッシュグループが変更された
         *
         * @param group      更新されたグループ
         * @param oldGeohash 古いジオハッシュ
         * @param newGeohash 新しいジオハッシュ
         */
        public void onGeohashChanged(GeohashGroup group, String oldGeohash, String newGeohash);
    }
}
