package com.flounder.fishDiary.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.flounder.fishDiary.R;
import com.flounder.fishDiary.view.BaseStyleDialog.ICallBack;

/** These messy code tastes like Martin's Vodka... */
public class PasswordDialog {
    private EditText etPasswd1;
    private Button btnPositive;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
        }
        @Override
        public void afterTextChanged(Editable s) {
            if (!TextUtils.isEmpty(etPasswd1.getText())) {
                btnPositive.setEnabled(true);
            } else {
                // Disable button if use type in sth. and backspace [fix]
                btnPositive.setEnabled(false);
            }
        }
    };

    /**
     * Display a password dialog, with predefined layout and message
     */
    public PasswordDialog(Context context, final IPasswdCallBack callback) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_password, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setTitle(R.string.tv_input_passwd);
        builder.setNegativeButton(R.string.button_cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.negButtonClicked();
            }
        });
        builder.setPositiveButton(R.string.button_ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                callback.getPassword(etPasswd1.getText().toString());
                callback.posButtonClicked();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);    // not cancelable [fix]
        dialog.show();
        btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnPositive.setEnabled(false);

        etPasswd1 = (EditText) view.findViewById(R.id.et_passwd);
        etPasswd1.addTextChangedListener(textWatcher);
    }

    public interface IPasswdCallBack extends ICallBack {
        void getPassword(String password);
    }

    public static void prompPasswdErrDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                new ContextThemeWrapper(context, android.R.style.Theme_NoTitleBar));
        builder.setMessage(R.string.text_passwd_incorrect);
        builder.setNeutralButton(R.string.button_ok, null);
        builder.show();
    }
}
