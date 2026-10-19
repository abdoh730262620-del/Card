package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.FilterChip
import java.util.Locale
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CardCategory
import com.example.data.model.TransactionRecord
import com.example.data.model.InvoiceItemDraft
import com.example.data.model.TransactionItem
import com.example.ui.NetCardsViewModel
import com.example.ui.components.InvoicePreviewDialog
import com.example.util.PdfAndShareHelper
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.focus.onFocusChanged

@Composable
fun PurchasesScreen(
    viewModel: NetCardsViewModel
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val purchaseInvoices by viewModel.purchaseTransactions.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"

    // Search query
    var searchQuery by remember { mutableStateOf("") }

    // Dialog states
    var showAddPurchaseDialog by remember { mutableStateOf(false) }
    var showAddPurchaseReturnDialog by remember { mutableStateOf(false) }
    var purchaseToReturn by remember { mutableStateOf<TransactionRecord?>(null) }
    var purchaseToEdit by remember { mutableStateOf<TransactionRecord?>(null) }
    var purchaseToDelete by remember { mutableStateOf<TransactionRecord?>(null) }
    var showSuppliersListDialog by remember { mutableStateOf(false) }
    var viewingSupplierStatement by remember { mutableStateOf<String?>(null) }
    var invoiceToPreview by remember { mutableStateOf<TransactionRecord?>(null) }
    var invoiceItemsToPreview by remember { mutableStateOf<List<TransactionItem>>(emptyList()) }

    LaunchedEffect(invoiceToPreview) {
        invoiceItemsToPreview = if (invoiceToPreview != null) {
            viewModel.getTransactionItems(invoiceToPreview!!.id)
        } else {
            emptyList()
        }
    }

    // Filtered invoices
    val filteredInvoices = purchaseInvoices.filter { inv ->
        val query = searchQuery.trim().lowercase()
        if (query.isEmpty()) true
        else {
            inv.invoiceNumber.lowercase().contains(query) ||
            inv.categoryName.lowercase().contains(query) ||
            inv.distributorName.lowercase().contains(query) ||
            inv.notes.lowercase().contains(query)
        }
    }.sortedByDescending { it.date }

    // Totals for purchases
    val totalPurchasesAmount = purchaseInvoices.sumOf { it.totalAmount }
    val totalPurchasedCards = purchaseInvoices.sumOf { it.quantity }
    val totalInvoicesCount = purchaseInvoices.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("purchases_screen")
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
                        onClick = { showAddPurchaseDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_add_purchase_header")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة فاتورة مشتريات", fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = { showAddPurchaseReturnDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEA580C)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_purchase_return_header")
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مرتجع للمورد", fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { showSuppliersListDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_suppliers_statement_header")
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("كشف حساب ومعاينة الموردين (واتساب وبرامج الجوال)", fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }

            // Summary Statistics Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Spending Card
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "إجمالي المشتريات",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = PdfAndShareHelper.formatCurrency(totalPurchasesAmount, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Total Cards Procured
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = Color(0xFF059669),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "الكروت الموردة",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalPurchasedCards كرت",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    // Invoices Count
                    Surface(
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        tonalElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "الفواتير",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$totalInvoicesCount فاتورة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("بحث برقم الفاتورة، اسم المورد، الفئة...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "بحث")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_purchases_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Invoices List
            if (filteredInvoices.isEmpty()) {
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
                                text = if (searchQuery.isNotBlank()) "لا توجد فواتير مشتريات مطابقة للبحث" else "لا توجد فواتير مشتريات مسجلة حتى الآن",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "يمكنك استخدام زر (إضافة فاتورة مشتريات جديدة) في الأعلى لتسجيل التوريد",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredInvoices, key = { it.id }) { invoice ->
                    PurchaseInvoiceCard(
                        invoice = invoice,
                        currency = currency,
                        onReturn = { purchaseToReturn = invoice },
                        onEdit = { purchaseToEdit = invoice },
                        onDelete = { purchaseToDelete = invoice },
                        onShare = {
                            invoiceToPreview = invoice
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Purchase Return Dialog (from card or header button)
    val targetReturnInvoice = purchaseToReturn
    if (showAddPurchaseReturnDialog || targetReturnInvoice != null) {
        PurchaseReturnDialog(
            invoice = targetReturnInvoice,
            categories = categories,
            currency = currency,
            onDismiss = {
                showAddPurchaseReturnDialog = false
                purchaseToReturn = null
            },
            onConfirm = { categoryId, quantity, costPrice, supplierName, notes ->
                viewModel.recordPurchaseReturn(
                    categoryId = categoryId,
                    quantity = quantity,
                    costPrice = costPrice,
                    supplierName = supplierName,
                    refundToFund = true,
                    notes = notes
                )
                showAddPurchaseReturnDialog = false
                purchaseToReturn = null
            }
        )
    }

    // 1. Add Purchase Dialog
    if (showAddPurchaseDialog) {
        val knownSuppliers = purchaseInvoices.map { it.distributorName.ifBlank { "المورد الرئيسي" } }.distinct()
        AddPurchaseDialog(
            categories = categories,
            knownSuppliers = if (knownSuppliers.isEmpty()) listOf("المورد الرئيسي") else knownSuppliers,
            currency = currency,
            onDismiss = { showAddPurchaseDialog = false },
            onSaveMulti = { items, discount, paidAmount, paymentMethod, supplierName, notes ->
                viewModel.recordMultiItemPurchase(
                    items = items,
                    discount = discount,
                    paidAmount = paidAmount,
                    paymentMethod = paymentMethod,
                    supplierName = supplierName,
                    notes = notes,
                    onSuccess = { newTx ->
                        showAddPurchaseDialog = false
                        invoiceToPreview = newTx
                    }
                )
            }
        )
    }

    // Purchase Invoice Preview & WhatsApp / Print Dialog
    invoiceToPreview?.let { tx ->
        InvoicePreviewDialog(
            transaction = tx,
            distributor = null,
            networkName = settings?.networkName ?: "شبكة كروت نت",
            currency = currency,
            items = invoiceItemsToPreview,
            onDismiss = { invoiceToPreview = null }
        )
    }

    // 2. Edit Purchase Dialog
    purchaseToEdit?.let { invoice ->
        EditPurchaseDialog(
            invoice = invoice,
            currency = currency,
            onDismiss = { purchaseToEdit = null },
            onSave = { newQuantity, newCostPrice, newSupplierName, newNotes ->
                viewModel.updatePurchase(
                    oldPurchase = invoice,
                    newQuantity = newQuantity,
                    newCostPrice = newCostPrice,
                    newSupplierName = newSupplierName,
                    newNotes = newNotes
                )
                purchaseToEdit = null
            }
        )
    }

    // 3. Delete Purchase Confirmation Dialog
    purchaseToDelete?.let { invoice ->
        AlertDialog(
            onDismissRequest = { purchaseToDelete = null },
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
                    text = "تأكيد حذف فاتورة المشتريات",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "هل أنت متأكد من حذف فاتورة الشراء (${invoice.invoiceNumber})؟",
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
                                text = "• الفئة: ${invoice.categoryName} (${invoice.quantity} كرت)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• سيتم خصم ${invoice.quantity} كرت من رصيد المخزن الحالي.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "• سيتم استرجاع مبلغ الشراء (${PdfAndShareHelper.formatCurrency(invoice.totalAmount, currency)}) وإيداعه في الصندوق الرئيسي تلقائياً.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePurchase(invoice)
                        purchaseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_purchase_button")
                ) {
                    Text("نعم، حذف واسترداد المبلغ")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { purchaseToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Suppliers List Dialog & Supplier Statement Preview Dialog
    if (showSuppliersListDialog) {
        val suppliers = remember(purchaseInvoices) {
            val list = purchaseInvoices.map { it.distributorName.ifBlank { "المورد الرئيسي" } }.toMutableSet()
            list.add("المورد الرئيسي")
            list.toList()
        }

        AlertDialog(
            onDismissRequest = { showSuppliersListDialog = false },
            title = { Text("اختر المورد لعرض كشف الحساب والمعاينة", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (suppliers.isEmpty()) {
                        Text("لا توجد توريدات أو موردين مسجلين بعد.")
                    } else {
                        suppliers.forEach { supName ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showSuppliersListDialog = false
                                        viewingSupplierStatement = supName
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = supName, fontWeight = FontWeight.Bold)
                                    Text(text = "عرض كشف الحساب ⬅", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSuppliersListDialog = false }) {
                    Text("إغلاق")
                }
            }
        )
    }

    if (viewingSupplierStatement != null) {
        val supName = viewingSupplierStatement!!
        val supTx = purchaseInvoices.filter {
            val name = it.distributorName.ifBlank { "المورد الرئيسي" }
            name == supName || (supName == "المورد الرئيسي" && it.distributorName.isBlank())
        }
        com.example.ui.components.AccountStatementPreviewDialog(
            title = "كشف حساب المورد: $supName",
            accountName = supName,
            phone = "",
            openingBalance = 0.0,
            creditLimit = 0.0,
            currency = currency,
            networkName = "شبكة كروت نت",
            transactions = supTx,
            isSupplier = true,
            supplierName = supName,
            onDismiss = { viewingSupplierStatement = null }
        )
    }
}

@Composable
fun PurchaseInvoiceCard(
    invoice: TransactionRecord,
    currency: String,
    onReturn: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("purchase_card_${invoice.id}"),
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
            // Header Row: Invoice Number, Date, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(28.dp)
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
                            text = invoice.invoiceNumber,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = PdfAndShareHelper.formatDate(invoice.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "صرف من الصندوق",
                        color = Color(0xFF059669),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Details Body
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
                        Text("الفئة المشتراة:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = invoice.categoryName,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الكمية والتكلفة:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "${invoice.quantity} كرت × ${PdfAndShareHelper.formatCurrency(invoice.unitPrice, currency)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("المورد / الجهة:", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = invoice.distributorName.ifBlank { "المورد الرئيسي" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (invoice.notes.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ملاحظات:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = invoice.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Total Amount
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("إجمالي الفاتورة المدفوع:", fontWeight = FontWeight.Bold)
                        Text(
                            text = PdfAndShareHelper.formatCurrency(invoice.totalAmount, currency),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "مشاركة الفاتورة",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedButton(
                    onClick = onReturn,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEA580C)),
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
fun AddPurchaseDialog(
    categories: List<CardCategory>,
    knownSuppliers: List<String>,
    currency: String,
    onDismiss: () -> Unit,
    onSaveMulti: (
        items: List<InvoiceItemDraft>,
        discount: Double,
        paid: Double,
        method: String,
        supplierName: String,
        notes: String
    ) -> Unit
) {
    // Current step: 1 = اختيار الأصناف والكميات، 2 = نافذة الدفع وتحديد المورد
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

    var itemQuantityStr by remember { mutableStateOf("100") }
    var itemCostPriceStr by remember { mutableStateOf("") }
    var hasClearedItemQty by remember { mutableStateOf(false) }
    var hasClearedItemCost by remember { mutableStateOf(false) }
    var catDropdownExpanded by remember { mutableStateOf(false) }

    fun calculateCategoryCost(cat: CardCategory?): String {
        if (cat == null) return ""
        val cost = cat.costPrice
        return if (cost % 1.0 == 0.0) cost.toInt().toString() else String.format(Locale.US, "%.2f", cost)
    }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory != null) {
            itemCostPriceStr = calculateCategoryCost(selectedCategory)
            hasClearedItemCost = false
        }
    }

    // Supplier & Payment state
    var isNewSupplier by remember { mutableStateOf(false) }
    var selectedSupplierName by remember { mutableStateOf(knownSuppliers.firstOrNull() ?: "المورد الرئيسي") }
    var supplierSearchText by remember(selectedSupplierName) { mutableStateOf(selectedSupplierName) }
    val filteredSuppliers = remember(knownSuppliers, supplierSearchText) {
        if (supplierSearchText.isBlank()) {
            knownSuppliers
        } else {
            knownSuppliers.filter { it.contains(supplierSearchText.trim(), ignoreCase = true) }
        }
    }
    var newSupplierName by remember { mutableStateOf("") }
    var supplierDropdownExpanded by remember { mutableStateOf(false) }

    var paymentMethod by remember { mutableStateOf("CASH") } // CASH, CREDIT, TRANSFER
    var discountStr by remember { mutableStateOf("0") }
    var hasClearedDiscount by remember { mutableStateOf(false) }
    var paidAmountStr by remember { mutableStateOf("") }
    var hasClearedPaid by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

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
                                        if (currentStep == 1) Icons.Default.ReceiptLong else Icons.Default.Payments,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (currentStep == 1) "فاتورة مشتريات وتوريد كروت" else "سداد الفاتورة وتحديد المورد",
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
                                            text = "2. الدفع والمورد",
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
                        Text("اختر أو اكتب فئة الكرت المراد توريدها:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
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
                                    .testTag("purchase_category_search_input")
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
                                                            "سعر التكلفة: ${PdfAndShareHelper.formatCurrency(cat.costPrice, currency)}",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = "متوفر: ${cat.stockQuantity}",
                                                            color = Color(0xFF047857),
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

                        // Selected category info pill
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
                                        text = "المتوفر حالياً: ${selectedCategory!!.stockQuantity} كرت",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF15803D),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 2. Quantity & Cost Price Row (Centered & Auto-clearing on focus)
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
                                        if (it.isFocused && !hasClearedItemQty && itemQuantityStr == "100") {
                                            itemQuantityStr = ""
                                            hasClearedItemQty = true
                                        }
                                    }
                                    .testTag("purchase_quantity_input")
                            )

                            OutlinedTextField(
                                value = itemCostPriceStr,
                                onValueChange = { itemCostPriceStr = it },
                                label = { Text("سعر التكلفة ($currency) *") },
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged {
                                        if (it.isFocused && !hasClearedItemCost) {
                                            val defaultCost = selectedCategory?.costPrice?.let { p ->
                                                if (p % 1.0 == 0.0) p.toInt().toString() else p.toString()
                                            } ?: ""
                                            if (itemCostPriceStr == defaultCost) {
                                                itemCostPriceStr = ""
                                                hasClearedItemCost = true
                                            }
                                        }
                                    }
                                    .testTag("purchase_cost_input")
                            )
                        }

                        // 3. Add Item Button
                        Button(
                            onClick = {
                                val cat = selectedCategory
                                val qty = itemQuantityStr.toIntOrNull() ?: 0
                                val cost = itemCostPriceStr.toDoubleOrNull() ?: 0.0
                                if (cat != null && qty > 0 && cost >= 0) {
                                    invoiceItems = invoiceItems + InvoiceItemDraft(
                                        categoryId = cat.id,
                                        categoryName = cat.name,
                                        quantity = qty,
                                        unitPrice = cost
                                    )
                                    itemQuantityStr = "100"
                                    hasClearedItemQty = false
                                }
                            },
                            enabled = selectedCategory != null && (itemQuantityStr.toIntOrNull() ?: 0) > 0 && (itemCostPriceStr.toDoubleOrNull() ?: 0.0) >= 0,
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
                                                Icons.Default.ReceiptLong,
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
                        // ================= STEP 2: PAYMENT & SUPPLIER WINDOW =================
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
                                Text("أصناف المشتريات (${invoiceItems.size} صنف):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text(
                                    PdfAndShareHelper.formatCurrency(subtotal, currency),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // 1. Payment Method (نوع الشراء: نقدي، آجل للمورد، حوالة)
                        Text("نوع الشراء / طريقة السداد:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = paymentMethod == "CASH",
                                onClick = {
                                    paymentMethod = "CASH"
                                    paidAmountStr = if (totalAfterDiscount % 1.0 == 0.0) totalAfterDiscount.toInt().toString() else totalAfterDiscount.toString()
                                },
                                label = { Text("نقداً (خصم من الصندوق)") }
                            )
                            FilterChip(
                                selected = paymentMethod == "CREDIT",
                                onClick = {
                                    paymentMethod = "CREDIT"
                                    paidAmountStr = "0"
                                },
                                label = { Text("على الحساب (آجل للمورد)") }
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
                                label = { Text("خصم الشراء ($currency)") },
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
                                    .testTag("purchase_discount_input")
                            )

                            OutlinedTextField(
                                value = paidAmountStr,
                                onValueChange = { paidAmountStr = it },
                                label = { Text("المبلغ المدفوع ($currency)") },
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
                                    .testTag("purchase_paid_input")
                            )
                        }

                        // 3. Supplier Selection (Automatic search & create)
                        Text("المورد / الشركة:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)

                        if (!isNewSupplier) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = supplierSearchText,
                                    onValueChange = { query ->
                                        supplierSearchText = query
                                        supplierDropdownExpanded = true
                                        val exact = knownSuppliers.firstOrNull { it.equals(query.trim(), ignoreCase = true) }
                                        if (exact != null) {
                                            selectedSupplierName = exact
                                        }
                                    },
                                    label = { Text("ابحث أو اكتب اسم المورد المسجل...") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    },
                                    trailingIcon = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (supplierSearchText.isNotBlank()) {
                                                IconButton(
                                                    onClick = {
                                                        supplierSearchText = ""
                                                        selectedSupplierName = ""
                                                        supplierDropdownExpanded = true
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = "مسح", modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            IconButton(
                                                onClick = { supplierDropdownExpanded = !supplierDropdownExpanded },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("purchase_supplier_search_input")
                                        .onFocusChanged {
                                            if (it.isFocused) {
                                                supplierDropdownExpanded = true
                                            }
                                        }
                                )

                                DropdownMenu(
                                    expanded = supplierDropdownExpanded,
                                    onDismissRequest = { supplierDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.88f)
                                ) {
                                    if (filteredSuppliers.isEmpty() && supplierSearchText.isNotBlank()) {
                                        DropdownMenuItem(
                                            text = {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        "لا يوجد مورد مسجل بهذا الاسم \"$supplierSearchText\"",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                    Button(
                                                        onClick = {
                                                            newSupplierName = supplierSearchText.trim()
                                                            isNewSupplier = true
                                                            supplierDropdownExpanded = false
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(8.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                                    ) {
                                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("➕ إضافه مورد جديد باسم \"$supplierSearchText\"", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            },
                                            onClick = {}
                                        )
                                    } else {
                                        filteredSuppliers.forEach { sup ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(sup, fontWeight = FontWeight.Bold)
                                                    }
                                                },
                                                onClick = {
                                                    selectedSupplierName = sup
                                                    supplierSearchText = sup
                                                    supplierDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (selectedSupplierName.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "المورد المختار: $selectedSupplierName",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        } else {
                            // Enter New Supplier Form
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("بيانات المورد الجديد:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    OutlinedTextField(
                                        value = newSupplierName,
                                        onValueChange = { newSupplierName = it },
                                        label = { Text("اسم المورد الجديد *") },
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
                                    Text("إجمالي المشتريات الصافي:", fontWeight = FontWeight.Bold)
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
                                    Text("المسدد (خصم من الصندوق):", style = MaterialTheme.typography.bodySmall)
                                    Text(PdfAndShareHelper.formatCurrency(paidAmount, currency), style = MaterialTheme.typography.bodySmall)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("المتبقي دين للمورد:", style = MaterialTheme.typography.bodySmall)
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
                                    .testTag("btn_proceed_to_purchase_payment"),
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
                                        val finalSupplier = if (isNewSupplier) newSupplierName.trim() else selectedSupplierName
                                        onSaveMulti(
                                            invoiceItems,
                                            discount,
                                            paidAmount,
                                            paymentMethod,
                                            finalSupplier.ifBlank { "المورد الرئيسي" },
                                            notes
                                        )
                                    }
                                },
                                enabled = invoiceItems.isNotEmpty() && (!isNewSupplier || newSupplierName.isNotBlank()),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_confirm_save_purchase"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("تأكيد وحفظ فاتورة الشراء", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditPurchaseDialog(
    invoice: TransactionRecord,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (newQuantity: Int, newCostPrice: Double, newSupplierName: String, newNotes: String) -> Unit
) {
    var quantityStr by remember { mutableStateOf(invoice.quantity.toString()) }
    var costPriceStr by remember {
        mutableStateOf(if (invoice.unitPrice % 1.0 == 0.0) invoice.unitPrice.toInt().toString() else invoice.unitPrice.toString())
    }
    var supplierName by remember { mutableStateOf(invoice.distributorName) }
    var notes by remember { mutableStateOf(invoice.notes) }

    val newQuantity = quantityStr.toIntOrNull() ?: invoice.quantity
    val newCostPrice = costPriceStr.toDoubleOrNull() ?: invoice.unitPrice
    val newTotal = newQuantity * newCostPrice

    val quantityDelta = newQuantity - invoice.quantity
    val costDelta = newTotal - invoice.totalAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("تعديل فاتورة مشتريات (${invoice.invoiceNumber})", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "الفئة: ${invoice.categoryName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Quantity & Cost Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("الكمية (كرت)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it },
                        label = { Text("سعر التكلفة ($currency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Supplier
                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("اسم المورد / الشركة") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Difference details
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
                                text = "• فارق الكمية بالمخزن: ${if (quantityDelta > 0) "+$quantityDelta" else "$quantityDelta"} كرت",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (quantityDelta > 0) Color(0xFF059669) else MaterialTheme.colorScheme.error
                            )
                        }
                        if (costDelta != 0.0) {
                            Text(
                                text = "• فارق تسوية الصندوق: ${if (costDelta > 0) "خصم إضافي " else "استرجاع للصندوق "} ${PdfAndShareHelper.formatCurrency(kotlin.math.abs(costDelta), currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newQuantity > 0 && newCostPrice >= 0) {
                        onSave(newQuantity, newCostPrice, supplierName, notes)
                    }
                },
                enabled = newQuantity > 0
            ) {
                Text("حفظ التعديلات وتسوية الصندوق")
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
fun PurchaseReturnDialog(
    invoice: TransactionRecord?,
    categories: List<CardCategory>,
    currency: String,
    onDismiss: () -> Unit,
    onConfirm: (categoryId: Long, quantity: Int, costPrice: Double, supplierName: String, notes: String) -> Unit
) {
    var selectedCategory by remember {
        mutableStateOf(
            if (invoice != null) categories.find { it.id == invoice.categoryId } ?: categories.firstOrNull()
            else categories.firstOrNull()
        )
    }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var quantityStr by remember {
        mutableStateOf(invoice?.quantity?.toString() ?: "10")
    }
    var costPriceStr by remember {
        mutableStateOf(
            invoice?.unitPrice?.let { if (it > 0) it.toInt().toString() else null }
                ?: selectedCategory?.costPrice?.toInt()?.toString() ?: "0"
        )
    }
    var supplierName by remember {
        mutableStateOf(invoice?.distributorName?.ifBlank { "المورد الرئيسي" } ?: "المورد الرئيسي")
    }
    var notes by remember { mutableStateOf(if (invoice != null) "مرتجع من فاتورة #${invoice.invoiceNumber}" else "") }

    val quantity = quantityStr.toIntOrNull() ?: 0
    val costPrice = costPriceStr.toDoubleOrNull() ?: 0.0
    val totalRefund = quantity * costPrice
    val currentStock = selectedCategory?.stockQuantity ?: 0
    val isStockSufficient = currentStock >= quantity

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (invoice != null) "مرتجع مشتريات من فاتورة #${invoice.invoiceNumber}" else "تسجيل مرتجع مشتريات كروت للمورد",
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
                // Category selection
                Text("فئة الكروت المرتجعة:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "اختر الفئة...",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = invoice == null) { if (invoice == null) categoryDropdownExpanded = true },
                        trailingIcon = {
                            if (invoice == null) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { categoryDropdownExpanded = true })
                            }
                        }
                    )
                    if (invoice == null) {
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${cat.name} (المتوفر بالمخزن: ${cat.stockQuantity})") },
                                    onClick = {
                                        selectedCategory = cat
                                        costPriceStr = cat.costPrice.toInt().toString()
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Available stock indicator
                Surface(
                    color = if (isStockSufficient) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الرصيد المتوفر بالمخزن حالياً:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "$currentStock كرت",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isStockSufficient) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }

                // Quantity and Cost Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("كمية المرتجع (كرت) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        isError = quantity <= 0 || !isStockSufficient
                    )

                    OutlinedTextField(
                        value = costPriceStr,
                        onValueChange = { costPriceStr = it },
                        label = { Text("سعر التكلفة ($currency) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!isStockSufficient) {
                    Text(
                        text = "⚠️ الكمية المطلوبة ($quantity) أكبر من المتوفر بالمخزن ($currentStock)",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Supplier
                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("اسم المورد / الشركة") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("سبب المرتجع / ملاحظات") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Impact Banner
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("إجمالي المبلغ المسترد للصندوق:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = PdfAndShareHelper.formatCurrency(totalRefund, currency),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                        Text(
                            text = "💡 سيتم خصم $quantity كرت من المخزن، وإيداع المبلغ فوراً في الصندوق الرئيسي كحركة استرداد نقدية.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cat = selectedCategory
                    if (cat != null && quantity > 0 && isStockSufficient) {
                        onConfirm(cat.id, quantity, costPrice, supplierName, notes)
                    }
                },
                enabled = selectedCategory != null && quantity > 0 && isStockSufficient,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C))
            ) {
                Text("تأكيد إرجاع المشتريات وإيداع المبلغ")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}


