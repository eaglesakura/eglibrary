package com.eaglesakura.android.framework.support.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.eaglesakura.android.framework.support.ui.playservice.GoogleApiClientToken;
import com.eaglesakura.android.framework.support.ui.playservice.GoogleApiTask;
import com.eaglesakura.android.util.ContextUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

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

    protected static final int REQUEST_GOOGLEPLAYSERVICE_RECOVER = 0x1100;

    protected UserNotificationController userNotificationController = new UserNotificationController(this);

    protected GoogleApiClientToken googleApiClientToken;

    protected boolean activityResumed;

    protected boolean playServiceCheck = false;

    protected BaseActivity() {
        fragments.setCallback(this);
    }

    public void setGoogleApiClientToken(GoogleApiClientToken googleApiClientToken) {
        if (this.googleApiClientToken != null) {
            this.googleApiClientToken.unlock();
        }
        this.googleApiClientToken = googleApiClientToken;
        if (this.googleApiClientToken != null) {
            this.googleApiClientToken.lock();
        }
    }

    public GoogleApiClientToken getGoogleApiClientToken() {
        return googleApiClientToken;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClientToken != null) {
            googleApiClientToken.startInitialConnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityResumed = true;

        if (playServiceCheck) {
            final int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            if (statusCode != ConnectionResult.SUCCESS) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode, this, REQUEST_GOOGLEPLAYSERVICE_RECOVER, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onGooglePlayServiceRecoverCanceled(statusCode);
                    }
                });
                dialog.show();
            } else {
                log("Google Play Service OK!");
            }
        }
    }

    /**
     * Google Play Serviceの復旧を取りやめた場合
     *
     * @param errorCode
     */
    protected void onGooglePlayServiceRecoverCanceled(int errorCode) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityResumed = false;
    }

    public boolean isActivityResumed() {
        return activityResumed;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClientToken != null) {
            googleApiClientToken.unlock();
        }
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
    public <T> T executeGoogleApi(final GoogleApiTask<T> task) {
        return googleApiClientToken.executeGoogleApi(task);
    }

}
