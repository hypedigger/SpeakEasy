package com.teacher.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.teacher.app.TeacherApp
import com.teacher.app.data.db.BoxCount
import com.teacher.app.data.db.CardRow
import com.teacher.app.data.db.GrammarPageEntity
import com.teacher.app.data.model.Direction
import com.teacher.app.data.model.ItemType
import com.teacher.app.data.model.Language
import com.teacher.app.data.model.ReviewCard
import com.teacher.app.data.model.Tense
import com.teacher.app.data.model.VerbCard
import com.teacher.app.data.repo.Settings
import com.teacher.app.data.repo.TeacherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

private val app: (CreationExtras) -> TeacherApp = { extras ->
    extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TeacherApp
}

// ---------------------------------------------------------------------------
// Reglages et langue active
// ---------------------------------------------------------------------------

class AppViewModel(
    private val settings: Settings,
) : ViewModel() {

    val language: StateFlow<Language?> =
        settings.language.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val newPerDay: StateFlow<Int> =
        settings.newPerDay.stateIn(viewModelScope, SharingStarted.Eagerly, 10)

    val sessionSize: StateFlow<Int> =
        settings.sessionSize.stateIn(viewModelScope, SharingStarted.Eagerly, 20)

    val ttsEnabled: StateFlow<Boolean> =
        settings.ttsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val ttsAuto: StateFlow<Boolean> =
        settings.ttsAuto.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val showPronunciation: StateFlow<Boolean> =
        settings.showPronunciation.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun selectLanguage(language: Language) = viewModelScope.launch {
        settings.setLanguage(language)
    }

    fun setNewPerDay(value: Int) = viewModelScope.launch { settings.setNewPerDay(value) }
    fun setSessionSize(value: Int) = viewModelScope.launch { settings.setSessionSize(value) }
    fun setTtsEnabled(value: Boolean) = viewModelScope.launch { settings.setTtsEnabled(value) }
    fun setTtsAuto(value: Boolean) = viewModelScope.launch { settings.setTtsAuto(value) }
    fun setShowPronunciation(value: Boolean) = viewModelScope.launch {
        settings.setShowPronunciation(value)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AppViewModel(app(this).settings) }
        }
    }
}

@Composable
fun appViewModel(): AppViewModel = viewModel(factory = AppViewModel.Factory)

// ---------------------------------------------------------------------------
// Revision du vocabulaire et des phrases
// ---------------------------------------------------------------------------

data class ReviewUiState(
    val loading: Boolean = true,
    val cards: List<ReviewCard> = emptyList(),
    val index: Int = 0,
    val revealed: Boolean = false,
    val ok: Int = 0,
    val failed: Int = 0,
) {
    val current: ReviewCard? get() = cards.getOrNull(index)
    val finished: Boolean get() = !loading && index >= cards.size
    val progress: Float get() = if (cards.isEmpty()) 0f else index.toFloat() / cards.size
}

