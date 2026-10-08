package com.example.util

object AppConstants {
    const val EDIT_WINDOW_HOURS = 24L

    /**
     * Check if any item/purchase/transaction is within its 24-hour edit/delete window.
     * Applies universally to all categories and purchases.
     */
    fun isItemEditable(creationTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Boolean {
        if (creationTimestamp <= 0L) return true
        val diffMs = currentTimestamp - creationTimestamp
        return diffMs in 0..(EDIT_WINDOW_HOURS * 3600 * 1000L)
    }

    /**
     * Alias for isItemEditable to support existing call sites.
     */
    fun isExpenseEditable(creationTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Boolean {
        return isItemEditable(creationTimestamp, currentTimestamp)
    }

    /**
     * Get remaining hours for editing or deleting
     */
    fun remainingHours(creationTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Long {
        if (creationTimestamp <= 0L) return EDIT_WINDOW_HOURS
        val diffMs = currentTimestamp - creationTimestamp
        val remainingMs = (EDIT_WINDOW_HOURS * 3600 * 1000L) - diffMs
        return ((remainingMs + 3599999) / (3600 * 1000L)).coerceAtLeast(0L)
    }

    fun remainingExpenseMinutes(creationTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Long {
        return remainingHours(creationTimestamp, currentTimestamp) * 60
    }

    /**
     * Add [months] to a "yyyy-MM" string.
     */
    fun addMonths(monthYear: String, months: Int): String {
        return try {
            val parts = monthYear.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt() // 1-based
            val cal = java.util.Calendar.getInstance()
            cal.set(java.util.Calendar.YEAR, year)
            cal.set(java.util.Calendar.MONTH, month - 1)
            cal.add(java.util.Calendar.MONTH, months)
            java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US).format(cal.time)
        } catch (e: Exception) {
            monthYear
        }
    }

    /**
     * Format "yyyy-MM" into a localized month name and year, e.g. "Dec 2026" or "déc. 2026".
     */
    fun formatMonthYear(monthYear: String, isFrench: Boolean): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
            val date = sdf.parse(monthYear) ?: return monthYear
            val locale = if (isFrench) java.util.Locale.FRENCH else java.util.Locale.ENGLISH
            java.text.SimpleDateFormat("MMM yyyy", locale).format(date)
        } catch (e: Exception) {
            monthYear
        }
    }
}
