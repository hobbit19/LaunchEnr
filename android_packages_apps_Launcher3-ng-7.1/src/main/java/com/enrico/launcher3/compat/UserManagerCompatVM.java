package com.enrico.launcher3.compat;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.UserHandle;

@TargetApi(Build.VERSION_CODES.M)
class UserManagerCompatVM extends UserManagerCompatVL {

    UserManagerCompatVM(Context context) {
        super(context);
    }

    @Override
    public long getUserCreationTime(UserHandle user) {
        return mUserManager.getUserCreationTime(user);
    }
}
