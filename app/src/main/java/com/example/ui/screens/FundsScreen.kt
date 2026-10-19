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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CashRegister
import com.example.data.model.FundMovement
import com.example.data.model.MovementDirection
import com.example.ui.NetCardsViewModel
import com.example.util.PdfAndShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundsScreen(
    viewModel: NetCardsViewModel
) {
    val context = LocalContext.current
    val mainTreasury by viewModel.mainTreasury.collectAsStateWithLifecycle()
    val employeeFunds by viewModel.employeeFunds.collectAsStateWithLifecycle()
    val allMovements by viewModel.allFundMovements.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"
    val networkName = settings?.networkName ?: "شبكة كروت نت"

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: الصندوق الرئيسي, 1: صناديق الموظفين المستقلة

    // Selected Employee Fund
    var selectedEmployeeFund by remember {
        mutableStateOf<CashRegister?>(null)
    }

    val actualEmployeeFunds = employeeFunds.filter { it.employeeUsername != null && it.employeeUsername != "admin" }

    // Auto select logged in employee's fund if available
    val activeEmpFund = selectedEmployeeFund ?: actualEmployeeFunds.find { it.employeeUsername == currentUser.username } ?: actualEmployeeFunds.firstOrNull()

    // Dialog States
    var showMainDepositDialog by remember { mutableStateOf(false) }
    var showMainExpenseDialog by remember { mutableStateOf(false) }
    var showEmpDepositDialog by remember { mutableStateOf(false) }
    var showEmpExpenseDialog by remember { mutableStateOf(false) }
    var showTransferToMainDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("funds_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Clarification / Isolation Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "صندوق البرنامج الرئيسي هو الصندوق الرسمي العام. وصناديق الموظفين مستقلة تماماً لكل موظف لمعرفة حسابه وعهدته، ولا يتدخل أي حساب فيها بصندوق البرنامج الرئيسي.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Tabs
        item {
            if (actualEmployeeFunds.isNotEmpty()) {
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("صندوق البرنامج الرئيسي 🏦") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("صناديق الموظفين المستقلة 💼") }
                    )
                }
            } else {
                PrimaryTabRow(selectedTabIndex = 0) {
                    Tab(
                        selected = true,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("صندوق البرنامج الرئيسي 🏦") }
                    )
                }
            }
        }

        if (selectedTabIndex == 0) {
            // === TAB 0: MAIN APP TREASURY ===
            val treasury = mainTreasury
            val mainMovements = allMovements.filter { !it.isEmployeeFund }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "صندوق البرنامج الرئيسي (الرسمي)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "الخزينة المركزية الرسمية للشبكة",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Current Balance Display
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "الرصيد الرسمي المتوفر بالخزينة الرئيسية",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = PdfAndShareHelper.formatCurrency(treasury?.currentBalance ?: 0.0, currency),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "إجمالي المقبوضات: ${PdfAndShareHelper.formatCurrency(treasury?.totalInflows ?: 0.0, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF16A34A),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "إجمالي المصروفات: ${PdfAndShareHelper.formatCurrency(treasury?.totalOutflows ?: 0.0, currency)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions for Main Treasury
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showMainDepositDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إيداع رسمي", color = Color.White)
                            }

                            Button(
                                onClick = { showMainExpenseDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("صرف رسمي", color = Color.White)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "سجل حركات صندوق البرنامج الرئيسي (${mainMovements.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (mainMovements.isEmpty()) {
                item {
                    Text(
                        text = "لا توجد حركات مسجلة بالصندوق الرئيسي حتى الآن",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(mainMovements) { mov ->
                    MovementItemCard(movement = mov, currency = currency)
                }
            }
        } else {
            // === TAB 1: INDEPENDENT EMPLOYEE FUNDS ===
            if (actualEmployeeFunds.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "لا توجد صناديق موظفين حالياً",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "تنشأ صناديق وعهد الموظفين المستقلة تلقائياً عند إضافة موظفين في شاشة (الموظفين والصلاحيات) وتسجيل عمليات بيع أو تحصيل نقدية بأسمائهم بنظام العزل المالي المستقل.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "اختر موظف لعرض صندوقه المستقل:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(actualEmployeeFunds) { fund ->
                            val isSelected = activeEmpFund?.id == fund.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedEmployeeFund = fund },
                                label = { Text(fund.name) },
                                leadingIcon = {
                                    Icon(
                                        if (isSelected) Icons.Default.CheckCircle else Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (activeEmpFund != null) {
                val fund = activeEmpFund
                val empMovements = allMovements.filter { it.registerId == fund.id }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = fund.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "الموظف المسؤول: ${fund.employeeDisplayName ?: "موظف"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFBBF24).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "معزول عن صندوق البرنامج",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Cash in Hand Balance
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "الرصيد النقدي الحالي في عهدة الموظف",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = PdfAndShareHelper.formatCurrency(fund.currentBalance, currency),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "إجمالي التحصيل: ${PdfAndShareHelper.formatCurrency(fund.totalInflows, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                                Text(
                                    text = "إجمالي المصروف والمسلم: ${PdfAndShareHelper.formatCurrency(fund.totalOutflows, currency)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Action Buttons for Employee Fund
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showEmpDepositDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                    ) {
                                        Text("إيداع / تحصيل نقدية", fontSize = 12.sp, color = Color.White)
                                    }

                                    Button(
                                        onClick = { showEmpExpenseDialog = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("صرف من الصندوق", fontSize = 12.sp, color = Color.White)
                                    }
                                }

                                // Transfer to Main Fund Button (Handover)
                                Button(
                                    onClick = { showTransferToMainDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(Icons.Default.MoveToInbox, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("تسليم وتوريد نقدية لصندوق البرنامج الرئيسي", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                // Share Statement via WhatsApp
                                OutlinedButton(
                                    onClick = {
                                        val statementMsg = buildString {
                                            appendLine("💼 *كشف حساب صندوق الموظف المستقل - $networkName*")
                                            appendLine("👤 الموظف: ${fund.employeeDisplayName}")
                                            appendLine("📅 التاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date())}")
                                            appendLine("━━━━━━━━━━━━━━━━━━━")
                                            appendLine("💰 *الرصيد النقدي الحالي في العهدة: ${PdfAndShareHelper.formatCurrency(fund.currentBalance, currency)}*")
                                            appendLine("• إجمالي المقبوضات والتحصيلات: ${PdfAndShareHelper.formatCurrency(fund.totalInflows, currency)}")
                                            appendLine("• إجمالي المصروفات والمسلمات: ${PdfAndShareHelper.formatCurrency(fund.totalOutflows, currency)}")
                                            appendLine("━━━━━━━━━━━━━━━━━━━")
                                            appendLine("تنبيه: هذا الصندوق مستقل تماماً ولا يتدخل في صندوق البرنامج الرئيسي.")
                                        }
                                        PdfAndShareHelper.shareViaWhatsApp(context, "", statementMsg)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF25D366))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إرسال كشف حساب الصندوق عبر واتساب", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "سجل حركات صندوق ${fund.name} (${empMovements.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (empMovements.isEmpty()) {
                    item {
                        Text(
                            text = "لا توجد حركات مسجلة في صندوق هذا الموظف",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(empMovements) { mov ->
                        MovementItemCard(movement = mov, currency = currency)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Dialogs:
    // 1. Main Treasury Deposit Dialog
    if (showMainDepositDialog) {
        FundActionDialog(
            title = "إيداع رسمي في صندوق البرنامج الرئيسي",
            actionLabel = "تأكيد الإيداع بالخزينة الرئيسية",
            onDismiss = { showMainDepositDialog = false },
            onConfirm = { amount, title, notes ->
                viewModel.recordMainTreasuryDeposit(amount, title, notes)
                showMainDepositDialog = false
            }
        )
    }

    // 2. Main Treasury Expense Dialog
    if (showMainExpenseDialog) {
        FundActionDialog(
            title = "صرف رسمي من صندوق البرنامج الرئيسي",
            actionLabel = "تأكيد الصرف من الخزينة الرئيسية",
            onDismiss = { showMainExpenseDialog = false },
            onConfirm = { amount, title, notes ->
                viewModel.recordMainTreasuryExpense(amount, title, notes)
                showMainExpenseDialog = false
            }
        )
    }

    // 3. Employee Fund Deposit Dialog
    if (showEmpDepositDialog && activeEmpFund != null) {
        FundActionDialog(
            title = "إيداع في ${activeEmpFund.name}",
            actionLabel = "إيداع في صندوق الموظف المستقل",
            onDismiss = { showEmpDepositDialog = false },
            onConfirm = { amount, title, notes ->
                viewModel.recordEmployeeFundDeposit(
                    username = activeEmpFund.employeeUsername ?: currentUser.username,
                    displayName = activeEmpFund.employeeDisplayName ?: currentUser.displayName,
                    amount = amount,
                    title = title,
                    notes = notes
                )
                showEmpDepositDialog = false
            }
        )
    }

    // 4. Employee Fund Expense Dialog
    if (showEmpExpenseDialog && activeEmpFund != null) {
        FundActionDialog(
            title = "صرف من ${activeEmpFund.name}",
            actionLabel = "تسجيل صرف من صندوق الموظف",
            onDismiss = { showEmpExpenseDialog = false },
            onConfirm = { amount, title, notes ->
                viewModel.recordEmployeeFundExpense(
                    username = activeEmpFund.employeeUsername ?: currentUser.username,
                    displayName = activeEmpFund.employeeDisplayName ?: currentUser.displayName,
                    amount = amount,
                    title = title,
                    notes = notes
                )
                showEmpExpenseDialog = false
            }
        )
    }

    // 5. Transfer to Main Treasury Dialog
    if (showTransferToMainDialog && activeEmpFund != null) {
        FundActionDialog(
            title = "تسليم وتوريد نقدية لصندوق البرنامج الرئيسي",
            actionLabel = "تأكيد التسليم والتوريد",
            onDismiss = { showTransferToMainDialog = false },
            onConfirm = { amount, title, notes ->
                viewModel.transferEmployeeToMainTreasury(
                    username = activeEmpFund.employeeUsername ?: currentUser.username,
                    displayName = activeEmpFund.employeeDisplayName ?: currentUser.displayName,
                    amount = amount,
                    notes = "$title - $notes"
                )
                showTransferToMainDialog = false
            }
        )
    }
}

@Composable
fun MovementItemCard(
    movement: FundMovement,
    currency: String
) {
    val dateStr = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(movement.date))
    val isInflow = movement.direction == MovementDirection.INFLOW
    val color = if (isInflow) Color(0xFF16A34A) else MaterialTheme.colorScheme.error

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
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isInflow) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = movement.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$dateStr • الرصيد بعد الحركة: ${PdfAndShareHelper.formatCurrency(movement.balanceAfter, currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${if (isInflow) "+" else "-"} ${PdfAndShareHelper.formatCurrency(movement.amount, currency)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
    }
}

@Composable
fun FundActionDialog(
    title: String,
    actionLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, title: String, notes: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var titleStr by remember { mutableStateOf("") }
    var notesStr by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("المبلغ *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = titleStr,
                        onValueChange = { titleStr = it },
                        label = { Text("البيان / السبب *") },
                        placeholder = { Text("مثال: تحصيل نقد، وقود، تسليم عهدة") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notesStr,
                        onValueChange = { notesStr = it },
                        label = { Text("ملاحظات إضافية") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = amountStr.toDoubleOrNull() ?: 0.0
                                if (amt <= 0 || titleStr.isBlank()) {
                                    return@Button
                                }
                                onConfirm(amt, titleStr, notesStr)
                            }
                        ) {
                            Text(actionLabel)
                        }
                    }
                }
            }
        }
    }
}
