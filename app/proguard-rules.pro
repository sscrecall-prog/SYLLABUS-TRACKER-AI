# Add project specific ProGuard rules here.

# AndroidX and Material3
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.animation.** { *; }

# Room Database rules
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Data Models & Entities (Ensure reflection / Room DAO mappings are preserved)
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Android Architecture Components / ViewModels
-keepclassmembers public class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}

# Line numbers & stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
