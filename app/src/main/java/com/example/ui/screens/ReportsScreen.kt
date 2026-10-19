package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.ui.NetCardsViewModel
import com.example.util.PdfAndShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ReportType(val title: String, val subtitle: String, val icon: ImageVector, val color: Color) {
    SALES("المبيعات", "تقارير مبيعات الكروت والإيرادات", Icons.Default.ShoppingCart, Color(0xFF16A34A)),
    DISTRIBUTOR_DEBTS("ديون الموزعين", "أرصدة الموزعين والحدود الائتمانية", Icons.Default.AccountBalanceWallet, Color(0xFFEA580C)),
    PROFITS("الأرباح", "صافي الأرباح والخصومات التقديرية", Icons.Default.TrendingUp, Color(0xFF7C3AED)),
    TREASURY("الصندوق", "حركة النقدية والمقبوضات والمصروفات", Icons.Default.AttachMoney, Color(0xFF2563EB)),
    EMPLOYEES("الموظفين", "صلاحيات وحركة الموظفين", Icons.Default.People, Color(0xFFDB2777)),
    PURCHASES("المشتريات", "فواتير ومخزون مشتريات الكروت", Icons.Default.Inventory, Color(0xFF0891B2)),
    VOUCHERS("السندات", "سندات القبض والصرف", Icons.Default.ReceiptLong, Color(0xFFD97706)),
    TRANSFERS("التحويلات", "تحويلات العهد والستوك بين الموظفين", Icons.Default.SwapHoriz, Color(0xFF6D28D9)),
    SWAPS("الاستبدال", "عمليات استبدال الكروت وفروقاتها", Icons.Default.History, Color(0xFF4F46E5))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: NetCardsViewModel
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val distributors by viewModel.distributors.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val employeeTransfers by viewModel.employeeTransfers.collectAsStateWithLifecycle()
    val cardSwaps by viewModel.cardSwaps.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"
    val networkName = settings?.networkName ?: "شبكة كروت نت"

    var timeFilter by remember { mutableStateOf("ALL") } // ALL, TODAY, WEEK, MONTH
    var activeReportScreen by remember { mutableStateOf<ReportType?>(null) } // null = menu list, else = detail screen

    var previewDetailsText by remember { mutableStateOf<String?>(null) }
    var previewDetailsTitle by remember { mutableStateOf<String>("معاينة الكشف") }

    val availableReports = remember(currentUser.canViewProfits) {
        buildList {
            add(ReportType.SALES)
            add(ReportType.DISTRIBUTOR_DEBTS)
            if (currentUser.canViewProfits) add(ReportType.PROFITS)
            add(ReportType.TREASURY)
            add(ReportType.EMPLOYEES)
            add(ReportType.PURCHASES)
            add(ReportType.VOUCHERS)
            add(ReportType.TRANSFERS)
            add(ReportType.SWAPS)
        }
    }

    val now = System.currentTimeMillis()
    val dayMs = 24 * 60 * 60 * 1000L

    val filteredTransactions = transactions.filter { tx ->
        when (timeFilter) {
            "TODAY" -> (now - tx.date) <= dayMs
            "WEEK" -> (now - tx.date) <= (7 * dayMs)
            "MONTH" -> (now - tx.date) <= (30 * dayMs)
            else -> true
        }
    }

    val totalSales = filteredTransactions.filter { it.type == TransactionType.SALE }.sumOf { it.totalAmount }
    val totalCardsSold = filteredTransactions.filter { it.type == TransactionType.SALE }.sumOf { it.quantity }
    val totalCashReceived = filteredTransactions.filter { it.type == TransactionType.PAYMENT_IN }.sumOf { it.paidAmount } +
            filteredTransactions.filter { it.type == TransactionType.SALE }.sumOf { it.paidAmount }
    val totalExpenses = filteredTransactions.filter { it.type == TransactionType.PAYMENT_OUT }.sumOf { it.totalAmount }
    val totalDiscounts = filteredTransactions.filter { it.type == TransactionType.DISCOUNT }.sumOf { it.totalAmount }

    // Profit calculation
    var estimatedProfit = 0.0
    for (tx in filteredTransactions.filter { it.type == TransactionType.SALE }) {
        val cat = categories.find { it.id == tx.categoryId }
        if (cat != null) {
            val margin = (tx.unitPrice - cat.costPrice).coerceAtLeast(0.0)
            estimatedProfit += (margin * tx.quantity) - tx.discount
        }
    }

    // Preview Dialog
    if (previewDetailsText != null) {
        AlertDialog(
            onDismissRequest = { previewDetailsText = null },
            title = { Text(previewDetailsTitle, fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Text(
                                text = previewDetailsText ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            PdfAndShareHelper.shareViaWhatsApp(context, "", previewDetailsText ?: "")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Text("واتساب", color = Color.White)
                    }
                    Button(onClick = { previewDetailsText = null }) {
                        Text("إغلاق")
                    }
                }
            }
        )
    }

    // If a specific report is opened
    if (activeReportScreen != null) {
        val reportType = activeReportScreen!!
        val reportTitle = when (reportType) {
            ReportType.SALES -> "تقرير مبيعات كروت الشبكة"
            ReportType.DISTRIBUTOR_DEBTS -> "تقرير ديون الموزعين والحدود الائتمانية"
            ReportType.PROFITS -> "تقرير الأرباح التقديرية"
            ReportType.TREASURY -> "تقرير حركة الصندوق والمقبوضات"
            ReportType.EMPLOYEES -> "تقرير الموظفين والصلاحيات"
            ReportType.PURCHASES -> "تقرير مشتريات الكروت"
            ReportType.VOUCHERS -> "تقرير سندات القبض والصرف"
            ReportType.TRANSFERS -> "تقرير تحويلات الموظفين والعهد"
            ReportType.SWAPS -> "تقرير استبدال الكروت"
        }

        fun generateReportText(): String {
            val timeLabel = when (timeFilter) {
                "TODAY" -> "اليوم"
                "WEEK" -> "آخر 7 أيام"
                "MONTH" -> "هذا الشهر"
                else -> "كافة الفترات"
            }
            return buildString {
                appendLine("📊 *$reportTitle - $networkName*")
                appendLine("📅 الفترة: $timeLabel | تاريخ التقرير: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date())}")
                appendLine("━━━━━━━━━━━━━━━━━━━")
                when (reportType) {
                    ReportType.SALES -> {
                        appendLine("• إجمالي مبلغ المبيعات: ${PdfAndShareHelper.formatCurrency(totalSales, currency)}")
                        appendLine("• إجمالي عدد الكروت المباعة: $totalCardsSold كرت")
                        appendLine("• إجمالي المدفوع نقداً: ${PdfAndShareHelper.formatCurrency(totalCashReceived, currency)}")
                        appendLine("• إجمالي المتبقي آجل: ${PdfAndShareHelper.formatCurrency(totalSales - totalCashReceived, currency)}")
                    }
                    ReportType.DISTRIBUTOR_DEBTS -> {
                        val totalDebt = distributors.sumOf { it.currentBalance.coerceAtLeast(0.0) }
                        appendLine("• إجمالي الديون في السوق: ${PdfAndShareHelper.formatCurrency(totalDebt, currency)}")
                        appendLine("• عدد الموزعين المدينين: ${distributors.count { it.currentBalance > 0 }}")
                        appendLine("• الموزعين المتجاوزين للحد الائتماني: ${distributors.count { it.isCreditLimitExceeded }}")
                        appendLine("───────────────────")
                        distributors.filter { it.currentBalance > 0 }.forEach { d ->
                            appendLine("- ${d.name}: ${PdfAndShareHelper.formatCurrency(d.currentBalance, currency)} (حد: ${PdfAndShareHelper.formatCurrency(d.creditLimit, currency)})")
                        }
                    }
                    ReportType.PROFITS -> {
                        appendLine("• إجمالي المبيعات: ${PdfAndShareHelper.formatCurrency(totalSales, currency)}")
                        appendLine("• الربح الصافي المقدر: ${PdfAndShareHelper.formatCurrency(estimatedProfit, currency)}")
                        appendLine("• الخصومات الممنوحة: ${PdfAndShareHelper.formatCurrency(totalDiscounts, currency)}")
                    }
                    ReportType.TREASURY -> {
                        appendLine("• إجمالي المقبوضات النقدية: ${PdfAndShareHelper.formatCurrency(totalCashReceived, currency)}")
                        appendLine("• إجمالي سندات الصرف: ${PdfAndShareHelper.formatCurrency(totalExpenses, currency)}")
                        appendLine("• صافي السيولة النقدية: ${PdfAndShareHelper.formatCurrency(totalCashReceived - totalExpenses, currency)}")
                    }
                    ReportType.EMPLOYEES -> {
                        appendLine("• إجمالي عدد الموظفين: ${users.size}")
                        appendLine("• الموظفين النشطين: ${users.count { it.isActive }}")
                        appendLine("───────────────────")
                        users.forEach { u ->
                            appendLine("- ${u.displayName} (${u.role.titleAr})")
                        }
                    }
                    ReportType.PURCHASES -> {
                        val purTxs = filteredTransactions.filter { it.type == TransactionType.PURCHASE }
                        val totalPurAmount = purTxs.sumOf { it.totalAmount }
                        val totalPurCards = purTxs.sumOf { it.quantity }
                        appendLine("• إجمالي قيمة المشتريات: ${PdfAndShareHelper.formatCurrency(totalPurAmount, currency)}")
                        appendLine("• إجمالي الكروت المشتراة: $totalPurCards كرت")
                        appendLine("• عدد فواتير الشراء: ${purTxs.size}")
                    }
                    ReportType.VOUCHERS -> {
                        val payInTxs = filteredTransactions.filter { it.type == TransactionType.PAYMENT_IN }
                        val payOutTxs = filteredTransactions.filter { it.type == TransactionType.PAYMENT_OUT }
                        val totalIn = payInTxs.sumOf { it.paidAmount }
                        val totalOut = payOutTxs.sumOf { it.totalAmount }
                        appendLine("• إجمالي سندات القبض: ${PdfAndShareHelper.formatCurrency(totalIn, currency)} (${payInTxs.size} سند)")
                        appendLine("• إجمالي سندات الصرف: ${PdfAndShareHelper.formatCurrency(totalOut, currency)} (${payOutTxs.size} سند)")
                    }
                    ReportType.TRANSFERS -> {
                        val trans = employeeTransfers.filter { tx ->
                            when (timeFilter) {
                                "TODAY" -> (now - tx.date) <= dayMs
                                "WEEK" -> (now - tx.date) <= (7 * dayMs)
                                "MONTH" -> (now - tx.date) <= (30 * dayMs)
                                else -> true
                            }
                        }
                        appendLine("• إجمالي عدد التحويلات: ${trans.size}")
                        appendLine("• عهد الكروت المحولة: ${trans.filter { it.transferType == "CARDS" }.sumOf { it.quantity }} كرت")
                        appendLine("• العهد النقدية المحولة: ${PdfAndShareHelper.formatCurrency(trans.filter { it.transferType == "CASH" }.sumOf { it.cashAmount }, currency)}")
                    }
                    ReportType.SWAPS -> {
                        val swaps = cardSwaps.filter { s ->
                            when (timeFilter) {
                                "TODAY" -> (now - s.date) <= dayMs
                                "WEEK" -> (now - s.date) <= (7 * dayMs)
                                "MONTH" -> (now - s.date) <= (30 * dayMs)
                                else -> true
                            }
                        }
                        appendLine("• إجمالي عمليات الاستبدال: ${swaps.size}")
                        appendLine("• إجمالي الفروقات المالية: ${PdfAndShareHelper.formatCurrency(swaps.sumOf { it.differenceAmount }, currency)}")
                    }
                }
                appendLine("━━━━━━━━━━━━━━━━━━━")
                appendLine("تم الإصدار بواسطة نظام كروت نت")
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Back & Title Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { activeReportScreen = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                    Text(
                        text = reportTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Time Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = timeFilter == "ALL",
                        onClick = { timeFilter = "ALL" },
                        label = { Text("كافة الفترات") }
                    )
                    FilterChip(
                        selected = timeFilter == "TODAY",
                        onClick = { timeFilter = "TODAY" },
                        label = { Text("اليوم") }
                    )
                    FilterChip(
                        selected = timeFilter == "WEEK",
                        onClick = { timeFilter = "WEEK" },
                        label = { Text("آخر 7 أيام") }
                    )
                    FilterChip(
                        selected = timeFilter == "MONTH",
                        onClick = { timeFilter = "MONTH" },
                        label = { Text("هذا الشهر") }
                    )
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        when (reportType) {
                            ReportType.SALES -> {
                                Text("ملخص مبيعات الكروت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportSummaryRow("إجمالي المبيعات", PdfAndShareHelper.formatCurrency(totalSales, currency), isBold = true, color = MaterialTheme.colorScheme.primary)
                                ReportSummaryRow("عدد الكروت المباعة", "$totalCardsSold كرت")
                                ReportSummaryRow("المحصل نقداً", PdfAndShareHelper.formatCurrency(totalCashReceived, currency))
                                ReportSummaryRow("المتبقي آجل على الموزعين", PdfAndShareHelper.formatCurrency((totalSales - totalCashReceived).coerceAtLeast(0.0), currency), color = MaterialTheme.colorScheme.error)
                            }
                            ReportType.DISTRIBUTOR_DEBTS -> {
                                val totalDebt = distributors.sumOf { it.currentBalance.coerceAtLeast(0.0) }
                                Text("تقرير أرصدة وديون الموزعين بالسوق", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportSummaryRow("إجمالي الديون في السوق", PdfAndShareHelper.formatCurrency(totalDebt, currency), isBold = true, color = MaterialTheme.colorScheme.error)
                                ReportSummaryRow("عدد الموزعين المدينين", "${distributors.count { it.currentBalance > 0 }} موزع")
                                ReportSummaryRow("المتجاوزين للحد الائتماني", "${distributors.count { it.isCreditLimitExceeded }} موزع", color = MaterialTheme.colorScheme.error)
                                ReportSummaryRow("المتأخرين عن فترة التسديد", "${distributors.count { it.isPaymentOverdue() }} موزع", color = MaterialTheme.colorScheme.error)
                            }
                            ReportType.PROFITS -> {
                                Text("تقرير الأرباح التقديرية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportSummaryRow("الربح الصافي المقدر", PdfAndShareHelper.formatCurrency(estimatedProfit, currency), isBold = true, color = Color(0xFF16A34A))
                                ReportSummaryRow("إجمالي المبيعات", PdfAndShareHelper.formatCurrency(totalSales, currency))
                                ReportSummaryRow("إجمالي الخصومات الممنوحة", PdfAndShareHelper.formatCurrency(totalDiscounts, currency))
                            }
                            ReportType.TREASURY -> {
                                Text("تقرير حركة النقدية والصندوق", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportSummaryRow("إجمالي المقبوضات النقدية", PdfAndShareHelper.formatCurrency(totalCashReceived, currency), color = Color(0xFF16A34A))
                                ReportSummaryRow("إجمالي سندات الصرف", PdfAndShareHelper.formatCurrency(totalExpenses, currency), color = MaterialTheme.colorScheme.error)
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                ReportSummaryRow("صافي السيولة النقدية المتوفرة", PdfAndShareHelper.formatCurrency(totalCashReceived - totalExpenses, currency), isBold = true, color = MaterialTheme.colorScheme.primary)
                            }
                            ReportType.EMPLOYEES -> {
                                Text("تقرير الموظفين وصلاحيات النظام", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportSummaryRow("إجمالي عدد الموظفين", "${users.size} موزع/موظف")
                                ReportSummaryRow("الموظفين النشطين", "${users.count { it.isActive }} نشط")
                            }
                            ReportType.PURCHASES -> {
                                val purTxs = filteredTransactions.filter { it.type == TransactionType.PURCHASE }
                                val totalPurAmount = purTxs.sumOf { it.totalAmount }
                                val totalPurCards = purTxs.sumOf { it.quantity }
                                Text("تقرير مشتريات الكروت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportSummaryRow("إجمالي قيمة المشتريات", PdfAndShareHelper.formatCurrency(totalPurAmount, currency), isBold = true, color = MaterialTheme.colorScheme.primary)
                                ReportSummaryRow("إجمالي الكروت المشتراة", "$totalPurCards كرت")
                                ReportSummaryRow("عدد فواتير الشراء", "${purTxs.size} فاتورة")
                            }
                            ReportType.VOUCHERS -> {
                                val payInTxs = filteredTransactions.filter { it.type == TransactionType.PAYMENT_IN }
                                val payOutTxs = filteredTransactions.filter { it.type == TransactionType.PAYMENT_OUT }
                                val totalIn = payInTxs.sumOf { it.paidAmount }
                                val totalOut = payOutTxs.sumOf { it.totalAmount }
                                Text("تقرير سندات القبض والصرف", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportSummaryRow("إجمالي سندات القبض", PdfAndShareHelper.formatCurrency(totalIn, currency), color = Color(0xFF16A34A))
                                ReportSummaryRow("عدد سندات القبض", "${payInTxs.size} سند")
                                Spacer(modifier = Modifier.height(4.dp))
                                ReportSummaryRow("إجمالي سندات الصرف", PdfAndShareHelper.formatCurrency(totalOut, currency), color = MaterialTheme.colorScheme.error)
                                ReportSummaryRow("عدد سندات الصرف", "${payOutTxs.size} سند")
                            }
                            ReportType.TRANSFERS -> {
                                val trans = employeeTransfers.filter { tx ->
                                    when (timeFilter) {
                                        "TODAY" -> (now - tx.date) <= dayMs
                                        "WEEK" -> (now - tx.date) <= (7 * dayMs)
                                        "MONTH" -> (now - tx.date) <= (30 * dayMs)
                                        else -> true
                                    }
                                }
                                Text("تقرير تحويلات الموظفين والعهد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportSummaryRow("إجمالي عدد التحويلات", "${trans.size} عملية")
                                ReportSummaryRow("عهد الكروت المحولة", "${trans.filter { it.transferType == "CARDS" }.sumOf { it.quantity }} كرت")
                                ReportSummaryRow("العهد النقدية المحولة", PdfAndShareHelper.formatCurrency(trans.filter { it.transferType == "CASH" }.sumOf { it.cashAmount }, currency))
                            }
                            ReportType.SWAPS -> {
                                val swaps = cardSwaps.filter { s ->
                                    when (timeFilter) {
                                        "TODAY" -> (now - s.date) <= dayMs
                                        "WEEK" -> (now - s.date) <= (7 * dayMs)
                                        "MONTH" -> (now - s.date) <= (30 * dayMs)
                                        else -> true
                                    }
                                }
                                Text("تقرير استبدال الكروت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportSummaryRow("إجمالي عمليات الاستبدال", "${swaps.size} عملية")
                                ReportSummaryRow("إجمالي الفروقات المالية", PdfAndShareHelper.formatCurrency(swaps.sumOf { it.differenceAmount }, currency), color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Share Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val text = generateReportText()
                                    PdfAndShareHelper.shareViaWhatsApp(context, "", text)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إرسال واتساب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val text = generateReportText()
                                    PdfAndShareHelper.shareViaUniversal(context, reportTitle, text)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مشاركة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Detailed List header
            item {
                Text(
                    text = "📋 الكشف التفصيلي والسجلات (اضغط على أي بند للمعاينه)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Records List
            when (reportType) {
                ReportType.SALES -> {
                    val salesList = filteredTransactions.filter { it.type == TransactionType.SALE }
                    if (salesList.isEmpty()) {
                        item { Text("لا توجد مبيعات في هذه الفترة", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(salesList) { tx ->
                            ReportItemCard(
                                title = "${tx.distributorName} - ${tx.categoryName}",
                                subtitle = "${tx.quantity} كرت • ${PdfAndShareHelper.formatCurrency(tx.totalAmount, currency)}",
                                date = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tx.date)),
                                amount = PdfAndShareHelper.formatCurrency(tx.paidAmount, currency) + " مدفوع",
                                onClick = {
                                    previewDetailsTitle = "معاينة كشف فاتورة مبيعات"
                                    previewDetailsText = buildString {
                                        appendLine("رقم الفاتورة: #${tx.id}")
                                        appendLine("الموزع: ${tx.distributorName}")
                                        appendLine("الصنف: ${tx.categoryName}")
                                        appendLine("الكمية: ${tx.quantity}")
                                        appendLine("سعر الوحدة: ${PdfAndShareHelper.formatCurrency(tx.unitPrice, currency)}")
                                        appendLine("إجمالي المبلغ: ${PdfAndShareHelper.formatCurrency(tx.totalAmount, currency)}")
                                        appendLine("المدفوع: ${PdfAndShareHelper.formatCurrency(tx.paidAmount, currency)}")
                                        appendLine("المتبقي: ${PdfAndShareHelper.formatCurrency(tx.remainingAmount, currency)}")
                                        appendLine("التاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tx.date))}")
                                        appendLine("الموظف: ${tx.employeeName}")
                                        if (tx.notes.isNotEmpty()) appendLine("ملاحظات: ${tx.notes}")
                                    }
                                }
                            )
                        }
                    }
                }
                ReportType.DISTRIBUTOR_DEBTS -> {
                    val activeDebts = distributors.filter { it.currentBalance > 0 }
                    if (activeDebts.isEmpty()) {
                        item { Text("لا توجد ديون على الموزعين", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(activeDebts) { d ->
                            ReportItemCard(
                                title = d.name,
                                subtitle = "الهاتف: ${d.phone.ifEmpty { "غير محدد" }} • الحد: ${PdfAndShareHelper.formatCurrency(d.creditLimit, currency)}",
                                date = "الديون الحالية",
                                amount = PdfAndShareHelper.formatCurrency(d.currentBalance, currency),
                                amountColor = MaterialTheme.colorScheme.error,
                                onClick = {
                                    previewDetailsTitle = "كشف حساب الموزع: ${d.name}"
                                    previewDetailsText = buildString {
                                        appendLine("اسم الموزع: ${d.name}")
                                        appendLine("ملاحظات: ${d.notes}")
                                        appendLine("الرصيد والديون الحالية: ${PdfAndShareHelper.formatCurrency(d.currentBalance, currency)}")
                                        appendLine("الحد الائتماني: ${PdfAndShareHelper.formatCurrency(d.creditLimit, currency)}")
                                        appendLine("فترة التسديد: ${d.paymentPeriodDays} يوم")
                                        appendLine("تجاوز الحد الائتماني: ${if (d.isCreditLimitExceeded) "نعم" else "لا"}")
                                    }
                                }
                            )
                        }
                    }
                }
                ReportType.PROFITS -> {
                    val salesList = filteredTransactions.filter { it.type == TransactionType.SALE }
                    if (salesList.isEmpty()) {
                        item { Text("لا توجد بيانات أرباح في هذه الفترة", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(salesList) { tx ->
                            val cat = categories.find { it.id == tx.categoryId }
                            val margin = cat?.let { (tx.unitPrice - it.costPrice).coerceAtLeast(0.0) * tx.quantity - tx.discount } ?: 0.0
                            ReportItemCard(
                                title = "${tx.distributorName} - ${tx.categoryName}",
                                subtitle = "الكمية: ${tx.quantity} كرت • تكلفة الوحدة: ${cat?.costPrice ?: 0.0}",
                                date = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tx.date)),
                                amount = "ربح: " + PdfAndShareHelper.formatCurrency(margin, currency),
                                amountColor = Color(0xFF16A34A),
                                onClick = {
                                    previewDetailsTitle = "تفاصيل أرباح العملية"
                                    previewDetailsText = buildString {
                                        appendLine("الموزع: ${tx.distributorName}")
                                        appendLine("الصنف: ${tx.categoryName}")
                                        appendLine("الكمية المباعة: ${tx.quantity}")
                                        appendLine("سعر البيع: ${PdfAndShareHelper.formatCurrency(tx.unitPrice, currency)}")
                                        appendLine("سعر التكلفة: ${PdfAndShareHelper.formatCurrency(cat?.costPrice ?: 0.0, currency)}")
                                        appendLine("صافي ربح العملية: ${PdfAndShareHelper.formatCurrency(margin, currency)}")
                                    }
                                }
                            )
                        }
                    }
                }
                ReportType.TREASURY -> {
                    val cashList = filteredTransactions.filter { it.type == TransactionType.PAYMENT_IN || it.type == TransactionType.PAYMENT_OUT }
                    if (cashList.isEmpty()) {
                        item { Text("لا توجد حركات نقدية في هذه الفترة", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(cashList) { tx ->
                            val isIn = tx.type == TransactionType.PAYMENT_IN
                            ReportItemCard(
                                title = if (isIn) "سند قبض من ${tx.distributorName}" else "سند صرف / مصروفات",
                                subtitle = tx.notes.ifEmpty { "بدون ملاحظات" },
                                date = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tx.date)),
                                amount = PdfAndShareHelper.formatCurrency(if (isIn) tx.paidAmount else tx.totalAmount, currency),
                                amountColor = if (isIn) Color(0xFF16A34A) else MaterialTheme.colorScheme.error,
                                onClick = {
                                    previewDetailsTitle = if (isIn) "معاينة سند قبض" else "معاينة سند صرف"
                                    previewDetailsText = buildString {
                                        appendLine("نوع الحركة: ${if (isIn) "سند قبض نقدي" else "سند صرف نقدي"}")
                                        appendLine("الجهة / الموزع: ${tx.distributorName}")
                                        appendLine("المبلغ: ${PdfAndShareHelper.formatCurrency(if (isIn) tx.paidAmount else tx.totalAmount, currency)}")
                                        appendLine("التاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tx.date))}")
                                        appendLine("الموظف المسؤول: ${tx.employeeName}")
                                        if (tx.notes.isNotEmpty()) appendLine("البيان / ملاحظات: ${tx.notes}")
                                    }
                                }
                            )
                        }
                    }
                }
                ReportType.EMPLOYEES -> {
                    if (users.isEmpty()) {
                        item { Text("لا يوجد موظفون مسجلون", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(users) { u ->
                            ReportItemCard(
                                title = u.displayName,
                                subtitle = "اسم المستخدم: ${u.username} • الدور: ${u.role.titleAr}",
                                date = if (u.isActive) "نشط" else "متوقف",
                                amount = u.phone.ifEmpty { "بلا هاتف" },
                                onClick = {
                                    previewDetailsTitle = "معاينة صلاحيات الموظف: ${u.displayName}"
                                    previewDetailsText = buildString {
                                        appendLine("اسم الموظف: ${u.displayName}")
                                        appendLine("اسم المستخدم: ${u.username}")
                                        appendLine("الدور الوظيفي: ${u.role.titleAr}")
                                        appendLine("رقم الهاتف: ${u.phone}")
                                        appendLine("الحالة: ${if (u.isActive) "نشط" else "موقوف"}")
                                        appendLine("--- الصلاحيات التفصيلية ---")
                                        appendLine("المبيعات (إضافة: ${u.canAddSales}, تعديل: ${u.canEditSales}, حذف: ${u.canDeleteSales})")
                                        appendLine("المشتريات (إضافة: ${u.canAddPurchases}, تعديل: ${u.canEditPurchases}, حذف: ${u.canDeletePurchases})")
                                        appendLine("السندات (إضافة: ${u.canAddVouchers}, تعديل: ${u.canEditVouchers}, حذف: ${u.canDeleteVouchers})")
                                        appendLine("التحويلات (إضافة: ${u.canAddTransfers}, تعديل: ${u.canEditTransfers}, حذف: ${u.canDeleteTransfers})")
                                        appendLine("التقارير والكروت (إضافة: ${u.canAddReports}, كروت: ${u.canAddCards})")
                                    }
                                }
                            )
                        }
                    }
                }
                ReportType.PURCHASES -> {
                    val purList = filteredTransactions.filter { it.type == TransactionType.PURCHASE }
                    if (purList.isEmpty()) {
                        item { Text("لا توجد مشتريات في هذه الفترة", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(purList) { tx ->
                            ReportItemCard(
                                title = tx.categoryName,
                                subtitle = "الكمية: ${tx.quantity} كرت • التكلفة: ${PdfAndShareHelper.formatCurrency(tx.unitPrice, currency)}",
                                date = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tx.date)),
                                amount = PdfAndShareHelper.formatCurrency(tx.totalAmount, currency),
                                amountColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    previewDetailsTitle = "معاينة فاتورة مشتريات مخزن"
                                    previewDetailsText = buildString {
                                        appendLine("رقم الفاتورة: #${tx.id}")
                                        appendLine("الصنف: ${tx.categoryName}")
                                        appendLine("الكمية المشتراة: ${tx.quantity}")
                                        appendLine("سعر التكلفة للوحدة: ${PdfAndShareHelper.formatCurrency(tx.unitPrice, currency)}")
                                        appendLine("إجمالي التكلفة: ${PdfAndShareHelper.formatCurrency(tx.totalAmount, currency)}")
                                        appendLine("التاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tx.date))}")
                                        appendLine("المسؤول: ${tx.employeeName}")
                                    }
                                }
                            )
                        }
                    }
                }
                ReportType.VOUCHERS -> {
                    val vList = filteredTransactions.filter { it.type == TransactionType.PAYMENT_IN || it.type == TransactionType.PAYMENT_OUT }
                    if (vList.isEmpty()) {
                        item { Text("لا توجد سندات مسجلة في هذه الفترة", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(vList) { tx ->
                            val isIn = tx.type == TransactionType.PAYMENT_IN
                            ReportItemCard(
                                title = if (isIn) "سند قبض - ${tx.distributorName}" else "سند صرف",
                                subtitle = tx.notes.ifEmpty { "بدون ملاحظات" },
                                date = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tx.date)),
                                amount = PdfAndShareHelper.formatCurrency(if (isIn) tx.paidAmount else tx.totalAmount, currency),
                                amountColor = if (isIn) Color(0xFF16A34A) else MaterialTheme.colorScheme.error,
                                onClick = {
                                    previewDetailsTitle = if (isIn) "معاينة سند قبض تفصيلي" else "معاينة سند صرف تفصيلي"
                                    previewDetailsText = buildString {
                                        appendLine("رقم السند: #${tx.id}")
                                        appendLine("النوع: ${if (isIn) "سند قبض" else "سند صرف"}")
                                        appendLine("الجهة: ${tx.distributorName}")
                                        appendLine("المبلغ: ${PdfAndShareHelper.formatCurrency(if (isIn) tx.paidAmount else tx.totalAmount, currency)}")
                                        appendLine("التاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tx.date))}")
                                        appendLine("الموظف: ${tx.employeeName}")
                                        if (tx.notes.isNotEmpty()) appendLine("البيان: ${tx.notes}")
                                    }
                                }
                            )
                        }
                    }
                }
                ReportType.TRANSFERS -> {
                    val tList = employeeTransfers.filter { tx ->
                        when (timeFilter) {
                            "TODAY" -> (now - tx.date) <= dayMs
                            "WEEK" -> (now - tx.date) <= (7 * dayMs)
                            "MONTH" -> (now - tx.date) <= (30 * dayMs)
                            else -> true
                        }
                    }
                    if (tList.isEmpty()) {
                        item { Text("لا توجد تحويلات في هذه الفترة", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(tList) { tr ->
                            ReportItemCard(
                                title = "${tr.fromEmployee} ⟵ ${tr.toEmployee}",
                                subtitle = if (tr.transferType == "CARDS") "عهدة كروت: ${tr.quantity} ${tr.categoryName}" else "عهدة نقدية: ${PdfAndShareHelper.formatCurrency(tr.cashAmount, currency)}",
                                date = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tr.date)),
                                amount = tr.transferCode,
                                onClick = {
                                    previewDetailsTitle = "معاينة تحويل عهدة تفصيلي"
                                    previewDetailsText = buildString {
                                        appendLine("رمز التحويل: ${tr.transferCode}")
                                        appendLine("من موظف: ${tr.fromEmployee}")
                                        appendLine("إلى موظف: ${tr.toEmployee}")
                                        appendLine("نوع التحويل: ${if (tr.transferType == "CARDS") "عهدة كروت" else "عهدة نقدية"}")
                                        if (tr.transferType == "CARDS") {
                                            appendLine("الفئة: ${tr.categoryName}")
                                            appendLine("الكمية: ${tr.quantity}")
                                        } else {
                                            appendLine("المبلغ النقدي: ${PdfAndShareHelper.formatCurrency(tr.cashAmount, currency)}")
                                        }
                                        appendLine("الحالة: ${tr.status}")
                                        appendLine("التاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tr.date))}")
                                        if (tr.notes.isNotEmpty()) appendLine("ملاحظات: ${tr.notes}")
                                    }
                                }
                            )
                        }
                    }
                }
                ReportType.SWAPS -> {
                    val sList = cardSwaps.filter { s ->
                        when (timeFilter) {
                            "TODAY" -> (now - s.date) <= dayMs
                            "WEEK" -> (now - s.date) <= (7 * dayMs)
                            "MONTH" -> (now - s.date) <= (30 * dayMs)
                            else -> true
                        }
                    }
                    if (sList.isEmpty()) {
                        item { Text("لا توجد عمليات استبدال في هذه الفترة", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else {
                        items(sList) { sw ->
                            ReportItemCard(
                                title = "${sw.distributorName} (${sw.employeeName})",
                                subtitle = "من: ${sw.fromQuantity} ${sw.fromCategoryName} إلى: ${sw.toQuantity} ${sw.toCategoryName}",
                                date = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(sw.date)),
                                amount = PdfAndShareHelper.formatCurrency(sw.differenceAmount, currency),
                                amountColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    previewDetailsTitle = "معاينة عملية استبدال الكروت"
                                    previewDetailsText = buildString {
                                        appendLine("رمز الاستبدال: ${sw.swapCode}")
                                        appendLine("الموزع: ${sw.distributorName}")
                                        appendLine("الموظف المسؤول: ${sw.employeeName}")
                                        appendLine("استبدال من: ${sw.fromQuantity} كرت من (${sw.fromCategoryName}) بقيمة ${PdfAndShareHelper.formatCurrency(sw.fromTotalValue, currency)}")
                                        appendLine("إلى: ${sw.toQuantity} كرت من (${sw.toCategoryName}) بقيمة ${PdfAndShareHelper.formatCurrency(sw.toTotalValue, currency)}")
                                        appendLine("فرق المبلغ: ${PdfAndShareHelper.formatCurrency(sw.differenceAmount, currency)}")
                                        appendLine("المدفوع من الفرق: ${PdfAndShareHelper.formatCurrency(sw.paidDifference, currency)}")
                                        appendLine("المتبقي من الفرق: ${PdfAndShareHelper.formatCurrency(sw.remainingDifference, currency)}")
                                        appendLine("التاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(sw.date))}")
                                        if (sw.notes.isNotEmpty()) appendLine("ملاحظات: ${sw.notes}")
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    } else {
        // Menu List View (Vertical Cards for each Report Section)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("reports_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "التقارير المالية والإدارية الشاملة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(availableReports) { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activeReportScreen = report },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = report.color.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = report.icon,
                                    contentDescription = null,
                                    tint = report.color,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = report.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun ReportSummaryRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun ReportItemCard(
    title: String,
    subtitle: String,
    date: String,
    amount: String,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
