package com.eaglesakura.material.widget.support.header;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.eaglesakura.android.util.ViewUtil;
import com.eaglesakura.util.LogUtil;

/**
 * スクロールにリンクしてヘッダ用のView位置や透過等を制御する
 * <p/>
 * Scroll本体とヘッダはFrameLayoutの前後になるようにして、お互いの影響を与えないように注意する。
 */
public class ScrollLinkHeaderController {
    /**
     * ScrollView等でスペーサーとして機能させる空のView
     * <p/>
     * ViewやFrameLayout等で位置指定に使用する
     */
    View spacerView;

    /**
     * ヘッダとして使用するView
     */
    View headerViewContainer;

    /**
     * ヘッダを見せている状態の高さ
     */
    int openHeight;

    /**
     * ヘッダを閉じている状態の高さ
     */
    int closeHeight = 0;

    Toolbar toolbar;

    /**
     * ツールバーの背景色(RGB)
     */
    int toolbarBackgroundRGB;

    /**
     * ヘッダスクロールの遅延レベル
     */
    float headerScrollDelay = 0.25f;

    private final Context context;

    public ScrollLinkHeaderController(Context context) {
        this.context = context.getApplicationContext();
    }

    public void setHeaderView(final View headerView, final View spacerView) {
        this.headerViewContainer = headerView;
        this.spacerView = spacerView;
        openHeight = headerView.getMeasuredHeight();

        // 高さを合わせる
        headerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (openHeight != headerViewContainer.getHeight()) {
                    onLayoutChanged(0);
                }
            }
        });
    }

    /**
     * ヘッダの高さ等が更新された場合に呼び出して再度指定する
     */
    public void onLayoutChanged(int scroll) {
        openHeight = headerViewContainer.getHeight();
        ViewUtil.setViewHeight(spacerView, openHeight);
        onScrollY(scroll);
    }

    /**
     * リンクさせるツールバーを指定する
     *
     * @param toolbar
     */
    public void setToolbar(Toolbar toolbar) {
        this.toolbar = toolbar;
        closeHeight = toolbar.getHeight();
        toolbar.setBackgroundColor(0);
    }

    /**
     * ヘッダスクロールの遅延値を指定する
     *
     * @param headerScrollDelay
     */
    public void setHeaderScrollDelay(float headerScrollDelay) {
        this.headerScrollDelay = headerScrollDelay;
    }

    /**
     * Toolbarの背景色を指定する
     *
     * @param toolbarBackgroundRGB
     */
    public void setToolbarBackgroundXRGB(int toolbarBackgroundRGB) {
        this.toolbarBackgroundRGB = toolbarBackgroundRGB;
    }

    public void setOpenHeight(int openHeight) {
        this.openHeight = openHeight;
    }

    /**
     * 閉じた状態の高さを指定する
     *
     * @param closeHeight
     */
    public void setCloseHeight(int closeHeight) {
        this.closeHeight = closeHeight;
    }

    /**
     * Y座標がスクロールされたため、コールバックする
     *
     * @param currentY
     */
    @SuppressLint("NewApi")
    public void onScrollY(int currentY) {
        // オーバースクロールには対応しない
        currentY = Math.max(0, currentY);
        LogUtil.log("onScrollY(%d)", currentY);

        // スクロール量に合わせてヘッダを透過する
        float scrollLevel = Math.min(1, (float) currentY / (float) openHeight);
        if (Build.VERSION.SDK_INT >= 14) {
            headerViewContainer.setAlpha(1.0f - scrollLevel);
        }

        // 適度にスクロールさせる
        {
            int scrollOffset = (openHeight - closeHeight);
            int viewScrollY = (int) (scrollLevel * headerScrollDelay * scrollOffset);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) headerViewContainer.getLayoutParams();
            layoutParams.topMargin = -viewScrollY;
            headerViewContainer.setLayoutParams(layoutParams);
        }
    }
}
