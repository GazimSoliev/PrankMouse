# proguard_rules.pro
#-dontobfuscate
#-dontoptimize

-dontwarn kotlinx.**

#noinspection ShrinkerUnresolvedReference
-keepclasseswithmembers public class MainKt {
    public static void main(java.lang.String[]);
}
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class org.jetbrains.skia.** { *; }
-keep class org.jetbrains.skiko.** { *; }