package com.eaglesakura.android.framework.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.widget.Toast;

import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

/**
 *
 */
@EFragment
public abstract class BaseFragment extends Fragment {

    public static final int BACKSTACK_NONE = 0xFEFEFEFE;

    private int backstackId = BACKSTACK_NONE;

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
     * @param backstackId
     */
    public void setBackstackId(int backstackId) {
        this.backstackId = backstackId;
    }

    /**
     * Backstackを持つならばtrue
     *
     * @return
     */
    public boolean hasBackstackId() {
        return backstackId != BACKSTACK_NONE;
    }

    /**
     * 自身をFragmentから外す
     *
     * @param withBackStack backstack階層も含めて排除する場合はtrue
     */
    @UiThread
    public void detatchSelf(boolean withBackStack) {
        if (withBackStack && hasBackstackId()) {
            getFragmentManager().popBackStack(backstackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            getFragmentManager().beginTransaction().remove(this).commit();
        }
    }
}