package com.flounder.fishDiary.view;

import java.io.FileNotFoundException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.flounder.fishDiary.FishPreferences;
import com.flounder.fishDiary.R;
import com.flounder.fishDiary.image.ImageUtil;

public class ImageViewPreference extends Preference {

    private ImageView mImage;
    private Bitmap mPhoto;

    public ImageViewPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setWidgetLayoutResource(R.layout.pref_account_image);
        mPhoto = ImageUtil.getHeadPhoto(getContext());
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mImage = (ImageView) view.findViewById(R.id.pref_imageView);
        mImage.setImageBitmap(mPhoto);
    }

    public void setImageUri(Uri uri) {
        try {
            mPhoto = BitmapFactory.decodeStream(getContext().getContentResolver()
                    .openInputStream(uri));
            SharedPreferences.Editor editor = FishPreferences
                    .getEditor(getContext());
            editor.putString(FishPreferences.KEY_HEAD_PHOTO, uri.toString());
            editor.commit();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
