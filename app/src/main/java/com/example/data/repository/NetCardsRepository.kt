package com.example.data.repository

import com.example.data.local.NetCardsDao
import com.example.data.model.AppUser
import com.example.data.model.CardCategory
import com.example.data.model.CardSwapRecord
import com.example.data.model.Distributor
import com.example.data.model.EmployeeTransferRecord
import com.example.data.model.NetworkSettings
import com.example.data.model.NotificationType
import com.example.data.model.SystemNotification
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.data.model.TransactionItem
import com.example.data.model.InvoiceItemDraft
import kotlinx.coroutines.flow.Flow
import com.example.data.firebase.FirestoreSyncRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NetCardsRepository(
    private val dao: NetCardsDao,
    private val firestoreSync: FirestoreSyncRepository? = null
) {

    private var effectiveCloudUserId: String? = null

    fun setEffectiveCloudUserId(userId: String?) {
        effectiveCloudUserId = userId
    }

    fun getEffectiveCloudUserId(): String? = effectiveCloudUserId

    private fun syncToCloud(block: suspend (FirestoreSyncRepository, String) -> Unit) {
        val uid = effectiveCloudUserId ?: Firebase.auth.currentUser?.uid
        if (uid != null && firestoreSync != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    block(firestoreSync, uid)
                } catch (e: Exception) {
                    android.util.Log.e("NetCardsRepo", "Cloud sync push error: ${e.message}")
                }
            }
        }
    }

    fun startCloudSync(userId: String, scope: CoroutineScope) {
        effectiveCloudUserId = userId
        firestoreSync?.startRealtimeSync(userId, dao, scope)
    }

    val categories: Flow<List<CardCategory>> = dao.getAllCategories()
    val distributors: Flow<List<Distributor>> = dao.getAllDistributors()
    val transactions: Flow<List<TransactionRecord>> = dao.getAllTransactions()
    val cardSwaps: Flow<List<CardSwapRecord>> = dao.getAllCardSwaps()
    val employeeTransfers: Flow<List<EmployeeTransferRecord>> = dao.getAllEmployeeTransfers()
    val users: Flow<List<AppUser>> = dao.getAllUsers()
    val notifications: Flow<List<SystemNotification>> = dao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = dao.getUnreadNotificationsCount()
    val settings: Flow<NetworkSettings?> = dao.getSettings()
    val employeeStocks: Flow<List<com.example.data.model.EmployeeStock>> = dao.getAllEmployeeStocks()

    // === Cash Registers & Fund Isolation ===
    val allCashRegisters: Flow<List<com.example.data.model.CashRegister>> = dao.getAllCashRegisters()
    val mainTreasury: Flow<com.example.data.model.CashRegister?> = dao.getMainTreasury()
    val employeeFunds: Flow<List<com.example.data.model.CashRegister>> = dao.getEmployeeFunds()
    val allFundMovements: Flow<List<com.example.data.model.FundMovement>> = dao.getAllFundMovements()
    val mainTreasuryMovements: Flow<List<com.example.data.model.FundMovement>> = dao.getMainTreasuryMovements()

    fun getMovementsForRegister(registerId: Long): Flow<List<com.example.data.model.FundMovement>> =
        dao.getMovementsByRegister(registerId)

    fun getEmployeeMovements(username: String): Flow<List<com.example.data.model.FundMovement>> =
        dao.getEmployeeFundMovements(username)

    fun getEmployeeFund(username: String): Flow<com.example.data.model.CashRegister?> =
        dao.getEmployeeFundByUsername(username)

    suspend fun getSettingsDirect(): NetworkSettings? = dao.getSettingsDirect()

    suspend fun saveSettings(settings: NetworkSettings) {
        dao.saveSettings(settings)
        syncToCloud { sync, uid -> sync.syncSettings(uid, settings) }
    }

    // === Category & Stock Management ===
    suspend fun insertCategory(category: CardCategory): Long {
        val id = dao.insertCategory(category)
        syncToCloud { sync, uid -> sync.syncCategory(uid, category.copy(id = id)) }
        return id
    }

    suspend fun updateCategory(category: CardCategory) {
        dao.updateCategory(category)
        syncToCloud { sync, uid -> sync.syncCategory(uid, category) }
    }

    suspend fun deleteCategory(category: CardCategory) {
        dao.deleteCategory(category)
        syncToCloud { sync, uid -> sync.deleteCategory(uid, category.id) }
    }

    suspend fun adjustCategoryStock(categoryId: Long, diff: Int) {
        dao.adjustCategoryStock(categoryId, diff)
        dao.getCategoryById(categoryId)?.let { cat ->
            syncToCloud { sync, uid -> sync.syncCategory(uid, cat) }
        }
    }

    // === Distributor Management ===
    suspend fun insertDistributor(distributor: Distributor): Long {
        val id = dao.insertDistributor(distributor)
        syncToCloud { sync, uid -> sync.syncDistributor(uid, distributor.copy(id = id)) }
        return id
    }

    suspend fun updateDistributor(distributor: Distributor) {
        dao.updateDistributor(distributor)
        syncToCloud { sync, uid -> sync.syncDistributor(uid, distributor) }
    }

    suspend fun deleteDistributor(distributor: Distributor) {
        dao.deleteDistributor(distributor)
        syncToCloud { sync, uid -> sync.deleteDistributor(uid, distributor.id) }
    }

    suspend fun getDistributorById(id: Long): Distributor? = dao.getDistributorById(id)
    fun getTransactionsForDistributor(distributorId: Long): Flow<List<TransactionRecord>> =
        dao.getTransactionsByDistributor(distributorId)

    // === Core Business Operations ===

    /**
     * عملية بيع كروت لموزع أو بيع مباشر
     */
    suspend fun recordSale(
        distributorId: Long?,
        categoryId: Long,
        quantity: Int,
        unitPrice: Double,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String,
        employeeName: String,
        notes: String
    ): Result<TransactionRecord> {
        val category = dao.getCategoryById(categoryId)
            ?: return Result.failure(Exception("الفئة غير موجودة"))

        if (category.stockQuantity < quantity) {
            return Result.failure(Exception("الكمية المتوفرة بالمخزن (${category.stockQuantity}) غير كافية للبيع المطلوب ($quantity)"))
        }

        val totalAmount = (quantity * unitPrice) - discount
        val remainingAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)

        var distName = "بيع مباشر نقدي"
        var distributor: Distributor? = null
        if (distributorId != null) {
            distributor = dao.getDistributorById(distributorId)
            distName = distributor?.name ?: "موزع غير معروف"
        }

        val invNum = ((System.currentTimeMillis() % 900000 + 100000)).toString()
        val transaction = TransactionRecord(
            invoiceNumber = invNum,
            type = TransactionType.SALE,
            distributorId = distributorId,
            distributorName = distName,
            categoryId = categoryId,
            categoryName = category.name,
            quantity = quantity,
            unitPrice = unitPrice,
            totalAmount = totalAmount,
            discount = discount,
            paidAmount = paidAmount,
            remainingAmount = remainingAmount,
            paymentMethod = paymentMethod,
            date = System.currentTimeMillis(),
            employeeName = employeeName,
            notes = notes
        )

        // 1. Decrement stock
        dao.adjustCategoryStock(categoryId, -quantity)

        // 2. Adjust distributor balance if debt remaining or payment made
        if (distributorId != null && distributor != null) {
            val netDelta = remainingAmount
            val newBalance = distributor.currentBalance + netDelta
            dao.adjustDistributorBalance(distributorId, netDelta)

            if (paidAmount > 0) {
                dao.recordDistributorPayment(distributorId, 0.0, System.currentTimeMillis())
            }

            // Check credit limit alert
            if (newBalance > distributor.creditLimit) {
                insertNotification(
                    SystemNotification(
                        title = "تنبيه تجاوز الحد الائتماني",
                        message = "الموزع '${distributor.name}' تجاوز حده الائتماني المسموح به (${formatCurrency(distributor.creditLimit)}) حيث بلغ رصيده (${formatCurrency(newBalance)}).",
                        type = NotificationType.CREDIT_LIMIT,
                        date = System.currentTimeMillis()
                    )
                )
            }
        }

        // 3. Insert transaction record
        val id = dao.insertTransaction(transaction)
        val finalTx = transaction.copy(id = id)

        // إشعار للمدير بخصوص عملية البيع الجديدة
        val payTypeText = if (paymentMethod.equals("CASH", true) || paymentMethod.equals("نقدي", true)) "نقدي" else "أجل"
        insertNotification(
            SystemNotification(
                title = "عملية بيع جديدة للموظف: $employeeName",
                message = "العميل/الموزع: $distName | الإجمالي: ${formatCurrency(totalAmount)} | النوع: $payTypeText",
                type = NotificationType.GENERAL,
                targetEmployee = "المدير العام",
                senderName = employeeName,
                date = System.currentTimeMillis()
            )
        )

        syncToCloud { sync, uid ->
            sync.syncTransaction(uid, finalTx)
            dao.getCategoryById(categoryId)?.let { sync.syncCategory(uid, it) }
            if (distributorId != null) {
                dao.getDistributorById(distributorId)?.let { sync.syncDistributor(uid, it) }
            }
        }
        return Result.success(finalTx)
    }

    /**
     * عملية بيع كروت متعددة الأصناف مع إمكانية إنشاء عميل جديد واختيار طريقة الدفع
     */
    suspend fun recordMultiItemSale(
        distributorId: Long?,
        newCustomerName: String? = null,
        newCustomerPhone: String? = null,
        items: List<InvoiceItemDraft>,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String,
        employeeName: String,
        notes: String
    ): Result<TransactionRecord> {
        if (items.isEmpty()) {
            return Result.failure(Exception("يرجى إضافة صنف واحد على الأقل للفاتورة"))
        }

        // Validate stock for all items
        for (item in items) {
            val cat = dao.getCategoryById(item.categoryId)
                ?: return Result.failure(Exception("الفئة '${item.categoryName}' غير موجودة"))
            if (cat.stockQuantity < item.quantity) {
                return Result.failure(Exception("الكمية المتوفرة بالمخزن للفئة '${cat.name}' (${cat.stockQuantity}) غير كافية للبيع المطلوب (${item.quantity})"))
            }
        }

        // Determine distributor or create new customer
        var effectiveDistId = distributorId
        var distName = "عميل نقدي مباشر"
        var distributor: Distributor? = null

        if (!newCustomerName.isNullOrBlank()) {
            val trimmedName = newCustomerName.trim()
            val newDist = Distributor(
                name = trimmedName,
                phone = newCustomerPhone?.trim() ?: "",
                openingBalance = 0.0,
                currentBalance = 0.0,
                creditLimit = 100000.0,
                paymentPeriodDays = 30,
                notes = "عميل جديد تم إنشاؤه أثناء الفاتورة"
            )
            val newId = dao.insertDistributor(newDist)
            effectiveDistId = newId
            distName = trimmedName
            distributor = newDist.copy(id = newId)
        } else if (distributorId != null) {
            distributor = dao.getDistributorById(distributorId)
            distName = distributor?.name ?: "موزع غير معروف"
        }

        val subtotal = items.sumOf { it.quantity * it.unitPrice }
        val totalAmount = (subtotal - discount).coerceAtLeast(0.0)
        val remainingAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)
        val totalQuantity = items.sumOf { it.quantity }

        val categorySummary = if (items.size == 1) {
            items.first().categoryName
        } else {
            items.joinToString("، ") { "${it.categoryName} (${it.quantity})" }
        }

        val invNum = ((System.currentTimeMillis() % 900000 + 100000)).toString()
        val transaction = TransactionRecord(
            invoiceNumber = invNum,
            type = TransactionType.SALE,
            distributorId = effectiveDistId,
            distributorName = distName,
            categoryId = items.firstOrNull()?.categoryId,
            categoryName = categorySummary,
            quantity = totalQuantity,
            unitPrice = if (items.isNotEmpty()) items.first().unitPrice else 0.0,
            totalAmount = totalAmount,
            discount = discount,
            paidAmount = paidAmount,
            remainingAmount = remainingAmount,
            paymentMethod = paymentMethod,
            date = System.currentTimeMillis(),
            employeeName = employeeName,
            notes = notes
        )

        // 1. Decrement stock for each item
        for (item in items) {
            dao.adjustCategoryStock(item.categoryId, -item.quantity)
        }

        // 2. Adjust distributor balance if debt remaining or payment made
        if (effectiveDistId != null && distributor != null) {
            val netDelta = remainingAmount
            val newBalance = distributor.currentBalance + netDelta
            dao.adjustDistributorBalance(effectiveDistId, netDelta)

            if (paidAmount > 0) {
                dao.recordDistributorPayment(effectiveDistId, 0.0, System.currentTimeMillis())
            }

            if (newBalance > distributor.creditLimit) {
                insertNotification(
                    SystemNotification(
                        title = "تنبيه تجاوز الحد الائتماني",
                        message = "الموزع '${distributor.name}' تجاوز حده الائتماني المسموح به (${formatCurrency(distributor.creditLimit)}) حيث بلغ رصيده (${formatCurrency(newBalance)}).",
                        type = NotificationType.CREDIT_LIMIT,
                        date = System.currentTimeMillis()
                    )
                )
            }
        }

        // 3. Insert transaction record
        val id = dao.insertTransaction(transaction)
        val finalTx = transaction.copy(id = id)

        // إشعار للمدير بعملية البيع المتعددة الجديدة
        val distCustomerName = distributor?.name ?: newCustomerName ?: "بيع مباشر"
        val payTypeText = if (paymentMethod.equals("CASH", true) || paymentMethod.equals("نقدي", true)) "نقدي" else "أجل"
        insertNotification(
            SystemNotification(
                title = "عملية بيع متعددة للموظف: $employeeName",
                message = "العميل: $distCustomerName | الإجمالي: ${formatCurrency(totalAmount)} | النوع: $payTypeText",
                type = NotificationType.GENERAL,
                targetEmployee = "المدير العام",
                senderName = employeeName,
                date = System.currentTimeMillis()
            )
        )

        // 4. Insert transaction items
        val txItems = items.map { item ->
            TransactionItem(
                transactionId = id,
                categoryId = item.categoryId,
                categoryName = item.categoryName,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                totalAmount = item.quantity * item.unitPrice
            )
        }
        dao.insertTransactionItems(txItems)

        syncToCloud { sync, uid ->
            sync.syncTransaction(uid, finalTx)
            for (item in items) {
                dao.getCategoryById(item.categoryId)?.let { sync.syncCategory(uid, it) }
            }
            if (effectiveDistId != null) {
                dao.getDistributorById(effectiveDistId)?.let { sync.syncDistributor(uid, it) }
            }
        }

        return Result.success(finalTx)
    }

    /**
     * عملية شراء / توريد كروت للمخزن
     */
    suspend fun recordPurchase(
        categoryId: Long,
        quantity: Int,
        costPrice: Double,
        supplierName: String,
        employeeName: String,
        notes: String
    ): Result<TransactionRecord> {
        val category = dao.getCategoryById(categoryId)
            ?: return Result.failure(Exception("الفئة غير موجودة"))

        val totalAmount = quantity * costPrice
        val invNum = ((System.currentTimeMillis() % 900000 + 100000)).toString()

        val transaction = TransactionRecord(
            invoiceNumber = invNum,
            type = TransactionType.PURCHASE,
            distributorId = null,
            distributorName = supplierName.ifBlank { "المورد الرئيسي" },
            categoryId = categoryId,
            categoryName = category.name,
            quantity = quantity,
            unitPrice = costPrice,
            totalAmount = totalAmount,
            discount = 0.0,
            paidAmount = totalAmount,
            remainingAmount = 0.0,
            paymentMethod = "CASH",
            date = System.currentTimeMillis(),
            employeeName = employeeName,
            notes = notes
        )

        dao.adjustCategoryStock(categoryId, quantity)

        // الربط بالصندوق: خصم قيمة المشتريات من الصندوق (Outflow)
        val mainFund = dao.getMainTreasuryDirect()
        if (mainFund != null) {
            val balanceAfter = mainFund.currentBalance - totalAmount
            dao.adjustRegisterBalance(mainFund.id, -totalAmount, 0.0, totalAmount)
            dao.insertFundMovement(
                com.example.data.model.FundMovement(
                    registerId = mainFund.id,
                    registerName = mainFund.name,
                    isEmployeeFund = false,
                    direction = com.example.data.model.MovementDirection.OUTFLOW,
                    amount = totalAmount,
                    balanceAfter = balanceAfter,
                    title = "شراء وتوريد كروت مخزن (${category.name}) - $quantity كرت",
                    referenceType = "PURCHASE",
                    referenceNumber = invNum,
                    date = System.currentTimeMillis(),
                    notes = "فاتورة مشتريات #$invNum - ${supplierName.ifBlank { "المورد الرئيسي" }}"
                )
            )
        }

        val id = dao.insertTransaction(transaction)
        val finalTx = transaction.copy(id = id)
        syncToCloud { sync, uid ->
            sync.syncTransaction(uid, finalTx)
            dao.getCategoryById(categoryId)?.let { sync.syncCategory(uid, it) }
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(finalTx)
    }

    /**
     * عملية شراء / توريد كروت متعددة الأصناف مع إمكانية تحديد مورد جديد ونوع الدفع والمبلغ والخصم
     */
    suspend fun recordMultiItemPurchase(
        items: List<InvoiceItemDraft>,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String,
        supplierName: String,
        employeeName: String,
        notes: String
    ): Result<TransactionRecord> {
        if (items.isEmpty()) {
            return Result.failure(Exception("يرجى إضافة صنف واحد على الأقل لفاتورة المشتريات"))
        }

        val subtotal = items.sumOf { it.quantity * it.unitPrice }
        val totalAmount = (subtotal - discount).coerceAtLeast(0.0)
        val remainingAmount = (totalAmount - paidAmount).coerceAtLeast(0.0)
        val totalQuantity = items.sumOf { it.quantity }

        val categorySummary = if (items.size == 1) {
            items.first().categoryName
        } else {
            items.joinToString("، ") { "${it.categoryName} (${it.quantity})" }
        }

        val invNum = ((System.currentTimeMillis() % 900000 + 100000)).toString()
        val transaction = TransactionRecord(
            invoiceNumber = invNum,
            type = TransactionType.PURCHASE,
            distributorId = null,
            distributorName = supplierName.ifBlank { "المورد الرئيسي" },
            categoryId = items.firstOrNull()?.categoryId,
            categoryName = categorySummary,
            quantity = totalQuantity,
            unitPrice = if (items.isNotEmpty()) items.first().unitPrice else 0.0,
            totalAmount = totalAmount,
            discount = discount,
            paidAmount = paidAmount,
            remainingAmount = remainingAmount,
            paymentMethod = paymentMethod,
            date = System.currentTimeMillis(),
            employeeName = employeeName,
            notes = notes
        )

        // Increment stock for each category
        for (item in items) {
            dao.adjustCategoryStock(item.categoryId, item.quantity)
        }

        // الربط بالصندوق: خصم المبلغ المسدد نقداً من الصندوق (Outflow)
        if (paidAmount > 0) {
            val mainFund = dao.getMainTreasuryDirect()
            if (mainFund != null) {
                val balanceAfter = mainFund.currentBalance - paidAmount
                dao.adjustRegisterBalance(mainFund.id, -paidAmount, 0.0, paidAmount)
                dao.insertFundMovement(
                    com.example.data.model.FundMovement(
                        registerId = mainFund.id,
                        registerName = mainFund.name,
                        isEmployeeFund = false,
                        direction = com.example.data.model.MovementDirection.OUTFLOW,
                        amount = paidAmount,
                        balanceAfter = balanceAfter,
                        title = "شراء وتوريد كروت مخزن ($categorySummary) - $totalQuantity كرت",
                        referenceType = "PURCHASE",
                        referenceNumber = invNum,
                        date = System.currentTimeMillis(),
                        notes = "فاتورة مشتريات #$invNum - ${supplierName.ifBlank { "المورد الرئيسي" }}"
                    )
                )
            }
        }

        val id = dao.insertTransaction(transaction)
        val finalTx = transaction.copy(id = id)

        val txItems = items.map { item ->
            TransactionItem(
                transactionId = id,
                categoryId = item.categoryId,
                categoryName = item.categoryName,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                totalAmount = item.quantity * item.unitPrice
            )
        }
        dao.insertTransactionItems(txItems)

        syncToCloud { sync, uid ->
            sync.syncTransaction(uid, finalTx)
            for (item in items) {
                dao.getCategoryById(item.categoryId)?.let { sync.syncCategory(uid, it) }
            }
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }

        return Result.success(finalTx)
    }

    suspend fun getTransactionItems(transactionId: Long): List<TransactionItem> {
        return dao.getTransactionItems(transactionId)
    }

    /**
     * حذف فاتورة مبيعات مع إعادة الكروت للمخزن وتعديل ديون الموزع واسترداد الصندوق
     */
    suspend fun deleteSale(sale: TransactionRecord): Result<Boolean> {
        // 1. إعادة الكروت المباعة إلى رصيد المخزن
        if (sale.categoryId != null) {
            dao.adjustCategoryStock(sale.categoryId, sale.quantity)
        }
        // 2. إلغاء الدين المسجل على الموزع إن وجد
        if (sale.distributorId != null && sale.remainingAmount > 0) {
            dao.adjustDistributorBalance(sale.distributorId, -sale.remainingAmount)
        }
        // 3. خصم النقدية من الصندوق في حال تم تحصيل مبلغ نقدي
        if (sale.paidAmount > 0) {
            val mainFund = dao.getMainTreasuryDirect()
            if (mainFund != null) {
                val balanceAfter = mainFund.currentBalance - sale.paidAmount
                dao.adjustRegisterBalance(mainFund.id, -sale.paidAmount, 0.0, sale.paidAmount)
                dao.insertFundMovement(
                    com.example.data.model.FundMovement(
                        registerId = mainFund.id,
                        registerName = mainFund.name,
                        isEmployeeFund = false,
                        direction = com.example.data.model.MovementDirection.OUTFLOW,
                        amount = sale.paidAmount,
                        balanceAfter = balanceAfter,
                        title = "استرداد نقدي - إلغاء وحذف فاتورة مبيعات (${sale.invoiceNumber})",
                        referenceType = "SALE_CANCEL",
                        referenceNumber = sale.invoiceNumber,
                        date = System.currentTimeMillis(),
                        notes = "حذف الفاتورة واسترجاع الكروت للمخزن"
                    )
                )
            }
        }
        dao.deleteTransaction(sale)

        // إشعار للمدير بحذف الفاتورة
        insertNotification(
            SystemNotification(
                title = "إلغاء وحذف فاتورة بيع (${sale.invoiceNumber})",
                message = "العميل/الموزع: ${sale.distributorName} | المبلغ: ${formatCurrency(sale.totalAmount)}",
                type = NotificationType.GENERAL,
                targetEmployee = "المدير العام",
                senderName = sale.employeeName.ifBlank { "موظف" },
                date = System.currentTimeMillis()
            )
        )

        syncToCloud { sync, uid ->
            sync.deleteTransaction(uid, sale.id)
            if (sale.categoryId != null) {
                dao.getCategoryById(sale.categoryId)?.let { sync.syncCategory(uid, it) }
            }
            if (sale.distributorId != null) {
                dao.getDistributorById(sale.distributorId)?.let { sync.syncDistributor(uid, it) }
            }
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(true)
    }

    /**
     * تعديل فاتورة مبيعات مع تسوية فارق المخزون والديون والنقدية
     */
    suspend fun updateSale(
        oldSale: TransactionRecord,
        newQuantity: Int,
        newUnitPrice: Double,
        newDiscount: Double,
        newPaidAmount: Double,
        newPaymentMethod: String,
        newNotes: String
    ): Result<TransactionRecord> {
        val categoryId = oldSale.categoryId
            ?: return Result.failure(Exception("الفئة غير محددة"))
        val category = dao.getCategoryById(categoryId)
            ?: return Result.failure(Exception("الفئة غير موجودة"))

        val quantityDelta = newQuantity - oldSale.quantity
        if (quantityDelta > 0 && category.stockQuantity < quantityDelta) {
            return Result.failure(Exception("الكمية المتوفرة بالمخزن لا تكفي للزيادة المطلوبة"))
        }

        val newTotal = (newQuantity * newUnitPrice) - newDiscount
        val newRemaining = (newTotal - newPaidAmount).coerceAtLeast(0.0)

        // تسوية فارق المخزن
        dao.adjustCategoryStock(categoryId, -quantityDelta)

        // تسوية فارق ديون الموزع
        if (oldSale.distributorId != null) {
            val debtDelta = newRemaining - oldSale.remainingAmount
            if (debtDelta != 0.0) {
                dao.adjustDistributorBalance(oldSale.distributorId, debtDelta)
            }
        }

        // تسوية فارق الصندوق
        val cashDelta = newPaidAmount - oldSale.paidAmount
        if (cashDelta != 0.0) {
            val mainFund = dao.getMainTreasuryDirect()
            if (mainFund != null) {
                if (cashDelta > 0) {
                    val balanceAfter = mainFund.currentBalance + cashDelta
                    dao.adjustRegisterBalance(mainFund.id, cashDelta, cashDelta, 0.0)
                    dao.insertFundMovement(
                        com.example.data.model.FundMovement(
                            registerId = mainFund.id,
                            registerName = mainFund.name,
                            isEmployeeFund = false,
                            direction = com.example.data.model.MovementDirection.INFLOW,
                            amount = cashDelta,
                            balanceAfter = balanceAfter,
                            title = "فارق زيادة تحصيل - تعديل فاتورة (${oldSale.invoiceNumber})",
                            referenceType = "SALE_EDIT",
                            referenceNumber = oldSale.invoiceNumber,
                            date = System.currentTimeMillis()
                        )
                    )
                } else {
                    val refund = -cashDelta
                    val balanceAfter = mainFund.currentBalance - refund
                    dao.adjustRegisterBalance(mainFund.id, -refund, 0.0, refund)
                    dao.insertFundMovement(
                        com.example.data.model.FundMovement(
                            registerId = mainFund.id,
                            registerName = mainFund.name,
                            isEmployeeFund = false,
                            direction = com.example.data.model.MovementDirection.OUTFLOW,
                            amount = refund,
                            balanceAfter = balanceAfter,
                            title = "فارق استرداد نقدي - تعديل فاتورة (${oldSale.invoiceNumber})",
                            referenceType = "SALE_EDIT",
                            referenceNumber = oldSale.invoiceNumber,
                            date = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        val updated = oldSale.copy(
            quantity = newQuantity,
            unitPrice = newUnitPrice,
            totalAmount = newTotal,
            discount = newDiscount,
            paidAmount = newPaidAmount,
            remainingAmount = newRemaining,
            paymentMethod = newPaymentMethod,
            notes = newNotes
        )
        dao.updateTransaction(updated)

        // إشعار للمدير بتعديل الفاتورة
        insertNotification(
            SystemNotification(
                title = "تعديل فاتورة بيع (${updated.invoiceNumber})",
                message = "العميل/الموزع: ${updated.distributorName} | المبلغ الجديد: ${formatCurrency(updated.totalAmount)}",
                type = NotificationType.GENERAL,
                targetEmployee = "المدير العام",
                senderName = updated.employeeName.ifBlank { "موظف" },
                date = System.currentTimeMillis()
            )
        )

        syncToCloud { sync, uid ->
            sync.syncTransaction(uid, updated)
            dao.getCategoryById(categoryId)?.let { sync.syncCategory(uid, it) }
            if (oldSale.distributorId != null) {
                dao.getDistributorById(oldSale.distributorId)?.let { sync.syncDistributor(uid, it) }
            }
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(updated)
    }

    /**
     * حذف فاتورة مشتريات مع خصم الكروت من المخزن واستعادة المبلغ للصندوق
     */
    suspend fun deletePurchase(purchase: TransactionRecord): Result<Boolean> {
        // 1. خصم الكروت المشتراة من رصيد المخزن
        if (purchase.categoryId != null) {
            dao.adjustCategoryStock(purchase.categoryId, -purchase.quantity)
        }
        // 2. استعادة المبلغ المدفوع إلى الصندوق (Inflow)
        if (purchase.totalAmount > 0) {
            val mainFund = dao.getMainTreasuryDirect()
            if (mainFund != null) {
                val balanceAfter = mainFund.currentBalance + purchase.totalAmount
                dao.adjustRegisterBalance(mainFund.id, purchase.totalAmount, purchase.totalAmount, 0.0)
                dao.insertFundMovement(
                    com.example.data.model.FundMovement(
                        registerId = mainFund.id,
                        registerName = mainFund.name,
                        isEmployeeFund = false,
                        direction = com.example.data.model.MovementDirection.INFLOW,
                        amount = purchase.totalAmount,
                        balanceAfter = balanceAfter,
                        title = "استرداد قيمة شراء - إلغاء فاتورة (${purchase.invoiceNumber})",
                        referenceType = "PURCHASE_CANCEL",
                        referenceNumber = purchase.invoiceNumber,
                        date = System.currentTimeMillis(),
                        notes = "حذف فاتورة الشراء واسترجاع المبلغ للصندوق"
                    )
                )
            }
        }
        dao.deleteTransaction(purchase)
        syncToCloud { sync, uid ->
            sync.deleteTransaction(uid, purchase.id)
            if (purchase.categoryId != null) {
                dao.getCategoryById(purchase.categoryId)?.let { sync.syncCategory(uid, it) }
            }
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(true)
    }

    /**
     * تعديل فاتورة مشتريات مع تسوية فارق المخزن ومبلغ الشراء بالصندوق
     */
    suspend fun updatePurchase(
        oldPurchase: TransactionRecord,
        newQuantity: Int,
        newCostPrice: Double,
        newSupplierName: String,
        newNotes: String
    ): Result<TransactionRecord> {
        val categoryId = oldPurchase.categoryId
            ?: return Result.failure(Exception("الفئة غير محددة"))

        val quantityDelta = newQuantity - oldPurchase.quantity
        val newTotal = newQuantity * newCostPrice
        val costDelta = newTotal - oldPurchase.totalAmount

        // 1. تسوية فارق رصيد المخزن
        dao.adjustCategoryStock(categoryId, quantityDelta)

        // 2. تسوية فارق الصندوق
        if (costDelta != 0.0) {
            val mainFund = dao.getMainTreasuryDirect()
            if (mainFund != null) {
                if (costDelta > 0) {
                    val balanceAfter = mainFund.currentBalance - costDelta
                    dao.adjustRegisterBalance(mainFund.id, -costDelta, 0.0, costDelta)
                    dao.insertFundMovement(
                        com.example.data.model.FundMovement(
                            registerId = mainFund.id,
                            registerName = mainFund.name,
                            isEmployeeFund = false,
                            direction = com.example.data.model.MovementDirection.OUTFLOW,
                            amount = costDelta,
                            balanceAfter = balanceAfter,
                            title = "صرف إضافي - تعديل فاتورة شراء (${oldPurchase.invoiceNumber})",
                            referenceType = "PURCHASE_EDIT",
                            referenceNumber = oldPurchase.invoiceNumber,
                            date = System.currentTimeMillis()
                        )
                    )
                } else {
                    val refund = -costDelta
                    val balanceAfter = mainFund.currentBalance + refund
                    dao.adjustRegisterBalance(mainFund.id, refund, refund, 0.0)
                    dao.insertFundMovement(
                        com.example.data.model.FundMovement(
                            registerId = mainFund.id,
                            registerName = mainFund.name,
                            isEmployeeFund = false,
                            direction = com.example.data.model.MovementDirection.INFLOW,
                            amount = refund,
                            balanceAfter = balanceAfter,
                            title = "استرداد فارغ تكلفة - تعديل فاتورة شراء (${oldPurchase.invoiceNumber})",
                            referenceType = "PURCHASE_EDIT",
                            referenceNumber = oldPurchase.invoiceNumber,
                            date = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        val updated = oldPurchase.copy(
            distributorName = newSupplierName.ifBlank { "المورد الرئيسي" },
            quantity = newQuantity,
            unitPrice = newCostPrice,
            totalAmount = newTotal,
            paidAmount = newTotal,
            notes = newNotes
        )
        dao.updateTransaction(updated)
        syncToCloud { sync, uid ->
            sync.syncTransaction(uid, updated)
            dao.getCategoryById(categoryId)?.let { sync.syncCategory(uid, it) }
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(updated)
    }

    /**
     * عملية مرتجع مبيعات كروت من عميل/موزع إلى المخزن
     * مع إعادة الكروت للمخزن، وتسوية حساب العميل/الموزع أو استرداد النقد من الصندوق
     */
    suspend fun recordSaleReturn(
        distributorId: Long?,
        categoryId: Long,
        quantity: Int,
        unitPrice: Double,
        refundFromCash: Boolean, // هل تم رد المبلغ نقداً من الصندوق أم خصمه من مديونية الموزع
        employeeName: String,
        notes: String
    ): Result<TransactionRecord> {
        val category = dao.getCategoryById(categoryId)
            ?: return Result.failure(Exception("الفئة غير موجودة"))

        val totalAmount = quantity * unitPrice
        var distName = "عميل نقدي"
        var distributor: Distributor? = null
        if (distributorId != null) {
            distributor = dao.getDistributorById(distributorId)
            distName = distributor?.name ?: "موزع غير معروف"
        }

        // 1. إعادة الكروت المرتجعة إلى رصيد المخزن
        dao.adjustCategoryStock(categoryId, quantity)

        // 2. تسوية الحساب أو الصندوق
        if (refundFromCash) {
            // رد نقدي من الصندوق (Outflow)
            val mainFund = dao.getMainTreasuryDirect()
            if (mainFund != null) {
                val balanceAfter = mainFund.currentBalance - totalAmount
                dao.adjustRegisterBalance(mainFund.id, -totalAmount, 0.0, totalAmount)
                dao.insertFundMovement(
                    com.example.data.model.FundMovement(
                        registerId = mainFund.id,
                        registerName = mainFund.name,
                        isEmployeeFund = false,
                        direction = com.example.data.model.MovementDirection.OUTFLOW,
                        amount = totalAmount,
                        balanceAfter = balanceAfter,
                        title = "مرتجع مبيعات نقدي ($distName) - $quantity كرت ${category.name}",
                        referenceType = "SALE_RETURN",
                        referenceNumber = "",
                        date = System.currentTimeMillis(),
                        notes = "صرف استرداد نقدي لمرتجع مبيعات"
                    )
                )
            }
        } else {
            // خصم من مديونية الموزع / رصيد حسابه
            if (distributorId != null) {
                dao.adjustDistributorBalance(distributorId, -totalAmount)
            }
        }

        val invNum = ((System.currentTimeMillis() % 900000 + 100000)).toString()
        val transaction = TransactionRecord(
            invoiceNumber = invNum,
            type = TransactionType.SALE_RETURN,
            distributorId = distributorId,
            distributorName = distName,
            categoryId = categoryId,
            categoryName = category.name,
            quantity = quantity,
            unitPrice = unitPrice,
            totalAmount = totalAmount,
            discount = 0.0,
            paidAmount = if (refundFromCash) totalAmount else 0.0,
            remainingAmount = if (refundFromCash) 0.0 else totalAmount,
            paymentMethod = if (refundFromCash) "CASH" else "CREDIT",
            date = System.currentTimeMillis(),
            employeeName = employeeName,
            notes = notes
        )

        val id = dao.insertTransaction(transaction)
        val finalTx = transaction.copy(id = id)
        syncToCloud { sync, uid ->
            sync.syncTransaction(uid, finalTx)
            dao.getCategoryById(categoryId)?.let { sync.syncCategory(uid, it) }
            if (distributorId != null) {
                dao.getDistributorById(distributorId)?.let { sync.syncDistributor(uid, it) }
            }
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(finalTx)
    }

    /**
     * عملية مرتجع كروت عام من موزع إلى المخزن (متوافق مع الاستخدامات السابقة)
     */
    suspend fun recordReturn(
        distributorId: Long?,
        categoryId: Long,
        quantity: Int,
        unitPrice: Double,
        employeeName: String,
        notes: String
    ): Result<TransactionRecord> {
        return recordSaleReturn(
            distributorId = distributorId,
            categoryId = categoryId,
            quantity = quantity,
            unitPrice = unitPrice,
            refundFromCash = false,
            employeeName = employeeName,
            notes = notes
        )
    }

    /**
     * عملية مرتجع مشتريات (إرجاع كروت للمورد الرئيسي / الشركة)
     * خصم الكروت المرتجعة من المخزن واسترجاع وإيداع المبلغ في الصندوق (Inflow)
     */
    suspend fun recordPurchaseReturn(
        categoryId: Long,
        quantity: Int,
        costPrice: Double,
        supplierName: String,
        refundToFund: Boolean, // هل تم استرداد المبلغ نقداً للصندوق
        employeeName: String,
        notes: String
    ): Result<TransactionRecord> {
        val category = dao.getCategoryById(categoryId)
            ?: return Result.failure(Exception("الفئة غير موجودة"))

        if (category.stockQuantity < quantity) {
            return Result.failure(Exception("الكمية المتوفرة بالمخزن (${category.stockQuantity}) أقل من كمية المرتجع المطلوبة ($quantity)"))
        }

        val totalAmount = quantity * costPrice
        val invNum = ((System.currentTimeMillis() % 900000 + 100000)).toString()

        // 1. خصم الكروت المرتجعة من المخزن
        dao.adjustCategoryStock(categoryId, -quantity)

        // 2. إيداع المبلغ المسترد في الصندوق الرئيسي
        if (refundToFund && totalAmount > 0) {
            val mainFund = dao.getMainTreasuryDirect()
            if (mainFund != null) {
                val balanceAfter = mainFund.currentBalance + totalAmount
                dao.adjustRegisterBalance(mainFund.id, totalAmount, totalAmount, 0.0)
                dao.insertFundMovement(
                    com.example.data.model.FundMovement(
                        registerId = mainFund.id,
                        registerName = mainFund.name,
                        isEmployeeFund = false,
                        direction = com.example.data.model.MovementDirection.INFLOW,
                        amount = totalAmount,
                        balanceAfter = balanceAfter,
                        title = "استرداد مرتجع مشتريات للمورد ($supplierName) - $quantity كرت ${category.name}",
                        referenceType = "PURCHASE_RETURN",
                        referenceNumber = invNum,
                        date = System.currentTimeMillis(),
                        notes = "إيداع قيمة المرتجع في الصندوق"
                    )
                )
            }
        }

        val transaction = TransactionRecord(
            invoiceNumber = invNum,
            type = TransactionType.PURCHASE_RETURN,
            distributorId = null,
            distributorName = supplierName.ifBlank { "المورد الرئيسي" },
            categoryId = categoryId,
            categoryName = category.name,
            quantity = quantity,
            unitPrice = costPrice,
            totalAmount = totalAmount,
            discount = 0.0,
            paidAmount = if (refundToFund) totalAmount else 0.0,
            remainingAmount = 0.0,
            paymentMethod = if (refundToFund) "CASH" else "OTHER",
            date = System.currentTimeMillis(),
            employeeName = employeeName,
            notes = notes
        )

        val id = dao.insertTransaction(transaction)
        val finalTx = transaction.copy(id = id)
        syncToCloud { sync, uid ->
            sync.syncTransaction(uid, finalTx)
            dao.getCategoryById(categoryId)?.let { sync.syncCategory(uid, it) }
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(finalTx)
    }

    /**
     * سند صرف مالي / مصروفات مع الربط التلقائي بالصندوق
     */
    suspend fun recordPaymentOut(
        title: String,
        amount: Double,
        employeeName: String,
        notes: String
    ): Result<TransactionRecord> {
        val now = System.currentTimeMillis()
        val invNum = ((now % 900000 + 100000)).toString()
        val transaction = TransactionRecord(
            invoiceNumber = invNum,
            type = TransactionType.PAYMENT_OUT,
            distributorName = title,
            totalAmount = amount,
            paidAmount = amount,
            paymentMethod = "CASH",
            date = now,
            employeeName = employeeName,
            notes = notes
        )

        // الربط بالصندوق: تسجيل حركة صرف رسمي من الخزينة الرئيسية
        val mainFund = dao.getMainTreasuryDirect()
        if (mainFund != null && amount > 0) {
            val balanceAfter = mainFund.currentBalance - amount
            dao.adjustRegisterBalance(mainFund.id, -amount, 0.0, amount)
            dao.insertFundMovement(
                com.example.data.model.FundMovement(
                    registerId = mainFund.id,
                    registerName = mainFund.name,
                    isEmployeeFund = false,
                    direction = com.example.data.model.MovementDirection.OUTFLOW,
                    amount = amount,
                    balanceAfter = balanceAfter,
                    title = "سند صرف مصروفات: $title",
                    referenceType = "PAYMENT_OUT",
                    referenceNumber = invNum,
                    date = now,
                    notes = notes
                )
            )
        }

        val id = dao.insertTransaction(transaction)
        val finalTx = transaction.copy(id = id)
        syncToCloud { sync, uid ->
            sync.syncTransaction(uid, finalTx)
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(finalTx)
    }

    /**
     * تسجيل سند قبض نقدي من موزع
     */
    suspend fun recordPaymentIn(
        distributorId: Long,
        amount: Double,
        paymentMethod: String,
        employeeName: String,
        notes: String
    ): Result<TransactionRecord> {
        val dist = dao.getDistributorById(distributorId)
            ?: return Result.failure(Exception("الموزع غير موجود"))

        dao.adjustDistributorBalance(distributorId, -amount)
        dao.recordDistributorPayment(distributorId, amount, System.currentTimeMillis())

        val now = System.currentTimeMillis()
        val invNum = ((now % 900000 + 100000)).toString()
        val transaction = TransactionRecord(
            invoiceNumber = invNum,
            type = TransactionType.PAYMENT_IN,
            distributorId = distributorId,
            distributorName = dist.name,
            totalAmount = amount,
            paidAmount = amount,
            paymentMethod = paymentMethod,
            date = now,
            employeeName = employeeName,
            notes = notes
        )

        val id = dao.insertTransaction(transaction)
        val finalTx = transaction.copy(id = id)
        syncToCloud { sync, uid ->
            sync.syncTransaction(uid, finalTx)
            dao.getDistributorById(distributorId)?.let { sync.syncDistributor(uid, it) }
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(finalTx)
    }

    /**
     * تسجيل خصم لحساب الموزع
     */
    suspend fun recordDiscount(
        distributorId: Long,
        amount: Double,
        employeeName: String,
        notes: String
    ): Result<TransactionRecord> {
        val dist = dao.getDistributorById(distributorId)
            ?: return Result.failure(Exception("الموزع غير موجود"))

        dao.adjustDistributorBalance(distributorId, -amount)

        val now = System.currentTimeMillis()
        val invNum = ((now % 900000 + 100000)).toString()
        val transaction = TransactionRecord(
            invoiceNumber = invNum,
            type = TransactionType.DISCOUNT,
            distributorId = distributorId,
            distributorName = dist.name,
            totalAmount = amount,
            discount = amount,
            date = now,
            employeeName = employeeName,
            notes = notes
        )

        val id = dao.insertTransaction(transaction)
        val finalTx = transaction.copy(id = id)
        syncToCloud { sync, uid ->
            sync.syncTransaction(uid, finalTx)
            dao.getDistributorById(distributorId)?.let { sync.syncDistributor(uid, it) }
        }
        return Result.success(finalTx)
    }

    /**
     * استبدال فئات كروت بفئات أخرى
     */
    suspend fun recordCardSwap(
        distributorId: Long,
        fromCategoryId: Long,
        fromQuantity: Int,
        toCategoryId: Long,
        toQuantity: Int,
        paidDifference: Double,
        employeeName: String,
        notes: String
    ): Result<CardSwapRecord> {
        val dist = dao.getDistributorById(distributorId)
            ?: return Result.failure(Exception("الموزع غير موجود"))
        val fromCat = dao.getCategoryById(fromCategoryId)
            ?: return Result.failure(Exception("الفئة المستبدلة غير موجودة"))
        val toCat = dao.getCategoryById(toCategoryId)
            ?: return Result.failure(Exception("الفئة الجديدة غير موجودة"))

        if (toCat.stockQuantity < toQuantity) {
            return Result.failure(Exception("الرصيد المتوفر من ${toCat.name} (${toCat.stockQuantity}) غير كافٍ للاستبدال المطلوب ($toQuantity)"))
        }

        val fromTotal = fromQuantity * fromCat.sellingPrice
        val toTotal = toQuantity * toCat.sellingPrice
        val diffTotal = toTotal - fromTotal
        val remainingDiff = diffTotal - paidDifference

        // Adjust stock: add returned cards, remove new cards
        dao.adjustCategoryStock(fromCategoryId, fromQuantity)
        dao.adjustCategoryStock(toCategoryId, -toQuantity)

        // Adjust distributor balance with remaining difference
        if (remainingDiff != 0.0) {
            dao.adjustDistributorBalance(distributorId, remainingDiff)
        }

        val swapCode = "SWAP-${System.currentTimeMillis() % 100000}"
        val record = CardSwapRecord(
            swapCode = swapCode,
            distributorId = distributorId,
            distributorName = dist.name,
            fromCategoryId = fromCategoryId,
            fromCategoryName = fromCat.name,
            fromQuantity = fromQuantity,
            fromUnitPrice = fromCat.sellingPrice,
            fromTotalValue = fromTotal,
            toCategoryId = toCategoryId,
            toCategoryName = toCat.name,
            toQuantity = toQuantity,
            toUnitPrice = toCat.sellingPrice,
            toTotalValue = toTotal,
            differenceAmount = diffTotal,
            paidDifference = paidDifference,
            remainingDifference = remainingDiff,
            date = System.currentTimeMillis(),
            employeeName = employeeName,
            notes = notes
        )

        val fromBefore = fromCat.stockQuantity
        val fromAfter = fromBefore + fromQuantity
        val toBefore = toCat.stockQuantity
        val toAfter = toBefore - toQuantity

        val id = dao.insertCardSwap(record)
        val finalSwap = record.copy(id = id)

        // System notification for card swap showing quantities before and after
        val swapNotifMsg = buildString {
            appendLine("🔄 تفاصيل عملية استبدال الكروت:")
            appendLine("• الموزع / العميل: ${dist.name}")
            appendLine("• الفئة المرجعة للمخزن: ${fromCat.name}")
            appendLine("   الكمية المرجعة: $fromQuantity كرت (الرصيد بالمخزن قبل: $fromBefore كرت ⬅ بعد: $fromAfter كرت)")
            appendLine("• الفئة البديلة المصروفة: ${toCat.name}")
            appendLine("   الكمية المصروفة: $toQuantity كرت (الرصيد بالمخزن قبل: $toBefore كرت ⬅ بعد: $toAfter كرت)")
            appendLine("• الفارق المالي: ${formatCurrency(diffTotal)} (المسدد: ${formatCurrency(paidDifference)})")
            if (remainingDiff != 0.0) {
                appendLine("• المتبقي على الحساب: ${formatCurrency(remainingDiff)}")
            }
            if (notes.isNotBlank()) appendLine("• ملاحظات: $notes")
            appendLine("• الموظف المنفذ: $employeeName")
            appendLine("• رمز العملية: $swapCode")
        }
        insertNotification(
            SystemNotification(
                title = "🔄 استبدال كروت: ${dist.name}",
                message = swapNotifMsg,
                type = NotificationType.SWAP,
                targetEmployee = "ALL",
                senderName = employeeName,
                actionUrlOrId = swapCode
            )
        )

        syncToCloud { sync, uid ->
            sync.syncCardSwap(uid, finalSwap)
            dao.getCategoryById(fromCategoryId)?.let { sync.syncCategory(uid, it) }
            dao.getCategoryById(toCategoryId)?.let { sync.syncCategory(uid, it) }
            dao.getDistributorById(distributorId)?.let { sync.syncDistributor(uid, it) }
        }
        return Result.success(finalSwap)
    }

    /**
     * تحويل عهدة بين الموظفين
     */
    suspend fun recordEmployeeTransfer(
        fromEmployee: String,
        toEmployee: String,
        transferType: String,
        categoryId: Long?,
        quantity: Int,
        cashAmount: Double,
        employeeName: String,
        notes: String
    ): Result<EmployeeTransferRecord> {
        var catName = ""
        var faceVal = 0.0
        var senderQtyBefore = 0
        var senderQtyAfter = 0
        var receiverQtyBefore = 0
        var receiverQtyAfter = 0

        if (transferType == "CARDS" && categoryId != null) {
            val cat = dao.getCategoryById(categoryId)
            catName = cat?.name ?: ""
            faceVal = (cat?.faceValue ?: 0.0) * quantity

            if (quantity > 0) {
                // Adjust sender stock
                if (fromEmployee != "المدير العام") {
                    val sStock = dao.getEmployeeStock(fromEmployee, categoryId)
                    senderQtyBefore = sStock?.quantity ?: 0
                    dao.adjustEmployeeStockQuantity(fromEmployee, categoryId, -quantity)
                } else {
                    senderQtyBefore = cat?.stockQuantity ?: 0
                    dao.adjustCategoryStock(categoryId, -quantity)
                }
                senderQtyAfter = senderQtyBefore - quantity

                // Adjust receiver stock
                if (toEmployee != "المدير العام") {
                    val rStock = dao.getEmployeeStock(toEmployee, categoryId)
                    if (rStock == null) {
                        receiverQtyBefore = 0
                        dao.insertEmployeeStock(
                            com.example.data.model.EmployeeStock(
                                employeeName = toEmployee,
                                categoryId = categoryId,
                                categoryName = catName,
                                quantity = quantity
                            )
                        )
                    } else {
                        receiverQtyBefore = rStock.quantity
                        dao.adjustEmployeeStockQuantity(toEmployee, categoryId, quantity)
                    }
                } else {
                    receiverQtyBefore = dao.getCategoryById(categoryId)?.stockQuantity ?: 0
                    dao.adjustCategoryStock(categoryId, quantity)
                }
                receiverQtyAfter = receiverQtyBefore + quantity
            }
        }

        val transferCode = "TRF-${System.currentTimeMillis() % 100000}"
        val record = EmployeeTransferRecord(
            transferCode = transferCode,
            fromEmployee = fromEmployee,
            toEmployee = toEmployee,
            transferType = transferType,
            categoryId = categoryId,
            categoryName = catName,
            quantity = quantity,
            totalFaceValue = faceVal,
            cashAmount = cashAmount,
            date = System.currentTimeMillis(),
            status = "COMPLETED",
            notes = notes
        )

        val id = dao.insertEmployeeTransfer(record)
        val finalTransfer = record.copy(id = id)

        // Insert notification for Sender
        val senderNotifMsg = if (transferType == "CARDS") {
            buildString {
                appendLine("📦 إشعار تحويل عهدة مخزن (صادر):")
                appendLine("• الفئة المحولة: $catName")
                appendLine("• الكمية المحولة: $quantity كرت")
                appendLine("• المستلم: $toEmployee")
                appendLine("• رصيد مخزنك قبل التحويل: $senderQtyBefore كرت ⬅ بعد التحويل: $senderQtyAfter كرت")
                if (notes.isNotBlank()) appendLine("• ملاحظات: $notes")
                appendLine("• رمز التحويل: $transferCode")
            }
        } else {
            buildString {
                appendLine("💸 إشعار تحويل عهدة نقدية (صادر):")
                appendLine("• المبلغ المحول: ${formatCurrency(cashAmount)}")
                appendLine("• المستلم: $toEmployee")
                if (notes.isNotBlank()) appendLine("• ملاحظات: $notes")
                appendLine("• رمز التحويل: $transferCode")
            }
        }
        insertNotification(
            SystemNotification(
                title = if (transferType == "CARDS") "📦 تحويل عهدة صادر: $catName ($toEmployee)" else "💸 تحويل نقدية صادر: $toEmployee",
                message = senderNotifMsg,
                type = NotificationType.TRANSFER,
                targetEmployee = fromEmployee,
                senderName = fromEmployee,
                actionUrlOrId = transferCode
            )
        )

        // Insert notification for Receiver
        val receiverNotifMsg = if (transferType == "CARDS") {
            buildString {
                appendLine("📥 إشعار استلام عهدة مخزن (وارد):")
                appendLine("• الفئة المستلمة: $catName")
                appendLine("• الكمية المستلمة: $quantity كرت")
                appendLine("• المرسل: $fromEmployee")
                appendLine("• رصيد مخزنك قبل الاستلام: $receiverQtyBefore كرت ⬅ بعد الاستلام: $receiverQtyAfter كرت")
                if (notes.isNotBlank()) appendLine("• ملاحظات: $notes")
                appendLine("• رمز التحويل: $transferCode")
            }
        } else {
            buildString {
                appendLine("💵 إشعار استلام عهدة نقدية (وارد):")
                appendLine("• المبلغ المستلم: ${formatCurrency(cashAmount)}")
                appendLine("• المرسل: $fromEmployee")
                if (notes.isNotBlank()) appendLine("• ملاحظات: $notes")
                appendLine("• رمز التحويل: $transferCode")
            }
        }
        insertNotification(
            SystemNotification(
                title = if (transferType == "CARDS") "📥 استلام عهدة وارد: $catName ($fromEmployee)" else "💵 استلام نقدية وارد: من $fromEmployee",
                message = receiverNotifMsg,
                type = NotificationType.TRANSFER,
                targetEmployee = toEmployee,
                senderName = fromEmployee,
                actionUrlOrId = transferCode
            )
        )

        syncToCloud { sync, uid ->
            sync.syncEmployeeTransfer(uid, finalTransfer)
            if (categoryId != null) {
                dao.getCategoryById(categoryId)?.let { sync.syncCategory(uid, it) }
            }
        }
        return Result.success(finalTransfer)
    }

    // === User & Roles Operations ===
    suspend fun insertUser(user: AppUser): Long {
        val id = dao.insertUser(user)
        syncToCloud { sync, uid -> sync.syncAppUser(uid, user.copy(id = id)) }
        return id
    }

    suspend fun updateUser(user: AppUser) {
        dao.updateUser(user)
        syncToCloud { sync, uid -> sync.syncAppUser(uid, user) }
    }

    suspend fun deleteUser(user: AppUser) {
        dao.deleteUser(user)
        syncToCloud { sync, uid -> sync.deleteAppUser(uid, user.id, user.email) }
    }

    // === Notifications Operations ===
    suspend fun insertNotification(notif: SystemNotification): Long {
        val id = dao.insertNotification(notif)
        val finalNotif = notif.copy(id = id)
        syncToCloud { sync, uid -> sync.syncNotification(uid, finalNotif) }
        return id
    }

    suspend fun sendBroadcastNotification(
        title: String,
        message: String,
        senderName: String,
        targetEmployee: String = "ALL"
    ): Long {
        val notif = SystemNotification(
            title = title,
            message = message,
            type = NotificationType.MANAGER_MESSAGE,
            targetEmployee = targetEmployee,
            senderName = senderName,
            date = System.currentTimeMillis()
        )
        return insertNotification(notif)
    }

    suspend fun markNotificationAsRead(id: Long) = dao.markNotificationAsRead(id)
    suspend fun markAllNotificationsAsRead() = dao.markAllNotificationsAsRead()
    
    suspend fun deleteNotification(id: Long) {
        dao.deleteNotification(id)
        syncToCloud { sync, uid -> sync.deleteNotification(uid, id) }
    }
    suspend fun clearAllNotifications() = dao.clearAllNotifications()
    suspend fun deleteTransaction(tx: TransactionRecord) {
        dao.deleteTransaction(tx)
        syncToCloud { sync, uid -> sync.deleteTransaction(uid, tx.id) }
    }

    suspend fun updateTransaction(tx: TransactionRecord) {
        dao.updateTransaction(tx)
        syncToCloud { sync, uid -> sync.syncTransaction(uid, tx) }
    }

    suspend fun deleteCardSwap(swap: CardSwapRecord) {
        dao.deleteCardSwap(swap)
        syncToCloud { sync, uid -> sync.deleteCardSwap(uid, swap.id) }
    }

    suspend fun deleteEmployeeTransfer(transfer: EmployeeTransferRecord) {
        dao.deleteEmployeeTransfer(transfer)
        syncToCloud { sync, uid -> sync.deleteEmployeeTransfer(uid, transfer.id) }
    }

    // === Isolated Cash Register Operations ===

    /**
     * إيداع في صندوق الموظف المستقل (لا يتدخل في صندوق البرنامج الرئيسي نهائياً)
     */
    suspend fun recordEmployeeFundInflow(
        employeeUsername: String,
        employeeDisplayName: String,
        amount: Double,
        title: String,
        referenceType: String = "MANUAL",
        referenceNumber: String = "",
        notes: String = ""
    ): Result<com.example.data.model.FundMovement> {
        var register = dao.getEmployeeFundByUsernameDirect(employeeUsername)
        if (register == null) {
            val newReg = com.example.data.model.CashRegister(
                name = "صندوق الموظف ($employeeDisplayName)",
                type = com.example.data.model.FundType.EMPLOYEE_FUND,
                employeeUsername = employeeUsername,
                employeeDisplayName = employeeDisplayName,
                currentBalance = 0.0,
                openingBalance = 0.0,
                notes = "صندوق شخصي مستقل للموظف"
            )
            val regId = dao.insertCashRegister(newReg)
            register = newReg.copy(id = regId)
        }

        val balanceAfter = register.currentBalance + amount
        dao.adjustRegisterBalance(register.id, amount, amount, 0.0)

        val movement = com.example.data.model.FundMovement(
            registerId = register.id,
            registerName = register.name,
            isEmployeeFund = true,
            employeeUsername = employeeUsername,
            employeeDisplayName = employeeDisplayName,
            direction = com.example.data.model.MovementDirection.INFLOW,
            amount = amount,
            balanceAfter = balanceAfter,
            title = title,
            referenceType = referenceType,
            referenceNumber = referenceNumber,
            date = System.currentTimeMillis(),
            notes = notes
        )
        val movId = dao.insertFundMovement(movement)
        val finalMov = movement.copy(id = movId)
        syncToCloud { sync, uid ->
            sync.syncFundMovement(uid, finalMov)
            dao.getEmployeeFundByUsernameDirect(employeeUsername)?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(finalMov)
    }

    /**
     * صرف من صندوق الموظف المستقل (لا يتدخل في صندوق البرنامج الرئيسي)
     */
    suspend fun recordEmployeeFundOutflow(
        employeeUsername: String,
        employeeDisplayName: String,
        amount: Double,
        title: String,
        referenceType: String = "MANUAL",
        referenceNumber: String = "",
        notes: String = ""
    ): Result<com.example.data.model.FundMovement> {
        val register = dao.getEmployeeFundByUsernameDirect(employeeUsername)
            ?: return Result.failure(Exception("لم يتم العثور على صندوق خاص بهذا الموظف"))

        val balanceAfter = register.currentBalance - amount
        dao.adjustRegisterBalance(register.id, -amount, 0.0, amount)

        val movement = com.example.data.model.FundMovement(
            registerId = register.id,
            registerName = register.name,
            isEmployeeFund = true,
            employeeUsername = employeeUsername,
            employeeDisplayName = employeeDisplayName,
            direction = com.example.data.model.MovementDirection.OUTFLOW,
            amount = amount,
            balanceAfter = balanceAfter,
            title = title,
            referenceType = referenceType,
            referenceNumber = referenceNumber,
            date = System.currentTimeMillis(),
            notes = notes
        )
        val movId = dao.insertFundMovement(movement)
        val finalMov = movement.copy(id = movId)
        syncToCloud { sync, uid ->
            sync.syncFundMovement(uid, finalMov)
            dao.getEmployeeFundByUsernameDirect(employeeUsername)?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(finalMov)
    }

    /**
     * إيداع في صندوق البرنامج الرئيسي (الرسمي)
     */
    suspend fun recordMainTreasuryInflow(
        amount: Double,
        title: String,
        referenceType: String = "MANUAL",
        referenceNumber: String = "",
        notes: String = ""
    ): Result<com.example.data.model.FundMovement> {
        val treasury = dao.getMainTreasuryDirect()
            ?: return Result.failure(Exception("صندوق البرنامج الرئيسي غير موجود"))

        val balanceAfter = treasury.currentBalance + amount
        dao.adjustRegisterBalance(treasury.id, amount, amount, 0.0)

        val movement = com.example.data.model.FundMovement(
            registerId = treasury.id,
            registerName = treasury.name,
            isEmployeeFund = false,
            direction = com.example.data.model.MovementDirection.INFLOW,
            amount = amount,
            balanceAfter = balanceAfter,
            title = title,
            referenceType = referenceType,
            referenceNumber = referenceNumber,
            date = System.currentTimeMillis(),
            notes = notes
        )
        val movId = dao.insertFundMovement(movement)
        val finalMov = movement.copy(id = movId)
        syncToCloud { sync, uid ->
            sync.syncFundMovement(uid, finalMov)
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(finalMov)
    }

    /**
     * صرف رسمي من صندوق البرنامج الرئيسي
     */
    suspend fun recordMainTreasuryOutflow(
        amount: Double,
        title: String,
        referenceType: String = "MANUAL",
        referenceNumber: String = "",
        notes: String = ""
    ): Result<com.example.data.model.FundMovement> {
        val treasury = dao.getMainTreasuryDirect()
            ?: return Result.failure(Exception("صندوق البرنامج الرئيسي غير موجود"))

        val balanceAfter = treasury.currentBalance - amount
        dao.adjustRegisterBalance(treasury.id, -amount, 0.0, amount)

        val movement = com.example.data.model.FundMovement(
            registerId = treasury.id,
            registerName = treasury.name,
            isEmployeeFund = false,
            direction = com.example.data.model.MovementDirection.OUTFLOW,
            amount = amount,
            balanceAfter = balanceAfter,
            title = title,
            referenceType = referenceType,
            referenceNumber = referenceNumber,
            date = System.currentTimeMillis(),
            notes = notes
        )
        val movId = dao.insertFundMovement(movement)
        val finalMov = movement.copy(id = movId)
        syncToCloud { sync, uid ->
            sync.syncFundMovement(uid, finalMov)
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }
        return Result.success(finalMov)
    }

    /**
     * توريد نقدية من صندوق الموظف إلى صندوق البرنامج الرئيسي (تسليم عهدة موثق)
     */
    suspend fun transferEmployeeToMainTreasury(
        employeeUsername: String,
        employeeDisplayName: String,
        amount: Double,
        notes: String
    ): Result<Boolean> {
        val empFund = dao.getEmployeeFundByUsernameDirect(employeeUsername)
            ?: return Result.failure(Exception("صندوق الموظف غير موجود"))
        val mainFund = dao.getMainTreasuryDirect()
            ?: return Result.failure(Exception("صندوق البرنامج الرئيسي غير موجود"))

        // 1. Outflow from employee
        val empBalanceAfter = empFund.currentBalance - amount
        dao.adjustRegisterBalance(empFund.id, -amount, 0.0, amount)
        dao.insertFundMovement(
            com.example.data.model.FundMovement(
                registerId = empFund.id,
                registerName = empFund.name,
                isEmployeeFund = true,
                employeeUsername = employeeUsername,
                employeeDisplayName = employeeDisplayName,
                direction = com.example.data.model.MovementDirection.OUTFLOW,
                amount = amount,
                balanceAfter = empBalanceAfter,
                title = "توريد وتسليم نقدية لصندوق البرنامج الرئيسي",
                referenceType = "TRANSFER_TO_MAIN",
                date = System.currentTimeMillis(),
                notes = notes
            )
        )

        // 2. Inflow into main fund
        val mainBalanceAfter = mainFund.currentBalance + amount
        dao.adjustRegisterBalance(mainFund.id, amount, amount, 0.0)
        dao.insertFundMovement(
            com.example.data.model.FundMovement(
                registerId = mainFund.id,
                registerName = mainFund.name,
                isEmployeeFund = false,
                direction = com.example.data.model.MovementDirection.INFLOW,
                amount = amount,
                balanceAfter = mainBalanceAfter,
                title = "استلام توريد نقدية من صندوق الموظف: $employeeDisplayName",
                referenceType = "TRANSFER_TO_MAIN",
                date = System.currentTimeMillis(),
                notes = notes
            )
        )

        syncToCloud { sync, uid ->
            dao.getEmployeeFundByUsernameDirect(employeeUsername)?.let { sync.syncRegister(uid, it) }
            dao.getMainTreasuryDirect()?.let { sync.syncRegister(uid, it) }
        }

        // إشعار للموظف بسند توريد وتوصيل المبلغ النقدى لصندوق المدير
        insertNotification(
            SystemNotification(
                title = "سند توريد عهدة نقدية للمدير",
                message = "تم اعتماد وتوريد مبلغ (${formatCurrency(amount)}) من صندوقك إلى صندوق المدير العام بنجاح. $notes",
                type = NotificationType.GENERAL,
                targetEmployee = employeeUsername,
                senderName = "المدير العام",
                date = System.currentTimeMillis()
            )
        )

        return Result.success(true)
    }

    suspend fun transferStockBetweenEmployees(
        fromEmployee: String,
        toEmployee: String,
        categoryId: Long,
        categoryName: String,
        quantity: Int,
        totalFaceValue: Double,
        notes: String
    ): Result<EmployeeTransferRecord> {
        if (quantity <= 0) return Result.failure(Exception("الكمية يجب أن تكون أكبر من صفر"))

        val senderQtyBefore: Int
        if (fromEmployee != "المدير العام") {
            val senderStock = dao.getEmployeeStock(fromEmployee, categoryId)
            val available = senderStock?.quantity ?: 0
            if (available < quantity) {
                return Result.failure(Exception("الكمية المتوفرة في مخزن الموظف ($fromEmployee) هي $available فقط، ولا تكفي للتحويل المطلوب ($quantity)"))
            }
            senderQtyBefore = available
            dao.adjustEmployeeStockQuantity(fromEmployee, categoryId, -quantity)
        } else {
            val cat = dao.getCategoryById(categoryId)
                ?: return Result.failure(Exception("فئة الكرت غير موجودة"))
            if (cat.stockQuantity < quantity) {
                return Result.failure(Exception("الكمية المتوفرة في المخزن الرئيسي (${cat.stockQuantity}) غير كافية"))
            }
            senderQtyBefore = cat.stockQuantity
            dao.adjustCategoryStock(categoryId, -quantity)
        }
        val senderQtyAfter = senderQtyBefore - quantity

        val receiverQtyBefore: Int
        if (toEmployee != "المدير العام") {
            val destStock = dao.getEmployeeStock(toEmployee, categoryId)
            if (destStock == null) {
                receiverQtyBefore = 0
                dao.insertEmployeeStock(
                    com.example.data.model.EmployeeStock(
                        employeeName = toEmployee,
                        categoryId = categoryId,
                        categoryName = categoryName,
                        quantity = quantity
                    )
                )
            } else {
                receiverQtyBefore = destStock.quantity
                dao.adjustEmployeeStockQuantity(toEmployee, categoryId, quantity)
            }
        } else {
            val cat = dao.getCategoryById(categoryId)
            receiverQtyBefore = cat?.stockQuantity ?: 0
            dao.adjustCategoryStock(categoryId, quantity)
        }
        val receiverQtyAfter = receiverQtyBefore + quantity

        val transferCode = "TRF-${System.currentTimeMillis().toString().takeLast(6)}"
        val record = EmployeeTransferRecord(
            transferCode = transferCode,
            fromEmployee = fromEmployee,
            toEmployee = toEmployee,
            transferType = "CARDS",
            categoryId = categoryId,
            categoryName = categoryName,
            quantity = quantity,
            totalFaceValue = totalFaceValue,
            notes = notes
        )
        dao.insertEmployeeTransfer(record)

        // Notification for Sender showing quantities before and after
        val senderNotifMsg = buildString {
            appendLine("📦 إشعار تحويل عهدة مخزن (صادر):")
            appendLine("• الفئة المحولة: $categoryName")
            appendLine("• الكمية المحولة: $quantity كرت")
            appendLine("• المستلم: $toEmployee")
            appendLine("• رصيد مخزنك قبل التحويل: $senderQtyBefore كرت ⬅ بعد التحويل: $senderQtyAfter كرت")
            if (notes.isNotBlank()) appendLine("• ملاحظات: $notes")
            appendLine("• رمز التحويل: $transferCode")
        }
        insertNotification(
            SystemNotification(
                title = "📦 تحويل عهدة صادر: $categoryName ($toEmployee)",
                message = senderNotifMsg,
                type = NotificationType.TRANSFER,
                targetEmployee = fromEmployee,
                senderName = fromEmployee,
                actionUrlOrId = transferCode
            )
        )

        // Notification for Receiver showing quantities before and after
        val receiverNotifMsg = buildString {
            appendLine("📥 إشعار استلام عهدة مخزن (وارد):")
            appendLine("• الفئة المستلمة: $categoryName")
            appendLine("• الكمية المستلمة: $quantity كرت")
            appendLine("• المرسل: $fromEmployee")
            appendLine("• رصيد مخزنك قبل الاستلام: $receiverQtyBefore كرت ⬅ بعد الاستلام: $receiverQtyAfter كرت")
            if (notes.isNotBlank()) appendLine("• ملاحظات: $notes")
            appendLine("• رمز التحويل: $transferCode")
        }
        insertNotification(
            SystemNotification(
                title = "📥 استلام عهدة وارد: $categoryName ($fromEmployee)",
                message = receiverNotifMsg,
                type = NotificationType.TRANSFER,
                targetEmployee = toEmployee,
                senderName = fromEmployee,
                actionUrlOrId = transferCode
            )
        )

        return Result.success(record)
    }

    suspend fun transferMultipleStockBetweenEmployees(
        fromEmployee: String,
        toEmployee: String,
        items: List<InvoiceItemDraft>,
        notes: String
    ): Result<EmployeeTransferRecord> {
        if (items.isEmpty()) return Result.failure(Exception("يرجى إضافة صنف واحد على الأقل للتحويل"))
        if (fromEmployee == toEmployee) return Result.failure(Exception("لا يمكن التحويل لنفس المخزن"))

        for (item in items) {
            if (item.quantity <= 0) continue
            if (fromEmployee != "المدير العام") {
                val senderStock = dao.getEmployeeStock(fromEmployee, item.categoryId)
                val available = senderStock?.quantity ?: 0
                if (available < item.quantity) {
                    return Result.failure(Exception("الكمية المتوفرة في مخزن الموظف ($fromEmployee) للصنف (${item.categoryName}) هي $available فقط، بينما المطلوب ${item.quantity}"))
                }
            } else {
                val cat = dao.getCategoryById(item.categoryId)
                    ?: return Result.failure(Exception("فئة الكرت (${item.categoryName}) غير موجودة"))
                if (cat.stockQuantity < item.quantity) {
                    return Result.failure(Exception("الكمية المتوفرة في المخزن الرئيسي للصنف (${item.categoryName}) هي ${cat.stockQuantity}، بينما المطلوب ${item.quantity}"))
                }
            }
        }

        val totalFaceValue = items.sumOf { it.quantity * it.unitPrice }
        val primaryItem = items.first()
        val transferCode = "TRF-${System.currentTimeMillis().toString().takeLast(6)}"

        val transferItemsStats = mutableListOf<String>()

        for (item in items) {
            val qty = item.quantity
            val catId = item.categoryId
            val catName = item.categoryName

            val senderBefore = if (fromEmployee != "المدير العام") {
                dao.getEmployeeStock(fromEmployee, catId)?.quantity ?: 0
            } else {
                dao.getCategoryById(catId)?.stockQuantity ?: 0
            }
            val senderAfter = senderBefore - qty

            if (fromEmployee != "المدير العام") {
                dao.adjustEmployeeStockQuantity(fromEmployee, catId, -qty)
            } else {
                dao.adjustCategoryStock(catId, -qty)
            }

            val receiverBefore = if (toEmployee != "المدير العام") {
                dao.getEmployeeStock(toEmployee, catId)?.quantity ?: 0
            } else {
                dao.getCategoryById(catId)?.stockQuantity ?: 0
            }
            val receiverAfter = receiverBefore + qty

            if (toEmployee != "المدير العام") {
                val destStock = dao.getEmployeeStock(toEmployee, catId)
                if (destStock == null) {
                    dao.insertEmployeeStock(
                        com.example.data.model.EmployeeStock(
                            employeeName = toEmployee,
                            categoryId = catId,
                            categoryName = catName,
                            quantity = qty
                        )
                    )
                } else {
                    dao.adjustEmployeeStockQuantity(toEmployee, catId, qty)
                }
            } else {
                dao.adjustCategoryStock(catId, qty)
            }

            transferItemsStats.add(
                "▪ $catName ($qty كرت): رصيد المرسل (قبل: $senderBefore ⬅ بعد: $senderAfter) | رصيد المستلم (قبل: $receiverBefore ⬅ بعد: $receiverAfter)"
            )
        }

        val record = EmployeeTransferRecord(
            transferCode = transferCode,
            fromEmployee = fromEmployee,
            toEmployee = toEmployee,
            transferType = "CARDS",
            categoryId = primaryItem.categoryId,
            categoryName = if (items.size == 1) primaryItem.categoryName else "أصناف متعددة (${items.size})",
            quantity = items.sumOf { it.quantity },
            totalFaceValue = totalFaceValue,
            notes = notes
        )
        dao.insertEmployeeTransfer(record)

        // Notification for Sender
        val senderNotifMsg = buildString {
            appendLine("📦 إشعار تحويل عهدة مخزن صادر (${items.size} أصناف):")
            appendLine("• إلى مخزن المستلم: $toEmployee")
            appendLine("• إجمالي الكروت المحولة: ${items.sumOf { it.quantity }} كرت")
            appendLine("───────────────────")
            transferItemsStats.forEach { line ->
                appendLine(line)
            }
            if (notes.isNotBlank()) appendLine("• ملاحظات: $notes")
            appendLine("• رمز التحويل: $transferCode")
        }
        insertNotification(
            SystemNotification(
                title = "📦 تحويل عهدة صادر (${items.size} أصناف) ⬅ $toEmployee",
                message = senderNotifMsg,
                type = NotificationType.TRANSFER,
                targetEmployee = fromEmployee,
                senderName = fromEmployee,
                actionUrlOrId = transferCode
            )
        )

        // Notification for Receiver
        val receiverNotifMsg = buildString {
            appendLine("📥 إشعار استلام عهدة مخزن وارد (${items.size} أصناف):")
            appendLine("• من مخزن المرسل: $fromEmployee")
            appendLine("• إجمالي الكروت المستلمة: ${items.sumOf { it.quantity }} كرت")
            appendLine("───────────────────")
            transferItemsStats.forEach { line ->
                appendLine(line)
            }
            if (notes.isNotBlank()) appendLine("• ملاحظات: $notes")
            appendLine("• رمز التحويل: $transferCode")
        }
        insertNotification(
            SystemNotification(
                title = "📥 استلام عهدة وارد (${items.size} أصناف) ⬅ من $fromEmployee",
                message = receiverNotifMsg,
                type = NotificationType.TRANSFER,
                targetEmployee = toEmployee,
                senderName = fromEmployee,
                actionUrlOrId = transferCode
            )
        )

        return Result.success(record)
    }

    private fun formatCurrency(amount: Double): String {
        return "%,.0f".format(Locale.US, amount)
    }
}
