# Proguard rules for NothingTune

# Just Audio & Audio Service
-keep class com.ryanheise.audioservice.** { *; }
-keep class com.ryanheise.just_audio.** { *; }

# Isar database
-keep class io.isar.** { *; }

# JSON
-keepclassmembers class * {
    @org.json.JSONObject *;
}
