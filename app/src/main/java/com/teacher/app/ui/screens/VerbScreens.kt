package com.teacher.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teacher.app.data.model.Language
import com.teacher.app.data.model.Tense
import com.teacher.app.tts.rememberSpeaker
import com.teacher.app.ui.ConjugationViewModel
import com.teacher.app.ui.TenseListViewModel
import com.teacher.app.ui.VerbViewModel
import com.teacher.app.ui.appViewModel

/** Choix du temps a reviser. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TensePickerScreen(
    language: Language,
    onPick: (Tense) -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: TenseListViewModel = viewModel(factory = TenseListViewModel.Factory)
    val tenses by viewModel.tenses.collectAsState()

    LaunchedEffect(language) { viewModel.load(language) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conjugaison") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Choisissez le temps à réviser. On vous montre un verbe, " +
                        "vous récitez mentalement, puis vous vérifiez le tableau complet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
            }
            items(tenses.size) { index ->
                val tense = tenses[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPick(tense) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = tense.labelFor(language),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/** Revision d'un temps : verbe affiche, puis tableau complet et auto-evaluation. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbReviewScreen(
    language: Language,
    tense: Tense,
    onBack: () -> Unit,
    onOpenVerb: (Long) -> Unit,
) {
    val viewModel: VerbViewModel = viewModel(factory = VerbViewModel.Factory)
    val settings = appViewModel()
    val state by viewModel.state.collectAsState()
    val ttsSetting by settings.ttsEnabled.collectAsState()
    val ttsEnabled = ttsSetting && language.hasSpeech
    val speaker = rememberSpeaker()

    LaunchedEffect(language, tense) { viewModel.start(language, tense) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Conjugaison")
                        Text(
                            text = tense.labelFor(language),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (!state.finished && state.cards.isNotEmpty()) {
                        Text(
                            text = "${state.index + 1} / ${state.cards.size}",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.cards.isEmpty() -> Column(
                    Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Aucun verbe à réviser pour ce temps.",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                    )
                }

                state.finished -> SessionSummary(
                    ok = state.ok,
                    failed = state.failed,
                    onRestart = { viewModel.load() },
                    onBack = onBack,
                )

                else -> {
                    val card = state.current!!
                    Column(Modifier.fillMaxSize()) {
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Column(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = card.infinitive,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = card.meaningFr,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            if (!card.aspect.isNullOrBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = card.aspect,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                            if (ttsEnabled) {
                                IconButton(
                                    onClick = { speaker.speak(card.infinitive, language) },
                                    modifier = Modifier.size(56.dp),
                                ) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Écouter",
                                        modifier = Modifier.size(30.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }

                            AnimatedVisibility(visible = state.revealed) {
                                Column(
                                    Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Spacer(Modifier.height(12.dp))
                                    ConjugationCard(tense.labelFor(language), state.forms)
                                    if (!card.note.isNullOrBlank()) {
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            text = card.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = "Voir tous les temps de ce verbe",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { onOpenVerb(card.verbId) },
                                    )
                                }
                            }
                        }

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 20.dp),
                        ) {
                            if (!state.revealed) {
                                Button(
                                    onClick = { viewModel.reveal() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 60.dp),
                                ) {
                                    Text("Afficher", style = MaterialTheme.typography.titleMedium)
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Button(
                                        onClick = { viewModel.answer(false) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 60.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFC5443B),
                                            contentColor = Color.White,
                                        ),
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = null)
                                        Text("  Non maîtrisé")
                                    }
                                    Button(
                                        onClick = { viewModel.answer(true) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 60.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF2F7A4F),
                                            contentColor = Color.White,
                                        ),
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                        Text("  Su")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConjugationCard(title: String, forms: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            forms.forEach { (person, form) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = personLabel(person),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(96.dp),
                    )
                    Text(
                        text = form,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }
    }
}

/** Fiche complete d'un verbe : tous les temps disponibles. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConjugationDetailScreen(verbId: Long, language: Language, onBack: () -> Unit) {
    val viewModel: ConjugationViewModel = viewModel(factory = ConjugationViewModel.Factory)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(verbId) { viewModel.load(verbId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.infinitive.ifBlank { "Verbe" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        if (state.loading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(Modifier.height(4.dp))
                val details = listOfNotNull(
                    state.aspect,
                    state.group?.let { "groupe $it" },
                    state.aspectPair?.let { "paire : $it" },
                ).joinToString(" · ")
                if (details.isNotBlank()) {
                    Text(
                        text = details,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                if (!state.note.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = state.note!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Tense.entries.forEach { tense ->
                val forms = state.tables[tense]
                if (!forms.isNullOrEmpty()) {
                    item { ConjugationCard(tense.labelFor(language), forms) }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

private fun personLabel(person: String): String = when (person) {
    "m" -> "masculin"
    "f" -> "féminin"
    "n" -> "neutre"
    "pl" -> "pluriel"
    else -> person
}
