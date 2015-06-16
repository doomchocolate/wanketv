package com.wanke.ui.fragment;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.mobstat.StatService;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.wanke.model.ChannelInfo;
import com.wanke.model.ParserUtil;
import com.wanke.network.http.CommonHttpUtils;
import com.wanke.network.http.HttpExceptionButFoundCache;
import com.wanke.tv.R;
import com.wanke.ui.ToastUtil;
import com.wanke.ui.adapter.LiveChannelAdapter;

public class FragmentLive extends BaseFragment {

    private PullToRefreshGridView mChannelList;
    private LiveChannelAdapter mAdapter;
    protected ImageLoader mImageLoader = ImageLoader.getInstance();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        StatService.onEvent(getActivity(), "105001", "live.fragment", 1);
        //判断有误网络连接
        if (isConnect(getActivity()) == false) {
            ToastUtil.showToast(getActivity(), R.string.network);
        }

        mChannelList = (PullToRefreshGridView) inflater.inflate(R.layout.fragment_live,
                container,
                false);
        //        mChannelList = (PullToRefreshGridView) view.findViewById(R.id.channel_list);
        mChannelList.getRefreshableView();
        mChannelList.setMode(Mode.BOTH);
        mChannelList.getLoadingLayoutProxy(false, true).setPullLabel("上拉加载...");
        mChannelList.getLoadingLayoutProxy(false, true)
                .setRefreshingLabel("正在加载...");
        mChannelList.getLoadingLayoutProxy(false, true)
                .setReleaseLabel("松开加载更多...");

        // 设置PullRefreshListView下拉加载时的加载提示
        mChannelList.getLoadingLayoutProxy(true, false).setPullLabel("下拉刷新...");
        mChannelList.getLoadingLayoutProxy(true, false)
                .setRefreshingLabel("正在加载...");
        mChannelList.getLoadingLayoutProxy(true, false)
                .setReleaseLabel("松开加载更多...");

        mChannelList.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {

            @Override
            public void onLastItemVisible() {
                if (mHasMore) {
                    getNextPage();
                }
            }
        });

        mChannelList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                StatService.onEvent(getActivity(), "105002", "live.fragment", 1);
                // TODO Auto-generated method stub
                //                Intent intent;
                //                intent = new Intent(getActivity(), VideoDetailPages.class);
                //                intent.putExtra("gameurl", "rtmp://192.168.41.234/live/hello");
                //                startActivity(intent);
            }

        });

        mAdapter = new LiveChannelAdapter();
        mChannelList.setAdapter(mAdapter);

        getNextPage();

        Log.d(TAG, "FragmentLive: onCreateView");

        return mChannelList;
    }

    private int mCurrentPage = 0;
    private boolean mHasMore = true;
    private boolean mInGetNextPage = false;
    private boolean mPreviousState = true;

    private void getNextPage() {
        if (mInGetNextPage) {
            // 正在进行更新
            return;
        }
        mInGetNextPage = true;

        final int thisPage = mCurrentPage;
        //        String action = "recommend?offset=" + mCurrentPage + "&limit=20";

        RequestParams params = new RequestParams();
        params.addQueryStringParameter("offset", "" + mCurrentPage);
        params.addQueryStringParameter("limit", "" + 20);

        CommonHttpUtils.get("recommend", params, new RequestCallBack<String>() {

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (responseInfo.statusCode == 200) {
                    try {
                        //                        JSONObject object = new JSONObject(responseInfo.result);
                        //                        ArrayList<ChannelInfo> channelInfos = ParserUtil.parseChannelsInfo(object.getJSONArray("data"));

                        if (parseResult(responseInfo.result) >= 20) {
                            mCurrentPage++;
                        } else {
                            mHasMore = false;
                        }

                        //                        mAdapter.addChannels(channelInfos);
                    } catch (Exception e) {
                        Log.d(TAG, "Get Next Page Exception:" + e.toString());
                    }

                    mPreviousState = true;
                    mInGetNextPage = false;
                }
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                if (thisPage == 0 && mPreviousState) {
                    if (error instanceof HttpExceptionButFoundCache) {
                        parseResult(msg);
                    }
                }

                mPreviousState = false;
                mInGetNextPage = false;
            }
        }, "recomment:" + mCurrentPage + ":20");
    }

    private int parseResult(String content) {
        int size = 0;
        try {
            JSONObject object = new JSONObject(content);
            ArrayList<ChannelInfo> channelInfos = ParserUtil.parseChannelsInfo(object.getJSONArray("data"));
            mAdapter.addChannels(channelInfos);
            size = channelInfos.size();
        } catch (Exception e) {
            Log.d(TAG, "Get Next Page Exception:" + e.toString());
        }
        return size;
    }

    private boolean isConnect(FragmentActivity fragmentActivity) {
        ConnectivityManager conManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = conManager
                .getActiveNetworkInfo();

        if (networkInfo != null) {
            return networkInfo
                    .isAvailable();
        }
        return false;
    }
}
