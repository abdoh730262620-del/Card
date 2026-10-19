package com.example.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobHelper {
    private const val TAG = "AdMobHelper"

    // Standard Google AdMob Test Ad Unit IDs
    const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val TEST_NATIVE_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false

    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            MobileAds.initialize(context) { status ->
                isInitialized = true
                Log.d(TAG, "AdMob initialized successfully: $status")
            }
            loadInterstitial(context)
            loadRewarded(context)
        } catch (e: Exception) {
            Log.e(TAG, "AdMob initialization failed", e)
        }
    }

    fun loadInterstitial(context: Context, adUnitId: String = TEST_INTERSTITIAL_AD_UNIT_ID) {
        if (interstitialAd != null || isLoadingInterstitial) return
        isLoadingInterstitial = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoadingInterstitial = false
                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    isLoadingInterstitial = false
                    Log.w(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showInterstitial(
        activity: Activity,
        adUnitId: String = TEST_INTERSTITIAL_AD_UNIT_ID,
        onDismissed: () -> Unit = {}
    ) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity, adUnitId)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity, adUnitId)
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            // Not ready or failed, reload and proceed
            loadInterstitial(activity, adUnitId)
            onDismissed()
        }
    }

    fun loadRewarded(context: Context, adUnitId: String = TEST_REWARDED_AD_UNIT_ID) {
        if (rewardedAd != null || isLoadingRewarded) return
        isLoadingRewarded = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingRewarded = false
                    Log.d(TAG, "Rewarded ad loaded successfully")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    isLoadingRewarded = false
                    Log.w(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                }
            }
        )
    }

    fun showRewarded(
        activity: Activity,
        adUnitId: String = TEST_REWARDED_AD_UNIT_ID,
        onUserEarnedReward: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewarded(activity, adUnitId)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    loadRewarded(activity, adUnitId)
                    // Grant simulated reward for developer testing in case emulator display drops
                    onUserEarnedReward(50, "تجريبي")
                    onDismissed()
                }
            }
            ad.show(activity) { rewardItem ->
                onUserEarnedReward(rewardItem.amount, rewardItem.type)
            }
        } else {
            // If ad is not ready, reload and simulate successful reward so developer can test immediately
            loadRewarded(activity, adUnitId)
            onUserEarnedReward(50, "تجريبي (محاكاة)")
            onDismissed()
        }
    }
}
