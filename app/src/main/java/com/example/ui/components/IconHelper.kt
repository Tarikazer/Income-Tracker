package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    fun getCategoryIcon(iconKey: String): ImageVector {
        return when (iconKey.lowercase()) {
            "rent" -> Icons.Rounded.Home
            "sport" -> Icons.Rounded.FitnessCenter
            "alimentation", "food", "groceries" -> Icons.Rounded.ShoppingCart
            "water" -> Icons.Rounded.WaterDrop
            "electricity", "power" -> Icons.Rounded.Bolt
            "internet", "wifi" -> Icons.Rounded.Wifi
            "family" -> Icons.Rounded.Groups
            "transport", "car", "taxi" -> Icons.Rounded.DirectionsCar
            "health", "medical" -> Icons.Rounded.LocalHospital
            "restaurant", "fastfood" -> Icons.Rounded.Fastfood
            "coffee" -> Icons.Rounded.LocalCafe
            else -> Icons.Rounded.Category
        }
    }
}
