package com.eaglesakura.android.framework.support.ui.content;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;

import com.eaglesakura.android.R;
import com.eaglesakura.android.framework.support.ui.BaseActivity;
import com.eaglesakura.android.framework.support.ui.BaseFragment;
import com.eaglesakura.util.Util;

/**
 * 親となるFragmentを指定して起動するActivityの雛形
 * <p/>
 * メインコンテンツは @+id/Content.Holder.Root を持たなければならない。
 * Toolbarは @+id/EsMaterial.Toolbar を自動的に検索し、存在するならToolbarとして自動設定する。
 */
public abstract class ContentHolderActivity extends BaseActivity {
    static final String EXTRA_ACTIVITY_LAYOUT = "EXTRA_ACTIVITY_LAYOUT";

    /**
     * 遷移対象のFragment Class
     */
    private static final String EXTRA_CONTENT_FRAGMENT_CLASS = "EXTRA_CONTENT_FRAGMENT_CLASS";

    /**
     * 遷移対象のArgment
     */
    private static final String EXTRA_CONTENT_FRAGMENT_ARGMENTS = "EXTRA_CONTENT_FRAGMENT_ARGMENTS";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int layoutId = getIntent().getIntExtra(EXTRA_ACTIVITY_LAYOUT, getDefaultLayoutId());
        if (layoutId != 0) {
            requestInjection(layoutId);
            Toolbar toolbar = findViewById(Toolbar.class, R.id.EsMaterial_Toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }

            if (savedInstanceState == null) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                {
                    String className = getIntent().getStringExtra(EXTRA_CONTENT_FRAGMENT_CLASS);
                    BaseFragment fragment = null;
                    if (className != null) {
                        fragment = Util.newInstanceOrNull(className);
                    }

                    if (fragment == null) {
                        fragment = newDefaultContentFragment();
                    } else {
                        Bundle argments = getIntent().getBundleExtra(EXTRA_CONTENT_FRAGMENT_ARGMENTS);
                        if (argments != null) {
                            fragment.setArguments(argments);
                        }
                    }
                    transaction.add(R.id.Content_Holder_Root, fragment, createTag(fragment));
                }
                transaction.commit();
            }
        }

    }

    /**
     * デフォルトで使用されるレイアウトIDを取得する
     *
     * @return
     */
    protected int getDefaultLayoutId() {
        return R.layout.activity_content_holder;
    }

    /**
     * 表示するコンテンツが指定されない場合のデフォルトコンテンツを開く
     *
     * @return
     */
    protected abstract BaseFragment newDefaultContentFragment();

    /**
     * 管理用のTagを生成する
     *
     * @param fragment
     * @return
     */
    protected String createTag(BaseFragment fragment) {
        return fragment.createSimpleTag();
    }

    /**
     * コンテンツ表示用Intentを生成する
     *
     * @param contentFragment
     */
    public static Intent createIntent(Context context,
                                      Class<? extends ContentHolderActivity> activityClass, int activityLayoutId,
                                      Class<? extends BaseFragment> contentFragment, Bundle argments) {
        Intent intent = new Intent(context, activityClass);
        intent.putExtra(EXTRA_CONTENT_FRAGMENT_CLASS, contentFragment.getName());
        if (activityLayoutId != 0) {
            intent.putExtra(EXTRA_ACTIVITY_LAYOUT, activityLayoutId);
        } else {

        }
        if (argments != null) {
            intent.putExtra(EXTRA_CONTENT_FRAGMENT_ARGMENTS, argments);
        }
        return intent;
    }
}
