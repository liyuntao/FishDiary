/**
 * FishDiary.java
 *
 * Ver 1.0, 2012-12-22, alex_yh, Create file.
 */
package com.flounder.fishDiary.data;

import android.net.Uri;
import android.provider.BaseColumns;

public final class FishDiary {

    public static final String AUTHORITY = "com.flounder.provider.FishDiary";

    /** This class cannot be instantiated */
    private FishDiary() {
    }

    public static final class Notes implements BaseColumns {

        /** The table name offered by this provider */
        public static final String TABLE_NAME = "notes";

        // Column definitions
        /**
         * Column name for the title of the note
         * Type: TEXT
         */
        public static final String COLUMN_NAME_TITLE = "title";

        /**
         * Column name for the note content
         * Type: TEXT
         */
        public static final String COLUMN_NAME_NOTE = "note";

        /**
         * Column name for the creation timestamp
         * Type: INTEGER (long from System.currentTimeMillis())
         */
        public static final String COLUMN_NAME_CREATE_DATE = "created";

        /**
         * Column name for the modification timestamp
         * Type: INTEGER (long from System.currentTimeMillis())
         */
        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";

        /**
         * Column name for the tag
         * Type: TEXT
         */
        public static final String COLUMN_NAME_TAG = "tag";

        /**
         * Column name for author name
         * Type: TEXT
         */
        public static final String COLUMN_NAME_AUTHOR = "author";

        /**
         * Column name for encrypt
         * Type: INTEGER (1 for encrypted, 0 for not)
         */
        public static final String COLUMN_NAME_ENCRYTED = "encrypted";

        /** The default sort order for this table */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";

        // URI definitions
        /** The scheme part for this provider's URI */
        private static final String SCHEME = "content://";

        /** Path part for the Notes URI */
        private static final String PATH_NOTES = "/notes";

        /** Path part for the Note ID URI */
        private static final String PATH_NOTE_ID = "/notes/";

        /** The Content:// style URI for this table */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY
                + PATH_NOTES);

        /**
         * The content URI base for a single note. Caller must append a numeric
         * note id to this Uri to retrieve a note
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY
                + PATH_NOTE_ID);

        /**
         * The content URI match pattern for a single note, specified by its ID.
         * Use this to match incoming URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME
                + AUTHORITY + PATH_NOTE_ID + "/#");

        /** 0-relative position of a note ID segment in the path part of a note ID URI */
        public static final int NOTE_ID_PATH_POSITION = 1;

        // MIME type definitions
        /** The MIME type of CONTENT_URI, providing a directory of notes */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.note";

        /** The MIME type of CONTENT_URI, sub-directory of a single note */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.note";
    }
}
