# Compose Multiplatform runtime keep rules
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# App domain models & Compose resource accessors
-keep class com.mformula.** { *; }
-keep class mformula.shared.generated.resources.** { *; }

# RevenueCat Purchases SDK keep rules
-keep class com.revenuecat.purchases.** { *; }
-dontwarn com.revenuecat.purchases.**
