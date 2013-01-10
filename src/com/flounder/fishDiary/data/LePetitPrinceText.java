/**
 * LePetitPrinceText.java
 *
 * Ver 1.0, 2013-1-8, alex_yh, Create file.
 */
package com.flounder.fishDiary.data;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.flounder.fishDiary.util.FileUtils;

public class LePetitPrinceText extends TextContent {

    private int mChapter = 0;

    public LePetitPrinceText(Context context, Button btnForward, Button btnBack,
            EditText text, EditText title) {
        super(context, btnForward, btnBack, text, title);
    }

    @Override
    protected CharSequence setText(Context context) {
        return FileUtils.readTextFromAssets(context,
                getTitle() + Constants.TEXT_EXTENSTION);
    }

    @Override
    public String getTitle() {
        return Constants.FILENAME_PRINCE_PREFIX + mChapter;
    }

    @Override
    protected OnClickListener forward(final Context context, final EditText text,
            final EditText title) {
        View.OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChapter >= 5)
                    return;

                mChapter++;
                title.setText(getTitle());
                text.setText(setText(context));
            }
        };

        return listener;
    }
    @Override
    protected OnClickListener back(final Context context, final EditText text,
            final EditText title) {
        View.OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mChapter <= 0)
                    return;

                mChapter--;
                title.setText(getTitle());
                text.setText(setText(context));
            }
        };
        return listener;
    }

    @Override
    protected boolean isMultiPages() {
        return true;
    }
}
