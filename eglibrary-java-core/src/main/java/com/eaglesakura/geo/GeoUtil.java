package com.eaglesakura.geo;

/**
 * GPS系のUtil
 */
public class GeoUtil {

    /**
     * GPS2点の距離をキロメートル計算する
     *
     * 参考:http://perota.sakura.ne.jp/blog/android%E3%81%A7%E3%82%82%E7%B7%AF%E5%BA%A6%E7%B5%8C%E5%BA%A6%E3%81%A72%E7%82%B9%E9%96%93%E3%81%AE%E8%B7%9D%E9%9B%A2%E3%82%92%E5%8F%96%E5%BE%97/
     * @param lat0
     * @param lng0
     * @param lat1
     * @param lng1
     * @return GPS2点の距離をキロメートル計算する
     */
    public static double calcDistanceKiloMeter(double lat0, double lng0, double lat1, double lng1) {
        double theta = lng0 - lng1;
        double dist = Math.sin(Math.toRadians(lat0)) * Math.sin(Math.toRadians(lat1)) + Math.cos(Math.toRadians(lat0)) * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(theta));
        dist = Math.acos(dist);
        dist = Math.toDegrees(dist);
        double miles = dist * 60 * 1.1515;
        return (miles * 1.609344);
    }

}
