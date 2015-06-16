package com.wanke.ui.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.wanke.tv.R;
import com.wanke.ui.ToastUtil;
import com.wanke.ui.UiUtils;

public abstract class BaseActivity extends AppCompatActivity {
    protected final static String TAG = "activity";

    protected Dialog mWaitingDialog = null;
    private GestureDetector mGestureDetector;

    protected void showWaitingDialog() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mWaitingDialog = UiUtils.showWaitingDialog(BaseActivity.this);
            }
        });
    }

    protected void dismissWaitingDialog() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (mWaitingDialog != null) {
                        mWaitingDialog.dismiss();
                    }
                } catch (Exception e) {
                }
            }
        });

    }

    protected void showToast(final String msg) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ToastUtil.showToast(BaseActivity.this, msg);
            }
        });
    }

    protected void showToast(final int resId) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ToastUtil.showToast(BaseActivity.this, resId);
            }
        });
    }

    /**
     * 不显示搜索view
     */
    protected final static int FLAG_SEARCH_VIEW = 0x01;
    protected final static int FLAG_DISABLE_HOME_AS_UP = 0x02;

    protected int getFlag() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if ((getFlag() & FLAG_DISABLE_HOME_AS_UP) != FLAG_DISABLE_HOME_AS_UP) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        //GestureDetector 
        mGestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                            float velocityX, float velocityY) {

                        if (Math.abs(velocityX) < 200) {
                            Log.i(TAG, "移动的太慢了...");
                            return true;
                        }

                        if (Math.abs(e1.getRawY() - e2.getRawY()) > 200) {
                            Log.i(TAG, "动作非法");
                            return true;
                        }
                        // left
                        if (e1.getRawX() - e2.getRawX() > 200) {
                            showNext();
                            return true;
                        }
                        //right
                        if (e2.getRawX() - e1.getRawX() > 200) {
                            showPre();
                            return true;
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ((getFlag() & FLAG_SEARCH_VIEW) == FLAG_SEARCH_VIEW) {
            MenuItem menuItem = menu.add("delete");
            menuItem.setIcon(R.drawable.action_bar_search_btn_bg)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
                            | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
            menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Intent intent = new Intent();
                    intent.setClass(BaseActivity.this,
                            SearchActivity.class);
                    startActivity(intent);
                    return true;
                }
            });
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            break;

        default:
            break;
        }
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        super.dispatchTouchEvent(ev);
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    public void next(View view) {
        showNext();
    }

    public void pre(View view) {
        showPre();
    }

    public abstract void showNext();

    public abstract void showPre();
}
