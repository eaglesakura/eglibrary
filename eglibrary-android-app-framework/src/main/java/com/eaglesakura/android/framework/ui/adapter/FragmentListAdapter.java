package com.eaglesakura.android.framework.ui.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Parcelable;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import com.eaglesakura.android.annotations.AnnotationUtil;
import com.eaglesakura.android.framework.ui.BaseFragment;
import com.eaglesakura.android.framework.ui.FragmentChooser;
import com.eaglesakura.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * FragmentListAdapterをサポートする
 */
@Deprecated
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
     *
     * @param pager
     */
    public void setViewPager(ViewPager pager) {
        this.containerViewId = pager.getId();
        pager.setAdapter(this);
    }

    /**
     * Fragment管理を行う
     * Callbackを自動で設定するため、事前に設定したCallbackが呼び出されないことに注意すること。
     *
     * @param chooser
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
         *
         * @param adapter
         * @param index
         * @return
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
         *
         * @param fragment
         * @return
         */
        protected Fragment initialize(Fragment fragment) {
            return fragment;
        }

        @Override
        public Fragment newFragment(FragmentListAdapter adapter, int index) {
            return initialize(AnnotationUtil.newFragment(clazz));
        }
    }
}
