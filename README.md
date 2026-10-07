# 🗣️ SpeakEasy

**An offline, no-typing language-learning app for Android — spaced repetition the lazy (efficient) way.**

SpeakEasy drills vocabulary with the **Leitner system** (6 boxes): read the French word, think, reveal, then tap *Got it* or *Missed*. No typing, no account, no backend, no ads — everything runs on the device.

## Languages

- 🇷🇺 **Russian**, 🇮🇹 **Italian**, and **Friulan** (Pordenone variety) — a seriously under-served minority language
- Spanish, Portuguese and English are planned (the data pipeline is language-agnostic)

## Features

- **Leitner spaced repetition** — 6 boxes, per-language progress, review sessions sized to what's actually due.
- **No keyboard**: self-graded recall (show → reveal → Got it / Missed), the fastest honest drill loop.
- **Conjugation tables** and **grammar sheets** (Markdown) per language, with sources cited.
- **Native Android TTS** pronounces every word and example sentence.
- **Profiles** — several learners on one device, local only.
- 100 % **offline**; the only permission is `VIBRATE`.
- Kotlin, Jetpack Compose (Material 3), Room, DataStore.

## Building

```bash
git clone https://github.com/hypedigger/SpeakEasy.git
cd SpeakEasy
# open in Android Studio, or:
gradlew.bat assembleDebug
```

Requires Android Studio (AGP 9.x) with SDK 37; min SDK 26 (Android 8.0).

The vocabulary database ships as JSON assets (`app/src/main/assets/`), built by the scripts in `tools/` from spreadsheet sources. The `data/` working directory (raw course notes, AI generation batches) is intentionally not part of the repository.

## Content disclaimer

Vocabulary, example sentences and the Friulan content in particular are **partly AI-generated and only partially human-reviewed** — expect occasional mistakes. Grammar sheets cite their sources (ARLeF, Treccani and others). Corrections are very welcome, especially from native speakers.

## Support

If SpeakEasy helps your language journey:

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/hypedigger)

## License

[MIT](LICENSE) for the code. Vocabulary data: see the content disclaimer above.
