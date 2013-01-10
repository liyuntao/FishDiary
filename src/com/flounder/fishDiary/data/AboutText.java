/**
 * AboutText.java
 *
 * Ver 1.0, 2013-1-8, alex_yh, Create file.
 */
package com.flounder.fishDiary.data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.flounder.fishDiary.FishDiaryActivity;
import com.flounder.fishDiary.R;

public class AboutText extends TextContent {

    public AboutText(Context context, Button btnForward, Button btnBack,
            EditText text, EditText title) {
        super(context, btnForward, btnBack, text, title);
    }

    @Override
    protected CharSequence setText(final Context context) {
        int start_1 = Constants.ABOUT_TEXT.indexOf(Constants.ABOUT_TEXT_2);
        int end_1 = start_1 + Constants.ABOUT_TEXT_2.length();

        int start_2 = Constants.ABOUT_TEXT.indexOf(Constants.ABOUT_TEXT_4);
        int end_2 = start_2 + Constants.ABOUT_TEXT_4.length();

        int start_3 = Constants.ABOUT_TEXT.indexOf(Constants.ABOUT_TEXT_6);
        int end_3 = start_3 + Constants.ABOUT_TEXT_6.length();

        SpannableString text = new SpannableString(Constants.ABOUT_TEXT);
        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Constants.ABOUT_TEXT_2));
                try {
                    context.startActivity(Intent.createChooser(intent,
                            context.getString(R.string.intent_chooser_view)));
                } catch (Exception e) {
                    // ignore
                }
            }
        }, start_1, end_1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL,
                        new String[] { Constants.ABOUT_TEXT_4 });
                intent.putExtra(Intent.EXTRA_SUBJECT, Constants.MAIL_SUBJECT);
                intent.putExtra(Intent.EXTRA_TEXT, Constants.MAIL_CONTENT);
                try {
                    context.startActivity(Intent.createChooser(intent,
                            context.getString(R.string.intent_chooser_mail)));
                } catch (Exception e) {
                    // ignore
                }
            }
        }, start_2, end_2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(context, FishDiaryActivity.class);
                intent.setAction(Constants.ACTION_CONTENT);
                try {
                    intent.putExtra(Constants.ACTION_CONTENT,
                            Constants.CONTENT_READING);
                    context.startActivity(intent);
                } catch (Exception e) {
                    // ignore
                }
            }
        }, start_3, end_3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return text;
    }

    @Override
    public String getTitle() {
        return Constants.ABOUT_TITLE;
    }

    @Override
    protected boolean isMultiPages() {
        return false;
    }
}
