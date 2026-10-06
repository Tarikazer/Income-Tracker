package com.example

import com.example.util.AppConstants
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testRecentItemIsEditable() {
    val now = System.currentTimeMillis()
    // Within 24 hours
    assertTrue(AppConstants.isExpenseEditable(now, now))
    assertTrue(AppConstants.isExpenseEditable(now - 1000L * 3600 * 2, now))
    assertTrue(AppConstants.isExpenseEditable(now - 1000L * 3600 * 23, now))
  }

  @Test
  fun testBackdatedItemOver24hIsLocked() {
    val now = System.currentTimeMillis()
    // Backdated by 25 hours
    assertFalse(AppConstants.isExpenseEditable(now - 1000L * 3600 * 25, now))
    // Backdated by 3 days
    assertFalse(AppConstants.isExpenseEditable(now - 1000L * 3600 * 72, now))
  }

  @Test
  fun testLocalizationEnglishAndFrench() {
    val enStrings = com.example.ui.util.AppStrings(com.example.ui.util.AppLanguage.ENGLISH)
    val frStrings = com.example.ui.util.AppStrings(com.example.ui.util.AppLanguage.FRENCH)

    assertEquals("Settings", enStrings.settingsTitle)
    assertEquals("Paramètres", frStrings.settingsTitle)

    assertEquals("Dark Mode", enStrings.themeDark)
    assertEquals("Mode Sombre", frStrings.themeDark)

    assertEquals("Light Mode", enStrings.themeLight)
    assertEquals("Mode Clair", frStrings.themeLight)

    assertEquals("Monthly Income", enStrings.monthlyIncome)
    assertEquals("Revenu Mensuel", frStrings.monthlyIncome)

    assertEquals("Other Incomes", enStrings.otherIncomes)
    assertEquals("Autres Revenus", frStrings.otherIncomes)

    assertEquals("Created by Frost Dev", enStrings.createdBy)
    assertEquals("Créé par Frost Dev", frStrings.createdBy)
    assertEquals("+212693780909", enStrings.developerPhone)
  }
}
