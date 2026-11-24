# 保留 TTS 相关回调（通过系统 Binder 反射调用的监听器）
-keep class android.speech.tts.** { *; }
-keepclassmembers class * implements android.speech.tts.TextToSpeech$OnInitListener { *; }
-keepclassmembers class * extends android.speech.tts.UtteranceProgressListener { *; }

-dontwarn org.jetbrains.annotations.**
