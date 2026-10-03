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
}
