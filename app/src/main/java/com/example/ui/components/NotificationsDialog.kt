package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NotificationType
import com.example.data.model.SystemNotification
import com.example.ui.NetCardsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationsDialog(
    viewModel: NetCardsViewModel,
    onDismiss: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val visibleNotifications = remember(notifications, currentUser) {
        if (currentUser.role.name == "ADMIN" || currentUser.role.name == "SUPERVISOR" || currentUser.displayName == "المدير العام") {
            notifications
        } else {
            notifications.filter {
                it.targetEmployee == "ALL" ||
                it.targetEmployee.equals(currentUser.displayName, ignoreCase = true) ||
                it.targetEmployee.equals(currentUser.username, ignoreCase = true) ||
                it.senderName.equals(currentUser.displayName, ignoreCase = true) ||
                it.senderName.equals(currentUser.username, ignoreCase = true)
            }
        }
    }

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredNotifications = remember(visibleNotifications, selectedFilter) {
        when (selectedFilter) {
            "TRANSFERS" -> visibleNotifications.filter { it.type == NotificationType.TRANSFER }
            "SWAPS" -> visibleNotifications.filter { it.type == NotificationType.SWAP }
            "UNREAD" -> visibleNotifications.filter { !it.isRead }
            else -> visibleNotifications
        }
    }

    val transfersCount = visibleNotifications.count { it.type == NotificationType.TRANSFER }
    val swapsCount = visibleNotifications.count { it.type == NotificationType.SWAP }
    val visibleUnreadCount = visibleNotifications.count { !it.isRead }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize().testTag("notifications_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
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
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "مركز الإشعارات والتنبيهات",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (visibleUnreadCount > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(2.dp)
                                    ) {
                                        Text(
                                            text = "$visibleUnreadCount جديد",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "إشعارات تحويلات المخازن واستبدال الكروت والعمليات",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Bar (Mark all as read, Clear all)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filter Chips Row
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("الكل (${visibleNotifications.size})", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "TRANSFERS",
                            onClick = { selectedFilter = "TRANSFERS" },
                            label = { Text("التحويلات ($transfersCount)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "SWAPS",
                            onClick = { selectedFilter = "SWAPS" },
                            label = { Text("الاستبدال ($swapsCount)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedFilter == "UNREAD",
                            onClick = { selectedFilter = "UNREAD" },
                            label = { Text("غير مقروء ($visibleUnreadCount)", fontSize = 11.sp) }
                        )
                    }

                    Row {
                        if (visibleUnreadCount > 0) {
                            TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("قراءة الكل", fontSize = 11.sp)
                            }
                        }
                        if (notifications.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearAllNotifications() }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "مسح الكل",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Notifications List
                if (filteredNotifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.NotificationsNone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = "لا توجد إشعارات حالياً",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "ستظهر هنا إشعارات تحويلات المخازن واستبدال الكروت مع تفاصيل الكميات",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredNotifications, key = { it.id }) { notif ->
                            NotificationItemCard(
                                notification = notif,
                                onMarkAsRead = { viewModel.markNotificationAsRead(notif.id) },
                                onDelete = { viewModel.deleteNotification(notif.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationItemCard(
    notification: SystemNotification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(notification.date) {
        SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(notification.date))
    }

    val isTransferIn = notification.title.contains("وارد") || notification.title.contains("استلام")
    val isTransferOut = notification.title.contains("صادر") || notification.title.contains("تحويل")

    val icon = when (notification.type) {
        NotificationType.TRANSFER -> if (isTransferIn) Icons.Default.CallReceived else Icons.Default.CallMade
        NotificationType.SWAP -> Icons.Default.CompareArrows
        NotificationType.CREDIT_LIMIT, NotificationType.LOW_STOCK, NotificationType.DUE_PAYMENT -> Icons.Default.Warning
        NotificationType.MANAGER_MESSAGE -> Icons.Default.Campaign
        else -> Icons.Default.Notifications
    }

    val iconTint = when (notification.type) {
        NotificationType.TRANSFER -> if (isTransferIn) Color(0xFF16A34A) else Color(0xFFEA580C)
        NotificationType.SWAP -> Color(0xFF0D9488)
        NotificationType.CREDIT_LIMIT, NotificationType.LOW_STOCK -> MaterialTheme.colorScheme.error
        NotificationType.DUE_PAYMENT -> Color(0xFFB45309)
        NotificationType.MANAGER_MESSAGE -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }

    val cardBg = if (!notification.isRead) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!notification.isRead) onMarkAsRead() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(
            1.dp,
            if (!notification.isRead) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Icon, Title, Date, Unread Dot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = iconTint.copy(alpha = 0.15f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = notification.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (!notification.isRead) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            }
                        }
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!notification.isRead) {
                        IconButton(
                            onClick = onMarkAsRead,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "تحديد كمقروء",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Notification Message content with highlighted quantity boxes
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    notification.message.lines().forEach { line ->
                        if (line.contains("قبل:") || line.contains("بعد:")) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Inventory,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        } else if (line.contains("الرصيد") || line.contains("الفارق المالي:")) {
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        } else {
                            Text(
                                text = line,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
