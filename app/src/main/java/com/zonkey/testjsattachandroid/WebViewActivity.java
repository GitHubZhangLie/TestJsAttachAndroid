package com.zonkey.testjsattachandroid;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zonkey.testjsattachandroid.view.ScrollSwipeRefreshLayout;

/**
 * Created by xu.wang
 * Date on 2017/5/22 13:48
 */
public class WebViewActivity extends BaseWebViewActivity implements SwipeRefreshLayout.OnRefreshListener {
    //    private String testUrl = "http://192.168.12.214:12091/";
    private String testUrl = "file:///android_asset/zonkey/testjs.html";
    private final static String TAG = "WebViewActivity";
    private final static String PROJECT_NAME = "zkzs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        mWebView = (WebView) findViewById(R.id.wv_test);
        tv_title = (TextView) findViewById(R.id.tv_webview_title);
        mSwipeRefreshLayout = (ScrollSwipeRefreshLayout) findViewById(R.id.scroll_swipe_refresh_layout);
        rl_nodata = (RelativeLayout) findViewById(R.id.rl_nodata);

        initData();

    }

    private void initData() {
        JsApi.getInstance().initial(this);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(testUrl);
        mWebView.addJavascriptInterface(JsApi.getInstance(), PROJECT_NAME);
        mSwipeRefreshLayout.setColorSchemeColors(Color.GREEN, Color.RED);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setViewGroup(mWebView);
        mSwipeRefreshLayout.setRefreshing(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
//                Log.e(TAG, "progress = " + newProgress);
                if (newProgress == 100) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showWebView();
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(WebViewActivity.this);
                b.setTitle("提示");
                b.setMessage(message);
                b.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setCancelable(false);
                if (WebViewActivity.this.isFinishing()) {
                    return super.onJsAlert(view, url, message, result);
                }
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                AlertDialog.Builder b = new AlertDialog.Builder(WebViewActivity.this);
                b.setTitle("提示");
                b.setMessage(message);
                b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                if (WebViewActivity.this.isFinishing()) {
                    return super.onJsConfirm(view, url, message, result);
                }
                b.create().show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                final View v = LayoutInflater.from(WebViewActivity.this).inflate(R.layout.prompt_dialog, null);
                ((TextView) v.findViewById(R.id.prompt_message_text)).setText(message);
                ((EditText) v.findViewById(R.id.prompt_input_field)).setText(defaultValue);
                AlertDialog.Builder b = new AlertDialog.Builder(WebViewActivity.this);
                b.setTitle("提示");
                b.setView(v);
                b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String value = ((EditText) v.findViewById(R.id.prompt_input_field)).getText().toString();
                        result.confirm(value);
                    }
                });
                b.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                if (WebViewActivity.this.isFinishing()) {
                    return super.onJsPrompt(view, url, message, defaultValue, result);
                }
                b.create().show();
                return true;
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Log.e(TAG, "onReceivedError");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.e(TAG, "shouldOverrideUrlLoading");
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case JsApi.REQUEST_CODE:
                mWebView.loadUrl("javascript:showFromNative()");
                mWebView.loadUrl("javascript: alertJs()");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    String script = "getState(" + 1 + ")";
                    mWebView.evaluateJavascript(script, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            Log.e("WebViewActivity", "" + value);
                        }
                    });
                } else {
//                    wv_test.loadUrl("javascript:getState()");
                }
                break;
        }
    }

    @Override
    public void onRefresh() {
        mWebView.loadUrl(testUrl);
        mSwipeRefreshLayout.setRefreshing(true);
    }


}
