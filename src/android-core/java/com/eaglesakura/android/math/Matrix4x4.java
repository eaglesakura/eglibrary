package com.eaglesakura.android.math;

import android.opengl.Matrix;

import com.eaglesakura.math.Vector3;

/**
 * OpenGL ESの挙動に合わせた4x4行列を管理する
 */
public final class Matrix4x4 {
    /**
     * 単位行列を作成する。
     */
    public Matrix4x4() {
    }

    public Matrix4x4(Matrix4x4 origin) {
        for (int i = 0; i < 16; ++i) {
            m[i] = origin.m[i];
        }
    }

    /**
     * 回転行列を作成する。
     *
     * @param x
     * @param y
     * @param z
     * @param w
     */
    public void rotate(float x, float y, float z, float w) {
        Matrix.rotateM(m, 0, w, x, y, z);
    }

    /**
     * 位置行列を作成する。
     *
     * @param x
     * @param y
     * @param z
     */
    public void translate(float x, float y, float z) {
        m[4 * 3 + 0] += x;
        m[4 * 3 + 1] += y;
        m[4 * 3 + 2] += z;
    }

    /**
     * 逆行列を作成する。
     */
    public void invert() {
        Matrix.invertM(m, 0, m, 0);
    }

    /**
     * 逆行列を作成する。
     *
     * @param result
     */
    public Matrix4x4 invert(Matrix4x4 result) {
        Matrix.invertM(result.m, 0, m, 0);
        return result;
    }

    /**
     * this = this * transの計算を行う。
     *
     * @param trans
     */
    public void multiply(Matrix4x4 trans) {
        Matrix.multiplyMM(m, 0, trans.m, 0, m, 0);
    }

    /**
     * この行列を適用したベクトルをresultへ格納する。
     *
     * @param v
     * @param result
     * @return resultの参照
     */
    public Vector3 transVector(Vector3 v, Vector3 result) {
        //! Wを生成する
        float w = m[4 * 0 + 3] * v.x + m[4 * 1 + 3] * v.y + m[4 * 2 + 3] * v.z + m[4 * 3 + 3];

        result.set(((m[4 * 0 + 0] * v.x) + (m[4 * 1 + 0] * v.y) + (m[4 * 2 + 0] * v.z) + m[4 * 3 + 0]) / w,

                ((m[4 * 0 + 1] * v.x) + (m[4 * 1 + 1] * v.y) + (m[4 * 2 + 1] * v.z) + m[4 * 3 + 1]) / w,

                ((m[4 * 0 + 2] * v.x) + (m[4 * 1 + 2] * v.y) + (m[4 * 2 + 2] * v.z) + m[4 * 3 + 2]) / w);

        return result;
    }

    private static Matrix4x4 tempTrans = new Matrix4x4();

    /**
     * 描画用変換行列を作成する。<BR>
     * 適用は<BR>
     * scale -＞ rotateX -＞ rotateY -＞ rotateZ -＞ position<BR>
     * となる。
     *
     * @param scale
     * @param rotate
     * @param position
     * @param result
     * @return resultの参照
     */
    public static Matrix4x4 create(Vector3 scale, Vector3 rotate, Vector3 position, Matrix4x4 result) {
        result.identity();

        if (scale != null && (scale.x != 1.0f || scale.y != 1.0f || scale.z != 1.0f)) {
            result.scale(scale.x, scale.y, scale.z);
        }

        if (rotate != null) {
            //! x
            if (rotate.x != 0.0f) {
                tempTrans.identity();
                tempTrans.rotate(1.0f, 0.0f, 0.0f, rotate.x);
                result.multiply(tempTrans);
            }

            //! y
            if (rotate.y != 0.0f) {
                tempTrans.identity();
                tempTrans.rotate(0.0f, 1.0f, 0.0f, rotate.y);
                result.multiply(tempTrans);
            }

            //! z
            if (rotate.z != 0.0f) {
                tempTrans.identity();
                tempTrans.rotate(0.0f, 0.0f, 1.0f, rotate.z);
                result.multiply(tempTrans);
            }
        }

        if (position != null) {
            tempTrans.identity();
            tempTrans.translate(position.x, position.y, position.z);

            result.multiply(tempTrans);
        }
        return result;
    }

