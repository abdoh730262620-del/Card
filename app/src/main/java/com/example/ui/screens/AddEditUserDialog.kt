package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppUser
import com.example.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditUserDialog(
    user: AppUser?,
    onDismiss: () -> Unit,
    onSave: (AppUser) -> Unit,
    showMessage: (String) -> Unit
) {
    val isEdit = user != null

    var username by remember(user) { mutableStateOf(user?.username ?: "") }
    var displayName by remember(user) { mutableStateOf(user?.displayName ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }
    var pinCode by remember(user) { mutableStateOf(user?.pinCode ?: "1234") }
    var phone by remember(user) { mutableStateOf(user?.phone ?: "") }
    var selectedRole by remember(user) { mutableStateOf(user?.role ?: UserRole.SALES) }

    var canViewProfits by remember(user) { mutableStateOf(user?.canViewProfits ?: false) }
    var canDeleteRecords by remember(user) { mutableStateOf(user?.canDeleteRecords ?: false) }
    var canManageUsers by remember(user) { mutableStateOf(user?.canManageUsers ?: false) }
    var canManageStock by remember(user) { mutableStateOf(user?.canManageStock ?: true) }
    var canGrantDiscount by remember(user) { mutableStateOf(user?.canGrantDiscount ?: false) }
    var canExportBackup by remember(user) { mutableStateOf(user?.canExportBackup ?: false) }

    // Granular permissions for the 7 modules (Add, Edit, Delete)
    var canAddSales by remember(user) { mutableStateOf(user?.canAddSales ?: true) }
    var canEditSales by remember(user) { mutableStateOf(user?.canEditSales ?: false) }
    var canDeleteSales by remember(user) { mutableStateOf(user?.canDeleteSales ?: false) }

    var canAddPurchases by remember(user) { mutableStateOf(user?.canAddPurchases ?: (user?.role == UserRole.ADMIN || user?.role == UserRole.ACCOUNTANT)) }
    var canEditPurchases by remember(user) { mutableStateOf(user?.canEditPurchases ?: false) }
    var canDeletePurchases by remember(user) { mutableStateOf(user?.canDeletePurchases ?: false) }

    var canAddVouchers by remember(user) { mutableStateOf(user?.canAddVouchers ?: true) }
    var canEditVouchers by remember(user) { mutableStateOf(user?.canEditVouchers ?: false) }
    var canDeleteVouchers by remember(user) { mutableStateOf(user?.canDeleteVouchers ?: false) }

    var canAddSwaps by remember(user) { mutableStateOf(user?.canAddSwaps ?: true) }
    var canEditSwaps by remember(user) { mutableStateOf(user?.canEditSwaps ?: false) }
    var canDeleteSwaps by remember(user) { mutableStateOf(user?.canDeleteSwaps ?: false) }

    var canAddTransfers by remember(user) { mutableStateOf(user?.canAddTransfers ?: true) }
    var canEditTransfers by remember(user) { mutableStateOf(user?.canEditTransfers ?: false) }
    var canDeleteTransfers by remember(user) { mutableStateOf(user?.canDeleteTransfers ?: false) }

    var canAddReports by remember(user) { mutableStateOf(user?.canAddReports ?: (user?.role == UserRole.ADMIN || user?.role == UserRole.ACCOUNTANT)) }
    var canEditReports by remember(user) { mutableStateOf(user?.canEditReports ?: false) }
    var canDeleteReports by remember(user) { mutableStateOf(user?.canDeleteReports ?: false) }

    var canAddCards by remember(user) { mutableStateOf(user?.canAddCards ?: (user?.role == UserRole.ADMIN || user?.canManageStock == true)) }
    var canEditCards by remember(user) { mutableStateOf(user?.canEditCards ?: false) }
    var canDeleteCards by remember(user) { mutableStateOf(user?.canDeleteCards ?: false) }

    var canManageExpenses by remember(user) { mutableStateOf(user?.canManageExpenses ?: false) }

    var roleDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("full_screen_add_edit_employee"),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isEdit) "تعديل بيانات وصلاحيات الموظف" else "إضافة موظف جديد",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEdit) "تحديث الصلاحيات وبيانات تسجيل الدخول" else "تحديد بيانات الحساب والدور والصلاحيات التفصيلية",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إلغاء")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                if (displayName.isBlank() || username.isBlank()) {
                                    showMessage("يرجى إكمال البيانات المطلوبة (الاسم الكامل واسم المستخدم)")
                                    return@Button
                                }
                                val toSave = (user ?: AppUser(username = username.trim(), displayName = displayName.trim())).copy(
                                    displayName = displayName.trim(),
                                    username = username.trim(),
                                    pinCode = pinCode.trim(),
                                    role = selectedRole,
                                    phone = phone.trim(),
                                    email = email.trim().lowercase(),
                                    canViewProfits = canViewProfits,
                                    canDeleteRecords = canDeleteRecords,
                                    canManageUsers = canManageUsers,
                                    canManageStock = canManageStock,
                                    canGrantDiscount = canGrantDiscount,
                                    canExportBackup = canExportBackup,
                                    canAddSales = canAddSales,
                                    canEditSales = canEditSales,
                                    canDeleteSales = canDeleteSales,
                                    canAddPurchases = canAddPurchases,
                                    canEditPurchases = canEditPurchases,
                                    canDeletePurchases = canDeletePurchases,
                                    canAddVouchers = canAddVouchers,
                                    canEditVouchers = canEditVouchers,
                                    canDeleteVouchers = canDeleteVouchers,
                                    canAddSwaps = canAddSwaps,
                                    canEditSwaps = canEditSwaps,
                                    canDeleteSwaps = canDeleteSwaps,
                                    canAddTransfers = canAddTransfers,
                                    canEditTransfers = canEditTransfers,
                                    canDeleteTransfers = canDeleteTransfers,
                                    canAddReports = canAddReports,
                                    canEditReports = canEditReports,
                                    canDeleteReports = canDeleteReports,
                                    canAddCards = canAddCards,
                                    canEditCards = canEditCards,
                                    canDeleteCards = canDeleteCards
                                )
                                onSave(toSave)
                            },
                            modifier = Modifier
                                .weight(2f)
                                .height(50.dp)
                                .testTag("save_employee_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(if (isEdit) Icons.Default.Save else Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isEdit) "حفظ التعديلات" else "إضافة الموظف الآن",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                // Section 1: Basic Information Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("البيانات الأساسية ومعلومات الدخول", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }

                            // Full Name (Single Line)
                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("الاسم الكامل للموظف *") },
                                placeholder = { Text("مثال: أحمد عبد الله") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("employee_name_input"),
                                singleLine = true
                            )

                            // Username (Dedicated full row - singleLine = true as requested!)
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("اسم المستخدم (لتسجيل الدخول) *") },
                                placeholder = { Text("مثال: ahmed1") },
                                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("employee_username_input"),
                                singleLine = true
                            )

                            // PIN Code (Dedicated full row - singleLine = true)
                            OutlinedTextField(
                                value = pinCode,
                                onValueChange = { pinCode = it },
                                label = { Text("رمز المرور PIN للدخول السريع *") },
                                placeholder = { Text("افتراضي: 1234") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("employee_pin_input"),
                                singleLine = true
                            )

                            // Phone
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("رقم هاتف الموظف (اختياري)") },
                                placeholder = { Text("مثال: 771234567") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("employee_phone_input"),
                                singleLine = true
                            )
                        }
                    }
                }

                // Section 2: Google Cloud Invite & Auto-Linking Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF10B981))
                                Text("الدعوة والربط السحابي التلقائي (Google)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = Color(0xFF047857))
                            }

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("بريد Google للموظف (للدعوة والربط السحابي)") },
                                placeholder = { Text("مثال: employee@gmail.com") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF10B981)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("employee_email_input"),
                                singleLine = true
                            )

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(20.dp))
                                    Text(
                                        text = "💡 بمجرد إدخال بريد Google الخاص بالموظف، سيتعرف التطبيق عليه تلقائياً فور تسجيل دخوله ببريده في جهازه، وسيفتح له متجرك وصلاحياته المحددة هنا مباشرة دون أن يكون مستقلاً عنك.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF065F46),
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 3: Role Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("الرتبة والوظيفة (تحديد قالب الصلاحيات)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            }

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = "${selectedRole.titleAr} (${selectedRole.getRoleDescription()})",
                                    onValueChange = {},
                                    readOnly = true,
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { roleDropdownExpanded = true },
                                    trailingIcon = {
                                        IconButton(onClick = { roleDropdownExpanded = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = roleDropdownExpanded,
                                    onDismissRequest = { roleDropdownExpanded = false }
                                ) {
                                    UserRole.values().forEach { r ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(r.titleAr, fontWeight = FontWeight.Bold)
                                                    Text(r.getRoleDescription(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            },
                                            onClick = {
                                                selectedRole = r
                                                if (r == UserRole.ADMIN) {
                                                    canViewProfits = true; canDeleteRecords = true; canManageUsers = true; canManageStock = true; canGrantDiscount = true; canExportBackup = true
                                                    canAddSales = true; canEditSales = true; canDeleteSales = true
                                                    canAddPurchases = true; canEditPurchases = true; canDeletePurchases = true
                                                    canAddVouchers = true; canEditVouchers = true; canDeleteVouchers = true
                                                    canAddSwaps = true; canEditSwaps = true; canDeleteSwaps = true
                                                    canAddTransfers = true; canEditTransfers = true; canDeleteTransfers = true
                                                    canAddReports = true; canEditReports = true; canDeleteReports = true
                                                    canAddCards = true; canEditCards = true; canDeleteCards = true
                                                } else if (r == UserRole.ACCOUNTANT) {
                                                    canViewProfits = true; canDeleteRecords = false; canManageUsers = false; canManageStock = true; canGrantDiscount = true; canExportBackup = true
                                                    canAddSales = true; canEditSales = true; canDeleteSales = false
                                                    canAddPurchases = true; canEditPurchases = true; canDeletePurchases = false
                                                    canAddVouchers = true; canEditVouchers = true; canDeleteVouchers = false
                                                    canAddSwaps = true; canEditSwaps = false; canDeleteSwaps = false
                                                    canAddTransfers = true; canEditTransfers = false; canDeleteTransfers = false
                                                    canAddReports = true; canEditReports = true; canDeleteReports = false
                                                    canAddCards = false; canEditCards = false; canDeleteCards = false
                                                } else if (r == UserRole.SUPERVISOR) {
                                                    canViewProfits = true; canDeleteRecords = false; canManageUsers = false; canManageStock = true; canGrantDiscount = true; canExportBackup = false
                                                    canAddSales = true; canEditSales = true; canDeleteSales = false
                                                    canAddPurchases = true; canEditPurchases = true; canDeletePurchases = false
                                                    canAddVouchers = true; canEditVouchers = true; canDeleteVouchers = false
                                                    canAddSwaps = true; canEditSwaps = true; canDeleteSwaps = false
                                                    canAddTransfers = true; canEditTransfers = true; canDeleteTransfers = false
                                                    canAddReports = true; canEditReports = true; canDeleteReports = false
                                                    canAddCards = true; canEditCards = true; canDeleteCards = false
                                                } else { // SALES
                                                    canViewProfits = false; canDeleteRecords = false; canManageUsers = false; canManageStock = false; canGrantDiscount = false; canExportBackup = false
                                                    canAddSales = true; canEditSales = false; canDeleteSales = false
                                                    canAddPurchases = false; canEditPurchases = false; canDeletePurchases = false
                                                    canAddVouchers = true; canEditVouchers = false; canDeleteVouchers = false
                                                    canAddSwaps = true; canEditSwaps = false; canDeleteSwaps = false
                                                    canAddTransfers = true; canEditTransfers = false; canDeleteTransfers = false
                                                    canAddReports = false; canEditReports = false; canDeleteReports = false
                                                    canAddCards = false; canEditCards = false; canDeleteCards = false
                                                }
                                                roleDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 4: Detailed Module Permissions (Add / Edit / Delete)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column {
                                    Text("صلاحيات الأقسام التفصيلية", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("تحكم دقيق بصلاحية (إضافة / تعديل / حذف) في كل قسم:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            // 1. Sales
                            SectionPermissionBox(
                                title = "قسم المبيعات والفواتير",
                                icon = Icons.Default.ShoppingCart,
                                accentColor = Color(0xFF10B981),
                                canAdd = canAddSales,
                                onAddChange = { canAddSales = it },
                                canEdit = canEditSales,
                                onEditChange = { canEditSales = it },
                                canDelete = canDeleteSales,
                                onDeleteChange = { canDeleteSales = it }
                            )

                            // 2. Purchases
                            SectionPermissionBox(
                                title = "قسم المشتريات والتوريدات",
                                icon = Icons.Default.Inventory,
                                accentColor = Color(0xFF0F766E),
                                canAdd = canAddPurchases,
                                onAddChange = { canAddPurchases = it },
                                canEdit = canEditPurchases,
                                onEditChange = { canEditPurchases = it },
                                canDelete = canDeletePurchases,
                                onDeleteChange = { canDeletePurchases = it }
                            )

                            // 3. Vouchers
                            SectionPermissionBox(
                                title = "قسم السندات والقبض والصرف",
                                icon = Icons.Default.ReceiptLong,
                                accentColor = Color(0xFFF43F5E),
                                canAdd = canAddVouchers,
                                onAddChange = { canAddVouchers = it },
                                canEdit = canEditVouchers,
                                onEditChange = { canEditVouchers = it },
                                canDelete = canDeleteVouchers,
                                onDeleteChange = { canDeleteVouchers = it }
                            )

                            // 4. Swaps
                            SectionPermissionBox(
                                title = "قسم مقايضة وتبديل الكروت",
                                icon = Icons.Default.SwapHoriz,
                                accentColor = Color(0xFF06B6D4),
                                canAdd = canAddSwaps,
                                onAddChange = { canAddSwaps = it },
                                canEdit = canEditSwaps,
                                onEditChange = { canEditSwaps = it },
                                canDelete = canDeleteSwaps,
                                onDeleteChange = { canDeleteSwaps = it }
                            )

                            // 5. Transfers
                            SectionPermissionBox(
                                title = "قسم التحويلات والمخازن",
                                icon = Icons.Default.Sync,
                                accentColor = Color(0xFFEA580C),
                                canAdd = canAddTransfers,
                                onAddChange = { canAddTransfers = it },
                                canEdit = canEditTransfers,
                                onEditChange = { canEditTransfers = it },
                                canDelete = canDeleteTransfers,
                                onDeleteChange = { canDeleteTransfers = it }
                            )

                            // 6. Reports
                            SectionPermissionBox(
                                title = "قسم التقارير المالية والإحصائية",
                                icon = Icons.Default.TrendingUp,
                                accentColor = Color(0xFF7C3AED),
                                canAdd = canAddReports,
                                onAddChange = { canAddReports = it },
                                canEdit = canEditReports,
                                onEditChange = { canEditReports = it },
                                canDelete = canDeleteReports,
                                onDeleteChange = { canDeleteReports = it }
                            )

                            // 7. Cards
                            SectionPermissionBox(
                                title = "قسم الكروت والأسعار والفئات",
                                icon = Icons.Default.CreditCard,
                                accentColor = Color(0xFF4F46E5),
                                canAdd = canAddCards,
                                onAddChange = { canAddCards = it },
                                canEdit = canEditCards,
                                onEditChange = { canEditCards = it },
                                canDelete = canDeleteCards,
                                onDeleteChange = { canDeleteCards = it }
                            )
                        }
                    }
                }

                // Section 5: General Administrative Permissions
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    ) {
                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Text("صلاحيات إدارية عامة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                            PermissionCheckbox("رؤية الأرباح وأسعار التكلفة", canViewProfits) { canViewProfits = it }
                            PermissionCheckbox("حذف وتعديل الفواتير العامة", canDeleteRecords) { canDeleteRecords = it }
                            PermissionCheckbox("توريد وتعديل كميات المخزن", canManageStock) { canManageStock = it }
                            PermissionCheckbox("منح خصومات مالية للموزعين", canGrantDiscount) { canGrantDiscount = it }
                            PermissionCheckbox("النسخ الاحتياطي واستعادة البيانات", canExportBackup) { canExportBackup = it }
                            PermissionCheckbox("إدارة المصروفات والصناديق", canManageExpenses) { canManageExpenses = it }
                        }
                    }
                }
            }
        }
    }
}

fun UserRole.getRoleDescription(): String {
    return when (this) {
        UserRole.ADMIN -> "كامل الصلاحيات والتحكم الإداري بالنظام"
        UserRole.ACCOUNTANT -> "التقارير والمحاسبة والسندات وإدارة المخزون"
        UserRole.SUPERVISOR -> "الإشراف والمبيعات والتحويلات والمقايضة"
        UserRole.SALES -> "عمليات البيع وإصدار الفواتير والسندات"
    }
}
