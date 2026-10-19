package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val titleAr: String) {
    ADMIN("مدير النظام"),
    ACCOUNTANT("محاسب عام"),
    SALES("موظف مبيعات"),
    SUPERVISOR("مشرف شبكة")
}

@Entity(tableName = "app_users")
data class AppUser(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val displayName: String,
    val pinCode: String = "1234",
    val role: UserRole = UserRole.SALES,
    val phone: String = "",
    val email: String = "",
    val canViewProfits: Boolean = false,
    val canDeleteRecords: Boolean = false,
    val canManageUsers: Boolean = false,
    val canManageStock: Boolean = true,
    val canGrantDiscount: Boolean = false,
    val canExportBackup: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),

    // --- صلاحيات المبيعات ---
    val canAddSales: Boolean = true,
    val canEditSales: Boolean = false,
    val canDeleteSales: Boolean = false,

    // --- صلاحيات المشتريات ---
    val canAddPurchases: Boolean = true,
    val canEditPurchases: Boolean = false,
    val canDeletePurchases: Boolean = false,

    // --- صلاحيات السندات ---
    val canAddVouchers: Boolean = true,
    val canEditVouchers: Boolean = false,
    val canDeleteVouchers: Boolean = false,

    // --- صلاحيات الاستبدال ---
    val canAddSwaps: Boolean = true,
    val canEditSwaps: Boolean = false,
    val canDeleteSwaps: Boolean = false,

    // --- صلاحيات التحويلات ---
    val canAddTransfers: Boolean = true,
    val canEditTransfers: Boolean = false,
    val canDeleteTransfers: Boolean = false,

    // --- صلاحيات التقارير ---
    val canAddReports: Boolean = true,
    val canEditReports: Boolean = false,
    val canDeleteReports: Boolean = false,

    // --- صلاحيات الكروت ---
    val canAddCards: Boolean = true,
    val canEditCards: Boolean = false,
    val canDeleteCards: Boolean = false,

    // --- صلاحيات المصروفات والصناديق ---
    val canManageExpenses: Boolean = false
)
