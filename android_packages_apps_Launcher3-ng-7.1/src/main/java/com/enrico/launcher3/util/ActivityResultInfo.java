package com.enrico.launcher3.util;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Utility class which stores information from onActivityResult
 */
public class ActivityResultInfo implements Parcelable {

    public static final Parcelable.Creator<ActivityResultInfo> CREATOR =
            new Parcelable.Creator<ActivityResultInfo>() {
                public ActivityResultInfo createFromParcel(Parcel source) {
                    return new ActivityResultInfo(source);
                }

                public ActivityResultInfo[] newArray(int size) {
                    return new ActivityResultInfo[size];
                }
            };
    public final int requestCode;
    public final int resultCode;
    public final Intent data;

    public ActivityResultInfo(int requestCode, int resultCode, Intent data) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
    }

    private ActivityResultInfo(Parcel parcel) {
        requestCode = parcel.readInt();
        resultCode = parcel.readInt();
        data = parcel.readInt() != 0 ? Intent.CREATOR.createFromParcel(parcel) : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(requestCode);
        dest.writeInt(resultCode);
        if (data != null) {
            dest.writeInt(1);
            data.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
    }
}
