package com.wanke.ui.activity.my;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.baidu.mobstat.StatService;
import com.wanke.model.AccountInfo;
import com.wanke.tv.R;
import com.wanke.ui.ToastUtil;
import com.wanke.ui.activity.BaseActivity;

public class ChangePasswordActivity extends BaseActivity {

    private EditText mPassword, mChangepassword, mChangepasswordverify;
    AccountInfo info = new AccountInfo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        StatService.onEvent(this, "103001", "changepassword.activity", 1);
        setTitle(R.string.changepassword_activity_title);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepassword);
        initView();
    }

    public void initView() {
        mPassword = (EditText) findViewById(R.id.change_password_change);
        mChangepassword = (EditText) findViewById(R.id.change_password);
        mChangepasswordverify = (EditText) findViewById(R.id.change_password_verify);
        mPassword.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                String password = mPassword.getText().toString().trim();
                String changepassword = mChangepassword.getText()
                        .toString()
                        .trim();
                String changepasswordverify = mChangepasswordverify.getText()
                        .toString()
                        .trim();
                if (TextUtils.isEmpty(password)) {
                    ToastUtil.showToast(ChangePasswordActivity.this,
                            R.string.change_password_is_empty);
                    return;
                }

                if (TextUtils.isEmpty(password)
                        || TextUtils.isEmpty(changepasswordverify)) {
                    ToastUtil.showToast(ChangePasswordActivity.this,
                            R.string.change_password_is_empty);
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
