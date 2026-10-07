# Add project specific ProGuard rules here.

# Keep RoomDatabase subclass
-keep class * extends androidx.room.RoomDatabase

# Keep data models used in Room and JSON backup import/export
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }
