package com.eggttball.library.view;

import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * WebView.loadUrl 預設會另開瀏覽器，必須自定義 WebViewClient
 * Created by eggttball on 2015/1/11.
 */
public class MyWebViewClient extends WebViewClient {

    private int _progressBarId = 0;
    private final int LOADTIME = 3000;  // 網頁載入的預估時間（毫秒），時間到後自動隱藏 ProgressBar

    public MyWebViewClient()	{
        this(0);
    }


    public MyWebViewClient(int progressBarId)	{
        _progressBarId = progressBarId;
    }


    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        // 網頁載入 LOADTIME 毫秒後，自動隱藏 ProgressBar
        if (_progressBarId > 0) {
            final View progress = view.getRootView().findViewById(_progressBarId);
            progress.postDelayed(new Runnable() {

                @Override
                public void run() {
                    progress.setVisibility(View.GONE);
                }
            }, LOADTIME);
        }

        super.onPageFinished(view, url);
    }

}
