/**
 * SettingsPrefrenceActivity.java
 *
 * Ver 1.0, 2012-12-9, alex_yh, Create file.
 */
package com.flounder.fishDiary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.flounder.fishDiary.data.Constants;
import com.flounder.fishDiary.image.ImageUtil;
import com.flounder.fishDiary.view.BaseStyleDialog;
import com.flounder.fishDiary.view.BaseStyleDialog.ICallBack;
import com.flounder.fishDiary.view.ImageViewPreference;
import com.flounder.fishDiary.view.PasswordDialog;
import com.flounder.fishDiary.view.PasswordDialog.IPasswdCallBack;

public class FishSettingsActivity extends PreferenceActivity {

    private ImageViewPreference mImagePref;
    private Preference mAuthorPref;
    private CheckBoxPreference mLinedPref;
    private Preference mBackgroundPref;
    private CheckBoxPreference mPasswdPref;
    private Preference mRestorePref;
    private Preference mAboutPref;

    private String mPassword;
    private Context mContext;

    /** The launch code when picking a photo and the raw data is returned */
    private static final int REQUEST_PICK_HEAD = 100;
    private static final int REQUEST_PICK_BG = 200;

    private static final int ICON_SIZE_HEAD = 96;
    private static final int ICON_SIZE_BG = 640;

