package com.eaglesakura.lib.android.game.loop;

import com.eaglesakura.lib.android.game.display.VirtualDisplay;
import com.eaglesakura.lib.android.game.graphics.gl11.BitmapTextureImage;
import com.eaglesakura.lib.android.game.graphics.gl11.GPU;
import com.eaglesakura.lib.android.game.graphics.gl11.TextureImageBase;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.input.MultiTouchInput;
import com.eaglesakura.lib.android.game.thread.AsyncHandler;
import com.eaglesakura.lib.android.game.thread.ThreadSyncRunnerBase;
import com.eaglesakura.lib.android.game.thread.UIHandler;
import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.android.game.util.Timer;
import com.eaglesakura.lib.android.view.CanvasView;
import com.eaglesakura.lib.android.view.OpenGLView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.io.IOException;
import java.io.InputStream;

/**
 * ゲームループとそれに付随するViewを管理する。 ゲームループはUIスレッドとは別スレッドを利用していることに注意すること。
 */
@Deprecated
public abstract class GameLoopManagerBase {

    /**
     * フレームレートをFPS単位で指定する。
     */
    int frameRate = 30;

    /**
     * 実行中の実際にフレームレートを取得する
     */
    float runningFrameRate = 0;

    /**
     * レジュームをしてからフレームの復帰をさせるまでの時間。
     * 標準で1000ms
     */
    int resumeWaitTime = 1000;

    /**
     * 実フレームのカウントを行う。
     */
    FramerateCounter framerateCounter = new FramerateCounter();

    /**
     * 呼び出し元
     */
    Context context = null;

    /**
     * OpenGL用
     */
    OpenGLView glView = null;

    /**
     * EGL管理クラス
     */
    EGLManager egl = null;

    /**
     * Canvas用
     */
    CanvasView canvasView = null;

    /**
     * 各Layoutを格納する。
     */
    FrameLayout rootLayout = null;

    /**
     * マルチタッチ制御を行う。
     */
    MultiTouchInput multiTouchInput;

    /**
     * 仮想ディスプレイを定義する。
     */
    VirtualDisplay virtualDisplay = new VirtualDisplay();

    /**
     * ライフサイクルの状態を取得する。
     *
     * @author TAKESHI YAMASHITA
     */
    protected enum LifeCycle {
        /**
         * ネイティブの起動中
         */
        Booting,

        /**
         * 初期化中
         */
        Initializing,

        /**
         * 実行中
         */
        Running,

        /**
         * 一時中断中
         */
        Paused,

        /**
         * 破棄されている。
         */
        Finished,
    }

    LifeCycle lifeCycle = LifeCycle.Booting;

    /**
     * デバッグ用のタイマー
     */
    private Timer debugTimer = new Timer();

    /**
     * デバッグフラグ
     */
    private static final boolean TIME_OUTPUT = false;

    /**
     * タイマーを開始する。
     */
    private void debugTimeBegin() {
        if (TIME_OUTPUT) {
            debugTimer.start();
        }
    }

    /**
     * タイマーを終了し、ログを吐き出す
     */
    private void debugTimerEnd(String messageFormat) {
        if (TIME_OUTPUT) {
            LogUtil.log(String.format(messageFormat, debugTimer.end()));
        }
    }

    /**
     * 毎フレームのランナー
     */
    Runnable frameRunner = new Runnable() {
        @Override
        public void run() {
            final long frameTime = 1000 / frameRate;
            final long start = System.currentTimeMillis();
            debugTimeBegin();
            {
                //! タッチパネルを更新する
                getMultiTouchInput().update();

                //! ゲーム処理を呼び出す
                onGameFrame();
            }
            debugTimerEnd("onGameFrame :: %d ms");
            final long end = System.currentTimeMillis();
            final long nextTime = Math.max(1, frameTime - (end - start));

            //! 仮想フレームレートを更新する
            runningFrameRate = 1000.0f / (Math.max(1.0f, (float) (end - start)));

            //! 実フレームレートを更新する。
            framerateCounter.update();
            if (isNextFrameEnable()) {
                gameHandle.postDelayed(this, nextTime);
            }
        }
    };

    /**
     * 直近1フレームのフレームレートを取得する。
     */
    public float getFramerateLast() {
        return runningFrameRate;
    }

    /**
     * 直近1秒のフレームレート実績値を取得する。
     */
    public int getFramerateReal() {
        return framerateCounter.getRealRate();
    }

    /**
     * 親クラスが実装している。
     */
    ILoopParent loopParent = null;

    /**
     * UIスレッド用ハンドル。
     */
    UIHandler uiHandle = null;

    /**
     * ゲームループ用ハンドラ。
     */
    static AsyncHandler gameHandle = AsyncHandler.createInstance("gameloop");

