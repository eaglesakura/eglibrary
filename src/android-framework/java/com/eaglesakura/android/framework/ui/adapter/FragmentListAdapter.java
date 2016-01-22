package com.eaglesakura.android.framework.ui.adapter;

import com.eaglesakura.android.framework.ui.FragmentChooser;
import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.Util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * FragmentListAdapterをサポートする
 */
public class FragmentListAdapter extends FragmentPagerAdapter {
    FragmentChooser chooser;
    final List<FragmentCreater> creaters = new ArrayList<FragmentCreater>();

    final FragmentManager fragmentManager;

    int containerViewId;

    public FragmentListAdapter(final FragmentManager fragmentManager) {
        super(fragmentManager);
        this.fragmentManager = fragmentManager;
    }

    String genFragmentId(int index) {
        return "android:switcher:" + containerViewId + ":" + index;
    }

    private static String genFragmentId(int containerViewId, int index) {
        return "android:switcher:" + containerViewId + ":" + index;
    }

    @Override
    public void startUpdate(ViewGroup container) {
        if (containerViewId == 0) {
            this.containerViewId = container.getId();
        }
        super.startUpdate(container);
    }

    public void setContainerViewId(int containerViewId) {
        this.containerViewId = containerViewId;
    }

    /**
     * ViewPagerと接続する
     */
    public void setViewPager(ViewPager pager) {
        this.containerViewId = pager.getId();
        pager.setAdapter(this);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Fragment item = getItem(position);
        if (item instanceof IFragmentPagerTitle) {
            return ((IFragmentPagerTitle) item).getTitle();
        }
        return super.getPageTitle(position);
    }

    /**
     * Fragment管理を行う
     * Callbackを自動で設定するため、事前に設定したCallbackが呼び出されないことに注意すること。
     */
    public void setChooser(FragmentChooser chooser) {
        this.chooser = chooser;
        chooser.setCallback(new FragmentChooser.Callback() {
            @Override
            public FragmentManager getFragmentManager(FragmentChooser chooser) {
                return fragmentManager;
            }

            @Override
            public boolean isFragmentExist(FragmentChooser chooser, Fragment fragment) {
                return fragment != null;
            }

            @Override
            public Fragment newFragment(FragmentChooser chooser, String requestTag) {
                return null;
            }
        });
    }

    @Override
    public Fragment getItem(int position) {
        FragmentCreater creater = creaters.get(position);
        final String reqTag = genFragmentId(position);
        Fragment result = chooser.find(reqTag);
        if (result == null) {
            result = creater.newFragment(this, position);
            LogUtil.log("adapter new fragment(%s)", reqTag);
        } else {
            LogUtil.log("adapter find fragment from chooser(%s)", reqTag);
        }
        return result;
    }

    @Override
    public int getCount() {
        return creaters.size();
    }

    public void addFragmentCreater(FragmentCreater creater) {
        creaters.add(creater);
    }

    public List<Fragment> listFragments() {
        List<Fragment> result = new ArrayList<Fragment>();
        for (int i = 0; i < creaters.size(); ++i) {
            result.add(getItem(i));
        }
        return result;
    }

    /**
     * 指定した条件のFragmentを生成する
     */
    public interface FragmentCreater {
        /**
         * Fragmentを生成させる
         */
        Fragment newFragment(FragmentListAdapter adapter, int index);
    }

    /**
     * 単純にClassを生成する
     */
    public static class SimpleFragmentCreater implements FragmentCreater {
        Class<? extends Fragment> clazz;

        public SimpleFragmentCreater(Class<? extends Fragment> clazz) {
            this.clazz = clazz;
        }

        /**
         * 何か初期化の必要がある時はオーバーライドする
         */
        protected Fragment initialize(Fragment fragment) {
            return fragment;
        }

        @Override
        public Fragment newFragment(FragmentListAdapter adapter, int index) {
            return initialize(Util.newInstanceOrNull(clazz));
        }
    }


    public static class SimpleFragmentCreater2 extends SimpleFragmentCreater {
        /**
         * 登録先のセレクタ
         */
        final FragmentChooserRef chooserRef;
        final int containerViewId;

        public SimpleFragmentCreater2(Class<? extends Fragment> clazz, ViewPager pager, FragmentChooserRef chooserRef) {
            super(clazz);
            this.chooserRef = chooserRef;
            this.containerViewId = pager.getId();
        }

        @Override
        public Fragment newFragment(FragmentListAdapter adapter, int index) {
            Fragment result = super.newFragment(adapter, index);
            FragmentChooser parentChooser = chooserRef.getFragmentChooser();
            parentChooser.compact();
            parentChooser.addFragment(FragmentChooser.ReferenceType.Weak, result, genFragmentId(containerViewId, index), 0);
            return result;
        }
    }

    public interface FragmentChooserRef {
        FragmentChooser getFragmentChooser();
    }
}
