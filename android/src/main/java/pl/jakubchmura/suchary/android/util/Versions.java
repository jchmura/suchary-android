package pl.jakubchmura.suchary.android.util;

import android.os.Build;

public class Versions {

    private static final int apiVersion = Build.VERSION.SDK_INT;

    public static boolean isLollipop() {
        return apiVersion >= Build.VERSION_CODES.LOLLIPOP;
    }

}
