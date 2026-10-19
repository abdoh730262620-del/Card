package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CardCategory
import com.example.data.model.EmployeeTransferRecord
import com.example.ui.NetCardsViewModel
import com.example.util.PdfAndShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmployeeTransfersScreen(
    viewModel: NetCardsViewModel
) {
    val transfers by viewModel.employeeTransfers.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().testTag("transfers_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "التحويلات وعهد الموظفين",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "توثيق تحويل عهد الكروت والمبالغ النقدية بين المناديب والمشرفين والمحاسبين",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (transfers.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد تحويلات بين الموظفين مسجلة بعد",
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
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "#${trf.transferCode}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "من: ${trf.fromEmployee}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(20.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = "إلى: ${trf.toEmployee}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isCards) {
                                Text(
                                    text = "📦 عهدة كروت: ${trf.quantity} كرت من [${trf.categoryName}] (القيمة: ${PdfAndShareHelper.formatCurrency(trf.totalFaceValue, currency)})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            } else {
                                Text(
                                    text = "💵 عهدة نقدية: ${PdfAndShareHelper.formatCurrency(trf.cashAmount, currency)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF16A34A)
                                )
                            }

                            if (trf.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ملاحظات: ${trf.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 60.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "تحويل عهدة جديد")
        }
    }

    if (showAddDialog) {
        var fromEmployee by remember { mutableStateOf(users.firstOrNull()?.displayName ?: "المدير العام") }
        var toEmployee by remember { mutableStateOf(users.getOrNull(1)?.displayName ?: "") }
        var transferType by remember { mutableStateOf("CARDS") } // CARDS, CASH
        var selectedCategory by remember { mutableStateOf(categories.firstOrNull()) }
        var quantityStr by remember { mutableStateOf("20") }
        var cashAmountStr by remember { mutableStateOf("10000") }
        var notes by remember { mutableStateOf("") }

        var fromUserDropdown by remember { mutableStateOf(false) }
        var toUserDropdown by remember { mutableStateOf(false) }
        var catDropdown by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "تحويل عهدة جديد بين الموظفين",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // From Employee
                    Text("الموظف المسلّم (من):", style = MaterialTheme.typography.bodySmall)
                    Box {
                        OutlinedTextField(
                            value = fromEmployee,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { fromUserDropdown = true },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { fromUserDropdown = true }) }
                        )
                        DropdownMenu(expanded = fromUserDropdown, onDismissRequest = { fromUserDropdown = false }) {
                            users.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.displayName} (${u.role.titleAr})") },
                                    onClick = {
                                        fromEmployee = u.displayName
                                        fromUserDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // To Employee
                    Text("الموظف المستلم (إلى):", style = MaterialTheme.typography.bodySmall)
                    Box {
                        OutlinedTextField(
                            value = toEmployee,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { toUserDropdown = true },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { toUserDropdown = true }) }
                        )
                        DropdownMenu(expanded = toUserDropdown, onDismissRequest = { toUserDropdown = false }) {
                            users.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.displayName} (${u.role.titleAr})") },
                                    onClick = {
                                        toEmployee = u.displayName
                                        toUserDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // Transfer Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = transferType == "CARDS",
                            onClick = { transferType = "CARDS" },
                            label = { Text("عهدة كروت") },
                            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                        FilterChip(
                            selected = transferType == "CASH",
                            onClick = { transferType = "CASH" },
                            label = { Text("عهدة نقدية") },
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    if (transferType == "CARDS") {
                        // Category
                        Text("فئة الكروت:", style = MaterialTheme.typography.bodySmall)
                        Box {
                            OutlinedTextField(
                                value = selectedCategory?.name ?: "اختر الفئة...",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { catDropdown = true },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { catDropdown = true }) }
                            )
                            DropdownMenu(expanded = catDropdown, onDismissRequest = { catDropdown = false }) {
                                categories.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.name) },
                                        onClick = {
                                            selectedCategory = c
                                            catDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("كمية الكروت المحولة *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = cashAmountStr,
                            onValueChange = { cashAmountStr = it },
                            label = { Text("المبلغ النقدي المحول *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات") },
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
                                if (toEmployee.isBlank()) {
                                    viewModel.showMessage("يرجى تحديد الموظف المستلم")
                                    return@Button
                                }
                                val qty = quantityStr.toIntOrNull() ?: 0
                                val cash = cashAmountStr.toDoubleOrNull() ?: 0.0

                                viewModel.recordEmployeeTransfer(
                                    fromEmployee = fromEmployee,
                                    toEmployee = toEmployee,
                                    transferType = transferType,
                                    categoryId = selectedCategory?.id,
                                    quantity = qty,
                                    cashAmount = cash,
                                    notes = notes
                                )
                                showAddDialog = false
                            }
                        ) {
                            Text("توثيق التحويل")
                        }
                    }
                }
            }
        }
    }
}
