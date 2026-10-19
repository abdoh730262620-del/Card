package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.data.model.InvoiceItemDraft
import com.example.data.model.TransactionItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CardCategory
import com.example.data.model.Distributor
import com.example.data.model.TransactionRecord
import com.example.ui.NetCardsViewModel
import com.example.ui.components.InvoicePreviewDialog
import com.example.util.PdfAndShareHelper
import java.util.Locale

@Composable
fun SalesAndInvoicingScreen(
    viewModel: NetCardsViewModel
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val distributors by viewModel.distributors.collectAsStateWithLifecycle()
    val salesInvoices by viewModel.salesTransactions.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"
    val networkName = settings?.networkName ?: "شبكة كروت نت"

    // Search and filter
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // ALL, CASH, CREDIT

    // Dialog states
    var showCreateSaleDialog by remember { mutableStateOf(false) }
    var showAddSaleReturnDialog by remember { mutableStateOf(false) }
    var saleToReturn by remember { mutableStateOf<TransactionRecord?>(null) }
    var invoiceToPreview by remember { mutableStateOf<TransactionRecord?>(null) }
    var invoiceItemsToPreview by remember { mutableStateOf<List<TransactionItem>>(emptyList()) }
    var saleToEdit by remember { mutableStateOf<TransactionRecord?>(null) }
    var saleToDelete by remember { mutableStateOf<TransactionRecord?>(null) }

    LaunchedEffect(invoiceToPreview) {
        invoiceItemsToPreview = if (invoiceToPreview != null) {
            viewModel.getTransactionItems(invoiceToPreview!!.id)
        } else {
            emptyList()
        }
    }

    // Filtered sales invoices
    val filteredSales = salesInvoices.filter { sale ->
        val query = searchQuery.trim().lowercase()
        val matchesQuery = if (query.isEmpty()) true else {
            sale.invoiceNumber.lowercase().contains(query) ||
            sale.distributorName.lowercase().contains(query) ||
            sale.categoryName.lowercase().contains(query) ||
            sale.notes.lowercase().contains(query)
        }
        val matchesFilter = when (filterType) {
            "CASH" -> sale.remainingAmount <= 0.0
            "CREDIT" -> sale.remainingAmount > 0.0
            else -> true
        }
        matchesQuery && matchesFilter
    }.sortedByDescending { it.date }

    // Statistics
    val totalSalesAmount = salesInvoices.sumOf { it.totalAmount }
    val totalPaidCash = salesInvoices.sumOf { it.paidAmount }
    val totalRemainingDebt = salesInvoices.sumOf { it.remainingAmount }
    val totalSalesCount = salesInvoices.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sales_invoicing_screen")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showCreateSaleDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_create_sale_header")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إنشاء فاتورة بيع جديدة", fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { showAddSaleReturnDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFDC2626)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_sale_return_header")
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مرتجع مبيعات", fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }

            // Summary Stats Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Sales
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("إجمالي المبيعات", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, softWrap = false)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = PdfAndShareHelper.formatCurrency(totalSalesAmount, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Cash in Fund
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("محصل بالصندوق", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = PdfAndShareHelper.formatCurrency(totalPaidCash, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    // Remaining Debt
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("الآجل / الديون", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = PdfAndShareHelper.formatCurrency(totalRemainingDebt, currency),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Search Bar & Filter Icon Menu
            item {
                var showFilterMenu by remember { mutableStateOf(false) }
                val filterLabel = when (filterType) {
                    "CASH" -> "نقداً"
                    "CREDIT" -> "آجل"
                    else -> "الكل"
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("بحث برقم الفاتورة، اسم الموزع، الفئة...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                        trailingIcon = {
                            Box {
                                IconButton(onClick = { showFilterMenu = true }) {
                                    Icon(
                                        Icons.Default.FilterList,
                                        contentDescription = "تصفية",
                                        tint = if (filterType != "ALL") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                DropdownMenu(
                                    expanded = showFilterMenu,
                                    onDismissRequest = { showFilterMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("📋 الكل ($totalSalesCount)") },
                                        onClick = {
                                            filterType = "ALL"
                                            showFilterMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("💵 نقداً بالكامل") },
                                        onClick = {
                                            filterType = "CASH"
                                            showFilterMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("💳 آجل / متبقي") },
                                        onClick = {
                                            filterType = "CREDIT"
                                            showFilterMenu = false
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_sales_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Sales Invoices List
            if (filteredSales.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "لا توجد فواتير مبيعات مطابقة للبحث" else "لا توجد فواتير مبيعات مسجلة حتى الآن",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "يمكنك استخدام زر (إنشاء فاتورة بيع جديدة) في الأعلى لتسجيل عملية بيع",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredSales, key = { it.id }) { sale ->
                    SalesInvoiceCard(
                        sale = sale,
                        currency = currency,
                        onReturn = { saleToReturn = sale },
                        onPreview = { invoiceToPreview = sale },
                        onEdit = { saleToEdit = sale },
                        onDelete = { saleToDelete = sale }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Sale Return Dialog (from card or header button)
    val targetReturnSale = saleToReturn
    if (showAddSaleReturnDialog || targetReturnSale != null) {
        SaleReturnDialog(
            sale = targetReturnSale,
            categories = categories,
            distributors = distributors,
            currency = currency,
            onDismiss = {
                showAddSaleReturnDialog = false
                saleToReturn = null
            },
            onConfirm = { distId, catId, qty, price, refundFromCash, nts ->
                viewModel.recordSaleReturn(
                    distributorId = distId,
                    categoryId = catId,
                    quantity = qty,
                    unitPrice = price,
                    refundFromCash = refundFromCash,
                    notes = nts
                )
                showAddSaleReturnDialog = false
                saleToReturn = null
            }
        )
    }

    // 1. Create Sale Dialog (Window)
    if (showCreateSaleDialog) {
        CreateSaleDialog(
            categories = categories,
            distributors = distributors,
            currency = currency,
            onDismiss = { showCreateSaleDialog = false },
            onSaveMulti = { distId, newCustName, newCustPhone, items, disc, paid, method, nts ->
                viewModel.recordMultiItemSale(
                    distributorId = distId,
                    newCustomerName = newCustName,
                    newCustomerPhone = newCustPhone,
                    items = items,
                    discount = disc,
                    paidAmount = paid,
                    paymentMethod = method,
                    notes = nts,
                    onSuccess = { newTx ->
                        showCreateSaleDialog = false
                        invoiceToPreview = newTx
                    }
                )
            }
        )
    }

    // 2. Edit Sale Dialog
    saleToEdit?.let { sale ->
        EditSaleDialog(
            sale = sale,
            currency = currency,
            onDismiss = { saleToEdit = null },
            onSave = { newQuantity, newUnitPrice, newDiscount, newPaidAmount, newPaymentMethod, newNotes ->
                viewModel.updateSale(
                    oldSale = sale,
                    newQuantity = newQuantity,
                    newUnitPrice = newUnitPrice,
                    newDiscount = newDiscount,
                    newPaidAmount = newPaidAmount,
                    newPaymentMethod = newPaymentMethod,
                    newNotes = newNotes
                )
                saleToEdit = null
            }
        )
    }

    // 3. Delete Sale Dialog
    saleToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
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
                    text = "تأكيد حذف فاتورة المبيعات",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "هل أنت متأكد من حذف فاتورة البيع (${sale.invoiceNumber}) للموزع (${sale.distributorName})؟",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "• سيتم إعادة ${sale.quantity} كرت إلى رصيد المخزن (${sale.categoryName}).",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Bold
                            )
                            if (sale.remainingAmount > 0) {
                                Text(
                                    text = "• سيتم إلغاء الدين المسجل على الموزع بقيمة ${PdfAndShareHelper.formatCurrency(sale.remainingAmount, currency)}.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (sale.paidAmount > 0) {
                                Text(
                                    text = "• سيتم استرجاع وخصم المبلغ المحصل (${PdfAndShareHelper.formatCurrency(sale.paidAmount, currency)}) من الصندوق.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSale(sale)
                        saleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_sale_button")
                ) {
                    Text("نعم، حذف واسترجاع المخزن")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { saleToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // 4. Invoice Preview & WhatsApp / Print Dialog
    invoiceToPreview?.let { tx ->
        val dist = distributors.find { it.id == tx.distributorId }
        InvoicePreviewDialog(
            transaction = tx,
            distributor = dist,
            networkName = networkName,
            currency = currency,
            items = invoiceItemsToPreview,
            onDismiss = { invoiceToPreview = null }
        )
    }
}

@Composable
fun SalesInvoiceCard(
    sale: TransactionRecord,
    currency: String,
    onReturn: () -> Unit,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sale_card_${sale.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Invoice Number, Distributor, Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = sale.invoiceNumber,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "${sale.distributorName} • ${PdfAndShareHelper.formatDate(sale.date)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (sale.remainingAmount <= 0) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (sale.remainingAmount <= 0) "مسدد بالكامل" else "متبقي دين",
                        color = if (sale.remainingAmount <= 0) Color(0xFF059669) else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Body
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الفئة المباعة:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${sale.categoryName} (${sale.quantity} كرت)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("سعر الكرت:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = PdfAndShareHelper.formatCurrency(sale.unitPrice, currency),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (sale.discount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الخصم الممنوح:", style = MaterialTheme.typography.bodySmall, color = Color(0xFF059669))
                            Text(
                                text = "- ${PdfAndShareHelper.formatCurrency(sale.discount, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("المسدد نقداً (بالصندوق):", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = PdfAndShareHelper.formatCurrency(sale.paidAmount, currency),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                    }

                    if (sale.remainingAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المتبقي آجل (على الحساب):", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = PdfAndShareHelper.formatCurrency(sale.remainingAmount, currency),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Total Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("إجمالي الفاتورة:", fontWeight = FontWeight.Bold)
                        Text(
                            text = PdfAndShareHelper.formatCurrency(sale.totalAmount, currency),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreview,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "معاينة ومشاركة الفاتورة",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedButton(
                    onClick = onReturn,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مرتجع", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعديل", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun CreateSaleDialog(
    categories: List<CardCategory>,
    distributors: List<Distributor>,
    currency: String,
    onDismiss: () -> Unit,
    onSaveMulti: (
        distId: Long?,
        newCustName: String?,
        newCustPhone: String?,
        items: List<InvoiceItemDraft>,
        discount: Double,
        paid: Double,
        method: String,
        notes: String
    ) -> Unit
) {
    // Current step: 1 = اختيار الأصناف والكميات، 2 = نافذة الدفع وتحديد العميل
    var currentStep by remember { mutableStateOf(1) }

    // List of items in invoice
    var invoiceItems by remember { mutableStateOf(listOf<InvoiceItemDraft>()) }

    // Item adding state
    var selectedCategory by remember { mutableStateOf<CardCategory?>(categories.firstOrNull()) }
    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }

    var categorySearchText by remember(selectedCategory) { mutableStateOf(selectedCategory?.name ?: "") }
    val filteredCategories = remember(categories, categorySearchText) {
        if (categorySearchText.isBlank()) {
            categories
        } else {
            categories.filter { it.name.contains(categorySearchText.trim(), ignoreCase = true) }
        }
    }

    var itemQuantityStr by remember { mutableStateOf("10") }
    var itemPriceStr by remember { mutableStateOf("") }
    var hasClearedItemQty by remember { mutableStateOf(false) }
    var hasClearedItemPrice by remember { mutableStateOf(false) }
    var catDropdownExpanded by remember { mutableStateOf(false) }

    // Customer & Payment state
    var isNewCustomer by remember { mutableStateOf(false) }
    var selectedDistributor by remember { mutableStateOf<Distributor?>(null) }
    var customerSearchText by remember { mutableStateOf("") }
    val filteredDistributors = remember(distributors, customerSearchText) {
        if (customerSearchText.isBlank()) {
            distributors
        } else {
            distributors.filter {
                it.name.contains(customerSearchText.trim(), ignoreCase = true) ||
                it.phone.contains(customerSearchText.trim())
            }
        }
    }
    var newCustomerName by remember { mutableStateOf("") }
    var newCustomerPhone by remember { mutableStateOf("") }
    var distDropdownExpanded by remember { mutableStateOf(false) }

    var paymentMethod by remember { mutableStateOf("CREDIT") } // CREDIT, CASH, TRANSFER
    var discountStr by remember { mutableStateOf("0") }
    var hasClearedDiscount by remember { mutableStateOf(false) }
    var paidAmountStr by remember { mutableStateOf("") }
    var hasClearedPaid by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    fun calculateCategoryPrice(cat: CardCategory?, dist: Distributor?): String {
        if (cat == null) return ""
        val discountPct = dist?.discountPercent ?: 0.0
        val finalPrice = if (discountPct > 0.0) {
            cat.sellingPrice * (1.0 - (discountPct / 100.0))
        } else {
            cat.sellingPrice
        }
        return if (finalPrice % 1.0 == 0.0) finalPrice.toInt().toString() else String.format(Locale.US, "%.2f", finalPrice)
    }

    LaunchedEffect(selectedCategory, selectedDistributor) {
        if (selectedCategory != null) {
            itemPriceStr = calculateCategoryPrice(selectedCategory, selectedDistributor)
            hasClearedItemPrice = false
        }
    }

    val subtotal = invoiceItems.sumOf { it.quantity * it.unitPrice }
    val discount = discountStr.toDoubleOrNull() ?: 0.0
    val totalAfterDiscount = (subtotal - discount).coerceAtLeast(0.0)
    val paidAmount = paidAmountStr.toDoubleOrNull() ?: if (paymentMethod == "CASH" || paymentMethod == "TRANSFER") totalAfterDiscount else 0.0
    val remainingDebt = (totalAfterDiscount - paidAmount).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ================= 1. FIXED TOP HEADER =================
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (currentStep == 1) Icons.Default.ShoppingCart else Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (currentStep == 1) "إنشاء فاتورة بيع كروت" else "سداد الفاتورة وتحديد العميل",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (currentStep == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = "1. الأصناف",
                                            color = if (currentStep == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text("◀", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (currentStep == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = "2. الدفع والعميل",
                                            color = if (currentStep == 2) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (currentStep == 2) {
                            TextButton(onClick = { currentStep = 1 }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("الأصناف")
                            }
                        } else {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "إغلاق")
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // ================= 2. SCROLLABLE MIDDLE SECTION =================
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (currentStep == 1) {
                        // 1. Searchable/Writable Category Field with Matching Results
                        Text("اختر أو اكتب فئة الكرت المراد بيعها:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = categorySearchText,
                                onValueChange = { query ->
                                    categorySearchText = query
                                    catDropdownExpanded = true
                                    val exact = categories.firstOrNull { it.name.equals(query.trim(), ignoreCase = true) }
                                    if (exact != null) {
                                        selectedCategory = exact
                                    }
                                },
                                label = { Text("بحث أو كتابة اسم الفئة...") },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingIcon = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (categorySearchText.isNotBlank()) {
                                            IconButton(
                                                onClick = {
                                                    categorySearchText = ""
                                                    selectedCategory = null
                                                    catDropdownExpanded = true
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        IconButton(
                                            onClick = { catDropdownExpanded = !catDropdownExpanded },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sale_category_search_input")
                                    .onFocusChanged {
                                        if (it.isFocused) {
                                            catDropdownExpanded = true
                                        }
                                    }
                            )

                            DropdownMenu(
                                expanded = catDropdownExpanded && (filteredCategories.isNotEmpty() || categorySearchText.isNotBlank()),
                                onDismissRequest = { catDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth(0.88f)
                            ) {
                                if (filteredCategories.isEmpty()) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "لا توجد نتائج مطابقة لـ \"$categorySearchText\"",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = { catDropdownExpanded = false }
                                    )
                                } else {
                                    filteredCategories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(cat.name, fontWeight = FontWeight.Bold)
                                                        Text(
                                                            "سعر البيع: ${PdfAndShareHelper.formatCurrency(cat.sellingPrice, currency)}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (cat.stockQuantity > 0) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = if (cat.stockQuantity > 0) "متوفر: ${cat.stockQuantity}" else "نفد",
                                                            color = if (cat.stockQuantity > 0) Color(0xFF047857) else Color(0xFFB91C1C),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedCategory = cat
                                                categorySearchText = cat.name
                                                catDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Selected category stock info pill
                        if (selectedCategory != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الفئة المحددة: ${selectedCategory!!.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "المتوفر بالمخزن: ${selectedCategory!!.stockQuantity} كرت",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selectedCategory!!.stockQuantity > 0) Color(0xFF15803D) else MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 2. Quantity & Price Fields (Centered and Auto-clearing on focus)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = itemQuantityStr,
                                onValueChange = { itemQuantityStr = it },
                                label = { Text("الكمية (كرت) *") },
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged {
                                        if (it.isFocused && !hasClearedItemQty && itemQuantityStr == "10") {
                                            itemQuantityStr = ""
                                            hasClearedItemQty = true
                                        }
                                    }
                                    .testTag("sale_item_qty_input")
                            )

                            OutlinedTextField(
                                value = itemPriceStr,
                                onValueChange = { itemPriceStr = it },
                                label = { Text("سعر الكرت ($currency) *") },
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged {
                                        if (it.isFocused && !hasClearedItemPrice) {
                                            val defaultPrice = selectedCategory?.sellingPrice?.let { p ->
                                                if (p % 1.0 == 0.0) p.toInt().toString() else p.toString()
                                            } ?: ""
                                            if (itemPriceStr == defaultPrice) {
                                                itemPriceStr = ""
                                                hasClearedItemPrice = true
                                            }
                                        }
                                    }
                                    .testTag("sale_item_price_input")
                            )
                        }

                        // 3. Add Item Button
                        Button(
                            onClick = {
                                val cat = selectedCategory
                                val qty = itemQuantityStr.toIntOrNull() ?: 0
                                val prc = itemPriceStr.toDoubleOrNull() ?: 0.0
                                if (cat != null && qty > 0 && prc > 0) {
                                    invoiceItems = invoiceItems + InvoiceItemDraft(
                                        categoryId = cat.id,
                                        categoryName = cat.name,
                                        quantity = qty,
                                        unitPrice = prc
                                    )
                                    itemQuantityStr = "10"
                                    hasClearedItemQty = false
                                }
                            },
                            enabled = selectedCategory != null && (itemQuantityStr.toIntOrNull() ?: 0) > 0 && (itemPriceStr.toDoubleOrNull() ?: 0.0) > 0,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("➕ إضافة الصنف إلى الفاتورة", fontWeight = FontWeight.Bold)
                        }

                        // ================= 4. SCROLLABLE ADDED ITEMS TABLE =================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "جدول الأصناف المضافة (${invoiceItems.size}):",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (invoiceItems.isNotEmpty()) {
                                Text(
                                    text = "${invoiceItems.sumOf { it.quantity }} كرت إجمالي",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // The Scrollable Table Container
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Table Header Row
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "الصنف", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(2.2f), textAlign = TextAlign.Start)
                                        Text(text = "السعر", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                                        Text(text = "الكمية", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                        Text(text = "الإجمالي", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                                        Spacer(modifier = Modifier.width(36.dp))
                                    }
                                }

                                if (invoiceItems.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp, horizontal = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Default.ShoppingCart,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                modifier = Modifier.size(32.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "لم تتم إضافة أي صنف بعد. اختر الفئة والكمية ثم اضغط 'إضافة الصنف'.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    // Scrollable table rows container
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 90.dp, max = 220.dp)
                                            .verticalScroll(rememberScrollState())
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            invoiceItems.forEachIndexed { index, item ->
                                                val rowBg = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                                Surface(
                                                    color = rowBg,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = item.categoryName,
                                                            fontWeight = FontWeight.SemiBold,
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(2.2f),
                                                            textAlign = TextAlign.Start,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = PdfAndShareHelper.formatCurrency(item.unitPrice, currency),
                                                            fontSize = 11.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.weight(1.1f),
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Text(
                                                            text = "${item.quantity}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface,
                                                            modifier = Modifier.weight(0.9f),
                                                            textAlign = TextAlign.Center
                                                        )
                                                        Text(
                                                            text = PdfAndShareHelper.formatCurrency(item.totalAmount, currency),
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.weight(1.3f),
                                                            textAlign = TextAlign.End
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                invoiceItems = invoiceItems.toMutableList().also { it.removeAt(index) }
                                                            },
                                                            modifier = Modifier.size(36.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Delete,
                                                                contentDescription = "حذف الصنف",
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // ================= STEP 2: PAYMENT & CUSTOMER WINDOW =================
                        // Brief summary banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("أصناف الفاتورة (${invoiceItems.size} صنف):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text(
                                    PdfAndShareHelper.formatCurrency(subtotal, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // 1. Payment Method (نوع البيع: آجل، نقدي، حوالة)
                        Text("نوع البيع / طريقة الدفع:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = paymentMethod == "CREDIT",
                                onClick = {
                                    paymentMethod = "CREDIT"
                                    paidAmountStr = "0"
                                },
                                label = { Text("على الحساب (آجل)") }
                            )
                            FilterChip(
                                selected = paymentMethod == "CASH",
                                onClick = {
                                    paymentMethod = "CASH"
                                    paidAmountStr = if (totalAfterDiscount % 1.0 == 0.0) totalAfterDiscount.toInt().toString() else totalAfterDiscount.toString()
                                },
                                label = { Text("نقداً بالكامل") }
                            )
                            FilterChip(
                                selected = paymentMethod == "TRANSFER",
                                onClick = {
                                    paymentMethod = "TRANSFER"
                                    paidAmountStr = if (totalAfterDiscount % 1.0 == 0.0) totalAfterDiscount.toInt().toString() else totalAfterDiscount.toString()
                                },
                                label = { Text("حوالة بنكية") }
                            )
                        }

                        // 2. Amount and Discount Fields (Centered & Auto-clearing on focus)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = discountStr,
                                onValueChange = {
                                    discountStr = it
                                    val discVal = it.toDoubleOrNull() ?: 0.0
                                    val net = (subtotal - discVal).coerceAtLeast(0.0)
                                    if (paymentMethod == "CASH" || paymentMethod == "TRANSFER") {
                                        paidAmountStr = if (net % 1.0 == 0.0) net.toInt().toString() else net.toString()
                                    }
                                },
                                label = { Text("الخصم ($currency)") },
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged {
                                        if (it.isFocused && !hasClearedDiscount && discountStr == "0") {
                                            discountStr = ""
                                            hasClearedDiscount = true
                                        }
                                    }
                                    .testTag("sale_discount_input")
                            )

                            OutlinedTextField(
                                value = paidAmountStr,
                                onValueChange = { paidAmountStr = it },
                                label = { Text("المبلغ المسدد ($currency)") },
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                placeholder = { Text(if (paymentMethod == "CASH") totalAfterDiscount.toString() else "0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged {
                                        if (it.isFocused && !hasClearedPaid) {
                                            val defaultVal = if (paymentMethod == "CASH" || paymentMethod == "TRANSFER") {
                                                if (totalAfterDiscount % 1.0 == 0.0) totalAfterDiscount.toInt().toString() else totalAfterDiscount.toString()
                                            } else "0"
                                            if (paidAmountStr == defaultVal) {
                                                paidAmountStr = ""
                                                hasClearedPaid = true
                                            }
                                        }
                                    }
                                    .testTag("sale_paid_input")
                            )
                        }

                        // 3. Customer Selection (Automatic search & create)
                        Text("العميل / الموزع:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                        if (!isNewCustomer) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = customerSearchText,
                                    onValueChange = { query ->
                                        customerSearchText = query
                                        distDropdownExpanded = true
                                        val exact = distributors.firstOrNull { it.name.equals(query.trim(), ignoreCase = true) }
                                        if (exact != null) {
                                            selectedDistributor = exact
                                        }
                                    },
                                    label = { Text("ابحث أو اكتب اسم العميل / الموزع...") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    },
                                    trailingIcon = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (customerSearchText.isNotBlank()) {
                                                IconButton(
                                                    onClick = {
                                                        customerSearchText = ""
                                                        selectedDistributor = null
                                                        distDropdownExpanded = true
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            IconButton(
                                                onClick = { distDropdownExpanded = !distDropdownExpanded },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("sale_customer_search_input")
                                        .onFocusChanged {
                                            if (it.isFocused) {
                                                distDropdownExpanded = true
                                            }
                                        }
                                )

                                DropdownMenu(
                                    expanded = distDropdownExpanded,
                                    onDismissRequest = { distDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.88f)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("عميل نقدي مباشر (بدون موزع)", fontWeight = FontWeight.SemiBold)
                                            }
                                        },
                                        onClick = {
                                            selectedDistributor = null
                                            customerSearchText = "عميل نقدي مباشر (بدون موزع)"
                                            distDropdownExpanded = false
                                        }
                                    )

                                    if (filteredDistributors.isEmpty() && customerSearchText.isNotBlank()) {
                                        DropdownMenuItem(
                                            text = {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        "لا يوجد عميل مسجل بهذا الاسم \"$customerSearchText\"",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                    Button(
                                                        onClick = {
                                                            newCustomerName = customerSearchText.trim()
                                                            isNewCustomer = true
                                                            distDropdownExpanded = false
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                                    ) {
                                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("➕ إضافه عميل جديد باسم \"$customerSearchText\"", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            },
                                            onClick = {}
                                        )
                                    } else {
                                        filteredDistributors.forEach { dist ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(dist.name, fontWeight = FontWeight.Bold)
                                                        Text(
                                                            "الرصيد: ${PdfAndShareHelper.formatCurrency(dist.currentBalance, currency)}${if (dist.phone.isNotBlank()) " • 📞 ${dist.phone}" else ""}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    selectedDistributor = dist
                                                    customerSearchText = dist.name
                                                    distDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (selectedDistributor != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "رصيد العميل الحالي: ${PdfAndShareHelper.formatCurrency(selectedDistributor!!.currentBalance, currency)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedDistributor!!.currentBalance > 0) MaterialTheme.colorScheme.error else Color(0xFF15803D)
                                        )
                                        if (selectedDistributor!!.creditLimit > 0) {
                                            Text(
                                                text = "الحد الائتماني: ${PdfAndShareHelper.formatCurrency(selectedDistributor!!.creditLimit, currency)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Enter New Customer Form
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("بيانات العميل الجديد:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    OutlinedTextField(
                                        value = newCustomerName,
                                        onValueChange = { newCustomerName = it },
                                        label = { Text("اسم العميل الجديد *") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = newCustomerPhone,
                                        onValueChange = { newCustomerPhone = it },
                                        label = { Text("رقم هاتف العميل (لإرسال إشعار الواتساب)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }
                        }

                        // 4. Notes
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات الفاتورة (اختياري)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 5. Final Summary Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("إجمالي الفاتورة الصافي:", fontWeight = FontWeight.Bold)
                                    Text(
                                        PdfAndShareHelper.formatCurrency(totalAfterDiscount, currency),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("المسدد للصندوق:", style = MaterialTheme.typography.bodySmall)
                                    Text(PdfAndShareHelper.formatCurrency(paidAmount, currency), style = MaterialTheme.typography.bodySmall)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("المتبقي آجل على العميل:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        PdfAndShareHelper.formatCurrency(remainingDebt, currency),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (remainingDebt > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // ================= 3. FIXED BOTTOM BAR (PINNED ACTIONS) =================
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (currentStep == 1) {
                        // Fixed Pay Button Row
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "إجمالي الأصناف: ${invoiceItems.sumOf { it.quantity }} كرت",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = PdfAndShareHelper.formatCurrency(subtotal, currency),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = {
                                    if (invoiceItems.isNotEmpty()) {
                                        if (paymentMethod == "CASH" || paymentMethod == "TRANSFER") {
                                            paidAmountStr = if (totalAfterDiscount % 1.0 == 0.0) totalAfterDiscount.toInt().toString() else totalAfterDiscount.toString()
                                        }
                                        currentStep = 2
                                    }
                                },
                                enabled = invoiceItems.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_proceed_to_payment"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "💳 دفع (${PdfAndShareHelper.formatCurrency(subtotal, currency)})",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    } else {
                        // Fixed Step 2 Action Buttons (Single full-width confirm and save button)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (invoiceItems.isNotEmpty()) {
                                        val finalDistId = if (isNewCustomer) null else selectedDistributor?.id
                                        val finalCustName = if (isNewCustomer) newCustomerName.trim() else null
                                        val finalCustPhone = if (isNewCustomer) newCustomerPhone.trim() else null
                                        onSaveMulti(
                                            finalDistId,
                                            finalCustName,
                                            finalCustPhone,
                                            invoiceItems,
                                            discount,
                                            paidAmount,
                                            paymentMethod,
                                            notes
                                        )
                                    }
                                },
                                enabled = invoiceItems.isNotEmpty() && (!isNewCustomer || newCustomerName.isNotBlank()),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_confirm_save_sale"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تأكيد وحفظ الفاتورة", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditSaleDialog(
    sale: TransactionRecord,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (newQuantity: Int, newUnitPrice: Double, newDiscount: Double, newPaidAmount: Double, newPaymentMethod: String, newNotes: String) -> Unit
) {
    var quantityStr by remember { mutableStateOf(sale.quantity.toString()) }
    var unitPriceStr by remember {
        mutableStateOf(if (sale.unitPrice % 1.0 == 0.0) sale.unitPrice.toInt().toString() else sale.unitPrice.toString())
    }
    var discountStr by remember {
        mutableStateOf(if (sale.discount % 1.0 == 0.0) sale.discount.toInt().toString() else sale.discount.toString())
    }
    var paidAmountStr by remember {
        mutableStateOf(if (sale.paidAmount % 1.0 == 0.0) sale.paidAmount.toInt().toString() else sale.paidAmount.toString())
    }
    var paymentMethod by remember { mutableStateOf(sale.paymentMethod) }
    var notes by remember { mutableStateOf(sale.notes) }

    val newQuantity = quantityStr.toIntOrNull() ?: sale.quantity
    val newUnitPrice = unitPriceStr.toDoubleOrNull() ?: sale.unitPrice
    val newDiscount = discountStr.toDoubleOrNull() ?: sale.discount
    val newSubtotal = newQuantity * newUnitPrice
    val newTotal = (newSubtotal - newDiscount).coerceAtLeast(0.0)
    val newPaid = paidAmountStr.toDoubleOrNull() ?: sale.paidAmount
    val newRemaining = (newTotal - newPaid).coerceAtLeast(0.0)

    val quantityDelta = newQuantity - sale.quantity
    val cashDelta = newPaid - sale.paidAmount
    val debtDelta = newRemaining - sale.remainingAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("تعديل فاتورة مبيعات (${sale.invoiceNumber})", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "الموزع: ${sale.distributorName} • الفئة: ${sale.categoryName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Quantity & Unit Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("الكمية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unitPriceStr,
                        onValueChange = { unitPriceStr = it },
                        label = { Text("سعر الكرت") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Discount & Paid Amount
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = discountStr,
                        onValueChange = { discountStr = it },
                        label = { Text("الخصم") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = paidAmountStr,
                        onValueChange = { paidAmountStr = it },
                        label = { Text("المسدد نقداً") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Summary & Balance Adjustments
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الإجمالي الجديد:")
                            Text(
                                PdfAndShareHelper.formatCurrency(newTotal, currency),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (quantityDelta != 0) {
                            Text(
                                text = "• فارق المخزون: ${if (quantityDelta > 0) "خصم إضافي -$quantityDelta" else "إعادة للمخزن +${-quantityDelta}"} كرت",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (cashDelta != 0.0) {
                            Text(
                                text = "• فارق الصندوق: ${if (cashDelta > 0) "إيداع +" else "استرداد -"}${PdfAndShareHelper.formatCurrency(kotlin.math.abs(cashDelta), currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (debtDelta != 0.0) {
                            Text(
                                text = "• فارق دين الموزع: ${if (debtDelta > 0) "زيادة دين +" else "تخفيض دين -"}${PdfAndShareHelper.formatCurrency(kotlin.math.abs(debtDelta), currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (debtDelta > 0) MaterialTheme.colorScheme.error else Color(0xFF059669)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newQuantity > 0 && newUnitPrice >= 0) {
                        onSave(newQuantity, newUnitPrice, newDiscount, newPaid, paymentMethod, notes)
                    }
                },
                enabled = newQuantity > 0
            ) {
                Text("حفظ وتسوية المخزن والصندوق")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun SaleReturnDialog(
    sale: TransactionRecord?,
    categories: List<CardCategory>,
    distributors: List<Distributor>,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (distId: Long?, catId: Long, qty: Int, price: Double, refundFromCash: Boolean, nts: String) -> Unit
) {
    var selectedDistributor by remember {
        mutableStateOf(
            if (sale != null && sale.distributorId != null) distributors.find { it.id == sale.distributorId }
            else null
        )
    }
    var distDropdownExpanded by remember { mutableStateOf(false) }

    var selectedCategory by remember {
        mutableStateOf(
            if (sale != null) categories.find { it.id == sale.categoryId } ?: categories.firstOrNull()
            else categories.firstOrNull()
        )
    }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var quantityStr by remember {
        mutableStateOf(sale?.quantity?.toString() ?: "10")
    }
    var unitPriceStr by remember {
        mutableStateOf(
            sale?.unitPrice?.let { if (it > 0) it.toInt().toString() else null }
                ?: selectedCategory?.sellingPrice?.toInt()?.toString() ?: "0"
        )
    }
    var refundFromCash by remember {
        mutableStateOf(sale?.distributorId == null || sale.paymentMethod == "CASH" || sale.remainingAmount <= 0.0)
    }
    var notes by remember {
        mutableStateOf(if (sale != null) "مرتجع من فاتورة بيع #${sale.invoiceNumber}" else "")
    }

    val quantity = quantityStr.toIntOrNull() ?: 0
    val unitPrice = unitPriceStr.toDoubleOrNull() ?: 0.0
    val totalAmount = quantity * unitPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (sale != null) "مرتجع مبيعات من فاتورة #${sale.invoiceNumber}" else "تسجيل مرتجع مبيعات كروت من عميل",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Distributor / Customer selection
                Text("الموزع / العميل:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedTextField(
                        value = selectedDistributor?.name ?: (if (sale?.distributorId == null && sale?.distributorName?.isNotBlank() == true) sale.distributorName else "عميل نقدي مباشر"),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = sale == null) { if (sale == null) distDropdownExpanded = true },
                        trailingIcon = {
                            if (sale == null) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { distDropdownExpanded = true })
                            }
                        }
                    )
                    if (sale == null) {
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
                                    text = { Text("${dist.name} (المستحق: ${PdfAndShareHelper.formatCurrency(dist.currentBalance, currency)})") },
                                    onClick = {
                                        selectedDistributor = dist
                                        refundFromCash = false
                                        distDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Category selection
                Text("فئة الكروت المرتجعة للمخزن:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "اختر الفئة...",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = sale == null) { if (sale == null) categoryDropdownExpanded = true },
                        trailingIcon = {
                            if (sale == null) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { categoryDropdownExpanded = true })
                            }
                        }
                    )
                    if (sale == null) {
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${cat.name} (المتوفر: ${cat.stockQuantity})") },
                                    onClick = {
                                        selectedCategory = cat
                                        unitPriceStr = cat.sellingPrice.toInt().toString()
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Quantity and Unit Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("الكمية المرتجعة *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        isError = quantity <= 0
                    )

                    OutlinedTextField(
                        value = unitPriceStr,
                        onValueChange = { unitPriceStr = it },
                        label = { Text("سعر البيع ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Settlement mode
                Text("طريقة تسوية المرتجع المالية:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !refundFromCash && selectedDistributor != null,
                        onClick = {
                            if (selectedDistributor != null) refundFromCash = false
                        },
                        enabled = selectedDistributor != null,
                        label = { Text("خصم من ديون الموزع") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = refundFromCash,
                        onClick = { refundFromCash = true },
                        label = { Text("رد نقد من الصندوق") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("سبب المرتجع / ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Impact details
                Surface(
                    color = Color(0xFF0284C7).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي قيمة المرتجع:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = PdfAndShareHelper.formatCurrency(totalAmount, currency),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                        }
                        Text(
                            text = "• سيتم إعادة $quantity كرت إلى رصيد المخزن تلقائياً.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.Bold
                        )
                        if (refundFromCash) {
                            Text(
                                text = "• سيتم صرف ${PdfAndShareHelper.formatCurrency(totalAmount, currency)} نقداً من الصندوق الرئيسي لحساب العميل.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFC2410C),
                                fontWeight = FontWeight.Medium
                            )
                        } else if (selectedDistributor != null) {
                            Text(
                                text = "• سيتم خصم وتنزيل ${PdfAndShareHelper.formatCurrency(totalAmount, currency)} من مديونية وحساب الموزع (${selectedDistributor!!.name}).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cat = selectedCategory
                    if (cat != null && quantity > 0) {
                        onConfirm(
                            selectedDistributor?.id,
                            cat.id,
                            quantity,
                            unitPrice,
                            refundFromCash || selectedDistributor == null,
                            notes
                        )
                    }
                },
                enabled = selectedCategory != null && quantity > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text("تأكيد تسجيل مرتجع المبيعات")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
