package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import com.example.ui.components.NotificationsDialog
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.NetCardsViewModel
import com.example.ui.components.AdBannerView
import com.example.ui.components.PasscodeLockDialog
import com.example.ui.screens.AboutAppScreen
import com.example.ui.screens.AnalyticsChartsScreen
import com.example.ui.screens.CardCategoriesScreen
import com.example.ui.screens.CardSwapScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DistributorsScreen
import com.example.ui.screens.EmployeeTransfersScreen
import com.example.ui.screens.FundsScreen
import com.example.ui.screens.PermissionsAndUsersScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesAndInvoicingScreen
import com.example.ui.screens.SettingsAndBackupScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.VouchersScreen
import com.example.ui.screens.WarehousesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AdMobHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

internal fun FirebaseAuth.authStateFlow(): kotlinx.coroutines.flow.Flow<com.google.firebase.auth.FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : androidx.fragment.app.FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdMobHelper.initialize(this)
        enableEdgeToEdge()

        // Schedule WorkManager background sync when network is connected
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()

        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.worker.SyncWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "NetCardsBackgroundSync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        setContent {
            MyApplicationTheme {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
                ) {
                    val viewModel: NetCardsViewModel = viewModel()
                    val authUser by remember { Firebase.auth.authStateFlow() }
                        .collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)
                    var isLocalLoggedIn by remember { mutableStateOf(false) }
                    val isLoggedIn = authUser != null || isLocalLoggedIn

                    LaunchedEffect(authUser) {
                        if (authUser != null) {
                            viewModel.onGoogleAuthSuccess(authUser!!.uid)
                        }
                    }

                    if (!isLoggedIn) {
                        SignInScreen(
                            onSignInSuccess = { _, _ ->
                                isLocalLoggedIn = true
                            },
                            onGoogleSignInSuccess = { uid ->
                                viewModel.onGoogleAuthSuccess(uid)
                            },
                            onBiometricSignIn = {
                                com.example.util.BiometricAuthHelper.authenticate(
                                    activity = this@MainActivity,
                                    title = "تسجيل الدخول بالبصمة",
                                    subtitle = "تأكيد الهوية للوصول إلى نظام كروت نت",
                                    negativeButtonText = "إلغاء / استخدام الحساب",
                                    onSuccess = {
                                        isLocalLoggedIn = true
                                        viewModel.showMessage("تم تسجيل الدخول بنجاح عبر البصمة")
                                    },
                                    onError = { errorMsg ->
                                        viewModel.showMessage(errorMsg)
                                    }
                                )
                            }
                        )
                    } else {
                        NetCardsApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetCardsApp(viewModel: NetCardsViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val devAdConfig by viewModel.developerAdConfig.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    // Back handling: close drawer first if open, else return to Dashboard, or show exit dialog if on Dashboard
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    BackHandler(enabled = !drawerState.isOpen && currentScreen != AppScreen.DASHBOARD) {
        viewModel.navigateBack()
    }
    BackHandler(enabled = !drawerState.isOpen && currentScreen == AppScreen.DASHBOARD) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "تأكيد الخروج",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من رغبتك في الخروج من التطبيق؟",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        (context as? ComponentActivity)?.finish()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("خروج")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showExitDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إلغاء")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_app_logo),
                                contentDescription = "App Logo",
                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Column {
                            Text(
                                text = currentScreen.titleAr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${settings?.networkName ?: "شبكة كروت نت"} • ${currentUser.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (currentScreen != AppScreen.DASHBOARD) {
                        IconButton(
                            onClick = {
                                viewModel.navigateBack()
                            },
                            modifier = Modifier.testTag("back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    } else {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isOpen) drawerState.close() else drawerState.open()
                                }
                            },
                            modifier = Modifier.testTag("menu_drawer_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "القائمة الرئيسية")
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.syncCloudData() },
                        modifier = Modifier.testTag("cloud_sync_button")
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = "مزامنة سحابية")
                    }
                    IconButton(
                        onClick = { showNotificationsDialog = true },
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (unreadNotificationsCount > 99) "99+" else unreadNotificationsCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "الإشعارات")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.lockApp() },
                        modifier = Modifier.testTag("lock_app_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = "قفل التطبيق")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel, onNavigate = { viewModel.navigateTo(it) })
                    AppScreen.SALES -> SalesAndInvoicingScreen(viewModel = viewModel)
                    AppScreen.PURCHASES -> PurchasesScreen(viewModel = viewModel)
                    AppScreen.DISTRIBUTORS -> DistributorsScreen(viewModel = viewModel)
                    AppScreen.CATEGORIES -> CardCategoriesScreen(viewModel = viewModel)
                    AppScreen.SWAP -> WarehousesScreen(viewModel = viewModel, initialTab = 2)
                    AppScreen.VOUCHERS -> VouchersScreen(viewModel = viewModel)
                    AppScreen.VOUCHERS_PAYMENT_IN, AppScreen.VOUCHERS_PAYMENT_OUT, AppScreen.VOUCHERS_DISCOUNT, AppScreen.VOUCHERS_RETURNS -> VouchersScreen(viewModel = viewModel)
                    AppScreen.FUNDS -> FundsScreen(viewModel = viewModel)
                    AppScreen.TRANSFERS -> WarehousesScreen(viewModel = viewModel, initialTab = 1)
                    AppScreen.WAREHOUSES -> WarehousesScreen(viewModel = viewModel, initialTab = 0)
                    AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                    AppScreen.ANALYTICS -> AnalyticsChartsScreen(viewModel = viewModel)
                    AppScreen.USERS -> PermissionsAndUsersScreen(viewModel = viewModel)
                    AppScreen.SETTINGS -> SettingsAndBackupScreen(viewModel = viewModel)
                    AppScreen.DEVELOPER -> com.example.ui.screens.DeveloperSettingsScreen(viewModel = viewModel)
                    AppScreen.ABOUT -> AboutAppScreen(onBack = { viewModel.navigateTo(AppScreen.SETTINGS) })
                }
        }
    }

    if (isAppLocked) {
        val context = LocalContext.current
        val activity = context as? androidx.fragment.app.FragmentActivity
        PasscodeLockDialog(
            onUnlock = { pin -> viewModel.unlockApp(pin) },
            onBiometricUnlock = {
                if (activity != null) {
                    com.example.util.BiometricAuthHelper.authenticate(
                        activity = activity,
                        title = "فتح التطبيق بالبصمة",
                        subtitle = "تأكيد البصمة للوصول إلى نظام كروت نت",
                        negativeButtonText = "إلغاء / استخدام PIN",
                        onSuccess = {
                            viewModel.unlockAppWithBiometric()
                        },
                        onError = { errorMsg ->
                            viewModel.showMessage(errorMsg)
                        }
                    )
                } else {
                    viewModel.showMessage("خدمة البصمة غير متاحة")
                }
            }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            viewModel = viewModel,
            onDismiss = { showNotificationsDialog = false }
        )
    }
}

