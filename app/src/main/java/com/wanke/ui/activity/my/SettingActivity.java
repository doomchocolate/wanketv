package com.wanke.ui.activity.my;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.baidu.mobstat.StatService;
import com.wanke.tv.R;
import com.wanke.ui.activity.BaseActivity;
import com.wanke.ui.activity.FeedbackActivity;
import com.wanke.ui.activity.VideoActivity;
import com.wanke.ui.widget.SwitchSeekBar;
import com.wanke.ui.widget.SwitchSeekBar.OnCheckedChangeListener;
import com.wanke.util.PreferenceUtil;

public class SettingActivity extends BaseActivity {

    private View mChangePassword;

    private SwitchSeekBar mHardwareDecode;
    private SwitchSeekBar mAutoPlayIn23G;
    private SeekBar mDanmakuAlphaSetting;
    private View mFeedbackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mChangePassword = findViewById(R.id.setting_change_password);
        mChangePassword.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(SettingActivity.this,
                        ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        mHardwareDecode = (SwitchSeekBar) findViewById(R.id.hardware_decoder);
        mHardwareDecode.setChecked(PreferenceUtil.getDecodeByHardware());
        mHardwareDecode.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(boolean isChecked) {
                PreferenceUtil.setDecodeByHardware(isChecked);
            }
        });

        mAutoPlayIn23G = (SwitchSeekBar) findViewById(R.id.auto_play_setting);
        mAutoPlayIn23G.setChecked(PreferenceUtil.getAutoPlayIn23G());
        mAutoPlayIn23G.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(boolean isChecked) {
                PreferenceUtil.setAutoPlayIn23G(isChecked);
            }
        });

        mDanmakuAlphaSetting = (SeekBar) findViewById(R.id.danmaku_alpha_setting);
        mDanmakuAlphaSetting.setMax(255);
        mDanmakuAlphaSetting.setProgress((int) ((PreferenceUtil.getDanmakuAlpha() - 0x30)
                * 70.f / (0xFF - 0x30)));
        mDanmakuAlphaSetting.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = seekBar.getProgress();
                PreferenceUtil.setDanmakuAlpha((int) (value
                        * ((255.0f - 0x30) / 70.0f) + 0x30));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(
                    SeekBar seekBar, int progress, boolean fromUser) {

            }
        });

        mFeedbackBtn = findViewById(R.id.feedback_btn);
        mFeedbackBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                StatService.onEvent(getApplicationContext(),
                        "109011",
                        "setting.activity",
                        1);
                Intent intent = new Intent();
                intent.setClass(SettingActivity.this, FeedbackActivity.class);
                startActivity(intent);
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
