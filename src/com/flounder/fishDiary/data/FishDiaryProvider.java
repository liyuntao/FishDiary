/**
 * FishDiaryProvider.java
 *
 * Ver 1.0, 2012-12-22, alex_yh, Create file.
 */
package com.flounder.fishDiary.data;

import java.util.HashMap;

import com.flounder.fishDiary.FishPreferences;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;

public class FishDiaryProvider extends ContentProvider {

    private static final String DB_NAME = "fish_diary.db";
    private static final int DB_VERSION = 1;

    // Constants used by the URI matcher to choose an action
    // based on the pattern of the incoming URI
    /** The incoming URI matches the Notes URI pattern */
    private static final int NOTES = 1;

    /** The incoming URI matches the Note ID URI pattern */
    private static final int NOTE_ID = 2;

    /** A projection map used to select columns from the database */
    private static HashMap<String, String> sNotesProjectionMap;

    private static final UriMatcher sUriMatcher;
    static {
        // Creates and initializes the URI matcher
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /** Add a pattern that routes URIs terminated with "notes" to a NOTES operation */
        sUriMatcher.addURI(FishDiary.AUTHORITY, "notes", NOTES);

        /**
         * Add a pattern that routes URIs terminated with "notes" plus an integer to a
         * note ID operation
         */
        sUriMatcher.addURI(FishDiary.AUTHORITY, "notes/#", NOTE_ID);

        // Creates and initialized a projection map that returns all columns
        /**
         * Creates a new projection map instance.
         * The map returns a column name given a string. The two are usually equal
         */
        sNotesProjectionMap = new HashMap<String, String>();

        /** Maps the string "_ID" to the column name "_ID" */
        sNotesProjectionMap.put(FishDiary.Notes._ID, FishDiary.Notes._ID);

        /** Maps "title" to "title" */
        sNotesProjectionMap.put(FishDiary.Notes.COLUMN_NAME_TITLE,
                FishDiary.Notes.COLUMN_NAME_TITLE);

        /** Maps "note" to "note" */
        sNotesProjectionMap.put(FishDiary.Notes.COLUMN_NAME_NOTE,
                FishDiary.Notes.COLUMN_NAME_NOTE);

        /** Maps "tag" to "tag" */
        sNotesProjectionMap.put(FishDiary.Notes.COLUMN_NAME_TAG,
                FishDiary.Notes.COLUMN_NAME_TAG);

        /** Maps "author" to "author" */
        sNotesProjectionMap.put(FishDiary.Notes.COLUMN_NAME_AUTHOR,
                FishDiary.Notes.COLUMN_NAME_AUTHOR);

        /** Maps "encrypted" to "encrypted" */
        sNotesProjectionMap.put(FishDiary.Notes.COLUMN_NAME_ENCRYTED,
                FishDiary.Notes.COLUMN_NAME_ENCRYTED);

        /** Maps "created" to "created" */
        sNotesProjectionMap.put(FishDiary.Notes.COLUMN_NAME_CREATE_DATE,
                FishDiary.Notes.COLUMN_NAME_CREATE_DATE);

        /** Maps "modified" to "modified" */
        sNotesProjectionMap.put(FishDiary.Notes.COLUMN_NAME_MODIFICATION_DATE,
                FishDiary.Notes.COLUMN_NAME_MODIFICATION_DATE);
    }

    private static final String CREATE_TABLE = "CREATE TABLE "
            + FishDiary.Notes.TABLE_NAME + " ("
            + FishDiary.Notes._ID + " INTEGER PRIMARY KEY,"
            + FishDiary.Notes.COLUMN_NAME_TITLE + " TEXT,"
            + FishDiary.Notes.COLUMN_NAME_NOTE + " TEXT,"
            + FishDiary.Notes.COLUMN_NAME_TAG + " TEXT,"
            + FishDiary.Notes.COLUMN_NAME_AUTHOR + " TEXT,"
            + FishDiary.Notes.COLUMN_NAME_ENCRYTED + " INTEGER,"
            + FishDiary.Notes.COLUMN_NAME_CREATE_DATE + " INTEGER,"
            + FishDiary.Notes.COLUMN_NAME_MODIFICATION_DATE + " INTEGER" + ");";

    private DatebaseHelper mOpenHelper;

    /** This class helps open, create, and upgrade the database file. */
    public static class DatebaseHelper extends SQLiteOpenHelper {

        public DatebaseHelper(Context context) {
            // calls the super constructor, requesting the default cursor factory
            super(context, DB_NAME, null, DB_VERSION);
        }

