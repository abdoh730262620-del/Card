package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.Distributor
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.data.model.TransactionItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfAndShareHelper {

    private val dateFormatter = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar"))
    private val shortDateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale("ar"))

    fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        return shortDateFormatter.format(Date(timestamp))
    }

    fun shareText(context: Context, text: String, title: String = "مشاركة") {
        shareViaUniversal(context, title, text)
    }

    fun formatCurrency(amount: Double, currency: String = "ريال"): String {
        return "%,.0f %s".format(Locale.US, amount, currency)
    }

    /**
     * نص إشعار واتساب / رسالة نصية عند كل عملية بيع لموزع
     */
    fun createSaleInvoiceMessage(
        transaction: TransactionRecord,
        distributor: Distributor?,
        networkName: String,
        currency: String,
        items: List<TransactionItem> = emptyList()
    ): String {
        val dateStr = dateFormatter.format(Date(transaction.date))
        val distName = distributor?.name ?: transaction.distributorName.ifBlank { "عميل نقدي مباشر" }
        val currentBalance = distributor?.currentBalance ?: transaction.remainingAmount
        val creditLimit = distributor?.creditLimit ?: 0.0

        val methodLabel = when (transaction.paymentMethod) {
            "CASH" -> "نقداً"
            "TRANSFER" -> "حوالة"
            else -> "على الحساب (آجل)"
        }

        return buildString {
            appendLine("🌐 *$networkName*")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("🧾 *إشعار فاتورة بيع كروت*")
            appendLine("🔹 رقم الفاتورة: #${transaction.invoiceNumber}")
            appendLine("📅 التاريخ: $dateStr")
            appendLine("👤 العميل / الموزع: $distName")
            appendLine("💳 طريقة البيع: $methodLabel")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            if (items.isNotEmpty()) {
                appendLine("📋 *أصناف الفاتورة:*")
                items.forEachIndexed { index, itm ->
                    appendLine("${index + 1}. ${itm.categoryName}: ${itm.quantity} كرت × ${formatCurrency(itm.unitPrice, currency)} = ${formatCurrency(itm.totalAmount, currency)}")
                }
                appendLine("📦 إجمالي عدد الكروت: ${transaction.quantity} كرت")
            } else {
                appendLine("🏷 الفئة: ${transaction.categoryName}")
                appendLine("📦 الكمية: ${transaction.quantity} كرت")
                if (transaction.unitPrice > 0) {
                    appendLine("💵 سعر الكرت: ${formatCurrency(transaction.unitPrice, currency)}")
                }
            }
            if (transaction.discount > 0) {
                appendLine("🎁 الخصم الممنوح: ${formatCurrency(transaction.discount, currency)}")
            }
            appendLine("💰 إجمالي الفاتورة: ${formatCurrency(transaction.totalAmount, currency)}")
            appendLine("💵 المدفوع: ${formatCurrency(transaction.paidAmount, currency)}")
            appendLine("⏳ المتبقي آجل: ${formatCurrency(transaction.remainingAmount, currency)}")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            if (distributor != null) {
                appendLine("📊 *الرصيد الإجمالي الحالي: ${formatCurrency(currentBalance, currency)}*")
                if (creditLimit > 0) {
                    appendLine("🛡 الحد الائتماني المسموح: ${formatCurrency(creditLimit, currency)}")
                }
                if (distributor.paymentPeriodDays > 0) {
                    appendLine("⏰ مهلة السداد المحددة: ${distributor.paymentPeriodDays} يوماً")
                }
            }
            if (transaction.notes.isNotBlank()) {
                appendLine("📝 ملاحظات: ${transaction.notes}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("شكراً لتعاملكم معنا 🙏")
        }
    }

    /**
     * نص إشعار واتساب ومشاركة لفاتورة المشتريات والتوريد
     */
    fun createPurchaseInvoiceMessage(
        transaction: TransactionRecord,
        networkName: String,
        currency: String,
        items: List<TransactionItem> = emptyList()
    ): String {
        val dateStr = dateFormatter.format(Date(transaction.date))
        val methodLabel = when (transaction.paymentMethod) {
            "CASH" -> "نقداً"
            "TRANSFER" -> "حوالة"
            else -> "آجل للمورد"
        }

        return buildString {
            appendLine("🌐 *$networkName*")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("📦 *فاتورة مشتريات وتوريد كروت*")
            appendLine("🔹 رقم الفاتورة: #${transaction.invoiceNumber}")
            appendLine("📅 التاريخ: $dateStr")
            appendLine("🏢 المورد: ${transaction.distributorName.ifBlank { "المورد الرئيسي" }}")
            appendLine("💳 نوع الدفع: $methodLabel")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            if (items.isNotEmpty()) {
                appendLine("📋 *أصناف المشتريات:*")
                items.forEachIndexed { index, itm ->
                    appendLine("${index + 1}. ${itm.categoryName}: ${itm.quantity} كرت × ${formatCurrency(itm.unitPrice, currency)} = ${formatCurrency(itm.totalAmount, currency)}")
                }
                appendLine("📦 إجمالي عدد الكروت: ${transaction.quantity} كرت")
            } else {
                appendLine("🏷 الفئة: ${transaction.categoryName}")
                appendLine("📦 الكمية: ${transaction.quantity} كرت")
                if (transaction.unitPrice > 0) {
                    appendLine("💵 سعر التكلفة: ${formatCurrency(transaction.unitPrice, currency)}")
                }
            }
            if (transaction.discount > 0) {
                appendLine("🎁 خصم الشراء: ${formatCurrency(transaction.discount, currency)}")
            }
            appendLine("💰 إجمالي الفاتورة: ${formatCurrency(transaction.totalAmount, currency)}")
            appendLine("💵 المبلغ المدفوع: ${formatCurrency(transaction.paidAmount, currency)}")
            if (transaction.remainingAmount > 0) {
                appendLine("⏳ المتبقي للمورد: ${formatCurrency(transaction.remainingAmount, currency)}")
            }
            if (transaction.notes.isNotBlank()) {
                appendLine("📝 ملاحظات: ${transaction.notes}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("تم التوريد لرصيد المخزن بنجاح ✅")
        }
    }

    /**
     * رسالة تذكير بالسداد وفترة الاستحقاق للموزع
     */
    fun createDuePaymentReminderMessage(
        distributor: Distributor,
        networkName: String,
        currency: String
    ): String {
        val days = distributor.daysSinceLastPayment()
        val lastDate = shortDateFormatter.format(Date(distributor.lastPaymentDate))

        return buildString {
            appendLine("⚠️ *تذكير استحقاق سداد - $networkName*")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("الأخ الموزع الكريم / *${distributor.name}* المحترم")
            appendLine("نود تذكيركم بضرورة تسديد الرصيد المستحق لحساب كروت الشبكة:")
            appendLine("💰 *المبلغ المستحق: ${formatCurrency(distributor.currentBalance, currency)}*")
            appendLine("🛡 الحد الائتماني: ${formatCurrency(distributor.creditLimit, currency)}")
            appendLine("📅 تاريخ آخر دفعة: $lastDate (منذ $days يوم)")
            appendLine("⏰ مهلة السداد المسموحة: ${distributor.paymentPeriodDays} يوماً")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("يرجى المبادرة بالسداد لضمان استمرار تغذية الرصيد ونقاط البيع.")
            appendLine("شاكرين ومقدرين حسن تعاونكم.")
        }
    }

    /**
     * كشف حساب مختصر
     */
    fun createSummaryAccountReport(
        distributor: Distributor,
        transactions: List<TransactionRecord>,
        networkName: String,
        currency: String
    ): String {
        val totalPurchases = transactions.filter { it.type == TransactionType.SALE }.sumOf { it.totalAmount }
        val totalReturns = transactions.filter { it.type == TransactionType.RETURN || it.type == TransactionType.SALE_RETURN }.sumOf { it.totalAmount }
        val totalPayments = transactions.filter { it.type == TransactionType.PAYMENT_IN }.sumOf { it.paidAmount } +
                transactions.filter { it.type == TransactionType.SALE }.sumOf { it.paidAmount }
        val totalDiscounts = transactions.filter { it.type == TransactionType.DISCOUNT }.sumOf { it.totalAmount }

        return buildString {
            appendLine("📑 *كشف حساب مختصر - $networkName*")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("👤 الموزع: *${distributor.name}*")
            appendLine("📱 الهاتف: ${distributor.phone}")
            appendLine("📅 التاريخ: ${shortDateFormatter.format(Date())}")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("• الرصيد السابق: ${formatCurrency(distributor.openingBalance, currency)}")
            appendLine("• إجمالي المسحوبات: ${formatCurrency(totalPurchases, currency)}")
            appendLine("• إجمالي المرتجعات: ${formatCurrency(totalReturns, currency)}")
            appendLine("• إجمالي المدفوعات: ${formatCurrency(totalPayments, currency)}")
            appendLine("• إجمالي الخصومات: ${formatCurrency(totalDiscounts, currency)}")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("🔴 *الرصيد المتبقي المستحق: ${formatCurrency(distributor.currentBalance, currency)}*")
            appendLine("🛡 الحد الائتماني: ${formatCurrency(distributor.creditLimit, currency)}")
            appendLine("━━━━━━━━━━━━━━━━━━━")
        }
    }

    /**
     * كشف حساب مختصر للمورد
     */
    fun createSummarySupplierReport(
        supplierName: String,
        transactions: List<TransactionRecord>,
        networkName: String,
        currency: String
    ): String {
        val totalPurchases = transactions.filter { it.type == TransactionType.PURCHASE }.sumOf { it.totalAmount }
        val totalReturns = transactions.filter { it.type == TransactionType.PURCHASE_RETURN }.sumOf { it.totalAmount }
        val totalPaid = transactions.filter { it.type == TransactionType.PURCHASE || it.type == TransactionType.PAYMENT_OUT }.sumOf { it.paidAmount }

        return buildString {
            appendLine("📑 *كشف حساب مورد مختصر - $networkName*")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("🏢 المورد / الشركة: *$supplierName*")
            appendLine("📅 التاريخ: ${shortDateFormatter.format(Date())}")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("• إجمالي المشتريات: ${formatCurrency(totalPurchases, currency)}")
            appendLine("• إجمالي المرتجعات: ${formatCurrency(totalReturns, currency)}")
            appendLine("• إجمالي المدفوعات للمورد: ${formatCurrency(totalPaid, currency)}")
            appendLine("━━━━━━━━━━━━━━━━━━━")
            appendLine("📌 صافي المعاملات والمدفوعات للمورد")
        }
    }

    /**
     * كشف حساب شامل وتفصيلي للمورد مع الحركات
     */
    fun createDetailedSupplierReport(
        supplierName: String,
        transactions: List<TransactionRecord>,
        networkName: String,
        currency: String
    ): String {
        return buildString {
            appendLine("📋 *كشف حساب مورد تفصيلي شامل - $networkName*")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🏢 المورد / الشركة: *$supplierName*")
            appendLine("📅 تاريخ التقرير: ${dateFormatter.format(Date())}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📊 *سجل فواتير وحركات المورد:*")
            appendLine("────────────────────────────")

            if (transactions.isEmpty()) {
                appendLine("لا توجد حركات مسجلة لهذا المورد حتى الآن.")
            } else {
                transactions.forEachIndexed { index, tx ->
                    val txDate = shortDateFormatter.format(Date(tx.date))
                    val typeAr = when (tx.type) {
                        TransactionType.PURCHASE -> "فاتورة مشتريات [${tx.categoryName}] (${tx.quantity} كرت)"
                        TransactionType.PURCHASE_RETURN -> "مرتجع مشتريات [${tx.categoryName}] (${tx.quantity} كرت)"
                        TransactionType.PAYMENT_OUT -> "سند صرف / دفعة للمورد"
                        else -> tx.type.name
                    }
                    appendLine("${index + 1}) #$txDate | فاتورة #${tx.invoiceNumber}")
                    appendLine("   🔸 البيان: $typeAr")
                    appendLine("   💰 إجمالي المبلغ: ${formatCurrency(tx.totalAmount, currency)} | مسدد: ${formatCurrency(tx.paidAmount, currency)}")
                    appendLine("────────────────────────────")
                }
            }

            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("نظام كروت نت لإدارة شبكات الواي فاي")
        }
    }

    /**
     * كشف حساب شامل وتفصيلي مع الحركات
     */
    fun createDetailedAccountReport(
        distributor: Distributor,
        transactions: List<TransactionRecord>,
        networkName: String,
        currency: String
    ): String {
        return buildString {
            appendLine("📋 *كشف حساب تفصيلي شامل - $networkName*")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("👤 الموزع: *${distributor.name}* | 📱 الهاتف: ${distributor.phone}")
            appendLine("📅 تاريخ التقرير: ${dateFormatter.format(Date())}")
            appendLine("💼 الرصيد الافتتاحي: ${formatCurrency(distributor.openingBalance, currency)}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📊 *سجل الحركات المالية والفواتير:*")
            appendLine("────────────────────────────")

            if (transactions.isEmpty()) {
                appendLine("لا توجد حركات مسجلة حتى الآن.")
            } else {
                transactions.forEachIndexed { index, tx ->
                    val txDate = shortDateFormatter.format(Date(tx.date))
                    val typeAr = when (tx.type) {
                        TransactionType.SALE -> "مبيعات [${tx.categoryName}] (${tx.quantity} كرت)"
                        TransactionType.PURCHASE -> "شراء مشتريات"
                        TransactionType.RETURN -> "مرتجع [${tx.categoryName}] (${tx.quantity} كرت)"
                        TransactionType.SALE_RETURN -> "مرتجع مبيعات [${tx.categoryName}] (${tx.quantity} كرت)"
                        TransactionType.PURCHASE_RETURN -> "مرتجع مشتريات للمورد [${tx.categoryName}] (${tx.quantity} كرت)"
                        TransactionType.PAYMENT_IN -> "سند قبض نقدي"
                        TransactionType.PAYMENT_OUT -> "سند صرف مصروفات"
                        TransactionType.DISCOUNT -> "خصم ممنوح"
                        TransactionType.SWAP -> "استبدال فئات"
                    }
                    appendLine("${index + 1}) #$txDate | ${tx.invoiceNumber}")
                    appendLine("   🔸 البيان: $typeAr")
                    appendLine("   💰 المبلغ: ${formatCurrency(tx.totalAmount, currency)} | مسدد: ${formatCurrency(tx.paidAmount, currency)}")
                    if (tx.remainingAmount > 0) {
                        appendLine("   ⏳ متبقي: ${formatCurrency(tx.remainingAmount, currency)}")
                    }
                    appendLine("────────────────────────────")
                }
            }

            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📌 *الرصيد الإجمالي النهائي المستحق: ${formatCurrency(distributor.currentBalance, currency)}*")
            appendLine("🛡 الحد الائتماني: ${formatCurrency(distributor.creditLimit, currency)}")
            appendLine("⏰ مهلة السداد: ${distributor.paymentPeriodDays} يوماً")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("نظام كروت نت لإدارة شبكات الواي فاي")
        }
    }

    /**
     * إرسال عبر واتساب للموزع
     */
    fun shareViaWhatsApp(context: Context, phone: String, message: String) {
        try {
            // Clean phone number (strip spaces and +)
            val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to generic share chooser
            shareViaUniversal(context, "إشعار حساب كروت الشبكة", message)
        }
    }

    /**
     * إرسال عبر رسالة نصية SMS
     */
    fun shareViaSms(context: Context, phone: String, message: String) {
        try {
            val uri = Uri.parse("smsto:${phone.trim()}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            shareViaUniversal(context, "إشعار حساب", message)
        }
    }

    /**
     * مشاركة شاملة عبر أي برنامج تواصل (واتساب، تيليجرام، بريد، إلخ)
     */
    fun shareViaUniversal(context: Context, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, subject).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * تقرير جرد مخزن وعهدة الموظف
     */
    fun createEmployeeWarehouseInventoryReport(
        employeeName: String,
        items: List<Pair<String, Pair<Int, Double>>>,
        networkName: String,
        currency: String
    ): String {
        val totalQty = items.sumOf { it.second.first }
        val totalVal = items.sumOf { it.second.first * it.second.second }
        val dateStr = dateFormatter.format(Date())

        return buildString {
            appendLine("📦 *تقرير جرد مخزن وعهدة الموظف*")
            appendLine("🌐 *$networkName*")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("👤 اسم الموظف / صاحب المخزن: *$employeeName*")
            appendLine("📅 تاريخ ووقت الجرد: $dateStr")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📋 *محتويات المخزن الحالية:*")
            if (items.isEmpty() || totalQty == 0) {
                appendLine("لا توجد كروت حالياً في مخزن هذا الموظف (الرصيد: 0 كرت)")
            } else {
                items.filter { it.second.first > 0 }.forEachIndexed { idx, item ->
                    val name = item.first
                    val qty = item.second.first
                    val face = item.second.second
                    val lineTotal = qty * face
                    appendLine("${idx + 1}. $name | الكمية: $qty كرت | القيمة: ${formatCurrency(lineTotal, currency)}")
                }
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📊 *إجمالي عدد الكروت في المخزن:* $totalQty كرت")
            appendLine("💰 *إجمالي القيمة التقديرية:* ${formatCurrency(totalVal, currency)}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("توقيع المستلم (الموظف): ....................")
            appendLine("توقيع المسؤول المعتمد: ....................")
        }
    }

    /**
     * كشف حساب حركة عهد وتحويلات الموظف
     */
    fun createEmployeeTransfersStatementReport(
        employeeName: String,
        transfers: List<com.example.data.model.EmployeeTransferRecord>,
        networkName: String,
        currency: String
    ): String {
        val dateStr = dateFormatter.format(Date())
        val incoming = transfers.filter { it.toEmployee == employeeName }
        val outgoing = transfers.filter { it.fromEmployee == employeeName }

        val totalInQty = incoming.sumOf { it.quantity }
        val totalInVal = incoming.sumOf { it.totalFaceValue }
        val totalOutQty = outgoing.sumOf { it.quantity }
        val totalOutVal = outgoing.sumOf { it.totalFaceValue }

        return buildString {
            appendLine("📑 *كشف حساب حركة عهد وتحويلات الموظف*")
            appendLine("🌐 *$networkName*")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("👤 الموظف: *$employeeName*")
            appendLine("📅 تاريخ الإصدار: $dateStr")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📊 *ملخص إجمالي الحركات:*")
            appendLine("📥 إجمالي الوارد (استلام عهدة): $totalInQty كرت (${formatCurrency(totalInVal, currency)})")
            appendLine("📤 إجمالي الصادر (تسليم عهدة): $totalOutQty كرت (${formatCurrency(totalOutVal, currency)})")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📋 *سجل الحركات التفصيلية:*")
            if (transfers.isEmpty()) {
                appendLine("لا توجد تحويلات مسجلة لهذا الموظف حتى الآن")
            } else {
                transfers.forEachIndexed { idx, trf ->
                    val trfDate = formatShortDate(trf.date)
                    val isIncoming = trf.toEmployee == employeeName
                    val direction = if (isIncoming) "📥 وارد من [${trf.fromEmployee}]" else "📤 صادر إلى [${trf.toEmployee}]"
                    val desc = if (trf.transferType == "CARDS") "${trf.quantity} كرت [${trf.categoryName}]" else "عهدة نقدية: ${formatCurrency(trf.cashAmount, currency)}"
                    appendLine("${idx + 1}. #${trf.transferCode} | $trfDate | $direction")
                    appendLine("   🔹 البيان: $desc (القيمة: ${formatCurrency(trf.totalFaceValue, currency)})")
                    if (trf.notes.isNotBlank()) {
                        appendLine("   📝 ملاحظة: ${trf.notes}")
                    }
                }
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("المعتمد المالي / إدارة النظام")
        }
    }

    /**
     * كشف حساب شامل لحركة المخزن والعهدة (تحويلات + مبيعات ومردودات)
     */
    fun createEmployeeComprehensiveAccountStatement(
        employeeName: String,
        transfers: List<com.example.data.model.EmployeeTransferRecord>,
        salesTransactions: List<com.example.data.model.TransactionRecord>,
        currentStockCards: Int,
        currentStockValue: Double,
        networkName: String,
        currency: String
    ): String {
        val dateStr = dateFormatter.format(Date())
        val incoming = transfers.filter { it.toEmployee == employeeName }
        val outgoing = transfers.filter { it.fromEmployee == employeeName }
        val empSales = salesTransactions.filter { it.employeeName == employeeName && it.type == TransactionType.SALE }

        val totalInQty = incoming.sumOf { it.quantity }
        val totalInVal = incoming.sumOf { it.totalFaceValue }
        val totalOutQty = outgoing.sumOf { it.quantity }
        val totalOutVal = outgoing.sumOf { it.totalFaceValue }
        val totalSalesQty = empSales.sumOf { it.quantity }
        val totalSalesVal = empSales.sumOf { it.totalAmount }

        return buildString {
            appendLine("📑 *كشف حساب حركة مخزن وعهدة الموظف*")
            appendLine("🌐 *$networkName*")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("👤 الموظف المسؤول: *$employeeName*")
            appendLine("📅 تاريخ الإصدار: $dateStr")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📊 *الملخص العام لحركة المخزن:*")
            appendLine("📥 إجمالي الوارد للمخزن: $totalInQty كرت (${formatCurrency(totalInVal, currency)})")
            appendLine("📤 إجمالي الصادر من المخزن: $totalOutQty كرت (${formatCurrency(totalOutVal, currency)})")
            appendLine("🛒 إجمالي مبيعات العهدة: $totalSalesQty كرت (${formatCurrency(totalSalesVal, currency)})")
            appendLine("📦 *الرصيد المتبقي الحالي:* $currentStockCards كرت (${formatCurrency(currentStockValue, currency)})")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📋 *تفاصيل الحركات:*")

            var count = 1
            incoming.forEach { trf ->
                val trfDate = formatShortDate(trf.date)
                appendLine("${count++}. [📥 تحويل وارد] $trfDate | #${trf.transferCode}")
                appendLine("   من: ${trf.fromEmployee} | ${trf.quantity} كرت [${trf.categoryName}] (${formatCurrency(trf.totalFaceValue, currency)})")
            }

            outgoing.forEach { trf ->
                val trfDate = formatShortDate(trf.date)
                appendLine("${count++}. [📤 تحويل صادر] $trfDate | #${trf.transferCode}")
                appendLine("   إلى: ${trf.toEmployee} | ${trf.quantity} كرت [${trf.categoryName}] (${formatCurrency(trf.totalFaceValue, currency)})")
            }

            empSales.forEach { sale ->
                val saleDate = formatShortDate(sale.date)
                appendLine("${count++}. [🛒 مبيعات] $saleDate | #${sale.invoiceNumber}")
                appendLine("   للعميل: ${sale.distributorName} | ${sale.quantity} كرت [${sale.categoryName}] (${formatCurrency(sale.totalAmount, currency)})")
            }

            if (incoming.isEmpty() && outgoing.isEmpty() && empSales.isEmpty()) {
                appendLine("لا توجد حركات مسجلة لمخزن هذا الموظف حتى الآن.")
            }

            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("توقيع المستلم: ....................")
            appendLine("ختم الإدارة المعتمد: ....................")
        }
    }
}
