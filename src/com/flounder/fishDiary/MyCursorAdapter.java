package com.flounder.fishDiary;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.flounder.fishDiary.data.Constants;
import com.flounder.fishDiary.data.FishDiary;

public class MyCursorAdapter extends CursorAdapter {

    private SpannableStringBuilder mBuilder;
    private LayoutInflater mInflater;

    private static final String TIME_STAMP_NAME = "yyyy-MM-dd HH:mm";

    private static final int SPANNABLE_TITLE = 1;
    private static final int SPANNABLE_DATE = 2;
    private static final int SPANNABLE_TEXT = 3;

    private static final int MAX_LENGTH = 40;

    private static final String COLOR_GREY31 = "#FF4F4F4F";     // use for title
    private static final String COLOR_BLUE = "#FF4682B4";       // use for timestamp
    private static final String COLOR_GREY61 = "#FF9C9C9C";     // use for text

    public MyCursorAdapter(Context context) {
        super(context, null, false);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        mBuilder = new SpannableStringBuilder();
        final TextView tv = (TextView) view.findViewById(R.id.tv_list_text);

        String tagStr = cursor.getString(cursor
                .getColumnIndex(FishDiary.Notes.COLUMN_NAME_TAG));

        String title = cursor.getString(cursor
                .getColumnIndex(FishDiary.Notes.COLUMN_NAME_TITLE));

        // Trim whitespace and blank lines
        String text = cursor.getString(cursor
                .getColumnIndex(FishDiary.Notes.COLUMN_NAME_NOTE)).trim();

        int encryptFlag = cursor.getInt(cursor
                .getColumnIndex(FishDiary.Notes.COLUMN_NAME_ENCRYTED));

        // better use line.separator than "\n"
        if (text.split("\n").length > 2) {
            // keep the first two lines
            text = text.split("\n")[0] + "\n" + text.split("\n")[1] + "...";
        } else if (text.length() > MAX_LENGTH) {
            text = text.substring(0, MAX_LENGTH) + "...";
        }

        // Convert millisTime to String format
        long lDate = cursor.getLong(cursor
                .getColumnIndex(FishDiary.Notes.COLUMN_NAME_MODIFICATION_DATE));
        String date = new SimpleDateFormat(TIME_STAMP_NAME).format(new Date(
                lDate));

        writeSpannableText(title, SPANNABLE_TITLE);
        writeSpannableText(date, SPANNABLE_DATE);
        String strText = (encryptFlag == 1) ?
                context.getString(R.string.hint_encrypted) : text;
        writeSpannableText(strText, SPANNABLE_TEXT);

        if (!tagStr.equals(""))
            writeTag(tagStr);

        tv.setText(mBuilder);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.list_item, parent, false);
    }

    private void writeTag(String text) {
        String[] tagSelected = text.split(Constants.TAG_SEPERATOR);
        // add whitespace at the beginning and the end
        // for a better looking tag
        for (int i = 0; i < tagSelected.length; i++) {
            tagSelected[i] = " " + tagSelected[i] + " ";
        }

        String tagTotalStr = "";
        mBuilder.append("\n"); // add a new line first
        int start = mBuilder.length();
        for (String str : tagSelected) {
            tagTotalStr += (str + "    ");

            if (tagTotalStr.length() < MAX_LENGTH - 5) {
                mBuilder.append(str + "    ");
                mBuilder.setSpan(
                        new ForegroundColorSpan(Color.parseColor(COLOR_GREY31)),
                        start, start + str.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mBuilder.setSpan(new BackgroundColorSpan(Color.LTGRAY), start,
                        start + str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mBuilder.setSpan(new RelativeSizeSpan(0.9f), start,
                        start + str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                start += (str + "    ").length();
            } else {
                mBuilder.append("...");
                break;
            }
        }
    }

    private void writeSpannableText(String text, int flag) {
        int start = mBuilder.length();
        int end = start + text.length();
        mBuilder.append(text + "\n");
        switch (flag) {
        case SPANNABLE_TITLE:
            mBuilder.setSpan(new RelativeSizeSpan(1.1f), start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mBuilder.setSpan(
                    new ForegroundColorSpan(Color.parseColor(COLOR_GREY31)),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            break;
        case SPANNABLE_DATE:
            mBuilder.setSpan(new RelativeSizeSpan(0.9f), start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mBuilder.setSpan(
                    new ForegroundColorSpan(Color.parseColor(COLOR_BLUE)),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            break;
        case SPANNABLE_TEXT:
            mBuilder.setSpan(
                    new ForegroundColorSpan(Color.parseColor(COLOR_GREY61)),
                    start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            break;
        }
    }
}
