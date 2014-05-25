package com.eaglesakura.util;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Googleアカウントを管理する。
 * @author SAKURA
 *
 */
public class GoogleAccount {
    AccountManager manager = null;
    Account[] accounts = null;
    Activity context = null;

    public GoogleAccount(Activity context) {
        this.context = context;
        manager = AccountManager.get(context);
        accounts = manager.getAccountsByType("com.google");
    }

    /**
     * 登録されているアカウント数を取得する。
     * @return
     */
    public int getAccountCount() {
        return accounts.length;
    }

    /**
     * ユーザーのメールアドレスを取得する
     * @param num
     * @return
     */
    public String getMailAddress(int num) {
        return accounts[num].name;
    }

    /**
     * アカウントアクセス用トークンを取得する。
     * @param acountNumber
     * @return
     */
    public void getGoogleDocsToken(int accountNumber, AccountManagerCallback<Bundle> callback) {
        manager.getAuthToken(accounts[accountNumber], "writely", null, context, callback, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                LogUtil.log("what : " + msg.what);
                super.handleMessage(msg);
            }
        });
    }

    /**
     * docsのTokenをリセットし、取得しなおす。
     */
    public void resetDocsToken(int accountNumber, String token) throws AuthenticatorException, IOException,
            OperationCanceledException {
        manager.invalidateAuthToken(accounts[accountNumber].type, token);
    }
}
