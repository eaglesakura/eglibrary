package com.eaglesakura.lib.android.splib.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;

/**
 * Intentを利用してFragmentを構築する。
 * @author TAKESHI YAMASHITA
 *
 */
public class IntentFragment extends Fragment {
    /**
     * 保持するIntent。
     * 自動でsave／restoreされる
     */
    protected Intent intent = null;

    /**
     * セーブ時の名称
     */
    protected static String SAVE_INTENT = "SAVE_" + IntentFragment.class.getName();

    /**
     * 何も行わずに初期化する
     */
    public IntentFragment() {
        setIntent(new Intent());
    }

    /**
     * 初期値を設定して初期化する
     * @param intent
     */
    public IntentFragment(Intent intent) {
        setIntent(intent);
    }

    /**
     * Intentを取得する。
     * @return
     */
    public Intent getIntent() {
        return intent;
    }

    /**
     * Intentを新しく入れ替える。
     * @param intent
     */
    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    /**
     * 値のセーブを行う
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_INTENT, intent);
    }

    /**
     * 値の復旧を行う
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // intentが付属していたら読み込む。
        if (savedInstanceState != null) {
            Parcelable parcelable = savedInstanceState.getParcelable(SAVE_INTENT);

            if (parcelable != null) {
                setIntent((Intent) parcelable);
            }
        }
    }
}
