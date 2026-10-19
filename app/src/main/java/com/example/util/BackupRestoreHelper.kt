package com.example.util

import android.content.Context
import com.example.data.local.NetCardsDao
import com.example.data.model.AppUser
import com.example.data.model.CardCategory
import com.example.data.model.CardSwapRecord
import com.example.data.model.Distributor
import com.example.data.model.EmployeeTransferRecord
import com.example.data.model.NetworkSettings
import com.example.data.model.TransactionRecord
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupRestoreHelper {

    suspend fun exportBackupToJson(dao: NetCardsDao): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

        // Categories
        val categories = dao.getAllCategories().first()
        val catArray = JSONArray()
        for (c in categories) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("faceValue", c.faceValue)
            obj.put("costPrice", c.costPrice)
            obj.put("sellingPrice", c.sellingPrice)
            obj.put("stockQuantity", c.stockQuantity)
            obj.put("validityHours", c.validityHours)
            obj.put("quotaMb", c.quotaMb)
            obj.put("colorHex", c.colorHex)
            obj.put("notes", c.notes)
            catArray.put(obj)
        }
        root.put("categories", catArray)

        // Distributors
        val distributors = dao.getAllDistributors().first()
        val distArray = JSONArray()
        for (d in distributors) {
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("name", d.name)
            obj.put("phone", d.phone)
            obj.put("openingBalance", d.openingBalance)
            obj.put("currentBalance", d.currentBalance)
            obj.put("creditLimit", d.creditLimit)
            obj.put("paymentPeriodDays", d.paymentPeriodDays)
            obj.put("discountPercent", d.discountPercent)
            obj.put("lastPaymentDate", d.lastPaymentDate)
            obj.put("notes", d.notes)
            distArray.put(obj)
        }
        root.put("distributors", distArray)

        // Transactions
        val transactions = dao.getAllTransactions().first()
        val txArray = JSONArray()
        for (t in transactions) {
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("invoiceNumber", t.invoiceNumber)
            obj.put("type", t.type.name)
            obj.put("distributorId", t.distributorId ?: JSONObject.NULL)
            obj.put("distributorName", t.distributorName)
            obj.put("categoryId", t.categoryId ?: JSONObject.NULL)
            obj.put("categoryName", t.categoryName)
            obj.put("quantity", t.quantity)
            obj.put("unitPrice", t.unitPrice)
            obj.put("totalAmount", t.totalAmount)
            obj.put("discount", t.discount)
            obj.put("paidAmount", t.paidAmount)
            obj.put("remainingAmount", t.remainingAmount)
            obj.put("paymentMethod", t.paymentMethod)
            obj.put("date", t.date)
            obj.put("employeeName", t.employeeName)
            obj.put("notes", t.notes)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // Swaps
        val swaps = dao.getAllCardSwaps().first()
        val swapArray = JSONArray()
        for (s in swaps) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("swapCode", s.swapCode)
            obj.put("distributorId", s.distributorId)
            obj.put("distributorName", s.distributorName)
            obj.put("fromCategoryName", s.fromCategoryName)
            obj.put("fromQuantity", s.fromQuantity)
            obj.put("toCategoryName", s.toCategoryName)
            obj.put("toQuantity", s.toQuantity)
            obj.put("differenceAmount", s.differenceAmount)
            obj.put("date", s.date)
            swapArray.put(obj)
        }
        root.put("cardSwaps", swapArray)

        return root.toString(2)
    }

    suspend fun saveBackupToFile(context: Context, json: String): File {
        val fileName = "NetCards_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.json"
        val file = File(context.filesDir, fileName)
        file.writeText(json)
        return file
    }
}
