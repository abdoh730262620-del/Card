package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Distributor
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.data.model.TransactionItem
import com.example.util.PdfAndShareHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicePreviewDialog(
    transaction: TransactionRecord,
    distributor: Distributor?,
    networkName: String,
    currency: String,
    items: List<TransactionItem> = emptyList(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isPurchase = transaction.type == TransactionType.PURCHASE
    val invoiceText = if (isPurchase) {
        PdfAndShareHelper.createPurchaseInvoiceMessage(
            transaction = transaction,
            networkName = networkName,
            currency = currency,
            items = items
        )
    } else {
        PdfAndShareHelper.createSaleInvoiceMessage(
            transaction = transaction,
            distributor = distributor,
            networkName = networkName,
            currency = currency,
            items = items
        )
    }

    val phone = distributor?.phone ?: ""
    val invoiceDate = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale.getDefault()).format(Date(transaction.date))
    val methodLabel = when (transaction.paymentMethod) {
        "CASH" -> "نقداً بالكامل"
        "TRANSFER" -> "حوالة بنكية"
        else -> "على الحساب (آجل)"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E293B)),
            color = Color(0xFF1E293B)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // ================= TOP ACTION BAR (ICONS AT TOP OF PREVIEW) =================
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PDF",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isPurchase) "معاينة فاتورة مشتريات #${transaction.invoiceNumber}" else "معاينة فاتورة مبيعات #${transaction.invoiceNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "مستند فاتورة رسمي للطباعة والمشاركة",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_invoice_button")) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    },
                    actions = {
                        // 1. WhatsApp Share Icon Button
                        IconButton(
                            onClick = {
                                PdfAndShareHelper.shareViaWhatsApp(context, phone, invoiceText)
                            },
                            modifier = Modifier.testTag("whatsapp_share_button")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF25D366).copy(alpha = 0.18f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = "إرسال عبر واتساب",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // 2. SMS Share Icon Button (if phone exists)
                        if (phone.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    PdfAndShareHelper.shareViaSms(context, phone, invoiceText)
                                },
                                modifier = Modifier.testTag("sms_share_button")
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF0284C7).copy(alpha = 0.18f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.PhoneAndroid,
                                            contentDescription = "رسالة SMS",
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Universal Mobile Share Icon Button
                        IconButton(
                            onClick = {
                                PdfAndShareHelper.shareViaUniversal(
                                    context,
                                    if (isPurchase) "فاتورة مشتريات #${transaction.invoiceNumber}" else "فاتورة مبيعات #${transaction.invoiceNumber}",
                                    invoiceText
                                )
                            },
                            modifier = Modifier.testTag("universal_share_button")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Share,
                                        contentDescription = "مشاركة برامج الجوال",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // 4. Print / PDF Export Icon Button
                        IconButton(
                            onClick = {
                                PdfAndShareHelper.shareViaUniversal(
                                    context,
                                    if (isPurchase) "طباعة فاتورة مشتريات #${transaction.invoiceNumber}" else "طباعة فاتورة مبيعات #${transaction.invoiceNumber}",
                                    invoiceText
                                )
                            },
                            modifier = Modifier.testTag("print_invoice_button")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Print,
                                        contentDescription = "طباعة / تصدير PDF",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Top Helper Quick-Labels Bar
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إجراءات الفاتورة:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        // Pill 1: WhatsApp
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF25D366).copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.4f)),
                            onClick = { PdfAndShareHelper.shareViaWhatsApp(context, phone, invoiceText) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(12.dp))
                                Text("إرسال واتساب", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF15803D))
                            }
                        }

                        // Pill 2: General Share
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                            onClick = {
                                PdfAndShareHelper.shareViaUniversal(
                                    context,
                                    if (isPurchase) "فاتورة مشتريات #${transaction.invoiceNumber}" else "فاتورة مبيعات #${transaction.invoiceNumber}",
                                    invoiceText
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Text("مشاركة للجوال", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        // Pill 3: Print PDF
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)),
                            onClick = {
                                PdfAndShareHelper.shareViaUniversal(
                                    context,
                                    if (isPurchase) "طباعة فاتورة مشتريات #${transaction.invoiceNumber}" else "طباعة فاتورة مبيعات #${transaction.invoiceNumber}",
                                    invoiceText
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(12.dp))
                                Text("طباعة الفاتورة", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }

                // ================= SIMULATED AUTHENTIC PDF INVOICE SHEET =================
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF334155)) // Professional dark PDF canvas
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxSize(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Official Document Header
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Top Row: Network Branding & Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF1E3A8A)
                                        ) {
                                            Text(
                                                text = "🌐 $networkName",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isPurchase) Color(0xFFFEF3C7) else Color(0xFFEFF6FF),
                                            border = BorderStroke(1.dp, if (isPurchase) Color(0xFFFDE68A) else Color(0xFFBFDBFE))
                                        ) {
                                            Text(
                                                text = if (isPurchase) "توريد كروت للمخزن" else "مبيعات شبكة",
                                                color = if (isPurchase) Color(0xFFB45309) else Color(0xFF1E40AF),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Main Title
                                    Text(
                                        text = if (isPurchase) "فاتورة مشتريات وتوريد كروت" else "فاتورة مبيعات كروت إنترنت",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0F172A),
                                        textAlign = TextAlign.Center
                                    )

                                    Text(
                                        text = "رقم الفاتورة: #${transaction.invoiceNumber}  •  التاريخ: $invoiceDate",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B),
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .background(Color(0xFF1E3A8A))
                                    )
                                }
                            }

                            // 2. Party & Payment Details Box
                            item {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1E3A8A), modifier = Modifier.size(16.dp))
                                                Text(
                                                    text = if (isPurchase) "المورد: ${transaction.distributorName.ifBlank { "المورد الرئيسي" }}" else "العميل / الموزع: ${transaction.distributorName.ifBlank { "عميل نقدي مباشر" }}",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A),
                                                    fontSize = 13.sp
                                                )
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when (transaction.paymentMethod) {
                                                    "CASH" -> Color(0xFFDCFCE7)
                                                    "TRANSFER" -> Color(0xFFE0E7FF)
                                                    else -> Color(0xFFFEE2E2)
                                                }
                                            ) {
                                                Text(
                                                    text = methodLabel,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (transaction.paymentMethod) {
                                                        "CASH" -> Color(0xFF15803D)
                                                        "TRANSFER" -> Color(0xFF4338CA)
                                                        else -> Color(0xFFB91C1C)
                                                    },
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        if (phone.isNotBlank()) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = "الهاتف: $phone",
                                                    color = Color(0xFF475569),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }

                                        if (transaction.employeeName.isNotBlank()) {
                                            Text(
                                                text = "الموظف المنفذ: ${transaction.employeeName}",
                                                color = Color(0xFF64748B),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // 3. Items Table Header
                            item {
                                Surface(
                                    color = Color(0xFF1E3A8A),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "#", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                                        Text(text = "الصنف / فئة الكرت", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2.5f), textAlign = TextAlign.Start)
                                        Text(text = "الكمية", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.0f), textAlign = TextAlign.Center)
                                        Text(text = "السعر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                                        Text(text = "الإجمالي", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                                    }
                                }
                            }

                            // 4. Items Table Rows
                            if (items.isNotEmpty()) {
                                items(items.size) { index ->
                                    val item = items[index]
                                    val rowBg = if (index % 2 == 0) Color.White else Color(0xFFF8FAFC)
                                    Surface(
                                        color = rowBg,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 7.dp, horizontal = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "${index + 1}", fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                                            Text(text = item.categoryName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A), modifier = Modifier.weight(2.5f), textAlign = TextAlign.Start)
                                            Text(text = "${item.quantity}", fontSize = 11.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(1.0f), textAlign = TextAlign.Center)
                                            Text(text = PdfAndShareHelper.formatCurrency(item.unitPrice, currency), fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                                            Text(text = PdfAndShareHelper.formatCurrency(item.totalAmount, currency), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                }
                            } else {
                                // Single-item fallback row
                                item {
                                    Surface(
                                        color = Color.White,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 7.dp, horizontal = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "1", fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                                            Text(text = transaction.categoryName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A), modifier = Modifier.weight(2.5f), textAlign = TextAlign.Start)
                                            Text(text = "${transaction.quantity}", fontSize = 11.sp, color = Color(0xFF0F172A), modifier = Modifier.weight(1.0f), textAlign = TextAlign.Center)
                                            Text(text = if (transaction.unitPrice > 0) PdfAndShareHelper.formatCurrency(transaction.unitPrice, currency) else "-", fontSize = 11.sp, color = Color(0xFF475569), modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                                            Text(text = PdfAndShareHelper.formatCurrency(transaction.totalAmount, currency), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.weight(1.3f), textAlign = TextAlign.End)
                                        }
                                    }
                                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                }
                            }

                            // 5. Financial Summary Card
                            item {
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        if (transaction.discount > 0) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("الخصم الممنوح:", color = Color(0xFF475569), fontSize = 12.sp)
                                                Text(PdfAndShareHelper.formatCurrency(transaction.discount, currency), color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("إجمالي الفاتورة الصافي:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                                            Text(PdfAndShareHelper.formatCurrency(transaction.totalAmount, currency), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF1E3A8A))
                                        }

                                        HorizontalDivider(color = Color(0xFFE2E8F0))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("المدفوع نقداً للصندوق:", color = Color(0xFF475569), fontSize = 12.sp)
                                            Text(PdfAndShareHelper.formatCurrency(transaction.paidAmount, currency), color = Color(0xFF16A34A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("المتبقي آجل:", color = Color(0xFF475569), fontSize = 12.sp)
                                            Text(
                                                PdfAndShareHelper.formatCurrency(transaction.remainingAmount, currency),
                                                color = if (transaction.remainingAmount > 0) Color(0xFFDC2626) else Color(0xFF475569),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }

                                        if (!isPurchase && distributor != null) {
                                            HorizontalDivider(color = Color(0xFFE2E8F0))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("رصيد العميل الإجمالي الحالي:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0F172A))
                                                Text(PdfAndShareHelper.formatCurrency(distributor.currentBalance, currency), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFDC2626))
                                            }
                                            if (distributor.creditLimit > 0) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("الحد الائتماني المسموح:", color = Color(0xFF64748B), fontSize = 11.sp)
                                                    Text(PdfAndShareHelper.formatCurrency(distributor.creditLimit, currency), color = Color(0xFF64748B), fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 6. Notes if any
                            if (transaction.notes.isNotBlank()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFFEF3C7).copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "ملاحظات الفاتورة: ${transaction.notes}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF92400E),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }

                            // 7. Official Stamps and Signature Section
                            item {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Stamp Box
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(80.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "ختم الفاتورة المعتمد",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF64748B),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Surface(
                                                shape = CircleShape,
                                                color = Color(0xFF1E3A8A).copy(alpha = 0.08f),
                                                border = BorderStroke(1.dp, Color(0xFF1E3A8A).copy(alpha = 0.3f)),
                                                modifier = Modifier.size(38.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = "مدفوع",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF1E3A8A),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Receiver / Accountant Signature
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(80.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (isPurchase) "توقيع المستلم / المخازن" else "توقيع المحاسب / البائع",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF64748B),
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(0.8f)
                                                    .height(1.dp)
                                                    .background(Color(0xFF94A3B8))
                                            )
                                            Text(
                                                text = "شكراً لتعاملكم معنا 🙏",
                                                fontSize = 10.sp,
                                                color = Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
