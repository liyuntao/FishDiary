package com.flounder.fishDiary.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.view.ContextThemeWrapper;

import com.flounder.fishDiary.R;

/** This funny looking class is created because Dialog of Holo style is horribly ugly */
public class BaseStyleDialog {

    /**
     * InfoDialog in this context means Dialog with Pos/Neg button
     * 
     * @param msg
     *            message passed in
     * @param callback
     *            defining operation when button clicked
     * @param cancelable
     *            whether the dialog is cancelable
     */
    public static void buildInfoDialog(Context context, String msg,
            final ICallBack callback, boolean cancelable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(context, android.R.style.Theme_NoTitleBar));
        builder.setMessage(msg);
        builder.setNegativeButton(R.string.button_cancel,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.negButtonClicked();
                    }
                });
        builder.setPositiveButton(R.string.button_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.posButtonClicked();
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                callback.negButtonClicked();
            }
        });
        builder.setCancelable(cancelable);
        builder.show();
    }

    public static void buildInfoDialog(Context context, int resId,
            final ICallBack callback, boolean cancelable) {
        String msg = context.getResources().getString(resId);
        buildInfoDialog(context, msg, callback, cancelable);
    }

    /**
     * InfoDialog in this context means Dialog with Pos/Neg button
     * 
     * @param resId
     *            message passed in
     * @param callback
     *            defining operation when button clicked
     */
    public static void buildInfoDialog(Context context, int resId,
            final ICallBack callback) {
        String msg = context.getResources().getString(resId);
        buildInfoDialog(context, msg, callback, true);
    }

    public static void buildInfoDialog(Context context, String msg,
            final ICallBack callback) {
        buildInfoDialog(context, msg, callback, true);
    }

    /**
     * MsgDialog in this context means Dialog an "OK" button,
     * just for conveying message, and no callback method defined
     * 
     * @param resId
     *            message passed in
     */
    public static void buildMsgDialog(Context context, int resId) {
        String msg = context.getResources().getString(resId);
        buildMsgDialog(context, msg);
    }

    /**
     * MsgDialog in this context means Dialog an "OK" button,
     * just for conveying message, and no callback method defined
     * 
     * @param msg
     *            message passed in
     */
    public static void buildMsgDialog(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(
                        context, android.R.style.Theme_NoTitleBar));
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.button_ok, null);
        builder.show();
    }

    public interface ICallBack {
        void posButtonClicked();
        void negButtonClicked();
    }
}