package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.AdBannerSize
import com.example.data.model.AdPlacement
import com.example.data.model.DeveloperAdConfig
import com.example.ui.AppScreen
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier,
    config: DeveloperAdConfig = DeveloperAdConfig(),
    currentScreen: AppScreen? = null,
    placement: AdPlacement? = null,
    forceShow: Boolean = false
) {
    val isPreview = LocalInspectionMode.current

    // Check if ads or banner ads are globally disabled (unless forced for live preview in Dev screen)
    if (!forceShow) {
        if (!config.isAdsEnabled || !config.isBannerEnabled) {
            return
        }
        // Check if current screen is allowed
        if (currentScreen != null && !config.enabledScreens.contains(currentScreen.name)) {
            return
        }
        // Check placement matching
        if (placement != null && config.bannerPlacement != placement) {
            return
        }
    }

    val bannerHeight = config.bannerSize.heightDp.dp
    val adSize = when (config.bannerSize) {
        AdBannerSize.STANDARD -> AdSize.BANNER
        AdBannerSize.LARGE -> AdSize.LARGE_BANNER
        AdBannerSize.MEDIUM_RECTANGLE -> AdSize.MEDIUM_RECTANGLE
        AdBannerSize.SMART_ADAPTIVE -> AdSize.FULL_BANNER
    }

    val adUnitId = config.effectiveBannerAdUnitId
    if (adUnitId.isBlank()) {
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (config.isTestAdsMode) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "إعلان تجريبي (وضع المطور نشط) • ${config.bannerSize.titleAr}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(4.dp)
                )
                .testTag("admob_banner_box"),
            contentAlignment = Alignment.Center
        ) {
            if (isPreview) {
                Text(
                    text = "مساحة إعلانية Google AdMob (${config.bannerSize.titleAr})",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        AdView(context).apply {
                            setAdSize(adSize)
                            this.adUnitId = adUnitId
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                    update = { view ->
                        // Re-trigger load if unit id changed
                        if (view.adUnitId != adUnitId) {
                            view.adUnitId = adUnitId
                            view.loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
        }
    }
}
