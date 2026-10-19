package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NetworkSettings
import com.example.ui.AppScreen
import com.example.ui.NetCardsViewModel
import com.example.util.AuthHelper
import com.example.util.PdfAndShareHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DarkBgColor = Color(0xFF0F172A)
private val CardBgColor = Color(0xFF1E293B)
private val CardBorderColor = Color(0xFF334155)

@Composable
fun SettingsAndBackupScreen(
    viewModel: NetCardsViewModel
) {
    val context = LocalContext.current
    val settingsState by viewModel.settings.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    val settings = settingsState ?: NetworkSettings()

    var showNetworkSettingsDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showCloudSyncDialog by remember { mutableStateOf(false) }

    var networkName by remember(settings) { mutableStateOf(settings.networkName) }
    var managerName by remember(settings) { mutableStateOf(settings.managerName) }
    var managerPhone by remember(settings) { mutableStateOf(settings.managerPhone) }
    var currency by remember(settings) { mutableStateOf(settings.currency) }
    var requirePin by remember(settings) { mutableStateOf(settings.requirePinOnLaunch) }
    var biometricEnabled by remember(settings) { mutableStateOf(settings.biometricEnabled) }
    var cloudSyncEnabled by remember(settings) { mutableStateOf(settings.cloudSyncEnabled) }

    val lastSyncStr = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(settings.lastSyncTimestamp))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
            .testTag("settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {


                // 1. بيانات الشبكة والفواتير العامة
                item {
                    SettingsMenuCard(
                        title = "بيانات الشبكة والفواتير العامة",
                        subtitle = "تعديل اسم الشبكة، المدير، العملة، ورقم التواصل",
                        icon = Icons.Default.Settings,
                        iconContainerColor = Color(0xFF1D4ED8),
                        iconTintColor = Color(0xFF93C5FD),
                        onClick = { showNetworkSettingsDialog = true }
                    )
                }

                // 2. المزامنة السحابية والنسخ الاحتياطي
                item {
                    SettingsMenuCard(
                        title = "المزامنة السحابية والنسخ الاحتياطي",
                        subtitle = "مزامنة سحابية، تصدير نسخة JSON، واستعادة البيانات",
                        icon = Icons.Default.CloudSync,
                        iconContainerColor = Color(0xFF0369A1),
                        iconTintColor = Color(0xFF38BDF8),
                        badgeText = if (cloudSyncEnabled) "مفعل" else "متوقف",
                        badgeContainerColor = if (cloudSyncEnabled) Color(0xFF065F46) else Color(0xFF7F1D1D),
                        badgeTextColor = if (cloudSyncEnabled) Color(0xFF34D399) else Color(0xFFFCA5A5),
                        onClick = { showCloudSyncDialog = true }
                    )
                }

                // 3. إدارة الموظفين والصلاحيات (قسم مستقل داخل الإعدادات)
                item {
                    SettingsMenuCard(
                        title = "إدارة الموظفين والصلاحيات",
                        subtitle = "إضافة موظفين، صلاحيات العمليات، وتقييد رؤية الأرباح",
                        icon = Icons.Default.AdminPanelSettings,
                        iconContainerColor = Color(0xFFC2410C),
                        iconTintColor = Color(0xFFFB923C),
                        onClick = { viewModel.navigateTo(AppScreen.USERS) }
                    )
                }

                // 4. حماية التطبيق والأمان
                item {
                    SettingsMenuCard(
                        title = "حماية التطبيق والأمان",
                        subtitle = "قفل برمز المرور PIN وبصمة الإصبع الحيوية",
                        icon = Icons.Default.Security,
                        iconContainerColor = Color(0xFF047857),
                        iconTintColor = Color(0xFF34D399),
                        onClick = { showSecurityDialog = true }
                    )
                }


                item {
                    SettingsMenuCard(
                        title = "عن البرنامج والمعلومات",
                        subtitle = "حول التطبيق، المميزات، مركز المساعدة، وسياسة الخصوصية",
                        icon = Icons.Default.Info,
                        iconContainerColor = Color(0xFF581C87),
                        iconTintColor = Color(0xFFC084FC),
                        badgeText = "v2.5.0",
                        badgeContainerColor = Color(0xFF3B0764),
                        badgeTextColor = Color(0xFFD8B4FE),
                        onClick = { viewModel.navigateTo(AppScreen.ABOUT) }
                    )
                }

                




                item {
                    SettingsMenuCard(
                        title = "وضع المطور والإعلانات",
                        subtitle = "إدارة إعلانات AdMob، وضع الاختبار، واختبار الإعلانات الفورية",
                        icon = Icons.Default.Code,
                        iconContainerColor = Color(0xFF334155),
                        iconTintColor = Color(0xFF94A3B8),
                        onClick = { viewModel.navigateTo(AppScreen.DEVELOPER) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // --- Dialogs ---

    if (showNetworkSettingsDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showNetworkSettingsDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBgColor)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "بيانات الشبكة والفواتير العامة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showNetworkSettingsDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = networkName,
                        onValueChange = { networkName = it },
                        label = { Text("اسم الشبكة (يظهر في الترويسة والفواتير)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = managerName,
                        onValueChange = { managerName = it },
                        label = { Text("اسم المدير العام") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("العملة (ريال / $)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = managerPhone,
                        onValueChange = { managerPhone = it },
                        label = { Text("رقم هاتف / واتساب الإدارة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    var lowStockStr by remember(settings) { mutableStateOf(settings.lowStockThreshold.toString()) }
                    OutlinedTextField(
                        value = lowStockStr,
                        onValueChange = { lowStockStr = it },
                        label = { Text("حد التنبيه بانخفاض المخزون (عدد الكروت)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val threshold = lowStockStr.toIntOrNull() ?: 20
                            viewModel.updateSettings(
                                settings.copy(
                                    networkName = networkName,
                                    managerName = managerName,
                                    managerPhone = managerPhone,
                                    currency = currency,
                                    lowStockThreshold = threshold
                                )
                            )
                            showNetworkSettingsDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حفظ التغييرات", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCloudSyncDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showCloudSyncDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBgColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "المزامنة السحابية والنسخ الاحتياطي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showCloudSyncDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val coroutineScope = rememberCoroutineScope()
                    val credentialManager = remember { androidx.credentials.CredentialManager.create(context) }
                    var currentFirebaseUser by remember { mutableStateOf<com.google.firebase.auth.FirebaseUser?>(Firebase.auth.currentUser) }

                    // Google Account Status Banner
                    Surface(
                        color = if (currentFirebaseUser != null) Color(0xFF065F46).copy(alpha = 0.35f) else Color(0xFF7F1D1D).copy(alpha = 0.35f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (currentFirebaseUser != null) Color(0xFF059669) else Color(0xFFDC2626)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    if (currentFirebaseUser != null) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (currentFirebaseUser != null) Color(0xFF34D399) else Color(0xFFF87171),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (currentFirebaseUser != null) "متصل بالسحابة عبر Google" else "غير متصل بحساب Google",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (currentFirebaseUser != null) {
                                Text(
                                    text = "الحساب: ${currentFirebaseUser?.email ?: currentFirebaseUser?.uid}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFE2E8F0)
                                )
                                val linkedInfo by viewModel.linkedManagerInfo.collectAsStateWithLifecycle()
                                if (linkedInfo != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1E3A8A).copy(alpha = 0.6f),
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = "🏢 أنت متصل كموظف تابع لشبكة:",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF93C5FD),
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Text(
                                                text = "المدير: ${linkedInfo?.managerName} (${linkedInfo?.managerEmail})",
                                                color = Color.White,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "حسابك: ${linkedInfo?.employeeDisplayName} • الصلاحية: ${linkedInfo?.role}",
                                                color = Color(0xFFE2E8F0),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "✓ المزامنة اللحظية مفعلة تلقائياً لمشاركة البيانات بين أجهزتك والشبكة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF34D399)
                                )
                            } else {
                                Text(
                                    text = "سجل الدخول بحساب Google على جميع أجهزتك لمشاركة ومزامنة نفس البيانات لحظياً.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFCA5A5)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        AuthHelper.onGoogleSignInClicked(
                                            context = context,
                                            credentialManager = credentialManager,
                                            onAuthSuccess = {
                                                currentFirebaseUser = Firebase.auth.currentUser
                                                currentFirebaseUser?.let { u -> viewModel.onGoogleAuthSuccess(u.uid) }
                                            },
                                            onAuthError = { err ->
                                                viewModel.showMessage("فشل الاتصال بـ Google: $err")
                                            },
                                            scope = coroutineScope
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth().height(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                                ) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ربط التطبيق بحساب Google الآن", fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("تفعيل المزامنة السحابية", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("مزامنة البيانات بشكل آلي مع سحابية الخادم", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = cloudSyncEnabled,
                            onCheckedChange = {
                                cloudSyncEnabled = it
                                viewModel.updateSettings(settings.copy(cloudSyncEnabled = it))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "آخر مزامنة ناجحة: $lastSyncStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.syncCloudData() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري المزامنة...")
                        } else {
                            Icon(Icons.Default.CloudSync, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("مزامنة الآن ☁️")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.exportBackup(context) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تصدير نسخة JSON احتياطية", color = Color.White)
                    }

                    if (currentFirebaseUser != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.logoutFromGoogle()
                                showCloudSyncDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171))
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFF87171))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تسجيل الخروج من حساب Google", color = Color(0xFFF87171))
                        }
                    }
                }
            }
        }
    }

    if (showSecurityDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showSecurityDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBgColor)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "حماية التطبيق والأمان",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { showSecurityDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("قفل برمز المرور PIN", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("المطالبة برمز الدخول عند فتح التطبيق", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = requirePin,
                            onCheckedChange = {
                                requirePin = it
                                viewModel.updateSettings(settings.copy(requirePinOnLaunch = it))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("بصمة الإصبع والحماية الحيوية", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("تسجيل الدخول السريع بالبصمة", style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                viewModel.updateSettings(settings.copy(biometricEnabled = it))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.lockApp(); showSecurityDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("قفل الشاشة الآن (تجربة القفل)")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTintColor: Color,
    badgeText: String? = null,
    badgeContainerColor: Color = Color.Transparent,
    badgeTextColor: Color = Color.Transparent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Titles will go here, I need to adjust this.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeContainerColor
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeTextColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = iconContainerColor,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = Color(0xFF64748B)
            )
        }
    }
}
