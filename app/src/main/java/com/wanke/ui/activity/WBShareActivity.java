/*
 * Copyright (C) 2010-2013 The SINA WEIBO Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wanke.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMessage;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboDownloadListener;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboShareException;
import com.wanke.network.http.Constants;
import com.wanke.tv.R;

public class WBShareActivity extends Activity implements IWeiboHandler.Response {
    @SuppressWarnings("unused")
    private static final String TAG = "WBShareActivity";

    private TextView mTitleView;

    private ImageView mImageView;

    /** 微博微博分享接口实例 */
    private IWeiboShareAPI mWeiboShareAPI = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        initViews();

        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);

        // 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
        // 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
        // NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
        mWeiboShareAPI.registerApp();

        // 如果未安装微博客户端，设置下载微博对应的回调
        if (!mWeiboShareAPI.isWeiboAppInstalled()) {
            mWeiboShareAPI.registerWeiboDownloadListener(new IWeiboDownloadListener() {
                @Override
                public void onCancel() {
                    Toast.makeText(WBShareActivity.this,
                            R.string.weibosdk_demo_cancel_download_weibo,
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        try {
            // 检查微博客户端环境是否正常，如果未安装微博，弹出对话框询问用户下载微博客户端
            if (mWeiboShareAPI.checkEnvironment(true)) {
                sendMessage(mImageView);
            }
            this.finish();
        } catch (WeiboShareException e) {
            e.printStackTrace();
            Toast.makeText(WBShareActivity.this,
                    e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @see {@link Activity#onNewIntent}
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 从当前应用唤起微博并进行分享后，返回到当前应用时，需要在此处调用该函数
        // 来接收微博客户端返回的数据；执行成功，返回 true，并调用
        // {@link IWeiboHandler.Response#onResponse}；失败返回 false，不调用上述回调
        mWeiboShareAPI.handleWeiboResponse(intent, this);
    }

    /**
     * 接收微客户端博请求的数据。
     * 当微博客户端唤起当前应用并进行分享时，该方法被调用。
     * 
     * @param baseRequest
     *            微博请求数据对象
     * @see {@link IWeiboShareAPI#handleWeiboRequest}
     */
    @Override
    public void onResponse(BaseResponse baseResp) {
        switch (baseResp.errCode) {
        case WBConstants.ErrorCode.ERR_OK:
            Toast.makeText(this,
                    R.string.weibosdk_demo_toast_share_success,
                    Toast.LENGTH_LONG).show();
            break;
        case WBConstants.ErrorCode.ERR_CANCEL:
            Toast.makeText(this,
                    R.string.weibosdk_demo_toast_share_canceled,
                    Toast.LENGTH_LONG).show();
            break;
        case WBConstants.ErrorCode.ERR_FAIL:
            Toast.makeText(this,
                    getString(R.string.weibosdk_demo_toast_share_failed)
                            + "Error Message: " + baseResp.errMsg,
                    Toast.LENGTH_LONG).show();
            break;
        }
    }

    /**
     * 初始化界面。
     */
    private void initViews() {
        mTitleView = (TextView) findViewById(R.id.share_title);
        mTitleView.setText(R.string.weibosdk_demo_share_to_weibo_title);
        mImageView = (ImageView) findViewById(R.id.share_imageview);

    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     */
    private void sendMessage(ImageView imag) {
        if (mWeiboShareAPI.isWeiboAppSupportAPI()) {
            int supportApi = mWeiboShareAPI.getWeiboAppSupportAPI();
            if (supportApi >= 10351 /* ApiUtils.BUILD_INT_VER_2_2 */) {
                sendMultiMessage(imag);
            }
        } else {
            Toast.makeText(this,
                    R.string.weibosdk_demo_not_support_api_hint,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     * 注意：当 {@link IWeiboShareAPI#getWeiboAppSupportAPI()} >= 10351
     * 时，支持同时分享多条消息，
     * 同时可以分享文本、图片以及其它媒体资源（网页、音乐、视频、声音中的一种）。
     * 
     * 
     * @param hasImage
     *            分享的内容是否有图片
     */
    private void sendMultiMessage(ImageView hasImage) {

        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();

        weiboMessage.imageObject = getImageObj();

        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        mWeiboShareAPI.sendRequest(request);
    }

    /**
     * 第三方应用发送请求消息到微博，唤起微博分享界面。
     * 当{@link IWeiboShareAPI#getWeiboAppSupportAPI()} < 10351 时，只支持分享单条消息，即
     * 文本、图片、网页、音乐、视频中的一种，不支持Voice消息。
     * 
     * 
     * @param hasImage
     *            分享的内容是否有图片
     */
    private void sendSingleMessage(boolean hasImage) {

        // 1. 初始化微博的分享消息
        WeiboMessage weiboMessage = new WeiboMessage();
        if (hasImage) {
            weiboMessage.mediaObject = getImageObj();
        }

        // 2. 初始化从第三方到微博的消息请求
        SendMessageToWeiboRequest request = new SendMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.message = weiboMessage;

        // 3. 发送请求消息到微博，唤起微博分享界面
        mWeiboShareAPI.sendRequest(request);
    }

    /**
     * 获取分享的文本模板。
     * 
     * @return 分享的文本模板
     */
    private String getSharedText() {
        int formatId = R.string.weibosdk_demo_share_text_template;
        String format = getString(formatId);
        String text = format;
        String demoUrl = getString(R.string.weibosdk_demo_app_url);
        format = getString(R.string.weibosdk_demo_share_text_template);
        return text;
    }

    /**
     * 创建文本消息对象。
     * 
     * @return 文本消息对象。
     */
    private TextObject getTextObj() {
        TextObject textObject = new TextObject();
        textObject.text = getSharedText();
        return textObject;
    }

    /**
     * 创建图片消息对象。
     * 
     * @return 图片消息对象。
     */
    private ImageObject getImageObj() {
        ImageObject imageObject = new ImageObject();
        BitmapDrawable bitmapDrawable = (BitmapDrawable) mImageView.getDrawable();
        imageObject.setImageObject(bitmapDrawable.getBitmap());
        return imageObject;
    }

}
