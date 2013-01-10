/**
 * LinedEditText.java
 *
 * Ver 1.0, 2012-12-2, alex_yh, Create file.
 */
package com.flounder.fishDiary.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.EditText;

/** Defines a custom EditText View that draws lines of whole page. */
public class LinedEditText extends EditText {

    private Paint mPaint;
    private boolean underLine;

    public LinedEditText(Context context) {
        super(context);
        initPaint();
    }

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public LinedEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint();
    }

    private void initPaint() {
        underLine = true;
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.LTGRAY);
    }

    /** This is called to draw the LinedEditText object */
    @Override
    protected void onDraw(Canvas canvas) {
        if (underLine) {
            int left = getLeft();
            int right = getRight();
            int paddingTop = getPaddingTop();
            int paddingLeft = getPaddingLeft();
            int paddingRight = getPaddingRight();
            int paddingBottom = getPaddingBottom();
            int height = getHeight();
            int lineHeight = getLineHeight();
            int count = (height - paddingTop - paddingBottom) / lineHeight;

            // If number of lines is larger than lines of height of EditText,
            // use number of lines instead
            count = (count < getLineCount()) ? getLineCount() : count;

            // Draws one line in the rectangle for every line of text in the EditText
            for (int i = 0; i < count; i++) {
                // Gets the baseline
                int baseline = lineHeight * (i + 1) + paddingTop;
                // Draws a line in the background from the left of the rectangle
                // to the right, at a vertical position one dip below the baseline
                canvas.drawLine(left + paddingLeft, baseline, right
                        - paddingRight, baseline, mPaint);
            }
        }
        super.onDraw(canvas);
    }

    public void setUnderLine(boolean underline) {
        this.underLine = underline;
    }
}