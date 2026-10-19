package com.example.data.model

import com.example.util.AdMobHelper

enum class AdBannerSize(val titleAr: String, val heightDp: Int, val widthDp: Int) {
    STANDARD("بانر قياسي (320x50 dp)", 50, 320),
    LARGE("بانر كبير (320x100 dp)", 100, 320),
    MEDIUM_RECTANGLE("مستطيل متوسط MREC (300x250 dp)", 250, 300),
    SMART_ADAPTIVE("تكيفي ملء العرض (Adaptive)", 60, -1)
}

enum class AdPlacement(val titleAr: String) {
    BOTTOM("أسفل الشاشة (فوق شريط التنقل)"),
    TOP("أعلى الشاشة (تحت شريط العنوان)"),
    IN_FEED("بين عناصر القوائم والبطاقات"),
    POPUP_ON_ACTION("نافذة منبثقة عند العمليات")
}

data class DeveloperAdConfig(
    val isDeveloperModeActive: Boolean = true,
    val isAdsEnabled: Boolean = true,
    val isTestAdsMode: Boolean = true,

    // Ad Types toggles (إظهار أي إعلان أو إغلاقه)
    val isBannerEnabled: Boolean = true,
    val isInterstitialEnabled: Boolean = true,
    val isRewardedEnabled: Boolean = true,
    val isNativeEnabled: Boolean = false,

    // Size & Placement (تحديد حجم الإعلان وأماكنه)
    val bannerSize: AdBannerSize = AdBannerSize.STANDARD,
    val bannerPlacement: AdPlacement = AdPlacement.BOTTOM,

    // Target Sections / Screens (الأقسام التي تظهر بها الإعلانات)
    val enabledScreens: Set<String> = setOf(
        "DASHBOARD", "SALES", "DISTRIBUTORS", "CATEGORIES",
        "SWAP", "VOUCHERS", "FUNDS", "TRANSFERS", "REPORTS"
    ),

    // Ad Unit IDs (حقول إدخال المعرفات)
    val appId: String = "ca-app-pub-3940256099942544~3347511713",
    val customBannerAdUnitId: String = "",
    val customInterstitialAdUnitId: String = "",
    val customRewardedAdUnitId: String = "",
    val customNativeAdUnitId: String = "",

    // Rewards Configuration (إعلانات المكافآت)
    val rewardAmount: Int = 50,
    val rewardType: String = "نقاط ولاء / رصيد تجريبي",
    val lastRewardEarnedTimestamp: Long = 0L,
    val totalRewardsEarnedCount: Int = 0,

    // Frequency and interval controls
    val refreshRateSeconds: Int = 30,
    val interstitialOnSales: Boolean = true,
    val interstitialOnVouchers: Boolean = false
) {
    val effectiveBannerAdUnitId: String
        get() = if (isTestAdsMode) {
            AdMobHelper.TEST_BANNER_AD_UNIT_ID
        } else customBannerAdUnitId.trim()

    val effectiveInterstitialAdUnitId: String
        get() = if (isTestAdsMode) {
            AdMobHelper.TEST_INTERSTITIAL_AD_UNIT_ID
        } else customInterstitialAdUnitId.trim()

    val effectiveRewardedAdUnitId: String
        get() = if (isTestAdsMode) {
            AdMobHelper.TEST_REWARDED_AD_UNIT_ID
        } else customRewardedAdUnitId.trim()

    val effectiveNativeAdUnitId: String
        get() = if (isTestAdsMode) {
            AdMobHelper.TEST_NATIVE_AD_UNIT_ID
        } else customNativeAdUnitId.trim()
}

data class AppUserStats(
    val totalUsersCount: Int = 0,
    val activeUsersCount: Int = 0,
    val adminCount: Int = 0,
    val salesCount: Int = 0,
    val accountantCount: Int = 0,
    val viewerCount: Int = 0,
    val currentFirebaseUserEmail: String? = null,
    val currentFirebaseUserId: String? = null,
    val isCloudConnected: Boolean = false,
    val usersList: List<AppUser> = emptyList()
)