class ReviewViewModel(
    private val repository: TeacherRepository,
    private val settings: Settings,
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewUiState())
    val state: StateFlow<ReviewUiState> = _state.asStateFlow()

    private var language: Language = Language.RU
    private var direction: Direction = Direction.FR_TO_TARGET
    private var type: ItemType = ItemType.WORD

    fun start(language: Language, direction: Direction, type: ItemType) {
        this.language = language
        this.direction = direction
        this.type = type
        load()
    }

    fun load() = viewModelScope.launch {
        _state.value = ReviewUiState(loading = true)
        val cards = repository.buildSession(
            language = language,
            direction = direction,
            type = type,
            sessionSize = settings.sessionSize.first(),
            newPerDay = settings.newPerDay.first(),
        )
        _state.value = ReviewUiState(loading = false, cards = cards)
    }

    fun reveal() {
        _state.value = _state.value.copy(revealed = true)
    }

    fun answer(success: Boolean) {
        val card = _state.value.current ?: return
        viewModelScope.launch {
            repository.answer(language, direction, card.conceptId, success)
        }
        _state.value = _state.value.let { s ->
            s.copy(
                index = s.index + 1,
                revealed = false,
                ok = s.ok + if (success) 1 else 0,
                failed = s.failed + if (success) 0 else 1,
            )
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = app(this)
                ReviewViewModel(application.repository, application.settings)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Revision des conjugaisons
// ---------------------------------------------------------------------------

data class VerbUiState(
    val loading: Boolean = true,
    val cards: List<VerbCard> = emptyList(),
    val index: Int = 0,
    val revealed: Boolean = false,
    val forms: List<Pair<String, String>> = emptyList(),
    val ok: Int = 0,
    val failed: Int = 0,
) {
    val current: VerbCard? get() = cards.getOrNull(index)
    val finished: Boolean get() = !loading && index >= cards.size
    val progress: Float get() = if (cards.isEmpty()) 0f else index.toFloat() / cards.size
}

class VerbViewModel(
    private val repository: TeacherRepository,
    private val settings: Settings,
) : ViewModel() {

    private val _state = MutableStateFlow(VerbUiState())
    val state: StateFlow<VerbUiState> = _state.asStateFlow()

    private var language: Language = Language.RU
    private var tense: Tense = Tense.PRESENT

    fun start(language: Language, tense: Tense) {
        this.language = language
        this.tense = tense
        load()
    }

    fun load() = viewModelScope.launch {
        _state.value = VerbUiState(loading = true)
        val cards = repository.buildVerbSession(
            language = language,
            tense = tense,
            sessionSize = settings.sessionSize.first(),
            newPerDay = settings.newPerDay.first(),
        )
        _state.value = VerbUiState(loading = false, cards = cards)
    }

    fun reveal() = viewModelScope.launch {
        val card = _state.value.current ?: return@launch
        val forms = repository.conjugationTable(card.verbId, tense)
        _state.value = _state.value.copy(revealed = true, forms = forms)
    }

    fun answer(success: Boolean) {
        val card = _state.value.current ?: return
        viewModelScope.launch {
            repository.answerVerb(card.verbId, tense, language, success)
        }
        _state.value = _state.value.let { s ->
            s.copy(
                index = s.index + 1,
                revealed = false,
                forms = emptyList(),
                ok = s.ok + if (success) 1 else 0,
                failed = s.failed + if (success) 0 else 1,
            )
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = app(this)
                VerbViewModel(application.repository, application.settings)
            }
        }
    }
}

/** Les temps qui ont vraiment des conjugaisons pour la langue active. */
class TenseListViewModel(
    private val repository: TeacherRepository,
) : ViewModel() {

    private val _tenses = MutableStateFlow<List<Tense>>(emptyList())
    val tenses: StateFlow<List<Tense>> = _tenses.asStateFlow()

    fun load(language: Language) = viewModelScope.launch {
        _tenses.value = repository.availableTenses(language)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { TenseListViewModel(app(this).repository) }
        }
    }
}

// ---------------------------------------------------------------------------
// Fiche complete d'un verbe
// ---------------------------------------------------------------------------

data class ConjugationUiState(
    val loading: Boolean = true,
    val infinitive: String = "",
    val meaningFr: String = "",
    val aspect: String? = null,
    val aspectPair: String? = null,
    val group: String? = null,
    val note: String? = null,
    val tables: Map<Tense, List<Pair<String, String>>> = emptyMap(),
)

class ConjugationViewModel(
    private val repository: TeacherRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ConjugationUiState())
    val state: StateFlow<ConjugationUiState> = _state.asStateFlow()

    fun load(verbId: Long) = viewModelScope.launch {
        val verb = repository.verb(verbId)
        if (verb == null) {
            _state.value = ConjugationUiState(loading = false)
            return@launch
        }
        _state.value = ConjugationUiState(
            loading = false,
            infinitive = verb.infinitive,
            meaningFr = "",
            aspect = verb.aspect,
            aspectPair = verb.aspectPair,
            group = verb.group,
            note = verb.note,
            tables = repository.fullConjugation(verbId),
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ConjugationViewModel(app(this).repository) }
        }
    }
}

