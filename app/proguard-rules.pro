################################################################################
# Firebase Crashlytics
################################################################################

# Keep file names and line numbers.
-keepattributes SourceFile,LineNumberTable

# Keep custom exceptions.
-keep public class * extends java.lang.Exception

################################################################################
# Okhttp/Retrofit
################################################################################

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# https://github.com/square/okhttp/blob/339732e3a1b78be5d792860109047f68a011b5eb/okhttp/src/jvmMain/resources/META-INF/proguard/okhttp3.pro#L11-L14
-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.**
# Related to this issue on https://github.com/square/retrofit/issues/3880
# Check https://github.com/square/retrofit/tags for new versions
-keep,allowobfuscation,allowshrinking class kotlin.Result
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
# This solves this issue https://github.com/square/retrofit/issues/3880
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

#######################################################
# Firebase Auth
#######################################################
-keep class com.google.firebase.auth.** { *; }
-dontwarn com.google.firebase.auth.**
-keep class com.google.android.gms.** { *; }

#######################################################
# Firebase Firestore
#######################################################
-keep class com.google.firebase.firestore.** { *; }
-dontwarn com.google.firebase.firestore.**
-keep class com.google.android.gms.tasks.** { *; }

#######################################################
# Firebase Storage
#######################################################
-keep class com.google.firebase.storage.** { *; }
-dontwarn com.google.firebase.storage.**
-keep class com.google.android.gms.tasks.** { *; }


-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.spongycastle.cert.X509CertificateHolder
-dontwarn org.spongycastle.cms.CMSEnvelopedData
-dontwarn org.spongycastle.cms.Recipient
-dontwarn org.spongycastle.cms.RecipientId
-dontwarn org.spongycastle.cms.RecipientInformation
-dontwarn org.spongycastle.cms.RecipientInformationStore
-dontwarn org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient
-dontwarn org.spongycastle.cms.jcajce.JceKeyTransRecipient
