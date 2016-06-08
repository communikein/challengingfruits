package com.example.eliam.challengingfruits;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;

import com.example.eliam.challengingfruits.accountmanager.AccountGeneral;


/**
 * Created by eliam on 01/06/2016.
 */
public class UserInfoUtility {

    public static UserInfo saveUserInfo(UserInfo user, Context ctx){
        AccountManager mAccountManager = AccountManager.get(ctx);
        Account[] accounts = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        Account account = null;

        if (accounts.length > 0) for (Account acc : accounts)
            if (acc.name.equals(user.getMail())) account = acc;

        if (account != null){
            mAccountManager.setPassword(account, user.getPassword());

            mAccountManager.setUserData(account,
                    UserInfo.PARAM_MAIL, user.getMail());
            mAccountManager.setUserData(account,
                    UserInfo.PARAM_PASSWORD, user.getPassword());
            mAccountManager.setUserData(account,
                    UserInfo.PARAM_LOCAL_PROFILE_PIC, user.getPicLocalPath());
            mAccountManager.setUserData(account,
                    UserInfo.PARAM_NAME, user.getFirstName());
            mAccountManager.setUserData(account,
                    UserInfo.PARAM_SURNAME, user.getLastName());
            mAccountManager.setUserData(account,
                    UserInfo.PARAM_PHONE, user.getPhone());
        }
        Utils.user = user;

        return user;
    }

    public static void removeAccount(Context ctx){
        Utils.user = null;

        AccountManager accountManager = AccountManager.get(ctx);
        Account[] accounts = accountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        if (accounts.length > 0){
            Account account = accounts[0];

            accountManager.removeAccount(account, new AccountManagerCallback<Boolean>() {
                @Override
                public void run(AccountManagerFuture<Boolean> future) {
                    try {
                        future.getResult();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, null);
        }
    }

    public static UserInfo getUserInfo(String email, Context ctx){
        AccountManager mAccountManager = AccountManager.get(ctx);
        Account[] accounts = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);
        Account account = null;

        if (accounts.length > 0) for (Account acc : accounts)
                if (acc.name.equals(email)) account = acc;

        UserInfo user = null;
        if (account != null){
            user = new UserInfo(
                    email,
                    mAccountManager.getPassword(account),
                    mAccountManager.getUserData(account,
                            UserInfo.PARAM_NAME),
                    mAccountManager.getUserData(account,
                            UserInfo.PARAM_SURNAME),
                    mAccountManager.getUserData(account,
                            UserInfo.PARAM_PHONE));
            user.setPicLocal(mAccountManager.getUserData(account,
                    UserInfo.PARAM_LOCAL_PROFILE_PIC));

            Utils.user = user;
        }

        return user;
    }

    public static UserInfo setUserInfo(Intent intent, Context context, String localPic){
        String accountName = intent.getStringExtra(UserInfo.PARAM_MAIL);
        String accountPassword = intent.getStringExtra(UserInfo.PARAM_PASSWORD);

        Utils.user = new UserInfo(accountName, accountPassword);
        Utils.user.setMail(accountName);
        Utils.user.setPassword(accountPassword);
        Utils.user.setPicLocal(localPic);
        Utils.user.setFirstName(intent.getStringExtra(UserInfo.PARAM_NAME));
        Utils.user.setLastName(intent.getStringExtra(UserInfo.PARAM_SURNAME));
        Utils.user.setPhone(intent.getStringExtra(UserInfo.PARAM_PHONE));

        UserInfoUtility.saveUserInfo(Utils.user, context);

        return Utils.user;
    }
}