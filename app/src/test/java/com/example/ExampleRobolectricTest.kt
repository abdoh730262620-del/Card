package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CashRegister
import com.example.data.model.Distributor
import com.example.data.model.FundType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("إدارة حسابات الكروت", appName)
    }

    @Test
    fun `test distributor discount percent calculation`() {
        val distributor = Distributor(
            name = "موزع الأمل",
            phone = "770000000",
            discountPercent = 10.0
        )
        val originalPrice = 1000.0
        val discountedPrice = originalPrice * (1.0 - (distributor.discountPercent / 100.0))
        assertEquals(900.0, discountedPrice, 0.001)
    }

    @Test
    fun `test distributor credit limit check`() {
        val distributor = Distributor(
            name = "مركز النور",
            phone = "771234567",
            openingBalance = 10000.0,
            currentBalance = 75000.0,
            creditLimit = 70000.0,
            paymentPeriodDays = 14
        )
        assertTrue(distributor.isCreditLimitExceeded)
    }

    @Test
    fun `test employee fund isolation from main treasury`() {
        val mainFund = CashRegister(
            id = 1,
            name = "صندوق البرنامج الرئيسي",
            type = FundType.MAIN_TREASURY,
            currentBalance = 200000.0
        )
        val employeeFund = CashRegister(
            id = 2,
            name = "صندوق الموظف أحمد",
            type = FundType.EMPLOYEE_FUND,
            employeeUsername = "sales1",
            currentBalance = 15000.0
        )

        // The employee fund has its own isolated balance and type
        assertNotEquals(mainFund.type, employeeFund.type)
        assertEquals(FundType.MAIN_TREASURY, mainFund.type)
        assertEquals(FundType.EMPLOYEE_FUND, employeeFund.type)
        assertEquals(15000.0, employeeFund.currentBalance, 0.001)
        assertEquals(200000.0, mainFund.currentBalance, 0.001)
    }
}
