package com.enrico.launcher3.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.UserHandle;

@TargetApi(Build.VERSION_CODES.N)
class UserManagerCompatVN extends UserManagerCompatVM {

    UserManagerCompatVN(Context context) {
        super(context);
    }

    @Override
    public boolean isQuietModeEnabled(UserHandle user) {
        try {
            return mUserManager.isQuietModeEnabled(user);
        }
        catch (SecurityException ex) {
            return false;
        }
    }

    @Override
    public boolean isUserUnlocked(UserHandle user) {
        try {
            return mUserManager.isUserUnlocked(user);
        }
        catch (SecurityException ex) {
            return false;
        }
    }
}

