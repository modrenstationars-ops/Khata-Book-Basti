package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class KhataLedgerLogicTest {

    @Test
    fun testCashOutCalculationRules() {
        val initialCashDrawer = 25000.0
        val initialJazzCash = 10000.0

        // Transaction 1: Customer Cash Out of Rs. 5,000 via JazzCash with Rs. 50 Fee
        // User rule: "cash out ka aik button hu us me value negative dalo"
        val cashImpact = -5000.0
        val accountImpact = +5000.0
        val fee = 50.0

        val endingJazzCash = initialJazzCash + accountImpact
        val endingCashDrawer = initialCashDrawer + cashImpact + fee

        assertEquals(15000.0, endingJazzCash, 0.01)
        assertEquals(20050.0, endingCashDrawer, 0.01)
    }

    @Test
    fun testCashInCalculationRules() {
        val initialCashDrawer = 20050.0
        val initialEasyPaisa = 12000.0

        // Transaction 2: Customer Cash In (Send) of Rs. 3,000 via EasyPaisa with Rs. 40 Fee
        // User rule: "r agr acount me se kisi aik account se paise send hun tu cash out/in me positive value dalon"
        val cashImpact = +3000.0
        val accountImpact = -3000.0
        val fee = 40.0

        val endingEasyPaisa = initialEasyPaisa + accountImpact
        val endingCashDrawer = initialCashDrawer + cashImpact + fee

        assertEquals(9000.0, endingEasyPaisa, 0.01)
        assertEquals(23090.0, endingCashDrawer, 0.01)
    }
}
