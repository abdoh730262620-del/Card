package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AdBannerSize
import com.example.data.model.AdPlacement
import com.example.data.model.DeveloperAdConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeveloperSettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("developer_ad_settings", Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<DeveloperAdConfig> = _config.asStateFlow()

    private fun loadConfig(): DeveloperAdConfig {
        val sizeName = prefs.getString("banner_size", AdBannerSize.STANDARD.name) ?: AdBannerSize.STANDARD.name
        val bannerSize = try {
            AdBannerSize.valueOf(sizeName)
        } catch (_: Exception) {
            AdBannerSize.STANDARD
        }

        val placementName = prefs.getString("banner_placement", AdPlacement.BOTTOM.name) ?: AdPlacement.BOTTOM.name
        val bannerPlacement = try {
            AdPlacement.valueOf(placementName)
        } catch (_: Exception) {
            AdPlacement.BOTTOM
        }

        val screensSet = prefs.getStringSet("enabled_screens", null) ?: setOf(
            "DASHBOARD", "SALES", "DISTRIBUTORS", "CATEGORIES",
            "SWAP", "VOUCHERS", "FUNDS", "TRANSFERS", "REPORTS"
        )

        return DeveloperAdConfig(
            isDeveloperModeActive = prefs.getBoolean("dev_mode_active", true),
            isAdsEnabled = prefs.getBoolean("ads_enabled", true),
            isTestAdsMode = prefs.getBoolean("test_ads_mode", true),
            isBannerEnabled = prefs.getBoolean("banner_enabled", true),
            isInterstitialEnabled = prefs.getBoolean("interstitial_enabled", true),
            isRewardedEnabled = prefs.getBoolean("rewarded_enabled", true),
            isNativeEnabled = prefs.getBoolean("native_enabled", false),
            bannerSize = bannerSize,
            bannerPlacement = bannerPlacement,
            enabledScreens = screensSet,
            appId = prefs.getString("app_id", "ca-app-pub-3940256099942544~3347511713") ?: "",
            customBannerAdUnitId = prefs.getString("custom_banner_id", "") ?: "",
            customInterstitialAdUnitId = prefs.getString("custom_interstitial_id", "") ?: "",
            customRewardedAdUnitId = prefs.getString("custom_rewarded_id", "") ?: "",
            customNativeAdUnitId = prefs.getString("custom_native_id", "") ?: "",
            rewardAmount = prefs.getInt("reward_amount", 50),
            rewardType = prefs.getString("reward_type", "نقاط ولاء / رصيد تجريبي") ?: "نقاط ولاء",
            lastRewardEarnedTimestamp = prefs.getLong("last_reward_ts", 0L),
            totalRewardsEarnedCount = prefs.getInt("rewards_earned_count", 0),
            refreshRateSeconds = prefs.getInt("refresh_rate", 30),
            interstitialOnSales = prefs.getBoolean("interstitial_sales", true),
            interstitialOnVouchers = prefs.getBoolean("interstitial_vouchers", false)
        )
    }

    fun updateConfig(newConfig: DeveloperAdConfig) {
        prefs.edit()
            .putBoolean("dev_mode_active", newConfig.isDeveloperModeActive)
            .putBoolean("ads_enabled", newConfig.isAdsEnabled)
            .putBoolean("test_ads_mode", newConfig.isTestAdsMode)
            .putBoolean("banner_enabled", newConfig.isBannerEnabled)
            .putBoolean("interstitial_enabled", newConfig.isInterstitialEnabled)
            .putBoolean("rewarded_enabled", newConfig.isRewardedEnabled)
            .putBoolean("native_enabled", newConfig.isNativeEnabled)
            .putString("banner_size", newConfig.bannerSize.name)
            .putString("banner_placement", newConfig.bannerPlacement.name)
            .putStringSet("enabled_screens", newConfig.enabledScreens)
            .putString("app_id", newConfig.appId)
            .putString("custom_banner_id", newConfig.customBannerAdUnitId)
            .putString("custom_interstitial_id", newConfig.customInterstitialAdUnitId)
            .putString("custom_rewarded_id", newConfig.customRewardedAdUnitId)
            .putString("custom_native_id", newConfig.customNativeAdUnitId)
            .putInt("reward_amount", newConfig.rewardAmount)
            .putString("reward_type", newConfig.rewardType)
            .putLong("last_reward_ts", newConfig.lastRewardEarnedTimestamp)
            .putInt("rewards_earned_count", newConfig.totalRewardsEarnedCount)
            .putInt("refresh_rate", newConfig.refreshRateSeconds)
            .putBoolean("interstitial_sales", newConfig.interstitialOnSales)
            .putBoolean("interstitial_vouchers", newConfig.interstitialOnVouchers)
            .apply()

        _config.value = newConfig
    }

    fun incrementRewardCount() {
        val current = _config.value
        val updated = current.copy(
            totalRewardsEarnedCount = current.totalRewardsEarnedCount + 1,
            lastRewardEarnedTimestamp = System.currentTimeMillis()
        )
        updateConfig(updated)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _config.value = DeveloperAdConfig()
    }
}
