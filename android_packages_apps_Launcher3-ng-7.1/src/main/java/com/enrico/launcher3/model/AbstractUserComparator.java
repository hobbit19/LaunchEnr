package com.enrico.launcher3.model;

import android.content.Context;
import android.os.UserHandle;

import com.enrico.launcher3.ItemInfo;
import com.enrico.launcher3.compat.UserHandleUtil;
import com.enrico.launcher3.compat.UserManagerCompat;

import java.util.Comparator;

/**
 * A comparator to arrange items based on user profiles.
 */
abstract class AbstractUserComparator<T extends ItemInfo> implements Comparator<T> {

    private final UserManagerCompat mUserManager;
    private final UserHandle mMyUser;

    AbstractUserComparator(Context context) {
        mUserManager = UserManagerCompat.getInstance(context);
        mMyUser = UserHandleUtil.myUserHandle();
    }

    @Override
    public int compare(T lhs, T rhs) {
        if (mMyUser.equals(lhs.user)) {
            return -1;
        } else {
            Long aUserSerial = mUserManager.getSerialNumberForUser(lhs.user);
            Long bUserSerial = mUserManager.getSerialNumberForUser(rhs.user);
            return aUserSerial.compareTo(bUserSerial);
        }
    }
}
