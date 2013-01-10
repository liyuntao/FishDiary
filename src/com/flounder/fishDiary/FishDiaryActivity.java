package com.flounder.fishDiary;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.flounder.fishDiary.data.AboutText;
import com.flounder.fishDiary.data.Constants;
import com.flounder.fishDiary.data.FishDiary;
import com.flounder.fishDiary.data.LePetitPrinceText;
import com.flounder.fishDiary.data.TextContent;
import com.flounder.fishDiary.image.ImageUtil;
import com.flounder.fishDiary.util.Utils;
import com.flounder.fishDiary.view.BaseStyleDialog;
import com.flounder.fishDiary.view.BaseStyleDialog.ICallBack;
import com.flounder.fishDiary.view.LinedEditText;

/**
 * TODO: [Bug #1]:
 * When user navigates to NoteActivity (from ListActivity) pressing the INSERT button,
 * ListActivity refreshes with an inserted empty Note BEFORE NoteActivity gets loaded.
 * I have tried the following approach:
 * calling overridePendingTransition(0, 0) at onResume(), but things didn't get better.
 */
public class FishDiaryActivity extends Activity {

    private Button mBtnSave;
    private Button mBtnQuit;
    private LinedEditText mEtText;
    private EditText mEtTitle;

    /** Creates a projection that returns the note ID and the note contents */
    private static final String[] PROJECTION = new String[] {
            FishDiary.Notes._ID,
            FishDiary.Notes.COLUMN_NAME_TITLE,
            FishDiary.Notes.COLUMN_NAME_NOTE
    };

    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;
    private static final int STATE_VIEW = 2;
    private static final int STATE_CONTENT = 3;

    private int mContentFlag;
    private int mState;
    private Uri mUri;
    private QueryHandler mHandler;

    /** A label for the saved state of the activity */
    private static final String ORIGINAL_CONTENT = "origContent";
    private static final String ORIGINAL_TITLE = "origTitle";
    private String mOriginalContent;
    private String mOriginalTitle;

