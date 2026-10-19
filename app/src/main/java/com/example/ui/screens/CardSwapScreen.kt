package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CardCategory
import com.example.data.model.CardSwapRecord
import com.example.data.model.Distributor
import com.example.ui.NetCardsViewModel
import com.example.util.PdfAndShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CardSwapScreen(
    viewModel: NetCardsViewModel
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val distributors by viewModel.distributors.collectAsStateWithLifecycle()
    val swaps by viewModel.cardSwaps.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"
    val networkName = settings?.networkName ?: "شبكة كروت نت"

    var selectedDistributor by remember { mutableStateOf<Distributor?>(null) }
    var fromCategory by remember { mutableStateOf<CardCategory?>(null) }
    var toCategory by remember { mutableStateOf<CardCategory?>(null) }

    var fromQuantityStr by remember { mutableStateOf("10") }
    var toQuantityStr by remember { mutableStateOf("5") }
    var paidDiffStr by remember { mutableStateOf("0") }
    var swapNotes by remember { mutableStateOf("") }

    var distDropdownExpanded by remember { mutableStateOf(false) }
    var fromCatDropdownExpanded by remember { mutableStateOf(false) }
    var toCatDropdownExpanded by remember { mutableStateOf(false) }

    val fromQty = fromQuantityStr.toIntOrNull() ?: 0
    val toQty = toQuantityStr.toIntOrNull() ?: 0

    val fromTotal = fromQty * (fromCategory?.sellingPrice ?: 0.0)
    val toTotal = toQty * (toCategory?.sellingPrice ?: 0.0)
    val diffAmount = toTotal - fromTotal // positive = distributor pays, negative = owes to distributor
    val paidDiff = paidDiffStr.toDoubleOrNull() ?: 0.0
    val remainingDiff = diffAmount - paidDiff

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("card_swap_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "استبدال فئات كروت بفئات أخرى",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "استبدال كروت راكدة بكروت مطلوبة للموزع، مع احتساب الفارق المالي وتحديث المخزون تلقائياً",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Swap Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Distributor Selector
                    Text("الموزع:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
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
                                    text = { Text(dist.name) },
                                    onClick = {
                                        selectedDistributor = dist
                                        distDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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

                            Box {
                                OutlinedTextField(
                                    value = fromCategory?.name ?: "اختر الفئة المرجعة...",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { fromCatDropdownExpanded = true },
                                    trailingIcon = {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { fromCatDropdownExpanded = true })
                                    }
                                )
                                DropdownMenu(
                                    expanded = fromCatDropdownExpanded,
                                    onDismissRequest = { fromCatDropdownExpanded = false }
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text("${cat.name} (${PdfAndShareHelper.formatCurrency(cat.sellingPrice, currency)})") },
                                            onClick = {
                                                fromCategory = cat
                                                fromCatDropdownExpanded = false
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
                                modifier = Modifier.fillMaxWidth()
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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

                            Box {
                                OutlinedTextField(
                                    value = toCategory?.name ?: "اختر الفئة المطلوبة...",
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { toCatDropdownExpanded = true },
                                    trailingIcon = {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.clickable { toCatDropdownExpanded = true })
                                    }
                                )
                                DropdownMenu(
                                    expanded = toCatDropdownExpanded,
                                    onDismissRequest = { toCatDropdownExpanded = false }
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = { Text("${cat.name} - متوفر: ${cat.stockQuantity} كرت") },
                                            onClick = {
                                                toCategory = cat
                                                toCatDropdownExpanded = false
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
                                modifier = Modifier.fillMaxWidth()
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

                    Spacer(modifier = Modifier.height(14.dp))

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
                                    modifier = Modifier.fillMaxWidth()
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

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = swapNotes,
                        onValueChange = { swapNotes = it },
                        label = { Text("ملاحظات عملية الاستبدال") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (selectedDistributor == null) {
                                viewModel.showMessage("يرجى اختيار الموزع")
                                return@Button
                            }
                            if (fromCategory == null || toCategory == null) {
                                viewModel.showMessage("يرجى تحديد الفئتين المرجعة والبديلة")
                                return@Button
                            }
                            if (fromQty <= 0 || toQty <= 0) {
                                viewModel.showMessage("يرجى إدخال كميات صحيحة")
                                return@Button
                            }

                            viewModel.recordCardSwap(
                                distributorId = selectedDistributor!!.id,
                                fromCategoryId = fromCategory!!.id,
                                fromQuantity = fromQty,
                                toCategoryId = toCategory!!.id,
                                toQuantity = toQty,
                                paidDifference = paidDiff,
                                notes = swapNotes,
                                onSuccess = { swapRecord ->
                                    val swapMsg = "🔄 إشعار استبدال فئات كروت - $networkName\n" +
                                            "الموزع: ${swapRecord.distributorName}\n" +
                                            "المرجع: ${swapRecord.fromQuantity} كرت من ${swapRecord.fromCategoryName}\n" +
                                            "المستلم: ${swapRecord.toQuantity} كرت من ${swapRecord.toCategoryName}\n" +
                                            "الفارق المالي: ${PdfAndShareHelper.formatCurrency(swapRecord.differenceAmount, currency)}\n" +
                                            "المسدد: ${PdfAndShareHelper.formatCurrency(swapRecord.paidDifference, currency)}\n" +
                                            "المتبقي على الحساب: ${PdfAndShareHelper.formatCurrency(swapRecord.remainingDifference, currency)}"
                                    PdfAndShareHelper.shareViaWhatsApp(context, selectedDistributor!!.phone, swapMsg)
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("execute_swap_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("تنفيذ الاستبدال وتحديث المخزون والحساب", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Previous Swaps History
        item {
            Text(
                text = "سجل عمليات الاستبدال السابقة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (swaps.isEmpty()) {
            item {
                Text(
                    text = "لا توجد عمليات استبدال مسجلة بعد.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(swaps) { swap ->
                val dateStr = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(swap.date))
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
                            Text(swap.distributorName, fontWeight = FontWeight.Bold)
                            Text("#${swap.swapCode} • $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "استبدل ${swap.fromQuantity} كرت (${swap.fromCategoryName}) بـ ${swap.toQuantity} كرت (${swap.toCategoryName})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الفارق: ${PdfAndShareHelper.formatCurrency(swap.differenceAmount, currency)}", fontWeight = FontWeight.Bold)
                            if (swap.remainingDifference != 0.0) {
                                Text("على الحساب: ${PdfAndShareHelper.formatCurrency(swap.remainingDifference, currency)}", color = MaterialTheme.colorScheme.error)
                            }
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
