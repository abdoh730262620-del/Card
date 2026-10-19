package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.TransactionType
import com.example.ui.AppScreen
import com.example.ui.NetCardsViewModel
import com.example.util.PdfAndShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VouchersScreen(
    viewModel: NetCardsViewModel
) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val distributors by viewModel.distributors.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"
    val networkName = settings?.networkName ?: "شبكة كروت نت"

    val categories by viewModel.categories.collectAsStateWithLifecycle()

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var voucherToEdit by remember { mutableStateOf<TransactionRecord?>(null) }
    var voucherToDelete by remember { mutableStateOf<TransactionRecord?>(null) }

    val filteredList = transactions.filter { tx ->
        when (currentScreen) {
            AppScreen.VOUCHERS_PAYMENT_IN -> tx.type == TransactionType.PAYMENT_IN
            AppScreen.VOUCHERS_PAYMENT_OUT -> tx.type == TransactionType.PAYMENT_OUT
            AppScreen.VOUCHERS_DISCOUNT -> tx.type == TransactionType.DISCOUNT
            AppScreen.VOUCHERS_RETURNS -> tx.type == TransactionType.RETURN || tx.type == TransactionType.SALE_RETURN || tx.type == TransactionType.PURCHASE_RETURN
            else -> true
        }
    }.sortedByDescending { it.date }

    Box(modifier = Modifier.fillMaxSize().testTag("vouchers_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Conditional Info Banners
            item {
                when (currentScreen) {
                    AppScreen.VOUCHERS_PAYMENT_OUT -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEA580C).copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CallMade,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "💡 ربط المصروفات بالصندوق: أي سند صرف أو مصروفات يتم تسجيله هنا يُخصم تلقائياً وبشكل فوري من صندوق البرنامج الرئيسي.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC2410C),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    AppScreen.VOUCHERS_RETURNS -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "💡 ربط المرتجعات: مرتجع المبيعات يعيد الكروت للمخزن ويسوي مديونية العميل أو يرد النقد. ومرتجع المشتريات يخصم الكروت من المخزن ويودع المبلغ بالصندوق.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    else -> {}
                }
            }

            // Menu Options (Vertical)
            if (currentScreen == AppScreen.VOUCHERS) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val voucherOptions = listOf(
                            Triple(AppScreen.VOUCHERS_PAYMENT_IN, "سندات قبض (تحصيل)", Icons.Default.CallReceived),
                            Triple(AppScreen.VOUCHERS_PAYMENT_OUT, "مصروفات وسندات صرف", Icons.Default.CallMade),
                            Triple(AppScreen.VOUCHERS_DISCOUNT, "خصومات مالية", Icons.Default.Discount),
                            Triple(AppScreen.VOUCHERS_RETURNS, "سجل المرتجعات", Icons.Default.History)
                        )

                        voucherOptions.forEach { (screen, title, icon) ->
                            val iconColor = when (screen) {
                                AppScreen.VOUCHERS_PAYMENT_IN -> Color(0xFF16A34A)
                                AppScreen.VOUCHERS_PAYMENT_OUT -> Color(0xFFEA580C)
                                AppScreen.VOUCHERS_DISCOUNT -> Color(0xFF7C3AED)
                                else -> Color(0xFF2563EB)
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.navigateTo(screen) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(icon, contentDescription = null, tint = iconColor)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                if (filteredList.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (currentScreen) {
                                    AppScreen.VOUCHERS_RETURNS -> "لا توجد عمليات مرتجع مسجلة حالياً"
                                    else -> "لا توجد سندات مسجلة في هذا القسم"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredList) { tx ->
                        VoucherItemCard(
                            tx = tx,
                            currency = currency,
                            onShareWhatsApp = {
                                val typeTitle = when (tx.type) {
                                    TransactionType.PAYMENT_IN -> "سند قبض نقدي"
                                    TransactionType.PAYMENT_OUT -> "سند صرف مصروفات (مخصوم من الصندوق)"
                                    TransactionType.DISCOUNT -> "خصم مالي"
                                    TransactionType.SALE_RETURN -> "مرتجع مبيعات كروت"
                                    TransactionType.PURCHASE_RETURN -> "مرتجع مشتريات كروت للمورد"
                                    else -> "سند مرتجع كروت"
                                }
                                val msg = "🧾 سند رقم #${tx.invoiceNumber} - $networkName\n" +
                                        "البيان: $typeTitle\n" +
                                        "الجهة / الموزع / المورد: ${tx.distributorName}\n" +
                                        "المبلغ: ${PdfAndShareHelper.formatCurrency(tx.totalAmount, currency)}\n" +
                                        (if (tx.quantity > 0) "الكمية: ${tx.quantity} كرت (${tx.categoryName})\n" else "") +
                                        "المسجل: ${tx.employeeName}\n" +
                                        "ملاحظات: ${tx.notes}"
                                val dist = distributors.find { it.id == tx.distributorId }
                                PdfAndShareHelper.shareViaWhatsApp(context, dist?.phone ?: "", msg)
                            },
                            onShareUniversal = {
                                val msg = "سند رقم #${tx.invoiceNumber}\n${tx.distributorName}\nالمبلغ: ${PdfAndShareHelper.formatCurrency(tx.totalAmount, currency)}"
                                PdfAndShareHelper.shareViaUniversal(context, "سند #${tx.invoiceNumber}", msg)
                            },
                            onEdit = {
                                if (!currentUser.canEditVouchers && currentUser.role != com.example.data.model.UserRole.ADMIN) {
                                    viewModel.showMessage("عذراً، ليس لديك صلاحية لتعديل السندات")
                                } else {
                                    voucherToEdit = tx
                                }
                            },
                            onDelete = {
                                if (!currentUser.canDeleteVouchers && currentUser.role != com.example.data.model.UserRole.ADMIN) {
                                    viewModel.showMessage("عذراً، ليس لديك صلاحية لحذف السندات")
                                } else {
                                    voucherToDelete = tx
                                }
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        // Floating Action Button to Add Selected Voucher Type
        FloatingActionButton(
            onClick = {
                if (!currentUser.canAddVouchers && currentUser.role != com.example.data.model.UserRole.ADMIN) {
                    viewModel.showMessage("عذراً، ليس لديك صلاحية لإضافة سندات جديدة")
                } else {
                    showAddDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 60.dp)
                .testTag("add_voucher_fab"),
            containerColor = when (currentScreen) {
                AppScreen.VOUCHERS_PAYMENT_IN -> Color(0xFF16A34A)
                AppScreen.VOUCHERS_PAYMENT_OUT -> Color(0xFFEA580C)
                AppScreen.VOUCHERS_DISCOUNT -> Color(0xFF7C3AED)
                else -> Color(0xFF0284C7)
            },
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة جديد")
        }
    }

    // Add Voucher / Return Dialog
    if (showAddDialog) {
        var selectedDistributor by remember { mutableStateOf<Distributor?>(null) }
        var distDropdownExpanded by remember { mutableStateOf(false) }
        var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }
        var categoryDropdownExpanded by remember { mutableStateOf(false) }
        var returnType by remember { mutableStateOf("SALE_RETURN") } // "SALE_RETURN" or "PURCHASE_RETURN"
        var refundFromCash by remember { mutableStateOf(false) } // true: cash refund from fund, false: account debt reduction
        var quantityStr by remember { mutableStateOf("10") }
        var unitPriceStr by remember { mutableStateOf(selectedCategory?.sellingPrice?.toInt()?.toString() ?: "0") }
        var supplierName by remember { mutableStateOf("المورد الرئيسي") }
        var amountStr by remember { mutableStateOf("") }
        var titleStr by remember { mutableStateOf("") }
        var paymentMethod by remember { mutableStateOf("CASH") }
        var notes by remember { mutableStateOf("") }

        val dialogTitle = when (currentScreen) {
            AppScreen.VOUCHERS_PAYMENT_IN -> "إصدار سند قبض نقد / حوالة بنكية (تسديد من موزع)"
            AppScreen.VOUCHERS_PAYMENT_OUT -> "إصدار سند صرف مصروفات (ربط مباشر بالصندوق)"
            AppScreen.VOUCHERS_DISCOUNT -> "تسجيل خصم مالي لحساب موزع"
            else -> "تسجيل مرتجع كروت (مبيعات / مشتريات)"
        }

        Dialog(
            onDismissRequest = { showAddDialog = false },
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
                        Text(
                            text = dialogTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (currentScreen == AppScreen.VOUCHERS_PAYMENT_IN || currentScreen == AppScreen.VOUCHERS_DISCOUNT) {
                            // Distributor Selector
                            Text("الموزع:", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                OutlinedTextField(
                                    value = selectedDistributor?.name ?: "اختر الموزع...",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { distDropdownExpanded = true },
                                    trailingIcon = {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { distDropdownExpanded = true })
                                    }
                                )
                                DropdownMenu(
                                    expanded = distDropdownExpanded,
                                    onDismissRequest = { distDropdownExpanded = false }
                                ) {
                                    distributors.forEach { dist ->
                                        DropdownMenuItem(
                                            text = { Text("${dist.name} (المستحق: ${PdfAndShareHelper.formatCurrency(dist.currentBalance, currency)})") },
                                            onClick = {
                                                selectedDistributor = dist
                                                distDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        } else if (currentScreen == AppScreen.VOUCHERS_PAYMENT_OUT) {
                            // Title / Expense Purpose
                            OutlinedTextField(
                                value = titleStr,
                                onValueChange = { titleStr = it },
                                label = { Text("بيان المصروفات (مثال: اشتراك خط، محروقات، إيجار، صيانة) *") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        } else if (currentScreen == AppScreen.VOUCHERS_RETURNS) {
                            // Returns: Choose between Sale Return and Purchase Return
                            Text("نوع عملية الإرجاع:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = returnType == "SALE_RETURN",
                                    onClick = {
                                        returnType = "SALE_RETURN"
                                        unitPriceStr = selectedCategory?.sellingPrice?.toInt()?.toString() ?: "0"
                                    },
                                    label = { Text("مرتجع مبيعات (من عميل)") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = returnType == "PURCHASE_RETURN",
                                    onClick = {
                                        returnType = "PURCHASE_RETURN"
                                        unitPriceStr = selectedCategory?.costPrice?.toInt()?.toString() ?: "0"
                                    },
                                    label = { Text("مرتجع مشتريات (إلى مورد)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            if (returnType == "SALE_RETURN") {
                                Text("الموزع / العميل (اختياري للعميل النقدي):", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.height(4.dp))
                                Box {
                                    OutlinedTextField(
                                        value = selectedDistributor?.name ?: "عميل نقدي مباشر",
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { distDropdownExpanded = true },
                                        trailingIcon = {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { distDropdownExpanded = true })
                                        }
                                    )
                                    DropdownMenu(
                                        expanded = distDropdownExpanded,
                                        onDismissRequest = { distDropdownExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("عميل نقدي مباشر") },
                                            onClick = {
                                                selectedDistributor = null
                                                refundFromCash = true
                                                distDropdownExpanded = false
                                            }
                                        )
                                        distributors.forEach { dist ->
                                            DropdownMenuItem(
                                                text = { Text("${dist.name} (حسابه الحالي: ${PdfAndShareHelper.formatCurrency(dist.currentBalance, currency)})") },
                                                onClick = {
                                                    selectedDistributor = dist
                                                    distDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                Text("طريقة التسوية المالية:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = !refundFromCash,
                                        onClick = { refundFromCash = false },
                                        label = { Text("خصم من حساب وديون الموزع") }
                                    )
                                    FilterChip(
                                        selected = refundFromCash,
                                        onClick = { refundFromCash = true },
                                        label = { Text("رد نقد من الصندوق") }
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            } else {
                                // Purchase return: Supplier Name
                                OutlinedTextField(
                                    value = supplierName,
                                    onValueChange = { supplierName = it },
                                    label = { Text("اسم المورد / الشركة") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Category Selector
                            Text("فئة الكرت المرتجع:", style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box {
                                OutlinedTextField(
                                    value = selectedCategory?.name ?: "اختر الفئة...",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { categoryDropdownExpanded = true },
                                    trailingIcon = {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { categoryDropdownExpanded = true })
                                    }
                                )
                                DropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false }
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text("${cat.name} (المتوفر بالمخزن: ${cat.stockQuantity})") },
                                            onClick = {
                                                selectedCategory = cat
                                                unitPriceStr = if (returnType == "SALE_RETURN") {
                                                    cat.sellingPrice.toInt().toString()
                                                } else {
                                                    cat.costPrice.toInt().toString()
                                                }
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = quantityStr,
                                    onValueChange = { quantityStr = it },
                                    label = { Text("الكمية (كرت) *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = unitPriceStr,
                                    onValueChange = { unitPriceStr = it },
                                    label = { Text("السعر للكرت ($currency)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (currentScreen != AppScreen.VOUCHERS_RETURNS) {
                            OutlinedTextField(
                                value = amountStr,
                                onValueChange = { amountStr = it },
                                label = { Text("المبلغ الإجمالي *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (currentScreen == AppScreen.VOUCHERS_PAYMENT_IN) {
                            Text("طريقة التحصيل:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = paymentMethod == "CASH",
                                    onClick = { paymentMethod = "CASH" },
                                    label = { Text("نقد CASH", maxLines = 1, softWrap = false) }
                                )
                                FilterChip(
                                    selected = paymentMethod == "TRANSFER",
                                    onClick = { paymentMethod = "TRANSFER" },
                                    label = { Text("حوالة بنكية", maxLines = 1, softWrap = false) },
                                    modifier = Modifier.wrapContentWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Info / Total Box
                        if (currentScreen == AppScreen.VOUCHERS_PAYMENT_OUT) {
                            Surface(
                                color = Color(0xFFEA580C).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "💡 سيتم خصم هذا المبلغ فوراً من الصندوق الرئيسي وتسجيل حركة صرف رسمية.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFC2410C),
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        } else if (currentScreen == AppScreen.VOUCHERS_RETURNS) {
                            val q = quantityStr.toIntOrNull() ?: 0
                            val p = unitPriceStr.toDoubleOrNull() ?: 0.0
                            val tot = q * p
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("إجمالي قيمة المرتجع: ${PdfAndShareHelper.formatCurrency(tot, currency)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    if (returnType == "SALE_RETURN") {
                                        Text(
                                            text = if (refundFromCash) "• سيتم إعادة $q كرت للمخزن وصرف ${PdfAndShareHelper.formatCurrency(tot, currency)} نقداً من الصندوق."
                                            else "• سيتم إعادة $q كرت للمخزن وخصم ${PdfAndShareHelper.formatCurrency(tot, currency)} من ديون الموزع.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    } else {
                                        Text(
                                            text = "• سيتم خصم $q كرت من المخزن وإيداع ${PdfAndShareHelper.formatCurrency(tot, currency)} في الصندوق الرئيسي.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات إضافية") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddDialog = false }) {
                                Text("إلغاء")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (currentScreen == AppScreen.VOUCHERS_RETURNS) {
                                        val cat = selectedCategory
                                        if (cat == null) {
                                            viewModel.showMessage("يرجى اختيار الفئة")
                                            return@Button
                                        }
                                        val qty = quantityStr.toIntOrNull() ?: 0
                                        val price = unitPriceStr.toDoubleOrNull() ?: 0.0
                                        if (qty <= 0) {
                                            viewModel.showMessage("يرجى إدخال كمية صحيحة")
                                            return@Button
                                        }

                                        if (returnType == "SALE_RETURN") {
                                            viewModel.recordSaleReturn(
                                                distributorId = selectedDistributor?.id,
                                                categoryId = cat.id,
                                                quantity = qty,
                                                unitPrice = price,
                                                refundFromCash = refundFromCash || selectedDistributor == null,
                                                notes = notes
                                            )
                                        } else {
                                            viewModel.recordPurchaseReturn(
                                                categoryId = cat.id,
                                                quantity = qty,
                                                costPrice = price,
                                                supplierName = supplierName,
                                                refundToFund = true,
                                                notes = notes
                                            )
                                        }
                                        showAddDialog = false
                                        return@Button
                                    }

                                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                                    if (amt <= 0) {
                                        viewModel.showMessage("يرجى إدخال مبلغ صحيح")
                                        return@Button
                                    }

                                    when (currentScreen) {
                                        AppScreen.VOUCHERS_PAYMENT_IN -> {
                                            if (selectedDistributor == null) {
                                                viewModel.showMessage("يرجى اختيار الموزع")
                                                return@Button
                                            }
                                            viewModel.recordPaymentIn(
                                                distributorId = selectedDistributor!!.id,
                                                amount = amt,
                                                paymentMethod = paymentMethod,
                                                notes = notes,
                                                onSuccess = { tx ->
                                                    val receiptMsg = "سند قبض رقم #${tx.invoiceNumber}\nالموزع: ${selectedDistributor!!.name}\nالمبلغ المستلم: ${PdfAndShareHelper.formatCurrency(amt, currency)}\nالرصيد المتبقي: ${PdfAndShareHelper.formatCurrency((selectedDistributor!!.currentBalance - amt), currency)}"
                                                    PdfAndShareHelper.shareViaWhatsApp(context, selectedDistributor!!.phone, receiptMsg)
                                                }
                                            )
                                        }
                                        AppScreen.VOUCHERS_PAYMENT_OUT -> {
                                            if (titleStr.isBlank()) {
                                                viewModel.showMessage("يرجى إدخال بيان المصروفات")
                                                return@Button
                                            }
                                            viewModel.recordPaymentOut(
                                                title = titleStr,
                                                amount = amt,
                                                notes = notes
                                            )
                                        }
                                        AppScreen.VOUCHERS_DISCOUNT -> {
                                            if (selectedDistributor == null) {
                                                viewModel.showMessage("يرجى اختيار الموزع")
                                                return@Button
                                            }
                                            viewModel.recordDiscount(
                                                distributorId = selectedDistributor!!.id,
                                                amount = amt,
                                                notes = notes
                                            )
                                        }
                                        else -> {}
                                    }
                                    showAddDialog = false
                                }
                            ) {
                                Text("حفظ السند والربط المالي")
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Voucher Dialog
    voucherToEdit?.let { tx ->
        var editAmount by remember(tx) { mutableStateOf(tx.totalAmount.toString()) }
        var editNotes by remember(tx) { mutableStateOf(tx.notes) }

        Dialog(onDismissRequest = { voucherToEdit = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "تعديل السند #${tx.invoiceNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text("المبلغ ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editNotes,
                        onValueChange = { editNotes = it },
                        label = { Text("ملاحظات السند") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { voucherToEdit = null }) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = editAmount.toDoubleOrNull() ?: 0.0
                                if (amt <= 0) {
                                    viewModel.showMessage("يرجى إدخال مبلغ صحيح")
                                    return@Button
                                }
                                viewModel.updateVoucher(tx, amt, editNotes)
                                voucherToEdit = null
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("حفظ التعديل")
                        }
                    }
                }
            }
        }
    }

    // Delete Voucher Confirmation Dialog
    voucherToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { voucherToDelete = null },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = {
                Text("تأكيد حذف السند", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("هل أنت متأكد من حذف هذا السند رقم #${tx.invoiceNumber} بقيمة ${PdfAndShareHelper.formatCurrency(tx.totalAmount, currency)}؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVoucher(tx)
                        voucherToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("نعم، حذف السند")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { voucherToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun VoucherItemCard(
    tx: TransactionRecord,
    currency: String,
    onShareWhatsApp: () -> Unit,
    onShareUniversal: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(tx.date))
    val (typeColor, icon) = when (tx.type) {
        TransactionType.PAYMENT_IN -> Color(0xFF16A34A) to Icons.Default.CallReceived
        TransactionType.PAYMENT_OUT -> Color(0xFFEA580C) to Icons.Default.CallMade
        TransactionType.DISCOUNT -> Color(0xFF7C3AED) to Icons.Default.Discount
        TransactionType.SALE_RETURN -> Color(0xFF0284C7) to Icons.Default.ReceiptLong
        TransactionType.PURCHASE_RETURN -> Color(0xFF0D9488) to Icons.Default.ReceiptLong
        else -> Color(0xFF0284C7) to Icons.Default.ReceiptLong
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = typeColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = typeColor, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = tx.distributorName.ifBlank { "سند عام" },
                            style = MaterialTheme.typography.titleMedium,
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = typeColor
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = typeColor.copy(alpha = 0.12f)
                    ) {
                        val badge = when (tx.type) {
                            TransactionType.PAYMENT_IN -> "سند قبض"
                            TransactionType.PAYMENT_OUT -> "مصروفات (صندوق)"
                            TransactionType.DISCOUNT -> "خصم مالي"
                            TransactionType.SALE_RETURN -> "مرتجع مبيعات"
                            TransactionType.PURCHASE_RETURN -> "مرتجع مشتريات"
                            else -> "مرتجع كروت"
                        }
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = typeColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (tx.quantity > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📦 الصنف والكمية: ${tx.categoryName} (${tx.quantity} كرت × ${PdfAndShareHelper.formatCurrency(tx.unitPrice, currency)})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (tx.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ملاحظات: ${tx.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الموظف: ${tx.employeeName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShareWhatsApp) {
                        Icon(Icons.Default.Send, contentDescription = "واتساب", tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onShareUniversal) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
