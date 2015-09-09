package com.enthusiast94.reddit_reader.app.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.enthusiast94.reddit_reader.app.R;
import com.enthusiast94.reddit_reader.app.events.HideContentViewerEvent;
import com.enthusiast94.reddit_reader.app.events.OauthCallbackEvent;
import com.enthusiast94.reddit_reader.app.network.AuthManager;
import com.enthusiast94.reddit_reader.app.utils.Helpers;
import com.enthusiast94.reddit_reader.app.utils.OnBackPressedListener;
import de.greenrobot.event.EventBus;

import java.util.Map;

/**
 * Created by manas on 05-09-2015.
 */
public class ContentViewerFragment extends Fragment implements OnBackPressedListener {

    public static final String TAG = ContentViewerFragment.class.getSimpleName();
    public static final String URL_BUNDLE_KEY = "url_key";
    public static final String CONTENT_TITLE_BUNDLE_KEY = "content_title_key";
    private Toolbar toolbar;
    private WebView webView;
    private String contentTitle;
    private String contentUrl;
    private boolean isVisible;
    private static final String IS_VISIBLE_BUNDLE_KEY = "is_visible_key";

    public static ContentViewerFragment newInstance(String contentTitle, String url) {
        Bundle bundle = new Bundle();
        bundle.putString(CONTENT_TITLE_BUNDLE_KEY, contentTitle);
        bundle.putString(URL_BUNDLE_KEY, url);
        ContentViewerFragment contentViewerFragment = new ContentViewerFragment();
        contentViewerFragment.setArguments(bundle);

        return contentViewerFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate fragment");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_viewer, container, false);

        /**
         * Find views
         */

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        webView = (WebView) view.findViewById(R.id.webview);

        /**
         * Setup toolbar
         */

        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        /**
         * Configure browser settings
         */

        WebSettings settings = webView.getSettings();
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptEnabled(true);
        // fit content to screen
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        // enable pinch to zoom
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        // enable local storage
        settings.setDomStorageEnabled(true);
        // force to load url in the webview itself instead of opening default browser
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(AuthManager.REDIRECT_URI)) {
                    Map<String, String> params = Helpers.parseUrlHashParams(url);

                    EventBus.getDefault().post(new OauthCallbackEvent(
                            params.get("access_token"),
                            params.get("expires_in"),
                            params.get("state"),
                            params.get("error")
                    ));

                    onBackPressed();
                } else {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (contentTitle == null) {
                    toolbar.setTitle(contentUrl);
                    toolbar.setSubtitle(null);
                } else {
                    toolbar.setTitle(contentTitle);
                    toolbar.setSubtitle(contentUrl);
                }
            }
        });

        /**
         * Retrieve content info from arguments and load URL
         */

        Bundle bundle = getArguments();
        loadContent(bundle.getString(CONTENT_TITLE_BUNDLE_KEY), bundle.getString(URL_BUNDLE_KEY));

        /**
         * Restore isVisible from saved instance state and hide this fragment depending if its value is false. This
         * needs to be done because for some reason the fragment manager shows all hidden fragments on configuration
         * change. isVisible is set to true initially since onHiddenChanged only starts getting called after the fragment
         * is hidden for the first time, i.e. when savedInstanceState != null.
         */

        isVisible = savedInstanceState == null || savedInstanceState.getBoolean(IS_VISIBLE_BUNDLE_KEY);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // event needs to be posted here because activity registers the eventbus in its onResume() method
        if (!isVisible) {
            EventBus.getDefault().post(new HideContentViewerEvent());
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, hidden + ": hidden");
        isVisible = !hidden;
    }

    public void loadContent(String contentTitle, String contentUrl) {
        this.contentTitle = contentTitle;
        this.contentUrl = contentUrl;

        toolbar.setTitle(R.string.loading);
        toolbar.setSubtitle(contentUrl);

        webView.loadUrl(contentUrl);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_VISIBLE_BUNDLE_KEY, isVisible);
    }

    @Override
    public void onBackPressed() {
        webView.loadUrl("about:blank");

        EventBus.getDefault().post(new HideContentViewerEvent());

        isVisible = false;
    }
}
