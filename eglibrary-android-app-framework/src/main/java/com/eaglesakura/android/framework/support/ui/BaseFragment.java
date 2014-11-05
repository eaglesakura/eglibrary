package com.eaglesakura.android.framework.support.ui;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;

/**
 *
 */
@EFragment
public abstract class BaseFragment extends Fragment {

    public static final int BACKSTACK_NONE = 0xFEFEFEFE;

    boolean destroyed = false;

    private int backstackIndex = BACKSTACK_NONE;

    @InstanceState
    protected boolean initializedViews = false;

    /**
     * 一意に識別可能なタグを生成する
     *
     * @return
     */
    public String genTag() {
        return getClass().getName() + "/" + hashCode();
    }

    /**
     * 初回のみ呼び出される
     */
    protected void onInitializeViews() {

    }

    /**
     * レストアを行う
     */
    protected void onRestoreViews() {

    }

    @AfterViews
    protected void onAfterViews() {
        if (!initializedViews) {
            onInitializeViews();
            initializedViews = true;
        } else {
            onRestoreViews();
        }
    }

    @UiThread
    protected void toast(String msg) {
        if (StringUtil.isEmpty(msg)) {
            LogUtil.log("message is empty");
            return;
        }
        Toast.makeText(FrameworkCentral.getApplication(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * toastを表示する
     *
     * @param resId
     */
    protected void toast(int resId) {
        toast(FrameworkCentral.getApplication().getString(resId));
    }

    /**
     * Progress Dialogを表示する
     *
     * @param stringId
     */
    protected void pushProgress(int stringId) {
        pushProgress(getString(stringId));
    }

    /**
     * progress dialogを表示する
     *
     * @param message
     */
    protected void pushProgress(String message) {
        try {
            ((BaseActivity) getActivity()).pushProgress(message);
        } catch (Exception e) {

        }
    }

    /**
     * progress dialogを一段階引き下げる
     */
    protected void popProgress() {
        try {
            ((BaseActivity) getActivity()).popProgress();
        } catch (Exception e) {

        }
    }

    /**
     * backstack idを指定する
     *
     * @param backstackIndex
     */
    public void setBackstackIndex(int backstackIndex) {
        this.backstackIndex = backstackIndex;
    }

    /**
     * Backstackを持つならばtrue
     *
     * @return
     */
    public boolean hasBackstackIndex() {
        return backstackIndex != BACKSTACK_NONE;
    }

    public int getBackstackIndex() {
        return backstackIndex;
    }

    /**
     * バックスタックが一致したらtrue
     *
     * @return
     */
    @SuppressLint("NewApi")
    public boolean isCurrentBackstack() {
        if (!ContextUtil.supportedChildFragmentManager() || getParentFragment() == null) {
            return backstackIndex == getFragmentManager().getBackStackEntryCount();
        } else {
            return backstackIndex == getParentFragment().getFragmentManager().getBackStackEntryCount();
        }
    }

    /**
     * 自身をFragmentから外す
     *
     * @param withBackStack backstack階層も含めて排除する場合はtrue
     */
    @UiThread
    public void detatchSelf(boolean withBackStack) {
        if (withBackStack && hasBackstackIndex()) {
            getFragmentManager().popBackStack(backstackIndex, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            getFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    protected void log(String fmt, Object... args) {
        Log.i(getClass().getSimpleName(), String.format(fmt, args));
    }

    protected void logi(String fmt, Object... args) {
        Log.i(getClass().getSimpleName(), String.format(fmt, args));
    }

    protected void logd(String fmt, Object... args) {
        Log.d(getClass().getSimpleName(), String.format(fmt, args));
    }

    @SuppressLint("NewApi")
    protected boolean hasChildBackStack() {
        return getChildFragmentManager().getBackStackEntryCount() > 1;
    }

    /**
     * 戻るボタンのハンドリングを行う
     *
     * @return ハンドリングを行えたらtrue
     */
    @SuppressLint("NewApi")
    public boolean handleBackButton() {
        if (!ContextUtil.supportedChildFragmentManager()) {
            // no backstack
            return false;
        }

        if (hasChildBackStack()) {
            // backStackを解放する
            getChildFragmentManager().popBackStack();
            return true;
        } else {
            return false;
        }
    }
}