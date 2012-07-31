package com.eaglesakura.lib.sample.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.eaglesakura.lib.android.game.util.LogUtil;
import com.eaglesakura.lib.gdata.GoogleAPIException;
import com.eaglesakura.lib.gdata.GoogleOAuth2Helper;
import com.eaglesakura.lib.gdata.GoogleOAuth2Helper.AuthToken;
import com.eaglesakura.lib.gdata.fragment.GoogleOAuth2Fragment;
import com.eaglesakura.lib.sample.R;

public class GoogleOAuth2Activity extends FragmentActivity implements GoogleOAuth2Fragment.OAuth2Listener {

    @Override
    protected void onCreate(Bundle saved) {
        super.onCreate(saved);
        LogUtil.setTag("gdata-test");
        LogUtil.setOutput(true);
        setContentView(R.layout.gllayout);
        if (saved == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            {
                Fragment fragment = new GoogleOAuth2Fragment(CLIENT_ID, CLIENT_SECRET, new String[] {
                        //
                        GoogleOAuth2Helper.SCOPE_GDRIVE,// gdrive
                        GoogleOAuth2Helper.SCOPE_USERINFO_EMAIL, // email
                });
                transaction.add(R.id.gl_area, fragment);
            }
            transaction.commit();
        }
    }

    @Override
    public void onMakeTokenComplete(GoogleOAuth2Fragment fragment, AuthToken token) {
        LogUtil.log("onMakeTokenComplete = " + token.access_token);
    }

    @Override
    public void onErrorMakeAuthURL(GoogleOAuth2Fragment fragment, GoogleAPIException e) {
        LogUtil.log(e);
        LogUtil.log("onErrorMakeAuthURL :: " + e.getMessage());
        finish();
    }

    @Override
    public void onErrorMakeAuthToken(GoogleOAuth2Fragment fragment, GoogleAPIException e) {
        LogUtil.log(e);
        LogUtil.log("onErrorMakeAuthToken :: " + e.getMessage());
        finish();
    }

    @Override
    public void onAuthCanceled(GoogleOAuth2Fragment fragment) {
        LogUtil.log("onAuthCanceled");
        finish();
    }
}
