package com.wanke.ui.activity;

import java.text.SimpleDateFormat;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.exception.WeiboShareException;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.wanke.db.dao.HistoryDao;
import com.wanke.network.http.CommonHttpUtils;
import com.wanke.network.http.Constants;
import com.wanke.network.http.HttpExceptionButFoundCache;
import com.wanke.tv.R;
import com.wanke.ui.ToastUtil;
import com.wanke.ui.UiUtils;
import com.wanke.util.AccessTokenKeeper;
import com.wanke.util.PreferenceUtil;

public class LiveChannelDetailActivity extends BaseActivity {
    public final static String CHANNEL_ID = "channelId";
    public final static String CHANNEL_NAME = "channelName";
    public final static String CHANNEL_OWNER_NICKNAME = "channelOwnerNickname";
    public final static String CHANNEL_ONLINE = "channelOnline";
    public final static String CHANNEL_GAME_NAME = "gameName";

    private int mChannelId = 0;
    private String mChannelName = "";
    private String mChannelOwnerNickname = "";
    private int mChannelOwnerUid = 0;
    private int mChannelOnline = 0;
    private int mChannelFans = 0;
    private String mChannelDetail = "";
    private boolean mChannelSubscribed = false;
    private String mChannelCover = "";
    private String mChannelGameName;
    boolean logined = true;
    private HistoryDao mDao;
    boolean mState = true;
    private DisplayImageOptions mOptions = UiUtils.getOptionsFadeIn(100);
    private DisplayImageOptions mAvatarOptions = UiUtils.getOptionsRound((int) (4 * UiUtils.getDensity(null)));

    private DisplayImageOptions mCircleOption = UiUtils.getOptionCircle();

    ImageView mSubscribeBtn;
    RelativeLayout mSubscribeRlt;
    private View mShareBtn;

    private View layout;
    PopupWindow mPopupWindow;
    //weibo
    private static final String PREFERENCES_NAME = "com_weibo_sdk_android";
    SharedPreferences sp;
    private static final String KEY_UID = "uid";
    private WeiboAuth mWeiboAuth;
    private IWeiboShareAPI mWeiboShareAPI;
    SsoHandler mSsoHandler;
    private Oauth2AccessToken mAccessToken;
    //weixin
    private IWXAPI wxApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatService.onEvent(this, "107001", "livechanneldetail.activity", 1);
        mWeiboAuth = new WeiboAuth(this,
                Constants.APP_KEY,
                Constants.REDIRECT_URL,
                Constants.SCOPE);

