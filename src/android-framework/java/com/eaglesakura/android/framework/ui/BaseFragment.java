package com.eaglesakura.android.framework.ui;

import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.framework.ui.state.IStateful;
import com.eaglesakura.android.framework.util.AppSupportUtil;
import com.eaglesakura.android.oari.ActivityResult;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.android.util.PermissionUtil;

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
public abstract class BaseFragment extends Fragment implements IStateful {

    static final int BACKSTACK_NONE = 0xFEFEFEFE;

    private int backstackIndex = BACKSTACK_NONE;

    @State
    boolean initializedViews = false;

    IStateful.LifecycleState state = LifecycleState.NewObject;

    private boolean injectionViews = false;

    private int injectionLayoutId;

    public void requestInjection(@LayoutRes int layoutId) {
        injectionLayoutId = layoutId;
        injectionViews = (injectionLayoutId != 0);
    }

    @Override
    public LifecycleState getCurrentState() {
        return state;
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
     * 初回のView構築
     */
    protected void onInitializeViews() {
    }

    /**
     * 二度目以降のView構築
     */
    protected void onRestoreViews() {

    }

    /**
     * View構築が完了した
     */
    protected void onAfterViews() {
        if (!initializedViews) {
            onInitializeViews();
            initializedViews = true;
        } else {
            onRestoreViews();
        }
    }

    public boolean isFragmentResumed() {
        return state == LifecycleState.OnResumed;
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
        state = LifecycleState.OnCreated;
    }

    @Override
    public void onStart() {
        super.onStart();
        state = LifecycleState.OnStarted;
    }

    @Override
    public void onResume() {
        super.onResume();
        state = LifecycleState.OnResumed;
    }

    @Override
    public void onPause() {
        super.onPause();
        state = LifecycleState.OnPaused;
    }

    /**
     * backstack idを指定する
     */
    void setBackstackIndex(int backstackIndex) {
        this.backstackIndex = backstackIndex;
    }

    /**
     * Backstackを持つならばtrue
     */
    boolean hasBackstackIndex() {
        return backstackIndex != BACKSTACK_NONE;
    }

    public int getBackstackIndex() {
        return backstackIndex;
    }

    /**
     * バックスタックが一致したらtrue
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
                    getFragmentManager().beginTransaction().remove(BaseFragment.this).commit();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        state = LifecycleState.OnDestroyed;
    }

    public boolean isFragmentDestroyed() {
        return state == LifecycleState.OnDestroyed;
    }

    public boolean isDestroyedView() {
        return isFragmentDestroyed() || getActivity() == null || getView() == null;
    }

    protected boolean hasChildBackStack() {
        return getChildFragmentManager().getBackStackEntryCount() > 0;
    }

    public final String createSimpleTag() {
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

    /**
     * Runtime Permissionの更新を行わせる
     *
     * @return パーミッション取得を開始した場合はtrue
     */
    public boolean requestRuntimePermission(String[] permissions) {
        return AppSupportUtil.requestRuntimePermissions(getActivity(), permissions);
    }

    /**
     * Runtime Permissionのブロードキャストを行わせる
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        AppSupportUtil.onRequestPermissionsResult(getActivity(), requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean requestRuntimePermission(PermissionUtil.PermissionType type) {
        return requestRuntimePermission(type.getPermissions());
    }


    /**
     * UIスレッドで実行する
     */
    protected void runUI(Runnable runnable) {
        UIHandler.postUIorRun(runnable);
    }

    /**
     * バックグラウンドで実行する
     */
    protected void runBackground(Runnable runner) {
        FrameworkCentral.getTaskController().pushBack(runner);
    }
}