package com.eaglesakura.android.camera;

import android.hardware.Camera;

import java.util.HashMap;
import java.util.Map;

/**
 * カメラの種類ごとに管理する
 */
public class CameraType {
    private static final Map<Integer, CameraType> gCameraTypeMap;

    /**
     * メインカメラ
     */
    public static final CameraType TYPE_MAIN;

    /**
     * サブカメラ
     * <br>
     * ただし、カメラが物理的に１つしか無い場合はnullとなる
     */
    public static final CameraType TYPE_SUB;

    static {
        gCameraTypeMap = new HashMap<Integer, CameraType>();

        int numCameras = Camera.getNumberOfCameras();
        if (numCameras > 0) {
            // メインカメラ情報を得る
            {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(0, info);
                TYPE_MAIN = new CameraType(info, 0);
            }

            if (numCameras > 1) {
                // サブカメラ情報を得る
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(1, info);
                TYPE_SUB = new CameraType(info, 1);
            } else {
                // サブカメラがない
                TYPE_SUB = null;
            }
        } else {
            // カメラが接続されていない
            TYPE_MAIN = null;
            TYPE_SUB = null;
        }
    }

    private final Camera.CameraInfo info;
    private final int cameraNumber;

    CameraType(Camera.CameraInfo info, int cameraNumber) {
        this.info = info;
        this.cameraNumber = cameraNumber;
    }


    public Camera.CameraInfo getInfo() {
        return info;
    }

    public int getCameraNumber() {
        return cameraNumber;
    }

    /**
     * フロントカメラであればtrue
     */
    public boolean isFrontCamera() {
        return info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
    }

    /**
     * リアカメラであればtrue
     */
    public boolean isRearCamera() {
        return !isFrontCamera();
    }

    /**
     * 端末がメインカメラを持っていたらtrue
     */
    public static boolean hasMainCamera() {
        return TYPE_MAIN != null;
    }

    /**
     * 端末がサブカメラを持っていたらtrue
     */
    public static boolean hasSubCamera() {
        return TYPE_SUB != null;
    }

    public static CameraType fromNumber(int number) {
        if (number == 0) {
            return TYPE_MAIN;
        } else {
            return TYPE_SUB;
        }
    }
}
