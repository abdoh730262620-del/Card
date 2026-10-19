package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CardCategory
import com.example.data.model.CardSwapRecord
import com.example.data.model.Distributor
import com.example.data.model.EmployeeStock
import com.example.data.model.EmployeeTransferRecord
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import com.example.data.model.InvoiceItemDraft
import com.example.ui.NetCardsViewModel
import com.example.util.PdfAndShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WarehousesScreen(
    viewModel: NetCardsViewModel,
    initialTab: Int = 0
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val employeeStocks by viewModel.employeeStocks.collectAsStateWithLifecycle()
    val employeeTransfers by viewModel.employeeTransfers.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val distributors by viewModel.distributors.collectAsStateWithLifecycle()
    val cardSwaps by viewModel.cardSwaps.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val selectedTabFromVm by viewModel.warehouseSelectedTab.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"
    val networkName = settings?.networkName ?: "شبكة كروت نت"

    var selectedMainTab by remember { mutableIntStateOf(initialTab.coerceIn(0, 2)) }
    var selectedEmployeeForDetail by remember { mutableStateOf<String?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }

    var transferInitialFromEmployee by remember { mutableStateOf<String?>(null) }
    var transferInitialToEmployee by remember { mutableStateOf<String?>(null) }
    var transferInitialCategory by remember { mutableStateOf<CardCategory?>(null) }

    LaunchedEffect(selectedTabFromVm) {
        selectedMainTab = selectedTabFromVm
    }

    val warehouseOwners = remember(users) {
        (listOf("المدير العام") + users.filter { it.displayName != "المدير العام" }.map { it.displayName }).distinct()
    }

    // When an employee's warehouse is opened:
    if (selectedEmployeeForDetail != null) {
        EmployeeWarehouseDetailView(
            employeeName = selectedEmployeeForDetail!!,
            users = users,
            categories = categories,
            employeeStocks = employeeStocks,
            employeeTransfers = employeeTransfers,
            transactions = transactions,
            currency = currency,
            networkName = networkName,
            onBack = { selectedEmployeeForDetail = null },
            onInitiateTransfer = { fromEmp, toEmp, cat ->
                transferInitialFromEmployee = fromEmp
                transferInitialToEmployee = toEmp
                transferInitialCategory = cat
                showTransferDialog = true
            },
            onNavigateToTab = { tabIdx ->
                selectedEmployeeForDetail = null
                selectedMainTab = tabIdx
                viewModel.setWarehouseSelectedTab(tabIdx)
            }
        )
    } else {
        // Main Screen View with Top Tabs (المخازن، التحويلات، الاستبدال)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("warehouses_screen")
        ) {
            // Main Top Tabs Bar
            PrimaryTabRow(
                selectedTabIndex = selectedMainTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedMainTab == 0,
                    onClick = {
                        selectedMainTab = 0
                        viewModel.setWarehouseSelectedTab(0)
                    },
                    text = { Text("مخازن الموظفين", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedMainTab == 1,
                    onClick = {
                        selectedMainTab = 1
                        viewModel.setWarehouseSelectedTab(1)
                    },
                    text = { Text("تحويلات المخازن", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedMainTab == 2,
                    onClick = {
                        selectedMainTab = 2
                        viewModel.setWarehouseSelectedTab(2)
                    },
                    text = { Text("استبدال الكروت", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                when (selectedMainTab) {
                    0 -> {
                        // TAB 0: WAREHOUSES PER EMPLOYEE
                        EmployeeWarehousesListTab(
                            warehouseOwners = warehouseOwners,
                            users = users,
                            categories = categories,
                            employeeStocks = employeeStocks,
                            currency = currency,
                            onEmployeeClick = { employeeName -> selectedEmployeeForDetail = employeeName }
                        )
                    }
                    1 -> {
                        // TAB 1: TRANSFERS
                        WarehouseTransfersTab(
                            transfers = employeeTransfers,
                            currency = currency,
                            onNewTransferClick = {
                                transferInitialFromEmployee = null
                                transferInitialToEmployee = null
                                transferInitialCategory = null
                                showTransferDialog = true
                            }
                        )
                    }
                    2 -> {
                        // TAB 2: CARD SWAP
                        WarehouseCardSwapTab(
                            viewModel = viewModel,
                            distributors = distributors,
                            categories = categories,
                            swaps = cardSwaps,
                            currency = currency
                        )
                    }
                }

                // Floating Action Button on Tab 0 & 1 for fast transfers
                if (selectedMainTab != 2) {
                    FloatingActionButton(
                        onClick = {
                            transferInitialFromEmployee = null
                            transferInitialToEmployee = null
                            transferInitialCategory = null
                            showTransferDialog = true
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .padding(bottom = 60.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null)
                            Text("تحويل بين المخازن", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    // Full-Width Multi-Item Transfer Window Dialog
    if (showTransferDialog) {
        FullWidthTransferDialog(
            warehouseOwners = warehouseOwners,
            categories = categories,
            initialFromEmployee = transferInitialFromEmployee,
            initialToEmployee = transferInitialToEmployee,
            initialCategory = transferInitialCategory,
            onDismiss = {
                showTransferDialog = false
                transferInitialFromEmployee = null
                transferInitialToEmployee = null
                transferInitialCategory = null
            },
            onConfirmTransfer = { fromEmp, toEmp, items, notes ->
                viewModel.transferMultipleStock(
                    fromEmployee = fromEmp,
                    toEmployee = toEmp,
                    items = items,
                    notes = notes
                )
                showTransferDialog = false
                transferInitialFromEmployee = null
                transferInitialToEmployee = null
                transferInitialCategory = null
            },
            onShowMessage = { msg -> viewModel.showMessage(msg) }
        )
    }
}

// ======================================================================================
// TAB 0: LIST OF EMPLOYEE WAREHOUSES
// ======================================================================================
@Composable
private fun EmployeeWarehousesListTab(
    warehouseOwners: List<String>,
    users: List<com.example.data.model.AppUser>,
    categories: List<CardCategory>,
    employeeStocks: List<EmployeeStock>,
    currency: String,
    onEmployeeClick: (String) -> Unit
) {
    val totalMainCards = categories.sumOf { it.stockQuantity }
    val totalMainVal = categories.sumOf { it.stockQuantity * it.faceValue }

    val totalEmployeeCards = employeeStocks.sumOf { it.quantity }
    val totalEmployeeVal = employeeStocks.sumOf { st ->
        val cat = categories.find { it.id == st.categoryId }
        st.quantity * (cat?.faceValue ?: 0.0)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            // Summary Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "إجمالي الكروت في كافة المخازن",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${totalMainCards + totalEmployeeCards} كرت",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${warehouseOwners.size} مخزن موظف ومستودع رئيسي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "القيمة الاسمية الكلية",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = PdfAndShareHelper.formatCurrency(totalMainVal + totalEmployeeVal, currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "قائمة مخازن الموظفين (اضغط لفتح المخزن والتقرير وكشف الحساب):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Manager / Main Warehouse Card
        item {
            val catCount = categories.count { it.stockQuantity > 0 }
            EmployeeWarehouseSummaryCard(
                displayName = "المدير العام",
                roleTitle = "المستودع الرئيسي العام",
                totalCards = totalMainCards,
                totalValue = totalMainVal,
                categoriesCount = catCount,
                currency = currency,
                isManager = true,
                onClick = { onEmployeeClick("المدير العام") }
            )
        }

        // Each Employee's Warehouse Card
        items(warehouseOwners.filter { it != "المدير العام" }) { empName ->
            val empUser = users.find { it.displayName == empName }
            val empStockList = employeeStocks.filter { it.employeeName == empName && it.quantity > 0 }
            val empTotalCards = empStockList.sumOf { it.quantity }
            val empTotalVal = empStockList.sumOf { st ->
                val cat = categories.find { it.id == st.categoryId }
                st.quantity * (cat?.faceValue ?: 0.0)
            }

            EmployeeWarehouseSummaryCard(
                displayName = empName,
                roleTitle = empUser?.role?.titleAr ?: "موظف مبيعات",
                phone = empUser?.phone ?: "",
                totalCards = empTotalCards,
                totalValue = empTotalVal,
                categoriesCount = empStockList.size,
                currency = currency,
                isManager = false,
                onClick = { onEmployeeClick(empName) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Summary Card for each employee warehouse
@Composable
private fun EmployeeWarehouseSummaryCard(
    displayName: String,
    roleTitle: String,
    phone: String = "",
    totalCards: Int,
    totalValue: Double,
    categoriesCount: Int,
    currency: String,
    isManager: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isManager) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (isManager) Icons.Default.Store else Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isManager) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (isManager) "مخزن المدير العام (المستودع الرئيسي)" else "مخزن: $displayName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (phone.isNotBlank()) "$roleTitle • $phone" else roleTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "فتح المخزن ⬅",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isManager) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "$totalCards كرت",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isManager) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "($categoriesCount فئات)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "القيمة: ${PdfAndShareHelper.formatCurrency(totalValue, currency)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ======================================================================================
// EMPLOYEE WAREHOUSE DETAIL VIEW (الكمية، تقرير بها، وكشف حساب)
// ======================================================================================
@Composable
private fun EmployeeWarehouseDetailView(
    employeeName: String,
    users: List<com.example.data.model.AppUser>,
    categories: List<CardCategory>,
    employeeStocks: List<EmployeeStock>,
    employeeTransfers: List<EmployeeTransferRecord>,
    transactions: List<TransactionRecord>,
    currency: String,
    networkName: String,
    onBack: () -> Unit,
    onInitiateTransfer: (fromEmp: String, toEmp: String?, cat: CardCategory?) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var subTab by remember { mutableIntStateOf(0) } // 0: الأصناف والكمية, 1: تقرير الجرد, 2: كشف الحساب
    var searchQuery by remember { mutableStateOf("") }
    var statementFilter by remember { mutableStateOf("ALL") } // ALL, IN, OUT, SALE
    var statementViewMode by remember { mutableStateOf("LIST") } // LIST, A4

    val isManager = employeeName == "المدير العام"
    val empUser = users.find { it.displayName == employeeName }

    // Prepare Stock Items: (CardCategory, Quantity)
    val stockItems = remember(employeeName, categories, employeeStocks) {
        if (isManager) {
            categories.map { it to it.stockQuantity }
        } else {
            categories.map { cat ->
                val qty = employeeStocks.find { it.employeeName == employeeName && it.categoryId == cat.id }?.quantity ?: 0
                cat to qty
            }
        }
    }

    val filteredStockItems = remember(stockItems, searchQuery) {
        if (searchQuery.isBlank()) stockItems
        else stockItems.filter { it.first.name.contains(searchQuery, ignoreCase = true) }
    }

    val totalCards = stockItems.sumOf { it.second }
    val totalValue = stockItems.sumOf { it.second * it.first.faceValue }

    // Movements involving this employee:
    val incomingTransfers = remember(employeeName, employeeTransfers) {
        employeeTransfers.filter { it.toEmployee == employeeName }
    }
    val outgoingTransfers = remember(employeeName, employeeTransfers) {
        employeeTransfers.filter { it.fromEmployee == employeeName }
    }
    val empSales = remember(employeeName, transactions) {
        transactions.filter { it.employeeName == employeeName && it.type == TransactionType.SALE }
    }

    val totalInCards = incomingTransfers.sumOf { it.quantity }
    val totalInVal = incomingTransfers.sumOf { it.totalFaceValue }
    val totalOutCards = outgoingTransfers.sumOf { it.quantity }
    val totalOutVal = outgoingTransfers.sumOf { it.totalFaceValue }
    val totalSalesCards = empSales.sumOf { it.quantity }
    val totalSalesVal = empSales.sumOf { it.totalAmount }

    // Data model for Statement Movements
    data class StatementMovement(
        val date: Long,
        val code: String,
        val type: String, // IN, OUT, SALE
        val typeLabel: String,
        val description: String,
        val inQty: Int,
        val outQty: Int,
        val amount: Double,
        val notes: String
    )

    val statementMovements = remember(incomingTransfers, outgoingTransfers, empSales) {
        val list = mutableListOf<StatementMovement>()
        incomingTransfers.forEach { trf ->
            list.add(
                StatementMovement(
                    date = trf.date,
                    code = trf.transferCode,
                    type = "IN",
                    typeLabel = "📥 تحويل وارد",
                    description = if (trf.transferType == "CARDS") "${trf.quantity} كرت [${trf.categoryName}] من [${trf.fromEmployee}]" else "عهدة نقدية: ${PdfAndShareHelper.formatCurrency(trf.cashAmount, currency)} من [${trf.fromEmployee}]",
                    inQty = trf.quantity,
                    outQty = 0,
                    amount = if (trf.transferType == "CARDS") trf.totalFaceValue else trf.cashAmount,
                    notes = trf.notes
                )
            )
        }
        outgoingTransfers.forEach { trf ->
            list.add(
                StatementMovement(
                    date = trf.date,
                    code = trf.transferCode,
                    type = "OUT",
                    typeLabel = "📤 تحويل صادر",
                    description = if (trf.transferType == "CARDS") "${trf.quantity} كرت [${trf.categoryName}] إلى [${trf.toEmployee}]" else "عهدة نقدية: ${PdfAndShareHelper.formatCurrency(trf.cashAmount, currency)} إلى [${trf.toEmployee}]",
                    inQty = 0,
                    outQty = trf.quantity,
                    amount = if (trf.transferType == "CARDS") trf.totalFaceValue else trf.cashAmount,
                    notes = trf.notes
                )
            )
        }
        empSales.forEach { sale ->
            list.add(
                StatementMovement(
                    date = sale.date,
                    code = sale.invoiceNumber,
                    type = "SALE",
                    typeLabel = "🛒 مبيعات عهدة",
                    description = "${sale.quantity} كرت [${sale.categoryName}] للعميل [${sale.distributorName.ifBlank { "عميل مباشر" }}]",
                    inQty = 0,
                    outQty = sale.quantity,
                    amount = sale.totalAmount,
                    notes = sale.notes
                )
            )
        }
        list.sortedBy { it.date }
    }

    val filteredMovements = remember(statementMovements, statementFilter) {
        when (statementFilter) {
            "IN" -> statementMovements.filter { it.type == "IN" }
            "OUT" -> statementMovements.filter { it.type == "OUT" }
            "SALE" -> statementMovements.filter { it.type == "SALE" }
            else -> statementMovements
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Detail Top App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                        Surface(
                            shape = CircleShape,
                            color = if (isManager) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (isManager) Icons.Default.Store else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isManager) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isManager) "مخزن المدير العام (المستودع الرئيسي)" else "مخزن الموظف: $employeeName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val phoneStr = empUser?.phone?.let { if (it.isNotBlank()) " • $it" else "" } ?: ""
                            Text(
                                text = (empUser?.role?.titleAr ?: if (isManager) "المستودع الرئيسي" else "موظف مبيعات") + phoneStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { onInitiateTransfer(employeeName, null, null) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تحويل", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { onNavigateToTab(2) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استبدال", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Three Sub-tabs: 1. الكميات, 2. تقرير الجرد, 3. كشف الحساب
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { subTab = 0 },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = if (subTab == 0) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.outlinedButtonColors(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("الكميات (${totalCards})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { subTab = 1 },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = if (subTab == 1) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.outlinedButtonColors(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("تقرير الجرد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { subTab = 2 },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp),
                        colors = if (subTab == 2) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary) else ButtonDefaults.outlinedButtonColors(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("كشف الحساب (${statementMovements.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sub-Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(14.dp)
        ) {
            when (subTab) {
                // =========================================================================
                // SUB-TAB 0: STOCK QUANTITIES (الأصناف والكميات المتوفرة)
                // =========================================================================
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Summary Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "إجمالي الكروت في مخزن هذا الموظف",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "$totalCards كرت",
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "القيمة الاسمية الإجمالية",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = PdfAndShareHelper.formatCurrency(totalValue, currency),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = { onInitiateTransfer(employeeName, null, null) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        ) {
                                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تحويل من هذا المخزن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { onInitiateTransfer("المدير العام", employeeName, null) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("توريد لهذا المخزن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Search Bar
                        item {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("بحث في فئات الكروت بمخزن الموظف...", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        // Category Stock Rows
                        items(filteredStockItems) { (cat, qty) ->
                            val lineValue = qty * cat.faceValue
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                                }
                                            }
                                            Column {
                                                Text(text = cat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text(
                                                    text = "سعر البيع: ${PdfAndShareHelper.formatCurrency(cat.sellingPrice, currency)} • القيمة: ${PdfAndShareHelper.formatCurrency(cat.faceValue, currency)}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (qty > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "$qty كرت",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (qty > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "إجمالي قيمة الفئة: ${PdfAndShareHelper.formatCurrency(lineValue, currency)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        TextButton(
                                            onClick = { onInitiateTransfer(employeeName, null, cat) },
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("تحويل هذه الفئة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredStockItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("لا توجد فئات مطابقة للبحث", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(60.dp)) }
                    }
                }

                // =========================================================================
                // SUB-TAB 1: INVENTORY REPORT (تقرير جرد المخزن والعهدة)
                // =========================================================================
                1 -> {
                    val reportText = remember(employeeName, stockItems) {
                        PdfAndShareHelper.createEmployeeWarehouseInventoryReport(
                            employeeName = employeeName,
                            items = stockItems.map { it.first.name to Pair(it.second, it.first.faceValue) },
                            networkName = networkName,
                            currency = currency
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Action Buttons Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val phone = empUser?.phone ?: ""
                                    PdfAndShareHelper.shareViaWhatsApp(context, phone, reportText)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إرسال عبر واتساب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    PdfAndShareHelper.shareViaUniversal(context, "تقرير جرد مخزن - $employeeName", reportText)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("مشاركة / طباعة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Authentic Document Paper Preview Card (A4 Style)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                // Header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "🌐 $networkName",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1E3A8A)
                                        )
                                        Text(
                                            text = "نظام كروت نت لإدارة الشبكات والمخازن",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFFEFF6FF),
                                        shape = RoundedCornerShape(6.dp),
                                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                    ) {
                                        Text(
                                            text = "وثيقة جرد رسمية",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E3A8A),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFF1E3A8A), thickness = 2.dp)
                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "تقرير جرد العهدة والمخزن",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "الموظف المسؤول: $employeeName (${empUser?.role?.titleAr ?: "موظف مبيعات"})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF334155)
                                    )
                                    Text(
                                        text = "التاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date())}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Table Header
                                Surface(
                                    color = Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("م", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(0.4f))
                                        Text("اسم الفئة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(2f))
                                        Text("سعر الكرت", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                                        Text("الكمية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                        Text("الإجمالي", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                                    }
                                }

                                val inStockCategories = stockItems.filter { it.second > 0 }
                                if (inStockCategories.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("المخزن فارغ حالياً (لا توجد كروت برصيد أكبر من صفر)", fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                } else {
                                    inStockCategories.forEachIndexed { idx, (cat, qty) ->
                                        val lineVal = qty * cat.faceValue
                                        val rowColor = if (idx % 2 == 0) Color.White else Color(0xFFF8FAFC)
                                        Surface(color = rowColor, modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("${idx + 1}", fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.weight(0.4f))
                                                Text(cat.name, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0F172A), modifier = Modifier.weight(2f))
                                                Text(PdfAndShareHelper.formatCurrency(cat.faceValue, currency), fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                                                Text("$qty كرت", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                                Text(PdfAndShareHelper.formatCurrency(lineVal, currency), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                                            }
                                        }
                                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFF1E3A8A), thickness = 1.5.dp)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("إجمالي عدد الكروت: $totalCards كرت", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                    Text("القيمة الاسمية الكلية: ${PdfAndShareHelper.formatCurrency(totalValue, currency)}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color(0xFF16A34A))
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                // Stamp & Signatures
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("الموظف المستلم العهدة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                        Text(employeeName, fontSize = 11.sp, color = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text("التوقيع: ....................", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("ختم الإدارة والاعتماد الرسمي", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF1F5F9),
                                            border = BorderStroke(1.dp, Color(0xFF94A3B8)),
                                            modifier = Modifier.size(width = 110.dp, height = 48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("ختم معتمد\n$networkName", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), textAlign = TextAlign.Center)
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("المسؤول المالي / الإدارة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                        Text("المدير العام", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text("التوقيع: ....................", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }

                // =========================================================================
                // SUB-TAB 2: COMPREHENSIVE ACCOUNT STATEMENT (كشف الحساب وحركة المخزن)
                // =========================================================================
                2 -> {
                    val comprehensiveReportText = remember(employeeName, incomingTransfers, outgoingTransfers, empSales, totalCards, totalValue) {
                        PdfAndShareHelper.createEmployeeComprehensiveAccountStatement(
                            employeeName = employeeName,
                            transfers = employeeTransfers,
                            salesTransactions = transactions,
                            currentStockCards = totalCards,
                            currentStockValue = totalValue,
                            networkName = networkName,
                            currency = currency
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Summary Metrics Grid (4 Blocks)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("📥 الوارد للمخزن", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                    Text("$totalInCards كرت", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF15803D))
                                    Text(PdfAndShareHelper.formatCurrency(totalInVal, currency), fontSize = 10.sp, color = Color(0xFF166534))
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("📤 الصادر والتحويل", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                    Text("$totalOutCards كرت", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                                    Text(PdfAndShareHelper.formatCurrency(totalOutVal, currency), fontSize = 10.sp, color = Color(0xFF92400E))
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFDBEAFE))
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("🛒 مبيعات العهدة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                    Text("$totalSalesCards كرت", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D4ED8))
                                    Text(PdfAndShareHelper.formatCurrency(totalSalesVal, currency), fontSize = 10.sp, color = Color(0xFF1E40AF))
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("📦 الرصيد الحالي", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("$totalCards كرت", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                    Text(PdfAndShareHelper.formatCurrency(totalValue, currency), fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        // Action Bar: Share Buttons & View Toggle (List vs A4 Sheet)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // View Mode Toggle
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = statementViewMode == "LIST",
                                    onClick = { statementViewMode = "LIST" },
                                    label = { Text("سجل الحركات", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                                FilterChip(
                                    selected = statementViewMode == "A4",
                                    onClick = { statementViewMode = "A4" },
                                    label = { Text("ورقة A4 رسمية", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }

                            // Share Action Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = {
                                        val phone = empUser?.phone ?: ""
                                        PdfAndShareHelper.shareViaWhatsApp(context, phone, comprehensiveReportText)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("واتساب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        PdfAndShareHelper.shareViaUniversal(context, "كشف حساب مخزن وعهدة - $employeeName", comprehensiveReportText)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مشاركة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Filter Chips row (All, In, Out, Sales)
                        if (statementViewMode == "LIST") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = statementFilter == "ALL",
                                    onClick = { statementFilter = "ALL" },
                                    label = { Text("الكل (${statementMovements.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = statementFilter == "IN",
                                    onClick = { statementFilter = "IN" },
                                    label = { Text("وارد (+${incomingTransfers.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = statementFilter == "OUT",
                                    onClick = { statementFilter = "OUT" },
                                    label = { Text("صادر (-${outgoingTransfers.size})", fontSize = 11.sp) }
                                )
                                FilterChip(
                                    selected = statementFilter == "SALE",
                                    onClick = { statementFilter = "SALE" },
                                    label = { Text("مبيعات (-${empSales.size})", fontSize = 11.sp) }
                                )
                            }
                        }

                        // Main Statement Content (LIST vs A4)
                        if (statementViewMode == "LIST") {
                            if (filteredMovements.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("لا توجد حركات مسجلة تطابق التصفية", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filteredMovements) { mov ->
                                        val dateStr = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(mov.date))
                                        val badgeColor = when (mov.type) {
                                            "IN" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                                            "OUT" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                                            else -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
                                        }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = badgeColor.first
                                                        ) {
                                                            Text(
                                                                text = mov.typeLabel,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = badgeColor.second,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                            )
                                                        }
                                                        Text(text = "#${mov.code}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    }
                                                    Text(text = dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(text = mov.description, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                                                Spacer(modifier = Modifier.height(4.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    val qtyChange = if (mov.inQty > 0) "+${mov.inQty} كرت" else if (mov.outQty > 0) "-${mov.outQty} كرت" else ""
                                                    Text(
                                                        text = if (qtyChange.isNotBlank()) "تأثير الكمية: $qtyChange" else "",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (mov.inQty > 0) Color(0xFF15803D) else Color(0xFFB45309)
                                                    )
                                                    Text(
                                                        text = "القيمة: ${PdfAndShareHelper.formatCurrency(mov.amount, currency)}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }

                                                if (mov.notes.isNotBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(text = "ملاحظة: ${mov.notes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }

                                    item { Spacer(modifier = Modifier.height(60.dp)) }
                                }
                            }
                        } else {
                            // Authentic A4 Statement Paper Preview
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        // Header
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(text = "🌐 $networkName", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E3A8A))
                                                Text(text = "إدارة المستودعات وحركات العهد", fontSize = 10.sp, color = Color(0xFF64748B))
                                            }
                                            Surface(
                                                color = Color(0xFFEFF6FF),
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                            ) {
                                                Text("كشف حساب عهدة موظف A4", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = Color(0xFF1E3A8A), thickness = 2.dp)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(text = "كشف حساب حركة مخزن وعهدة الموظف", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF0F172A), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(text = "الموظف: $employeeName (${empUser?.role?.titleAr ?: "موظف مبيعات"})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                                            Text(text = "التاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date())}", fontSize = 10.sp, color = Color(0xFF64748B))
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Balance Overview Strip
                                        Surface(
                                            color = Color(0xFFF8FAFC),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceAround
                                            ) {
                                                Text("وارد: $totalInCards كرت", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                                Text("صادر: $totalOutCards كرت", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                                Text("مبيعات: $totalSalesCards كرت", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D4ED8))
                                                Text("الرصيد: $totalCards كرت", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3A8A))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Statement Table
                                        Surface(
                                            color = Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("م", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.4f))
                                                Text("التاريخ", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                                Text("السند", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                                Text("البيان", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2.4f))
                                                Text("وارد", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                                                Text("صادر", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                                                Text("الرصيد", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                            }
                                        }

                                        if (statementMovements.isEmpty()) {
                                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                                                Text("لا توجد حركات مسجلة لهذا المخزن حتى الآن", fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                        } else {
                                            var runningCardQty = 0
                                            statementMovements.forEachIndexed { idx, mov ->
                                                runningCardQty += (mov.inQty - mov.outQty)
                                                val rowBg = if (idx % 2 == 0) Color.White else Color(0xFFF8FAFC)
                                                Surface(color = rowBg, modifier = Modifier.fillMaxWidth()) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 5.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("${idx + 1}", fontSize = 10.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.4f))
                                                        Text(PdfAndShareHelper.formatShortDate(mov.date), fontSize = 10.sp, color = Color(0xFF334155), modifier = Modifier.weight(1.2f))
                                                        Text("#${mov.code}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E3A8A), modifier = Modifier.weight(1f))
                                                        Text(mov.description, fontSize = 10.sp, color = Color(0xFF0F172A), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(2.4f))
                                                        Text(if (mov.inQty > 0) "+${mov.inQty}" else "-", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D), modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                                                        Text(if (mov.outQty > 0) "-${mov.outQty}" else "-", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309), modifier = Modifier.weight(0.8f), textAlign = TextAlign.Center)
                                                        Text("$runningCardQty", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), modifier = Modifier.weight(0.9f), textAlign = TextAlign.End)
                                                    }
                                                }
                                                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = Color(0xFF1E3A8A), thickness = 1.5.dp)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("إجمالي الحركات: ${statementMovements.size}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("صافي رصيد الكروت الحالي: $totalCards كرت (${PdfAndShareHelper.formatCurrency(totalValue, currency)})", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF16A34A))
                                        }

                                        Spacer(modifier = Modifier.height(20.dp))

                                        // Signatures & Stamp
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("الموظف المسؤول", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                                Text(employeeName, fontSize = 10.sp, color = Color(0xFF64748B))
                                                Spacer(modifier = Modifier.height(20.dp))
                                                Text("التوقيع: ............", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                            }

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("ختم الإدارة المعتمد", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFF1F5F9),
                                                    border = BorderStroke(1.dp, Color(0xFF94A3B8)),
                                                    modifier = Modifier.size(width = 90.dp, height = 40.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text("ختم الحسابات\n$networkName", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A), textAlign = TextAlign.Center)
                                                    }
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("الحسابات والرقابة", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                                Text("المدير العام", fontSize = 10.sp, color = Color(0xFF64748B))
                                                Spacer(modifier = Modifier.height(20.dp))
                                                Text("التوقيع: ............", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(40.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ======================================================================================
// TAB 1: TRANSFERS LOG & ACTIONS
// ======================================================================================
@Composable
private fun WarehouseTransfersTab(
    transfers: List<EmployeeTransferRecord>,
    currency: String,
    onNewTransferClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "سجل تحويلات المخازن والعهد (${transfers.size}):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(
                    onClick = onNewTransferClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تحويل جديد", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (transfers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد تحويلات بين المخازن مسجلة حتى الآن",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(transfers) { trf ->
                val dateStr = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(trf.date))
                val isCards = trf.transferType == "CARDS"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "#${trf.transferCode}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text(text = dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "من: ${trf.fromEmployee}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp).padding(horizontal = 2.dp))
                            Text(text = "إلى: ${trf.toEmployee}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        if (isCards) {
                            Text(
                                text = "📦 ${trf.quantity} كرت [${trf.categoryName}] (القيمة: ${PdfAndShareHelper.formatCurrency(trf.totalFaceValue, currency)})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = "💵 عهدة نقدية: ${PdfAndShareHelper.formatCurrency(trf.cashAmount, currency)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = Color(0xFF16A34A)
                            )
                        }

                        if (trf.notes.isNotBlank()) {
                            Text(text = "ملاحظات: ${trf.notes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ======================================================================================
// TAB 2: CARD SWAP INSIDE WAREHOUSES
// ======================================================================================
@Composable
private fun WarehouseCardSwapTab(
    viewModel: NetCardsViewModel,
    distributors: List<Distributor>,
    categories: List<CardCategory>,
    swaps: List<CardSwapRecord>,
    currency: String
) {
    val context = LocalContext.current
    var showSwapDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "استبدال فئات الكروت في المخزن",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = { showSwapDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CompareArrows, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("عملية استبدال جديدة", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Recent Swaps Header matching Transfers Tab
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سجل عمليات الاستبدال السابقة (${swaps.size}):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (swaps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد عمليات استبدال كروت مسجلة حتى الآن",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(swaps) { sw ->
                    val dateStr = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(sw.date))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "#${sw.swapCode}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                Text(text = dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "الموزع / العميل: ${sw.distributorName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "🔻 مرجع: ${sw.fromQuantity} كرت [${sw.fromCategoryName}]",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "🔺 بديل: ${sw.toQuantity} كرت [${sw.toCategoryName}]",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💰 الفارق المالي: ${PdfAndShareHelper.formatCurrency(sw.differenceAmount, currency)} (المدفوع: ${PdfAndShareHelper.formatCurrency(sw.paidDifference, currency)})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF16A34A)
                            )

                            if (sw.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "ملاحظات: ${sw.notes}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showSwapDialog) {
            FullWidthSwapDialog(
                distributors = distributors,
                categories = categories,
                currency = currency,
                onDismiss = { showSwapDialog = false },
                onConfirmSwap = { dist, fromCat, fromQ, toCat, toQ, paidD, notes ->
                    viewModel.recordCardSwap(
                        distributorId = dist.id,
                        fromCategoryId = fromCat.id,
                        fromQuantity = fromQ,
                        toCategoryId = toCat.id,
                        toQuantity = toQ,
                        paidDifference = paidD,
                        notes = notes,
                        onSuccess = { swapRecord ->
                            val swapMsg = "🔄 إشعار استبدال فئات كروت - ${viewModel.settings.value?.networkName ?: "شبكة كروت نت"}\n" +
                                    "الموزع: ${swapRecord.distributorName}\n" +
                                    "المرجع: ${swapRecord.fromQuantity} كرت من ${swapRecord.fromCategoryName}\n" +
                                    "المستلم: ${swapRecord.toQuantity} كرت من ${swapRecord.toCategoryName}\n" +
                                    "الفارق المالي: ${PdfAndShareHelper.formatCurrency(swapRecord.differenceAmount, currency)}\n" +
                                    "المسدد: ${PdfAndShareHelper.formatCurrency(swapRecord.paidDifference, currency)}\n" +
                                    "المتبقي على الحساب: ${PdfAndShareHelper.formatCurrency(swapRecord.remainingDifference, currency)}"
                            PdfAndShareHelper.shareViaWhatsApp(context, dist.phone, swapMsg)
                            showSwapDialog = false
                        }
                    )
                },
                onShowMessage = { msg -> viewModel.showMessage(msg) }
            )
        }
    }
}

@Composable
private fun FullWidthSwapDialog(
    distributors: List<Distributor>,
    categories: List<CardCategory>,
    currency: String,
    onDismiss: () -> Unit,
    onConfirmSwap: (Distributor, CardCategory, Int, CardCategory, Int, Double, String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    var selectedDistributor by remember { mutableStateOf<Distributor?>(distributors.firstOrNull()) }
    var fromCategory by remember { mutableStateOf<CardCategory?>(categories.firstOrNull()) }
    var toCategory by remember { mutableStateOf<CardCategory?>(categories.getOrNull(1) ?: categories.firstOrNull()) }

    var fromQuantityStr by remember { mutableStateOf("10") }
    var toQuantityStr by remember { mutableStateOf("5") }
    var paidDiffStr by remember { mutableStateOf("0") }
    var swapNotes by remember { mutableStateOf("") }

    var distDropdown by remember { mutableStateOf(false) }
    var fromCatDropdown by remember { mutableStateOf(false) }
    var toCatDropdown by remember { mutableStateOf(false) }

    val fromQty = fromQuantityStr.toIntOrNull() ?: 0
    val toQty = toQuantityStr.toIntOrNull() ?: 0

    val fromTotal = fromQty * (fromCategory?.sellingPrice ?: 0.0)
    val toTotal = toQty * (toCategory?.sellingPrice ?: 0.0)
    val diffAmount = toTotal - fromTotal
    val paidDiff = paidDiffStr.toDoubleOrNull() ?: 0.0
    val remainingDiff = diffAmount - paidDiff

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عملية استبدال فئات كروت جديدة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "إغلاق")
                    }
                }
                Text(
                    text = "استبدال كروت راكدة أو تالفة بكروت مطلوبة للموزع، مع احتساب الفارق المالي وتحديث المخزون والحساب",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Distributor Selector
                Text("الموزع:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { distDropdown = true }
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = selectedDistributor?.name ?: "اختر الموزع...", fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = distDropdown, onDismissRequest = { distDropdown = false }) {
                        distributors.forEach { dist ->
                            DropdownMenuItem(
                                text = { Text(dist.name) },
                                onClick = {
                                    selectedDistributor = dist
                                    distDropdown = false
                                }
                            )
                        }
                    }
                }

                // From Category (Returned by distributor)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🔻 الكروت المرجعة من الموزع (تضاف للمخزن)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { fromCatDropdown = true }
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = fromCategory?.name ?: "اختر الفئة المرجعة...", fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = fromCatDropdown, onDismissRequest = { fromCatDropdown = false }) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text("${cat.name} (${PdfAndShareHelper.formatCurrency(cat.sellingPrice, currency)})") },
                                        onClick = {
                                            fromCategory = cat
                                            fromCatDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = fromQuantityStr,
                            onValueChange = { fromQuantityStr = it },
                            label = { Text("الكمية المرجعة (عدد الكروت)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (fromCategory != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "قيمة الكروت المرجعة: ${PdfAndShareHelper.formatCurrency(fromTotal, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }

                // To Category (Taken by distributor)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🔺 الكروت البديلة المطلوبة (تخصم من المخزن)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { toCatDropdown = true }
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = toCategory?.name ?: "اختر الفئة المطلوبة...", fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(expanded = toCatDropdown, onDismissRequest = { toCatDropdown = false }) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text("${cat.name} - متوفر: ${cat.stockQuantity} كرت") },
                                        onClick = {
                                            toCategory = cat
                                            toCatDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = toQuantityStr,
                            onValueChange = { toQuantityStr = it },
                            label = { Text("الكمية المطلوبة (عدد الكروت)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (toCategory != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "المتوفر بالمخزن: ${toCategory!!.stockQuantity} كرت",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (toCategory!!.stockQuantity < toQty) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "القيمة: ${PdfAndShareHelper.formatCurrency(toTotal, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Net Difference Calculation Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الفارق المالي الصافي:", fontWeight = FontWeight.Bold)
                            Text(
                                text = if (diffAmount >= 0) "+ ${PdfAndShareHelper.formatCurrency(diffAmount, currency)} (على الموزع)"
                                else "- ${PdfAndShareHelper.formatCurrency(-diffAmount, currency)} (لصالح الموزع)",
                                fontWeight = FontWeight.Bold,
                                color = if (diffAmount > 0) MaterialTheme.colorScheme.error else Color(0xFF16A34A)
                            )
                        }

                        if (diffAmount > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = paidDiffStr,
                                onValueChange = { paidDiffStr = it },
                                label = { Text("المسدد نقداً من الفارق") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "المتبقي يضاف لحساب الموزع: ${PdfAndShareHelper.formatCurrency(remainingDiff, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = swapNotes,
                    onValueChange = { swapNotes = it },
                    label = { Text("ملاحظات عملية الاستبدال (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (selectedDistributor == null) {
                                onShowMessage("يرجى اختيار الموزع")
                                return@Button
                            }
                            if (fromCategory == null || toCategory == null) {
                                onShowMessage("يرجى تحديد الفئتين المرجعة والبديلة")
                                return@Button
                            }
                            if (fromQty <= 0 || toQty <= 0) {
                                onShowMessage("يرجى إدخال كميات صحيحة")
                                return@Button
                            }
                            onConfirmSwap(
                                selectedDistributor!!,
                                fromCategory!!,
                                fromQty,
                                toCategory!!,
                                toQty,
                                paidDiff,
                                swapNotes
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تنفيذ الاستبدال")
                    }
                }
            }
        }
    }
}

// ======================================================================================
// FULL-WIDTH TRANSFER DIALOG (MULTI-ITEM TRANSFERS)
// ======================================================================================
@Composable
private fun FullWidthTransferDialog(
    warehouseOwners: List<String>,
    categories: List<CardCategory>,
    initialFromEmployee: String? = null,
    initialToEmployee: String? = null,
    initialCategory: CardCategory? = null,
    onDismiss: () -> Unit,
    onConfirmTransfer: (String, String, List<InvoiceItemDraft>, String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    var fromEmployee by remember { mutableStateOf(initialFromEmployee ?: warehouseOwners.firstOrNull() ?: "المدير العام") }
    var toEmployee by remember {
        mutableStateOf(
            initialToEmployee ?: warehouseOwners.find { it != fromEmployee } ?: warehouseOwners.getOrNull(1) ?: warehouseOwners.first()
        )
    }
    var selectedCategory by remember { mutableStateOf(initialCategory ?: categories.firstOrNull()) }
    var itemQuantityStr by remember { mutableStateOf("10") }
    var itemPriceStr by remember { mutableStateOf(selectedCategory?.faceValue?.toString() ?: "0.0") }
    var transferItems by remember { mutableStateOf(listOf<InvoiceItemDraft>()) }
    var notes by remember { mutableStateOf("") }

    var fromDropdown by remember { mutableStateOf(false) }
    var toDropdown by remember { mutableStateOf(false) }
    var catDropdown by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تحويل كروت بين المخازن",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "إغلاق")
                    }
                }
                Text(
                    text = "اختر المخزن المرسل والمستلم، وأضف عدة أصناف لجدول التحويل دفعة واحدة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // From Warehouse Dropdown
                Text(text = "من مخزن (المرسل):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { fromDropdown = true }
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = fromEmployee, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = fromDropdown, onDismissRequest = { fromDropdown = false }) {
                        warehouseOwners.forEach { owner ->
                            DropdownMenuItem(
                                text = { Text(owner) },
                                onClick = {
                                    fromEmployee = owner
                                    fromDropdown = false
                                }
                            )
                        }
                    }
                }

                // To Warehouse Dropdown
                Text(text = "إلى مخزن (المستلم):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { toDropdown = true }
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = toEmployee, fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = toDropdown, onDismissRequest = { toDropdown = false }) {
                        warehouseOwners.forEach { owner ->
                            DropdownMenuItem(
                                text = { Text(owner) },
                                onClick = {
                                    toEmployee = owner
                                    toDropdown = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Add Item Section
                Text(text = "إضافة صنف للتحويل:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { catDropdown = true }
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = selectedCategory?.name ?: "اختر فئة الكرت", fontWeight = FontWeight.Medium)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = catDropdown, onDismissRequest = { catDropdown = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.name} (متوفر: ${cat.stockQuantity}) - القيمة: ${cat.faceValue}") },
                                onClick = {
                                    selectedCategory = cat
                                    itemPriceStr = cat.faceValue.toString()
                                    catDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = itemQuantityStr,
                        onValueChange = { itemQuantityStr = it },
                        label = { Text("الكمية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = itemPriceStr,
                        onValueChange = { itemPriceStr = it },
                        label = { Text("القيمة / السعر") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Button(
                    onClick = {
                        val cat = selectedCategory
                        val qty = itemQuantityStr.toIntOrNull() ?: 0
                        val prc = itemPriceStr.toDoubleOrNull() ?: 0.0
                        if (cat != null && qty > 0) {
                            transferItems = transferItems + InvoiceItemDraft(
                                categoryId = cat.id,
                                categoryName = cat.name,
                                quantity = qty,
                                unitPrice = prc
                            )
                            itemQuantityStr = "10"
                        } else {
                            onShowMessage("يرجى اختيار الفئة وإدخال كمية صحيحة")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة الصنف إلى جدول التحويل", fontWeight = FontWeight.Bold)
                }

                // Table of added items
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الأصناف المضافة للتحويل (${transferItems.size}):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (transferItems.isNotEmpty()) {
                        Text(
                            text = "${transferItems.sumOf { it.quantity }} كرت إجمالي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "الصنف", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(2.2f), textAlign = TextAlign.Start)
                                Text(text = "القيمة", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                                Text(text = "الكمية", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                Text(text = "الإجمالي", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                                Spacer(modifier = Modifier.width(36.dp))
                            }
                        }

                        if (transferItems.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                                Text("لم يتم إضافة أي صنف للتحويل بعد", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 70.dp, max = 160.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    transferItems.forEachIndexed { index, item ->
                                        val rowBg = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                        Surface(color = rowBg, modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = item.categoryName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(2.2f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(text = "${item.unitPrice}", fontSize = 11.sp, modifier = Modifier.weight(1.1f), textAlign = TextAlign.Center)
                                                Text(text = "${item.quantity}", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.9f), textAlign = TextAlign.Center)
                                                Text(text = "${item.totalAmount}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                                                IconButton(
                                                    onClick = { transferItems = transferItems.filterIndexed { i, _ -> i != index } },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات التحويل (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            if (transferItems.isEmpty()) {
                                onShowMessage("يرجى إضافة صنف واحد على الأقل للتحويل")
                                return@Button
                            }
                            if (fromEmployee == toEmployee) {
                                onShowMessage("لا يمكن التحويل لنفس المخزن")
                                return@Button
                            }

                            onConfirmTransfer(fromEmployee, toEmployee, transferItems, notes)
                        },
                        enabled = transferItems.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("تنفيذ التحويل (${transferItems.size} أصناف)")
                    }
                }
            }
        }
    }
}