    /**
     *
     * @param context
     * @param loopParent
     */
    public GameLoopManagerBase(Context context, ILoopParent loopParent) {
        this.context = context;
        this.loopParent = loopParent;
        this.uiHandle = new UIHandler();
        this.multiTouchInput = new MultiTouchInput(virtualDisplay);
        gameHandle.getThread().setName(getThreadName());
        createViews();
    }

    /**
     * 仮想ディスプレイを取得する。
     */
    public VirtualDisplay getVirtualDisplay() {
        return virtualDisplay;
    }

    /**
     * ゲームスレッド名を取得する。
     * DDMSに反映される
     */
    protected String getThreadName() {
        return "GameThread";
    }

    /**
     * マルチタッチデバイスを取得する。<BR>
     * 基本的に二点を扱う。
     */
    public MultiTouchInput getMultiTouchInput() {
        return multiTouchInput;
    }

    /**
     * フレームレートをFPS単位で指定する。
     */
    public void setFrameRateParSec(int frameRate) {
        this.frameRate = frameRate;
    }

    /**
     * 描画用のViewを作成する。
     */
    private void createViews() {
        rootLayout = new FrameLayout(context);
        {
            glView = new OpenGLView(context);
            glView.getHolder().addCallback(new SurfaceHolder.Callback() {
                /**
                 * サーフェイスが破棄された
                 */
                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    (new ThreadSyncRunnerBase<Void>(gameHandle) {
                        @Override
                        public Void onOtherThreadRun() {
                            if (loopParent.isFinished() || lifeCycle == LifeCycle.Finished) {
                                onGamePause();
                                onGameFinalize();
                                System.gc();
                                //                                getGLManager().dispose();
                                egl.dispose();
                                lifeCycle = LifeCycle.Finished;
                                //                                gameHandle.dispose();
                            } else {
                                onGamePause();
                                //                                getGLManager().onPause();
                                lifeCycle = LifeCycle.Paused;
                            }
                            return null;
                        }
                    }).run();
                }

                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                }

                /**
                 * サーフェイスが作成された
                 */
                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    if (paused || lifeCycle == LifeCycle.Finished) {
                        return;
                    }
                    LogUtil.log(String.format("Surface Size : %d x %d", width, height));
                    if (egl.isInitialized()) {
                        //! GLとゲームの復帰を行う
                        (new ThreadSyncRunnerBase<Void>(gameHandle) {
                            @Override
                            public Void onOtherThreadRun() {
                                if (getGLView().isDestroyed()) {
                                    LogUtil.log("ResumeOpenGL");
                                }
                                LogUtil.log("GameResume");
                                onGameResume();
                                lifeCycle = LifeCycle.Running;
                                LogUtil.log("RestartGame");
                                gameHandle.post(frameRunner);
                                return null;
                            }
                        }).run();
                    } else {
                        gameHandle.post(new Runnable() {
                            @Override
                            public void run() {
                                onGameInitialize();
                                lifeCycle = LifeCycle.Running;
                                gameHandle.post(frameRunner);
                            }
                        });
                    }
                }
            });
        }
        {
            canvasView = new CanvasView(context);
            canvasView.setZOrderOnTop(true);
        }
        rootLayout.addView(glView);
        rootLayout.addView(canvasView);
    }

    /**
     * onResumeが呼ばれてからフレームを復帰させるまでの時間
     */
    public void setResumeWaitTime(int resumeWaitTime) {
        this.resumeWaitTime = resumeWaitTime;
    }

    /**
     * GL管理クラスを取得する。
     */
    public GPU getGLManager() {
        return glView.getGLManager();
    }

    /**
     *
     * @return
     */
    public OpenGLView getGLView() {
        return glView;
    }

    /**
     *
     * @return
     */
    public CanvasView getCanvasView() {
        return canvasView;
    }

    /**
     * 結び付けられたコンテキストを取得する。
     */
    public Context getContext() {
        return context;
    }

    /**
     * 描画用のViewを取得する。
     */
    public ViewGroup getRootView() {
        return rootLayout;
    }

    /**
     * 呼び出し元が実装する必要があるinterface
     */
    public interface ILoopParent {
        public boolean isFinished();
    }

    /**
     * 次のフレームの動作を許す場合true
     */
    boolean isNextFrameEnable() {
        updateLifeCycle();
        return lifeCycle == LifeCycle.Running;
    }

    /**
     * ゲームスレッドで動作を行わせる。
     */
    public void post(Runnable runnable) {
        if (gameHandle != null) {
            gameHandle.post(runnable);
        }
    }

    /**
     * ゲームスレッドにPostし、実行が完了するまでロックする。
     */
    public void postWithWait(final Runnable runnable) {
        if (gameHandle != null) {
            (new ThreadSyncRunnerBase<Void>(gameHandle) {
                @Override
                public Void onOtherThreadRun() throws Exception {
                    runnable.run();
                    return null;
                }
            }).run();
        }
    }

    /**
     * UIハンドルで動作を行わせる。
     */
    public void postUIThread(Runnable runnable) {
        if (uiHandle != null) {
            uiHandle.post(runnable);
        }
    }

    /**
     * UIスレッドにPostし、実行が完了するまでロックする。
     */
    public void postUIThreadWithWait(final Runnable runnable) {
        (new ThreadSyncRunnerBase<Void>(uiHandle) {
            @Override
            public Void onOtherThreadRun() throws Exception {
                runnable.run();
                return null;
            }
        }).run();
    }

    /**
     * ライフサイクルの更新を行う。<BR>
     * Activityが閉じられている場合、強制的にステートを切り替える。
     */
    void updateLifeCycle() {
        if (loopParent.isFinished()) {
            lifeCycle = LifeCycle.Finished;
        }
    }

    /**
     * drawableのIDから画像を生成する。
     */
    public Bitmap loadBitmapDrawable(int drawableId) {
        Bitmap image = BitmapFactory.decodeResource(getContext().getResources(), drawableId);
        return image;
    }

    /**
     * drawableのIDから画像を生成する。
     */
    public TextureImageBase loadImageDrawable(int drawableId) {
        Bitmap image = BitmapFactory.decodeResource(getContext().getResources(), drawableId);
        TextureImageBase result = new BitmapTextureImage(image, egl.getVRAM());
        result.setTag("drawable-" + Integer.toHexString(drawableId));
        image.recycle();
        return result;
    }

    /**
     * rawのidから画像を生成する。
     */
    public TextureImageBase loadImageRaw(int rawId) throws IOException {
        InputStream is = getContext().getResources().openRawResource(rawId);
        try {
            Bitmap image = BitmapFactory.decodeStream(is);
            TextureImageBase result = new BitmapTextureImage(image, egl.getVRAM());
            result.setTag("raw-" + Integer.toHexString(rawId));
            image.recycle();
            return result;
        } finally {

            is.close();
        }
    }

    /**
     * assetsのパスから画像を生成する。
     */
    public TextureImageBase loadImageAssets(String assetsPath) throws IOException {
        InputStream is = getContext().getAssets().open(assetsPath);

        try {
            Bitmap image = BitmapFactory.decodeStream(is);
            TextureImageBase result = new BitmapTextureImage(image, egl.getVRAM());
            result.setTag("assets-" + assetsPath);
            image.recycle();
            return result;
        } finally {
            is.close();
        }
    }

    /**
     * フォント描画用のテクスチャを生成して返す。
     */
    public TextureImageBase createFontTexture(String text, int fontSize, int boundsAddX, int boundsAddY) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        paint.setStyle(Style.STROKE);
        Rect bounds = new Rect();
        paint.getTextBounds(text, -1, -1, bounds);

        Bitmap bitmap = Bitmap
                .createBitmap(bounds.width() + boundsAddX, bounds.height() + boundsAddY, Config.ARGB_4444);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawText(text, 0, 0, paint);

        TextureImageBase result = new BitmapTextureImage(bitmap, egl.getVRAM());
        result.setTag("font-" + text + " :: " + fontSize);
        bitmap.recycle();
        return result;
    }

    /**
     * ゲームの初期化を行うフェイズ。
     */
    protected abstract void onGameInitialize();

    /**
     * ゲームの終了処理を行うフェイズ。
     */
    protected abstract void onGameFinalize();

    /**
     * 毎フレームの処理を行う。
     */
    protected abstract void onGameFrame();

    /**
     * ゲームが中断された。
     */
    protected abstract void onGamePause();

    /**
     * ゲームが復帰された。
     */
    protected abstract void onGameResume();

    /**
     * pauseされていたらtrue
     */
    boolean paused = true;

    /**
     * Activity#onPause
     */
    public void onPause() {
        paused = true;
    }

    public boolean isRunning() {
        return lifeCycle == LifeCycle.Running;
    }

    /**
     * Activity#onResume
     */
    public void onResume() {
        paused = false;
    }

    /**
     * Activityを閉じる
     */
    public void onDestroy() {
        lifeCycle = LifeCycle.Finished;
    }

    /**
     * このメソッドをゲーム用スレッドから呼び出している限り、trueを返す。
     */
    public boolean isGameThread() {
        return gameHandle.isHandlerThread();
    }

    /**
     * ゲームループ用のハンドラを取得する。
     */
    public Handler getGameHandler() {
        return gameHandle;
    }

    /**
     * UIハンドラを取得する。
     */
    public UIHandler getUIHandler() {
        return uiHandle;
    }

    /**
     * ゲームを終了させる
     */
    public void exit() {
        uiHandle.post(new Runnable() {

            @Override
            public void run() {
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            }
        });
    }
}
