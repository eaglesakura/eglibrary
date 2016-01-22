package com.eaglesakura.dummybeaconadvertiser;

import com.eaglesakura.android.annotations.AnnotationUtil;
import com.eaglesakura.android.framework.ui.BaseActivity;

import org.androidannotations.annotations.EActivity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;


@EActivity(R.layout.activity_main)
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            {
                Fragment fragment = AnnotationUtil.newFragment(DummyBeaconAdvertiseFragment.class);
                transaction.replace(R.id.container, fragment, fragment.getClass().getName());
            }
            transaction.commit();
        }
    }
}
