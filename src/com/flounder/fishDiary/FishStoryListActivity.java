/**
 * XeniaStoryList.java
 *
 * Ver 1.0, 2012-12-1, alex_yh, Create file.
 */
package com.flounder.fishDiary;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.flounder.fishDiary.data.Constants;
import com.flounder.fishDiary.data.FishDiary;
import com.flounder.fishDiary.data.FishDiaryProvider.DatebaseHelper;
import com.flounder.fishDiary.image.ImageUtil;
import com.flounder.fishDiary.util.FileUtils;
import com.flounder.fishDiary.util.Utils;
import com.flounder.fishDiary.view.BaseStyleDialog;
import com.flounder.fishDiary.view.PasswordDialog;
import com.flounder.fishDiary.view.BaseStyleDialog.ICallBack;
import com.flounder.fishDiary.view.PasswordDialog.IPasswdCallBack;

public class FishStoryListActivity extends ListActivity {

    private Button mBtnAdd;
    private ImageView mImage;

    private MyCursorAdapter mAdapter;
    private MyQueryHandler mHandler;

    private Context mContext;
    private String mExportedDir;

    private String mTagLists;
    private String[] mTagItems;

    public static final int QUERY_NOTES_TOKEN = 1;
    public static final int QUERY_SINGLE_TOKEN = 99;

    private static final String TIME_STAMP_NAME = "yyyy-MM-dd";
    private static final String TIME_STAMP_DEATAIL = "yyyy-MM-dd HH:mm";
    private static final String TIME_STAMP_RENAME = "MM-dd_HH-mm";

