package com.eaglesakura.android.playservice;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import com.eaglesakura.android.framework.ui.BaseFragment;
import com.eaglesakura.util.LogUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class GoogleApiFragment extends BaseFragment {
    protected GoogleApiClientToken googleApiClientToken;

    Callback callback;

    protected static final int REQUEST_GOOGLEPLAYSERVICE_RECOVER = 0x1100;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Callback) {
            this.callback = (Callback) context;
        } else if (getParentFragment() instanceof Callback) {
            this.callback = (Callback) getParentFragment();
        } else {
            throw new IllegalStateException("Callback not impl!!");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleApiClientToken = callback.newClientToken(this);

        final int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (statusCode != ConnectionResult.SUCCESS) {
            showGoogleErrorDialog(statusCode);
        } else {
            LogUtil.log("Google Play Service OK!");
        }
    }

    /**
     * エラーダイアログを表示する
     *
     * @param statusCode
     */
    protected void showGoogleErrorDialog(final int statusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode, getActivity(), REQUEST_GOOGLEPLAYSERVICE_RECOVER, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                callback.onGooglePlayServiceRecoverCanceled(GoogleApiFragment.this, statusCode);
            }
        });
        dialog.show();
    }

    public GoogleApiClientToken getGoogleApiClientToken() {
        return googleApiClientToken;
    }

    public interface Callback {
        GoogleApiClientToken newClientToken(GoogleApiFragment self);

        void onGooglePlayServiceRecoverCanceled(GoogleApiFragment self, int statusCode);
    }
}
