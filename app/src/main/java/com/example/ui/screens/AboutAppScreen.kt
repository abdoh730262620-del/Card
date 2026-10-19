package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.PdfAndShareHelper

// Custom sleek dark colors matching the screenshot
private val DarkBgColor = Color(0xFF0F172A)
private val CardBgColor = Color(0xFF1E293B)
private val CardBorderColor = Color(0xFF334155)

@Composable
fun AboutAppScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showHelpCenterDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBgColor)
            .testTag("about_app_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Cards List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // 1. حول التطبيق والمميزات
                item {
                    AboutMenuItemCard(
                        title = "حول التطبيق والمميزات",
                        subtitle = "المميزات والقدرات الاحترافية للنظام",
                        icon = Icons.Default.PhoneAndroid,
                        iconContainerColor = Color(0xFF581C87),
                        iconTintColor = Color(0xFFC084FC),
                        badgeText = "v2.5.0",
                        badgeContainerColor = Color(0xFF3B0764),
                        badgeTextColor = Color(0xFFD8B4FE),
                        onClick = { showAboutDialog = true }
                    )
                }

                // 2. مركز المساعدة والدعم الفني
                item {
                    AboutMenuItemCard(
                        title = "مركز المساعدة والدعم الفني",
                        subtitle = "قنوات الاتصال المباشر، واتساب، الدعم الفني والأسئلة الشائعة",
                        icon = Icons.Default.HelpOutline,
                        iconContainerColor = Color(0xFF0369A1),
                        iconTintColor = Color(0xFF38BDF8),
                        onClick = { showHelpCenterDialog = true }
                    )
                }

                // 3. إرسال ملاحظات واقتراحات
                item {
                    AboutMenuItemCard(
                        title = "إرسال ملاحظات واقتراحات",
                        subtitle = "أرسل اقتراحاتك لتطوير التطبيق أو أبلغ عن أي مشكلة فنية",
                        icon = Icons.Default.Feedback,
                        iconContainerColor = Color(0xFFC2410C),
                        iconTintColor = Color(0xFFFB923C),
                        onClick = { showFeedbackDialog = true }
                    )
                }

                // 4. سياسة الخصوصية وحماية البيانات
                item {
                    AboutMenuItemCard(
                        title = "سياسة الخصوصية وحماية البيانات",
                        subtitle = "التزام الخصوصية 100%، التخزين المحلي الآمن، وحماية السجلات المالية",
                        icon = Icons.Default.Security,
                        iconContainerColor = Color(0xFF047857),
                        iconTintColor = Color(0xFF34D399),
                        onClick = { showPrivacyDialog = true }
                    )
                }

                // 5. بطاقة الشركة والمطور (Footer)
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBgColor)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "عالم تيك للبرمجيات والتطوير",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Aalam Tech for Software & Development",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "جميع الحقوق محفوظة © 2026",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }

    // --- Sub-Dialogs with Full Detailed Content ---

    // 1. Dialog: حول التطبيق والمميزات
    if (showAboutDialog) {
        AboutAppDetailDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    // 2. Dialog: مركز المساعدة والدعم الفني
    if (showHelpCenterDialog) {
        HelpCenterDetailDialog(
            context = context,
            onDismiss = { showHelpCenterDialog = false }
        )
    }

    // 3. Dialog: إرسال ملاحظات واقتراحات
    if (showFeedbackDialog) {
        FeedbackDetailDialog(
            context = context,
            onDismiss = { showFeedbackDialog = false }
        )
    }

    // 4. Dialog: سياسة الخصوصية وحماية البيانات
    if (showPrivacyDialog) {
        PrivacyPolicyDetailDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

@Composable
private fun AboutMenuItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconContainerColor: Color,
    iconTintColor: Color,
    badgeText: String? = null,
    badgeContainerColor: Color = Color(0xFF4C1D95),
    badgeTextColor: Color = Color(0xFFE9D5FF),
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Chevron indicator (Clickable indicator)
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Center Text Information (RTL Layout)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeContainerColor,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Right Icon Container with rounded squircle
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = iconContainerColor,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

// ----------------- DETAIL DIALOGS -----------------

@Composable
private fun AboutAppDetailDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
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
                color = CardBgColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .heightIn(max = maxHeight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF581C87),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Color(0xFFC084FC))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("حول التطبيق والمميزات", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("إصدار النظام v2.5.0", color = Color(0xFFD8B4FE), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "نظام كروت نت هو الحل البرمجي المتكامل لإدارة شبكات الواي فاي ومبيعات الكروت، صُمم خصيصاً لتسهيل متابعة الموزعين، الحسابات المالية، المخزون، والديون بدقة وموثوقية عالية.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("أبرز المميزات والقدرات:", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    val features = listOf(
                        "إدارة شاملة للموزعين ونقاط البيع مع رصد فوري للمديونيات وتجاوز الحدود.",
                        "إصدار فواتير بيع سريعة مع طباعة وإرسال إشعار فوري للموزع عبر واتساب.",
                        "نظام استبدال فئات الكروت التالفة أو المرتجعة بدقة تامة.",
                        "سندات قبض وصرف مع دعم التحصيل النقدي والحوالات البنكية.",
                        "رسوم بيانية وإحصائيات تفاعلية لمبيعات الكروت الشهرية ومراقبة أداء الموزعين.",
                        "نظام الصناديق والعهد المالية المنفصلة للموظفين (نظام العزل المالي).",
                        "حماية كاملة بالرمز السري PIN وبصمة الإصبع لمنع التلاعب.",
                        "وضع المطور للتحكم في الإعلانات وتخصيص أماكن وتوقيت ظهورها."
                    )

                    features.forEach { feat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("✦ ", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold)
                            Text(feat, color = Color(0xFFE2E8F0), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تم، حسناً", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpCenterDetailDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    var expandedFaqIndex by remember { mutableIntStateOf(-1) }

    Dialog(
        onDismissRequest = onDismiss,
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
                color = CardBgColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .heightIn(max = maxHeight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0369A1),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = Color(0xFF38BDF8))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("مركز المساعدة والدعم الفني", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("مساعدة فورية 24/7", color = Color(0xFF7DD3FC), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "اختر وسيلة التواصل المناسبة للحصول على الدعم والمساعدة الفنية المباشرة:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // WhatsApp Support Action
                    Button(
                        onClick = {
                            PdfAndShareHelper.shareViaWhatsApp(
                                context,
                                "777000000",
                                "السلام عليكم، أحتاج دعم فني بخصوص تطبيق إدارة كروت الشبكة..."
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مراسلة الدعم عبر واتساب", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Phone Call
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:777000000"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "الرقم: 777000000", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الاتصال الهاتفي المباشر (777000000)", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("الأسئلة الشائعة (FAQ):", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    val faqs = listOf(
                        "كيف أقوم بإضافة موزع جديد وتحديد حد ائتماني؟" to "من شاشة الموزعين اضغط على زر (+) وأدخل اسم الموزع ورقم الهاتف والحد الائتماني المالي ومهلة السداد بالأيام ليتم التنبيه تلقائياً.",
                        "كيف يتم إرسال إشعار الفاتورة للموزع؟" to "بمجرد حفظ عملية البيع أو سند القبض، يظهر زر إرسال إشعار فوري عبر واتساب يحتوي على تفاصيل الفاتورة والمبلغ المسدد والرصيد المتبقي.",
                        "كيف يمكنني ضبط أو إيقاف الإعلانات؟" to "من الإعدادات ثم الانتقال إلى 'وضع المطور والإعلانات'، يمكنك تفعيل أو إيقاف الإعلانات التجريبية واختيار شاشات الظهور.",
                        "هل التطبيق يعمل بدون إنترنت؟" to "نعم بالكامل! جميع المعاملات والمخزون وحسابات الموزعين تحفظ محلياً على جهازك في قاعدة بيانات مشفرة وتعمل بدون أي اتصال إنترنت."
                    )

                    faqs.forEachIndexed { index, (q, a) ->
                        val isExpanded = expandedFaqIndex == index
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { expandedFaqIndex = if (isExpanded) -1 else index },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(q, fontWeight = FontWeight.SemiBold, color = Color.White, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8)
                                    )
                                }
                                AnimatedVisibility(visible = isExpanded) {
                                    Column {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Divider(color = Color(0xFF334155))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(a, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8), lineHeight = 18.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("إغلاق", color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackDetailDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    var senderName by remember { mutableStateOf("") }
    var feedbackType by remember { mutableStateOf("اقتراح ميزة") }
    var messageText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
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
                color = CardBgColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .heightIn(max = maxHeight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFC2410C),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Feedback, contentDescription = null, tint = Color(0xFFFB923C))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("إرسال ملاحظات واقتراحات", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("مشاركتك تساهم في تطوير التطبيق", color = Color(0xFFFDBA74), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("نوع الملاحظة:", style = MaterialTheme.typography.bodySmall, color = Color(0xFFCBD5E1))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("اقتراح ميزة", "بلاغ مشكلة", "استفسار عام").forEach { type ->
                            FilterChip(
                                selected = feedbackType == type,
                                onClick = { feedbackType = type },
                                label = { Text(type, maxLines = 1, softWrap = false) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = senderName,
                        onValueChange = { senderName = it },
                        label = { Text("اسمك أو اسم الشبكة (اختياري)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        label = { Text("اكتب اقتراحك أو وصف المشكلة بالتفصيل *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 110.dp),
                        maxLines = 6
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (messageText.isBlank()) {
                                Toast.makeText(context, "يرجى كتابة نص الملاحظة أو الاقتراح", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val msg = buildString {
                                appendLine("📩 ملاحظة واقتراح جديد - تطبيق كروت نت")
                                appendLine("━━━━━━━━━━━━━━━━━━━")
                                appendLine("🔸 النوع: $feedbackType")
                                if (senderName.isNotBlank()) {
                                    appendLine("👤 المرسل: $senderName")
                                }
                                appendLine("📝 الرسالة:")
                                appendLine(messageText.trim())
                            }
                            PdfAndShareHelper.shareViaWhatsApp(context, "777000000", msg)
                            Toast.makeText(context, "شكراً لمشاركتك القيّمة!", Toast.LENGTH_LONG).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إرسال الاقتراح لفريق التطوير", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("إلغاء", color = Color(0xFF94A3B8))
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyPolicyDetailDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
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
                color = CardBgColor,
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderColor),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
                    .heightIn(max = maxHeight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF047857),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF34D399))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("سياسة الخصوصية وحماية البيانات", fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleMedium)
                                Text("التزام تام بالخصوصية 100%", color = Color(0xFFA7F3D0), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val privacyPoints = listOf(
                        "التخزين المحلي الآمن" to "جميع بيانات الموزعين، أسعار وفئات الكروت، السجلات المحاسبية والمالية محفوظة محلياً على ذاكرة جهازك فقط ولا يتم رفعها أو مشاركتها مع أي خوادم خارجية إلا برغبتك عبر النسخ الاحتياطي السحابي المشفر.",
                        "حماية السجلات المالية" to "البيانات المالية وأرصدة الديون وسندات القبض والصرف تعتبر بيانات سرية ومحمية برمز مرور وتشفير أمني متقدم.",
                        "عدم مشاركة البيانات" to "نحن لا نقوم ببيع أو مشاركة أو تأجير أي بيانات شخصية أو تجارية لأي طرف ثالث تحت أي ظرف من الظروف.",
                        "صلاحيات التطبيق" to "يطلب التطبيق فقط الحد الأدنى من الصلاحيات المطلوبة لتشغيل المزايا (مثل البصمة للحماية، والإنترنت لمزامنة النسخ الاحتياطية وإرسال رسائل الواتساب).",
                        "الإعلانات والشفافية" to "يستخدم التطبيق مكتبات إعلانية معتمدة من Google، ولا يتم تمرير أي معلومات مالية أو سجلات محاسبية لشبكات الإعلانات، مع إمكانية التحكم الكامل في الإعلانات عبر وضع المطور."
                    )

                    privacyPoints.forEach { (title, desc) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(title, fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleSmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8), lineHeight = 19.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("موافق وقبول السياسة", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
