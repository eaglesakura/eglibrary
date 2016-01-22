package com.eaglesakura.material.widget;

import com.eaglesakura.android.R;

import android.support.v4.app.FragmentTransaction;

/**
 *
 */
public class MaterialAnimationUtil {

    /**
     * Fragmentが上から表示される
     */
    public static FragmentTransaction fragmentFromUpper(FragmentTransaction transaction) {
        transaction.setCustomAnimations(
                R.anim.fragment_fromupper_upper_enter,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_fromupper_upper_exit
        );
        return transaction;
    }

    /**
     * Fragmentが下から表示される
     */
    public static FragmentTransaction fragmentFromLower(FragmentTransaction transaction) {
        transaction.setCustomAnimations(
                R.anim.fragment_fromlower_upper_enter,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_fromlower_upper_exit
        );
        return transaction;
    }

    /**
     * FragmentがRippleで表示される
     * <br>
     * 遷移元のViewと遷移先のFragmentが同じFrameLayoutに格納されていないと、うまく座標が合わないことに注意する。
     */
    public static FragmentTransaction fragmentFromRipple(FragmentTransaction transaction) {
        transaction.setCustomAnimations(
                R.anim.fragment_ripple_upper_enter,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_layer_dummy,
                R.anim.fragment_ripple_upper_exit
        );
        return transaction;
    }
}
