package com.eaglesakura.android.debug.window;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.WindowManager;

import com.eaglesakura.android.debug.window.log.DebugItemGroup;
import com.eaglesakura.android.debug.window.log.DebugRenderingItem;
import com.eaglesakura.android.debug.window.log.DebugText;
import com.eaglesakura.android.thread.HandlerLoopController;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.util.Util;

import java.util.Date;

/**
 * 毎フレーム処理のリアルタイムデバッグ出力のサポートを行う
 */
@SuppressLint("NewApi")
public class RealtimeDebugWindow {

//    /**
//     * 上部に描画する
//     */
//    public static final int FLAG_RENDERING_POSITION_TOP = 0x1 << 1;
//
//    /**
//     * 下部に描画する
//     */
//    public static final int FLAG_RENDERING_POSITION_BOTTOM = 0x1 << 2;

    /**
     * 左側に描画する
     */
    public static final int FLAG_RENDERING_POSITION_LEFT = 0x1 << 3;

    /**
     * 右側に描画する
     */
    public static final int FLAG_RENDERING_POSITION_RIGHT = 0x1 << 4;

    final Context context;

    WindowManager windowManager;

    /**
     * レンダリング
     */
    DebugRenderingView renderView;

    /**
     * loop
     */
    HandlerLoopController looper;

    /**
     * 描画対象のデバッグアイテム
     */
    DebugItemGroup debugItems = new DebugItemGroup();

    Object lock = new Object();

    /**
     * 文字を見やすくするための背景色
     */
    int backgroundColor = Color.argb(128, 0, 0, 0);

    public RealtimeDebugWindow(Context context) {
        this.context = context.getApplicationContext();

        windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        looper = new HandlerLoopController(UIHandler.getInstance()) {
            @Override
            protected void onUpdate() {
                requestRendering();
            }
        };
        looper.setFrameRate(10);
    }

    /**
     * リクエストされたメッセージを画面に反映する
     */
    public void postMessages() {
        synchronized (lock) {
            final DebugItemGroup oldDebugItems = this.debugItems;
            // 引き継ぎアイテムを取得して生成する
            this.debugItems = new DebugItemGroup(oldDebugItems.listNonDropItems());

            // UIThreadに投げてそちらで更新を行う
            // 実際の画面反映されるかどうかは、レンダリングタイミングに依存する
            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    if (renderView != null) {
                        renderView.setDebugItemGroup(oldDebugItems);
                    }
                }
            });
        }
    }

    /**
     * デバッグ用メッセージを投げる
     *
     * @param item
     */
    public void addMessage(DebugRenderingItem item) {
        synchronized (lock) {
            this.debugItems.add(item);
        }
    }

    /**
     * デバッグ描画のフレームレートを指定する
     * <p/>
     * デフォルトは10fps
     *
     * @param rate
     */
    public void setFrameRate(int rate) {
        looper.setFrameRate(rate);
    }

    public void connect() {
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                // レイアウトの幅 / 高さ設定
                size.x, size.y,
                // レイアウトの挿入位置設定
                // TYPE_SYSTEM_OVERLAYはほぼ最上位に位置して、ロック画面よりも上に表示される。
                // ただし、タッチを拾うことはできない。
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON    // スクリーン表示Keep
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//                            | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                // 透過属性を持たなければならないため、TRANSLUCENTを利用する
                PixelFormat.TRANSLUCENT);

        renderView = new DebugRenderingView(context);
        windowManager.addView(renderView, params);

        looper.connect();
//        new Thread() {
//            @Override
//            public void run() {
//                int loop = 0;
//                while (renderView != null) {
//                    addMessage(new DebugText("Loop :: " + loop++));
//                    {
//                        DebugText text = new DebugText("Time :: " + System.currentTimeMillis());
//                        text.setTextHeightPixel(84);
//                        text.setRenderingTime(200);
//                        text.setFlags(FLAG_RENDERING_POSITION_RIGHT);
//                        addMessage(text);
//                    }
//                    postMessages();
//                    Util.sleep(1000 / 60);
//                }
//            }
//        }.start();
    }

    public void disconnect() {
        if (renderView != null) {
            windowManager.removeView(renderView);
            renderView = null;
        }

        looper.disconnect();
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * レンダリングを行わせる
     */
    private void requestRendering() {
        if (renderView != null) {
            renderView.setDebugBackgroundColor(backgroundColor);
            renderView.invalidate();
        }
    }
}