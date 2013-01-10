package com.flounder.fishDiary.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;

import com.flounder.fishDiary.FishPreferences;
import com.flounder.fishDiary.R;
import com.flounder.fishDiary.util.FileUtils;

public class ImageUtil {

    private static final int ROUND_CORNER_RADIUS = 4;

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap setImage(Uri fileUri, int width, int height) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileUri.getPath(), options);
        int photoW = options.outWidth;
        int photoH = options.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / width, photoH / height);

        // Decode the image file into a Bitmap sized to fill the view
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        options.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
        return bitmap;
    }

    /**
     * Constructs an intent for picking a photo from Gallery, cropping it and returning
     * the bitmap.
     */
    public static Intent getPhotoPickIntent(int iconSize, String fileName) {
        Uri uri = getImageUri(fileName);
        if (uri != null) {  // Fix NPE problems.
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("outputX", iconSize);
            intent.putExtra("outputY", iconSize);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            intent.putExtra("scale", true);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("return-data", false);
            return intent;
        }
        return null;
    }

    /** Launches Gallery to pick a photo. */
    public static void doPickPhotoFromGallery(Activity activity, int requestCode,
            int iconSize, String fileName) {
        try {
            // Launch picker to choose photo for selected contact
            final Intent intent = getPhotoPickIntent(iconSize, fileName);
            if (intent != null) // Fix NPE problems.
                activity.startActivityForResult(
                        Intent.createChooser(intent,
                                activity.getString(R.string.intent_chooser_image)),
                        requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Uri getImageUri(String fileName) {
        File f = getImageFile(fileName);
        if (f != null) {    // Fix NPE problems.
            return Uri.fromFile(f);
        }
        return null;
    }

    private static File getImageFile(String fileName) {
        File f = null;
        if (FileUtils.isRootFolderCreated()) {
            f = new File(FileUtils.getRootFolder(), fileName);
            try {
                if (f.createNewFile()) {
                    return f;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return f;
    }

    /** Get user head photo or default icon */
    public static Bitmap getHeadPhoto(Context context) {
        Bitmap bitmap = null;
        String uriString = FishPreferences.getHeadPhote(context);
        if (uriString != null) {
            try {
                bitmap = BitmapFactory.decodeStream(context.getContentResolver()
                        .openInputStream(Uri.parse(uriString)));
            } catch (FileNotFoundException e) {
                // In case user delete the rootFolder while application is still in back
                // stage, use default head photo [fix]
                bitmap = BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.icon);
            }
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.icon);
        }
        return ImageUtil.getRoundedCornerBitmap(bitmap, ROUND_CORNER_RADIUS);
    }

    public static Bitmap getBackgroundImage(Context context) {
        Bitmap bitmap = null;
        String uriString = FishPreferences.getBackgroundImage(context);
        int flag = FishPreferences.getBgImageEffect(context);
        if (uriString != null) {
            try {
                bitmap = BitmapFactory.decodeStream(context.getContentResolver()
                        .openInputStream(Uri.parse(uriString)));
                switch (flag) {
                case FishPreferences.EFFECT_NONE:
                    return bitmap;
                case FishPreferences.EFFECT_FEATHER:
                    return new ImageFilter(bitmap).processImage().getDstBitmap();
                case FishPreferences.EFFECT_REFLECT:
                    return ImageShadow.reflectImage(bitmap);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
