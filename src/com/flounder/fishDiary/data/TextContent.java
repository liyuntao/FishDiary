/**
 * TextContent.java
 *
 * Ver 1.0, 2013-1-8, alex_yh, Create file.
 */
package com.flounder.fishDiary.data;

import com.flounder.fishDiary.R;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Template Class for pre-exist contents.
 * 
 * This is my learning practice for Java DP...
 */
public abstract class TextContent {

    public TextContent(Context context, Button btnForward,
            Button btnBack, EditText text, EditText title) {
        // update UI state
        text.setFocusableInTouchMode(false);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        title.setFocusable(false);

        if (isMultiPages()) {
            setForwardButton(context, btnForward);
            setBackButton(context, btnBack);
            btnForward.setOnClickListener(forward(context, text, title));
            btnBack.setOnClickListener(back(context, text, title));
        } else {
            btnForward.setEnabled(false); // disable "Save" Button
        }
    }

    public final CharSequence getContent(Context context) {
        return setText(context);
    }

    /** Get title, set to Title field */
    public abstract String getTitle();

    /**
     * Set rich text content.
     * 
     * @return CharSequence
     */
    protected abstract CharSequence setText(final Context context);

    /**
     * Handle "forward" Button event
     * 
     * @param text
     *            Text content field
     * @param title
     *            Title field
     */
    protected OnClickListener forward(final Context context, final EditText text,
            final EditText title) {
        return null;
    };

    /**
     * Handle "back" Button event
     * 
     * @param text
     *            Text content field
     * @param title
     *            Title field
     */
    protected OnClickListener back(final Context context, final EditText text,
            final EditText title) {
        return null;
    };

    /** Set Button text "Forward" */
    private void setForwardButton(Context context, Button button) {
        button.setText(context.getString(R.string.string_forward));
    }

    /** Set Button text "Back" */
    private void setBackButton(Context context, Button button) {
        button.setText(context.getString(R.string.string_back));
    }

    /** If text content allows forward/back */
    protected abstract boolean isMultiPages();
}
