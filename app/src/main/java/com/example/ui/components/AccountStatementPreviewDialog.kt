package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Distributor
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.util.PdfAndShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountStatementPreviewDialog(
    title: String,
    accountName: String,
    phone: String,
    openingBalance: Double,
    creditLimit: Double,
    currency: String,
    networkName: String,
    transactions: List<TransactionRecord>,
    isSupplier: Boolean = false,
    supplierName: String = "",
    distributor: Distributor? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val detailedReport = if (isSupplier) {
        PdfAndShareHelper.createDetailedSupplierReport(supplierName, transactions, networkName, currency)
    } else if (distributor != null) {
        PdfAndShareHelper.createDetailedAccountReport(distributor, transactions, networkName, currency)
    } else {
        title
    }

    val summaryReport = if (isSupplier) {
        PdfAndShareHelper.createSummarySupplierReport(supplierName, transactions, networkName, currency)
    } else if (distributor != null) {
        PdfAndShareHelper.createSummaryAccountReport(distributor, transactions, networkName, currency)
    } else {
        title
    }

    // Calculate running balance rows
    data class TableRow(
        val date: String,
        val description: String,
        val debit: Double,  // مدين (مبيعات / مشتريات)
        val credit: Double, // دائن (سندات قبض / دفعات)
        val balance: Double
    )

    var currentRunningBalance = openingBalance
    var totalDebits = 0.0
    var totalCredits = 0.0

    val tableRows = transactions.map { tx ->
        val dateStr = PdfAndShareHelper.formatShortDate(tx.date)
        val (desc, debit, credit) = if (isSupplier) {
            when (tx.type) {
                TransactionType.PURCHASE -> Triple("فاتورة مشتريات #${tx.invoiceNumber} [${tx.categoryName}]", tx.totalAmount, 0.0)
                TransactionType.PURCHASE_RETURN -> Triple("مرتجع مشتريات #${tx.invoiceNumber}", 0.0, tx.totalAmount)
                TransactionType.PAYMENT_OUT -> Triple("سند صرف / دفعة للمورد", 0.0, tx.paidAmount)
                else -> Triple(tx.type.name, tx.totalAmount, 0.0)
            }
        } else {
            when (tx.type) {
                TransactionType.SALE -> Triple("فاتورة مبيعات #${tx.invoiceNumber} [${tx.categoryName}]", tx.totalAmount, tx.paidAmount)
                TransactionType.SALE_RETURN, TransactionType.RETURN -> Triple("مرتجع مبيعات #${tx.invoiceNumber}", 0.0, tx.totalAmount)
                TransactionType.PAYMENT_IN -> Triple("سند قبض نقدي #${tx.invoiceNumber}", 0.0, tx.paidAmount)
                TransactionType.DISCOUNT -> Triple("خصم ممنوح", 0.0, tx.discount)
                else -> Triple(tx.type.name, tx.totalAmount, tx.paidAmount)
            }
        }

        totalDebits += debit
        totalCredits += credit

        if (isSupplier) {
            currentRunningBalance += (debit - credit)
        } else {
            // For distributors: Sales increase balance (debt), Payments decrease balance
            val netDelta = tx.remainingAmount.let { if (it > 0) it else (tx.totalAmount - tx.paidAmount) }
            currentRunningBalance = if (tx.type == TransactionType.PAYMENT_IN) {
                currentRunningBalance - tx.paidAmount
            } else if (tx.type == TransactionType.SALE) {
                currentRunningBalance + netDelta
            } else {
                currentRunningBalance + (debit - credit)
            }
        }

        TableRow(date = dateStr, description = desc, debit = debit, credit = credit, balance = currentRunningBalance)
    }

    val finalBalance = tableRows.lastOrNull()?.balance ?: openingBalance
    val dateNow = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()).format(Date())
    val statementNumber = "ST-${(accountName.hashCode() and 0x7FFFFFFF).toString().takeLast(6)}"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E293B)),
            color = Color(0xFF1E293B)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // ================= TOP ACTION BAR (ICONS AT TOP OF PREVIEW) =================
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PDF",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isSupplier) "كشف حساب المورد: $accountName" else "كشف حساب: $accountName",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "معاينة المستند الرسمي للطباعة والمشاركة",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    },
                    actions = {
                        // 1. WhatsApp Detailed Icon Button
                        IconButton(
                            onClick = {
                                PdfAndShareHelper.shareViaWhatsApp(context, phone, detailedReport)
                            },
                            modifier = Modifier.testTag("action_whatsapp_detailed")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF25D366).copy(alpha = 0.18f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = "واتساب تفصيلي",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // 2. WhatsApp Summary Icon Button
                        IconButton(
                            onClick = {
                                PdfAndShareHelper.shareViaWhatsApp(context, phone, summaryReport)
                            },
                            modifier = Modifier.testTag("action_whatsapp_summary")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981).copy(alpha = 0.18f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.ReceiptLong,
                                        contentDescription = "واتساب مختصر",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // 3. Universal Mobile Share Icon Button
                        IconButton(
                            onClick = {
                                PdfAndShareHelper.shareViaUniversal(context, title, detailedReport)
                            },
                            modifier = Modifier.testTag("action_universal_share")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "مشاركة برامج الجوال",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // 4. Print / PDF Export Icon Button
                        IconButton(
                            onClick = {
                                PdfAndShareHelper.shareViaUniversal(context, title, detailedReport)
                            },
                            modifier = Modifier.testTag("action_print_pdf")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Print,
                                        contentDescription = "طباعة / تصدير PDF",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // ================= SIMULATED AUTHENTIC PDF PAPER SHEET =================
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF334155)) // Professional dark PDF canvas
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.707f) // A4 ratio
                            .padding(bottom = 16.dp), // Space for bottom system bars
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Official Document Header
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Top Row: Network Branding & Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF1E3A8A)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "🌐 $networkName",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF1F5F9),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Text(
                                                text = "كود المستند: $statementNumber",
                                                color = Color(0xFF475569),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Main Statement Title
                                    Text(
                                        text = if (isSupplier) "كشف حساب مالي تفصيلي للمورد" else "كشف حساب مالي تفصيلي للعميل / الموزع",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F172A),
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "تاريخ ووقت الإصدار: $dateNow  •  العملة المعتمدة: $currency",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(Color(0xFF1E3A8A))
                                    )
                                }
                            }

                            // 2. Account Information Box
                            item {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(16.dp))
                                                Text(
                                                    text = "اسم الحساب: $accountName",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A),
                                                    fontSize = 13.sp
                                                )
                                            }

                                            if (phone.isNotBlank()) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                                    Text(
                                                        text = phone,
                                                        color = Color(0xFF475569),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFE2E8F0))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "الرصيد الافتتاحي: ${PdfAndShareHelper.formatCurrency(openingBalance, currency)}",
                                                color = Color(0xFF475569),
                                                fontSize = 12.sp
                                            )

                                            if (!isSupplier && creditLimit > 0) {
                                                val isExceeded = finalBalance > creditLimit
                                                Text(
                                                    text = "الحد الائتماني: ${PdfAndShareHelper.formatCurrency(creditLimit, currency)} ${if (isExceeded) "⚠️ (متجاوز)" else "✅"}",
                                                    color = if (isExceeded) Color(0xFFDC2626) else Color(0xFF16A34A),
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // 3. Financial KPI Summary Metrics (3 Cards)
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Total Debits Card
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFEF2F2),
                                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (isSupplier) "المشتريات" else "المبيعات",
                                                fontSize = 9.sp,
                                                color = Color(0xFF991B1B),
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Text(
                                                text = PdfAndShareHelper.formatCurrency(totalDebits, currency),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFDC2626)
                                            )
                                        }
                                    }

                                    // Total Credits Card
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFF0FDF4),
                                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (isSupplier) "السداد" else "المقبوضات",
                                                fontSize = 9.sp,
                                                color = Color(0xFF166534),
                                                fontWeight = FontWeight.SemiBold,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Text(
                                                text = PdfAndShareHelper.formatCurrency(totalCredits, currency),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF16A34A)
                                            )
                                        }
                                    }

                                    // Net Balance Card
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFEFF6FF),
                                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "الصافي",
                                                fontSize = 9.sp,
                                                color = Color(0xFF1E40AF),
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(1.dp))
                                            Text(
                                                text = PdfAndShareHelper.formatCurrency(finalBalance, currency),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (finalBalance > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                                            )
                                        }
                                    }
                                }
                            }

                            // 4. Data Table Header Row
                            item {
                                Surface(
                                    color = Color(0xFF1E3A8A),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "التاريخ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.1f))
                                        Text(text = "البيان ونوع الحركة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2.4f), textAlign = TextAlign.Start)
                                        Text(text = "مدين (+)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.0f), textAlign = TextAlign.Center)
                                        Text(text = "دائن (-)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.0f), textAlign = TextAlign.Center)
                                        Text(text = "الرصيد", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                                    }
                                }
                            }

                            // 5. Data Table Rows
                            if (tableRows.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC))
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "لا توجد حركات أو معاملات مسجلة في هذا الكشف", color = Color.Gray, fontSize = 12.sp)
                                    }
                                }
                            } else {
                                itemsIndexed(tableRows) { index, row ->
                                    val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                                    Surface(
                                        color = rowBg,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 7.dp, horizontal = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = row.date, fontSize = 10.sp, color = Color(0xFF334155), modifier = Modifier.weight(1.1f))
                                            Text(text = row.description, fontSize = 11.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(2.4f), textAlign = TextAlign.Start, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                            Text(
                                                text = if (row.debit > 0) PdfAndShareHelper.formatCurrency(row.debit, currency) else "-",
                                                fontSize = 11.sp,
                                                color = Color(0xFFDC2626),
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1.0f),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = if (row.credit > 0) PdfAndShareHelper.formatCurrency(row.credit, currency) else "-",
                                                fontSize = 11.sp,
                                                color = Color(0xFF16A34A),
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1.0f),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = PdfAndShareHelper.formatCurrency(row.balance, currency),
                                                fontSize = 11.sp,
                                                color = Color(0xFF0F172A),
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1.2f),
                                                textAlign = TextAlign.End
                                            )
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                }
                            }

                            // 6. Table Summary Footer Row
                            item {
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "الإجمالي (${tableRows.size} حركة):",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF1E3A8A),
                                            modifier = Modifier.weight(3.5f)
                                        )
                                        Text(
                                            text = PdfAndShareHelper.formatCurrency(totalDebits, currency),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFFDC2626),
                                            modifier = Modifier.weight(1.0f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = PdfAndShareHelper.formatCurrency(totalCredits, currency),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF16A34A),
                                            modifier = Modifier.weight(1.0f),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = PdfAndShareHelper.formatCurrency(finalBalance, currency),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.weight(1.2f),
                                            textAlign = TextAlign.End
                                        )
                                    }
                                }
                            }

                            // 7. Official Stamps and Signature Section
                            item {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Official Stamp Box
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(90.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "ختم المنشأة والشبكة",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF64748B),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF1E3A8A).copy(alpha = 0.08f),
                                                border = BorderStroke(1.dp, Color(0xFF1E3A8A).copy(alpha = 0.3f)),
                                                modifier = Modifier.size(44.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "معتمد",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF1E3A8A),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Accountant Signature Line
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(90.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "توقيع المحاسب / الإدارة",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF64748B),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.8f)
                                                    .height(1.dp)
                                                    .background(Color(0xFF94A3B8))
                                            )
                                            Text(
                                                text = "المحاسب المالي",
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }

                            // 8. Legal Disclaimer & Security Watermark
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "ملاحظة هامة: يعتبر هذا الكشف نهائياً ومطابقاً لدفاتر الحسابات ما لم يرد أي اعتراض رسمي خلال 3 أيام من تاريخ استلامه.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Text(
                                            text = "تم إصدار هذا المستند إلكترونياً عبر نظام إدارة شبكات وكروت الإنترنت",
                                            fontSize = 9.sp,
                                            color = Color(0xFF94A3B8),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
