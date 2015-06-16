package com.wanke.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.baidu.mobstat.StatService;
import com.wanke.tv.R;

public class WelcomeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        StatService.onEvent(this, "100000", "welcome", 1);
        setContentView(R.layout.activity_splash);
        //
        new Thread(new CheckVersionTask()).start();
    }

    private class CheckVersionTask implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            loadHomeUI();
        }

    }

    /**
     * login
     */
    public void loadHomeUI() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.pre_in, R.anim.pre_out);
        finish();
    }

}
