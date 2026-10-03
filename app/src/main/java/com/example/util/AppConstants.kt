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
}
