package com.eaglesakura.lib.sample.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.dropbox.client2.session.Session.AccessType;
import com.eaglesakura.lib.android.dropbox.DropboxAPIException.Type;
import com.eaglesakura.lib.android.dropbox.fragment.DropboxAuthFragment;
import com.eaglesakura.lib.android.dropbox.fragment.DropboxAuthFragment.DropboxAuthListener;
import com.eaglesakura.lib.android.game.util.LogUtil;

public class DropboxAuthActivity extends FragmentActivity implements DropboxAuthListener {

    public static SharedPreferences getPreference(Context context) {
        LogUtil.setTag("dropbox");
        LogUtil.setOutput(true);
        return context.getSharedPreferences("dropbox.pref", MODE_PRIVATE);
    }

    public static final String APP_KEY = "";
    public static final String APP_SECRET = "";

    SharedPreferences preference;

    public static final String PREF_TOKEN = "PREF_TOKENKEY";

    public static final String PREF_TOKENSECRET = "PREF_TOKENSECRET";

    @Override
    protected void onCreate(Bundle bundle) {
        preference = getPreference(this);
        super.onCreate(bundle);

        if (bundle == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (preference.getString(PREF_TOKEN, null) == null) {
                Fragment fragment = new DropboxAuthFragment(APP_KEY, APP_SECRET, AccessType.APP_FOLDER);
                transaction.add(fragment, fragment.getClass().getName());
            } else {
                LogUtil.log("auth completed!!");
            }
            transaction.commit();
        }
    }

    @Override
    public void onAuthComplete(DropboxAuthFragment fragment, String token, String tokenSecret) {
        LogUtil.log("auth complete!!");
        preference.edit().putString(PREF_TOKEN, token).putString(PREF_TOKENSECRET, tokenSecret).commit();
    }

    @Override
    public void onAuthFailed(DropboxAuthFragment fragment, Type error) {
        Toast.makeText(this, "認証失敗 :: " + error.name(), Toast.LENGTH_SHORT).show();
    }
}
