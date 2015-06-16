package com.wanke.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.wanke.model.VersionInfo;
import com.wanke.tv.R;

/**
 * APK更新管理类
 * 
 * @author
 * 
 */
public class UpdateManager {
    public static final String TAG = "UpdateManager";
    private Context mContext;
    //更新版本信息对象  
    private VersionInfo mVersionInfo = null;
    private ProgressBar progressBar;
    private int progress = 0;
    // 是否终止下载  
    private boolean isInterceptDownload = false;

    /**
     * 参数为Context(上下文activity)的构造函数
     * 
     * @param context
     */
    public UpdateManager(Context context) {
        this.mContext = context;
    }

    public void checkUpdate() {
        // 从服务端获取版本信息  
        mVersionInfo = new VersionInfo();
        HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.GET,
                "http://54.64.105.44/wanketv/live/version?type=android",
                new RequestCallBack<String>() {
                    @Override
                    public void onLoading(
                            long total, long current, boolean isUploading) {
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        try {
                            String result = responseInfo.result;
                            JSONObject jsonObject = new JSONObject(result);
                            Log.i(TAG, "result" + result);
                            String downUrl = jsonObject.getString("downUrl");
                            String updateLog = jsonObject.getString("updateShortLog");
                            int versionCode = jsonObject.getInt("version");
                            mVersionInfo.setDownloadURL(downUrl);
                            mVersionInfo.setVersionCode(versionCode);
                            mVersionInfo.setDisplayMessage(updateLog);
                            try {
                                PackageInfo pi = mContext.getPackageManager()
                                        .getPackageInfo(mContext.getPackageName(),
                                                PackageManager.GET_CONFIGURATIONS);
                                int presentversionCode = pi.versionCode;
                                if (presentversionCode < mVersionInfo.getVersionCode()) {
                                    // 如果当前版本号小于服务端版本号,则弹出提示更新对话框  
                                    showUpdateDialog();
                                } else {
                                    ToastUtil.showToast(mContext,
                                            R.string.update_versions);
                                }
                            } catch (NameNotFoundException e) {
                                e.printStackTrace();
                            }

                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onFailure(HttpException error, String msg) {
                    }
                });

    }

    /**
     * 提示更新对话框
     * 
     * @param info
     *            版本信息对象
     */
    private void showUpdateDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle("版本更新");
        builder.setMessage(mVersionInfo.getDisplayMessage());
        builder.setPositiveButton("下载", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 弹出下载框  
                showDownloadDialog();
            }
        });
        builder.setNegativeButton("以后再说", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 弹出下载框
     */
    private void showDownloadDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle(mVersionInfo.getDisplayMessage());
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.version_update_progress, null);
        progressBar = (ProgressBar) view.findViewById(R.id.pb_update_progress);
        builder.setView(view);
        builder.setNegativeButton("取消", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //终止下载  
                isInterceptDownload = true;
            }
        });
        builder.create().show();
        //下载apk  
        downloadApk();
    }

    /**
     * 下载apk
     */
    private void downloadApk() {
        //开启另一线程下载  
        Thread downLoadThread = new Thread(downApkRunnable);
        downLoadThread.start();
    }

    /**
     * 从服务器下载新版apk的线程
     */
    private Runnable downApkRunnable = new Runnable() {
        @Override
        public void run() {
            if (!android.os.Environment.getExternalStorageState()
                    .equals(android.os.Environment.MEDIA_MOUNTED)) {
                //如果没有SD卡  
                Builder builder = new Builder(mContext);
                builder.setTitle("提示");
                builder.setMessage("当前设备无SD卡，数据无法下载");
                builder.setPositiveButton("确定", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return;
            } else {
                try {
                    //服务器上新版apk地址  
                    URL url = new URL(mVersionInfo.getDownloadURL());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    int length = conn.getContentLength();
                    InputStream is = conn.getInputStream();
                    File file = new File(Environment.getExternalStorageDirectory()
                            .getAbsolutePath()
                            + "/updateApkFile/");
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    //下载服务器中新版本软件（写文件）  
                    String apkFile = Environment.getExternalStorageDirectory()
                            .getAbsolutePath()
                            + "/updateApkFile/"
                            + mVersionInfo.getApkName();
                    File ApkFile = new File(apkFile);
                    FileOutputStream fos = new FileOutputStream(ApkFile);
                    int count = 0;
                    byte buf[] = new byte[1024];
                    do {
                        int numRead = is.read(buf);
                        count += numRead;
                        //更新进度条  
                        progress = (int) (((float) count / length) * 100);
                        handler.sendEmptyMessage(1);
                        if (numRead <= 0) {
                            //下载完成通知安装  
                            handler.sendEmptyMessage(0);
                            break;
                        }
                        fos.write(buf, 0, numRead);
                        //当点击取消时，则停止下载  
                    } while (!isInterceptDownload);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    /**
     * 声明一个handler来跟进进度条
     */
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case 1:
                // 更新进度情况  
                progressBar.setProgress(progress);
                break;
            case 0:
                progressBar.setVisibility(View.INVISIBLE);
                // 安装apk文件  
                installApk();
                break;
            default:
                break;
            }
        };
    };

    /**
     * 安装apk
     */
    private void installApk() {
        // 获取当前sdcard存储路径  
        File apkfile = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/updateApkFile/"
                + mVersionInfo.getApkName());
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        // 安装，如果签名不一致，可能出现程序未安装提示  
        i.setDataAndType(Uri.fromFile(new File(apkfile.getAbsolutePath())),
                "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }
}
