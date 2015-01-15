package com.eaglesakura.android.framework.support.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.eaglesakura.android.framework.support.ui.playservice.GoogleApiTask;
import com.eaglesakura.android.util.AndroidUtil;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.util.LogUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;

import java.util.List;

/**
 *
 */
@EActivity
public abstract class BaseActivity extends ActionBarActivity implements FragmentChooser.Callback {

    protected UserNotificationController userNotificationController = new UserNotificationController(this);

    protected BaseActivity() {
        fragments.setCallback(this);
    }

    @AfterViews
    protected void onAfterViews() {

    }

    protected void log(String fmt, Object... args) {
        Log.i(((Object) this).getClass().getSimpleName(), String.format(fmt, args));
    }

    protected void logi(String fmt, Object... args) {
        Log.i(((Object) this).getClass().getSimpleName(), String.format(fmt, args));
    }

    protected void logd(String fmt, Object... args) {
        Log.d(((Object) this).getClass().getSimpleName(), String.format(fmt, args));
    }

    public UserNotificationController getUserNotificationController() {
        return userNotificationController;
    }

    @UiThread
    protected void toast(String fmt, Object... args) {
        userNotificationController.toast(this, String.format(fmt, args));
    }

    /**
     * show toast
     *
     * @param resId
     */
    protected void toast(int resId) {
        toast(getString(resId));
    }

    /**
     * show progress dialog
     *
     * @param stringId
     */
    public void pushProgress(int stringId) {
        pushProgress(getString(stringId));
    }

    /**
     * 何らかの処理中であればtrue
     *
     * @return
     */
    public boolean isProgressing() {
        return userNotificationController.isProgressing();
    }

    /**
     * 処理を開始する
     *
     * @param message
     */
    @UiThread
    public void pushProgress(String message) {
        userNotificationController.pushProgress(this, message);
    }

    /**
     * 処理を終了する
     */
    @UiThread
    public void popProgress() {
        userNotificationController.popProgress(this);
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
        return getSupportFragmentManager();
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

    /**
     * Google Apiの実行を行う。
     * <p/>
     * 裏スレッドから呼び出さなくてはならない。
     *
     * @param task
     */
    public <T> T executeGoogleApi(GoogleApiTask<T> task, GoogleApiClient client) {
        if (AndroidUtil.isUIThread()) {
            throw new IllegalStateException();
        }

        if (task.isCanceled()) {
            return null;
        }

        client.blockingConnect();
        try {
            if (!client.isConnected()) {
                ConnectionResult result = client.blockingConnect();
                return task.connectedFailed(client, result);
            }
            if (task.isCanceled()) {
                return null;
            }

            return task.executeTask(client);
        } catch (Exception e) {
            LogUtil.log(e);
            return null;
        }
    }

}
