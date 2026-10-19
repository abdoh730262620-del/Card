package com.example.ui.screens

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BroadcastOnPersonal
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationAdd
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.data.model.AppUser
import com.example.data.model.UserRole
import com.example.ui.NetCardsViewModel

@Composable
fun PermissionsAndUsersScreen(
    viewModel: NetCardsViewModel
) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var showAddUserDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<AppUser?>(null) }
    var showBroadcastDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().testTag("users_screen")) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Current Active User Banner
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
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "المستخدم النشط حالياً: ${currentUser.displayName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "الرتبة: ${currentUser.role.titleAr}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Manager Broadcast Button
            item {
                Button(
                    onClick = { showBroadcastDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.NotificationAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إرسال إشعار / توجيه من المدير لجميع الموظفين", fontWeight = FontWeight.Bold)
                }
            }

            item {
                Text(
                    text = "قائمة الموظفين والمستخدمين (${users.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(users) { user ->
                val isCurrent = user.id == currentUser.id

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = when (user.role) {
                                        UserRole.ADMIN -> MaterialTheme.colorScheme.primaryContainer
                                        UserRole.ACCOUNTANT -> MaterialTheme.colorScheme.secondaryContainer
                                        UserRole.SUPERVISOR -> MaterialTheme.colorScheme.tertiaryContainer
                                        UserRole.SALES -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = user.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "اسم المستخدم: ${user.username}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${user.role.titleAr} • رمز الدخول: **** (${user.pinCode})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    if (user.email.isNotBlank()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CloudDone,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "دعوة Google: ${user.email}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF10B981),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "⚠️ دخول محلي فقط (بدون إيميل سحابي)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (!isCurrent) {
                                OutlinedButton(
                                    onClick = { viewModel.setCurrentUser(user) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("تسجيل دخول", fontSize = 12.sp)
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "النشط حالياً",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Permission Badges
                        Text(
                            text = "الصلاحيات الممنوحة:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (user.role == UserRole.ADMIN) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "مدير النظام: كامل صلاحيات الإضافة والتعديل والحذف لجميع أقسام النظام",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    UserSectionBadge("المبيعات", user.canAddSales, user.canEditSales, user.canDeleteSales, Modifier.weight(1f))
                                    UserSectionBadge("المشتريات", user.canAddPurchases, user.canEditPurchases, user.canDeletePurchases, Modifier.weight(1f))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    UserSectionBadge("السندات", user.canAddVouchers, user.canEditVouchers, user.canDeleteVouchers, Modifier.weight(1f))
                                    UserSectionBadge("الاستبدال", user.canAddSwaps, user.canEditSwaps, user.canDeleteSwaps, Modifier.weight(1f))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    UserSectionBadge("التحويلات", user.canAddTransfers, user.canEditTransfers, user.canDeleteTransfers, Modifier.weight(1f))
                                    UserSectionBadge("الكروت", user.canAddCards, user.canEditCards, user.canDeleteCards, Modifier.weight(1f))
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    UserSectionBadge("التقارير", user.canAddReports, user.canEditReports, user.canDeleteReports, Modifier.weight(1f))
                                    if (user.canViewProfits) {
                                        PermissionBadge("رؤية الأرباح", true)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Edit / Delete Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            IconButton(onClick = { editingUser = user }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(20.dp))
                            }
                            if (users.size > 1 && user.role != UserRole.ADMIN) {
                                IconButton(onClick = { viewModel.deleteUser(user) }) {
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

        // Add User FAB
        FloatingActionButton(
            onClick = { showAddUserDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 60.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "إضافة مستخدم جديد")
        }
    }

    // Add / Edit User Dialog
    if (showAddUserDialog) {
        AddEditUserDialog(
            user = null,
            onDismiss = { showAddUserDialog = false },
            onSave = { newUser ->
                viewModel.addUser(newUser)
                showAddUserDialog = false
            },
            showMessage = { viewModel.showMessage(it) }
        )
    }

    editingUser?.let { userToEdit ->
        AddEditUserDialog(
            user = userToEdit,
            onDismiss = { editingUser = null },
            onSave = { updatedUser ->
                viewModel.updateUser(updatedUser)
                editingUser = null
            },
            showMessage = { viewModel.showMessage(it) }
        )
    }

    // Manager Broadcast Dialog
    if (showBroadcastDialog) {
        var notifTitle by remember { mutableStateOf("توجيه إداري من المدير") }
        var notifMessage by remember { mutableStateOf("") }
        var targetEmployee by remember { mutableStateOf("ALL") }

        Dialog(onDismissRequest = { showBroadcastDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "إرسال توجيه وإشعار إداري للموظفين",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = notifTitle,
                        onValueChange = { notifTitle = it },
                        label = { Text("عنوان الإشعار *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = notifMessage,
                        onValueChange = { notifMessage = it },
                        label = { Text("نص التوجيه والإشعار للموظفين *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showBroadcastDialog = false }) {
                            Text("إلغاء")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (notifMessage.isBlank()) {
                                    viewModel.showMessage("يرجى كتابة نص التوجيه")
                                    return@Button
                                }
                                viewModel.sendBroadcastNotification(notifTitle, notifMessage, targetEmployee)
                                showBroadcastDialog = false
                            }
                        ) {
                            Text("بث الإشعار للموظفين")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionBadge(label: String, granted: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (granted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (granted) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PermissionCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun SectionPermissionBox(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    canAdd: Boolean,
    onAddChange: (Boolean) -> Unit,
    canEdit: Boolean,
    onEditChange: (Boolean) -> Unit,
    canDelete: Boolean,
    onDeleteChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = accentColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                val allSelected = canAdd && canEdit && canDelete
                TextButton(
                    onClick = {
                        val target = !allSelected
                        onAddChange(target)
                        onEditChange(target)
                        onDeleteChange(target)
                    },
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(
                        text = if (allSelected) "إلغاء الكل" else "تحديد الكل",
                        fontSize = 11.sp,
                        color = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PermissionActionChip(
                    label = "إضافة",
                    checked = canAdd,
                    onCheckedChange = onAddChange,
                    modifier = Modifier.weight(1f)
                )
                PermissionActionChip(
                    label = "تعديل",
                    checked = canEdit,
                    onCheckedChange = onEditChange,
                    modifier = Modifier.weight(1f)
                )
                PermissionActionChip(
                    label = "حذف",
                    checked = canDelete,
                    onCheckedChange = onDeleteChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PermissionActionChip(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier.clickable { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                if (checked) Icons.Default.Check else Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun UserSectionBadge(
    title: String,
    canAdd: Boolean,
    canEdit: Boolean,
    canDelete: Boolean,
    modifier: Modifier = Modifier
) {
    val actions = mutableListOf<String>()
    if (canAdd) actions.add("إضافة")
    if (canEdit) actions.add("تعديل")
    if (canDelete) actions.add("حذف")

    val hasAny = actions.isNotEmpty()
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (hasAny) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Text(
            text = if (hasAny) "$title: ${actions.joinToString("، ")}" else "$title: معطل",
            fontSize = 10.sp,
            fontWeight = if (hasAny) FontWeight.SemiBold else FontWeight.Normal,
            color = if (hasAny) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

