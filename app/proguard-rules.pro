# Keep kotlinx.serialization generated serializers
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.codingkingsk.nikaalo.game.** {
    *** Companion;
}
-keepclasseswithmembers class com.codingkingsk.nikaalo.game.** {
    kotlinx.serialization.KSerializer serializer(...);
}
