/**
 * Utils.java
 *
 * Ver 1.0, 2012-11-30, alex_yh, Create file.
 */
package com.flounder.fishDiary.util;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.ContextThemeWrapper;

import com.flounder.fishDiary.R;

public class Utils {

    private static final boolean DEBUG = true;
    private static final String TAG = "Xenia";

    public static void logd(String str) {
        if (DEBUG) {
            Log.d(TAG, str);
        }
    }

    public static void loge(String str) {
        if (DEBUG) {
            Log.e(TAG, str);
        }
    }

    public static boolean[] toPrimitiveArray(final List<Boolean> booleanList) {
        final boolean[] primitives = new boolean[booleanList.size()];
        int index = 0;
        for (Boolean object : booleanList) {
            primitives[index++] = object;
        }
        return primitives;
    }

    /** Display an errorDialog and finish the activity */
    public static void showErrorAndFinish(final Activity activity, int msgId) {
        DialogInterface.OnClickListener buttonListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        };
        new AlertDialog.Builder(new ContextThemeWrapper(activity,
                android.R.style.Theme_NoTitleBar)).setCancelable(false)
                .setTitle(R.string.error_title).setMessage(msgId)
                .setNeutralButton(R.string.button_ok, buttonListener).show();
    }
}
