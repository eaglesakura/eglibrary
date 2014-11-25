package com.eaglesakura.android.framework.support.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.eaglesakura.util.LogUtil;
import com.eaglesakura.util.StringUtil;

/**
 * Activityが表示されている間に使用される共通UIを定義する
 */
public class ActivityNotificationInterface {
    protected int progressNum = 0;
    private ProgressDialog progressDialog = null;

    protected Activity activity;

    public ActivityNotificationInterface(Activity activity) {
        this.activity = activity;
    }

    /**
     * 何らかの処理中であればtrue
     *
     * @return
     */
    public boolean isProgressing() {
        return progressNum > 0;
    }

    /**
     * 処理中のUIを生成する
     *
     * @param message
     */
    protected void showProgressInterface(String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * 処理中のUIを更新する
     *
     * @param message
     */
    protected void updateProgressInterface(String message) {
        progressDialog.setMessage(message);
    }

    /**
     * 処理中のUIを非表示にする
     */
    protected void dismissProgressInterface() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * Toastを表示する
     *
     * @param message
     */
    public void toast(String message) {
        if (StringUtil.isEmpty(message)) {
            LogUtil.log("message is empty");
            return;
        }

        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 処理を開始する
     *
     * @param message
     */
    public void pushProgress(String message) {
        if (progressNum == 0) {
            showProgressInterface(message);
        } else {
            updateProgressInterface(message);
        }
        ++progressNum;
    }

    /**
     * 処理を終了する
     */
    public void popProgress() {
        --progressNum;

        if (progressNum <= 0) {
            dismissProgressInterface();
            progressNum = 0;
        }
    }
}
