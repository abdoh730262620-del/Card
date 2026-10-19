package com.example.data.local

import com.example.data.model.AppUser
import com.example.data.model.CardCategory
import com.example.data.model.CashRegister
import com.example.data.model.Distributor
import com.example.data.model.FundMovement
import com.example.data.model.FundType
import com.example.data.model.MovementDirection
import com.example.data.model.NetworkSettings
import com.example.data.model.UserRole

object InitialDataSeeder {

    suspend fun seedDatabase(dao: NetCardsDao) {
        // 1. Default Settings
        val settings = NetworkSettings(
            id = 1,
            networkName = "شبكة كروت نت",
            managerName = "المدير العام",
            managerPhone = "",
            currency = "ريال",
            requirePinOnLaunch = false,
            biometricEnabled = false,
            cloudSyncEnabled = true,
            lowStockThreshold = 20,
            language = "ar"
        )
        dao.saveSettings(settings)

        // 2. Default Admin User (Essential so app has a login/user)
        val adminUser = AppUser(
            username = "admin",
            displayName = "المدير العام",
            pinCode = "1234",
            role = UserRole.ADMIN,
            phone = "",
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
        dao.insertUser(adminUser)

        // 3. Official Main Treasury & Admin Fund
        val mainTreasury = CashRegister(
            id = 1,
            name = "صندوق البرنامج الرئيسي",
            type = FundType.MAIN_TREASURY,
            employeeUsername = null,
            employeeDisplayName = null,
            openingBalance = 0.0,
            currentBalance = 0.0,
            totalInflows = 0.0,
            totalOutflows = 0.0,
            notes = "الصندوق المالي الرسمي للبرنامج",
            createdAt = System.currentTimeMillis()
        )

        dao.insertCashRegisters(listOf(mainTreasury))
    }
}
