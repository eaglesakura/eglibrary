package com.eaglesakura.android.framework.support.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.androidannotations.annotations.EView;

/**
 *
 */
public abstract class BaseView extends View {

    protected BaseView(Context context) {
        super(context);
    }

    protected BaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected BaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("NewApi")
    public BaseView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void log(String fmt, Object... args) {
        Log.i(((Object) this).getClass().getSimpleName(), String.format(fmt, args));
    }

    protected void logi(String fmt, Object... args) {
        Log.i(((Object) this).getClass().getSimpleName(), String.format(fmt, args));
    }

    protected void logd(String fmt, Object... args) {
        Log.d(((Object) this).getClass().getSimpleName(), String.format(fmt, args));
    }
}
