package com.applovin.mediation.rtb;

import static android.util.Log.DEBUG;
import static android.util.Log.WARN;
import static com.applovin.mediation.ApplovinAdapter.log;

import android.content.Context;
import androidx.annotation.NonNull;
import com.applovin.adview.AppLovinInterstitialAd;
import com.applovin.adview.AppLovinInterstitialAdDialog;
import com.applovin.mediation.AppLovinUtils;
import com.applovin.sdk.AppLovinAd;
import com.applovin.sdk.AppLovinAdClickListener;
import com.applovin.sdk.AppLovinAdDisplayListener;
import com.applovin.sdk.AppLovinAdLoadListener;
import com.applovin.sdk.AppLovinAdVideoPlaybackListener;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;

/**
 * Created by Thomas So on July 17 2018
 */
public final class AppLovinRtbInterstitialRenderer
    implements MediationInterstitialAd, AppLovinAdLoadListener, AppLovinAdDisplayListener,
    AppLovinAdClickListener, AppLovinAdVideoPlaybackListener {

  /**
   * Data used to render an RTB interstitial ad.
   */
  private final MediationInterstitialAdConfiguration adConfiguration;

  /**
   * Callback object to notify the Google Mobile Ads SDK if ad rendering succeeded or failed.
   */
  private final MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback>
      callback;

  /**
   * Listener object to notify the Google Mobile Ads SDK of interstitial presentation events.
   */
  private MediationInterstitialAdCallback mInterstitalAdCallback;

  private final AppLovinSdk sdk;
  private AppLovinInterstitialAdDialog interstitialAd;
  private AppLovinAd ad;

  public AppLovinRtbInterstitialRenderer(
      MediationInterstitialAdConfiguration adConfiguration,
      MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> callback) {

    this.adConfiguration = adConfiguration;
    this.callback = callback;

    this.sdk =
        AppLovinUtils.retrieveSdk(
            adConfiguration.getServerParameters(), adConfiguration.getContext());
  }

  public void loadAd() {
    // Create interstitial object
    interstitialAd = AppLovinInterstitialAd.create(sdk, adConfiguration.getContext());
    interstitialAd.setAdDisplayListener(this);
    interstitialAd.setAdClickListener(this);
    interstitialAd.setAdVideoPlaybackListener(this);

    // Load ad!
    sdk.getAdService().loadNextAdForAdToken(adConfiguration.getBidResponse(), this);
  }

  @Override
  public void showAd(@NonNull Context context) {
    // Update mute state
    boolean muted = AppLovinUtils.shouldMuteAudio(adConfiguration.getMediationExtras());
    sdk.getSettings().setMuted(muted);

    interstitialAd.showAndRender(ad);
  }

  // region AppLovin Listeners
  @Override
  public void adReceived(AppLovinAd ad) {
    log(DEBUG, "Interstitial did load ad: " + ad.getAdIdNumber());

    this.ad = ad;
    mInterstitalAdCallback = callback.onSuccess(AppLovinRtbInterstitialRenderer.this);
  }

  @Override
  public void failedToReceiveAd(int code) {
    AdError error = AppLovinUtils.getAdError(code);
    log(WARN, error.getMessage());
    callback.onFailure(error);
  }

  @Override
  public void adDisplayed(AppLovinAd ad) {
    log(DEBUG, "Interstitial displayed.");
    mInterstitalAdCallback.reportAdImpression();
    mInterstitalAdCallback.onAdOpened();
  }

  @Override
  public void adHidden(AppLovinAd ad) {
    log(DEBUG, "Interstitial hidden.");
    mInterstitalAdCallback.onAdClosed();
  }

  @Override
  public void adClicked(AppLovinAd ad) {
    log(DEBUG, "Interstitial clicked.");
    mInterstitalAdCallback.reportAdClicked();
    mInterstitalAdCallback.onAdLeftApplication();
  }

  @Override
  public void videoPlaybackBegan(AppLovinAd ad) {
    log(DEBUG, "Interstitial video playback began.");
  }

  @Override
  public void videoPlaybackEnded(AppLovinAd ad, double percentViewed, boolean fullyWatched) {
    log(DEBUG, "Interstitial video playback ended at playback percent: " + percentViewed + "%.");
  }
  // endregion

}
