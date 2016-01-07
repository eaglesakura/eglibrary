package com.eaglesakura.android.framework.support.ui.butterknife;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * OnAfterViewsのハンドリングを行う
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface OnActivityResult {
    int value();
}
