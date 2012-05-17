package com.eaglesakura.lib.android.splib.activity;

import android.app.Activity;
import android.view.Window;

import com.eaglesakura.lib.android.game.util.ContextUtil;

public class ActionBarSupport {
    Activity activity = null;

    public ActionBarSupport(Activity activity) {
        this.activity = activity;
    }

    public void show() {
        if (ContextUtil.isActionBarEnable()) {
            activity.getActionBar().show();
        }
    }

    public void hide() {
        if (ContextUtil.isActionBarEnable()) {
            activity.getActionBar().hide();
        }
    }

    /**
     * アクションバーが非対応だったらタイトル画像を隠す
     */
    public void hideTitleActionBarNotSupport() {
        if (!ContextUtil.isActionBarEnable()) {
            activity.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
    }
}