fun AppScreen.getIconColors(): Pair<Color, Color> {
    return when (this) {
        AppScreen.DASHBOARD -> Pair(Color(0xFF1D4ED8).copy(alpha = 0.2f), Color(0xFF3B82F6))
        AppScreen.SALES -> Pair(Color(0xFF047857).copy(alpha = 0.2f), Color(0xFF10B981))
        AppScreen.PURCHASES -> Pair(Color(0xFF0F766E).copy(alpha = 0.2f), Color(0xFF14B8A6))
        AppScreen.DISTRIBUTORS -> Pair(Color(0xFFB45309).copy(alpha = 0.2f), Color(0xFFF59E0B))
        AppScreen.CATEGORIES -> Pair(Color(0xFF6D28D9).copy(alpha = 0.2f), Color(0xFF8B5CF6))
        AppScreen.SWAP -> Pair(Color(0xFF0E7490).copy(alpha = 0.2f), Color(0xFF06B6D4))
        AppScreen.VOUCHERS -> Pair(Color(0xFF9F1239).copy(alpha = 0.2f), Color(0xFFF43F5E))
        AppScreen.VOUCHERS_PAYMENT_IN, AppScreen.VOUCHERS_PAYMENT_OUT, AppScreen.VOUCHERS_DISCOUNT, AppScreen.VOUCHERS_RETURNS -> Pair(Color(0xFF9F1239).copy(alpha = 0.2f), Color(0xFFF43F5E))
        AppScreen.FUNDS -> Pair(Color(0xFF3730A3).copy(alpha = 0.2f), Color(0xFF6366F1))
        AppScreen.TRANSFERS -> Pair(Color(0xFFC2410C).copy(alpha = 0.2f), Color(0xFFEA580C))
        AppScreen.WAREHOUSES -> Pair(Color(0xFF0D9488).copy(alpha = 0.2f), Color(0xFF14B8A6))
        AppScreen.REPORTS -> Pair(Color(0xFF0369A1).copy(alpha = 0.2f), Color(0xFF0EA5E9))
        AppScreen.ANALYTICS -> Pair(Color(0xFF581C87).copy(alpha = 0.2f), Color(0xFFA855F7))
        AppScreen.USERS -> Pair(Color(0xFF991B1B).copy(alpha = 0.2f), Color(0xFFEF4444))
        AppScreen.SETTINGS -> Pair(Color(0xFF1E40AF).copy(alpha = 0.2f), Color(0xFF3B82F6))
        AppScreen.DEVELOPER -> Pair(Color(0xFF334155).copy(alpha = 0.2f), Color(0xFF64748B))
        AppScreen.ABOUT -> Pair(Color(0xFF7E22CE).copy(alpha = 0.2f), Color(0xFFC084FC))
    }
}
