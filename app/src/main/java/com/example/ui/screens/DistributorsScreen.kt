package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Distributor
import com.example.data.model.TransactionRecord
import com.example.ui.NetCardsViewModel
import com.example.util.PdfAndShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DistributorsScreen(
    viewModel: NetCardsViewModel
) {
    val context = LocalContext.current
    val distributors by viewModel.distributors.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"
    val networkName = settings?.networkName ?: "شبكة كروت نت"

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, DEBT, OVER_LIMIT, OVERDUE

    var showAddDialog by remember { mutableStateOf(false) }
    var editingDistributor by remember { mutableStateOf<Distributor?>(null) }
    var viewingStatementDistributor by remember { mutableStateOf<Distributor?>(null) }
    var quickPaymentDistributor by remember { mutableStateOf<Distributor?>(null) }

    val filteredList = distributors.filter { dist ->
        val matchesQuery = dist.name.contains(searchQuery, ignoreCase = true) ||
                dist.phone.contains(searchQuery)
        val matchesFilter = when (selectedFilter) {
            "DEBT" -> dist.currentBalance > 0
            "OVER_LIMIT" -> dist.isCreditLimitExceeded
            "OVERDUE" -> dist.isPaymentOverdue()
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Box(modifier = Modifier.fillMaxSize().testTag("distributors_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث باسم الموزع أو رقم الهاتف...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_distributor_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("الكل (${distributors.size})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "DEBT",
                        onClick = { selectedFilter = "DEBT" },
                        label = { Text("عليهم ديون (${distributors.count { it.currentBalance > 0 }})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "OVER_LIMIT",
                        onClick = { selectedFilter = "OVER_LIMIT" },
                        label = { Text("تجاوز الحد (${distributors.count { it.isCreditLimitExceeded }})") }
                    )
                    FilterChip(
                        selected = selectedFilter == "OVERDUE",
                        onClick = { selectedFilter = "OVERDUE" },
                        label = { Text("متأخر بالسداد (${distributors.count { it.isPaymentOverdue() }})", maxLines = 1, softWrap = false) }
                    )
                }
            }

            // Distributors List
            if (filteredList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد نتائج مطابقة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredList) { dist ->
                    DistributorCard(
                        distributor = dist,
                        currency = currency,
                        networkName = networkName,
                        onSendWhatsApp = { type ->
                            val distTx = transactions.filter { it.distributorId == dist.id }
                            val msg = when (type) {
                                "SUMMARY" -> PdfAndShareHelper.createSummaryAccountReport(dist, distTx, networkName, currency)
                                "DETAILED" -> PdfAndShareHelper.createDetailedAccountReport(dist, distTx, networkName, currency)
                                else -> PdfAndShareHelper.createDuePaymentReminderMessage(dist, networkName, currency)
                            }
                            PdfAndShareHelper.shareViaWhatsApp(context, dist.phone, msg)
                        },
                        onViewStatement = { viewingStatementDistributor = dist },
                        onQuickPay = { quickPaymentDistributor = dist },
                        onEdit = { editingDistributor = dist },
                        onDelete = { viewModel.deleteDistributor(dist) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 60.dp)
                .testTag("add_distributor_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة موزع جديد")
        }
    }

    // Add / Edit Distributor Dialog
    if (showAddDialog || editingDistributor != null) {
        val isEdit = editingDistributor != null
        val target = editingDistributor

        var name by remember { mutableStateOf(target?.name ?: "") }
        var phone by remember { mutableStateOf(target?.phone ?: "") }
        var openingBalanceStr by remember { mutableStateOf(target?.openingBalance?.toInt()?.toString() ?: "0") }
        var creditLimitStr by remember { mutableStateOf(target?.creditLimit?.toInt()?.toString() ?: "50000") }
        var paymentPeriodDaysStr by remember { mutableStateOf(target?.paymentPeriodDays?.toString() ?: "14") }
        var discountPercentStr by remember {
            mutableStateOf(
                if (target != null && target.discountPercent > 0) {
                    if (target.discountPercent % 1.0 == 0.0) target.discountPercent.toInt().toString() else target.discountPercent.toString()
                } else "0"
            )
        }
        var notes by remember { mutableStateOf(target?.notes ?: "") }

        Dialog(
            onDismissRequest = {
                showAddDialog = false
                editingDistributor = null
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                        .heightIn(max = maxHeight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isEdit) Icons.Default.Edit else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isEdit) "تعديل بيانات الموزع" else "إضافة موزع أو نقطة بيع",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "أدخل تفاصيل الحساب والحد الائتماني للموزع",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الموزع / نقطة البيع *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dist_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف / الواتساب *") },
                        leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dist_phone_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = openingBalanceStr,
                            onValueChange = { openingBalanceStr = it },
                            label = { Text("الرصيد السابق") },
                            leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dist_opening_balance_input"),
                            enabled = !isEdit,
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = creditLimitStr,
                            onValueChange = { creditLimitStr = it },
                            label = { Text("حد الائتمان") },
                            leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dist_credit_limit_input"),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = paymentPeriodDaysStr,
                            onValueChange = { paymentPeriodDaysStr = it },
                            label = { Text("مهلة السداد (أيام)") },
                            leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dist_grace_period_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = discountPercentStr,
                            onValueChange = { discountPercentStr = it },
                            label = { Text("نسبة الخصم (%)") },
                            placeholder = { Text("0") },
                            leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dist_discount_percent_input"),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات (عنوان، موقع، تفاصيل)") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showAddDialog = false
                            editingDistributor = null
                        }) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isBlank() || phone.isBlank()) {
                                    viewModel.showMessage("يرجى إدخال اسم الموزع ورقم الهاتف")
                                    return@Button
                                }
                                val opening = openingBalanceStr.toDoubleOrNull() ?: 0.0
                                val limit = creditLimitStr.toDoubleOrNull() ?: 50000.0
                                val days = paymentPeriodDaysStr.toIntOrNull() ?: 14
                                val discountPct = discountPercentStr.toDoubleOrNull() ?: 0.0

                                if (isEdit && target != null) {
                                    viewModel.updateDistributor(
                                        target.copy(
                                            name = name,
                                            phone = phone,
                                            creditLimit = limit,
                                            paymentPeriodDays = days,
                                            discountPercent = discountPct,
                                            notes = notes
                                        )
                                    )
                                } else {
                                    viewModel.addDistributor(
                                        name = name,
                                        phone = phone,
                                        openingBalance = opening,
                                        creditLimit = limit,
                                        paymentPeriodDays = days,
                                        discountPercent = discountPct,
                                        notes = notes
                                    )
                                }
                                showAddDialog = false
                                editingDistributor = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("save_distributor_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEdit) "حفظ التعديلات" else "إضافة الموزع")
                        }
                    }
                }
            }
        }
    }
}

    // Statement Dialog & Preview (معاينة كشف الحساب وأزرار المشاركة للواتس وبرامج الجوال)
    if (viewingStatementDistributor != null) {
        val dist = viewingStatementDistributor!!
        val distTx = transactions.filter { it.distributorId == dist.id }
        com.example.ui.components.AccountStatementPreviewDialog(
            title = "كشف حساب الموزع: ${dist.name}",
            accountName = dist.name,
            phone = dist.phone,
            openingBalance = dist.openingBalance,
            creditLimit = dist.creditLimit,
            currency = currency,
            networkName = networkName,
            transactions = distTx,
            isSupplier = false,
            distributor = dist,
            onDismiss = { viewingStatementDistributor = null }
        )
    }

    // Quick Payment Receipt Dialog
    if (quickPaymentDistributor != null) {
        val dist = quickPaymentDistributor!!
        var payAmountStr by remember { mutableStateOf("") }
        var payMethod by remember { mutableStateOf("CASH") }
        var payNotes by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = { quickPaymentDistributor = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 480.dp)
                        .heightIn(max = maxHeight)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp)
                    ) {
                    Text(
                        text = "سند قبض نقدي سريع - ${dist.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                    Text(
                        text = "الرصيد المستحق الحالي: ${PdfAndShareHelper.formatCurrency(dist.currentBalance, currency)}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = payAmountStr,
                        onValueChange = { payAmountStr = it },
                        label = { Text("المبلغ المستلم *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = payNotes,
                        onValueChange = { payNotes = it },
                        label = { Text("ملاحظات السند (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { quickPaymentDistributor = null }) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = payAmountStr.toDoubleOrNull() ?: 0.0
                                if (amt <= 0) {
                                    viewModel.showMessage("يرجى إدخال مبلغ صحيح")
                                    return@Button
                                }
                                viewModel.recordPaymentIn(
                                    distributorId = dist.id,
                                    amount = amt,
                                    paymentMethod = payMethod,
                                    notes = payNotes,
                                    onSuccess = { tx ->
                                        // Share via whatsapp receipt
                                        val receiptMsg = "سند قبض رقم #${tx.invoiceNumber}\nالموزع: ${dist.name}\nالمبلغ المستلم: ${PdfAndShareHelper.formatCurrency(amt, currency)}\nالرصيد المتبقي: ${PdfAndShareHelper.formatCurrency((dist.currentBalance - amt), currency)}"
                                        PdfAndShareHelper.shareViaWhatsApp(context, dist.phone, receiptMsg)
                                    }
                                )
                                quickPaymentDistributor = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Text("حفظ السند وإرسال إشعار", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
}

@Composable
fun DistributorCard(
    distributor: Distributor,
    currency: String,
    networkName: String,
    onSendWhatsApp: (String) -> Unit,
    onViewStatement: () -> Unit,
    onQuickPay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var whatsappMenuExpanded by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }

    val isOverLimit = distributor.isCreditLimitExceeded
    val isOverdue = distributor.isPaymentOverdue()
    val progress = if (distributor.creditLimit > 0) {
        (distributor.currentBalance / distributor.creditLimit).toFloat().coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Name & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = distributor.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📱 ${distributor.phone}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (distributor.discountPercent > 0) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF16A34A).copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "🏷️ خصم: ${if (distributor.discountPercent % 1.0 == 0.0) distributor.discountPercent.toInt() else distributor.discountPercent}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Balance Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (distributor.currentBalance > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = PdfAndShareHelper.formatCurrency(distributor.currentBalance, currency),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (distributor.currentBalance > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Warning Chips if limit or grace passed
            if (isOverLimit || isOverdue) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isOverLimit) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "⚠️ تجاوز الحد الائتماني",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isOverdue) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = "⏰ متأخر بالسداد (${distributor.daysSinceLastPayment()} يوم)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Credit Limit Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "الحد: ${PdfAndShareHelper.formatCurrency(distributor.creditLimit, currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "مهلة السداد: ${distributor.paymentPeriodDays} يوم",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "الخصم: ${if (distributor.discountPercent % 1.0 == 0.0) distributor.discountPercent.toInt() else distributor.discountPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (distributor.discountPercent > 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (distributor.discountPercent > 0) Color(0xFF16A34A) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Statement Button
                Box {
                    Button(
                        onClick = { whatsappMenuExpanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("واتساب", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }

                    DropdownMenu(
                        expanded = whatsappMenuExpanded,
                        onDismissRequest = { whatsappMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("إرسال كشف حساب مختصر") },
                            onClick = {
                                whatsappMenuExpanded = false
                                onSendWhatsApp("SUMMARY")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("إرسال كشف حساب تفصيلي شامل") },
                            onClick = {
                                whatsappMenuExpanded = false
                                onSendWhatsApp("DETAILED")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("إرسال تذكير استحقاق السداد") },
                            onClick = {
                                whatsappMenuExpanded = false
                                onSendWhatsApp("REMINDER")
                            }
                        )
                    }
                }

                // Statement Dialog Button
                OutlinedButton(
                    onClick = onViewStatement,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("كشف الحساب", fontSize = 12.sp, maxLines = 1, softWrap = false)
                }

                // Quick Pay Button
                OutlinedButton(
                    onClick = onQuickPay,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("قبض نقد", fontSize = 12.sp, maxLines = 1, softWrap = false)
                }

                // Edit Button
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
