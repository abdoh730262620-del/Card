package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AdBannerSize
import com.example.data.model.AdPlacement
import com.example.ui.AppScreen
import com.example.ui.NetCardsViewModel
import com.example.ui.components.AdBannerView

private val DarkBgColor = Color(0xFF0F172A)
private val CardBgColor = Color(0xFF1E293B)
private val CardBorderColor = Color(0xFF334155)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperSettingsScreen(
    viewModel: NetCardsViewModel,
    onBack: () -> Unit = { viewModel.navigateTo(AppScreen.SETTINGS) }
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val config by viewModel.developerAdConfig.collectAsStateWithLifecycle()
    val userStats by viewModel.userStats.collectAsStateWithLifecycle()

    BackHandler { onBack() }

    var isAdsEnabled by remember(config) { mutableStateOf(config.isAdsEnabled) }
    var isTestAdsMode by remember(config) { mutableStateOf(config.isTestAdsMode) }
    var isBannerEnabled by remember(config) { mutableStateOf(config.isBannerEnabled) }
    var isInterstitialEnabled by remember(config) { mutableStateOf(config.isInterstitialEnabled) }
    var isRewardedEnabled by remember(config) { mutableStateOf(config.isRewardedEnabled) }

    var selectedBannerSize by remember(config) { mutableStateOf(config.bannerSize) }
    var selectedBannerPlacement by remember(config) { mutableStateOf(config.bannerPlacement) }

    var appId by remember(config) { mutableStateOf(config.appId) }
    var customBannerId by remember(config) { mutableStateOf(config.customBannerAdUnitId) }
    var customInterstitialId by remember(config) { mutableStateOf(config.customInterstitialAdUnitId) }
    var customRewardedId by remember(config) { mutableStateOf(config.customRewardedAdUnitId) }

    var rewardType by remember(config) { mutableStateOf(config.rewardType) }
    var rewardAmountStr by remember(config) { mutableStateOf(config.rewardAmount.toString()) }

    var expandedSizeDropdown by remember { mutableStateOf(false) }
    var expandedPlacementDropdown by remember { mutableStateOf(false) }

    val allScreens = listOf(
        "DASHBOARD" to "القائمة الرئيسية",
        "SALES" to "المبيعات والفواتير",
        "DISTRIBUTORS" to "الموزعين",
        "CATEGORIES" to "فئات الكروت",
        "SWAP" to "استبدال الفئات",
        "VOUCHERS" to "سندات القبض والصرف",
        "FUNDS" to "الصناديق والعهد",
        "TRANSFERS" to "تحويلات الموظفين",
        "REPORTS" to "التقارير المالية"
    )

    var enabledScreensSet by remember(config) { mutableStateOf(config.enabledScreens) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
            .testTag("developer_settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Header Top Bar matching AboutAppScreen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF334155),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    

                }


            }

            // Cards List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 1. App Users & Analytics Count Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgColor),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0369A1),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.SupervisedUserCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("إحصائيات مستخدمي التطبيق والنشاط السحابي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatBox("إجمالي المستخدمين", "${userStats.totalUsersCount}", Icons.Default.SupervisedUserCircle)
                                StatBox("المستخدمون النشطون", "${userStats.activeUsersCount}", Icons.Default.CheckCircle)
                                StatBox("المشرفون", "${userStats.adminCount}", Icons.Default.Shield)
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = CardBorderColor)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("حالة السحابة (Firebase Firestore):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color.White)
                                    val cloudStatusText = if (userStats.isCloudConnected) "متصل (${userStats.currentFirebaseUserEmail ?: "مسجل دخول"})" else "غير مسجل دخول سحابياً"
                                    val cloudStatusColor = if (userStats.isCloudConnected) Color(0xFF34D399) else Color(0xFFF87171)
                                    Text(
                                        text = cloudStatusText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = cloudStatusColor
                                    )
                                }
                                if (userStats.currentFirebaseUserId != null) {
                                    Text(
                                        text = "معرف UID: ${userStats.currentFirebaseUserId?.take(8)}...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Global Ad Control & Test Mode
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgColor),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("التحكم العام في الإعلانات ووضع الاختبار", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("تفعيل نظام الإعلانات بالكامل في التطبيق", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("إظهار أو إخفاء كافة الإعلانات في جميع الشاشات", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                                }
                                Switch(
                                    checked = isAdsEnabled,
                                    onCheckedChange = { checked ->
                                        isAdsEnabled = checked
                                        viewModel.setAdsEnabled(checked)
                                    },
                                    modifier = Modifier.testTag("ads_enabled_switch")
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorderColor)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("وضع الإعلانات التجريبية (Test Mode)", fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        text = if (isTestAdsMode) "الإعلانات التجريبية مفعلة حالياً (Google Test Ads)" else "تم إيقاف الإعلانات التجريبية بنجاح",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isTestAdsMode) Color(0xFFFBBF24) else Color(0xFF4ADE80)
                                    )
                                }
                                Switch(
                                    checked = isTestAdsMode,
                                    onCheckedChange = { checked ->
                                        isTestAdsMode = checked
                                        viewModel.setTestAdsMode(checked)
                                    },
                                    modifier = Modifier.testTag("test_ads_switch")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // زر إيقاف / تشغيل الإعلانات التجريبية المباشر
                            Button(
                                onClick = {
                                    val newStatus = !isTestAdsMode
                                    isTestAdsMode = newStatus
                                    viewModel.setTestAdsMode(newStatus)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_toggle_test_ads"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTestAdsMode) Color(0xFFDC2626) else Color(0xFF16A34A)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    if (isTestAdsMode) Icons.Default.Block else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTestAdsMode) "🛑 إيقاف الإعلانات التجريبية الآن" else "✅ تشغيل الإعلانات التجريبية",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 3. Ad Types Toggles
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgColor),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("أنواع الإعلانات (إظهار أو إغلاق كل نوع)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))

                            ToggleRow("إعلانات البانر (Banner Ads)", isBannerEnabled) { isBannerEnabled = it }
                            ToggleRow("إعلانات ملء الشاشة (Interstitial Ads)", isInterstitialEnabled) { isInterstitialEnabled = it }
                            ToggleRow("إعلانات المكافآت (Rewarded Ads)", isRewardedEnabled) { isRewardedEnabled = it }
                        }
                    }
                }

                // 4. Banner Size, Placement & Live Preview
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgColor),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("حجم ومكان إعلانات البانر وتحديد الأماكن", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(14.dp))

                            ExposedDropdownMenuBox(
                                expanded = expandedSizeDropdown,
                                onExpandedChange = { expandedSizeDropdown = !expandedSizeDropdown }
                            ) {
                                OutlinedTextField(
                                    value = selectedBannerSize.titleAr,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("حجم إعلان البانر", color = Color(0xFF94A3B8)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSizeDropdown) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = CardBorderColor
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedSizeDropdown,
                                    onDismissRequest = { expandedSizeDropdown = false }
                                ) {
                                    AdBannerSize.values().forEach { sizeOpt ->
                                        DropdownMenuItem(
                                            text = { Text(sizeOpt.titleAr) },
                                            onClick = {
                                                selectedBannerSize = sizeOpt
                                                expandedSizeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            ExposedDropdownMenuBox(
                                expanded = expandedPlacementDropdown,
                                onExpandedChange = { expandedPlacementDropdown = !expandedPlacementDropdown }
                            ) {
                                OutlinedTextField(
                                    value = selectedBannerPlacement.titleAr,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("مكان عرض البانر في الشاشات", color = Color(0xFF94A3B8)) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPlacementDropdown) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = Color(0xFF38BDF8),
                                        unfocusedBorderColor = CardBorderColor
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedPlacementDropdown,
                                    onDismissRequest = { expandedPlacementDropdown = false }
                                ) {
                                    AdPlacement.values().forEach { placementOpt ->
                                        DropdownMenuItem(
                                            text = { Text(placementOpt.titleAr) },
                                            onClick = {
                                                selectedBannerPlacement = placementOpt
                                                expandedPlacementDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("معاينة حية للبانر بالإعدادات الحالية:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))

                            val previewConfig = config.copy(
                                isAdsEnabled = isAdsEnabled,
                                isBannerEnabled = isBannerEnabled,
                                isTestAdsMode = isTestAdsMode,
                                bannerSize = selectedBannerSize,
                                bannerPlacement = selectedBannerPlacement
                            )
                            AdBannerView(config = previewConfig, forceShow = true)
                        }
                    }
                }

                // 5. Sections Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgColor),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("الأقسام والشاشات التي تظهر بها الإعلانات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("اختر الشاشات التي تسمح بعرض الإعلانات فيها", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                            Spacer(modifier = Modifier.height(12.dp))

                            allScreens.forEach { (screenKey, screenTitle) ->
                                val isChecked = enabledScreensSet.contains(screenKey)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(screenTitle, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                                    Switch(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            enabledScreensSet = if (checked) {
                                                enabledScreensSet + screenKey
                                            } else {
                                                enabledScreensSet - screenKey
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Ad Unit IDs & Reward Customization Fields
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgColor),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("حقول إدخال معرفات الإعلانات (Ad Unit IDs)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))

                            val textFieldColors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = CardBorderColor,
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color(0xFF94A3B8)
                            )

                            OutlinedTextField(
                                value = appId,
                                onValueChange = { appId = it },
                                label = { Text("معرف التطبيق في AdMob (App ID)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = textFieldColors
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = customBannerId,
                                onValueChange = { customBannerId = it },
                                label = { Text("معرف إعلان البانر المخصص (Banner ID)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = textFieldColors
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = customInterstitialId,
                                onValueChange = { customInterstitialId = it },
                                label = { Text("معرف إعلان ملء الشاشة (Interstitial ID)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = textFieldColors
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = customRewardedId,
                                onValueChange = { customRewardedId = it },
                                label = { Text("معرف إعلان المكافأة (Rewarded ID)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = textFieldColors
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = CardBorderColor)
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("إعدادات إعلانات المكافآت (Rewarded Rewards)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = rewardType,
                                    onValueChange = { rewardType = it },
                                    label = { Text("نوع المكافأة (نقاط / رصيد)") },
                                    modifier = Modifier.weight(1.5f),
                                    singleLine = true,
                                    colors = textFieldColors
                                )
                                OutlinedTextField(
                                    value = rewardAmountStr,
                                    onValueChange = { rewardAmountStr = it },
                                    label = { Text("الكمية") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = textFieldColors
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "إجمالي المكافآت التي ربحها المستخدمون حتى الآن: ${config.totalRewardsEarnedCount} مرة",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 7. Test Triggers & Save Actions
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgColor),
                        border = BorderStroke(1.dp, CardBorderColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("اختبار وإدارة الإعلانات الفورية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (activity != null) {
                                            viewModel.triggerInterstitialAd(activity)
                                        } else {
                                            viewModel.showMessage("يرجى فتح الشاشة داخل النشاط الرئيسي")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إظهار إعلان بيني", color = Color.White)
                                }

                                Button(
                                    onClick = {
                                        if (activity != null) {
                                            viewModel.triggerRewardEarned(activity) { amount, type ->
                                                viewModel.showMessage("🎉 مبروك! حصلت على $amount $type كمكافأة إعلان!")
                                            }
                                        } else {
                                            viewModel.showMessage("يرجى فتح الشاشة داخل النشاط الرئيسي")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("تجربة مكافأة 🎁", color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val parsedAmount = rewardAmountStr.toIntOrNull() ?: 50
                                    val updatedConfig = config.copy(
                                        isAdsEnabled = isAdsEnabled,
                                        isTestAdsMode = isTestAdsMode,
                                        isBannerEnabled = isBannerEnabled,
                                        isInterstitialEnabled = isInterstitialEnabled,
                                        isRewardedEnabled = isRewardedEnabled,
                                        bannerSize = selectedBannerSize,
                                        bannerPlacement = selectedBannerPlacement,
                                        enabledScreens = enabledScreensSet,
                                        appId = appId.trim(),
                                        customBannerAdUnitId = customBannerId.trim(),
                                        customInterstitialAdUnitId = customInterstitialId.trim(),
                                        customRewardedAdUnitId = customRewardedId.trim(),
                                        rewardType = rewardType,
                                        rewardAmount = parsedAmount
                                    )
                                    viewModel.updateDeveloperAdConfig(updatedConfig)
                                    viewModel.showMessage("✅ تم حفظ وتطبيق إعدادات وضع المطور بنجاح")
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("حفظ وتطبيق إعدادات وضع المطور والإعلانات", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { viewModel.resetDeveloperAdConfig() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, CardBorderColor),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF94A3B8))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("استعادة الإعدادات الافتراضية", color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun StatBox(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.width(105.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.dp, CardBorderColor)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Text(title, fontSize = 10.sp, color = Color(0xFF94A3B8), maxLines = 1, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2563EB)
            )
        )
    }
}
