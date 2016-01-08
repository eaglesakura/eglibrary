package com.eaglesakura.android.framework.support.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.oari.ActivityResult;
import com.eaglesakura.android.framework.support.ui.message.LocalMessageReceiver;
import com.eaglesakura.android.framework.support.ui.playservice.GoogleApiClientToken;
import com.eaglesakura.android.framework.support.ui.playservice.GoogleApiTask;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.util.LogUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;


import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

/**
 * startActivityForResultを行う場合、ParentFragmentが存在していたらそちらのstartActivityForResultを呼び出す。
 * <br>
 * これはchildFragmentの場合にonActivityResultが呼ばれない不具合を可能な限り回避するため。
 * <br>
 * ただし、複数のonActivityResultがハンドリングされる恐れが有るため、RequestCodeの重複には十分に注意すること
 */
public abstract class BaseFragment extends Fragment {

    public static final int BACKSTACK_NONE = 0xFEFEFEFE;

    boolean destroyed = false;

    private int backstackIndex = BACKSTACK_NONE;

    private LocalMessageReceiver localMessageReceiver;

    @State
    boolean initializedViews = false;

    boolean fragmentResumed = false;

    private boolean injectionViews = false;

    private int injectionLayoutId;

    public void requestInjection(@LayoutRes int layoutId) {
        injectionLayoutId = layoutId;
        injectionViews = (injectionLayoutId != 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (injectionViews) {
            View result = inflater.inflate(injectionLayoutId, container, false);
            ButterKnife.bind(this, result);
            // getView対策で、１クッション置いて実行する
            UIHandler.postUI(new Runnable() {
                @Override
                public void run() {
                    onAfterViews();
                }
            });
            return result;
        } else {
            return null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (injectionViews) {
            ButterKnife.unbind(this);
        }
    }

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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }
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

    protected BaseFragment self() {
        return this;
    }

    protected void toast(final String fmt, final Object... args) {
        UIHandler.postUIorRun(new Runnable() {
            @Override
            public void run() {
                try {
                    ((BaseActivity) getActivity()).getUserNotificationController().toast(self(), String.format(fmt, args));
                } catch (Exception e) {
                }
            }
        });

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
    protected void pushProgress(final String message) {
        runUI(new Runnable() {
            @Override
            public void run() {
                try {
                    ((BaseActivity) getActivity()).getUserNotificationController().pushProgress(self(), message);
                } catch (Exception e) {
                }
            }
        });
    }

    /**
     * progress dialogを一段階引き下げる
     */
    protected void popProgress() {
        runUI(new Runnable() {
            @Override
            public void run() {
                try {
                    ((BaseActivity) getActivity()).getUserNotificationController().popProgress(self());
                } catch (Exception e) {

                }
            }
        });
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
    public void detatchSelf(final boolean withBackStack) {
        UIHandler.postUIorRun(new Runnable() {
            @Override
            public void run() {
                if (withBackStack && hasBackstackIndex()) {
                    getFragmentManager().popBackStack(backstackIndex, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                } else {
                    getFragmentManager().beginTransaction().remove(self()).commit();
                }
            }
        });
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
        ActivityResult.invokeRecursive(this, requestCode, resultCode, data);
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

    public boolean requestRuntimePermission(PermissionUtil.PermissionType type) {
        return requestRuntimePermission(type.getPermissions());
    }


    /**
     * UIスレッドで実行する
     *
     * @param runnable
     */
    protected void runUI(Runnable runnable) {
        UIHandler.postUIorRun(runnable);
    }

    /**
     * バックグラウンドで実行する
     *
     * @param runner
     */
    protected void runBackground(Runnable runner) {
        FrameworkCentral.getTaskController().pushBack(runner);
    }
}