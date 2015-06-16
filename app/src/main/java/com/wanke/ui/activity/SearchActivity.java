package com.wanke.ui.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ListView;

import com.wanke.tv.R;
import com.wanke.ui.SearchHistoryData;
import com.wanke.ui.adapter.SearchHistoryAdapter;

import java.util.ArrayList;
import java.util.Arrays;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class SearchActivity extends BaseActivity implements OnClickListener {

    public static final String SEARCH_HISTORY = "search_history";
    private EditText mInput;
    private ListView mListView;
    private View mBack, mDelete, mSearch, mHistorydelete, mEmpty;
    ArrayList<String> mAllList = new ArrayList<String>();
    private SearchHistoryAdapter mSearchHistoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);



        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mTitleView = mInflater.inflate(R.layout.action_bar_layout,
                null);
        getSupportActionBar().setCustomView(
                mTitleView,
                new ActionBar.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
        onCreateinitView();
        initView();
    }

    @Override
    public void showNext() {

    }

    @Override
    public void showPre() {

    }

    protected void onCreateinitView() {
        mSearchHistoryAdapter = new SearchHistoryAdapter(this, -1, this);
        mHistorydelete = findViewById(R.id.action_bar_history_delete);
        mListView = (ListView) findViewById(R.id.search_listview);
        mListView.setAdapter(mSearchHistoryAdapter);
        mListView.setDivider(null);
    }

    protected void initView() {
        mBack = findViewById(R.id.action_bar_back);
        mSearch = findViewById(R.id.action_bar_srarch);
        mInput = (EditText) findViewById(R.id.action_bar_input);
        mDelete = findViewById(R.id.action_bar_delete_input);
        mSearch.setOnClickListener(this);
        mBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                finish();
            }
        });
        mDelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mInput.setText("");
            }
        });
        mInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                mSearchHistoryAdapter.performFiltering(s);
            }

            @Override
            public void beforeTextChanged(
                    CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });

        mHistorydelete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                dialog();
            }
        });
    }

    protected void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.history_dialog_title);
        builder.setPositiveButton(R.string.history_dialog_bt_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences sp = getSharedPreferences(SEARCH_HISTORY,
                                0);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.clear().commit();
                        Intent intent = new Intent();
                        intent.setClass(getApplicationContext(),
                                SearchActivity.class);
                        startActivity(intent);
                        finish();
                        dialog.dismiss();
                        mSearchHistoryAdapter.notifyDataSetChanged();

                    }
                });
        builder.setNegativeButton(R.string.history_dialog_bt_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    /*
     * 保存搜索记录
     */
    private void saveSearchHistory() {
        String text = mInput.getText().toString().trim();
        if (text.length() < 1) {
            return;
        }
        SharedPreferences sp = getSharedPreferences(SEARCH_HISTORY, 0);
        String longhistory = sp.getString(SEARCH_HISTORY, "");
        String[] tmpHistory = longhistory.split(",");
        ArrayList<String> history = new ArrayList<String>(
                Arrays.asList(tmpHistory));
        if (history.size() > 0) {
            int i;
            for (i = 0; i < history.size(); i++) {
                if (text.equals(history.get(i))) {
                    history.remove(i);
                    break;
                }
            }
            history.add(0, text);
        }
        if (history.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < history.size(); i++) {
                sb.append(history.get(i) + ",");
            }
            sp.edit().putString(SEARCH_HISTORY, sb.toString()).commit();
        } else {
            sp.edit().putString(SEARCH_HISTORY, text + ",").commit();
        }
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        int id = view.getId();
        if (id == R.id.action_bar_srarch) {
            saveSearchHistory();
            mSearchHistoryAdapter.initSearchHistory();
            Intent intent = new Intent();
            intent.setClass(SearchActivity.this,
                    SearchShowActivity.class);
            startActivity(intent);
            mInput.setText("");
        } else {
            SearchHistoryData data = (SearchHistoryData) view.getTag();
            mInput.setText(data.getContent());
        }

    }

}
