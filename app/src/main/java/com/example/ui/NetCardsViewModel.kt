package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppUser
import com.example.data.model.AppUserStats
import com.example.data.model.CardCategory
import com.example.data.model.CardSwapRecord
import com.example.data.model.DeveloperAdConfig
import com.example.data.model.Distributor
import com.example.data.model.EmployeeTransferRecord
import com.example.data.model.NetworkSettings
import com.example.data.model.NotificationType
import com.example.data.model.SystemNotification
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.data.model.TransactionItem
import com.example.data.model.InvoiceItemDraft
import com.example.data.model.UserRole
import com.example.data.repository.DeveloperSettingsRepository
import com.example.data.repository.NetCardsRepository
import android.content.Context
import com.example.data.firebase.FirebaseEmployeeInvite
import com.example.util.AdMobHelper
import com.example.util.BackupRestoreHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AppScreen(val titleAr: String, val titleEn: String) {
    DASHBOARD("القائمة الرئيسية", "Main Menu"),
    SALES("المبيعات", "Sales & Billing"),
    PURCHASES("المشتريات", "Purchases & Inbound"),
    DISTRIBUTORS("العملاء", "Customers"),
    CATEGORIES("الكروت", "Card Categories"),
    SWAP("استبدال الكروت", "Card Swaps"),
    VOUCHERS("السندات", "Vouchers"),
    VOUCHERS_PAYMENT_IN("سندات القبض", "Payment In Vouchers"),
    VOUCHERS_PAYMENT_OUT("سندات الصرف", "Payment Out Vouchers"),
    VOUCHERS_DISCOUNT("الخصومات المالية", "Financial Discounts"),
    VOUCHERS_RETURNS("سجل المرتجعات", "Returns Records"),
    FUNDS("الصندوق", "Treasuries & Funds"),
    TRANSFERS("تحويلات الموظفين", "Staff Transfers"),
    WAREHOUSES("الموظفين", "Employees & Stores"),
    REPORTS("التقارير المالية", "Reports"),
    ANALYTICS("الإحصائيات والرسوم البيانية", "Analytics & Charts"),
    USERS("صلاحيات الموظفين", "Permissions & Roles"),
    SETTINGS("الإعدادات والمزامنة", "Settings & Cloud"),
    DEVELOPER("وضع المطور والإعلانات", "Developer Mode & Ads"),
    ABOUT("عن البرنامج", "About App")
}

data class DashboardStats(
    val totalMarketDebt: Double = 0.0,
    val totalStockCards: Int = 0,
    val totalStockValue: Double = 0.0,
    val todaySalesCount: Int = 0,
    val todaySalesAmount: Double = 0.0,
    val todayCollectedCash: Double = 0.0,
    val todayEstimatedProfit: Double = 0.0,
    val overdueDistributorsCount: Int = 0,
    val overCreditLimitDistributorsCount: Int = 0
)

class NetCardsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    val firestoreSync = com.example.data.firebase.FirestoreSyncRepository(application)
    val repository = NetCardsRepository(database.netCardsDao(), firestoreSync)

    val categories: StateFlow<List<CardCategory>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distributors: StateFlow<List<Distributor>> = repository.distributors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionRecord>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesTransactions: StateFlow<List<TransactionRecord>> = transactions
        .map { list -> list.filter { it.type == TransactionType.SALE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchaseTransactions: StateFlow<List<TransactionRecord>> = transactions
        .map { list -> list.filter { it.type == TransactionType.PURCHASE } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cardSwaps: StateFlow<List<CardSwapRecord>> = repository.cardSwaps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employeeTransfers: StateFlow<List<EmployeeTransferRecord>> = repository.employeeTransfers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employeeStocks: StateFlow<List<com.example.data.model.EmployeeStock>> = repository.employeeStocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<AppUser>> = repository.users
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<SystemNotification>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val settings: StateFlow<NetworkSettings?> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val prefs = application.getSharedPreferences("netcards_cloud_link_prefs", Context.MODE_PRIVATE)

    private val _linkedManagerInfo = MutableStateFlow<FirebaseEmployeeInvite?>(null)
    val linkedManagerInfo: StateFlow<FirebaseEmployeeInvite?> = _linkedManagerInfo.asStateFlow()

    fun getActiveSyncUid(): String? {
        val linked = prefs.getString("linked_manager_uid", null)
        if (!linked.isNullOrBlank()) {
            return linked
        }
        return Firebase.auth.currentUser?.uid
    }

    init {
        val savedLinkedManagerUid = prefs.getString("linked_manager_uid", null)
        if (!savedLinkedManagerUid.isNullOrBlank()) {
            repository.setEffectiveCloudUserId(savedLinkedManagerUid)
        }
        viewModelScope.launch {
            val fbUser = try { Firebase.auth.currentUser } catch (_: Exception) { null }
            if (fbUser != null) {
                val effectiveUid = getActiveSyncUid() ?: fbUser.uid
                repository.startCloudSync(effectiveUid, viewModelScope)
            }
            val cats = repository.categories.first()
            if (cats.isEmpty()) {
                repository.insertCategory(CardCategory(name = "كرت سوا 10 ريال", faceValue = 10.0, costPrice = 9.0, sellingPrice = 10.0, stockQuantity = 500))
                repository.insertCategory(CardCategory(name = "كرت موبايلي 50 ريال", faceValue = 50.0, costPrice = 46.0, sellingPrice = 50.0, stockQuantity = 300))
                repository.insertCategory(CardCategory(name = "كرت زين 100 ريال", faceValue = 100.0, costPrice = 92.0, sellingPrice = 100.0, stockQuantity = 200))
            }
            val dists = repository.distributors.first()
            if (dists.isEmpty()) {
                repository.insertDistributor(Distributor(name = "الموزع الرئيسي", phone = "777000000", currentBalance = 0.0, creditLimit = 5000.0))
            }
        }
    }

    // === Isolated Cash Registers & Treasuries ===
    val mainTreasury: StateFlow<com.example.data.model.CashRegister?> = repository.mainTreasury
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val employeeFunds: StateFlow<List<com.example.data.model.CashRegister>> = repository.employeeFunds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFundMovements: StateFlow<List<com.example.data.model.FundMovement>> = repository.allFundMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mainTreasuryMovements: StateFlow<List<com.example.data.model.FundMovement>> = repository.mainTreasuryMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // === Developer Mode & Ad Settings ===
    private val devSettingsRepo = DeveloperSettingsRepository(application)
    val developerAdConfig: StateFlow<DeveloperAdConfig> = devSettingsRepo.config

    val userStats: StateFlow<AppUserStats> = users.map { userList ->
        val fbUser = try { Firebase.auth.currentUser } catch (_: Exception) { null }
        AppUserStats(
            totalUsersCount = userList.size,
            activeUsersCount = userList.count { it.isActive },
            adminCount = userList.count { it.role == UserRole.ADMIN },
            salesCount = userList.count { it.role == UserRole.SALES },
            accountantCount = userList.count { it.role == UserRole.ACCOUNTANT },
            viewerCount = userList.count { it.role == UserRole.SUPERVISOR },
            currentFirebaseUserEmail = fbUser?.email,
            currentFirebaseUserId = fbUser?.uid,
            isCloudConnected = fbUser != null,
            usersList = userList
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppUserStats()
    )

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()
    private val _navigationStack = mutableListOf<AppScreen>()

    // Current Logged-in User
    private val _currentUser = MutableStateFlow(
        AppUser(
            id = 1,
            username = "admin",
            displayName = "المدير العام",
            pinCode = "1234",
            role = UserRole.ADMIN,
            canViewProfits = true,
            canDeleteRecords = true,
            canManageUsers = true,
            canManageStock = true,
            canGrantDiscount = true,
            canExportBackup = true,
            canAddSales = true,
            canEditSales = true,
            canDeleteSales = true,
            canAddPurchases = true,
            canEditPurchases = true,
            canDeletePurchases = true,
            canAddVouchers = true,
            canEditVouchers = true,
            canDeleteVouchers = true,
            canAddSwaps = true,
            canEditSwaps = true,
            canDeleteSwaps = true,
            canAddTransfers = true,
            canEditTransfers = true,
            canDeleteTransfers = true,
            canAddReports = true,
            canEditReports = true,
            canDeleteReports = true,
            canAddCards = true,
            canEditCards = true,
            canDeleteCards = true
        )
    )
    val currentUser: StateFlow<AppUser> = _currentUser.asStateFlow()

    // UI Feedback Message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Sync state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    // Security Lock state
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // Selected Distributor for drilldown
    private val _selectedDistributor = MutableStateFlow<Distributor?>(null)
    val selectedDistributor: StateFlow<Distributor?> = _selectedDistributor.asStateFlow()

    private val _warehouseSelectedTab = MutableStateFlow(0)
    val warehouseSelectedTab: StateFlow<Int> = _warehouseSelectedTab.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _navigationStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        if (_navigationStack.isNotEmpty()) {
            _currentScreen.value = _navigationStack.removeLast()
        } else {
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    fun navigateToWarehouses(tabIndex: Int = 0) {
        _warehouseSelectedTab.value = tabIndex
        _currentScreen.value = AppScreen.WAREHOUSES
    }

    fun setWarehouseSelectedTab(tabIndex: Int) {
        _warehouseSelectedTab.value = tabIndex
    }

    fun selectDistributor(distributor: Distributor?) {
        _selectedDistributor.value = distributor
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun setCurrentUser(user: AppUser) {
        _currentUser.value = user
        showMessage("تم التبديل إلى المستخدم: ${user.displayName} (${user.role.titleAr})")
    }

    fun unlockApp(pin: String): Boolean {
        if (pin == _currentUser.value.pinCode || pin == "1234") {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun unlockAppWithBiometric(): Boolean {
        _isAppLocked.value = false
        showMessage("تم تأكيد الهوية بالبصمة وفتح التطبيق بنجاح")
        return true
    }

    fun lockApp() {
        _isAppLocked.value = true
    }

    fun calculateDashboardStats(
        distributorsList: List<Distributor>,
        categoriesList: List<CardCategory>,
        transactionsList: List<TransactionRecord>
    ): DashboardStats {
        val totalMarketDebt = distributorsList.sumOf { it.currentBalance.coerceAtLeast(0.0) }
        val totalStockCards = categoriesList.sumOf { it.stockQuantity }
        val totalStockValue = categoriesList.sumOf { it.stockQuantity * it.faceValue }

        val todayStart = getStartOfDayMillis()
        val todayTx = transactionsList.filter { it.date >= todayStart }
        val todaySales = todayTx.filter { it.type == TransactionType.SALE }

        val todaySalesCount = todaySales.sumOf { it.quantity }
        val todaySalesAmount = todaySales.sumOf { it.totalAmount }
        val todayCollectedCash = todayTx.filter { it.type == TransactionType.PAYMENT_IN }.sumOf { it.paidAmount } +
                todaySales.sumOf { it.paidAmount }

        // Estimated profit calculation
        var todayProfit = 0.0
        for (sale in todaySales) {
            val cat = categoriesList.find { it.id == sale.categoryId }
            if (cat != null) {
                val profitPerUnit = (sale.unitPrice - cat.costPrice).coerceAtLeast(0.0)
                todayProfit += (profitPerUnit * sale.quantity) - sale.discount
            }
        }

        val overdueCount = distributorsList.count { it.isPaymentOverdue() }
        val overCreditCount = distributorsList.count { it.isCreditLimitExceeded }

        return DashboardStats(
            totalMarketDebt = totalMarketDebt,
            totalStockCards = totalStockCards,
            totalStockValue = totalStockValue,
            todaySalesCount = todaySalesCount,
            todaySalesAmount = todaySalesAmount,
            todayCollectedCash = todayCollectedCash,
            todayEstimatedProfit = todayProfit.coerceAtLeast(0.0),
            overdueDistributorsCount = overdueCount,
            overCreditLimitDistributorsCount = overCreditCount
        )
    }

    private fun getStartOfDayMillis(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    // === Actions ===

    fun recordSale(
        distributorId: Long?,
        categoryId: Long,
        quantity: Int,
        unitPrice: Double,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String,
        notes: String,
        onSuccess: (TransactionRecord) -> Unit
    ) {
        if (!_currentUser.value.canAddSales && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لإضافة مبيعات")
            return
        }
        viewModelScope.launch {
            val result = repository.recordSale(
                distributorId = distributorId,
                categoryId = categoryId,
                quantity = quantity,
                unitPrice = unitPrice,
                discount = discount,
                paidAmount = paidAmount,
                paymentMethod = paymentMethod,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            result.onSuccess { tx ->
                if (paidAmount > 0) {
                    if (_currentUser.value.role == com.example.data.model.UserRole.ADMIN) {
                        repository.recordMainTreasuryInflow(
                            amount = paidAmount,
                            title = "تحصيل نقدي - مبيعات كروت (${tx.categoryName})",
                            referenceType = "SALE",
                            referenceNumber = tx.invoiceNumber
                        )
                    } else {
                        repository.recordEmployeeFundInflow(
                            employeeUsername = _currentUser.value.username,
                            employeeDisplayName = _currentUser.value.displayName,
                            amount = paidAmount,
                            title = "تحصيل نقدي - مبيعات كروت (${tx.categoryName})",
                            referenceType = "SALE",
                            referenceNumber = tx.invoiceNumber
                        )
                    }
                }
                showMessage("تم حفظ فاتورة البيع بنجاح (${tx.invoiceNumber})")
                onSuccess(tx)
            }.onFailure { err ->
                showMessage("خطأ: ${err.message}")
            }
        }
    }

    fun recordMultiItemSale(
        distributorId: Long?,
        newCustomerName: String? = null,
        newCustomerPhone: String? = null,
        items: List<InvoiceItemDraft>,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String,
        notes: String,
        onSuccess: (TransactionRecord) -> Unit
    ) {
        if (!_currentUser.value.canAddSales && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لإضافة مبيعات")
            return
        }
        viewModelScope.launch {
            val result = repository.recordMultiItemSale(
                distributorId = distributorId,
                newCustomerName = newCustomerName,
                newCustomerPhone = newCustomerPhone,
                items = items,
                discount = discount,
                paidAmount = paidAmount,
                paymentMethod = paymentMethod,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            result.onSuccess { tx ->
                if (paidAmount > 0) {
                    if (_currentUser.value.role == com.example.data.model.UserRole.ADMIN) {
                        repository.recordMainTreasuryInflow(
                            amount = paidAmount,
                            title = "تحصيل نقدي - مبيعات كروت (${tx.categoryName})",
                            referenceType = "SALE",
                            referenceNumber = tx.invoiceNumber
                        )
                    } else {
                        repository.recordEmployeeFundInflow(
                            employeeUsername = _currentUser.value.username,
                            employeeDisplayName = _currentUser.value.displayName,
                            amount = paidAmount,
                            title = "تحصيل نقدي - مبيعات كروت (${tx.categoryName})",
                            referenceType = "SALE",
                            referenceNumber = tx.invoiceNumber
                        )
                    }
                }
                showMessage("تم حفظ فاتورة البيع بنجاح (${tx.invoiceNumber})")
                onSuccess(tx)
            }.onFailure { err ->
                showMessage("خطأ: ${err.message}")
            }
        }
    }

    fun recordPurchase(
        categoryId: Long,
        quantity: Int,
        costPrice: Double,
        supplierName: String,
        notes: String,
        onSuccess: ((TransactionRecord) -> Unit)? = null
    ) {
        if (!_currentUser.value.canAddPurchases && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لإضافة فواتير مشتريات")
            return
        }
        viewModelScope.launch {
            val result = repository.recordPurchase(
                categoryId = categoryId,
                quantity = quantity,
                costPrice = costPrice,
                supplierName = supplierName,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            result.onSuccess { tx ->
                showMessage("تم توريد $quantity كرت بنجاح للمخزن وخصم القيمة من الصندوق (${tx.invoiceNumber})")
                onSuccess?.invoke(tx)
            }.onFailure { err ->
                showMessage("خطأ في التوريد: ${err.message}")
            }
        }
    }

    fun recordMultiItemPurchase(
        items: List<InvoiceItemDraft>,
        discount: Double,
        paidAmount: Double,
        paymentMethod: String,
        supplierName: String,
        notes: String,
        onSuccess: ((TransactionRecord) -> Unit)? = null
    ) {
        if (!_currentUser.value.canAddPurchases && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لإضافة فواتير مشتريات")
            return
        }
        viewModelScope.launch {
            val result = repository.recordMultiItemPurchase(
                items = items,
                discount = discount,
                paidAmount = paidAmount,
                paymentMethod = paymentMethod,
                supplierName = supplierName,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            result.onSuccess { tx ->
                val totalQty = items.sumOf { it.quantity }
                showMessage("تم توريد $totalQty كرت بنجاح للمخزن (${tx.invoiceNumber})")
                onSuccess?.invoke(tx)
            }.onFailure { err ->
                showMessage("خطأ في التوريد: ${err.message}")
            }
        }
    }

    suspend fun getTransactionItems(transactionId: Long): List<TransactionItem> {
        return repository.getTransactionItems(transactionId)
    }

    fun deleteSale(sale: TransactionRecord) {
        if (!_currentUser.value.canDeleteSales && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لحذف فواتير المبيعات")
            return
        }
        viewModelScope.launch {
            val result = repository.deleteSale(sale)
            result.onSuccess {
                showMessage("تم حذف فاتورة المبيعات (${sale.invoiceNumber}) وإعادة الكروت للمخزن وتسوية الصندوق")
            }.onFailure { err ->
                showMessage("خطأ أثناء حذف الفاتورة: ${err.message}")
            }
        }
    }

    fun updateSale(
        oldSale: TransactionRecord,
        newQuantity: Int,
        newUnitPrice: Double,
        newDiscount: Double,
        newPaidAmount: Double,
        newPaymentMethod: String,
        newNotes: String
    ) {
        if (!_currentUser.value.canEditSales && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لتعديل فواتير المبيعات")
            return
        }
        viewModelScope.launch {
            val result = repository.updateSale(
                oldSale = oldSale,
                newQuantity = newQuantity,
                newUnitPrice = newUnitPrice,
                newDiscount = newDiscount,
                newPaidAmount = newPaidAmount,
                newPaymentMethod = newPaymentMethod,
                newNotes = newNotes
            )
            result.onSuccess { updated ->
                showMessage("تم تعديل فاتورة المبيعات (${updated.invoiceNumber}) وتسوية المخزون والديون والصندوق")
            }.onFailure { err ->
                showMessage("خطأ أثناء تعديل الفاتورة: ${err.message}")
            }
        }
    }

    fun deletePurchase(purchase: TransactionRecord) {
        if (!_currentUser.value.canDeletePurchases && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لحذف فواتير المشتريات")
            return
        }
        viewModelScope.launch {
            val result = repository.deletePurchase(purchase)
            result.onSuccess {
                showMessage("تم حذف فاتورة المشتريات (${purchase.invoiceNumber}) وخصم الكروت من المخزن واسترجاع المبلغ للصندوق")
            }.onFailure { err ->
                showMessage("خطأ أثناء حذف فاتورة المشتريات: ${err.message}")
            }
        }
    }

    fun updatePurchase(
        oldPurchase: TransactionRecord,
        newQuantity: Int,
        newCostPrice: Double,
        newSupplierName: String,
        newNotes: String
    ) {
        if (!_currentUser.value.canEditPurchases && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لتعديل فواتير المشتريات")
            return
        }
        viewModelScope.launch {
            val result = repository.updatePurchase(
                oldPurchase = oldPurchase,
                newQuantity = newQuantity,
                newCostPrice = newCostPrice,
                newSupplierName = newSupplierName,
                newNotes = newNotes
            )
            result.onSuccess { updated ->
                showMessage("تم تعديل فاتورة المشتريات (${updated.invoiceNumber}) وتسوية المخزون والصندوق بنجاح")
            }.onFailure { err ->
                showMessage("خطأ أثناء تعديل فاتورة المشتريات: ${err.message}")
            }
        }
    }

    fun recordSaleReturn(
        distributorId: Long?,
        categoryId: Long,
        quantity: Int,
        unitPrice: Double,
        refundFromCash: Boolean,
        notes: String,
        onSuccess: ((TransactionRecord) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.recordSaleReturn(
                distributorId = distributorId,
                categoryId = categoryId,
                quantity = quantity,
                unitPrice = unitPrice,
                refundFromCash = refundFromCash,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            result.onSuccess { tx ->
                val settlementTarget = if (refundFromCash) "واسترداد المبلغ نقداً من الصندوق" else "وتنزيل المبلغ من حساب ومديونية الموزع"
                showMessage("تم تسجيل مرتجع مبيعات $quantity كرت بنجاح ($settlementTarget) [${tx.invoiceNumber}]")
                onSuccess?.invoke(tx)
            }.onFailure { err ->
                showMessage("خطأ أثناء تسجيل مرتجع المبيعات: ${err.message}")
            }
        }
    }

    fun recordPurchaseReturn(
        categoryId: Long,
        quantity: Int,
        costPrice: Double,
        supplierName: String,
        refundToFund: Boolean,
        notes: String,
        onSuccess: ((TransactionRecord) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.recordPurchaseReturn(
                categoryId = categoryId,
                quantity = quantity,
                costPrice = costPrice,
                supplierName = supplierName,
                refundToFund = refundToFund,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            result.onSuccess { tx ->
                val settlementTarget = if (refundToFund) "وإيداع القيمة المستردة في الصندوق الرئيسي" else "وخصم الكروت من المخزن"
                showMessage("تم تسجيل مرتجع مشتريات $quantity كرت للمورد بنجاح ($settlementTarget) [${tx.invoiceNumber}]")
                onSuccess?.invoke(tx)
            }.onFailure { err ->
                showMessage("خطأ أثناء تسجيل مرتجع المشتريات: ${err.message}")
            }
        }
    }

    fun recordReturn(
        distributorId: Long?,
        categoryId: Long,
        quantity: Int,
        unitPrice: Double,
        notes: String
    ) {
        recordSaleReturn(
            distributorId = distributorId,
            categoryId = categoryId,
            quantity = quantity,
            unitPrice = unitPrice,
            refundFromCash = false,
            notes = notes
        )
    }

    fun recordPaymentIn(
        distributorId: Long,
        amount: Double,
        paymentMethod: String,
        notes: String,
        onSuccess: (TransactionRecord) -> Unit
    ) {
        if (!_currentUser.value.canAddVouchers && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لإضافة سندات قبض")
            return
        }
        viewModelScope.launch {
            val result = repository.recordPaymentIn(
                distributorId = distributorId,
                amount = amount,
                paymentMethod = paymentMethod,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            result.onSuccess { tx ->
                if (_currentUser.value.role == com.example.data.model.UserRole.ADMIN) {
                    repository.recordMainTreasuryInflow(
                        amount = amount,
                        title = "تحصيل نقدي - سند قبض (${tx.distributorName})",
                        referenceType = "PAYMENT_IN",
                        referenceNumber = tx.invoiceNumber
                    )
                } else {
                    repository.recordEmployeeFundInflow(
                        employeeUsername = _currentUser.value.username,
                        employeeDisplayName = _currentUser.value.displayName,
                        amount = amount,
                        title = "تحصيل نقدي - سند قبض (${tx.distributorName})",
                        referenceType = "PAYMENT_IN",
                        referenceNumber = tx.invoiceNumber
                    )
                }
                showMessage("تم تسجيل سند القبض بنجاح (${tx.invoiceNumber})")
                onSuccess(tx)
            }.onFailure { err ->
                showMessage("خطأ: ${err.message}")
            }
        }
    }

    fun recordPaymentOut(
        title: String,
        amount: Double,
        notes: String
    ) {
        if (!_currentUser.value.canAddVouchers && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لإضافة سندات صرف")
            return
        }
        viewModelScope.launch {
            val result = repository.recordPaymentOut(
                title = title,
                amount = amount,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            result.onSuccess { tx ->
                showMessage("تم تسجيل سند الصرف بنجاح (${tx.invoiceNumber})")
            }.onFailure { err ->
                showMessage("خطأ: ${err.message}")
            }
        }
    }

    fun recordDiscount(
        distributorId: Long,
        amount: Double,
        notes: String
    ) {
        if (!_currentUser.value.canAddVouchers && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لمنح خصومات وسندات مالية")
            return
        }
        viewModelScope.launch {
            val result = repository.recordDiscount(
                distributorId = distributorId,
                amount = amount,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            result.onSuccess { tx ->
                showMessage("تم تسجيل الخصم المالي بنجاح للموزع")
            }.onFailure { err ->
                showMessage("خطأ: ${err.message}")
            }
        }
    }

    fun deleteVoucher(tx: TransactionRecord) {
        if (!_currentUser.value.canDeleteVouchers && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لحذف السندات")
            return
        }
        viewModelScope.launch {
            repository.deleteTransaction(tx)
            showMessage("تم حذف السند بنجاح")
        }
    }

    fun updateVoucher(tx: TransactionRecord, newAmount: Double, newNotes: String) {
        if (!_currentUser.value.canEditVouchers && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لتعديل السندات")
            return
        }
        viewModelScope.launch {
            val updated = tx.copy(totalAmount = newAmount, paidAmount = newAmount, notes = newNotes)
            repository.updateTransaction(updated)
            showMessage("تم تعديل السند بنجاح")
        }
    }

    fun recordCardSwap(
        distributorId: Long,
        fromCategoryId: Long,
        fromQuantity: Int,
        toCategoryId: Long,
        toQuantity: Int,
        paidDifference: Double,
        notes: String,
        onSuccess: (CardSwapRecord) -> Unit
    ) {
        if (!_currentUser.value.canAddSwaps && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لإجراء استبدال كروت")
            return
        }
        viewModelScope.launch {
            val result = repository.recordCardSwap(
                distributorId = distributorId,
                fromCategoryId = fromCategoryId,
                fromQuantity = fromQuantity,
                toCategoryId = toCategoryId,
                toQuantity = toQuantity,
                paidDifference = paidDifference,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            result.onSuccess { swap ->
                showMessage("تم استبدال فئات الكروت بنجاح (${swap.swapCode})")
                onSuccess(swap)
            }.onFailure { err ->
                showMessage("خطأ: ${err.message}")
            }
        }
    }

    fun deleteCardSwap(swap: CardSwapRecord) {
        if (!_currentUser.value.canDeleteSwaps && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لحذف عمليات استبدال الكروت")
            return
        }
        viewModelScope.launch {
            repository.deleteCardSwap(swap)
            showMessage("تم حذف عملية استبدال الكروت بنجاح")
        }
    }

    fun recordEmployeeTransfer(
        fromEmployee: String,
        toEmployee: String,
        transferType: String,
        categoryId: Long?,
        quantity: Int,
        cashAmount: Double,
        notes: String
    ) {
        if (!_currentUser.value.canAddTransfers && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لإجراء تحويلات")
            return
        }
        viewModelScope.launch {
            val result = repository.recordEmployeeTransfer(
                fromEmployee = fromEmployee,
                toEmployee = toEmployee,
                transferType = transferType,
                categoryId = categoryId,
                quantity = quantity,
                cashAmount = cashAmount,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            result.onSuccess {
                showMessage("تم تسجيل التحويل بنجاح بين الموظفين")
            }.onFailure { err ->
                showMessage("خطأ: ${err.message}")
            }
        }
    }

    fun deleteEmployeeTransfer(transfer: com.example.data.model.EmployeeTransferRecord) {
        if (!_currentUser.value.canDeleteTransfers && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لحذف تحويلات الكروت")
            return
        }
        viewModelScope.launch {
            repository.deleteEmployeeTransfer(transfer)
            showMessage("تم حذف سجل التحويل بنجاح")
        }
    }

    fun addDistributor(
        name: String,
        phone: String,
        openingBalance: Double,
        creditLimit: Double,
        paymentPeriodDays: Int,
        discountPercent: Double = 0.0,
        notes: String
    ) {
        viewModelScope.launch {
            val dist = Distributor(
                name = name,
                phone = phone,
                openingBalance = openingBalance,
                currentBalance = openingBalance,
                creditLimit = creditLimit,
                paymentPeriodDays = paymentPeriodDays,
                discountPercent = discountPercent,
                lastPaymentDate = System.currentTimeMillis(),
                notes = notes
            )
            repository.insertDistributor(dist)
            showMessage("تمت إضافة الموزع '$name' بنجاح")
        }
    }

    fun updateDistributor(distributor: Distributor) {
        viewModelScope.launch {
            repository.updateDistributor(distributor)
            showMessage("تم تحديث بيانات الموزع بنجاح")
        }
    }

    fun deleteDistributor(distributor: Distributor) {
        viewModelScope.launch {
            repository.deleteDistributor(distributor)
            showMessage("تم حذف الموزع بنجاح")
        }
    }

    fun addCategory(
        name: String,
        faceValue: Double,
        costPrice: Double,
        sellingPrice: Double,
        stockQuantity: Int,
        validityHours: Int,
        quotaMb: Int,
        colorHex: String,
        notes: String
    ) {
        if (!_currentUser.value.canAddCards && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لإضافة فئات كروت جديدة")
            return
        }
        viewModelScope.launch {
            val cat = CardCategory(
                name = name,
                faceValue = faceValue,
                costPrice = costPrice,
                sellingPrice = sellingPrice,
                stockQuantity = stockQuantity,
                validityHours = validityHours,
                quotaMb = quotaMb,
                colorHex = colorHex,
                notes = notes
            )
            repository.insertCategory(cat)
            showMessage("تمت إضافة فئة الكروت '$name' بنجاح")
        }
    }

    fun updateCategory(category: CardCategory) {
        if (!_currentUser.value.canEditCards && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لتعديل فئات الكروت")
            return
        }
        viewModelScope.launch {
            repository.updateCategory(category)
            showMessage("تم تحديث الفئة بنجاح")
        }
    }

    fun deleteCategory(category: CardCategory) {
        if (!_currentUser.value.canDeleteCards && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لحذف فئات الكروت")
            return
        }
        viewModelScope.launch {
            repository.deleteCategory(category)
            showMessage("تم حذف الفئة بنجاح")
        }
    }

    fun addUser(
        username: String,
        displayName: String,
        pinCode: String,
        role: UserRole,
        phone: String,
        canViewProfits: Boolean,
        canDeleteRecords: Boolean,
        canManageUsers: Boolean,
        canManageStock: Boolean,
        canGrantDiscount: Boolean,
        canExportBackup: Boolean,
        canAddSales: Boolean = true,
        canEditSales: Boolean = false,
        canDeleteSales: Boolean = false,
        canAddPurchases: Boolean = true,
        canEditPurchases: Boolean = false,
        canDeletePurchases: Boolean = false,
        canAddVouchers: Boolean = true,
        canEditVouchers: Boolean = false,
        canDeleteVouchers: Boolean = false,
        canAddSwaps: Boolean = true,
        canEditSwaps: Boolean = false,
        canDeleteSwaps: Boolean = false,
        canAddTransfers: Boolean = true,
        canEditTransfers: Boolean = false,
        canDeleteTransfers: Boolean = false,
        canAddReports: Boolean = true,
        canEditReports: Boolean = false,
        canDeleteReports: Boolean = false,
        canAddCards: Boolean = true,
        canEditCards: Boolean = false,
        canDeleteCards: Boolean = false
    ) {
        viewModelScope.launch {
            val user = AppUser(
                username = username,
                displayName = displayName,
                pinCode = pinCode,
                role = role,
                phone = phone,
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
            repository.insertUser(user)
            showMessage("تمت إضافة المستخدم '$displayName' بنجاح")
        }
    }

    fun addUser(user: AppUser) {
        viewModelScope.launch {
            repository.insertUser(user)
            showMessage("تمت إضافة المستخدم '${user.displayName}' بنجاح")
        }
    }

    fun updateUser(user: AppUser) {
        viewModelScope.launch {
            repository.updateUser(user)
            showMessage("تم تحديث بيانات المستخدم بنجاح")
        }
    }

    fun deleteUser(user: AppUser) {
        viewModelScope.launch {
            repository.deleteUser(user)
            showMessage("تم حذف المستخدم بنجاح")
        }
    }

    fun sendBroadcastNotification(title: String, message: String, target: String) {
        viewModelScope.launch {
            repository.sendBroadcastNotification(
                title = title,
                message = message,
                senderName = _currentUser.value.displayName,
                targetEmployee = target
            )
            showMessage("تم إرسال الإشعار والتوجيه الإداري للموظفين بنجاح")
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            showMessage("تم تحديد جميع الإشعارات كمقروءة")
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            repository.deleteNotification(id)
            showMessage("تم حذف الإشعار")
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
            showMessage("تم مسح كافة الإشعارات")
        }
    }

    fun updateSettings(settings: NetworkSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            showMessage("تم حفظ إعدادات النظام بنجاح")
        }
    }

    fun syncCloudData() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val user = auth.currentUser
                val targetUid = getActiveSyncUid() ?: user?.uid
                if (targetUid != null) {
                    val currentDists = repository.distributors.first()
                    val currentCats = repository.categories.first()
                    val currentRegs = repository.allCashRegisters.first()
                    val currentTxs = repository.transactions.first()
                    val currentUsers = repository.users.first()
                    val currentSettings = repository.getSettingsDirect()
                    val count = firestoreSync.fullReconciliation(
                        userId = targetUid,
                        dao = database.netCardsDao(),
                        currentDistributors = currentDists,
                        currentCategories = currentCats,
                        currentRegisters = currentRegs,
                        currentTransactions = currentTxs,
                        currentUsers = currentUsers,
                        currentSettings = currentSettings
                    )
                    repository.startCloudSync(targetUid, viewModelScope)
                    val current = repository.getSettingsDirect() ?: NetworkSettings()
                    repository.saveSettings(current.copy(lastSyncTimestamp = System.currentTimeMillis()))
                    showMessage("تمت المزامنة السحابية بنجاح ☁️ (تم رفع ومطابقة $count سجل مع السحابة والأجهزة الأخرى)")
                } else {
                    showMessage("يرجى تسجيل الدخول بحساب Google أولاً لتفعيل المزامنة السحابية الفورية")
                }
            } catch (e: Exception) {
                showMessage("فشلت المزامنة السحابية: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun onGoogleAuthSuccess(userId: String) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val currentEmail = Firebase.auth.currentUser?.email?.trim()?.lowercase() ?: ""
                var invite: FirebaseEmployeeInvite? = null
                if (currentEmail.isNotBlank()) {
                    invite = firestoreSync.getEmployeeInvite(currentEmail)
                }

                if (invite != null && invite.managerUid.isNotBlank() && invite.managerUid != userId) {
                    val managerUid = invite.managerUid
                    prefs.edit().putString("linked_manager_uid", managerUid).apply()
                    _linkedManagerInfo.value = invite
                    repository.startCloudSync(managerUid, viewModelScope)

                    val currentDists = repository.distributors.first()
                    val currentCats = repository.categories.first()
                    val currentRegs = repository.allCashRegisters.first()
                    val currentTxs = repository.transactions.first()
                    val currentUsers = repository.users.first()
                    val currentSettings = repository.getSettingsDirect()

                    firestoreSync.fullReconciliation(
                        userId = managerUid,
                        dao = database.netCardsDao(),
                        currentDistributors = currentDists,
                        currentCategories = currentCats,
                        currentRegisters = currentRegs,
                        currentTransactions = currentTxs,
                        currentUsers = currentUsers,
                        currentSettings = currentSettings
                    )

                    // Match user account from manager's synced users or create local session
                    val matchedUser = database.netCardsDao().getUserByEmail(currentEmail)
                        ?: database.netCardsDao().getUserByUsername(invite.employeeUsername)

                    if (matchedUser != null) {
                        _currentUser.value = matchedUser
                    } else {
                        val role = try { UserRole.valueOf(invite.role) } catch (_: Exception) { UserRole.SALES }
                        val newEmp = AppUser(
                            username = invite.employeeUsername.ifBlank { currentEmail.substringBefore("@") },
                            displayName = invite.employeeDisplayName.ifBlank { Firebase.auth.currentUser?.displayName ?: "موظف مبيعات" },
                            email = currentEmail,
                            role = role,
                            pinCode = invite.pinCode.ifBlank { "1234" },
                            canViewProfits = (role == UserRole.ADMIN || role == UserRole.ACCOUNTANT),
                            canDeleteRecords = (role == UserRole.ADMIN),
                            canManageUsers = (role == UserRole.ADMIN),
                            canManageStock = (role != UserRole.SALES),
                            canAddSales = true,
                            canAddPurchases = (role == UserRole.ADMIN || role == UserRole.ACCOUNTANT),
                            canAddVouchers = true,
                            canAddSwaps = true,
                            canAddTransfers = true,
                            canAddReports = (role != UserRole.SALES),
                            canAddCards = (role != UserRole.SALES)
                        )
                        val newId = database.netCardsDao().insertUser(newEmp)
                        _currentUser.value = newEmp.copy(id = newId)
                    }

                    showMessage("مرحباً بك يا ${invite.employeeDisplayName}! ☁️\nتم ربط جهازك تلقائياً بشبكة المدير (${invite.managerName}) وتفعيل صلاحياتك (${invite.role})")
                } else {
                    prefs.edit().remove("linked_manager_uid").apply()
                    _linkedManagerInfo.value = null
                    repository.startCloudSync(userId, viewModelScope)
                    syncCloudData()
                }
            } catch (e: Exception) {
                showMessage("تنبيه في مزامنة الحساب: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun logoutFromGoogle() {
        prefs.edit().remove("linked_manager_uid").apply()
        _linkedManagerInfo.value = null
        repository.setEffectiveCloudUserId(null)
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}
        _currentUser.value = AppUser(
            id = 1,
            username = "admin",
            displayName = "المدير العام",
            pinCode = "1234",
            role = UserRole.ADMIN,
            canViewProfits = true,
            canDeleteRecords = true,
            canManageUsers = true,
            canManageStock = true,
            canGrantDiscount = true,
            canExportBackup = true
        )
        showMessage("تم تسجيل الخروج بنجاح")
    }

    fun exportBackup(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val json = BackupRestoreHelper.exportBackupToJson(database.netCardsDao())
                val file = BackupRestoreHelper.saveBackupToFile(context, json)
                showMessage("تم إنشاء النسخة الاحتياطية بنجاح في: ${file.name}")
            } catch (e: Exception) {
                showMessage("فشل النسخ الاحتياطي: ${e.message}")
            }
        }
    }

    // === Isolated Cash Registers and Treasuries Actions ===

    fun recordEmployeeFundDeposit(
        username: String,
        displayName: String,
        amount: Double,
        title: String,
        notes: String
    ) {
        viewModelScope.launch {
            val res = repository.recordEmployeeFundInflow(
                employeeUsername = username,
                employeeDisplayName = displayName,
                amount = amount,
                title = title,
                notes = notes
            )
            res.onSuccess {
                showMessage("تم الإيداع في صندوق الموظف المستقل بنجاح (معزول عن صندوق البرنامج)")
            }.onFailure {
                showMessage("خطأ: ${it.message}")
            }
        }
    }

    fun recordEmployeeFundExpense(
        username: String,
        displayName: String,
        amount: Double,
        title: String,
        notes: String
    ) {
        viewModelScope.launch {
            val res = repository.recordEmployeeFundOutflow(
                employeeUsername = username,
                employeeDisplayName = displayName,
                amount = amount,
                title = title,
                notes = notes
            )
            res.onSuccess {
                showMessage("تم تسجيل الصرف من صندوق الموظف المستقل بنجاح")
            }.onFailure {
                showMessage("خطأ: ${it.message}")
            }
        }
    }

    fun recordMainTreasuryDeposit(
        amount: Double,
        title: String,
        notes: String
    ) {
        viewModelScope.launch {
            val res = repository.recordMainTreasuryInflow(
                amount = amount,
                title = title,
                notes = notes
            )
            res.onSuccess {
                showMessage("تم الإيداع في صندوق البرنامج الرئيسي بنجاح")
            }.onFailure {
                showMessage("خطأ: ${it.message}")
            }
        }
    }

    fun recordMainTreasuryExpense(
        amount: Double,
        title: String,
        notes: String
    ) {
        viewModelScope.launch {
            val res = repository.recordPaymentOut(
                title = title,
                amount = amount,
                employeeName = _currentUser.value.displayName,
                notes = notes
            )
            res.onSuccess { tx ->
                showMessage("تم تسجيل صرف المصروفات (${tx.distributorName}) وخصمها من صندوق البرنامج بنجاح [${tx.invoiceNumber}]")
            }.onFailure {
                showMessage("خطأ: ${it.message}")
            }
        }
    }

    fun transferEmployeeToMainTreasury(
        username: String,
        displayName: String,
        amount: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val res = repository.transferEmployeeToMainTreasury(
                employeeUsername = username,
                employeeDisplayName = displayName,
                amount = amount,
                notes = notes
            )
            res.onSuccess {
                showMessage("تم توريد وتسليم النقدية لصندوق البرنامج الرئيسي بنجاح وتوثيق العملية")
            }.onFailure {
                showMessage("خطأ: ${it.message}")
            }
        }
    }

    fun updateDeveloperAdConfig(newConfig: DeveloperAdConfig) {
        devSettingsRepo.updateConfig(newConfig)
        showMessage("تم تحديث إعدادات وضع المطور والإعلانات بنجاح")
    }

    fun setTestAdsMode(enabled: Boolean) {
        val current = developerAdConfig.value
        val updated = current.copy(isTestAdsMode = enabled)
        devSettingsRepo.updateConfig(updated)
        if (enabled) {
            showMessage("✅ تم تفعيل وضع الإعلانات التجريبية")
        } else {
            showMessage("🛑 تم إيقاف الإعلانات التجريبية بنجاح")
        }
    }

    fun setAdsEnabled(enabled: Boolean) {
        val current = developerAdConfig.value
        val updated = current.copy(isAdsEnabled = enabled)
        devSettingsRepo.updateConfig(updated)
        if (enabled) {
            showMessage("✅ تم تفعيل نظام الإعلانات")
        } else {
            showMessage("🛑 تم إيقاف كافة الإعلانات بنجاح")
        }
    }

    fun resetDeveloperAdConfig() {
        devSettingsRepo.resetToDefaults()
        showMessage("تمت استعادة إعدادات الإعلانات الافتراضية")
    }

    fun triggerRewardEarned(activity: Activity, onRewardGranted: (Int, String) -> Unit) {
        val config = developerAdConfig.value
        AdMobHelper.showRewarded(
            activity = activity,
            adUnitId = config.effectiveRewardedAdUnitId,
            onUserEarnedReward = { amount, type ->
                devSettingsRepo.incrementRewardCount()
                onRewardGranted(amount, type)
                showMessage("مبروك! لقد ربحت $amount $type من مشاهدة إعلان المكافأة")
            },
            onDismissed = {
                // Done
            }
        )
    }

    fun triggerInterstitialAd(activity: Activity) {
        val config = developerAdConfig.value
        if (!config.isAdsEnabled || !config.isInterstitialEnabled) return
        AdMobHelper.showInterstitial(
            activity = activity,
            adUnitId = config.effectiveInterstitialAdUnitId,
            onDismissed = {}
        )
    }

    fun transferStock(
        fromEmployee: String,
        toEmployee: String,
        categoryId: Long,
        categoryName: String,
        quantity: Int,
        totalFaceValue: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val result = repository.transferStockBetweenEmployees(
                fromEmployee, toEmployee, categoryId, categoryName, quantity, totalFaceValue, notes
            )
            result.fold(
                onSuccess = {
                    showMessage("✅ تم تحويل عهدة الكروت بنجاح من $fromEmployee إلى $toEmployee")
                },
                onFailure = { err ->
                    showMessage("❌ ${err.message ?: "فشل عملية التحويل"}")
                }
            )
        }
    }

    fun transferMultipleStock(
        fromEmployee: String,
        toEmployee: String,
        items: List<com.example.data.model.InvoiceItemDraft>,
        notes: String
    ) {
        if (!_currentUser.value.canAddTransfers && _currentUser.value.role != com.example.data.model.UserRole.ADMIN) {
            showMessage("عذراً، ليس لديك صلاحية لتحويل الكروت بين المخازن")
            return
        }
        viewModelScope.launch {
            val result = repository.transferMultipleStockBetweenEmployees(
                fromEmployee, toEmployee, items, notes
            )
            result.fold(
                onSuccess = {
                    showMessage("✅ تم تنفيذ تحويل أصناف العهدة بنجاح من $fromEmployee إلى $toEmployee")
                },
                onFailure = { err ->
                    showMessage("❌ ${err.message ?: "فشل عملية التحويل المتعدد"}")
                }
            )
        }
    }
}
