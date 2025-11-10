# Suppress unresolved-library warnings reported by ProGuard during packaging
-dontwarn androidx.**
-dontwarn org.jetbrains.skiko.**
-dontwarn org.jetbrains.skia.**
-dontwarn com.formdev.flatlaf.**
-dontwarn org.apache.pdfbox.**
-dontwarn org.bouncycastle.**
-dontwarn org.apache.logging.**
-dontwarn org.json.**
-dontwarn org.apache.commons.logging.**
# Keep application entry points to avoid removal
-keep class *MainKt { public static void main(java.lang.String[]); }
-keep class com.skira.app.** { *; }

# Ignore Android-specific classes
-dontwarn android.**
-dontwarn dalvik.system.**

# Ignore classes from libraries you aren't using
-dontwarn javax.servlet.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.apache.avalon.framework.logger.**
-dontwarn org.apache.log4j.**
-dontwarn org.openjsse.**

# --- General Kotlin & Compose Rules ---

# Keep Kotlin metadata, which is used for reflection
-keep class kotlin.Metadata.** { *; }
-keepclassmembers 'class ** {
    @kotlin.Metadata <methods>;
}'

# Keep all classes annotated with @Serializable and their serializers
-keepclasseswithmembers 'public class * {
    @kotlinx.serialization.Serializable <fields>;
}'
-keep class *$$serializer { *; }

# Fix for kotlinx.serialization error in your log [cite: 73, 74]
-keep class com.skira.app.r.AssayMeta.** { *; }
-keep class com.skira.app.r.AssayMeta$$serializer.** { *; }

# Keep companion objects for data classes
-keepclassmembers 'class **$Companion {
    public static final ** Companion;
}'

# --- Library-Specific Rules (based on your log) ---

# Keep FlatLaf (UI library)
-keep class com.formdev.flatlaf.** { *; }
-keepclassmembers class com.formdev.flatlaf.** { *; }

# Keep OkHttp
-keep class okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }

# Keep Apache PDFBox
-keep class org.apache.pdfbox.** { *; }

# Keep Apache Commons Logging
-keep class org.apache.commons.logging.** { *; }
