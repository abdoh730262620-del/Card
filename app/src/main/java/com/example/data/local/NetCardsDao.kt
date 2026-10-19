package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AppUser
import com.example.data.model.CardCategory
import com.example.data.model.CardSwapRecord
import com.example.data.model.Distributor
import com.example.data.model.EmployeeTransferRecord
import com.example.data.model.NetworkSettings
import com.example.data.model.SystemNotification
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface NetCardsDao {

    // === Card Categories & Stock ===
    @Query("SELECT * FROM card_categories ORDER BY faceValue ASC")
    fun getAllCategories(): Flow<List<CardCategory>>

    @Query("SELECT * FROM card_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): CardCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CardCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CardCategory>)

    @Update
    suspend fun updateCategory(category: CardCategory)

    @Delete
    suspend fun deleteCategory(category: CardCategory)

    @Query("UPDATE card_categories SET stockQuantity = stockQuantity + :diff WHERE id = :categoryId")
    suspend fun adjustCategoryStock(categoryId: Long, diff: Int)

    // === Distributors ===
    @Query("SELECT * FROM distributors ORDER BY name ASC")
    fun getAllDistributors(): Flow<List<Distributor>>

    @Query("SELECT * FROM distributors WHERE id = :id")
    suspend fun getDistributorById(id: Long): Distributor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistributor(distributor: Distributor): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDistributors(distributors: List<Distributor>)

    @Update
    suspend fun updateDistributor(distributor: Distributor)

    @Delete
    suspend fun deleteDistributor(distributor: Distributor)

    @Query("UPDATE distributors SET currentBalance = currentBalance + :balanceDelta WHERE id = :distributorId")
    suspend fun adjustDistributorBalance(distributorId: Long, balanceDelta: Double)

    @Query("UPDATE distributors SET currentBalance = currentBalance + :balanceDelta, lastPaymentDate = :paymentDate WHERE id = :distributorId")
    suspend fun recordDistributorPayment(distributorId: Long, balanceDelta: Double, paymentDate: Long)

    // === Transactions & Vouchers ===
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE distributorId = :distributorId ORDER BY date DESC")
    fun getTransactionsByDistributor(distributorId: Long): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<TransactionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionRecord>)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionRecord)

    @Update
    suspend fun updateTransaction(transaction: TransactionRecord)

    // === Transaction Items ===
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getTransactionItems(transactionId: Long): List<TransactionItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItem>)

    // === Card Swaps ===
    @Query("SELECT * FROM card_swaps ORDER BY date DESC")
    fun getAllCardSwaps(): Flow<List<CardSwapRecord>>

    @Query("SELECT * FROM card_swaps WHERE distributorId = :distributorId ORDER BY date DESC")
    fun getCardSwapsByDistributor(distributorId: Long): Flow<List<CardSwapRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardSwap(swap: CardSwapRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardSwaps(swaps: List<CardSwapRecord>)

    @Delete
    suspend fun deleteCardSwap(swap: CardSwapRecord)

    // === Employee Transfers ===
    @Query("SELECT * FROM employee_transfers ORDER BY date DESC")
    fun getAllEmployeeTransfers(): Flow<List<EmployeeTransferRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployeeTransfer(transfer: EmployeeTransferRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployeeTransfers(transfers: List<EmployeeTransferRecord>)

    @Delete
    suspend fun deleteEmployeeTransfer(transfer: EmployeeTransferRecord)

    // === App Users & Permissions ===
    @Query("SELECT * FROM app_users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<AppUser>>

    @Query("SELECT * FROM app_users WHERE id = :id")
    suspend fun getUserById(id: Long): AppUser?

    @Query("SELECT * FROM app_users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): AppUser?

    @Query("SELECT * FROM app_users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): AppUser?

    @Query("SELECT * FROM app_users ORDER BY id ASC")
    suspend fun getAllUsersDirect(): List<AppUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUser): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<AppUser>)

    @Update
    suspend fun updateUser(user: AppUser)

    @Delete
    suspend fun deleteUser(user: AppUser)

    // === System Notifications ===
    @Query("SELECT * FROM system_notifications ORDER BY date DESC")
    fun getAllNotifications(): Flow<List<SystemNotification>>

    @Query("SELECT COUNT(*) FROM system_notifications WHERE isRead = 0")
    fun getUnreadNotificationsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SystemNotification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<SystemNotification>)

    @Query("UPDATE system_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE system_notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    @Query("DELETE FROM system_notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long)

    @Query("DELETE FROM system_notifications")
    suspend fun clearAllNotifications()

    // === Settings ===
    @Query("SELECT * FROM network_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<NetworkSettings?>

    @Query("SELECT * FROM network_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): NetworkSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: NetworkSettings)

    // === Cash Registers (Main Treasury vs Independent Employee Funds) ===
    @Query("SELECT * FROM cash_registers ORDER BY type ASC, name ASC")
    fun getAllCashRegisters(): Flow<List<com.example.data.model.CashRegister>>

    @Query("SELECT * FROM cash_registers WHERE type = 'MAIN_TREASURY' LIMIT 1")
    fun getMainTreasury(): Flow<com.example.data.model.CashRegister?>

    @Query("SELECT * FROM cash_registers WHERE type = 'MAIN_TREASURY' LIMIT 1")
    suspend fun getMainTreasuryDirect(): com.example.data.model.CashRegister?

    @Query("SELECT * FROM cash_registers WHERE type = 'EMPLOYEE_FUND' ORDER BY name ASC")
    fun getEmployeeFunds(): Flow<List<com.example.data.model.CashRegister>>

    @Query("SELECT * FROM cash_registers WHERE employeeUsername = :username LIMIT 1")
    fun getEmployeeFundByUsername(username: String): Flow<com.example.data.model.CashRegister?>

    @Query("SELECT * FROM cash_registers WHERE employeeUsername = :username LIMIT 1")
    suspend fun getEmployeeFundByUsernameDirect(username: String): com.example.data.model.CashRegister?

    @Query("SELECT * FROM cash_registers WHERE id = :id")
    suspend fun getCashRegisterById(id: Long): com.example.data.model.CashRegister?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashRegister(register: com.example.data.model.CashRegister): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashRegisters(registers: List<com.example.data.model.CashRegister>)

    @Update
    suspend fun updateCashRegister(register: com.example.data.model.CashRegister)

    @Query("UPDATE cash_registers SET currentBalance = currentBalance + :amount, totalInflows = totalInflows + :inflowDelta, totalOutflows = totalOutflows + :outflowDelta WHERE id = :id")
    suspend fun adjustRegisterBalance(id: Long, amount: Double, inflowDelta: Double, outflowDelta: Double)

    // === Fund Movements (Isolated Ledgers) ===
    @Query("SELECT * FROM fund_movements ORDER BY date DESC")
    fun getAllFundMovements(): Flow<List<com.example.data.model.FundMovement>>

    @Query("SELECT * FROM fund_movements WHERE registerId = :registerId ORDER BY date DESC")
    fun getMovementsByRegister(registerId: Long): Flow<List<com.example.data.model.FundMovement>>

    @Query("SELECT * FROM fund_movements WHERE isEmployeeFund = 0 ORDER BY date DESC")
    fun getMainTreasuryMovements(): Flow<List<com.example.data.model.FundMovement>>

    @Query("SELECT * FROM fund_movements WHERE employeeUsername = :username ORDER BY date DESC")
    fun getEmployeeFundMovements(username: String): Flow<List<com.example.data.model.FundMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFundMovement(movement: com.example.data.model.FundMovement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFundMovements(movements: List<com.example.data.model.FundMovement>)

    // === Employee Warehouse Stocks ===
    @Query("SELECT * FROM employee_warehouse_stocks ORDER BY employeeName ASC, categoryName ASC")
    fun getAllEmployeeStocks(): Flow<List<com.example.data.model.EmployeeStock>>

    @Query("SELECT * FROM employee_warehouse_stocks WHERE employeeName = :employeeName AND categoryId = :categoryId LIMIT 1")
    suspend fun getEmployeeStock(employeeName: String, categoryId: Long): com.example.data.model.EmployeeStock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployeeStock(stock: com.example.data.model.EmployeeStock): Long

    @Update
    suspend fun updateEmployeeStock(stock: com.example.data.model.EmployeeStock)

    @Query("UPDATE employee_warehouse_stocks SET quantity = quantity + :delta WHERE employeeName = :employeeName AND categoryId = :categoryId")
    suspend fun adjustEmployeeStockQuantity(employeeName: String, categoryId: Long, delta: Int)
}
