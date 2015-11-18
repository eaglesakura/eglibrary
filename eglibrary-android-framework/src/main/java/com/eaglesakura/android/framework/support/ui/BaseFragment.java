package com.eaglesakura.android.framework.support.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.support.ui.message.LocalMessageReceiver;
import com.eaglesakura.android.framework.support.ui.playservice.GoogleApiClientToken;
import com.eaglesakura.android.framework.support.ui.playservice.GoogleApiTask;
import com.eaglesakura.android.thread.UIHandler;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.android.util.PackageUtil;
import com.eaglesakura.util.LogUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;

import java.util.List;

/**
 * startActivityForResultを行う場合、ParentFragmentが存在していたらそちらのstartActivityForResultを呼び出す。
 * <br>
 * これはchildFragmentの場合にonActivityResultが呼ばれない不具合を可能な限り回避するため。
 * <br>
 * ただし、複数のonActivityResultがハンドリングされる恐れが有るため、RequestCodeの重複には十分に注意すること
 */
@EFragment
public abstract class BaseFragment extends Fragment {

    public static final int BACKSTACK_NONE = 0xFEFEFEFE;

    boolean destroyed = false;

    private int backstackIndex = BACKSTACK_NONE;

    private LocalMessageReceiver localMessageReceiver;

    @InstanceState
    protected boolean initializedViews = false;

    protected boolean fragmentResumed = false;

    public <T extends View> T findViewById(Class<T> clazz, int id) {
        return (T) getView().findViewById(id);
    }

    public <T extends View> T findViewByIdFromActivity(Class<T> clazz, int id) {
        return (T) getActivity().findViewById(id);
    }

    /**
     * ActionBarを取得する
     *
     * @return
     */
    public ActionBar getActionBar() {
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            return ((AppCompatActivity) activity).getSupportActionBar();
        } else {
            return null;
        }
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

        if (localMessageReceiver == null) {
            localMessageReceiver = newLocalMessageReceiver();
        }
    }

    protected LocalMessageReceiver newLocalMessageReceiver() {
        return null;
    }

    public boolean isFragmentResumed() {
        return fragmentResumed;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (localMessageReceiver != null) {
            localMessageReceiver.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fragmentResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        fragmentResumed = false;
    }


    @UiThread
    protected void toast(String fmt, Object... args) {
        try {
            ((BaseActivity) getActivity()).getUserNotificationController().toast(this, String.format(fmt, args));
        } catch (Exception e) {
        }

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
    @UiThread
    protected void pushProgress(String message) {
        try {
            ((BaseActivity) getActivity()).getUserNotificationController().pushProgress(this, message);
        } catch (Exception e) {
        }
    }

    /**
     * progress dialogを一段階引き下げる
     */
    @UiThread
    protected void popProgress() {
        try {
            ((BaseActivity) getActivity()).getUserNotificationController().popProgress(this);
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
        if (localMessageReceiver != null) {
            localMessageReceiver.disconnect();
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isDestroyedView() {
        return isDestroyed() || getActivity() == null || getView() == null;
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

    protected boolean hasChildBackStack() {
        return getChildFragmentManager().getBackStackEntryCount() > 0;
    }

    final public String createSimpleTag() {
        return ((Object) this).getClass().getSimpleName();
    }

    /**
     * 戻るボタンのハンドリングを行う
     *
     * @return ハンドリングを行えたらtrue
     */
    public boolean handleBackButton() {
        if (hasChildBackStack()) {
            // backStackを解放する
            getChildFragmentManager().popBackStack();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (getParentFragment() != null) {
            getParentFragment().startActivityForResult(intent, requestCode);
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<Fragment> fragments = getChildFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    public <T> T executeGoogleApi(GoogleApiTask<T> task) {
        Activity activity = getActivity();
        if (activity == null || !(activity instanceof BaseActivity)) {
            return null;
        }
        return ((BaseActivity) activity).executeGoogleApi(task);
    }

    public void executeGoogleApiUiThread(final GoogleApiTask<?> task) {
        executeGoogleApi(new GoogleApiTask<Object>() {
            @Override
            public Object executeTask(final GoogleApiClient client) throws Exception {
                UIHandler.postUI(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            task.executeTask(client);
                        } catch (Exception e) {
                            LogUtil.log(e);
                        }
                    }
                });
                return null;
            }

            @Override
            public Object connectedFailed(final GoogleApiClient client, final ConnectionResult connectionResult) {
                UIHandler.postUI(new Runnable() {
                    @Override
                    public void run() {
                        task.connectedFailed(client, connectionResult);
                    }
                });
                return null;
            }

            @Override
            public boolean isCanceled() {
                return task.isCanceled();
            }
        });
    }

    public GoogleApiClientToken getGoogleApiClientToken() {
        Activity activity = getActivity();
        if (activity == null || !(activity instanceof BaseActivity)) {
            return null;
        }

        return ((BaseActivity) activity).getGoogleApiClientToken();
    }

    /**
     * Runtime Permissionの更新を行わせる
     *
     * @param permissions
     * @return パーミッション取得を開始した場合はtrue
     */
    public boolean requestRuntimePermission(String[] permissions) {
        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            return ((BaseActivity) activity).requestRuntimePermissions(permissions);
        } else {
            return false;
        }
    }

    public boolean requestRuntimePermission(PackageUtil.PermissionType type) {
        return requestRuntimePermission(type.getPermissions());
    }
}