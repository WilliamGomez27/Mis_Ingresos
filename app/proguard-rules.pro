# Rules for Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Rules for ML Kit Text Recognition
-keep class com.google.mlkit.vision.** { *; }
-dontwarn com.google.mlkit.vision.**

# Rules for Jetpack Compose & Navigation
-keep class androidx.navigation.** { *; }
