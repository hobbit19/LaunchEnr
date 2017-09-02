package com.enrico.launcher3.compat;

/**
 * Created by Enrico on 29/07/2017.
 */

public class UserHandleUtil {
    public static android.os.UserHandle myUserHandle() {
        return android.os.Process.myUserHandle();
    }
}
