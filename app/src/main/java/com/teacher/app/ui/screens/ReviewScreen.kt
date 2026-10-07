package com.teacher.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teacher.app.data.model.Direction
import com.teacher.app.data.model.ItemType
import com.teacher.app.data.model.Language
import com.teacher.app.data.model.ReviewCard
import com.teacher.app.srs.Leitner
import com.teacher.app.tts.rememberSpeaker
import com.teacher.app.ui.ReviewViewModel
import com.teacher.app.ui.appViewModel
import com.teacher.app.ui.theme.boxColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    language: Language,
    direction: Direction,
    type: ItemType,
    onBack: () -> Unit,
) {
    val viewModel: ReviewViewModel = viewModel(factory = ReviewViewModel.Factory)
    val settings = appViewModel()
    val state by viewModel.state.collectAsState()
    val ttsSetting by settings.ttsEnabled.collectAsState()
    // Inutile de proposer l'ecoute si Android n'a pas de voix pour la langue.
    val ttsEnabled = ttsSetting && language.hasSpeech
    val ttsAuto by settings.ttsAuto.collectAsState()
    val showPron by settings.showPronunciation.collectAsState()
    val speaker = rememberSpeaker()

    LaunchedEffect(language, direction, type) {
        viewModel.start(language, direction, type)
    }

    // Lecture automatique de la reponse, si l'option est activee.
    LaunchedEffect(state.revealed, state.index) {
        if (state.revealed && ttsEnabled && ttsAuto) {
            state.current?.let { speaker.speak(it.speakText, language) }
        }
    }

    val title = if (type == ItemType.WORD) "Vocabulaire" else "Phrases"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title)
                        Text(
                            text = directionLabel(direction, language),
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

                state.cards.isEmpty() -> EmptySession(onBack)

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
                        CardBody(
                            card = card,
                            revealed = state.revealed,
                            showPronunciation = showPron,
                            ttsEnabled = ttsEnabled,
                            onSpeak = { speaker.speak(card.speakText, language) },
                            onSpeakExample = {
                                card.example?.let { speaker.speak(it, language) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        )
                        AnswerBar(
                            revealed = state.revealed,
                            onReveal = viewModel::reveal,
                            onAnswer = viewModel::answer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardBody(
    card: ReviewCard,
    revealed: Boolean,
    showPronunciation: Boolean,
    ttsEnabled: Boolean,
    onSpeak: () -> Unit,
    onSpeakExample: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BoxBadge(card.box, card.isNew)
        Spacer(Modifier.height(20.dp))

        Text(
            text = card.prompt,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
        )

        // En sens langue -> francais, l'enonce est deja dans la langue cible :
        // on peut l'ecouter avant de repondre.
        if (ttsEnabled && card.direction == Direction.TARGET_TO_FR) {
            SpeakButton(onSpeak)
        }

        AnimatedVisibility(visible = revealed) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(Modifier.fillMaxWidth(0.5f))
                Spacer(Modifier.height(16.dp))

                Text(
                    text = card.answer,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                )

                if (showPronunciation && !card.pronunciation.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "[ ${card.pronunciation} ]",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Precision sur la forme : utile quand la variete apprise differe
                // de celle des dictionnaires (frioulan occidental vs Udine).
                if (!card.note.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = card.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                    )
                }

                if (ttsEnabled && card.direction == Direction.FR_TO_TARGET) {
                    SpeakButton(onSpeak)
                }

                if (!card.example.isNullOrBlank()) {
                    Spacer(Modifier.height(20.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Column(
                            Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = card.example,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                            )
                            if (!card.exampleFr.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = card.exampleFr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                )
                            }
                            // La phrase d'exemple s'ecoute aussi : entendre le mot
                            // en contexte vaut mieux que de l'entendre isole.
                            if (ttsEnabled) {
                                SpeakButton(
                                    onSpeak = onSpeakExample,
                                    size = 40.dp,
                                    iconSize = 22.dp,
                                    contentDescription = "Écouter la phrase d'exemple",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpeakButton(
    onSpeak: () -> Unit,
    size: Dp = 56.dp,
    iconSize: Dp = 32.dp,
    contentDescription: String = "Écouter",
) {
    IconButton(onClick = onSpeak, modifier = Modifier.size(size)) {
        Icon(
            Icons.Default.VolumeUp,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun BoxBadge(box: Int, isNew: Boolean) {
    val label = if (isNew) "Nouveau mot" else "Boîte $box · ${Leitner.intervalLabel(box)}"
    val color = if (isNew) {
        MaterialTheme.colorScheme.secondary
    } else {
        boxColors.getOrElse(box) { boxColors.last() }
    }
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        fontWeight = FontWeight.Medium,
    )
}

/**
 * Toute la reponse se donne au doigt : un bouton pour reveler, puis deux
 * boutons d'auto-evaluation. Jamais de saisie au clavier.
 */
@Composable
private fun AnswerBar(
    revealed: Boolean,
    onReveal: () -> Unit,
    onAnswer: (Boolean) -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
    ) {
        if (!revealed) {
            Button(
                onClick = onReveal,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
            ) {
                Text("Afficher", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onAnswer(false) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC5443B),
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.height(0.dp))
                    Text("  Raté", style = MaterialTheme.typography.titleMedium)
                }
                Button(
                    onClick = { onAnswer(true) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F7A4F),
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text("  Su", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun SessionSummary(
    ok: Int,
    failed: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    val total = ok + failed
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Session terminée", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (total == 0) "Rien à réviser." else "$ok réussites sur $total",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        if (total > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${(100f * ok / total).toInt()} % de réussite",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Text("Continuer à réviser")
        }
        Spacer(Modifier.height(12.dp))
        FilledTonalButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Retour au menu")
        }
    }
}

@Composable
private fun EmptySession(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Rien à réviser pour le moment", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Toutes les cartes dues sont faites et le quota de nouveaux mots " +
                "du jour est atteint. Revenez plus tard, ou augmentez le quota dans le profil.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        FilledTonalButton(onClick = onBack) { Text("Retour au menu") }
    }
}

private fun directionLabel(direction: Direction, language: Language): String =
    when (direction) {
        Direction.FR_TO_TARGET -> "Français → ${language.label}"
        Direction.TARGET_TO_FR -> "${language.label} → Français"
    }
