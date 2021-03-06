package com.eaglesakura.android.glkit.media;

import com.eaglesakura.android.camera.CameraManager;
import com.eaglesakura.android.camera.CameraType;
import com.eaglesakura.android.camera.FocusModeSpec;
import com.eaglesakura.android.camera.PictureSizeSpec;
import com.eaglesakura.android.camera.SceneSpec;
import com.eaglesakura.android.camera.WhiteBaranceSpec;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.math.Vector2;
import com.eaglesakura.time.Timer;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;
import com.eaglesakura.util.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.Surface;

import java.io.File;

/**
 * ビデオ撮影用リクエストを組み立てる
 */
public class VideoRecordRequest {
    /**
     * ビデオのビットレートを指定する
     * <br>
     * default = 18Mbps
     */
    int videoBitrate = 1024 * 1024 * 18;

    /**
     * ビデオのフレームレートを指定する
     */
    int videoFramerate = 30;

    /**
     * サウンドのビットレートを指定する
     * <br>
     * default = 128kbps
     */
    int audioBitrate = 1024 * 128;

    /**
     * サウンドのサンプリングレートを指定する
     * <br>
     * default = 44kHz
     */
    int audioSamplingRate = 44100;

    /**
     * 手ぶれ補正
     */
    boolean stabilization = true;

    /**
     * 録画時間
     * <br>
     * default = 60秒
     */
    long recoredMilliSec = 1000 * 60;

    /**
     * 撮影サイズの直接指定
     * <br>
     * default=FULL-HD
     */
    Vector2 size = new Vector2(1980, 1080);

    /**
     * 撮影サイズID
     */
    String sizeId = null;

    /**
     * フォーカスモード指定
     */
    FocusModeSpec focusModeSpec = FocusModeSpec.SETTING_CONTINUOUS_VIDEO;

    /**
     * ホワイトバランス指定
     */
    WhiteBaranceSpec whiteBaranceSpec = WhiteBaranceSpec.SETTING_AUTO;

    /**
     * シーン情報
     */
    SceneSpec sceneSpec = SceneSpec.SETTING_AUTO;

    /**
     * プレビュー対象のサーフェイス
     */
    Object previewSurface;

    /**
     * カメラの撮影タイプ
     */
    CameraType cameraType = CameraType.TYPE_MAIN;

    /**
     * 出力されるファイルコンテナ
     */
    int outputVideoFormat = MediaRecorder.OutputFormat.MPEG_4;

    /**
     * ビデオコーデック
     */
    int videoEncoder = MediaRecorder.VideoEncoder.H264;

    /**
     * オーディオコーデック
     */
    int audioEncoder = MediaRecorder.AudioEncoder.DEFAULT;

    public VideoRecordRequest cameraType(CameraType type) {
        this.cameraType = type;
        return this;
    }

    public VideoRecordRequest videoBitorate(int bitrate) {
        this.videoBitrate = bitrate;
        return this;
    }

    public VideoRecordRequest audioBitrate(int bitrate) {
        this.audioBitrate = bitrate;
        return this;
    }

    public VideoRecordRequest stabirlization(boolean set) {
        this.stabilization = set;
        return this;
    }

    public VideoRecordRequest recordTimeMs(int ms) {
        this.recoredMilliSec = ms;
        return this;
    }

    public VideoRecordRequest size(int width, int height) {
        this.size.set(width, height);
        return this;
    }

    public VideoRecordRequest size(String sizeId) {
        this.sizeId = sizeId;
        return this;
    }

    public VideoRecordRequest scene(SceneSpec scene) {
        this.sceneSpec = scene;
        return this;
    }

    public VideoRecordRequest focus(FocusModeSpec focus) {
        this.focusModeSpec = focus;
        return this;
    }

    public VideoRecordRequest whiteBarance(WhiteBaranceSpec white) {
        this.whiteBaranceSpec = white;
        return this;
    }

    public VideoRecordRequest previewSurface(Object surface) {
        this.previewSurface = surface;
        return this;
    }

