package com.wanke;

import android.app.Application;

import com.baidu.mobstat.StatService;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class WankeTVApplication extends Application {

    private static WankeTVApplication mCurrentApplication = null;

    public static WankeTVApplication getApplication() {
        return mCurrentApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mCurrentApplication = this;

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).threadPriority(Thread.NORM_PRIORITY + 1)
                .threadPoolSize(5).denyCacheImageMultipleSizesInMemory()
                .memoryCache(new WeakMemoryCache())
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        // 初始化百度统计
        StatService.setAppChannel(this, "offical", true);
        StatService.setSessionTimeOut(30);
    }

    public static WankeTVApplication getCurrentApplication() {
        return mCurrentApplication;
    }
}
