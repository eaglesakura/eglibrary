package com.eaglesakura.android.camera;

import android.hardware.Camera;

/**
 * 撮影・プレビュー用のサイズを返す
 */
public class PictureSize {
    private final Camera.Size size;

    public enum AspectID {
        /**
         * 縦横1:1
         */
        WH1x1 {
            @Override
            public double aspect() {
                return 1;
            }

            @Override
            public String aspectText() {
                return "1:1";
            }
        },

        /**
         * 縦横3x2
         */
        WH3x2 {
            @Override
            public double aspect() {
                return 3.0 / 2.0;
            }

            @Override
            public String aspectText() {
                return "3:2";
            }
        },
        /**
         * 縦横4:3
         */
        WH4x3 {
            @Override
            public double aspect() {
                return 4.0 / 3.0;
            }

            @Override
            public String aspectText() {
                return "4:3";
            }
        },

        /**
         * 縦横16:9
         */
        WH16x9 {
            @Override
            public double aspect() {
                return 16.0 / 9.0;
            }

            @Override
            public String aspectText() {
                return "16:9";
            }
        },

        /**
         * 縦横16:10
         */
        WH16x10 {
            @Override
            public double aspect() {
                return 16.0 / 10.0;
            }

            @Override
            public String aspectText() {
                return "16:10";
            }
        };

        /**
         * 横ピクセル数 / 縦ピクセル数のアスペクト比を取得する
         *
         * @return
         */
        public abstract double aspect();

        /**
         * アスペクト比のテキストを取得する
         * <p/>
         * 例：16:9
         *
         * @return
         */
        public abstract String aspectText();

        /**
         * 最も近いアスペクト比を取得する
         *
         * @param aspect
         * @return
         */
        public static AspectID getNearAspect(double aspect) {
            double diffNear = 99999999;
            AspectID result = null;

            AspectID[] values = values();
            for (AspectID value : values) {
                final double checkDiff = Math.abs(value.aspect() - aspect);
                if (checkDiff < diffNear) {
                    // 差が小さいならコレにする
                    result = value;
                    // 次はコレが比較対象
                    diffNear = checkDiff;
                }
            }
            return result;
        }
    }

    public PictureSize(Camera.Size size) {
        this.size = size;
    }

    /**
     * ピクセル数をメガピクセル単位で取得する
     *
     * @return 計算されたメガピクセル
     */
    public double getMegaPixel() {
        return ((double) (size.width * size.height)) / 1000.0 / 1000.0;
    }

    public int getWidth() {
        return size.width;
    }

    public int getHeight() {
        return size.height;
    }

    /**
     * カメラ用サイズを取得する
     *
     * @return
     */
    public Camera.Size getCameraSize() {
        return size;
    }

    /**
     * ユーザー表示用のメガピクセル数を取得する。
     * <p/>
     * 小数点第一位まで計算する
     * <p/>
     * 例: 5
     * <p/>
     * 例：13.1
     *
     * @return 表示用のメガピクセル
     */
    public String getMegaPixelText() {
        return String.format("%.1f", getMegaPixel());
    }

    /**
     * アスペクト比を取得する
     *
     * @return
     */
    public double getAspect() {
        return (double) getWidth() / (double) getHeight();
    }

    /**
     * アスペクト比のIDを取得する
     *
     * @return
     */
    public AspectID getAspectId() {
        return AspectID.getNearAspect(getAspect());
    }

    /**
     * 一意に識別するためのIDを取得する
     *
     * @return
     */
    public String getId() {
        return String.format("pic(%dx%d)", getWidth(), getHeight());
    }
}