    private static final String HEAD_FILENAME = "icon.dat";
    private static final String BG_FILENAME = "bg.dat";
    private int mFlag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        initPrefs();
    }

    private void initPrefs() {
        mContext = this;

        mAuthorPref = (Preference) findPreference(getString(R.string.pref_author_key));
        mAuthorPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                promptAuthorDialog();
                return true;
            }
        });

        mImagePref = (ImageViewPreference) findPreference(getString(R.string.pref_image_key));
        mImagePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ImageUtil.doPickPhotoFromGallery((Activity) mContext,
                        REQUEST_PICK_HEAD, ICON_SIZE_HEAD, HEAD_FILENAME);
                return true;
            }
        });

        mLinedPref = (CheckBoxPreference) findPreference(getString(R.string.pref_underline_enable_key));
        mLinedPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences.Editor editor = FishPreferences
                        .getEditor(mContext);
                editor.putBoolean(FishPreferences.KEY_ENABLE_UNDERLINE,
                        (Boolean) newValue);
                editor.commit();
                return true;
            }
        });

        mBackgroundPref = (Preference) findPreference(getString(R.string.pref_background_key));
        mBackgroundPref
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        promptBackgroundDialog();
                        return true;
                    }
                });

        mPasswdPref = (CheckBoxPreference) findPreference(getString(R.string.pref_passwd_key));
        mPasswdPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                promptPasswdDialog((Boolean) newValue);
                return true;
            }
        });

        mRestorePref = (Preference) findPreference(getString(R.string.pref_restore_key));
        mRestorePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                restoreSettingsDialog();
                return true;
            }
        });

        mAboutPref = (Preference) findPreference(getString(R.string.pref_about_key));
        mAboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(mContext, FishDiaryActivity.class);
                intent.setAction(Constants.ACTION_CONTENT);
                intent.putExtra(Constants.ACTION_CONTENT, Constants.CONTENT_ABOUT);
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == REQUEST_PICK_HEAD) {
            Uri uri = ImageUtil.getImageUri(HEAD_FILENAME);
            if (uri != null)
                mImagePref.setImageUri(uri);
        }
        if (requestCode == REQUEST_PICK_BG) {
            SharedPreferences.Editor editor = FishPreferences
                    .getEditor(mContext);
            editor.putString(FishPreferences.KEY_BACKGROUND_IMAGE,
                    ImageUtil.getImageUri(BG_FILENAME).toString());
            editor.commit();
        }
    }

    private void promptBackgroundDialog() {
        mFlag = FishPreferences.getBgImageEffect(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.text_effect);
        builder.setSingleChoiceItems(R.array.text_effect_choices, mFlag,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFlag = which;
                    }
                });
        builder.setPositiveButton(R.string.button_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = FishPreferences
                        .getEditor(mContext);
                editor.putInt(FishPreferences.KEY_BG_EFFECT, mFlag);
                editor.commit();

                ImageUtil.doPickPhotoFromGallery((Activity) mContext,
                        REQUEST_PICK_BG, ICON_SIZE_BG, BG_FILENAME);
            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.show();
    }

    private void promptAuthorDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_input_text, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle(R.string.tv_input_author);
        final EditText etAuthor = (EditText) view.findViewById(R.id.et_dialog);
        etAuthor.setHint(FishPreferences.getAuthorName(mContext));
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.setPositiveButton(R.string.button_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (etAuthor.getText().toString().trim().equals("")) {
                    SharedPreferences.Editor editor = FishPreferences
                            .getEditor(mContext);
                    editor.putString(FishPreferences.KEY_AUTHOR, etAuthor.getText()
                            .toString());
                    editor.commit();
                }
                Toast.makeText(mContext, R.string.text_author_saved,
                        Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
    /**
     * TODO: Well, these junk codes work and pass tests...
     * 
     * @param checked
     *            the new checked state
     */
    private void promptPasswdDialog(final boolean checked) {
        PasswordDialog.IPasswdCallBack callback = new IPasswdCallBack() {
            @Override
            public void posButtonClicked() {
                // Set password
                if (checked) {
                    // the common "are you sure?" routine: think twice, stupid user
                    confirmPasswdSave();
                }

                // Remove password
                if (!checked) {
                    if (mPassword.equals(FishPreferences.getPassword(mContext))) {
                        mPasswdPref.setChecked(false);
                        // password reset to null
                        mPassword = null;
                        SharedPreferences.Editor editor = FishPreferences
                                .getEditor(mContext);
                        editor.putString(FishPreferences.KEY_PASSWORD, mPassword);
                        editor.commit();
                    } else {
                        // incorrect password
                        PasswordDialog.prompPasswdErrDialog(mContext);
                        mPasswdPref.setChecked(true);
                    }
                }
            }
            @Override
            public void negButtonClicked() {
                // Keep the preview state
                mPasswdPref.setChecked(!checked);
            }
            @Override
            public void getPassword(String password) {
                mPassword = password;
            }
        };
        new PasswordDialog(this, callback);
    }

    /** You have a second chance. */
    private void confirmPasswdSave() {
        String msg = getResources().getString(R.string.text_passwd_confirm)
                + mPassword;
        BaseStyleDialog.ICallBack callback = new ICallBack() {
            @Override
            public void posButtonClicked() {
                SharedPreferences.Editor editor = FishPreferences
                        .getEditor(mContext);
                editor.putString(FishPreferences.KEY_PASSWORD, mPassword);
                editor.commit();
            }
            @Override
            public void negButtonClicked() {
                mPasswdPref.setChecked(false);
            }
        };
        BaseStyleDialog.buildInfoDialog(this, msg, callback);
    }

    /**
     * Prompt the restore settings dialog.
     * Still, are you sure?
     */
    private void restoreSettingsDialog() {
        BaseStyleDialog.ICallBack callback = new ICallBack() {
            String _passwd = "";

            @Override
            public void posButtonClicked() {
                // If user has set password, prompt password dialog
                if (FishPreferences.getPassword(mContext) != null) {
                    PasswordDialog.IPasswdCallBack callback = new IPasswdCallBack() {
                        @Override
                        public void posButtonClicked() {
                            // password correct, reset everything including password
                            if (_passwd
                                    .equals(FishPreferences.getPassword(mContext))) {
                                restoreSettings();
                            } else {
                                PasswordDialog.prompPasswdErrDialog(mContext);
                            }
                        }
                        @Override
                        public void negButtonClicked() {
                            // user cancel
                        }
                        @Override
                        public void getPassword(String password) {
                            _passwd = password;
                        }
                    };
                    new PasswordDialog(mContext, callback);
                } else {
                    restoreSettings();
                    Toast.makeText(mContext, R.string.text_restored_success,
                            Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void negButtonClicked() {
                // user cancel
            }
        };
        BaseStyleDialog.buildInfoDialog(this, R.string.pref_restore_text, callback);
    }

    /** Restore everything */
    private void restoreSettings() {
        String tagStr = FishPreferences.getNoteTag(mContext);
        boolean firstTime = FishPreferences.isFirstTime(mContext);

        SharedPreferences.Editor editor = FishPreferences.getEditor(mContext);
        editor.clear();
        editor.apply();

        editor.putString(FishPreferences.KEY_NOTE_TAG, tagStr); // keep note tag [fix]
        editor.putBoolean(FishPreferences.KEY_FIRST_TIME, firstTime); // keep firstTime
        editor.commit();
        refreshPrefs();
    }

    /** Refresh the UI */
    private void refreshPrefs() {
        getPreferenceScreen().removeAll();
        addPreferencesFromResource(R.xml.preferences);
        initPrefs();
    }
}