    /**
     * result = this * transの計算を行う。
     *
     * @param trans
     * @param result
     * @return
     */
    public Matrix4x4 multiply(Matrix4x4 trans, Matrix4x4 result) {
        Matrix.multiplyMM(result.m, 0, trans.m, 0, m, 0);

        return result;
    }

    /**
     * 内部管理を行っている行列。<BR>
     * 速度優先のため、公開属性とする。
     */
    public float[] m = {
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f
    };

    /**
     * 拡大行列を作成する。
     *
     * @param x
     * @param y
     * @param z
     */
    public void scale(float x, float y, float z) {
        m[4 * 0 + 0] = x;
        m[4 * 1 + 1] = y;
        m[4 * 2 + 2] = z;
    }

    /**
     * 値をコピーする。
     *
     * @param origin
     */
    public void set(Matrix4x4 origin) {
        for (int i = 0; i < 4 * 4; ++i) {
            m[i] = origin.m[i];
        }
    }

    /**
     * 単位行列を作成する。
     */
    public void identity() {
        for (int i = 0; i < 4; ++i) {
            for (int k = 0; k < 4; ++k) {
                if (i != k)
                    m[i * 4 + k] = 0.0f;
                else
                    m[i * 4 + k] = 1.0f;
            }
        }
    }

    private static final int TO_TRANS_INDEX(int y, int x) {
        return (y * 4) + x;
    }

    /**
     * 視線変更行列を生成する。
     *
     * @param position
     * @param look
     * @param up
     */
    public void lookAt(Vector3 position, Vector3 look, Vector3 up) {
        Vector3 zaxis = new Vector3(), xaxis = new Vector3(), yaxis = new Vector3();

        zaxis.sub(position, look);
        zaxis.normalize();
        up.cross(zaxis, xaxis);
        xaxis.normalize();
        zaxis.cross(xaxis, yaxis);

        {
            // Transformインスタンスに設定
            m[TO_TRANS_INDEX(0, 0)] = xaxis.x;
            m[TO_TRANS_INDEX(1, 0)] = xaxis.y;
            m[TO_TRANS_INDEX(2, 0)] = xaxis.z;
            m[TO_TRANS_INDEX(3, 0)] = -xaxis.dot(position);

        }
        {

            m[TO_TRANS_INDEX(0, 1)] = yaxis.x;
            m[TO_TRANS_INDEX(1, 1)] = yaxis.y;
            m[TO_TRANS_INDEX(2, 1)] = yaxis.z;
            m[TO_TRANS_INDEX(3, 1)] = -yaxis.dot(position);
        }
        // 視線ベクトルを求める
        {

            m[TO_TRANS_INDEX(0, 2)] = -zaxis.x;
            m[TO_TRANS_INDEX(1, 2)] = -zaxis.y;
            m[TO_TRANS_INDEX(2, 2)] = -zaxis.z;
            m[TO_TRANS_INDEX(3, 2)] = zaxis.dot(position);
        }

        // 視線ベクトルを求める
        {

            m[TO_TRANS_INDEX(0, 3)] = 0;
            m[TO_TRANS_INDEX(1, 3)] = 0;
            m[TO_TRANS_INDEX(2, 3)] = 0;
            m[TO_TRANS_INDEX(3, 3)] = 1;
        }
    }

    /**
     * 射影行列を作成する。
     *
     * @param near
     * @param far
     * @param fovY
     * @param aspect
     */
    public void projection(float near, float far, float fovY, float aspect) {
        float h, w, Q;

        float width_fov = (fovY * (aspect) / 360.0f), height_fov = (fovY / 360.0f);

        w = (float) (1.0 / Math.tan(width_fov * 0.5) / (Math.PI * 2)); // 1/tan(x)
        // ==
        // cot(x)
        h = (float) (1.0 / Math.tan(height_fov * 0.5) / (Math.PI * 2)); // 1/tan(x)
        // ==
        // cot(x)
        Q = far / (far - near);

        for (int i = 0; i < 16; ++i) {
            m[i] = 0;
        }

        m[TO_TRANS_INDEX(0, 0)] = w;
        m[TO_TRANS_INDEX(1, 1)] = h;
        m[TO_TRANS_INDEX(2, 2)] = Q;
        m[TO_TRANS_INDEX(3, 2)] = -Q * near;
        m[TO_TRANS_INDEX(2, 3)] = 1;
    }
}
