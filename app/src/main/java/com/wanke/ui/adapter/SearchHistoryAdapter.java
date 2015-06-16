package com.wanke.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wanke.tv.R;
import com.wanke.ui.SearchHistoryData;
import com.wanke.ui.activity.SearchActivity;
import com.wanke.ui.activity.SearchShowActivity;

public class SearchHistoryAdapter extends BaseAdapter {
    private Context mContext;
    private List<SearchHistoryData> mObjects;
    private ArrayList<SearchHistoryData> mOriginalValues;
    private final Object mLock = new Object();
    private int mMaxMatch = 10;// 最多显示多少个选项,负数表示全部
    private OnClickListener mOnClickListener;

    public SearchHistoryAdapter(Context context, int maxMatch,
            OnClickListener onClickListener) {
        this.mContext = context;
        this.mMaxMatch = maxMatch;
        this.mOnClickListener = onClickListener;
        initSearchHistory();
        mObjects = mOriginalValues;
    }

    @Override
    public int getCount() {
        return null == mObjects ? 0 : mObjects.size();
    }

    @Override
    public Object getItem(int position) {
        return null == mObjects ? 0 : mObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.action_search_history, parent, false);
            holder = new ViewHolder();
            holder.content = (TextView) convertView.findViewById(R.id.search_history_content);
            convertView.setTag(holder);
            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent();
                    intent.setClass(mContext,
                            SearchShowActivity.class);
                    mContext.startActivity(intent);
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        SearchHistoryData data = mObjects.get(position);
        holder.content.setText(data.getContent());
        return convertView;

    }

    /**
     * 读取历史搜索记录
     */
    public void initSearchHistory() {
        SharedPreferences sp = mContext.getSharedPreferences(
                SearchActivity.SEARCH_HISTORY, 0);
        String longhistory = sp.getString(SearchActivity.SEARCH_HISTORY, "");
        String[] hisArrays = longhistory.split(",");
        mOriginalValues = new ArrayList<SearchHistoryData>();
        if (hisArrays.length == 1) {
            return;
        }
        for (int i = 0; i < hisArrays.length; i++) {
            mOriginalValues.add(new SearchHistoryData().setContent(hisArrays[i]));
        }

    }

    /**
     * 匹配过滤搜索内容
     * 
     * @param prefix
     *            输入框中输入的内容
     */
    public void performFiltering(CharSequence prefix) {
        if (prefix == null || prefix.length() == 0) {//搜索框内容为空的时候显示所有历史记录
            synchronized (mLock) {
                mObjects = mOriginalValues;
            }
        } else {
            String prefixString = prefix.toString().toLowerCase();
            int count = mOriginalValues.size();
            ArrayList<SearchHistoryData> newValues = new ArrayList<SearchHistoryData>(
                    count);
            for (int i = 0; i < count; i++) {
                final String value = mOriginalValues.get(i).getContent();
                final String valueText = value.toLowerCase();
                if (valueText.contains(prefixString)) {
                }
                if (valueText.startsWith(prefixString)) {
                    newValues.add(new SearchHistoryData().setContent(valueText));
                } else {
                    final String[] words = valueText.split(" ");
                    final int wordCount = words.length;
                    for (int k = 0; k < wordCount; k++) {
                        if (words[k].startsWith(prefixString)) {
                            newValues.add(new SearchHistoryData()
                                    .setContent(value));
                            break;
                        }
                    }
                }
                if (mMaxMatch > 0) {
                    if (newValues.size() > mMaxMatch - 1) {
                        break;
                    }
                }
            }
            mObjects = newValues;
        }
        notifyDataSetChanged();
    }

    private class ViewHolder {
        TextView content;
    }
}