// ---------------------------------------------------------------------------
// Grammaire
// ---------------------------------------------------------------------------

class GrammarViewModel(
    private val repository: TeacherRepository,
) : ViewModel() {

    private val language = MutableStateFlow(Language.RU)

    val pages: StateFlow<List<GrammarPageEntity>> = language
        .flatMapLatest { repository.grammarPages(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _page = MutableStateFlow<GrammarPageEntity?>(null)
    val page: StateFlow<GrammarPageEntity?> = _page.asStateFlow()

    fun setLanguage(value: Language) {
        language.value = value
    }

    fun loadPage(id: String) = viewModelScope.launch {
        _page.value = repository.grammarPage(id)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { GrammarViewModel(app(this).repository) }
        }
    }
}

// ---------------------------------------------------------------------------
// Profil et statistiques
// ---------------------------------------------------------------------------

data class ProfileUiState(
    val totalWords: Int = 0,
    val totalPhrases: Int = 0,
    val startedFr: Int = 0,
    val masteredFr: Int = 0,
    val startedTarget: Int = 0,
    val masteredTarget: Int = 0,
    val reviewsToday: Int = 0,
    val streak: Int = 0,
    val boxesFr: List<BoxCount> = emptyList(),
    val boxesTarget: List<BoxCount> = emptyList(),
    val weakest: List<CardRow> = emptyList(),
)

class ProfileViewModel(
    private val repository: TeacherRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    fun observe(language: Language) {
        viewModelScope.launch {
            repository.totalCards(language, ItemType.WORD).collect { value ->
                _state.value = _state.value.copy(totalWords = value)
            }
        }
        viewModelScope.launch {
            repository.totalCards(language, ItemType.PHRASE).collect { value ->
                _state.value = _state.value.copy(totalPhrases = value)
            }
        }
        viewModelScope.launch {
            repository.startedCount(language, Direction.FR_TO_TARGET).collect { value ->
                _state.value = _state.value.copy(startedFr = value)
            }
        }
        viewModelScope.launch {
            repository.masteredCount(language, Direction.FR_TO_TARGET).collect { value ->
                _state.value = _state.value.copy(masteredFr = value)
            }
        }
        viewModelScope.launch {
            repository.startedCount(language, Direction.TARGET_TO_FR).collect { value ->
                _state.value = _state.value.copy(startedTarget = value)
            }
        }
        viewModelScope.launch {
            repository.masteredCount(language, Direction.TARGET_TO_FR).collect { value ->
                _state.value = _state.value.copy(masteredTarget = value)
            }
        }
        viewModelScope.launch {
            repository.reviewsToday(language).collect { value ->
                _state.value = _state.value.copy(reviewsToday = value)
            }
        }
        viewModelScope.launch {
            repository.boxDistribution(language, Direction.FR_TO_TARGET).collect { value ->
                _state.value = _state.value.copy(boxesFr = value)
            }
        }
        viewModelScope.launch {
            repository.boxDistribution(language, Direction.TARGET_TO_FR).collect { value ->
                _state.value = _state.value.copy(boxesTarget = value)
            }
        }
        viewModelScope.launch {
            repository.weakest(language).collect { value ->
                _state.value = _state.value.copy(weakest = value)
            }
        }
        viewModelScope.launch {
            repository.activeDays(language).collect { days ->
                _state.value = _state.value.copy(streak = computeStreak(days))
            }
        }
    }

    fun reset(language: Language) = viewModelScope.launch {
        repository.resetProgress(language)
    }

    /** Nombre de jours consecutifs travailles, en comptant depuis aujourd'hui ou hier. */
    private fun computeStreak(days: List<Long>): Int {
        if (days.isEmpty()) return 0
        val today = System.currentTimeMillis() / 86_400_000L
        val set = days.toSortedSet().reversed().toSet()
        var cursor = when {
            set.contains(today) -> today
            set.contains(today - 1) -> today - 1
            else -> return 0
        }
        var streak = 0
        while (set.contains(cursor)) {
            streak++
            cursor--
        }
        return streak
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ProfileViewModel(app(this).repository) }
        }
    }
}
