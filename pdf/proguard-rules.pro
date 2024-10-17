################################################################################
# IText Pdf Library
################################################################################

# Keep classes for iText PDF core and layout libraries.
-keep class com.itextpdf.kernel.** { *; }
-dontwarn com.itextpdf.kernel.**

-keep class com.itextpdf.layout.** { *; }
-dontwarn com.itextpdf.layout.**

-keep class com.itextpdf.io.** { *; }
-dontwarn com.itextpdf.io.**

# Keep classes for iText BouncyCastle cryptography adapters.
-keep class com.itextpdf.bouncycastle.** { *; }
-dontwarn com.itextpdf.bouncycastle.**

# General rule for iText to avoid warnings for any unknown classes.
-dontwarn com.itextpdf.**