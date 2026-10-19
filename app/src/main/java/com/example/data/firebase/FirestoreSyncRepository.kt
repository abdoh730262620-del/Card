package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.NetCardsDao
import com.example.data.model.AppUser
import com.example.data.model.CardCategory
import com.example.data.model.CardSwapRecord
import com.example.data.model.CashRegister
import com.example.data.model.FundType
import com.example.data.model.Distributor
import com.example.data.model.EmployeeTransferRecord
import com.example.data.model.FundMovement
import com.example.data.model.MovementDirection
import com.example.data.model.NetworkSettings
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionType
import com.example.data.model.UserRole
import com.example.data.model.SystemNotification
import com.example.data.model.NotificationType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreSyncRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val activeListeners = mutableListOf<ListenerRegistration>()

    private fun userDoc(userId: String) = db.collection("users").document(userId)

    fun stopListeners() {
        activeListeners.forEach { it.remove() }
        activeListeners.clear()
    }

    fun observeDistributors(userId: String): Flow<List<FirebaseDistributor>> = callbackFlow {
        val reg = userDoc(userId).collection("distributors")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(FirebaseDistributor::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { reg.remove() }
    }

    // === Individual Real-time Push methods (Room -> Firestore) ===

    suspend fun syncDistributor(userId: String, dist: Distributor) {
        val fDist = FirebaseDistributor(
            id = dist.id.toString(),
            userId = userId,
            name = dist.name,
            phone = dist.phone,
            currentBalance = dist.currentBalance,
            creditLimit = dist.creditLimit,
            paymentPeriodDays = dist.paymentPeriodDays.toLong(),
            discountPercent = dist.discountPercent,
            lastPaymentDate = dist.lastPaymentDate,
            notes = dist.notes,
            isActive = dist.isActive
        )
        userDoc(userId).collection("distributors").document(fDist.id).set(fDist).await()
    }

    suspend fun deleteDistributor(userId: String, distId: Long) {
        try {
            userDoc(userId).collection("distributors").document(distId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting distributor: ${e.message}")
        }
    }

    suspend fun syncCategory(userId: String, cat: CardCategory) {
        try {
            val fCat = FirebaseCardCategory(
                id = cat.id.toString(),
                userId = userId,
                name = cat.name,
                faceValue = cat.faceValue,
                costPrice = cat.costPrice,
                sellingPrice = cat.sellingPrice,
                stockQuantity = cat.stockQuantity.toLong(),
                validityHours = cat.validityHours.toLong(),
                quotaMb = cat.quotaMb.toLong()
            )
            userDoc(userId).collection("categories").document(fCat.id).set(fCat).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing category: ${e.message}")
        }
    }

    suspend fun deleteCategory(userId: String, catId: Long) {
        try {
            userDoc(userId).collection("categories").document(catId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting category: ${e.message}")
        }
    }

    suspend fun syncRegister(userId: String, reg: CashRegister) {
        try {
            val fReg = FirebaseCashRegister(
                id = reg.id.toString(),
                userId = userId,
                name = reg.name,
                type = reg.type.name,
                currentBalance = reg.currentBalance,
                openingBalance = reg.openingBalance,
                totalInflows = reg.totalInflows,
                totalOutflows = reg.totalOutflows,
                employeeUsername = reg.employeeUsername ?: "",
                employeeDisplayName = reg.employeeDisplayName ?: ""
            )
            userDoc(userId).collection("registers").document(fReg.id).set(fReg).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing register: ${e.message}")
        }
    }

    suspend fun syncTransaction(userId: String, tx: TransactionRecord) {
        try {
            val fTx = FirebaseTransactionRecord(
                id = tx.id.toString(),
                userId = userId,
                invoiceNumber = tx.invoiceNumber,
                type = tx.type.name,
                distributorId = tx.distributorId,
                distributorName = tx.distributorName,
                categoryId = tx.categoryId,
                categoryName = tx.categoryName,
                quantity = tx.quantity.toLong(),
                unitPrice = tx.unitPrice,
                totalAmount = tx.totalAmount,
                discount = tx.discount,
                paidAmount = tx.paidAmount,
                remainingAmount = tx.remainingAmount,
                paymentMethod = tx.paymentMethod,
                employeeName = tx.employeeName,
                notes = tx.notes,
                date = tx.date
            )
            userDoc(userId).collection("transactions").document(fTx.id).set(fTx).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing transaction: ${e.message}")
        }
    }

    suspend fun deleteTransaction(userId: String, txId: Long) {
        try {
            userDoc(userId).collection("transactions").document(txId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting transaction: ${e.message}")
        }
    }

    suspend fun syncAppUser(userId: String, user: AppUser) {
        try {
            val fUser = FirebaseAppUser(
                id = user.id.toString(),
                userId = userId,
                username = user.username,
                displayName = user.displayName,
                pinCode = user.pinCode,
                role = user.role.name,
                phone = user.phone,
                email = user.email,
                canViewProfits = user.canViewProfits,
                canDeleteRecords = user.canDeleteRecords,
                canManageUsers = user.canManageUsers,
                canManageStock = user.canManageStock,
                canGrantDiscount = user.canGrantDiscount,
                canExportBackup = user.canExportBackup,
                isActive = user.isActive,
                canAddSales = user.canAddSales,
                canEditSales = user.canEditSales,
                canDeleteSales = user.canDeleteSales,
                canAddPurchases = user.canAddPurchases,
                canEditPurchases = user.canEditPurchases,
                canDeletePurchases = user.canDeletePurchases,
                canAddVouchers = user.canAddVouchers,
                canEditVouchers = user.canEditVouchers,
                canDeleteVouchers = user.canDeleteVouchers,
                canAddSwaps = user.canAddSwaps,
                canEditSwaps = user.canEditSwaps,
                canDeleteSwaps = user.canDeleteSwaps,
                canAddTransfers = user.canAddTransfers,
                canEditTransfers = user.canEditTransfers,
                canDeleteTransfers = user.canDeleteTransfers,
                canAddReports = user.canAddReports,
                canEditReports = user.canEditReports,
                canDeleteReports = user.canDeleteReports,
                canAddCards = user.canAddCards,
                canEditCards = user.canEditCards,
                canDeleteCards = user.canDeleteCards
            )
            userDoc(userId).collection("app_users").document(fUser.id).set(fUser).await()

            // If user has email invitation, register in global employee_invites index for automatic linkage
            if (user.email.isNotBlank()) {
                val cleanEmail = user.email.trim().lowercase()
                val currentAuth = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val invite = FirebaseEmployeeInvite(
                    email = cleanEmail,
                    managerUid = userId,
                    managerEmail = currentAuth?.email ?: "",
                    managerName = currentAuth?.displayName ?: "المدير العام",
                    employeeUsername = user.username,
                    employeeDisplayName = user.displayName,
                    role = user.role.name,
                    pinCode = user.pinCode,
                    updatedAt = System.currentTimeMillis()
                )
                db.collection("employee_invites").document(cleanEmail).set(invite).await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing user: ${e.message}")
        }
    }

    suspend fun deleteAppUser(userId: String, userIdToDelete: Long, email: String = "") {
        try {
            userDoc(userId).collection("app_users").document(userIdToDelete.toString()).delete().await()
            if (email.isNotBlank()) {
                val cleanEmail = email.trim().lowercase()
                db.collection("employee_invites").document(cleanEmail).delete().await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting user: ${e.message}")
        }
    }

    suspend fun getEmployeeInvite(email: String): FirebaseEmployeeInvite? {
        return try {
            if (email.isBlank()) return null
            val cleanEmail = email.trim().lowercase()
            val snap = db.collection("employee_invites").document(cleanEmail).get().await()
            if (snap.exists()) {
                snap.toObject(FirebaseEmployeeInvite::class.java)
            } else null
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error fetching invite for $email: ${e.message}")
            null
        }
    }

    suspend fun syncCardSwap(userId: String, swap: CardSwapRecord) {
        try {
            val fSwap = FirebaseCardSwap(
                id = swap.id.toString(),
                userId = userId,
                distributorId = swap.distributorId,
                distributorName = swap.distributorName,
                fromCategoryId = swap.fromCategoryId,
                fromCategoryName = swap.fromCategoryName,
                fromQuantity = swap.fromQuantity.toLong(),
                fromUnitPrice = swap.fromUnitPrice,
                fromTotalValue = swap.fromTotalValue,
                toCategoryId = swap.toCategoryId,
                toCategoryName = swap.toCategoryName,
                toQuantity = swap.toQuantity.toLong(),
                toUnitPrice = swap.toUnitPrice,
                toTotalValue = swap.toTotalValue,
                differenceAmount = swap.differenceAmount,
                paidDifference = swap.paidDifference,
                remainingDifference = swap.remainingDifference,
                date = swap.date,
                employeeName = swap.employeeName,
                notes = swap.notes
            )
            userDoc(userId).collection("card_swaps").document(fSwap.id).set(fSwap).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing card swap: ${e.message}")
        }
    }

    suspend fun syncEmployeeTransfer(userId: String, transfer: EmployeeTransferRecord) {
        try {
            val fTransfer = FirebaseEmployeeTransfer(
                id = transfer.id.toString(),
                userId = userId,
                transferCode = transfer.transferCode,
                fromEmployee = transfer.fromEmployee,
                toEmployee = transfer.toEmployee,
                transferType = transfer.transferType,
                categoryId = transfer.categoryId,
                categoryName = transfer.categoryName,
                quantity = transfer.quantity.toLong(),
                totalFaceValue = transfer.totalFaceValue,
                cashAmount = transfer.cashAmount,
                date = transfer.date,
                status = transfer.status,
                notes = transfer.notes
            )
            userDoc(userId).collection("employee_transfers").document(fTransfer.id).set(fTransfer).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing employee transfer: ${e.message}")
        }
    }

    suspend fun syncNotification(userId: String, notif: SystemNotification) {
        try {
            val fNotif = FirebaseSystemNotification(
                id = notif.id.toString(),
                userId = userId,
                title = notif.title,
                message = notif.message,
                type = notif.type.name,
                targetEmployee = notif.targetEmployee,
                senderName = notif.senderName,
                date = notif.date,
                isRead = notif.isRead,
                actionUrlOrId = notif.actionUrlOrId
            )
            userDoc(userId).collection("system_notifications").document(fNotif.id).set(fNotif).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing notification: ${e.message}")
        }
    }

    suspend fun deleteNotification(userId: String, notifId: Long) {
        try {
            userDoc(userId).collection("system_notifications").document(notifId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting notification: ${e.message}")
        }
    }

    suspend fun deleteCardSwap(userId: String, swapId: Long) {
        try {
            userDoc(userId).collection("card_swaps").document(swapId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting card swap: ${e.message}")
        }
    }

    suspend fun deleteEmployeeTransfer(userId: String, transferId: Long) {
        try {
            userDoc(userId).collection("employee_transfers").document(transferId.toString()).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting employee transfer: ${e.message}")
        }
    }

    suspend fun syncSettings(userId: String, settings: NetworkSettings) {
        try {
            val fSettings = FirebaseNetworkSettings(
                id = "settings",
                userId = userId,
                networkName = settings.networkName,
                managerName = settings.managerName,
                managerPhone = settings.managerPhone,
                currency = settings.currency,
                requirePinOnLaunch = settings.requirePinOnLaunch,
                biometricEnabled = settings.biometricEnabled,
                cloudSyncEnabled = settings.cloudSyncEnabled,
                lastSyncTimestamp = System.currentTimeMillis()
            )
            userDoc(userId).collection("settings").document("settings").set(fSettings).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing settings: ${e.message}")
        }
    }

    suspend fun syncFundMovement(userId: String, movement: FundMovement) {
        try {
            val fMovement = FirebaseFundMovement(
                id = movement.id.toString(),
                userId = userId,
                registerId = movement.registerId,
                registerName = movement.registerName,
                isEmployeeFund = movement.isEmployeeFund,
                employeeUsername = movement.employeeUsername ?: "",
                employeeDisplayName = movement.employeeDisplayName ?: "",
                direction = movement.direction.name,
                amount = movement.amount,
                balanceAfter = movement.balanceAfter,
                title = movement.title,
                referenceType = movement.referenceType,
                referenceNumber = movement.referenceNumber,
                notes = movement.notes,
                date = movement.date
            )
            userDoc(userId).collection("fund_movements").document(fMovement.id).set(fMovement).await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error syncing fund movement: ${e.message}")
        }
    }

    // === Real-time Continuous Inbound Sync (Firestore -> Room) ===

    fun startRealtimeSync(userId: String, dao: NetCardsDao, scope: CoroutineScope) {
        stopListeners()

        // 1. Observe Distributors
        val distListener = userDoc(userId).collection("distributors")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val distList = snapshot.documents.mapNotNull { doc ->
                        val f = doc.toObject(FirebaseDistributor::class.java) ?: return@mapNotNull null
                        Distributor(
                            id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                            name = f.name,
                            phone = f.phone,
                            currentBalance = f.currentBalance,
                            creditLimit = f.creditLimit,
                            paymentPeriodDays = f.paymentPeriodDays.toInt(),
                            discountPercent = f.discountPercent,
                            lastPaymentDate = f.lastPaymentDate,
                            notes = f.notes,
                            isActive = f.isActive
                        )
                    }
                    if (distList.isNotEmpty()) {
                        dao.insertDistributors(distList)
                    }
                }
            }
        activeListeners.add(distListener)

        // 2. Observe Categories
        val catListener = userDoc(userId).collection("categories")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val catList = snapshot.documents.mapNotNull { doc ->
                        val f = doc.toObject(FirebaseCardCategory::class.java) ?: return@mapNotNull null
                        CardCategory(
                            id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                            name = f.name,
                            faceValue = f.faceValue,
                            costPrice = f.costPrice,
                            sellingPrice = f.sellingPrice,
                            stockQuantity = f.stockQuantity.toInt(),
                            validityHours = f.validityHours.toInt(),
                            quotaMb = f.quotaMb.toInt()
                        )
                    }
                    if (catList.isNotEmpty()) {
                        dao.insertCategories(catList)
                    }
                }
            }
        activeListeners.add(catListener)

        // 3. Observe Cash Registers
        val regListener = userDoc(userId).collection("registers")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val regList = snapshot.documents.mapNotNull { doc ->
                        val f = doc.toObject(FirebaseCashRegister::class.java) ?: return@mapNotNull null
                        val regType = try { FundType.valueOf(f.type) } catch (_: Exception) { FundType.MAIN_TREASURY }
                        CashRegister(
                            id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                            name = f.name,
                            type = regType,
                            currentBalance = f.currentBalance,
                            openingBalance = f.openingBalance,
                            totalInflows = f.totalInflows,
                            totalOutflows = f.totalOutflows,
                            employeeUsername = f.employeeUsername.ifBlank { null },
                            employeeDisplayName = f.employeeDisplayName.ifBlank { null }
                        )
                    }
                    if (regList.isNotEmpty()) {
                        dao.insertCashRegisters(regList)
                    }
                }
            }
        activeListeners.add(regListener)

        // 4. Observe Transactions / Invoices / Vouchers
        val txListener = userDoc(userId).collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val txList = snapshot.documents.mapNotNull { doc ->
                        val f = doc.toObject(FirebaseTransactionRecord::class.java) ?: return@mapNotNull null
                        val tType = try { TransactionType.valueOf(f.type) } catch (_: Exception) { TransactionType.SALE }
                        TransactionRecord(
                            id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                            invoiceNumber = f.invoiceNumber,
                            type = tType,
                            distributorId = f.distributorId,
                            distributorName = f.distributorName,
                            categoryId = f.categoryId,
                            categoryName = f.categoryName,
                            quantity = f.quantity.toInt(),
                            unitPrice = f.unitPrice,
                            totalAmount = f.totalAmount,
                            discount = f.discount,
                            paidAmount = f.paidAmount,
                            remainingAmount = f.remainingAmount,
                            paymentMethod = f.paymentMethod,
                            employeeName = f.employeeName,
                            notes = f.notes,
                            date = f.date
                        )
                    }
                    if (txList.isNotEmpty()) {
                        dao.insertTransactions(txList)
                    }
                }
            }
        activeListeners.add(txListener)

        // 5. Observe App Users / Employees & Permissions
        val userListener = userDoc(userId).collection("app_users")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val userList = snapshot.documents.mapNotNull { doc ->
                        val f = doc.toObject(FirebaseAppUser::class.java) ?: return@mapNotNull null
                        val role = try { UserRole.valueOf(f.role) } catch (_: Exception) { UserRole.SALES }
                        AppUser(
                            id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                            username = f.username,
                            displayName = f.displayName,
                            pinCode = f.pinCode,
                            role = role,
                            phone = f.phone,
                            email = f.email,
                            canViewProfits = f.canViewProfits,
                            canDeleteRecords = f.canDeleteRecords,
                            canManageUsers = f.canManageUsers,
                            canManageStock = f.canManageStock,
                            canGrantDiscount = f.canGrantDiscount,
                            canExportBackup = f.canExportBackup,
                            isActive = f.isActive,
                            canAddSales = f.canAddSales,
                            canEditSales = f.canEditSales,
                            canDeleteSales = f.canDeleteSales,
                            canAddPurchases = f.canAddPurchases,
                            canEditPurchases = f.canEditPurchases,
                            canDeletePurchases = f.canDeletePurchases,
                            canAddVouchers = f.canAddVouchers,
                            canEditVouchers = f.canEditVouchers,
                            canDeleteVouchers = f.canDeleteVouchers,
                            canAddSwaps = f.canAddSwaps,
                            canEditSwaps = f.canEditSwaps,
                            canDeleteSwaps = f.canDeleteSwaps,
                            canAddTransfers = f.canAddTransfers,
                            canEditTransfers = f.canEditTransfers,
                            canDeleteTransfers = f.canDeleteTransfers,
                            canAddReports = f.canAddReports,
                            canEditReports = f.canEditReports,
                            canDeleteReports = f.canDeleteReports,
                            canAddCards = f.canAddCards,
                            canEditCards = f.canEditCards,
                            canDeleteCards = f.canDeleteCards
                        )
                    }
                    if (userList.isNotEmpty()) {
                        dao.insertUsers(userList)
                    }
                }
            }
        activeListeners.add(userListener)

        // 6. Observe Settings
        val settingsListener = userDoc(userId).collection("settings").document("settings")
            .addSnapshotListener { doc, error ->
                if (error != null || doc == null || !doc.exists()) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val f = doc.toObject(FirebaseNetworkSettings::class.java) ?: return@launch
                    val current = dao.getSettingsDirect() ?: NetworkSettings()
                    dao.saveSettings(
                        current.copy(
                            networkName = f.networkName,
                            managerName = f.managerName,
                            managerPhone = f.managerPhone,
                            currency = f.currency,
                            cloudSyncEnabled = f.cloudSyncEnabled,
                            lastSyncTimestamp = f.lastSyncTimestamp
                        )
                    )
                }
            }
        activeListeners.add(settingsListener)

        // 7. Observe Card Swaps
        val swapListener = userDoc(userId).collection("card_swaps")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val swapList = snapshot.documents.mapNotNull { doc ->
                        val f = doc.toObject(FirebaseCardSwap::class.java) ?: return@mapNotNull null
                        CardSwapRecord(
                            id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                            swapCode = "SWAP-${f.id}",
                            distributorId = f.distributorId,
                            distributorName = f.distributorName,
                            fromCategoryId = f.fromCategoryId,
                            fromCategoryName = f.fromCategoryName,
                            fromQuantity = f.fromQuantity.toInt(),
                            fromUnitPrice = f.fromUnitPrice,
                            fromTotalValue = f.fromTotalValue,
                            toCategoryId = f.toCategoryId,
                            toCategoryName = f.toCategoryName,
                            toQuantity = f.toQuantity.toInt(),
                            toUnitPrice = f.toUnitPrice,
                            toTotalValue = f.toTotalValue,
                            differenceAmount = f.differenceAmount,
                            paidDifference = f.paidDifference,
                            remainingDifference = f.remainingDifference,
                            date = f.date,
                            employeeName = f.employeeName,
                            notes = f.notes
                        )
                    }
                    if (swapList.isNotEmpty()) {
                        dao.insertCardSwaps(swapList)
                    }
                }
            }
        activeListeners.add(swapListener)

        // 8. Observe Employee Transfers
        val transferListener = userDoc(userId).collection("employee_transfers")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val transferList = snapshot.documents.mapNotNull { doc ->
                        val f = doc.toObject(FirebaseEmployeeTransfer::class.java) ?: return@mapNotNull null
                        EmployeeTransferRecord(
                            id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                            transferCode = f.transferCode,
                            fromEmployee = f.fromEmployee,
                            toEmployee = f.toEmployee,
                            transferType = f.transferType.ifBlank { "CASH" },
                            categoryId = f.categoryId,
                            categoryName = f.categoryName,
                            quantity = f.quantity.toInt(),
                            totalFaceValue = f.totalFaceValue,
                            cashAmount = f.cashAmount,
                            date = f.date,
                            status = f.status.ifBlank { "COMPLETED" },
                            notes = f.notes
                        )
                    }
                    if (transferList.isNotEmpty()) {
                        dao.insertEmployeeTransfers(transferList)
                    }
                }
            }
        activeListeners.add(transferListener)

        // 9. Observe Fund Movements
        val fundListener = userDoc(userId).collection("fund_movements")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val movementList = snapshot.documents.mapNotNull { doc ->
                        val f = doc.toObject(FirebaseFundMovement::class.java) ?: return@mapNotNull null
                        val dir = try { MovementDirection.valueOf(f.direction) } catch (_: Exception) { MovementDirection.INFLOW }
                        FundMovement(
                            id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                            registerId = f.registerId,
                            registerName = f.registerName,
                            isEmployeeFund = f.isEmployeeFund,
                            employeeUsername = f.employeeUsername.ifBlank { null },
                            employeeDisplayName = f.employeeDisplayName.ifBlank { null },
                            direction = dir,
                            amount = f.amount,
                            balanceAfter = f.balanceAfter,
                            title = f.title,
                            referenceType = f.referenceType,
                            referenceNumber = f.referenceNumber,
                            notes = f.notes,
                            date = f.date
                        )
                    }
                    if (movementList.isNotEmpty()) {
                        dao.insertFundMovements(movementList)
                    }
                }
            }
        activeListeners.add(fundListener)

        // 10. Observe System Notifications
        val notifListener = userDoc(userId).collection("system_notifications")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                scope.launch(Dispatchers.IO) {
                    val notifList = snapshot.documents.mapNotNull { doc ->
                        val f = doc.toObject(FirebaseSystemNotification::class.java) ?: return@mapNotNull null
                        val nType = try { NotificationType.valueOf(f.type) } catch (_: Exception) { NotificationType.GENERAL }
                        SystemNotification(
                            id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                            title = f.title,
                            message = f.message,
                            type = nType,
                            targetEmployee = f.targetEmployee,
                            senderName = f.senderName,
                            date = f.date,
                            isRead = f.isRead,
                            actionUrlOrId = f.actionUrlOrId
                        )
                    }
                    if (notifList.isNotEmpty()) {
                        dao.insertNotifications(notifList)
                    }
                }
            }
        activeListeners.add(notifListener)

        Log.d("FirestoreSync", "Realtime sync listeners attached for user: $userId")
    }

    // === Two-Way Full Reconciliation (Manual Sync Trigger) ===

    suspend fun fullReconciliation(
        userId: String,
        dao: NetCardsDao,
        currentDistributors: List<Distributor>,
        currentCategories: List<CardCategory>,
        currentRegisters: List<CashRegister>,
        currentTransactions: List<TransactionRecord>,
        currentUsers: List<AppUser>,
        currentSettings: NetworkSettings?
    ): Int {
        var uploadCount = 0

        // 1. Upload local to cloud
        currentDistributors.forEach { syncDistributor(userId, it); uploadCount++ }
        currentCategories.forEach { syncCategory(userId, it); uploadCount++ }
        currentRegisters.forEach { syncRegister(userId, it); uploadCount++ }
        currentTransactions.takeLast(300).forEach { syncTransaction(userId, it); uploadCount++ }
        currentUsers.forEach { syncAppUser(userId, it); uploadCount++ }
        if (currentSettings != null) {
            syncSettings(userId, currentSettings)
        }

        // 2. Fetch cloud data and insert into Room
        val distSnap = userDoc(userId).collection("distributors").get().await()
        val distList = distSnap.documents.mapNotNull { doc ->
            val f = doc.toObject(FirebaseDistributor::class.java) ?: return@mapNotNull null
            Distributor(
                id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                name = f.name,
                phone = f.phone,
                currentBalance = f.currentBalance,
                creditLimit = f.creditLimit,
                paymentPeriodDays = f.paymentPeriodDays.toInt(),
                discountPercent = f.discountPercent,
                lastPaymentDate = f.lastPaymentDate,
                notes = f.notes,
                isActive = f.isActive
            )
        }
        if (distList.isNotEmpty()) dao.insertDistributors(distList)

        val catSnap = userDoc(userId).collection("categories").get().await()
        val catList = catSnap.documents.mapNotNull { doc ->
            val f = doc.toObject(FirebaseCardCategory::class.java) ?: return@mapNotNull null
            CardCategory(
                id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                name = f.name,
                faceValue = f.faceValue,
                costPrice = f.costPrice,
                sellingPrice = f.sellingPrice,
                stockQuantity = f.stockQuantity.toInt(),
                validityHours = f.validityHours.toInt(),
                quotaMb = f.quotaMb.toInt()
            )
        }
        if (catList.isNotEmpty()) dao.insertCategories(catList)

        val regSnap = userDoc(userId).collection("registers").get().await()
        val regList = regSnap.documents.mapNotNull { doc ->
            val f = doc.toObject(FirebaseCashRegister::class.java) ?: return@mapNotNull null
            val regType = try { FundType.valueOf(f.type) } catch (_: Exception) { FundType.MAIN_TREASURY }
            CashRegister(
                id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                name = f.name,
                type = regType,
                currentBalance = f.currentBalance,
                openingBalance = f.openingBalance,
                totalInflows = f.totalInflows,
                totalOutflows = f.totalOutflows,
                employeeUsername = f.employeeUsername.ifBlank { null },
                employeeDisplayName = f.employeeDisplayName.ifBlank { null }
            )
        }
        if (regList.isNotEmpty()) dao.insertCashRegisters(regList)

        val txSnap = userDoc(userId).collection("transactions").get().await()
        val txList = txSnap.documents.mapNotNull { doc ->
            val f = doc.toObject(FirebaseTransactionRecord::class.java) ?: return@mapNotNull null
            val tType = try { TransactionType.valueOf(f.type) } catch (_: Exception) { TransactionType.SALE }
            TransactionRecord(
                id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                invoiceNumber = f.invoiceNumber,
                type = tType,
                distributorId = f.distributorId,
                distributorName = f.distributorName,
                categoryId = f.categoryId,
                categoryName = f.categoryName,
                quantity = f.quantity.toInt(),
                unitPrice = f.unitPrice,
                totalAmount = f.totalAmount,
                discount = f.discount,
                paidAmount = f.paidAmount,
                remainingAmount = f.remainingAmount,
                paymentMethod = f.paymentMethod,
                employeeName = f.employeeName,
                notes = f.notes,
                date = f.date
            )
        }
        if (txList.isNotEmpty()) dao.insertTransactions(txList)

        val userSnap = userDoc(userId).collection("app_users").get().await()
        val appUserList = userSnap.documents.mapNotNull { doc ->
            val f = doc.toObject(FirebaseAppUser::class.java) ?: return@mapNotNull null
            val role = try { UserRole.valueOf(f.role) } catch (_: Exception) { UserRole.SALES }
            AppUser(
                id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                username = f.username,
                displayName = f.displayName,
                pinCode = f.pinCode,
                role = role,
                phone = f.phone,
                email = f.email,
                canViewProfits = f.canViewProfits,
                canDeleteRecords = f.canDeleteRecords,
                canManageUsers = f.canManageUsers,
                canManageStock = f.canManageStock,
                canGrantDiscount = f.canGrantDiscount,
                canExportBackup = f.canExportBackup,
                isActive = f.isActive,
                canAddSales = f.canAddSales,
                canEditSales = f.canEditSales,
                canDeleteSales = f.canDeleteSales,
                canAddPurchases = f.canAddPurchases,
                canEditPurchases = f.canEditPurchases,
                canDeletePurchases = f.canDeletePurchases,
                canAddVouchers = f.canAddVouchers,
                canEditVouchers = f.canEditVouchers,
                canDeleteVouchers = f.canDeleteVouchers,
                canAddSwaps = f.canAddSwaps,
                canEditSwaps = f.canEditSwaps,
                canDeleteSwaps = f.canDeleteSwaps,
                canAddTransfers = f.canAddTransfers,
                canEditTransfers = f.canEditTransfers,
                canDeleteTransfers = f.canDeleteTransfers,
                canAddReports = f.canAddReports,
                canEditReports = f.canEditReports,
                canDeleteReports = f.canDeleteReports,
                canAddCards = f.canAddCards,
                canEditCards = f.canEditCards,
                canDeleteCards = f.canDeleteCards
            )
        }
        if (appUserList.isNotEmpty()) dao.insertUsers(appUserList)

        val swapSnap = userDoc(userId).collection("card_swaps").get().await()
        val swapList = swapSnap.documents.mapNotNull { doc ->
            val f = doc.toObject(FirebaseCardSwap::class.java) ?: return@mapNotNull null
            CardSwapRecord(
                id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                swapCode = "SWAP-${f.id}",
                distributorId = f.distributorId,
                distributorName = f.distributorName,
                fromCategoryId = f.fromCategoryId,
                fromCategoryName = f.fromCategoryName,
                fromQuantity = f.fromQuantity.toInt(),
                fromUnitPrice = f.fromUnitPrice,
                fromTotalValue = f.fromTotalValue,
                toCategoryId = f.toCategoryId,
                toCategoryName = f.toCategoryName,
                toQuantity = f.toQuantity.toInt(),
                toUnitPrice = f.toUnitPrice,
                toTotalValue = f.toTotalValue,
                differenceAmount = f.differenceAmount,
                paidDifference = f.paidDifference,
                remainingDifference = f.remainingDifference,
                date = f.date,
                employeeName = f.employeeName,
                notes = f.notes
            )
        }
        if (swapList.isNotEmpty()) dao.insertCardSwaps(swapList)

        val transferSnap = userDoc(userId).collection("employee_transfers").get().await()
        val transferList = transferSnap.documents.mapNotNull { doc ->
            val f = doc.toObject(FirebaseEmployeeTransfer::class.java) ?: return@mapNotNull null
            EmployeeTransferRecord(
                id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                transferCode = f.transferCode,
                fromEmployee = f.fromEmployee,
                toEmployee = f.toEmployee,
                transferType = f.transferType.ifBlank { "CASH" },
                categoryId = f.categoryId,
                categoryName = f.categoryName,
                quantity = f.quantity.toInt(),
                totalFaceValue = f.totalFaceValue,
                cashAmount = f.cashAmount,
                date = f.date,
                status = f.status.ifBlank { "COMPLETED" },
                notes = f.notes
            )
        }
        if (transferList.isNotEmpty()) dao.insertEmployeeTransfers(transferList)

        val fundSnap = userDoc(userId).collection("fund_movements").get().await()
        val fundList = fundSnap.documents.mapNotNull { doc ->
            val f = doc.toObject(FirebaseFundMovement::class.java) ?: return@mapNotNull null
            val dir = try { MovementDirection.valueOf(f.direction) } catch (_: Exception) { MovementDirection.INFLOW }
            FundMovement(
                id = f.id.toLongOrNull() ?: doc.id.hashCode().toLong(),
                registerId = f.registerId,
                registerName = f.registerName,
                isEmployeeFund = f.isEmployeeFund,
                employeeUsername = f.employeeUsername.ifBlank { null },
                employeeDisplayName = f.employeeDisplayName.ifBlank { null },
                direction = dir,
                amount = f.amount,
                balanceAfter = f.balanceAfter,
                title = f.title,
                referenceType = f.referenceType,
                referenceNumber = f.referenceNumber,
                notes = f.notes,
                date = f.date
            )
        }
        if (fundList.isNotEmpty()) dao.insertFundMovements(fundList)

        return uploadCount
    }
}
