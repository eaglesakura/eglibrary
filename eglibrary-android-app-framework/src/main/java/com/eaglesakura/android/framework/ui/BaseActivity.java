package com.eaglesakura.android.framework.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.view.KeyEvent;
import android.widget.Toast;

import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 */
@EActivity
public abstract class BaseActivity extends Activity {

    @AfterViews
    protected void onAfterViews() {

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

    List<WeakReference<Fragment>> fragments = new ArrayList<WeakReference<Fragment>>();

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

        fragments.add(0, new WeakReference<Fragment>(fragment));
    }

    /**
     * フラグメント一覧を取得する
     */
    public Set<Fragment> listFragments() {
        Set<Fragment> result = new HashSet<Fragment>();

        Iterator<WeakReference<Fragment>> iterator = fragments.iterator();
        while (iterator.hasNext()) {
            WeakReference<Fragment> reference = iterator.next();

            // 生存チェック
            Fragment frag = reference.get();
            if (frag != null && !frag.isVisible()) {
                frag = null;
            }

            if (frag != null) {
                result.add(frag);
            } else if (reference.get() == null) {
                // リファレンスが死んでいる場合は効率化のために排除する
                iterator.remove();
            }
        }

        return result;
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

        Set<Fragment> list = listFragments();
        for (Fragment frag : list) {
            if (frag instanceof BaseFragment) {
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
}
