package com.wanke.network.http;

public class Constants {

//    public static final String BASE_HOST = "http://54.64.105.44";//"http://192.168.41.101:9257";
    public static final String BASE_HOST = "http://123.57.77.122:802";//"http://192.168.41.101:9257";

    public static String buildImageUrl(String imageId) {
        return BASE_HOST + "/wanketv/static/images/cover/" + imageId;
    }

    public static final String BASE_SDCARD = "file://";

    public static String SDImageUrl(String imageId) {
        return BASE_SDCARD + imageId;
    }

    //xinlang
    public static final String APP_KEY = "1478277550";
    public static final String REDIRECT_URL = "http://www.baidu.com";
    //weixin
    public static final String WX_APP_ID = "wxe793cd583c6cb873";

    /**
     * Scope 是 OAuth2.0 授权机制中 authorize 接口的一个参数。通过 Scope，平台将开放更多的微博
     * 核心功能给开发者，同时也加强用户隐私保护，提升了用户体验，用户在新 OAuth2.0 授权页中有权利
     * 选择赋予应用的功能。
     * 
     * 我们通过新浪微博开放平台-->管理中心-->我的应用-->接口管理处，能看到我们目前已有哪些接口的
     * 使用权限，高级权限需要进行申请。
     * 
     * 目前 Scope 支持传入多个 Scope 权限，用逗号分隔。
     * 
     * 有关哪些 OpenAPI 需要权限申请，请查看：http://open.weibo.com/wiki/%E5%BE%AE%E5%8D%9AAPI
     * 关于 Scope 概念及注意事项，请查看：http://open.weibo.com/wiki/Scope
     */
    public static final String SCOPE =
            "email,direct_messages_read,direct_messages_write,"
                    + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
                    + "follow_app_official_microblog," + "invitation_write";
}
