package com.eaglesakura.android.framework.ui;

import android.app.Fragment;

/**
 * Fragmentの遅延生成を行わせるクラス
 */
public abstract class FragmentCreater {
    protected Class<? extends Fragment> clazz;

    public abstract Fragment newFragment();
}
