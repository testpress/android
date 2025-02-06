-ignorewarnings
-dontskipnonpubliclibraryclasses

-ignorewarnings

-renamesourcefileattribute SourceFile

-keepattributes SourceFile,LineNumberTable,*Annotation*

-printmapping map.txt
-printseeds seed.txt

-keepclassmembers enum * { public static **[] values(); public static ** valueOf(java.lang.String); }

-keep class com.madgag.android.blockingprompt.**

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View { public <init>(android.content.Context); public <init>(android.content.Context, android.util.AttributeSet); public <init>(android.content.Context, android.util.AttributeSet, int); public void set*(...); }

-keepclassmembers class * extends android.app.Activity { public void *(android.view.View); }
-keepclassmembers class android.support.v4.app.Fragment { *** getActivity(); public *** onCreate(); public *** onCreateOptionsMenu(...); }

-keep public class * extends junit.framework.TestCase

-keepclassmembers class * { @com.google.inject.Provides *; @android.test.suitebuilder.annotation.* *; void test*(...); }

-keep class com.google.inject.Binder
-keep class com.google.inject.Key
-keep class com.google.inject.Provider
-keep class com.google.inject.TypeLiteral

-keepclassmembers class * { @com.google.inject.Inject <init>(...); }
-keepclassmembers class com.google.inject.assistedinject.FactoryProvider2 { *; }
-keepclassmembers class com.google.** {
    private void finalizeReferent();
    protected void finalizeReferent();
    public void finalizeReferent();
    void finalizeReferent();

    private *** startFinalizer(java.lang.Class,java.lang.Object);
    protected *** startFinalizer(java.lang.Class,java.lang.Object);
    public *** startFinalizer(java.lang.Class,java.lang.Object);
    *** startFinalizer(java.lang.Class,java.lang.Object);
}

-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

-dontwarn butterknife.Views$InjectViewProcessor
-keepclassmembers class **$$ViewInjector {*;}

-dontwarn com.squareup.okhttp.**

-dontwarn android.test.**
-keep class android.test.** { *; }

# Keep Dagger annotations and generated code
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * implements dagger.** { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.Component class * { *; }
-keep @dagger.Subcomponent class * { *; }
-keep @dagger.Provides class * { *; }
-keep @dagger.Binds class * { *; }
-keep @dagger.BindsInstance class * { *; }
-keep @dagger.android.AndroidInjector class * { *; }

# Keep GreenDAO generated classes
-keep class * extends de.greenrobot.dao.AbstractDao { *; }
-keep class * extends de.greenrobot.dao.AbstractDaoSession { *; }
-keep class * extends de.greenrobot.dao.identityscope.IdentityScopeType { *; }
-keep class * implements de.greenrobot.dao.AbstractDao { *; }
-keep class * implements de.greenrobot.dao.Property { *; }
-keep class de.greenrobot.dao.** { *; }

# Keep your entity classes (update with your actual package)
-keep class in.testpress.testpress.models.** { *; }

-keep class in.testpress.testpress.** { *; }

-keep class in.testpress.** { *; }

# Keep DaoMaster and DaoSession
-keep class **DaoMaster { *; }
-keep class **DaoSession { *; }
-keep class **DaoConfig { *; }

# Keep Retrofit annotations and methods
-keep class retrofit2.** { *; }
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit annotations and methods
-keep class retrofit.** { *; }
-keepclassmembers class * {
    @retrofit.http.* <methods>;
}

# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

# With R8 full mode generic signatures are stripped for classes that are not kept.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Keep the Otto Bus class and its methods
-keep class com.squareup.otto.Bus { *; }

# Keep all @Produce and @Subscribe annotated methods (Otto uses reflection for these)
-keepclassmembers class * {
    @com.squareup.otto.Subscribe <methods>;
    @com.squareup.otto.Produce <methods>;
}

# Keep any anonymous inner classes that reference Otto
-keep class **$$Lambda$* { *; }

# If you use event producers (deprecated feature)
-keepclassmembers class * {
    @com.squareup.otto.Produce *;
}


## Keep all classes used in JSON parsing
-keep class * implements java.io.Serializable { *; }
-keepattributes InnerClasses
-keep class kotlin.Metadata { *; }

## Retain all enums
-keepclassmembers enum * { *; }

## Prevent ProGuard from removing ButterKnife annotations
-keep class butterknife.* { *; }
-keep class butterknife.** { *; }
-keepclassmembers class ** { @butterknife.* <fields>; }
-keepclassmembers class ** { @butterknife.* <methods>; }

## Dagger (Dependency Injection)
-keep class dagger.* { *; }
-keep interface dagger.* { *; }
-keep class javax.inject.* { *; }
-keep interface javax.inject.* { *; }

## Otto Event Bus
-keep class com.squareup.otto.Bus { *; }
-keepclassmembers class * {
    @com.squareup.otto.Subscribe <methods>;
    @com.squareup.otto.Produce <methods>;
}

## GreenDAO (Database)
-keep class de.greenrobot.dao.** { *; }
-keepclassmembers class **$Properties { *; }
-keep class **$OpenHelper { *; }
-keep class **DaoMaster { *; }
-keep class **DaoSession { *; }
-keep class **Dao { *; }

## Retrofit & OkHttp (HTTP)
-keep class retrofit.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep class com.squareup.retrofit.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn retrofit.**
-dontwarn okio.**
-dontwarn okhttp3.**

## Universal Image Loader
-keep class com.nostra13.universalimageloader.** { *; }

## Firebase (GCM, Messaging, Core)
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

## Google Sign-In
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }

## Facebook SDK
-keep class com.facebook.** { *; }
-keep class com.facebook.internal.** { *; }

## Sentry (Error Tracking)
-keep class io.sentry.** { *; }
-keepattributes *Annotation*

## MultiDex
-keep class androidx.multidex.** { *; }

## ViewModel & LiveData
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class * {
    @androidx.lifecycle.* <fields>;
}

## MarketingCloud SDK
-keep class com.salesforce.marketingcloud.** { *; }
-keep class com.salesforce.androidsdk.** { *; }

# Keep Room Database Entities & DAOs
-keep class androidx.room.* { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.Entity { *; }
-keep class * extends androidx.room.Dao { *; }
-keep class * extends androidx.room.TypeConverter { *; }


# Keep Gson classes that use generic types
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keepattributes Signature
-dontwarn com.google.gson.**

# Keep ExoPlayer classes
-keep class com.google.android.exoplayer2.** { *; }
-dontwarn com.google.android.exoplayer2.**
