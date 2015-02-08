/**
 * FileUtils.java
 *
 * Ver 1.0, 2012-11-30, alex_yh, Create file.
 */
package com.flounder.fishDiary.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;
import android.os.Environment;

import com.flounder.fishDiary.data.Constants;

public class FileUtils {

    public static boolean isSDCardMounted() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    private static String getSDCardPath() {
        String ret = null;
        if (isSDCardMounted()) {
            ret = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + File.separator;
        }
        return ret;
    }

    public static boolean isRootFolderCreated() {
        File folder = new File(getSDCardPath() + Constants.FOLDER_NAME);
        if (folder.exists() && folder.isDirectory()) {
            return folder.canWrite();
        } else {
            return folder.mkdirs();
        }
    }

    public static String getRootFolder() {
        return isRootFolderCreated() ? (getSDCardPath() + Constants.FOLDER_NAME)
                : null;
    }

    public static boolean saveTextToFile(String fileName, String folderName,
            String text) {
        if (getRootFolder() == null)
            return false;

        // avoid overwritten when exporting notes with same name [fix]
        int _num = 0;
        File dstFile = new File(getRootFolder() + File.separator + folderName,
                fileName + Constants.TEXT_EXTENSTION);
        while (dstFile.exists()) {
            _num++;
            dstFile = new File(getRootFolder() + File.separator + folderName,
                    fileName + "(" + _num + ")" + Constants.TEXT_EXTENSTION);
        }

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(dstFile), "gbk"));
            bw.write(text);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            IOUtils.closeQuietly(bw);
        }
        return true;
    }

    public static String readFileToString(String fileName) {
        String line = null;
        String text = "";
        BufferedReader reader;
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
                    fileName));
            bis.mark(4);
            byte[] first3bytes = new byte[3];
            bis.read(first3bytes);
            bis.reset();

            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                    && first3bytes[2] == (byte) 0xBF) {
                reader = new BufferedReader(new InputStreamReader(bis, "utf-8"));
            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFE) {
                reader = new BufferedReader(new InputStreamReader(bis, "unicode"));
            } else if (first3bytes[0] == (byte) 0xFE
                    && first3bytes[1] == (byte) 0xFF) {
                reader = new BufferedReader(new InputStreamReader(bis, "utf-16be"));
            } else if (first3bytes[0] == (byte) 0xFF
                    && first3bytes[1] == (byte) 0xFF) {
                reader = new BufferedReader(new InputStreamReader(bis, "utf-16le"));
            } else {
                reader = new BufferedReader(new InputStreamReader(bis, "gbk"));
            }

            while ((line = reader.readLine()) != null) {
                text = text + line + "\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    /**
     * Note: This method would probably be called from UI thread
     * (I know it's a bad practice...),
     * since it's mostly used for reading provided contents (from assets),
     * so please split the text files to avoid ANR.
     */
    public static String readTextFromAssets(Context context, String fileName) {
        String line = null;
        String text = "";
        BufferedReader reader = null;
        try {
            InputStream is = context.getResources().getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            while ((line = reader.readLine()) != null) {
                text = text + line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return text;
    }

    public static boolean isFileNameValid(String name) {
        String regex = "^[a-zA-Z0-9\u4e00-\u9fa5]+[\\-\\_\\.]*[a-zA-Z0-9\u4e00-\u9fa5]*";
        return name.matches(regex);
    }

    public static boolean isPasswdValid(String passwd) {
        String regex = "[0-9]+";
        return passwd.matches(regex);
    }

    public static String getFileNameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.indexOf(Constants.TEXT_EXTENSTION));
    }
}
