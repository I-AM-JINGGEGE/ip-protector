package com.vpn.android.ui.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vpn.android.R;

public class CommonWebViewFragment extends CommonFragment {
    private String mUrl;
    private WebViewCallback mWebViewCallback;

    public CommonWebViewFragment() {
        super();
    }

    public CommonWebViewFragment(String url, WebViewCallback webViewCallback) {
        mUrl = url;
        mWebViewCallback = webViewCallback;
    }

    public CommonWebViewFragment(int contentLayoutId, String url, WebViewCallback webViewCallback) {
        super(contentLayoutId);
        mUrl = url;
        mWebViewCallback = webViewCallback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WebView webView = view.findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                if (mWebViewCallback == null) {
                    return;
                }
                mWebViewCallback.onPageFinished(view, url);
            }
        });
        webView.loadUrl(mUrl);
    }

    public interface WebViewCallback {
        void onPageFinished(WebView view, String url);
    }
}
