package com.eaglesakura.android.framework.ui.content;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Support FragmentでAnimationを行うためのLayout
 */
public class ContentHolderLayout extends FrameLayout {
    public ContentHolderLayout(Context context) {
        super(context);
    }

    public ContentHolderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ContentHolderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("all")
    public ContentHolderLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint("all")
    public float getXFraction() {
        final int width = getWidth();
        if (width != 0) return getX() / getWidth();
        else return getX();
    }

    @SuppressLint("all")
    public void setXFraction(float xFraction) {
        final int width = getWidth();
        setX((width > 0) ? (xFraction * width) : -9999);
    }
}
