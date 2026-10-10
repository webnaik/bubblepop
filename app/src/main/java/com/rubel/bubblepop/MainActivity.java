package com.rubel.bubblepop;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends ComponentActivity {

    private WebView web;
    private RewardedAd rewardedAd;
    private boolean loading = false;
    private boolean earned = false;
    private final AtomicBoolean adsInitStarted = new AtomicBoolean(false);

    // Gives the game its window.NativeRewarded hook (game checks it at call time)
    private static final String BRIDGE_JS =
        "(function(){window.__rw=null;" +
        "window.__rwDone=function(ok){var r=window.__rw;window.__rw=null;if(!r)return;if(ok)r.ok();else r.fail();};" +
        "window.NativeRewarded=function(ok,fail){window.__rw={ok:ok,fail:fail};AndroidAds.showRewarded();};})();";

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        if (Build.VERSION.SDK_INT >= 28) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        web = new WebView(this);
        setContentView(web);
        hideSystemUi();

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void showRewarded() { runOnUiThread(MainActivity.this::showRewardedAd); }
        }, "AndroidAds");
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v, String url) { v.evaluateJavascript(BRIDGE_JS, null); }
        });
        web.loadUrl("file:///android_asset/index.html");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { finish(); }
        });

        requestConsentThenInitAds();
    }

    // GDPR/EEA consent (Google UMP). Ads only start once consent allows it.
    private void requestConsentThenInitAds() {
        final ConsentInformation ci = UserMessagingPlatform.getConsentInformation(this);
        ci.requestConsentInfoUpdate(this, new ConsentRequestParameters.Builder().build(),
            () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(this, formError -> {
                if (ci.canRequestAds()) initAds();
            }),
            requestError -> { if (ci.canRequestAds()) initAds(); });
        if (ci.canRequestAds()) initAds();
    }

    private void initAds() {
        if (adsInitStarted.getAndSet(true)) return;
        MobileAds.initialize(this, status -> runOnUiThread(this::loadRewardedAd));
    }

    private void loadRewardedAd() {
        if (loading || rewardedAd != null) return;
        loading = true;
        RewardedAd.load(this, BuildConfig.REWARDED_UNIT_ID, new AdRequest.Builder().build(),
            new RewardedAdLoadCallback() {
                @Override public void onAdLoaded(@NonNull RewardedAd ad) { rewardedAd = ad; loading = false; }
                @Override public void onAdFailedToLoad(@NonNull LoadAdError e) { rewardedAd = null; loading = false; }
            });
    }

    private void showRewardedAd() {
        if (rewardedAd == null) {
            jsResult(false);
            if (adsInitStarted.get()) loadRewardedAd();
            return;
        }
        earned = false;
        final RewardedAd ad = rewardedAd;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override public void onAdDismissedFullScreenContent() {
                rewardedAd = null;
                jsResult(earned);              // reward only if fully watched
                loadRewardedAd();
            }
            @Override public void onAdFailedToShowFullScreenContent(@NonNull AdError e) {
                rewardedAd = null;
                jsResult(false);
                loadRewardedAd();
            }
        });
        ad.show(this, item -> earned = true);
    }

    private void jsResult(boolean ok) {
        web.evaluateJavascript("window.__rwDone&&window.__rwDone(" + ok + ")", null);
    }

    private void hideSystemUi() {
        WindowInsetsControllerCompat c = new WindowInsetsControllerCompat(getWindow(), web);
        c.hide(WindowInsetsCompat.Type.systemBars());
        c.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @Override protected void onPause() { super.onPause(); web.onPause(); }
    @Override protected void onResume() { super.onResume(); web.onResume(); }
    @Override protected void onDestroy() { web.destroy(); super.onDestroy(); }
}
