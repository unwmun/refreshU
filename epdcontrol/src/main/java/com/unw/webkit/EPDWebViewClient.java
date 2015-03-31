package com.unw.webkit;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by unw on 15. 3. 31..
 */
public class EPDWebViewClient extends WebViewClient
{

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return false;
    }
}
