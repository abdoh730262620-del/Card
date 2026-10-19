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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import com.example.ui.AppScreen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CardCategory
import com.example.ui.NetCardsViewModel
import com.example.util.PdfAndShareHelper

@Composable
fun CardCategoriesScreen(
    viewModel: NetCardsViewModel
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val currency = settings?.currency ?: "ريال"

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CardCategory?>(null) }
    var supplyingCategory by remember { mutableStateOf<CardCategory?>(null) }

    val totalCards = categories.sumOf { it.stockQuantity }
    val totalFaceValue = categories.sumOf { it.stockQuantity * it.faceValue }

    Box(modifier = Modifier.fillMaxSize().testTag("categories_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Overview Banner
            item {
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
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "إجمالي الكروت بالمخزن",
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
                                text = PdfAndShareHelper.formatCurrency(totalFaceValue, currency),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Quick Actions Bar
            item {
                Button(
                    onClick = { showAddCategoryDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_category_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إضافة فئة كروت جديدة", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Categories List
            items(categories) { cat ->
                val profitPerCard = (cat.sellingPrice - cat.costPrice).coerceAtLeast(0.0)
                val threshold = settings?.lowStockThreshold ?: 20
                val isLowStock = cat.stockQuantity <= threshold

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = cat.notes.ifBlank {
                                            val s = if (cat.quotaMb >= 1024) "${cat.quotaMb / 1024} جيجا" else if (cat.quotaMb > 0) "${cat.quotaMb} ميجا" else ""
                                            if (s.isNotBlank()) "$s • ${cat.validityHours} ساعة" else "${cat.validityHours} ساعة"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Stock Badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isLowStock) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = "${cat.stockQuantity} كرت",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        if (isLowStock) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تنبيه: الرصيد منخفض، يحتاج توريد كروت جديدة!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Pricing Grid
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("سعر البيع", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        PdfAndShareHelper.formatCurrency(cat.sellingPrice, currency),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (currentUser.canViewProfits) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("سعر التكلفة", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            PdfAndShareHelper.formatCurrency(cat.costPrice, currency),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("ربح الكرت", style = MaterialTheme.typography.labelSmall)
                                        Text(
                                            PdfAndShareHelper.formatCurrency(profitPerCard, currency),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF16A34A)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.PURCHASES) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("توريد وشراء كمية للمخزن", fontSize = 12.sp)
                            }

                            Row {
                                IconButton(onClick = { editingCategory = cat }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { viewModel.deleteCategory(cat) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }

    // Add / Edit Category Dialog
    if (showAddCategoryDialog || editingCategory != null) {
        val isEdit = editingCategory != null
        val target = editingCategory

        var name by remember { mutableStateOf(target?.name ?: "") }
        var faceValueStr by remember { mutableStateOf(target?.faceValue?.toInt()?.toString() ?: "1000") }
        var costPriceStr by remember { mutableStateOf(target?.costPrice?.toInt()?.toString() ?: "880") }
        var sellingPriceStr by remember { mutableStateOf(target?.sellingPrice?.toInt()?.toString() ?: "1000") }
        var initialStockStr by remember { mutableStateOf(target?.stockQuantity?.toString() ?: "100") }
        var validityHoursStr by remember { mutableStateOf(target?.validityHours?.toString() ?: "24") }
        var quotaMbStr by remember { mutableStateOf(target?.quotaMb?.toString() ?: "1024") }
        var hasClearedSelling by remember { mutableStateOf(false) }
        var hasClearedCost by remember { mutableStateOf(false) }
        var hasClearedStock by remember { mutableStateOf(false) }
        var hasClearedValidity by remember { mutableStateOf(false) }
        var hasClearedQuota by remember { mutableStateOf(false) }

        var notes by remember { mutableStateOf(target?.notes ?: "") }

        Dialog(
            onDismissRequest = {
                showAddCategoryDialog = false
                editingCategory = null
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
                        Text(
                        text = if (isEdit) "تعديل فئة الكرت" else "إضافة فئة كروت جديدة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الفئة (مثال: فئة 1000 ريال) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = sellingPriceStr,
                            onValueChange = {
                                sellingPriceStr = it
                                if (faceValueStr.isBlank() || faceValueStr == "0") {
                                    faceValueStr = it
                                }
                            },
                            label = { Text("سعر البيع *") },
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged {
                                    if (it.isFocused && !hasClearedSelling && sellingPriceStr == "1000") {
                                        sellingPriceStr = ""
                                        hasClearedSelling = true
                                    }
                                }
                        )
                        OutlinedTextField(
                            value = costPriceStr,
                            onValueChange = { costPriceStr = it },
                            label = { Text("سعر التكلفة *") },
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged {
                                    if (it.isFocused && !hasClearedCost && costPriceStr == "880") {
                                        costPriceStr = ""
                                        hasClearedCost = true
                                    }
                                }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = initialStockStr,
                            onValueChange = { initialStockStr = it },
                            label = { Text("الكمية المتوفرة") },
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged {
                                    if (it.isFocused && !hasClearedStock && initialStockStr == "100") {
                                        initialStockStr = ""
                                        hasClearedStock = true
                                    }
                                }
                        )
                        OutlinedTextField(
                            value = validityHoursStr,
                            onValueChange = { validityHoursStr = it },
                            label = { Text("الصلاحية (ساعة)") },
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged {
                                    if (it.isFocused && !hasClearedValidity && validityHoursStr == "24") {
                                        validityHoursStr = ""
                                        hasClearedValidity = true
                                    }
                                }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = quotaMbStr,
                        onValueChange = { quotaMbStr = it },
                        label = { Text("سعة الحجم (ميغابايت، مثلاً 1024 = 1 جيجا)") },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                if (it.isFocused && !hasClearedQuota && quotaMbStr == "1024") {
                                    quotaMbStr = ""
                                    hasClearedQuota = true
                                }
                            },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات / الباقة") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showAddCategoryDialog = false
                            editingCategory = null
                        }) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isBlank()) {
                                    viewModel.showMessage("يرجى إدخال اسم الفئة")
                                    return@Button
                                }
                                val sell = sellingPriceStr.toDoubleOrNull() ?: 1000.0
                                val cost = costPriceStr.toDoubleOrNull() ?: 880.0
                                val face = faceValueStr.toDoubleOrNull() ?: sell
                                val stock = initialStockStr.toIntOrNull() ?: 0
                                val valHours = validityHoursStr.toIntOrNull() ?: 24
                                val qMb = quotaMbStr.toIntOrNull() ?: 1024

                                if (isEdit && target != null) {
                                    viewModel.updateCategory(
                                        target.copy(
                                            name = name,
                                            faceValue = face,
                                            costPrice = cost,
                                            sellingPrice = sell,
                                            stockQuantity = stock,
                                            validityHours = valHours,
                                            quotaMb = qMb,
                                            notes = notes
                                        )
                                    )
                                } else {
                                    viewModel.addCategory(
                                        name = name,
                                        faceValue = face,
                                        costPrice = cost,
                                        sellingPrice = sell,
                                        stockQuantity = stock,
                                        validityHours = valHours,
                                        quotaMb = qMb,
                                        colorHex = "#1E88E5",
                                        notes = notes
                                    )
                                }
                                showAddCategoryDialog = false
                                editingCategory = null
                            }
                        ) {
                            Text(if (isEdit) "حفظ التعديلات" else "إضافة الفئة")
                        }
                    }
                }
            }
        }
    }
}

    // Restock / Supply Dialog (شراء وتوريد كروت للمخزن)
    if (supplyingCategory != null) {
        val cat = supplyingCategory!!
        var supplyQuantityStr by remember { mutableStateOf("50") }
        var supplyCostStr by remember { mutableStateOf(cat.costPrice.toInt().toString()) }
        var supplierName by remember { mutableStateOf("") }
        var supplyNotes by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = { supplyingCategory = null },
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
                        text = "توريد كروت جديدة للمخزن",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "الفئة: ${cat.name} (الرصيد الحالي: ${cat.stockQuantity} كرت)",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = supplyQuantityStr,
                        onValueChange = { supplyQuantityStr = it },
                        label = { Text("الكمية الموردة (عدد الكروت) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = supplyCostStr,
                        onValueChange = { supplyCostStr = it },
                        label = { Text("سعر التكلفة للكرت *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("اسم المورد / الشركة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = supplyNotes,
                        onValueChange = { supplyNotes = it },
                        label = { Text("ملاحظات التوريد (دفعة رقم...)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val q = supplyQuantityStr.toIntOrNull() ?: 0
                    val c = supplyCostStr.toDoubleOrNull() ?: 0.0
                    Text(
                        text = "إجمالي فاتورة التوريد: ${PdfAndShareHelper.formatCurrency(q * c, currency)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { supplyingCategory = null }) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val qty = supplyQuantityStr.toIntOrNull() ?: 0
                                val cost = supplyCostStr.toDoubleOrNull() ?: cat.costPrice
                                if (qty <= 0) {
                                    viewModel.showMessage("يرجى إدخال كمية صحيحة")
                                    return@Button
                                }
                                viewModel.recordPurchase(
                                    categoryId = cat.id,
                                    quantity = qty,
                                    costPrice = cost,
                                    supplierName = supplierName,
                                    notes = supplyNotes
                                )
                                supplyingCategory = null
                            }
                        ) {
                            Text("إتمام التوريد وإضافة للمخزن")
                        }
                    }
                }
            }
        }
    }
}
}
