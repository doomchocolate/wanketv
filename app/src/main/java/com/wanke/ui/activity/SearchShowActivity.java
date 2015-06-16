package com.wanke.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.wanke.tv.R;

public class SearchShowActivity extends BaseActivity {

    private ListView mListView;
    private View mEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchshow);
        mListView = (ListView) findViewById(R.id.searchshow_listview);
        mEmpty = findViewById(R.id.nomy_searchshow_listview);
        mEmpty = findViewById(R.id.nomy_searchshow_listview);
        mListView.setEmptyView(mEmpty);
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
