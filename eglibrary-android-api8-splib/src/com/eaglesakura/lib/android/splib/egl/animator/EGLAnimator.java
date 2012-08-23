package com.eaglesakura.lib.android.splib.egl.animator;

import android.os.Handler;

import com.eaglesakura.lib.android.game.graphics.gl11.hw.EGLManager;
import com.eaglesakura.lib.android.game.graphics.gl11.hw.GLRenderer;
import com.eaglesakura.lib.android.game.loop.FramerateCounter;
import com.eaglesakura.lib.android.game.thread.UIHandler;
import com.eaglesakura.lib.android.game.util.Timer;
import com.eaglesakura.lib.android.splib.fragment.EGLFragment;
import com.eaglesakura.lib.list.OrderAccessList;
import com.eaglesakura.lib.list.OrderAccessList.Iterator;

public class EGLAnimator implements Runnable {
    /**
     * 実行対象のスレッド。
     * デフォルトはUIハンドラ、別スレッドにも移動可能。
     */
    Handler handler = UIHandler.getInstance();

    EGLFragment fragment;

    /**
     * リアルのフレームレートを数える
     */
    FramerateCounter counter = new FramerateCounter();

    /**
     * delayは30fps程度を基本とする
     */
    int delay = 1000 / 30;

    /**
     * 現在のステップでレンダリングを行わせる場合はtrue
     */
    boolean rendering = false;

    /**
     * サーフェイスエラーが発生した
     */
    boolean surfaceError = false;

    /**
     * 開始済みの場合true
     */
    boolean started = false;

    /**
     * 更新物一覧
     */
    OrderAccessList<Updatable> updatables = new OrderAccessList<Updatable>();

    private GLRenderer worker = new GLRenderer() {
        @Override
        public void onWorking(EGLManager egl) {
            Iterator<Updatable> iterator = updatables.iterator();
            while (iterator.hasNext()) {
                Updatable updatable = iterator.next();
                if (updatable.update(EGLAnimator.this)) {
                    // 更新が完了
                    iterator.remove();
                }
            }
        }

        @Override
        public void onSurfaceReady(EGLManager egl) {
            surfaceError = false;
        }

        @Override
        public void onSurfaceNotReady(EGLManager egl) {
            rendering = false;
            surfaceError = true;
        }

        @Override
        public void onRendering(EGLManager egl) {
        }
    };

    public EGLAnimator(EGLFragment fragment) {
        this.fragment = fragment;
    }

    public void setHandler(Handler handler) {
        if (this.handler != handler && this.handler != null) {
            this.handler.removeCallbacks(this);
        }
        this.handler = handler;
    }

    /**
     * 更新を開始する
     */
    public void start() {
        if (started) {
            return;
        }
        this.handler.post(this);
        started = true;
    }

    /**
     * 更新後のレンダリングをリクエストする
     */
    public void requestRendering() {
        rendering = true;
    }

    /**
     * 更新クラスを追加する
     */
    public EGLAnimator add(Updatable item) {
        if (!updatables.contains(item)) {
            updatables.add(item);
        }
        return this;
    }

    /**
     * アップデータを削除する
     * @param item
     * @return
     */
    public EGLAnimator remove(Updatable item) {
        updatables.remove(item);
        return this;
    }

    /**
     * 通常、外部から呼び出さない
     */
    @Deprecated
    @Override
    public void run() {
        Timer timer = new Timer();

        rendering = false;
        fragment.getEGL().working(worker);

        // リクエストがあればレンダリングを行う
        if (rendering) {
            fragment.renderingNow();
        }

        // サーフェイスエラーが発生していなければtrue
        if (!surfaceError) {
            // エラーが発生している
            long workTime = timer.end();

            // 指定時間の遅延で次のフレームを実行させる
            long nextFrame = Math.max(1, this.delay - workTime);
            handler.postDelayed(this, nextFrame);
        } else {
            started = false;
        }
    }

    public interface Updatable {
        /**
         * 更新を行わせる。
         * trueを返した場合、更新完了とみなしてリストから削除する
         */
        public boolean update(EGLAnimator animator);
    }
}
