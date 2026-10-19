package com.example.data.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirebaseUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "ADMIN",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirebaseDistributor(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val phone: String = "",
    val currentBalance: Double = 0.0,
    val creditLimit: Double = 50000.0,
    val paymentPeriodDays: Long = 14L,
    val discountPercent: Double = 0.0,
    val lastPaymentDate: Long = 0L,
    val notes: String = "",
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirebaseCardCategory(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val faceValue: Double = 0.0,
    val costPrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val stockQuantity: Long = 0L,
    val validityHours: Long = 24L,
    val quotaMb: Long = 1024L
)

@IgnoreExtraProperties
data class FirebaseCashRegister(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val type: String = "MAIN_TREASURY",
    val currentBalance: Double = 0.0,
    val openingBalance: Double = 0.0,
    val totalInflows: Double = 0.0,
    val totalOutflows: Double = 0.0,
    val employeeUsername: String = "",
    val employeeDisplayName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirebaseTransactionRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val invoiceNumber: String = "",
    val type: String = "SALE",
    val distributorId: Long? = null,
    val distributorName: String = "",
    val categoryId: Long? = null,
    val categoryName: String = "",
    val quantity: Long = 0L,
    val unitPrice: Double = 0.0,
    val totalAmount: Double = 0.0,
    val discount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val remainingAmount: Double = 0.0,
    val paymentMethod: String = "CASH",
    val employeeName: String = "",
    val notes: String = "",
    val date: Long = 0L
)

@IgnoreExtraProperties
data class FirebaseAppUser(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val pinCode: String = "",
    val role: String = "SALES",
    val phone: String = "",
    val email: String = "",
    val canViewProfits: Boolean = false,
    val canDeleteRecords: Boolean = false,
    val canManageUsers: Boolean = false,
    val canManageStock: Boolean = false,
    val canGrantDiscount: Boolean = false,
    val canExportBackup: Boolean = false,
    val isActive: Boolean = true,
    val canAddSales: Boolean = true,
    val canEditSales: Boolean = false,
    val canDeleteSales: Boolean = false,
    val canAddPurchases: Boolean = true,
    val canEditPurchases: Boolean = false,
    val canDeletePurchases: Boolean = false,
    val canAddVouchers: Boolean = true,
    val canEditVouchers: Boolean = false,
    val canDeleteVouchers: Boolean = false,
    val canAddSwaps: Boolean = true,
    val canEditSwaps: Boolean = false,
    val canDeleteSwaps: Boolean = false,
    val canAddTransfers: Boolean = true,
    val canEditTransfers: Boolean = false,
    val canDeleteTransfers: Boolean = false,
    val canAddReports: Boolean = true,
    val canEditReports: Boolean = false,
    val canDeleteReports: Boolean = false,
    val canAddCards: Boolean = true,
    val canEditCards: Boolean = false,
    val canDeleteCards: Boolean = false
)

@IgnoreExtraProperties
data class FirebaseEmployeeInvite(
    @DocumentId val email: String = "",
    val managerUid: String = "",
    val managerEmail: String = "",
    val managerName: String = "",
    val employeeUsername: String = "",
    val employeeDisplayName: String = "",
    val role: String = "SALES",
    val pinCode: String = "1234",
    val updatedAt: Long = 0L
)

@IgnoreExtraProperties
data class FirebaseCardSwap(
    @DocumentId val id: String = "",
    val userId: String = "",
    val distributorId: Long = 0L,
    val distributorName: String = "",
    val fromCategoryId: Long = 0L,
    val fromCategoryName: String = "",
    val fromQuantity: Long = 0L,
    val fromUnitPrice: Double = 0.0,
    val fromTotalValue: Double = 0.0,
    val toCategoryId: Long = 0L,
    val toCategoryName: String = "",
    val toQuantity: Long = 0L,
    val toUnitPrice: Double = 0.0,
    val toTotalValue: Double = 0.0,
    val differenceAmount: Double = 0.0,
    val paidDifference: Double = 0.0,
    val remainingDifference: Double = 0.0,
    val date: Long = 0L,
    val employeeName: String = "",
    val notes: String = ""
)

@IgnoreExtraProperties
data class FirebaseEmployeeTransfer(
    @DocumentId val id: String = "",
    val userId: String = "",
    val transferCode: String = "",
    val fromEmployee: String = "",
    val toEmployee: String = "",
    val transferType: String = "CASH",
    val categoryId: Long? = null,
    val categoryName: String = "",
    val quantity: Long = 0L,
    val totalFaceValue: Double = 0.0,
    val cashAmount: Double = 0.0,
    val date: Long = 0L,
    val status: String = "COMPLETED",
    val notes: String = ""
)

@IgnoreExtraProperties
data class FirebaseFundMovement(
    @DocumentId val id: String = "",
    val userId: String = "",
    val registerId: Long = 0L,
    val registerName: String = "",
    val isEmployeeFund: Boolean = false,
    val employeeUsername: String = "",
    val employeeDisplayName: String = "",
    val direction: String = "INFLOW",
    val amount: Double = 0.0,
    val balanceAfter: Double = 0.0,
    val title: String = "",
    val referenceType: String = "",
    val referenceNumber: String = "",
    val notes: String = "",
    val date: Long = 0L
)

@IgnoreExtraProperties
data class FirebaseNetworkSettings(
    @DocumentId val id: String = "settings",
    val userId: String = "",
    val networkName: String = "شبكة كروت نت",
    val managerName: String = "المدير العام",
    val managerPhone: String = "777000000",
    val currency: String = "ريال",
    val requirePinOnLaunch: Boolean = false,
    val biometricEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L
)

@IgnoreExtraProperties
data class FirebaseSystemNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "GENERAL",
    val targetEmployee: String = "ALL",
    val senderName: String = "",
    val date: Long = 0L,
    val isRead: Boolean = false,
    val actionUrlOrId: String? = null
)
