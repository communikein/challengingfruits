package com.example.eliam.challengingfruits.accountmanager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.example.eliam.challengingfruits.UserInfo;
import com.example.eliam.challengingfruits.UserInfoUtility;


public class ServerInterface {

    public static final int OK = 1;
    public static final int ERROR = -1;
    public static final int ERROR_GENERIC = -2;

    public int login(UserInfo user, Context context){
        AccountManager mAccountManager = AccountManager.get(context);
        Account[] S3_accounts = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

        if (S3_accounts.length == 0)
            return ServerInterface.ERROR;
        else {
            String mail = S3_accounts[0].name;
            String pwd = mAccountManager.getPassword(S3_accounts[0]);

            if (mail.equals(user.getMail()) && pwd.equals(user.getPassword()))
                return ServerInterface.OK;
            else
                return ServerInterface.ERROR;
        }
    }

    public boolean userAlreadyRegistered(UserInfo user, Context context) {
        AccountManager mAccountManager = AccountManager.get(context);
        Account[] S3_accounts = mAccountManager.getAccountsByType(AccountGeneral.ACCOUNT_TYPE);

        return S3_accounts.length != 0 && S3_accounts[0].name.equals(user.getMail());
    }

    public UserInfo updateAccount(UserInfo user, Context context){
        return UserInfoUtility.saveUserInfo(user, context);
    }
}
