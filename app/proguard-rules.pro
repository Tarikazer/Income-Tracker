# Proguard / R8 rules for Income Control
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}
-dontwarn javax.annotation.**
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
