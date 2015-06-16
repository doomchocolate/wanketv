package com.wanke.ui.activity.my;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.wanke.model.VersionInfo;
import com.wanke.tv.R;
import com.wanke.ui.UpdateManager;
import com.wanke.ui.activity.BaseActivity;

public class AboutActivity extends BaseActivity {
    public static final String TAG = "AboutActivity";
    private Button mUpdate;
    private TextView mVersion;
    VersionInfo mVersionInfo;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        mVersion = (TextView) findViewById(R.id.version);
        mUpdate = (Button) findViewById(R.id.about_update_button);
        mVersion.setText(getVersion());

        mUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                UpdateManager um = new UpdateManager(AboutActivity.this);
                um.checkUpdate();

            }
        });
    }

    /**
     * 获取版本号
     * 
     * @return 当前应用的版本号
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return getString(R.string.app_name) + " " + version;
        } catch (Exception e) {
            return getString(R.string.app_name);
        }
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
