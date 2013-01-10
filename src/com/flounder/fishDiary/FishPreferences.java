/**
 * XeniaStorySettings.java
 *
 * Ver 1.0, 2012-12-1, alex_yh, Create file.
 */
package com.flounder.fishDiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class FishPreferences {
    public static final String KEY_ENABLE_UNDERLINE = "pref_enable_underline";
    public static final String KEY_TEXT_COLOR = "pref_text_color";
    public static final String KEY_BACKGROUND_IMAGE = "pref_background_image";
    public static final String KEY_TEXT_SIZE = "pref_text_size";
    public static final String KEY_PASSWORD = "pref_password";
    public static final String KEY_HEAD_PHOTO = "pref_photo";
    public static final String KEY_NOTE_TAG = "pref_tag";
    public static final String KEY_AUTHOR = "pref_author";
    public static final String KEY_BG_EFFECT = "pref_background_effect";
    public static final String KEY_FIRST_TIME = "pref_first_time";

    public static final int DEFAULT_TEXT_SIZE = 16;

    public static final int EFFECT_NONE = 0;
    public static final int EFFECT_FEATHER = 1;
    public static final int EFFECT_REFLECT = 2;

    public static String getAuthorName(Context context) {
        return getSharedPreferences(context).getString(KEY_AUTHOR,
                context.getString(R.string.pref_author_default));
    }

    public static boolean isUnderlineEnable(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_ENABLE_UNDERLINE, true);
    }

    public static String getHeadPhote(Context context) {
        return getSharedPreferences(context).getString(KEY_HEAD_PHOTO, null);
    }

    public static int getBgImageEffect(Context context) {
        return getSharedPreferences(context).getInt(KEY_BG_EFFECT, EFFECT_NONE);
    }

    /**
     * Create our own String list representation.
     * getStringSet() needs API level 11+.
     * 
     * @param context
     * @return string list split by comma
     */
    public static String getNoteTag(Context context) {
        return getSharedPreferences(context).getString(KEY_NOTE_TAG, null);
    }

    public static int getTextSize(Context context) {
        return getSharedPreferences(context)
                .getInt(KEY_TEXT_SIZE, DEFAULT_TEXT_SIZE);
    }

    public static String getBackgroundImage(Context context) {
        return getSharedPreferences(context).getString(KEY_BACKGROUND_IMAGE, null);
    }

    public static String getPassword(Context context) {
        return getSharedPreferences(context).getString(KEY_PASSWORD, null);
    }

    public static boolean isFirstTime(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_FIRST_TIME, true);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }
}
