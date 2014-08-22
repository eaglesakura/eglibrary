package com.eaglesakura.android.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * Activity/Fragment/Serviceを強参照しない形で保持を行う。
 */
public class WeakContext {
    /**
     * MainContext
     */
    final protected WeakReference<Object> owner;

    /**
     * ApplicationContext
     */
    final private Context appContext;

    /**
     * Fragmentから生成する
     *
     * @param fragment 生成元のFragment
     */
    public WeakContext(Fragment fragment) {
        this(fragment, fragment.getActivity().getApplicationContext());
    }

    /**
     * Activityから生成する
     *
     * @param activity 生成元のActivity
     */
    public WeakContext(Activity activity) {
        this(activity, activity.getApplicationContext());
    }

    /**
     * Serviceから生成する
     *
     * @param service 参照元のService
     */
    public WeakContext(Service service) {
        this(service, service.getApplicationContext());
    }

    protected WeakContext(Object obj, Context appContext) {
        this.owner = new WeakReference<Object>(obj);
        this.appContext = appContext;
    }

    public Service getService() {
        Object obj = owner.get();
        if (obj instanceof Service) {
            return (Service) obj;
        }
        return null;
    }

    public Activity getActivity() {
        Object obj = owner.get();
        if (obj instanceof Activity) {
            return (Activity) obj;
        } else if (obj instanceof Fragment) {
            return ((Fragment) obj).getActivity();
        }
        return null;
    }

    public Fragment getFragment() {
        Object obj = owner.get();
        if (obj instanceof Fragment) {
            return (Fragment) obj;
        }
        return null;
    }

    /**
     * オーナーオブジェクトが有効であればtrue
     *
     * @return
     */
    public boolean isExistOwner() {
        return owner.get() != null;
    }

    /**
     * app context
     *
     * @return
     */
    public Context getApplicationContext() {
        return appContext;
    }
}
