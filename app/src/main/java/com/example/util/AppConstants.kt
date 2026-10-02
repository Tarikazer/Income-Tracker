package com.example.util

object AppConstants {
    const val EDIT_WINDOW_MINUTES = 10L
    const val RENT_EDIT_WINDOW_HOURS = 24L

    /**
     * Check if a purchase is within its 10-minute edit window
     */
    fun isExpenseEditable(creationTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Boolean {
        val diffMs = currentTimestamp - creationTimestamp
        return diffMs in 0..(EDIT_WINDOW_MINUTES * 60 * 1000L)
    }

    /**
     * Check if rent price is within its 24-hour edit window
     */
    fun isRentEditable(lastUpdatedTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Boolean {
        if (lastUpdatedTimestamp <= 0L) return true
        val diffMs = currentTimestamp - lastUpdatedTimestamp
        return diffMs in 0..(RENT_EDIT_WINDOW_HOURS * 3600 * 1000L)
    }

    /**
     * Get remaining minutes for purchase editing
     */
    fun remainingExpenseMinutes(creationTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Long {
        val diffMs = currentTimestamp - creationTimestamp
        val remainingMs = (EDIT_WINDOW_MINUTES * 60 * 1000L) - diffMs
        return ((remainingMs + 59999) / (60 * 1000L)).coerceAtLeast(0L)
    }

    /**
     * Get remaining hours for rent editing
     */
    fun remainingRentHours(lastUpdatedTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): Long {
        if (lastUpdatedTimestamp <= 0L) return RENT_EDIT_WINDOW_HOURS
        val diffMs = currentTimestamp - lastUpdatedTimestamp
        val remainingMs = (RENT_EDIT_WINDOW_HOURS * 3600 * 1000L) - diffMs
        return ((remainingMs + 3599999) / (3600 * 1000L)).coerceAtLeast(0L)
    }
}
