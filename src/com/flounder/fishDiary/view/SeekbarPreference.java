/**
 * SeekbarPreference.java
 *
 * Ver 1.0, 2012-12-9, alex_yh, Create file.
 */
package com.flounder.fishDiary.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.flounder.fishDiary.FishPreferences;
import com.flounder.fishDiary.R;

public class SeekbarPreference extends DialogPreference implements
        OnSeekBarChangeListener {

    private int mValue;
    private int oldValue;
    private int minValue = 10;

    private TextView mTvTextSize;
    private SeekBar mSeekbar;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        if (mValue <= minValue) {
            mValue = FishPreferences.getTextSize(getContext());
        }
        oldValue = mValue;

        mTvTextSize = (TextView) view.findViewById(R.id.tv_text_size);
        mTvTextSize.setText(mValue + "");
        mSeekbar = (SeekBar) view.findViewById(R.id.sb_text_size);
        mSeekbar.setOnSeekBarChangeListener(this);
        mSeekbar.setProgress(mValue - minValue);
    }

    public SeekbarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setDialogLayoutResource(R.layout.setting_textsize_seekbar);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            SharedPreferences.Editor editor = FishPreferences
                    .getEditor(getContext());
            editor.putInt(FishPreferences.KEY_TEXT_SIZE, mValue);
            editor.commit();
        } else {
            mValue = oldValue;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
        int size = progress + minValue;
        mTvTextSize.setText(size + ""); // This is extremely lightweight, just
                                        // do it in UI thread
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mValue = seekBar.getProgress() + minValue;
    }
}
