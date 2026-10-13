package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

object IconHelper {
    fun getCategoryIcon(iconKey: String): ImageVector {
        return when (iconKey.lowercase(Locale.ROOT)) {
            "food", "groceries", "alimentation" -> Icons.Rounded.ShoppingCart
            "restaurant", "fastfood" -> Icons.Rounded.Fastfood
            "rent", "home", "housing" -> Icons.Rounded.Home
            "transport", "car", "taxi" -> Icons.Rounded.DirectionsCar
            "water" -> Icons.Rounded.WaterDrop
            "power", "electricity", "bolt" -> Icons.Rounded.Bolt
            "wifi", "internet" -> Icons.Rounded.Wifi
            "coffee", "cafe" -> Icons.Rounded.LocalCafe
            "health", "medical", "hospital" -> Icons.Rounded.LocalHospital
            "sport", "gym", "fitness" -> Icons.Rounded.FitnessCenter
            "shopping" -> Icons.Rounded.ShoppingBag
            "family" -> Icons.Rounded.Groups
            else -> Icons.Rounded.Category
        }
    }
}