    /**
     * 撮影を行う
     */
    @SuppressLint("NewApi")
    public static boolean record(Context context, File outputFile, VideoRecordRequest request, VideoRecordCallback callback) {
        if (AndroidUtil.isUIThread()) {
            throw new IllegalStateException("call Background!!");
        }

        CameraManager cameraManager = null;
        OffscreenPreviewSurface surface = null;
        Surface rawSurface = null;
        MediaRecorder recorder = null;
        try {

            cameraManager = new CameraManager(context);
            cameraManager.connect(request.cameraType);

            if (cameraManager.requestScene(request.sceneSpec)) {
                LogUtil.log("Scene Mode(%s) complete", request.sceneSpec.getApiSettingName());
            } else {
                LogUtil.log("Scene Mode(%s) fail", request.sceneSpec.getApiSettingName());
            }

            if (cameraManager.requestFocusMode(request.focusModeSpec)) {
                LogUtil.log("Focus Mode(%s) complete", request.focusModeSpec.getApiSettingName());
            } else {
                LogUtil.log("Focus Mode(%s) fail", request.focusModeSpec.getApiSettingName());
            }

            if (cameraManager.requestWhiteBarance(request.whiteBaranceSpec)) {
                LogUtil.log("WhiteBarance Mode(%s) complete", request.whiteBaranceSpec.getApiSettingName());
            } else {
                LogUtil.log("WhiteBarance Mode(%s) fail", request.whiteBaranceSpec.getApiSettingName());
            }

            Camera camera = cameraManager.getCamera();
            camera.unlock();

            recorder = new MediaRecorder();
            recorder.setCamera(camera);

            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

            recorder.setOutputFormat(request.outputVideoFormat);
            // 録画ディレクトリを作成
            outputFile.getParentFile().mkdirs();
            recorder.setOutputFile(outputFile.getAbsolutePath());

            recorder.setVideoEncoder(request.videoEncoder);
            recorder.setAudioEncoder(request.audioEncoder);


            // Preview Display
            if (request.previewSurface != null) {
                recorder.setPreviewDisplay((Surface) request.previewSurface);
            } else {
                surface = new OffscreenPreviewSurface(context);
                rawSurface = new Surface((SurfaceTexture) surface.createSurface());
                recorder.setPreviewDisplay(rawSurface);
            }

            // bitrate
            recorder.setVideoEncodingBitRate(request.videoBitrate);
            recorder.setAudioEncodingBitRate(request.audioBitrate);
            recorder.setAudioSamplingRate(request.audioSamplingRate);


            // Video size
            if (!StringUtil.isEmpty(request.sizeId)) {
                // IDで指定する
                PictureSizeSpec spec = cameraManager.getSpecs().getVideoSize(request.sizeId);
                recorder.setVideoSize(spec.getWidth(), spec.getHeight());
            } else {
                // 直接指定する
                recorder.setVideoSize((int) request.size.x, (int) request.size.y);
            }
            // フレームレート指定
            recorder.setVideoFrameRate(request.videoFramerate);

            recorder.prepare();
            recorder.start();

            Timer timer = new Timer();
            callback.onRecoredStart(request);

            // 規定時間まで録画を繰り返す
            while (timer.end() < request.recoredMilliSec && !callback.isCanceled()) {
                callback.onRecoredUpdated(request, timer.end());
                Util.sleep(10);
            }

            recorder.stop();
            recorder.release();

            callback.onRecoredFinished(request);
            return true;
        } catch (Exception e) {
            LogUtil.log(e);
            callback.onRecoredFailed(request);
            return false;
        } finally {
            if (cameraManager != null) {
                cameraManager.disconnect();
            }
            if (rawSurface != null) {
                rawSurface.release();
            }
            if (surface != null) {
                surface.dispose();
            }
        }
    }

    public interface VideoRecordCallback {
        /**
         * キャンセルされていたらtrue
         */
        boolean isCanceled();

        /**
         * ビデオ録画を開始した
         */
        void onRecoredStart(VideoRecordRequest request);

        /**
         * ビデオ録画中定期的に呼び出される
         */
        void onRecoredUpdated(VideoRecordRequest request, long recordTimeMs);

        /**
         * ビデオの録画を停止した
         */
        void onRecoredFinished(VideoRecordRequest request);

        /**
         * 録画に失敗した
         */
        void onRecoredFailed(VideoRecordRequest request);

    }
}
