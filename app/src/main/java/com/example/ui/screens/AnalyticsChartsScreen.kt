package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.ui.NetCardsViewModel
import com.example.util.PdfAndShareHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailySalesPoint(
    val dayOfMonth: Int,
    val dayLabel: String,
    val totalAmount: Double,
    val totalCardsCount: Int,
    val transactionsCount: Int
)

data class DistributorPerformanceStat(
    val distributorId: Long,
    val name: String,
    val phone: String,
    val totalCardsBought: Int,
    val totalPurchasesAmount: Double,
    val totalPaymentsMade: Double,
    val currentBalance: Double,
    val onTimePaymentRatio: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsChartsScreen(
    viewModel: NetCardsViewModel
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val distributors by viewModel.distributors.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"
    val networkName = settings?.networkName ?: "شبكة كروت نت"

    // Time Filter: "CURRENT_MONTH", "LAST_30_DAYS", "THIS_WEEK"
    var selectedPeriod by remember { mutableStateOf("CURRENT_MONTH") }
    var selectedDayDetail by remember { mutableStateOf<DailySalesPoint?>(null) }

    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH) // 0-based
    val daysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    val monthNameArabic = SimpleDateFormat("MMMM yyyy", Locale("ar")).format(calendar.time)

    // Filter sales transactions based on period
    val salesTransactions = remember(transactions, selectedPeriod) {
        val cal = Calendar.getInstance()
        transactions.filter { tx ->
            tx.type == TransactionType.SALE && when (selectedPeriod) {
                "CURRENT_MONTH" -> {
                    cal.timeInMillis = tx.date
                    cal.get(Calendar.YEAR) == currentYear && cal.get(Calendar.MONTH) == currentMonth
                }
                "THIS_WEEK" -> {
                    val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                    tx.date >= oneWeekAgo
                }
                else -> { // LAST_30_DAYS
                    val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
                    tx.date >= thirtyDaysAgo
                }
            }
        }
    }

    // Daily distribution map for the month
    val dailySalesData: List<DailySalesPoint> = remember(salesTransactions, daysInCurrentMonth, selectedPeriod) {
        val daysCount = if (selectedPeriod == "CURRENT_MONTH") daysInCurrentMonth else 30
        val cal = Calendar.getInstance()
        val dayMap = mutableMapOf<Int, MutableList<TransactionRecord>>()

        for (d in 1..daysCount) {
            dayMap[d] = mutableListOf()
        }

        salesTransactions.forEach { tx ->
            cal.timeInMillis = tx.date
            val d = cal.get(Calendar.DAY_OF_MONTH)
            dayMap[d]?.add(tx)
        }

        (1..daysCount).map { day ->
            val list = dayMap[day] ?: emptyList()
            DailySalesPoint(
                dayOfMonth = day,
                dayLabel = "$day",
                totalAmount = list.sumOf { it.totalAmount },
                totalCardsCount = list.sumOf { it.quantity },
                transactionsCount = list.size
            )
        }
    }

    // Key Performance Indicators (KPIs)
    val totalPeriodSalesAmount = salesTransactions.sumOf { it.totalAmount }
    val totalPeriodCardsCount = salesTransactions.sumOf { it.quantity }
    val activeDaysCount = dailySalesData.count { it.totalAmount > 0 }.coerceAtLeast(1)
    val averageDailySales = totalPeriodSalesAmount / activeDaysCount
    val peakSalesDay = dailySalesData.maxByOrNull { it.totalAmount }

    // Distributor Performance Ranking
    val distributorStats: List<DistributorPerformanceStat> = remember(distributors, transactions, salesTransactions) {
        distributors.map { dist ->
            val distSales = salesTransactions.filter { it.distributorId == dist.id }
            val distPayments = transactions.filter {
                it.distributorId == dist.id && it.type == TransactionType.PAYMENT_IN
            }
            val cardsBought = distSales.sumOf { it.quantity }
            val purchasesAmount = distSales.sumOf { it.totalAmount }
            val paymentsAmount = distPayments.sumOf { it.paidAmount }

            val ratio = if (purchasesAmount > 0) {
                ((paymentsAmount / purchasesAmount).coerceIn(0.0, 1.0)).toFloat()
            } else 1f

            DistributorPerformanceStat(
                distributorId = dist.id,
                name = dist.name,
                phone = dist.phone,
                totalCardsBought = cardsBought,
                totalPurchasesAmount = purchasesAmount,
                totalPaymentsMade = paymentsAmount,
                currentBalance = dist.currentBalance,
                onTimePaymentRatio = ratio
            )
        }.sortedByDescending { it.totalCardsBought }
    }

    // Category Sales breakdown
    val categorySalesBreakdown = remember(categories, salesTransactions) {
        categories.map { cat ->
            val catSales = salesTransactions.filter { it.categoryId == cat.id }
            val count = catSales.sumOf { it.quantity }
            val revenue = catSales.sumOf { it.totalAmount }
            Triple(cat, count, revenue)
        }.sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("analytics_charts_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Leaderboard,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "إحصائيات ورسوم بيانية للمبيعات",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "مراقبة مبيعات الكروت وأداء الموزعين ($monthNameArabic)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = {
                        val reportText = buildString {
                            appendLine("📊 تقرير أداء مبيعات الكروت - $networkName")
                            appendLine("📅 الفترة: $monthNameArabic")
                            appendLine("━━━━━━━━━━━━━━━━━━━")
                            appendLine("💳 إجمالي الكروت المباعة: $totalPeriodCardsCount كرت")
                            appendLine("💰 إجمالي مبيعات الفترة: ${PdfAndShareHelper.formatCurrency(totalPeriodSalesAmount, currency)}")
                            appendLine("📈 متوسط المبيعات اليومية: ${PdfAndShareHelper.formatCurrency(averageDailySales, currency)}")
                            if (peakSalesDay != null && peakSalesDay.totalAmount > 0) {
                                appendLine("🏆 ذروة المبيعات: يوم ${peakSalesDay.dayOfMonth} بمبلغ ${PdfAndShareHelper.formatCurrency(peakSalesDay.totalAmount, currency)} (${peakSalesDay.totalCardsCount} كرت)")
                            }
                            appendLine("━━━━━━━━━━━━━━━━━━━")
                            appendLine("👥 أعلى الموزعين سحباً للكروت:")
                            distributorStats.take(3).forEachIndexed { i, d ->
                                appendLine("${i + 1}. ${d.name}: ${d.totalCardsBought} كرت (${PdfAndShareHelper.formatCurrency(d.totalPurchasesAmount, currency)})")
                            }
                        }
                        PdfAndShareHelper.shareViaUniversal(context, "تقرير مبيعات الكروت والأداء", reportText)
                    }
                ) {
                    Icon(Icons.Default.Share, contentDescription = "مشاركة التقرير", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Time Period Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedPeriod == "CURRENT_MONTH",
                    onClick = {
                        selectedPeriod = "CURRENT_MONTH"
                        selectedDayDetail = null
                    },
                    label = { Text("الشهر الحالي ($monthNameArabic)", maxLines = 1, softWrap = false) },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = selectedPeriod == "THIS_WEEK",
                    onClick = {
                        selectedPeriod = "THIS_WEEK"
                        selectedDayDetail = null
                    },
                    label = { Text("هذا الأسبوع (آخر 7 أيام)", maxLines = 1, softWrap = false) },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                FilterChip(
                    selected = selectedPeriod == "LAST_30_DAYS",
                    onClick = {
                        selectedPeriod = "LAST_30_DAYS"
                        selectedDayDetail = null
                    },
                    label = { Text("آخر 30 يوماً", maxLines = 1, softWrap = false) },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
        }

        // KPI Summary Cards Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiMetricCard(
                    title = "مبيعات الكروت",
                    value = "$totalPeriodCardsCount كرت",
                    subValue = PdfAndShareHelper.formatCurrency(totalPeriodSalesAmount, currency),
                    icon = Icons.Default.CreditCard,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                KpiMetricCard(
                    title = "متوسط المبيعات اليومي",
                    value = PdfAndShareHelper.formatCurrency(averageDailySales, currency),
                    subValue = "${totalPeriodCardsCount / activeDaysCount} كرت / يوم",
                    icon = Icons.Default.TrendingUp,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Monthly Interactive Sales Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📈 الرسم البياني لمبيعات الشهر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "توزيع حركة سحب الكروت والمبيعات يوماً بيوم (المس أي عمود للتفاصيل)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (peakSalesDay != null && peakSalesDay.totalAmount > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ذروة: يوم ${peakSalesDay.dayOfMonth}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFB45309),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // The Canvas Chart
                    val maxAmount = dailySalesData.maxOfOrNull { it.totalAmount }?.coerceAtLeast(100.0) ?: 100.0
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.tertiary
                    val surfaceVariant = MaterialTheme.colorScheme.outlineVariant

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(dailySalesData) {
                                    detectTapGestures { offset ->
                                        val barWidthTotal = size.width / dailySalesData.size
                                        val clickedIndex = (offset.x / barWidthTotal).toInt().coerceIn(0, dailySalesData.size - 1)
                                        selectedDayDetail = dailySalesData[clickedIndex]
                                    }
                                }
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height - 30.dp.toPx()
                            val barSlotWidth = canvasWidth / dailySalesData.size
                            val barWidth = (barSlotWidth * 0.65f).coerceIn(4f, 24f)

                            // Draw reference horizontal grid lines
                            val gridLines = 3
                            for (i in 1..gridLines) {
                                val y = canvasHeight * (i.toFloat() / gridLines)
                                drawLine(
                                    color = surfaceVariant.copy(alpha = 0.5f),
                                    start = Offset(0f, y),
                                    end = Offset(canvasWidth, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                                )
                            }

                            // Draw bars
                            dailySalesData.forEachIndexed { index, point ->
                                val barHeight = if (maxAmount > 0) {
                                    (point.totalAmount / maxAmount).toFloat() * canvasHeight
                                } else 0f

                                val x = index * barSlotWidth + (barSlotWidth - barWidth) / 2f
                                val y = canvasHeight - barHeight

                                val isSelected = selectedDayDetail?.dayOfMonth == point.dayOfMonth
                                val isPeak = peakSalesDay?.dayOfMonth == point.dayOfMonth && point.totalAmount > 0

                                val brush = when {
                                    isSelected -> Brush.verticalGradient(
                                        colors = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                                    )
                                    isPeak -> Brush.verticalGradient(
                                        colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                    point.totalAmount > 0 -> Brush.verticalGradient(
                                        colors = listOf(primaryColor.copy(alpha = 0.85f), primaryColor)
                                    )
                                    else -> Brush.verticalGradient(
                                        colors = listOf(surfaceVariant.copy(alpha = 0.3f), surfaceVariant.copy(alpha = 0.3f))
                                    )
                                }

                                val finalBarHeight = if (point.totalAmount > 0) barHeight.coerceAtLeast(6.dp.toPx()) else 4.dp.toPx()

                                drawRoundRect(
                                    brush = brush,
                                    topLeft = Offset(x, canvasHeight - finalBarHeight),
                                    size = Size(barWidth, finalBarHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }
                    }

                    // X-Axis Day Numbers Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("5", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("10", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("15", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("20", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("25", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$daysInCurrentMonth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Interactive Tooltip Card when tapping a day
                    AnimatedVisibility(visible = selectedDayDetail != null) {
                        selectedDayDetail?.let { dayPoint ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "تفاصيل يوم ${dayPoint.dayOfMonth} في الشهر:",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "المبيعات: ${PdfAndShareHelper.formatCurrency(dayPoint.totalAmount, currency)} • ${dayPoint.totalCardsCount} كرت (${dayPoint.transactionsCount} عمليات)",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(onClick = { selectedDayDetail = null }, modifier = Modifier.size(28.dp)) {
                                        Text("✕", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Distributor Performance Leaderboard (مراقبة أداء الموزعين)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🏆 أداء وترتيب الموزعين في السحب",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "مراقبة نشاط كل موزع وحجم الكروت المسحوبة ونسبة الالتزام بالسداد",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (distributorStats.isEmpty()) {
                        Text(
                            text = "لا يوجد موزعين مسجلين حالياً",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        val maxCardsBought = distributorStats.maxOfOrNull { it.totalCardsBought }?.coerceAtLeast(1) ?: 1

                        distributorStats.forEachIndexed { index, distStat ->
                            val progress = (distStat.totalCardsBought.toFloat() / maxCardsBought).coerceIn(0f, 1f)

                            val rankMedal = when (index) {
                                0 -> "🥇"
                                1 -> "🥈"
                                2 -> "🥉"
                                else -> "#${index + 1}"
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = rankMedal,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = distStat.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "سحب: ${distStat.totalCardsBought} كرت (${PdfAndShareHelper.formatCurrency(distStat.totalPurchasesAmount, currency)})",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "المتبقي: ${PdfAndShareHelper.formatCurrency(distStat.currentBalance, currency)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (distStat.currentBalance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "سداد: ${PdfAndShareHelper.formatCurrency(distStat.totalPaymentsMade, currency)}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            IconButton(
                                                onClick = {
                                                    val msg = "مرحباً ${distStat.name}، تقرير أدائك لهذا الشهر لدى $networkName:\n" +
                                                            "💳 الكروت المسحوبة: ${distStat.totalCardsBought} كرت\n" +
                                                            "💰 إجمالي المشتريات: ${PdfAndShareHelper.formatCurrency(distStat.totalPurchasesAmount, currency)}\n" +
                                                            "💵 المسدد: ${PdfAndShareHelper.formatCurrency(distStat.totalPaymentsMade, currency)}\n" +
                                                            "📌 الرصيد المتبقي: ${PdfAndShareHelper.formatCurrency(distStat.currentBalance, currency)}\n" +
                                                            "شكراً لتعاملكم معنا!"
                                                    PdfAndShareHelper.shareViaWhatsApp(context, distStat.phone, msg)
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.Send, contentDescription = "إرسال للموزع", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Visual Comparative Progress Bar
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp),
                                        color = when (index) {
                                            0 -> Color(0xFFF59E0B)
                                            1 -> Color(0xFF6B7280)
                                            2 -> Color(0xFFD97706)
                                            else -> MaterialTheme.colorScheme.primary
                                        },
                                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Card Categories Market Share Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "📊 الحصة السوقية لفئات الكروت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "مقارنة الفئات الأكثر مبيعاً ونسبة إقبال العملاء والموزعين عليها",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (categorySalesBreakdown.isEmpty()) {
                        Text("لا توجد فئات كروت مباعة في هذه الفترة", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        val totalCardsSold = categorySalesBreakdown.sumOf { it.second }.coerceAtLeast(1)

                        categorySalesBreakdown.forEach { (cat, count, revenue) ->
                            val pct = (count.toFloat() / totalCardsSold) * 100f

                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "$count كرت (${String.format(Locale.US, "%.1f", pct)}%) • ${PdfAndShareHelper.formatCurrency(revenue, currency)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (pct / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    subValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.bodySmall, color = contentColor, fontWeight = FontWeight.SemiBold)
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
            Text(subValue, style = MaterialTheme.typography.labelSmall, color = contentColor.copy(alpha = 0.8f))
        }
    }
}
