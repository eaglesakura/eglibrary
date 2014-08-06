package com.eaglesakura.android.framework.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public final class FragmentChooser implements Parcelable {
    protected Map<String, Fragment> fragments = new HashMap<String, Fragment>();

    protected Callback callback = null;

    public FragmentChooser() {
    }

    private FragmentChooser(Parcel in) {
//        LogUtil.log("decode FragmentChooser :: %s", getClass().getName());
        // タグを復元する
        {
            List<String> tags = new ArrayList<String>();
            in.readStringList(tags);

            for (String tag : tags) {
                fragments.put(tag, null);
            }
        }
    }

    /**
     * 管理しているFragmentのタグ一覧を取得する
     */
    public List<String> listTags() {
        List<String> result = new ArrayList<String>();
        Iterator<Map.Entry<String, Fragment>> iterator = fragments.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Fragment> entry = iterator.next();
            result.add(entry.getKey());
        }
        return result;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;

        restoreFragments();
    }

    /**
     * Fragment情報をレストアする
     */
    public void restoreFragments() {
        FragmentManager fragmentManager = getFragmentManager();
        Iterator<Map.Entry<String, Fragment>> iterator = fragments.entrySet().iterator();
        // Fragmentをそれぞれ探し、有効であれば登録する
        while (iterator.hasNext()) {
            Map.Entry<String, Fragment> entry = iterator.next();
            Fragment fragment = fragmentManager.findFragmentByTag(entry.getKey());
            if (fragment != null && callback.isFragmentExist(this, fragment)) {
                entry.setValue(fragment);
            }
        }
    }

    private FragmentManager getFragmentManager() {
        return callback.getFragmentManager(this);
    }

    public void addFragment(Fragment fragment, String tag) {
        fragments.put(tag, fragment);
    }

    public Fragment find(String tag) {
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);

        if (fragment != null && !callback.isFragmentExist(this, fragment)) {
            // Fragmentが無効であればtrue
            fragment = null;
        }

        if (fragment == null) {
            fragment = fragments.get(tag);

            // fragmentが無ければ生成リクエストを送る
            if (fragment == null) {
                fragment = callback.newFragment(this, tag);
            }

            // 結果はともあれそれを返す
            return fragment;
        } else {
            fragments.remove(tag);
            fragments.put(tag, fragment);
            return fragment;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        LogUtil.log("writeToParcel :: %s", getClass().getName());

        List<String> tags = new ArrayList<String>();
        Iterator<Map.Entry<String, Fragment>> iterator = fragments.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Fragment> next = iterator.next();
            tags.add(next.getKey());
        }

        dest.writeStringList(tags);
    }

    public static final Parcelable.Creator<FragmentChooser> CREATOR
            = new Parcelable.Creator<FragmentChooser>() {
        public FragmentChooser createFromParcel(Parcel in) {
            return new FragmentChooser(in);
        }

        public FragmentChooser[] newArray(int size) {
            return new FragmentChooser[size];
        }
    };

    /**
     * callback
     */
    public interface Callback {
        /**
         * 検索対象のFragmentManagerを取得する
         *
         * @param chooser
         * @return
         */
        FragmentManager getFragmentManager(FragmentChooser chooser);

        /**
         * このFragmentが有効であればtrue
         *
         * @param chooser
         * @param fragment
         * @return
         */
        boolean isFragmentExist(FragmentChooser chooser, Fragment fragment);

        /**
         * Fragmentの生成を行わせる
         *
         * @param chooser
         * @param requestTag
         * @return
         */
        Fragment newFragment(FragmentChooser chooser, String requestTag);
    }
}