        /**
         * Creates the underlying database with table name and column names taken from the
         * FishDiary class
         */
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /** Initialized the provider by creating a new DatabaseHelper */
    @Override
    public boolean onCreate() {
        // creates a new helper object. Note that the database itself isn't opened until
        // something tries to access it, and it's only created if it doesn't already exist
        mOpenHelper = new DatebaseHelper(getContext());

        // assumes that any failures will be reported by a thrown exception
        return true;
    }

    /**
     * Deletes records from the database.
     * This is called when a client calls ContenteResolver.delete(Uri, String, String[]).
     * If the incoming URI matches the note ID URI pattern,
     * this method deletes the one record specified by the ID in the URI.
     * Otherwise, it deletes a set of records.
     * The record or records must also match the input selection criteria specified by
     * where and whereArgs.
     * 
     * If rows were deleted, then listeners are notified of the change.
     * 
     * @return If a "where" clause is used, the number of rows affected is returned.
     *         Otherwise 0 is returned.
     *         To delete all rows and get a row count, use "1" as the where clause.
     * 
     * @throws IllegalArgumentException
     *             if the incoming URI pattern is invalid.
     */
    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        // opens the database object in "write" mode
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String finalWhere;
        int count;

        // does the delete based on the incoming URI pattern.
        switch (sUriMatcher.match(uri)) {

        // If the incoming pattern matches the general pattern for notes,
        // does a delete based on the incoming "where" columns and arguments
        case NOTES:
            count = db.delete(
                    FishDiary.Notes.TABLE_NAME, // The database table name
                    where,                      // The incoming where clause column names
                    whereArgs                   // The incoming where clause values
                    );
            break;

        // If the incoming URI matches a single note ID
        // does the delete based on the incoming data,
        // but modifies the where clause to restrict it to the particular note ID
        case NOTE_ID:
            // starts a final WHERE clause by restricting it to the desired note ID
            finalWhere = FishDiary.Notes._ID
                    + " = "
                    + uri.getPathSegments().get(
                            FishDiary.Notes.NOTE_ID_PATH_POSITION);
            // if there were additional selection criteria, append them to the final WHERE
            // clause
            if (where != null) {
                finalWhere = finalWhere + " AND " + where;
            }

            // performs the delete
            count = db.delete(FishDiary.Notes.TABLE_NAME, finalWhere, whereArgs);
            break;

        // If the incoming pattern is invalid, throws an exception
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Gets a handle to the content resolver object for the current context
        // and notifies it that the incoming URI changed.
        // The object passed this along to the resolver framework,
        // and observers that have registered themselves for the provider are notified
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    /**
     * This is called when a client calls ContentResolver.getType(Uri).
     * Returns the MIME data type of the URI given as a parameter
     * 
     * @param uri
     *            The URI whose MIME type is desired.
     * @return The MIME type of the URI
     * @throws IllegalArgumentException
     *             if the incoming URI pattern is invalid
     */
    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        // if the pattern is for notes, returns the general content type
        case NOTES:
            return FishDiary.Notes.CONTENT_TYPE;

            // if the pattern is for note IDs, returns the note ID content type
        case NOTE_ID:
            return FishDiary.Notes.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**
     * This is called when a client calls ContentResolver.insert(Uri, ContentValues).
     * Inserts a new row into the database. This method sets up default values for any
     * columns that are not included in the incoming map.
     * If rows were inserted, then listeners are notified of the change.
     * 
     * @return The row ID of the inserted row.
     * @throws SQLException
     *             if the insertion fails
     */
    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // validates the incoming URI. Only the full provider URI is allowd for inserts
        if (sUriMatcher.match(uri) != NOTES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // a map to hold the new record's values
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        // if the values map doesn't contain the creation/modification data
        // sets the value to the current time
        Long now = Long.valueOf(System.currentTimeMillis());
        if (values.containsKey(FishDiary.Notes.COLUMN_NAME_CREATE_DATE) == false) {
            values.put(FishDiary.Notes.COLUMN_NAME_CREATE_DATE, now);
        }
        if (values.containsKey(FishDiary.Notes.COLUMN_NAME_MODIFICATION_DATE) == false) {
            values.put(FishDiary.Notes.COLUMN_NAME_MODIFICATION_DATE, now);
        }

        // if the values map doesn't contain a title, set the value to empty
        if (values.containsKey(FishDiary.Notes.COLUMN_NAME_TITLE) == false) {
            values.put(FishDiary.Notes.COLUMN_NAME_TITLE, "");
        }

        // if the values map doesn't contain a tag, set the value to empty
        if (values.containsKey(FishDiary.Notes.COLUMN_NAME_TAG) == false) {
            values.put(FishDiary.Notes.COLUMN_NAME_TAG, "");
        }

        // insert author name
        if (values.containsKey(FishDiary.Notes.COLUMN_NAME_AUTHOR) == false) {
            values.put(FishDiary.Notes.COLUMN_NAME_AUTHOR,
                    FishPreferences.getAuthorName(getContext()));
        }

        if (values.containsKey(FishDiary.Notes.COLUMN_NAME_ENCRYTED) == false) {
            values.put(FishDiary.Notes.COLUMN_NAME_ENCRYTED, 0);
        }

        // if the values map dosen't contain note text, set the value to an empty string
        if (values.containsKey(FishDiary.Notes.COLUMN_NAME_NOTE) == false) {
            values.put(FishDiary.Notes.COLUMN_NAME_NOTE, "");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(FishDiary.Notes.TABLE_NAME,
                FishDiary.Notes.COLUMN_NAME_NOTE, values);

        // if the insert succeeded, the rowId exists
        if (rowId > 0) {
            // create a URI with the note ID pattern and the new row ID appended to it
            Uri noteUri = ContentUris.withAppendedId(
                    FishDiary.Notes.CONTENT_ID_URI_BASE, rowId);
            // notifies observers registered against this provider that the data changed
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        // if the insert didn't succeed, the the rowId is <= 0. Throws an exception
        throw new SQLException("Failed to insert row into " + uri);
    }
    /**
     * This method is called when a client calls ContentResolver.query(Uri, String[],
     * String, String[], String). Queries the database and returns a cursor containing the
     * results.
     * 
     * @return A cursor containing the results of the query. The cursor exists but is
     *         empty if the query returns no results or an exception occurs.
     * @throws IllegalArgumentException
     *             if the incoming URI pattern is invalid.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        // Constructs a new query builder and sets its table name
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FishDiary.Notes.TABLE_NAME);

        // Choose the projection and adjust the "where" clause based on URI
        // pattern-matching
        switch (sUriMatcher.match(uri)) {
        // If the incoming URI is for notes, chooses the Notes projection
        case NOTES:
            builder.setProjectionMap(sNotesProjectionMap);
            break;
        // If the incoming URI is for a single note identified by its ID, chooses the note
        // ID projection, and appends "_ID = <noteID>" to the where clause, so that it
        // selects that single note
        case NOTE_ID:
            builder.setProjectionMap(sNotesProjectionMap);
            builder.appendWhere(FishDiary.Notes._ID
                    + "="
                    + uri.getPathSegments().get(
                            FishDiary.Notes.NOTE_ID_PATH_POSITION));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified, uses the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = FishDiary.Notes.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        // Performs the query, If no problems occur trying to read the database, then a
        // Cursor object is returned; otherwise, the cursor variable contains null.
        // If no records were selected, then the Cursor object is empty, and
        // Cursor.getCount() returns 0.
        Cursor c = builder.query(db, projection, selection,
                selectionArgs, null, null,
                orderBy);

        // Tells the Cursor what URI to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    /**
     * This is called when a client calls ContentResolver.update(Uri, ContentValues,
     * String, String[])
     * Updates records in the database. The column names specified by the keys in the
     * values map are updated with new data specified by the values in the map.
     * If the incoming URI matches the note ID URI pattern, then the method updates the
     * one record specified by the ID in the URI;
     * otherwise, it updates a set of records. The record or records must match the input
     * selection criteria specified by where and whereArgs.
     * If rows were updated, then listeners are notified of the change.
     * 
     * @param uri
     *            The URI pattern to match and update.
     * @param values
     *            A map of column names (keys) and new values (values).
     * @param where
     *            An SQL "WHERE" clause that selects records based on their column values.
     *            If this is null, then records that match the URI pattern are selected.
     * @param whereArgs
     *            An array of selection criteria. If the "where" param contains value
     *            placeholders("?"), then each placeholder is replaced by the
     *            corresponding element in the array.
     * @return The number of rows updated.
     * @throws IllegalArgumentException
     *             if the incoming URI pattern is invalid.
     */
    @Override
    public int update(Uri uri, ContentValues values, String where,
            String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count;
        String finalWhere;

        // Does the update based on the incoming URI pattern
        switch (sUriMatcher.match(uri)) {
        // if the incoming URI matches the general notes pattern,
        // does the update based on the incoming data
        case NOTES:
            count = db.update(FishDiary.Notes.TABLE_NAME, values, where, whereArgs);
            break;
        // if the incoming URI matches a single note ID
        // does the update based on the incoming data
        // but modifies the where clause to restrict it to the particular note ID.
        case NOTE_ID:
            finalWhere = FishDiary.Notes._ID
                    + " = "
                    + uri.getPathSegments().get(
                            FishDiary.Notes.NOTE_ID_PATH_POSITION);
            if (where != null) {
                finalWhere = finalWhere + " AND " + where;
            }

            count = db.update(FishDiary.Notes.TABLE_NAME, values, finalWhere,
                    whereArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}