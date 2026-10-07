package com.teacher.app.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.teacher.app.data.model.Language
import java.util.Locale

/**
 * Synthese vocale via le moteur natif d'Android : gratuit, et hors-ligne une
 * fois la voix de la langue telechargee sur le telephone.
 */
class Speaker(context: Context) {

    private var engine: TextToSpeech? = null
    private var ready = false
    private var currentLocale: Locale? = null

    init {
        engine = TextToSpeech(context.applicationContext, OnInitListener { status ->
            ready = status == TextToSpeech.SUCCESS
            if (!ready) Log.w(TAG, "Moteur de synthese vocale indisponible")
        })
    }

    /**
     * Indique si la voix de la langue est installee. Si elle ne l'est pas,
     * l'utilisateur doit la telecharger dans les reglages Android.
     */
    fun isAvailable(language: Language): Boolean {
        val tts = engine ?: return false
        if (!ready) return false
        val result = tts.isLanguageAvailable(Locale.forLanguageTag(language.localeTag))
        return result >= TextToSpeech.LANG_AVAILABLE
    }

    fun speak(text: String, language: Language) {
        val tts = engine ?: return
        if (!ready || text.isBlank()) return

        val locale = Locale.forLanguageTag(language.localeTag)
        if (locale != currentLocale) {
            val result = tts.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Log.w(TAG, "Voix absente pour ${language.localeTag}")
                return
            }
            currentLocale = locale
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
    }

    fun stop() {
        engine?.stop()
    }

    fun release() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
    }

    private companion object {
        const val TAG = "Speaker"
    }
}

/** Fournit un [Speaker] dont le cycle de vie suit celui de l'ecran. */
@Composable
fun rememberSpeaker(): Speaker {
    val context = LocalContext.current
    val speaker = remember { Speaker(context) }
    DisposableEffect(speaker) {
        onDispose { speaker.release() }
    }
    return speaker
}
