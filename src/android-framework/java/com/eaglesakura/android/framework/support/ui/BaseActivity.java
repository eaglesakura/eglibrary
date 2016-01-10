package com.eaglesakura.android.framework.support.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import com.eaglesakura.android.R;
import com.eaglesakura.android.thread.async.AsyncTaskController;
import com.eaglesakura.android.thread.async.AsyncTaskResult;
import com.eaglesakura.android.framework.FrameworkCentral;
import com.eaglesakura.android.oari.ActivityResult;
import com.eaglesakura.android.framework.support.ui.message.LocalMessageReceiver;
import com.eaglesakura.android.thread.ui.UIHandler;
import com.eaglesakura.android.util.ContextUtil;
import com.eaglesakura.android.util.PermissionUtil;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import icepick.State;

/**
 *
 */
public abstract class BaseActivity extends AppCompatActivity implements FragmentChooser.Callback {

    static final int REQUEST_RUNTIMEPERMISSION_UPDATE = 0x1100 + 1;

    protected UserNotificationController userNotificationController = new UserNotificationController(this);

    boolean activityResumed;

    boolean activityDestroyed;

    protected BaseActivity() {
        fragments.setCallback(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        edgeColorToPrimaryColor();
    }

    protected void requestInjection(@LayoutRes int layoutId) {
        setContentView(layoutId);
        ButterKnife.bind(this);
    }

    public <T extends View> T findViewById(Class<T> clazz, int id) {
        return (T) findViewById(id);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int argb) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(argb);
        }
    }

    /**
     * Scroll系レイアウトでのEdgeColorをブランドに合わせて変更する
     * <br/>
     * Lollipopでは自動的にcolorPrimary系の色が反映されるため、何も行わない。
     * <br/>
     * 参考: http://stackoverflow.com/questions/28978989/set-recyclerview-edge-glow-pre-lollipop-when-using-appcompat
     */
    private void edgeColorToPrimaryColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        try {
            TypedArray typedArray = getTheme().obtainStyledAttributes(R.styleable.Theme);
            int id = typedArray.getResourceId(R.styleable.Theme_colorPrimaryDark, 0);
            int brandColor = getResources().getColor(id);

            //glow
            int glowDrawableId = getResources().getIdentifier("overscroll_glow", "drawable", "android");
            Drawable androidGlow = getResources().getDrawable(glowDrawableId);
            androidGlow.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
            //edge
            int edgeDrawableId = getResources().getIdentifier("overscroll_edge", "drawable", "android");
            Drawable androidEdge = getResources().getDrawable(edgeDrawableId);
            androidEdge.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
        } catch (Exception e) {
            LogUtil.log(e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityResumed = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ActivityResult.invoke(this, requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected BaseActivity self() {
        return this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityResumed = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityDestroyed = true;
    }

    public boolean isActivityDestroyed() {
        return activityDestroyed;
    }

    public boolean isActivityResumed() {
        return activityResumed;
    }

    public UserNotificationController getUserNotificationController() {
        return userNotificationController;
    }

    protected void toast(final String fmt, final Object... args) {
        runUI(new Runnable() {
            @Override
            public void run() {
                userNotificationController.toast(this, String.format(fmt, args));
            }
        });
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
    public void pushProgress(final String message) {
        UIHandler.postUI(new Runnable() {
            @Override
            public void run() {
                userNotificationController.pushProgress(this, message);
            }
        });
    }

    /**
     * 処理を終了する
     */
    public void popProgress() {
        runUI(new Runnable() {
            @Override
            public void run() {
                userNotificationController.popProgress(this);
            }
        });
    }

    @State
    protected FragmentChooser fragments = new FragmentChooser();

    /**
     * Fragmentがアタッチされたタイミングで呼び出される。
     * <br>
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
     * Runtime Permissionの更新を行わせる
     *
     * @param permissions
     * @return パーミッション取得を開始した場合はtrue
     */
    @SuppressLint("all")
    public boolean requestRuntimePermissions(String[] permissions) {
        if (!PermissionUtil.isRuntimePermissionGranted(this, permissions)) {
            requestPermissions(permissions, REQUEST_RUNTIMEPERMISSION_UPDATE);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_RUNTIMEPERMISSION_UPDATE) {
            Intent intent = new Intent();
            intent.setAction(LocalMessageReceiver.ACTION_RUNTIMEPERMISSION_UPDATE);
            List<String> granted = new ArrayList<>();
            List<String> denied = new ArrayList<>();

            for (int i = 0; i < permissions.length; ++i) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted.add(permissions[i]);
                } else {
                    denied.add(permissions[i]);
                }
            }

            intent.putExtra(LocalMessageReceiver.EXTRA_RUNTIMEPERMISSION_GRANTED_LIST, Util.convert(granted, new String[granted.size()]));
            intent.putExtra(LocalMessageReceiver.EXTRA_RUNTIMEPERMISSION_DENIED_LIST, Util.convert(denied, new String[denied.size()]));

            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    protected AsyncTaskResult<AsyncTaskController> runBackground(Runnable runner) {
        return FrameworkCentral.getTaskController().pushBack(runner);
    }
}
