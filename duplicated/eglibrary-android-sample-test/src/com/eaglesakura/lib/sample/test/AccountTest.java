package com.eaglesakura.lib.sample.test;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.test.AndroidTestCase;

public class AccountTest extends AndroidTestCase {

    public void addAccountTest() {
        AccountManager manager = AccountManager.get(getContext());
        Account account = new Account("example@gmail.com", "com.google");
        manager.addAccountExplicitly(account, "", null);
    }
}
