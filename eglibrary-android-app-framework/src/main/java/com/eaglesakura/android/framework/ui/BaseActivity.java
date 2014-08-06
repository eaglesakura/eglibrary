package com.eaglesakura.android.framework.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

/**
 *
 */
@EActivity
public abstract class BaseActivity extends Activity {

    @AfterViews
    protected void onAfterViews() {

    }

    @UiThread
    protected void toast(String msg) {
        if (StringUtil.isEmpty(msg)) {
            LogUtil.log("message is empty");
            return;
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * show toast
     *
     * @param resId
     */
    protected void toast(int resId) {
        toast(getString(resId));
    }

    private int progressNum = 0;
    private ProgressDialog progressDialog = null;

    /**
     * show progress dialog
     * @param stringId
     */
    public void pushProgress(int stringId) {
        pushProgress(getString(stringId));
    }

    /**
     * 処理を開始する
     *
     * @param message
     */
    @UiThread
    public void pushProgress(String message) {
        if (progressNum == 0) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            progressDialog.setMessage(message);
        }
        ++progressNum;
    }

    /**
     * 処理を終了する
     */
    @UiThread
    public void popProgress() {
        --progressNum;

        if (progressNum <= 0) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            progressNum = 0;
        }
    }
}