    private boolean isCanceled;
    private boolean isNightMode = false;
    private boolean isFullScreen = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fish_diary);

        mHandler = new QueryHandler(getContentResolver());
        handleIntent();

        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
            mOriginalTitle = savedInstanceState.getString(ORIGINAL_TITLE);
        }
    }

    private void handleIntent() {
        // an intent to use when the Activity object's result is sent back to the caller
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            mState = STATE_VIEW;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action)) {
            mState = STATE_INSERT;
            // insert an empty record in the provider
            mUri = getContentResolver().insert(intent.getData(), null);
            if (mUri == null) {
                Utils.loge("Failed to insert new note into " + getIntent().getData());
                Utils.showErrorAndFinish(this, R.string.error_unknown);
                return;
            }
            // Since the new entry was created, set the result to be returned
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
        } else if (Constants.ACTION_CONTENT.equals(action)) {
            mContentFlag = intent.getIntExtra(Constants.ACTION_CONTENT,
                    Constants.CONTENT_ABOUT);
            mState = STATE_CONTENT;
        } else {
            Utils.loge("Unknown action, exiting");
            finish();
        }
    }

    private void printContent() {
        TextContent content = null;
        switch (mContentFlag) {
        case Constants.CONTENT_ABOUT:
            content = new AboutText(this, mBtnSave, mBtnQuit, mEtText, mEtTitle);
            break;
        case Constants.CONTENT_READING:
            content = new LePetitPrinceText(this,
                    mBtnSave, mBtnQuit, mEtText, mEtTitle);
            break;
        }
        mEtText.setText(content.getContent(this));
        mEtTitle.setText(content.getTitle());
    }

    /** Set background image. */
    private void setBackground() {
        Bitmap bgImage = ImageUtil.getBackgroundImage(this);
        if (bgImage != null) {
            BitmapDrawable drawable = new BitmapDrawable(getResources(), bgImage);
            drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            drawable.setDither(true);
            mEtText.setBackgroundDrawable(drawable);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fish_diary_activity_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.options_menu_night_mode:
            switchToNightMode();
            break;
        case R.id.options_menu_full_screen:
            switchToFullScreen();
            break;
        }
        return true;
    }

    private void initView() {
        mEtText = (LinedEditText) findViewById(R.id.et_story);

        // If user switch to nightMode and onPause called,
        // underline will be drawn here [fix]
        if (!isNightMode)
            mEtText.setUnderLine(FishPreferences.isUnderlineEnable(this));

        mEtText.setDrawingCacheEnabled(true);
        mEtText.setTextSize(FishPreferences.getTextSize(this));
        mEtText.requestFocus();
        mEtTitle = (EditText) findViewById(R.id.et_title);
        mBtnSave = (Button) findViewById(R.id.btn_save);
        mBtnQuit = (Button) findViewById(R.id.btn_back);

        if (mState == STATE_CONTENT) {
            printContent();
            return;
        }

        if (!isNightMode)
            setBackground();

        if (mState == STATE_VIEW) {
            mBtnSave.setText(R.string.button_edit);
            mEtText.setFocusable(false);
            mEtText.setCursorVisible(false);
            mEtTitle.setFocusable(false);
        }

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == STATE_VIEW) { // set state to "edit", allow user input
                    mState = STATE_EDIT;
                    mBtnSave.setText(R.string.button_save);
                    mEtTitle.setFocusableInTouchMode(true);
                    mEtText.setFocusableInTouchMode(true);
                    mEtText.setCursorVisible(true);
                    mEtText.requestFocus();
                } else {
                    saveStory();    // set state to "view", disable user input
                    mState = STATE_VIEW;
                    mBtnSave.setText(R.string.button_edit);
                    mEtText.setFocusable(false);
                    mEtTitle.setFocusable(false);
                    mEtText.setCursorVisible(false);
                }
            }
        });

        mBtnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmQuit();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
        isCanceled = false;
        if (mState != STATE_CONTENT)
            mHandler.startQuery(0, null, mUri, PROJECTION, null, null, null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
        outState.putString(ORIGINAL_TITLE, mOriginalTitle);
    }

    /** Writes the user's work to the provider */
    @Override
    protected void onPause() {
        super.onPause();
        if (!needSave() || isCanceled) {
            setResult(RESULT_CANCELED);
        } else {
            updateNote(mEtText.getText().toString(), mEtTitle.getText().toString()
                    .trim());
        }
    }
    /** Replaces the current note contents with the text and title provided as arguments */
    private void updateNote(String text, String title) {
        // Sets up a map to contain values to be updated in the provider
        ContentValues values = new ContentValues();
        values.put(FishDiary.Notes.COLUMN_NAME_MODIFICATION_DATE,
                System.currentTimeMillis());
        values.put(FishDiary.Notes.COLUMN_NAME_TITLE, title);
        values.put(FishDiary.Notes.COLUMN_NAME_NOTE, text);
        mHandler.startUpdate(0, null, mUri, values, null, null);
    }

    /**
     * Cancels the work done on a note. It deletes the note if it was newly created, or
     * reverts to the original text of the note.
     * cancelNote() takes care of revert(STATE_EDIT)/delete(STATE_INSERT) operations,
     * add flag value to avoid duplicated update/delete at onPause().
     */
    private void cancelNote() {
        isCanceled = true;
        if (mState == STATE_EDIT) {
            // Put the original note text back into the database
            ContentValues values = new ContentValues();
            values.put(FishDiary.Notes.COLUMN_NAME_NOTE, mOriginalContent);
            values.put(FishDiary.Notes.COLUMN_NAME_TITLE, mOriginalTitle);
            mHandler.startUpdate(0, null, mUri, values, null, null);
        } else if (mState == STATE_INSERT) {
            // We inserted an empty note, make sure to delete it
            mHandler.startDelete(0, null, mUri, null, null);
        }
    }

    @Override
    public void onBackPressed() {
        confirmQuit();
    }

    private void saveStory() {
        String title = mEtTitle.getText().toString().trim();
        String text = mEtText.getText().toString();

        updateNote(text, title);

        // Update the state of note [fix]
        mOriginalContent = text;
        mOriginalTitle = title;
        mState = STATE_EDIT;

        Toast.makeText(this, R.string.hint_note_saved, Toast.LENGTH_SHORT).show();
    }

    private void confirmQuit() {
        if (needSave()) {
            showNeedSaveDialog();
        } else {
            finish();
        }
    }

    private boolean needSave() {
        if (mState == STATE_VIEW || mState == STATE_CONTENT)
            return false;

        String text = mEtText.getText().toString();
        String title = mEtTitle.getText().toString().trim();

        if (mState == STATE_INSERT) {
            if (TextUtils.isEmpty(mEtText.getText())
                    && TextUtils.isEmpty(mEtTitle.getText())) {
                // Delete inserted note here
                mHandler.startDelete(0, null, mUri, null, null);
                return false;
            }
        }
        if (mState == STATE_EDIT) {
            if (text.equals(mOriginalContent) && title.equals(mOriginalTitle)) {
                return false;
            }
        }
        return true;
    }

    /** Display a dialog to ask whether save current story */
    private void showNeedSaveDialog() {
        BaseStyleDialog.ICallBack callback = new ICallBack() {
            @Override
            public void posButtonClicked() {
                saveStory();
                finish();
            }
            @Override
            public void negButtonClicked() {
                cancelNote();
                finish();
            }
        };
        BaseStyleDialog
                .buildInfoDialog(this, R.string.dialog_confirm_save, callback, false);
    }

    private void switchToFullScreen() {
        isFullScreen = !isFullScreen;
        if (isFullScreen) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void switchToNightMode() {
        isNightMode = true;
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout1);
        layout.setBackgroundColor(getResources().getColor(
                R.color.night_mode_bgcolor));
        mEtText.setBackgroundColor(getResources().getColor(
                R.color.night_mode_bgcolor));
        mEtText.setTextColor(getResources().getColor(
                R.color.dark_light));
        mEtText.setUnderLine(false);
        mEtTitle.setTextColor(getResources().getColor(
                R.color.dark_light));
        mEtTitle.setHintTextColor(getResources().getColor(
                R.color.dark_light));
        // Set cornered button
        mBtnQuit.getBackground().setColorFilter(getResources().getColor(
                R.color.dark_light), android.graphics.PorterDuff.Mode.MULTIPLY);
        mBtnSave.getBackground().setColorFilter(getResources().getColor(
                R.color.dark_light), android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    /** Perform provider operations asynchronously on a handler thread */
    private class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);
            if (cursor != null) {
                cursor.moveToFirst();
                String note = cursor.getString(cursor
                        .getColumnIndex(FishDiary.Notes.COLUMN_NAME_NOTE));
                mEtText.setTextKeepState(note);
                String title = cursor.getString(cursor
                        .getColumnIndex(FishDiary.Notes.COLUMN_NAME_TITLE));
                mEtTitle.setTextKeepState(title);

                // Stores the original note text, to allow the user to revert changes
                if (mOriginalContent == null) {
                    mOriginalContent = note;
                    mOriginalTitle = title;
                }
            }
        }
    }
}