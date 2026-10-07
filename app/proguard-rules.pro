# Add project specific ProGuard rules here.

# Keep Room annotations and classes
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-dontwarn androidx.room.paging.**

# Keep data models used in Room and JSON backup import/export
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }

# Keep Compose runtime
-keep class androidx.compose.** { *; }
