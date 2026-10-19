package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CardCategory
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.ui.AppScreen
import com.example.ui.NetCardsViewModel
import com.example.util.PdfAndShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: NetCardsViewModel,
    onNavigate: (AppScreen) -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val distributors by viewModel.distributors.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"
    val networkName = settings?.networkName ?: "شبكة كروت نت"

    val stats = viewModel.calculateDashboardStats(distributors, categories, transactions)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
            .testTag("dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 2. Alert Banner if Credit Limit Exceeded or Overdue
        if (stats.overCreditLimitDistributorsCount > 0 || stats.overdueDistributorsCount > 0) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(AppScreen.DISTRIBUTORS) }
                        .testTag("credit_alert_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تنبيهات فورية بالديون والائتمان",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            val alertMsg = buildString {
                                if (stats.overCreditLimitDistributorsCount > 0) {
                                    append("هناك ${stats.overCreditLimitDistributorsCount} موزع تجاوز الحد الائتماني المسموح به. ")
                                }
                                if (stats.overdueDistributorsCount > 0) {
                                    append("و ${stats.overdueDistributorsCount} موزع تجاوز فترة التسديد المحددة.")
                                }
                            }
                            Text(
                                text = alertMsg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            text = "عرض",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // 3. Stats Grid (4 Cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "ديون الموزعين بالسوق",
                        value = PdfAndShareHelper.formatCurrency(stats.totalMarketDebt, currency),
                        subtitle = "${distributors.count { it.currentBalance > 0 }} موزع عليهم ديون",
                        icon = Icons.Default.TrendingUp,
                        cardColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                        iconColor = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "كروت المخزن المتوفرة",
                        value = "${stats.totalStockCards} كرت",
                        subtitle = "القيمة: ${PdfAndShareHelper.formatCurrency(stats.totalStockValue, currency)}",
                        icon = Icons.Default.CreditCard,
                        cardColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        iconColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "مبيعات اليوم",
                        value = PdfAndShareHelper.formatCurrency(stats.todaySalesAmount, currency),
                        subtitle = "${stats.todaySalesCount} كرت تم بيعه",
                        icon = Icons.Default.ShoppingCart,
                        cardColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        iconColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "المقبوضات النقدية اليوم",
                        value = PdfAndShareHelper.formatCurrency(stats.todayCollectedCash, currency),
                        subtitle = if (currentUser.canViewProfits) "الربح التقديري: ${PdfAndShareHelper.formatCurrency(stats.todayEstimatedProfit, currency)}" else "الصندوق النقدي",
                        icon = Icons.Default.Payment,
                        cardColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3.5. Main Menu Sections (2 per row)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "الأقسام الرئيسية",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                val sections = listOf(
                    Triple(AppScreen.SALES, "المبيعات", Icons.Filled.ShoppingCart),
                    Triple(AppScreen.PURCHASES, "المشتريات", Icons.Filled.Inventory),
                    Triple(AppScreen.DISTRIBUTORS, "العملاء", Icons.Filled.People),
                    Triple(AppScreen.CATEGORIES, "الكروت", Icons.Filled.CreditCard),
                    Triple(AppScreen.WAREHOUSES, "الموظفين", Icons.Filled.Store),
                    Triple(AppScreen.VOUCHERS, "السندات", Icons.Filled.ReceiptLong),
                    Triple(AppScreen.FUNDS, "الصندوق", Icons.Filled.AccountBalanceWallet),
                    Triple(AppScreen.REPORTS, "التقارير", Icons.Filled.TrendingUp),
                    Triple(AppScreen.SETTINGS, "الإعدادات", Icons.Filled.Settings)
                )

                val colors = mapOf(
                    AppScreen.SALES to (Color(0xFFF1F5F9) to Color(0xFF2563EB)),
                    AppScreen.PURCHASES to (Color(0xFFF0FDF4) to Color(0xFF059669)),
                    AppScreen.DISTRIBUTORS to (Color(0xFFF5F3FF) to Color(0xFF7C3AED)),
                    AppScreen.CATEGORIES to (Color(0xFFFFF7ED) to Color(0xFFEA580C)),
                    AppScreen.WAREHOUSES to (Color(0xFFF0FDFA) to Color(0xFF0D9488)),
                    AppScreen.VOUCHERS to (Color(0xFFFFF1F2) to Color(0xFFE11D48)),
                    AppScreen.FUNDS to (Color(0xFFFFFBEB) to Color(0xFFD97706)),
                    AppScreen.REPORTS to (Color(0xFFF1F5F9) to Color(0xFF475569)),
                    AppScreen.USERS to (Color(0xFFFDF2F8) to Color(0xFFDB2777)),
                    AppScreen.SETTINGS to (Color(0xFFF1F5F9) to Color(0xFF475569))
                )

                sections.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { (screen, title, icon) ->
                            val (_, tintColor) = colors[screen] ?: (MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.primary)
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigate(screen) }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = tintColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(65.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = tintColor,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }



    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    cardColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
            .width(76.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor.copy(alpha = 0.18f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TransactionFeedItem(
    tx: TransactionRecord,
    currency: String
) {
    val dateStr = SimpleDateFormat("MM/dd hh:mm a", Locale("ar")).format(Date(tx.date))
    val (typeTitle, typeColor) = when (tx.type) {
        TransactionType.SALE -> "بيع كروت" to MaterialTheme.colorScheme.primary
        TransactionType.PAYMENT_IN -> "سند قبض" to Color(0xFF16A34A)
        TransactionType.PURCHASE -> "شراء مخزن" to Color(0xFF2563EB)
        TransactionType.RETURN -> "مرتجع كروت" to Color(0xFFDC2626)
        TransactionType.SALE_RETURN -> "مرتجع مبيعات" to Color(0xFFDC2626)
        TransactionType.PURCHASE_RETURN -> "مرتجع مشتريات" to Color(0xFFEA580C)
        TransactionType.PAYMENT_OUT -> "سند صرف" to Color(0xFFEA580C)
        TransactionType.DISCOUNT -> "خصم مالي" to Color(0xFF7C3AED)
        TransactionType.SWAP -> "استبدال فئات" to Color(0xFF0D9488)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = typeColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = typeTitle.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tx.distributorName.ifBlank { "عملية داخلية" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "#${tx.invoiceNumber} • $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = PdfAndShareHelper.formatCurrency(tx.totalAmount, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                if (tx.remainingAmount > 0) {
                    Text(
                        text = "آجل: ${PdfAndShareHelper.formatCurrency(tx.remainingAmount, currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
