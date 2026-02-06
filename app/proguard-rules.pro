# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\progr\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Retrofit & OkHttp
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Jackson XML
-dontwarn com.fasterxml.jackson.**
-keep class com.fasterxml.jackson.** { *; }
-keepattributes *Annotation*, EnclosingMethod, Signature
-keepnames class com.fasterxml.jackson.** { *; }

# Hilt
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Planszowsky Data Models (Required for Jackson and Room mapping)
-keep class pl.pointblank.planszowsky.data.remote.** { *; }
-keep class pl.pointblank.planszowsky.data.local.** { *; }
-keep class pl.pointblank.planszowsky.domain.model.** { *; }

# Keep Compose view model and state classes
-keep class * extends androidx.lifecycle.ViewModel { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Gemini AI SDK
-keep class com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**
