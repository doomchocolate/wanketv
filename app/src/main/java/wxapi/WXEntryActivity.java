package wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.wanke.tv.R;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
    private static final String TAG = "WXEntryActivity";
    // IWXAPI 是第三方app和微信通信的openapi接口  
    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        api = WXAPIFactory.createWXAPI(this, "wxe793cd583c6cb873", false);
        api.registerApp("wxe793cd583c6cb873");
        api.handleIntent(getIntent(), this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onReq(BaseReq arg0) {
    }

    @Override
    public void onResp(BaseResp resp) {
        int result = 0;

        switch (resp.errCode) {
        case BaseResp.ErrCode.ERR_OK:
            result = R.string.errcode_success;
            //分享成功  
            break;
        case BaseResp.ErrCode.ERR_USER_CANCEL:
            result = R.string.errcode_cancel;
            //分享取消  
            break;
        case BaseResp.ErrCode.ERR_AUTH_DENIED:
            result = R.string.errcode_deny;
            //分享拒绝  
            break;
        default:
            result = R.string.errcode_unknown;
            break;
        }

        // TODO 微信分享 成功之后调用接口  
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
