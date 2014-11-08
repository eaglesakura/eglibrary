package com.eaglesakura.android.framework.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;

import java.util.List;

/**
 *
 */
@EActivity
@Deprecated
public abstract class BaseActivity extends Activity implements FragmentChooser.Callback {

    protected BaseActivity() {
        fragments.setCallback(this);
    }

    @AfterViews
    protected void onAfterViews() {

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

    @UiThread
    protected void toast(String msg) {
        if (StringUtil.isEmpty(msg)) {
            LogUtil.log("message is empty");
            return;
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * show toast
     *
     * @param resId
     */
    protected void toast(int resId) {
        toast(getString(resId));
    }

    private int progressNum = 0;
    private ProgressDialog progressDialog = null;

    /**
     * show progress dialog
     *
     * @param stringId
     */
    public void pushProgress(int stringId) {
        pushProgress(getString(stringId));
    }

    /**
     * 処理を開始する
     *
     * @param message
     */
    @UiThread
    public void pushProgress(String message) {
        if (progressNum == 0) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            progressDialog.setMessage(message);
        }
        ++progressNum;
    }

    /**
     * 処理を終了する
     */
    @UiThread
    public void popProgress() {
        --progressNum;

        if (progressNum <= 0) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            progressNum = 0;
        }
    }

    @InstanceState
    protected FragmentChooser fragments = new FragmentChooser();

    /**
     * Fragmentがアタッチされたタイミングで呼び出される。
     * <p/>
     * このFragmentは最上位階層のみが扱われる。
     *
     * @param fragment
     */
    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);

        // キャッシュに登録する
        fragments.compact();
        fragments.addFragment(FragmentChooser.ReferenceType.Weak, fragment, fragment.getTag(), 0);
    }

    /**
     * キーイベントハンドリングを行う
     *
     * @param event
     * @return ハンドリングを行えたらtrue
     */
    protected boolean handleFragmentsKeyEvent(KeyEvent event) {
        if (!ContextUtil.isBackKeyEvent(event)) {
            return false;
        }

        List<Fragment> list = fragments.listExistFragments();
        for (Fragment frag : list) {
            if (frag.isVisible() && frag instanceof BaseFragment) {
                if (((BaseFragment) frag).handleBackButton()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (handleFragmentsKeyEvent(event)) {
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    public FragmentManager getFragmentManager(FragmentChooser chooser) {
        return getFragmentManager();
    }

    @Override
    public Fragment newFragment(FragmentChooser chooser, String requestTag) {
        return null;
    }

    @Override
    public boolean isFragmentExist(FragmentChooser chooser, Fragment fragment) {
        if (fragment == null) {
            return false;
        }

        if (fragment instanceof BaseFragment) {
            // 廃棄済みはさっさと排除する
            if (((BaseFragment) fragment).isDestroyed()) {
                return false;
            }
        }

        return true;
    }
}