        wxApi = WXAPIFactory.createWXAPI(this, Constants.WX_APP_ID);
        wxApi.registerApp(Constants.WX_APP_ID);
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mDao = new HistoryDao(this);
        setContentView(R.layout.activity_video_detail);
        sp = LiveChannelDetailActivity.this.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_APPEND);
        Intent intent = getIntent();
        mChannelName = intent.getStringExtra(CHANNEL_NAME);
        mChannelOwnerNickname = intent.getStringExtra(CHANNEL_OWNER_NICKNAME);
        mChannelOnline = intent.getIntExtra(CHANNEL_ONLINE, 0);
        mChannelId = intent.getIntExtra(CHANNEL_ID, 0);
        mChannelGameName = intent.getStringExtra(CHANNEL_GAME_NAME);

        setTitle(mChannelName);

        View view = findViewById(R.id.video_play_btn);
        view.setOnClickListener(new OnClickListener() {
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
            String mHistoryTime = sdf.format(new java.util.Date());

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                mDao.add(mChannelId,
                        mChannelName,
                        mChannelGameName,
                        mChannelOwnerNickname,
                        mHistoryTime,
                        mChannelFans);
                intent.setClass(LiveChannelDetailActivity.this,
                        VideoActivity.class);
                intent.putExtra(VideoActivity.KEY_ROOM_TITLE, mChannelName);
                startActivity(intent);
            }
        });

        mSubscribeBtn = (ImageView) findViewById(R.id.subscribe_btn);
        mSubscribeRlt = (RelativeLayout) findViewById(R.id.subscribe_rlt);
        mSubscribeRlt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (logined) {
                    mSubscribeBtn.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (logined) {
                                if (mChannelSubscribed) {
                                    unsubscribe();
                                    StatService.onEvent(getApplicationContext(),
                                            "107003",
                                            "glivechanneldetail.activity",
                                            1);
                                } else {
                                    subscribe();
                                    StatService.onEvent(getApplicationContext(),
                                            "107002",
                                            "livechanneldetail.activity",
                                            1);
                                }
                            } else {
                                ToastUtil.showToastInCenter(LiveChannelDetailActivity.this,
                                        R.string.subscribe_need_login);
                            }
                        }
                    });
                } else {
                    ToastUtil.showToast(getApplicationContext(),
                            R.string.need_login_hint);
                }
            }
        });

        //initView(); 
        getChannelInfo();
        initAccountView();

    }

    private void initAccountView() {
        String account = PreferenceUtil.getUsername();
        if (!TextUtils.isEmpty(account)) {
            logined = true;
        } else {
            logined = false;
        }
    }

    private void initView() {
        Log.d(TAG, "Init View!");
        TextView ownerNickname = (TextView) findViewById(R.id.owner_nickname);
        ownerNickname.setText(mChannelOwnerNickname);

        TextView online = (TextView) findViewById(R.id.online);
        online.setText("在线：" + mChannelOnline);

        TextView channelTitle = (TextView) findViewById(R.id.room_title);
        channelTitle.setText(mChannelName);
        setTitle(mChannelName);

        TextView channelDetail = (TextView) findViewById(R.id.room_detail);
        channelDetail.setText(mChannelDetail);

        ImageView channelCover = (ImageView) findViewById(R.id.room_cover);
        if (!TextUtils.isEmpty(mChannelCover)) {
            ImageLoader.getInstance()
                    .displayImage(Constants.buildImageUrl(mChannelCover),
                            channelCover,
                            mOptions);
        }

        ImageView channelOwnerAvatar = (ImageView) findViewById(R.id.owner_avatar);
        if (mChannelOwnerUid > 0) {
            ImageLoader.getInstance()
                    .displayImage(Constants.buildImageUrl("" + mChannelOwnerUid
                            + ".png"),
                            channelOwnerAvatar,
                            mCircleOption);
        }

        mSubscribeBtn = (ImageView) findViewById(R.id.subscribe_btn);
        if (mChannelSubscribed) {
            mSubscribeBtn.setImageResource(R.drawable.detail_activity_subscribed_btn);
        } else {
            mSubscribeBtn.setImageResource(R.drawable.detail_activity_unsubscribed_btn);
        }
        mShareBtn = findViewById(R.id.share_btn);
        mShareBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //                StatService.onEvent(getApplicationContext(),
                //                        "107004",
                //                        "livechanneldetail.activity",
                //                        1);
                //                Intent intent;
                //                intent = new Intent(Intent.ACTION_SEND);
                //                intent.setType("image/*");
                //                intent.putExtra(Intent.EXTRA_SUBJECT, "好友分享");
                //                // 自动添加的发送的具体信息
                //                intent.putExtra(Intent.EXTRA_TEXT,
                //                        "我在黑马TV观看主播，欢迎大家来围观。来自/#黑马#游戏直播/！");
                //                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //                startActivity(Intent.createChooser(intent, "黑马分享："));
                if (mState) {
                    mState = false;
                    LayoutInflater inflater = LayoutInflater.from(v.getContext());
                    layout = inflater.inflate(R.layout.share, null);
                    mPopupWindow = new PopupWindow(layout,
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    mPopupWindow.setContentView(layout);
                    mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                    mPopupWindow.setOutsideTouchable(true);
                    ImageView weixin = (ImageView) layout.findViewById(R.id.share_weixin);
                    ImageView weibo = (ImageView) layout.findViewById(R.id.share_weibo);
                    ImageView pengyouquan = (ImageView) layout.findViewById(R.id.share_pengyouquan);
                    //pengyouquan
                    pengyouquan.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub

                            if (wxApi.isWXAppInstalled()) {
                                wechatShare(1);
                            } else {
                                Toast.makeText(LiveChannelDetailActivity.this,
                                        R.string.share_noweixin,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    //weixin
                    weixin.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            if (wxApi.isWXAppInstalled()) {
                                wechatShare(0);
                            } else {
                                Toast.makeText(LiveChannelDetailActivity.this,
                                        R.string.share_noweixin,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    //weibo
                    weibo.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            // TODO Auto-generated method stub
                            try {
                                if (mWeiboShareAPI.registerApp()) {
                                    String uid = sp.getString(KEY_UID, "");
                                    if (uid == null) {
                                        mSsoHandler = new SsoHandler(LiveChannelDetailActivity.this,
                                                mWeiboAuth);
                                        mSsoHandler.authorize(new AuthListener());
                                    } else {
                                        startActivity(new Intent(LiveChannelDetailActivity.this,
                                                WBShareActivity.class));

                                    }
                                }

                            } catch (WeiboShareException e) {
                                e.printStackTrace();
                                Toast.makeText(LiveChannelDetailActivity.this,
                                        e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                } else {
                    mState = true;
                    mPopupWindow.dismiss();
                }

            }
        });
    }

    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用
     * {@link SsoHandler#authorizeCallBack} 后，
     * 该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {

        @Override
        public void onComplete(Bundle values) {
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                AccessTokenKeeper.writeAccessToken(LiveChannelDetailActivity.this,
                        mAccessToken);
                Toast.makeText(LiveChannelDetailActivity.this,
                        R.string.weibosdk_demo_toast_auth_success,
                        Toast.LENGTH_SHORT).show();
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = getString(R.string.about_update);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(LiveChannelDetailActivity.this,
                        message,
                        Toast.LENGTH_LONG)
                        .show();
            }

        }

        @Override
        public void onCancel() {
            Toast.makeText(LiveChannelDetailActivity.this,
                    R.string.weibosdk_demo_toast_auth_canceled,
                    Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(LiveChannelDetailActivity.this,
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    /**
     * 微信分享 （这里仅提供一个分享网页的示例，其它请参看官网示例代码）
     * 
     * @param flag
     *            (0:分享到微信好友，1：分享到微信朋友圈)
     */
    private void wechatShare(int flag) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = "www.baidu.com";
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "这里填写标题";
        msg.description = "这里填写内容";

        Bitmap thumb = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_launcher);
        msg.setThumbImage(thumb);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession
                : SendMessageToWX.Req.WXSceneTimeline;
        wxApi.sendReq(req);
    }

    private void getChannelInfo() {
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("uid", PreferenceUtil.getUid());
        params.addQueryStringParameter("roomId", "" + mChannelId);

        CommonHttpUtils.get("channel", params, new RequestCallBack<String>() {

            private void parseResult(String content) {
                try {
                    JSONObject object = new JSONObject(content);

                    mChannelOwnerNickname = object.optString("ownerNickname");
                    mChannelCover = object.optString("roomCover");
                    mChannelDetail = Html.fromHtml(object.optString("detail",
                            "")).toString();
                    mChannelName = object.optString("roomName");
                    mChannelOnline = object.optInt("online", -1);
                    mChannelId = object.optInt("roomId", -1);
                    mChannelFans = object.optInt("fans", -1);
                    mChannelOwnerUid = object.optInt("ownerUid", -1);
                    mChannelSubscribed = object.optBoolean("subscribed");
                    mChannelGameName = object.optString("gameName");

                    Log.i(TAG, "object+1" + object);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            initView();
                        }
                    });
                } catch (Exception e) {
                    Log.d(TAG,
                            "Parse Channel Info Exception:" + e.toString());
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                Log.d(TAG, "Get Channel Info Exception:" + msg);
                if (error instanceof HttpExceptionButFoundCache) {
                    parseResult(msg);
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                //                if (responseInfo.statusCode == 200) {
                parseResult(responseInfo.result);
                //                }
            }
        },
                null,
                0);
    }

    private void subscribe() {
        mSubscribeBtn.setEnabled(false);
        mSubscribeBtn.setImageResource(R.drawable.detail_activity_subscribed_btn);

        String uid = PreferenceUtil.getUid();
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("uid", "" + uid);
        params.addQueryStringParameter("roomId", "" + mChannelId);

        CommonHttpUtils.get("subscribe", params, new RequestCallBack<String>() {

            @Override
            public void onFailure(HttpException error, String msg) {
                mSubscribeBtn.setEnabled(true);
                mSubscribeBtn.setImageResource(R.drawable.detail_activity_unsubscribed_btn);
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                mSubscribeBtn.setEnabled(true);
                if (responseInfo.statusCode == 200) {
                    try {
                        JSONObject object = new JSONObject(responseInfo.result);
                        Log.i(TAG, "object" + object);
                        if (object.getInt("error") != 0) {
                            // 订阅失败
                            mSubscribeBtn.setImageResource(R.drawable.detail_activity_unsubscribed_btn);
                        } else {
                            mChannelSubscribed = true;
                        }
                    } catch (Exception e) {
                        mSubscribeBtn.setImageResource(R.drawable.detail_activity_unsubscribed_btn);
                    }
                }
            }
        },
                null,
                0);
    }

    private void unsubscribe() {
        mSubscribeBtn.setEnabled(false);
        mSubscribeBtn.setImageResource(R.drawable.detail_activity_unsubscribed_btn);

        String uid = PreferenceUtil.getUid();
        RequestParams params = new RequestParams();
        params.addQueryStringParameter("uid", "" + uid);
        params.addQueryStringParameter("roomId", "" + mChannelId);

        CommonHttpUtils.get("unsubscribe",
                params,
                new RequestCallBack<String>() {

                    @Override
                    public void onFailure(HttpException error, String msg) {
                        mSubscribeBtn.setEnabled(true);
                        mSubscribeBtn.setImageResource(R.drawable.detail_activity_subscribed_btn);
                    }

                    @Override
                    public void onSuccess(ResponseInfo<String> responseInfo) {
                        mSubscribeBtn.setEnabled(true);
                        if (responseInfo.statusCode == 200) {
                            try {
                                JSONObject object = new JSONObject(responseInfo.result);
                                if (object.getInt("error") != 0) {
                                    // 取消订阅失败
                                    mSubscribeBtn.setImageResource(R.drawable.detail_activity_subscribed_btn);
                                } else {
                                    mChannelSubscribed = false;
                                }
                            } catch (Exception e) {
                                mSubscribeBtn.setImageResource(R.drawable.detail_activity_subscribed_btn);
                            }
                        }
                    }
                },
                null,
                0);
    }

    public void showPre() {
        finish();
        overridePendingTransition(R.anim.pre_in, R.anim.pre_out);
    }

    @Override
    public void showNext() {
        // TODO Auto-generated method stub
        finish();
        overridePendingTransition(R.anim.pre_in, R.anim.pre_out);
    }

}