    /** The columns needed by the cursor adapter */
    private static final String[] PROJECTION = new String[] {
            FishDiary.Notes._ID,
            FishDiary.Notes.COLUMN_NAME_TITLE,
            FishDiary.Notes.COLUMN_NAME_TAG,
            FishDiary.Notes.COLUMN_NAME_AUTHOR,
            FishDiary.Notes.COLUMN_NAME_ENCRYTED,
            FishDiary.Notes.COLUMN_NAME_CREATE_DATE,
            FishDiary.Notes.COLUMN_NAME_MODIFICATION_DATE,
            FishDiary.Notes.COLUMN_NAME_NOTE };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);
        initView();

        // If no data is given in the Intent that started this Activity,
        // use the default provider URI.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(FishDiary.Notes.CONTENT_URI);
        }
        mContext = this;
        mAdapter = new MyCursorAdapter(this);
        mHandler = new MyQueryHandler(getContentResolver(), mAdapter);

        boolean firstTime = FishPreferences.isFirstTime(mContext);
        if (firstTime) {
            ContentValues value = new ContentValues();
            value.put(FishDiary.Notes.COLUMN_NAME_TITLE,
                    getString(R.string.text_welcome));
            value.put(FishDiary.Notes.COLUMN_NAME_NOTE,
                    FileUtils.readTextFromAssets(mContext,
                            Constants.FILENAME_WELCOME));
            value.put(FishDiary.Notes.COLUMN_NAME_TAG, "<Welcome>");
            mHandler.startInsert(0, null, getIntent().getData(), value);
            updateFirstTimePref(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);

        initHeadPhoto();
        checkEncryptState();    // check if password has been removed

        mHandler.startQuery(QUERY_NOTES_TOKEN, null, getIntent().getData(),
                PROJECTION, null, null, FishDiary.Notes.DEFAULT_SORT_ORDER);
        setListAdapter(mAdapter);
    }

    private void initView() {
        mBtnAdd = (Button) findViewById(R.id.btn_add);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertNote();
            }
        });

        /* Sets the callback for context menu activation for the ListView */
        getListView().setOnCreateContextMenuListener(this);
    }

    /** Set head icon */
    private void initHeadPhoto() {
        mImage = (ImageView) findViewById(R.id.image);
        mImage.setImageBitmap(ImageUtil.getHeadPhoto(this));
    }

    /** If password has been removed, update all entries first */
    private void checkEncryptState() {
        if (FishPreferences.getPassword(mContext) == null) {
            ContentValues diffValues = new ContentValues();
            diffValues.put(FishDiary.Notes.COLUMN_NAME_ENCRYTED, 0);
            mHandler.startUpdate(QUERY_NOTES_TOKEN, null, getIntent().getData(),
                    diffValues, null, null);
        }
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        if (viewEncryptedNote(uri))
            viewNote(uri);
    }

    private void viewNote(Uri uri) {
        Intent intent = new Intent(this, FishDiaryActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        startActivity(intent);
    }

    private boolean viewEncryptedNote(final Uri uri) {
        mHandler.startQuery(QUERY_SINGLE_TOKEN, null, uri, PROJECTION, null, null,
                null);
        Cursor cursor = mAdapter.getCursor();
        if (cursor.getInt(cursor
                .getColumnIndex(FishDiary.Notes.COLUMN_NAME_ENCRYTED)) == 0) {
            return true;
        }

        if (FishPreferences.getPassword(mContext) != null) {
            PasswordDialog.IPasswdCallBack callback = new IPasswdCallBack() {
                String _passwd = "";

                @Override
                public void posButtonClicked() {
                    if (_passwd.equals(FishPreferences.getPassword(mContext))) {
                        viewNote(uri);
                    } else {
                        PasswordDialog.prompPasswdErrDialog(mContext);
                    }
                }
                @Override
                public void negButtonClicked() {
                }
                @Override
                public void getPassword(String password) {
                    _passwd = password;
                }
            };
            new PasswordDialog(mContext, callback);
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity_context_menu, menu);
        // If no password has been set, disable the "encrypt" options
        if (FishPreferences.getPassword(mContext) == null) {
            menu.removeItem(R.id.context_menu_encrypt);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_activity_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.options_menu_insert:
            insertNote();
            break;
        case R.id.options_menu_settings:
            Intent intent = new Intent(this, FishSettingsActivity.class);
            startActivity(intent);
            break;
        case R.id.options_menu_export:
            initChecking(); // check environment first
            if (FishPreferences.getPassword(mContext) != null) {
                PasswordDialog.IPasswdCallBack callback = new IPasswdCallBack() {
                    String _passwd = "";

                    @Override
                    public void posButtonClicked() {
                        if (_passwd.equals(FishPreferences.getPassword(mContext))) {
                            exportNote();
                        } else {
                            PasswordDialog.prompPasswdErrDialog(mContext);
                        }
                    }
                    @Override
                    public void negButtonClicked() {
                    }
                    @Override
                    public void getPassword(String password) {
                        _passwd = password;
                    }
                };
                new PasswordDialog(mContext, callback);
            } else {
                exportNote();
            }
            break;
        case R.id.options_menu_quit:
            confirmQuit();
            break;
        }
        return true;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), info.id);
        switch (item.getItemId()) {
        case R.id.context_menu_tag:
            setTag(noteUri);
            return true;
        case R.id.context_menu_encrypt:
            mHandler.startQuery(QUERY_SINGLE_TOKEN, null, noteUri, PROJECTION, null,
                    null, null);
            Cursor cursor = mAdapter.getCursor();
            int flag = cursor.getInt(cursor
                    .getColumnIndex(FishDiary.Notes.COLUMN_NAME_ENCRYTED));
            setNoteEncrypted(noteUri, flag != 1);
            return true;
        case R.id.context_menu_delete:
            confirmDelete(noteUri);
            return true;
        case R.id.context_menu_copy:
            copyNote(noteUri);
            return true;
        default:
            return super.onContextItemSelected(item);
        }
    }

    private void setNoteEncrypted(final Uri uri, final boolean flag) {
        if (!flag) {    // unencrypt
            PasswordDialog.IPasswdCallBack callback = new IPasswdCallBack() {
                String _passwd = "";

                @Override
                public void posButtonClicked() {
                    if (_passwd.equals(FishPreferences.getPassword(mContext))) {
                        updateEncryptState(uri, false);
                    } else {
                        PasswordDialog.prompPasswdErrDialog(mContext);
                    }
                }
                @Override
                public void negButtonClicked() {
                }
                @Override
                public void getPassword(String password) {
                    _passwd = password;
                }
            };
            new PasswordDialog(mContext, callback);
        }
        // encrypt note
        updateEncryptState(uri, true);
    }

    private void updateEncryptState(Uri uri, boolean flag) {
        ContentValues values = new ContentValues();
        values.put(FishDiary.Notes.COLUMN_NAME_ENCRYTED, (flag ? 1 : 0));
        mHandler.startUpdate(0, null, uri, values, null, null);

        // For refresh
        mHandler.startQuery(QUERY_NOTES_TOKEN, null, getIntent().getData(),
                PROJECTION, null, null, FishDiary.Notes.DEFAULT_SORT_ORDER);
    }

    /**
     * TODO: Two operation "Manage Tag(list)" and "Set (single Note's)Tag" are combined
     * here into a single scenario.
     * It's NOT good... They are two completely different use-cases.
     * However there would be too much repeated code if separating the use's entrance.
     */
    private void setTag(final Uri uri) {
        String[] noteTagSelected;
        final Set<Integer> selectedIndexSet = new HashSet<Integer>();

        // get note's Tag, and split into string array
        mHandler.startQuery(QUERY_SINGLE_TOKEN, null, uri, PROJECTION,
                null, null, null);
        Cursor cursor = mAdapter.getCursor();
        String noteTagStr = cursor.getString(cursor
                .getColumnIndex(FishDiary.Notes.COLUMN_NAME_TAG));

        noteTagSelected = noteTagStr.split(Constants.TAG_SEPERATOR);

        // get global Tag lists (from SharedPref)
        mTagLists = FishPreferences.getNoteTag(this);

        // checkedItems for MultiChoiceDialog
        List<Boolean> _selected = new ArrayList<Boolean>();
        boolean[] selected = null;

        if (mTagLists != null && !mTagLists.equals("")) {
            // compare noteTagSelected to TagList
            mTagItems = mTagLists.split(Constants.TAG_SEPERATOR);
            for (int i = 0; i < mTagItems.length; i++) {
                String str = mTagItems[i];
                _selected.add(i, false);
                for (String tag : noteTagSelected) {
                    if (str.equals(tag)) {
                        selectedIndexSet.add(i);
                        _selected.add(i, true);
                    }
                }
            }

            // and set result to array selected(boolean[])
            selected = Utils.toPrimitiveArray(_selected);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (mTagLists == null || mTagLists.equals(""))
            // for a better looking dialog
            builder.setMessage(R.string.dialog_create_tag);

        builder.setMultiChoiceItems(mTagItems, selected,
                new OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                            boolean isChecked) {
                        if (isChecked) {
                            if (!selectedIndexSet.contains(which))
                                selectedIndexSet.add(which);
                        }

                        if (!isChecked) {
                            if (selectedIndexSet.contains(which))
                                selectedIndexSet.remove(which);
                        }
                    }
                });

        builder.setPositiveButton(R.string.button_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mTagItems == null)  // Fix NPE problems
                    return;

                StringBuilder strBuilder = new StringBuilder();
                for (int i = 0; i < mTagItems.length; i++) {
                    if (selectedIndexSet.contains(i))
                        strBuilder
                                .append(mTagItems[i] + Constants.TAG_SEPERATOR);
                }
                saveTag(strBuilder.toString(), uri);
            }
        });
        if (mTagLists != null && !mTagLists.equals("")) {
            builder.setNegativeButton(R.string.button_delete, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    StringBuilder strBuilder = new StringBuilder();
                    // mTagItems can't be null here, needless do the null check
                    for (int i = 0; i < mTagItems.length; i++) {
                        if (selectedIndexSet.contains(i)) {
                            String str = mTagItems[i] + Constants.TAG_SEPERATOR;
                            String sql = "UPDATE " + FishDiary.Notes.TABLE_NAME
                                    + " SET "
                                    + FishDiary.Notes.COLUMN_NAME_TAG + "="
                                    + "REPLACE(" + FishDiary.Notes.COLUMN_NAME_TAG
                                    + ",'" + str + "','')";

                            Utils.logd(sql);
                            DatebaseHelper dbHelper = new DatebaseHelper(mContext);
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            db.execSQL(sql);
                            dbHelper.close();   // everything closed?
                        }

                        if (!selectedIndexSet.contains(i)) {
                            strBuilder
                                    .append(mTagItems[i] + Constants.TAG_SEPERATOR);
                        }
                    }

                    // For refresh
                    mHandler.startQuery(QUERY_NOTES_TOKEN, null, getIntent()
                            .getData(), PROJECTION, null, null,
                            FishDiary.Notes.DEFAULT_SORT_ORDER);

                    SharedPreferences.Editor editor = FishPreferences
                            .getEditor(mContext);
                    editor.putString(FishPreferences.KEY_NOTE_TAG,
                            strBuilder.toString().trim());
                    editor.commit();
                }
            });
        }
        builder.setNeutralButton(R.string.button_add, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                promptTagDialog();
            }
        });
        builder.show();
    }

    private void promptTagDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_input_text, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setView(view);
        builder.setTitle(R.string.dialog_create_tag);
        final EditText etTag = (EditText) view.findViewById(R.id.et_dialog);
        builder.setNegativeButton(R.string.button_cancel, null);
        builder.setPositiveButton(R.string.button_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tagText = etTag.getText().toString().trim();
                // check for duplication
                if (mTagItems != null)
                    for (String str : mTagItems) {
                        if (tagText.equals(str))
                            return;
                    }

                if (!tagText.equals("")) {
                    mTagLists = (mTagLists == null || mTagLists.equals("")) ?
                            tagText : mTagLists + Constants.TAG_SEPERATOR + tagText;

                    SharedPreferences.Editor editor = FishPreferences
                            .getEditor(mContext);
                    editor.putString(FishPreferences.KEY_NOTE_TAG, mTagLists);
                    editor.commit();
                    Toast.makeText(mContext, R.string.text_tag_added,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }

    private void saveTag(String str, Uri uri) {
        ContentValues values = new ContentValues();
        values.put(FishDiary.Notes.COLUMN_NAME_TAG, str);
        mHandler.startUpdate(0, null, uri, values, null, null);
        // For refresh
        mHandler.startQuery(QUERY_NOTES_TOKEN, null, getIntent().getData(),
                PROJECTION, null, null, FishDiary.Notes.DEFAULT_SORT_ORDER);
    }

    private void insertNote() {
        Intent intent = new Intent(FishStoryListActivity.this,
                FishDiaryActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.setData(getIntent().getData());
        startActivity(intent);
    }

    private void initChecking() {
        if (!FileUtils.isSDCardMounted()) {
            Utils.showErrorAndFinish(this, R.string.error_sdcard_unmount);
        }
        if (!FileUtils.isRootFolderCreated()) {
            Utils.showErrorAndFinish(this, R.string.error_failed_create_folder);
        }
    }

    private String makeExportFolder() {
        String date = new SimpleDateFormat(TIME_STAMP_NAME).format(new Date());
        File dir = new File(FileUtils.getRootFolder(), date);
        if (!dir.exists() || !dir.isDirectory())
            dir.mkdirs();

        return date;
    }

    private void exportNote() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                saveNoteToText();
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                String msg = getString(R.string.text_exported)
                        + FileUtils.getRootFolder() + File.separator + mExportedDir;
                BaseStyleDialog.buildMsgDialog(mContext, msg);
            }
        }.execute();
    }

    private void saveNoteToText() {
        mExportedDir = makeExportFolder();
        Cursor cursor = mAdapter.getCursor();

        int encryptStateFlag;
        long lCreateDate, lModifyDate;
        String authorName, noteName, noteContent, createDate, modifyDate, encryptState, noteTag;
        String createDateStr = getString(R.string.export_text_createAt);
        String modifyDateStr = getString(R.string.export_text_modifyAt);
        String tagStr = getString(R.string.export_text_tag);
        String authorStr = getString(R.string.export_text_author);
        String encryptStr = getString(R.string.export_text_encrypt);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                // Get note createDate
                lCreateDate = cursor.getLong(cursor
                        .getColumnIndex(FishDiary.Notes.COLUMN_NAME_CREATE_DATE));
                createDate = new SimpleDateFormat(TIME_STAMP_DEATAIL)
                        .format(new Date(lCreateDate));
                // Get noteName, rename name if invalid for fileName
                noteName = cursor.getString(cursor
                        .getColumnIndex(FishDiary.Notes.COLUMN_NAME_TITLE));
                if (!FileUtils.isFileNameValid(noteName))
                    // noteName should be different [fix]
                    noteName = "note_" + new SimpleDateFormat(TIME_STAMP_RENAME)
                            .format(new Date(lCreateDate));
                // Get noteContent
                noteContent = cursor.getString(cursor
                        .getColumnIndex(FishDiary.Notes.COLUMN_NAME_NOTE));
                // Get note modifyDate
                lModifyDate = cursor
                        .getLong(cursor
                                .getColumnIndex(FishDiary.Notes.COLUMN_NAME_MODIFICATION_DATE));
                modifyDate = new SimpleDateFormat(TIME_STAMP_DEATAIL)
                        .format(new Date(lModifyDate));
                // Get author name
                authorName = cursor.getString(cursor
                        .getColumnIndex(FishDiary.Notes.COLUMN_NAME_AUTHOR));
                // Get encrypt state
                encryptStateFlag = cursor.getInt(cursor
                        .getColumnIndex(FishDiary.Notes.COLUMN_NAME_ENCRYTED));
                encryptState = (encryptStateFlag == 1) ? getString(R.string.yes)
                        : getString(R.string.no);

                // Get Note tag
                noteTag = "";
                String _noteTag = cursor.getString(cursor
                        .getColumnIndex(FishDiary.Notes.COLUMN_NAME_TAG));
                for (String s : _noteTag.split(Constants.TAG_SEPERATOR))
                    noteTag += (s + " ");

                StringBuilder builder = new StringBuilder(noteContent);
                builder.append("\n\n");
                builder.append("[" + authorStr + authorName + "]\n");
                builder.append("[" + createDateStr + createDate + "]\n");
                builder.append("[" + modifyDateStr + modifyDate + "]\n");
                if (!noteTag.equals(""))    // remove this line if no tag
                    builder.append("[" + tagStr + noteTag + "]\n");
                builder.append("[" + encryptStr + encryptState + "]");
                FileUtils.saveTextToFile(noteName, mExportedDir, builder.toString());
                cursor.moveToNext();
            }
        }
    }

    private void copyNote(Uri uri) {
        mHandler.startQuery(QUERY_SINGLE_TOKEN, null, uri, PROJECTION,
                null, null, null);
        Cursor _cursor = mAdapter.getCursor();
        if (_cursor != null) {
            _cursor.moveToFirst();
            String note = _cursor.getString(_cursor
                    .getColumnIndex(FishDiary.Notes.COLUMN_NAME_NOTE));
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("Note", note));
            Toast.makeText(mContext, R.string.text_copied, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void deleteNote(Uri uri) {
        mHandler.startDelete(0, null, uri, null, null);
        mHandler.startQuery(QUERY_NOTES_TOKEN, null, getIntent().getData(),
                PROJECTION, null, null, FishDiary.Notes.DEFAULT_SORT_ORDER);
    }

    private void confirmDelete(final Uri uri) {
        BaseStyleDialog.ICallBack callback = new ICallBack() {
            @Override
            public void posButtonClicked() {
                deleteNote(uri);
            }
            @Override
            public void negButtonClicked() {
            }
        };
        BaseStyleDialog.buildInfoDialog(this, R.string.dialog_confirm_delete,
                callback);
    }

    private void confirmQuit() {
        BaseStyleDialog.ICallBack callback = new ICallBack() {
            @Override
            public void posButtonClicked() {
                finish();
            }
            @Override
            public void negButtonClicked() {
            }
        };
        BaseStyleDialog
                .buildInfoDialog(this, R.string.dialog_confirm_quit, callback);
    }

    @Override
    public void onBackPressed() {
        confirmQuit();
    }

    private void updateFirstTimePref(boolean value) {
        SharedPreferences.Editor editor = FishPreferences.getEditor(mContext);
        editor.putBoolean(FishPreferences.KEY_FIRST_TIME, value);
        editor.commit();
    }

    /** Perform provider operations asynchronously on a handler thread */
    private class MyQueryHandler extends AsyncQueryHandler {
        private MyCursorAdapter mAdapter;

        public MyQueryHandler(ContentResolver cr, MyCursorAdapter adapter) {
            super(cr);
            this.mAdapter = adapter;
        }
        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);
            mAdapter.notifyDataSetChanged();
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);
            if (token == QUERY_NOTES_TOKEN) {
                mAdapter.changeCursor(cursor);
            }
        }
    }
}