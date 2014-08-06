package com.eaglesakura.android.framework.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

import com.eaglesakura.android.framework.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment Transactionを制御する
 */
public class FragmentTransactionBuilder {
    public enum AnimationType {
        /**
         * 新しいFragmentが右から下に移動する
         */
        TranslateHorizontal,

        /**
         * 新しいFragmentが下から上にせり上がる
         */
        TranslateUp,

        /**
         *
         */
        TranslateDown,

        /**
         * 通常のフェード
         */
        Fade,

        /**
         * アニメーションなし
         */
        None,
    }

    protected final Activity activity;

    protected final Fragment fragment;

    protected final FragmentTransaction transaction;

    protected final FragmentManager fragmentManager;

    /**
     * バックスタック数を数え、不要であればanimationを行わない
     */
    protected boolean checkBackstackCount = true;

    /**
     * コミット対象のFragments
     */
    protected List<Fragment> fragments = new ArrayList<Fragment>();

    public FragmentTransactionBuilder(Fragment currentFragment, FragmentManager fragmentManager) {
        this.fragment = currentFragment;
        this.activity = null;
        this.fragmentManager = fragmentManager;
        this.transaction = fragmentManager.beginTransaction();
    }

    public FragmentTransactionBuilder(Activity activity, FragmentManager fragmentManager) {
        this.fragment = null;
        this.activity = activity;
        this.fragmentManager = fragmentManager;
        this.transaction = fragmentManager.beginTransaction();
    }

    public FragmentTransactionBuilder checkBackstackCount(boolean checkBackstackCount) {
        this.checkBackstackCount = checkBackstackCount;
        return this;
    }

    public FragmentTransactionBuilder animation(int enter, int exit) {
        animation(enter, exit, 0, 0);
        return this;
    }

    public FragmentTransactionBuilder animation(int enter, int exit,
                                                int popEnter, int popExit) {
        if (checkBackstackCount) {
            if (fragmentManager.getBackStackEntryCount() == 0) {
                return this;
            }
        }

        if (popEnter == 0 || popExit == 0) {
            transaction.setCustomAnimations(enter, exit);
        } else {
            transaction.setCustomAnimations(enter, exit, popEnter, popExit);
        }
        return this;
    }

    public FragmentTransactionBuilder animation(AnimationType type) {
        return animation(type, type);
    }

    /**
     * setup animation
     *
     * @param enterAnimation
     * @param popAnimation
     * @return
     */
    public FragmentTransactionBuilder animation(AnimationType enterAnimation, AnimationType popAnimation) {
        int enter = 0;
        int exit = 0;
        int popEnter = 0;
        int popExit = 0;

        switch (enterAnimation) {
            case Fade:
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                return this;
            case TranslateHorizontal:
                enter = R.animator.fragment_horizontal_enter;
                exit = R.animator.fragment_horizontal_exit;
                break;
        }

        if (popAnimation != null) {
            switch (popAnimation) {
                case TranslateHorizontal:
                    popEnter = R.animator.fragment_horizontal_popenter;
                    popExit = R.animator.fragment_horizontal_popexit;
                    break;
            }
        }

        return animation(enter, exit, popEnter, popExit);
    }

    /**
     * 指定したコンテナに移動する
     *
     * @param container
     */
    public FragmentTransactionBuilder replace(int container, Fragment fragment) {
        transaction.replace(container, fragment, fragment.getClass().getName());
        fragments.add(fragment);
        return this;
    }

    public FragmentTransactionBuilder addToBackStack() {
        transaction.addToBackStack(null);
        return this;
    }

    /**
     * 内容のコミットを行う
     */
    public void commit() {
        int backStackId = transaction.commit();
        for (Fragment frag : fragments) {
            if (frag instanceof BaseFragment) {
                ((BaseFragment) frag).setBackstackId(backStackId);
            }
        }
    }
}
