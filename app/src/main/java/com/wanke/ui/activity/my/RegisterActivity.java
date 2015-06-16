package com.wanke.ui.activity.my;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.baidu.mobstat.StatService;
import com.wanke.tv.R;
import com.wanke.ui.ToastUtil;
import com.wanke.ui.activity.BaseActivity;
import com.wanke.util.AccountUtil;
import com.wanke.util.AccountUtil.RegisterCallback;
import com.wanke.util.RegexUtil;

public class RegisterActivity extends BaseActivity {
    public final static int REGISTER_SUCC = 0x11;

    private EditText mPassword, mAccount, mEmil, mPasswordConfirm;
    private Button mRegister;
    private ImageView mDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatService.onEvent(this, "101001", "register.activity", 1);
        setContentView(R.layout.activity_register);
        initView();
    }

    protected void initView() {
        mAccount = (EditText) findViewById(R.id.register_account);
        mPassword = (EditText) findViewById(R.id.login_password);
        mEmil = (EditText) findViewById(R.id.register_email);
        mPasswordConfirm = (EditText) findViewById(R.id.verify_password);
        mRegister = (Button) findViewById(R.id.register_btn);
        mDelete = (ImageView) findViewById(R.id.delete_username_input_btn);
        mDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mAccount.setText("");
            }
        });

        mRegister.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final String account = mAccount.getText().toString().trim();
                final String password = mPassword.getText().toString().trim();
                String email = mEmil.getText().toString().trim();
                String passwordConfirm = mPasswordConfirm.getText().toString()
                        .trim();

                if (TextUtils.isEmpty(account)) {
                    ToastUtil.showToast(RegisterActivity.this,
                            R.string.login_username_is_empty);
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    ToastUtil.showToast(RegisterActivity.this,
                            R.string.register_email_is_empty);
                    return;
                }

                if (!RegexUtil.isValidEmail(email)) {
                    ToastUtil.showToast(RegisterActivity.this,
                            R.string.register_email_is_error);
                    return;
                }

                if (TextUtils.isEmpty(password)
                        || TextUtils.isEmpty(passwordConfirm)) {
                    ToastUtil.showToast(RegisterActivity.this,
                            R.string.login_password_is_empty);
                    return;
                }

                if (password.equals(passwordConfirm)) {
                    showWaitingDialog();

                    AccountUtil.register(account,
                            password,
                            email,
                            new RegisterCallback() {

                                @Override
                                public void onRegisterSuccess() {
                                    dismissWaitingDialog();
                                    StatService.onEvent(getApplicationContext(),
                                            "101002",
                                            "register.activity",
                                            1);
                                    setResult(REGISTER_SUCC);
                                    finish();
                                }

                                @Override
                                public void onRegisterFailed(
                                        int error, String msg) {
                                    StatService.onEvent(getApplicationContext(),
                                            "101003",
                                            "register.activity",
                                            1);
                                    showToast(msg);
                                    dismissWaitingDialog();

                                }

                                @Override
                                public void onRegisterException(String msg) {
                                    StatService.onEvent(getApplicationContext(),
                                            "101003",
                                            "register.activity",
                                            1);
                                    showToast(msg);
                                    dismissWaitingDialog();
                                }
                            });

                } else {
                    ToastUtil.showToast(RegisterActivity.this,
                            R.string.confirm_password_is_error);
                    return;
                }
            }
        });
    }

    @Override
    public void showNext() {
        // TODO Auto-generated method stub
        finish();
        overridePendingTransition(R.anim.pre_in, R.anim.pre_out);
    }

    @Override
    public void showPre() {
        // TODO Auto-generated method stub
        finish();
        overridePendingTransition(R.anim.pre_in, R.anim.pre_out);
    }

}
