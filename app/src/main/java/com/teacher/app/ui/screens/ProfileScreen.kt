package com.teacher.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teacher.app.data.db.BoxCount
import com.teacher.app.data.model.Language
import com.teacher.app.srs.Leitner
import com.teacher.app.ui.ProfileViewModel
import com.teacher.app.ui.appViewModel
import com.teacher.app.ui.theme.boxColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(language: Language, onBack: () -> Unit) {
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
    val settings = appViewModel()
    val state by viewModel.state.collectAsState()
    val newPerDay by settings.newPerDay.collectAsState()
    val sessionSize by settings.sessionSize.collectAsState()
    val ttsEnabled by settings.ttsEnabled.collectAsState()
    val ttsAuto by settings.ttsAuto.collectAsState()
    val showPron by settings.showPronunciation.collectAsState()
    var confirmReset by remember { mutableStateOf(false) }

    LaunchedEffect(language) { viewModel.observe(language) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mon profil") },
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
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile("Série", "${state.streak} j", Modifier.weight(1f))
                    StatTile("Aujourd'hui", "${state.reviewsToday}", Modifier.weight(1f))
                    StatTile(
                        "Contenu",
                        "${state.totalWords + state.totalPhrases}",
                        Modifier.weight(1f),
                    )
                }
            }

            item {
                DirectionProgress(
                    title = "Français → ${language.label}",
                    started = state.startedFr,
                    mastered = state.masteredFr,
                    total = state.totalWords + state.totalPhrases,
                    boxes = state.boxesFr,
                )
            }

            item {
                DirectionProgress(
                    title = "${language.label} → Français",
                    started = state.startedTarget,
                    mastered = state.masteredTarget,
                    total = state.totalWords + state.totalPhrases,
                    boxes = state.boxesTarget,
                )
            }

            item {
                SettingsCard(
                    hasSpeech = language.hasSpeech,
                    newPerDay = newPerDay,
                    sessionSize = sessionSize,
                    ttsEnabled = ttsEnabled,
                    ttsAuto = ttsAuto,
                    showPron = showPron,
                    onNewPerDay = settings::setNewPerDay,
                    onSessionSize = settings::setSessionSize,
                    onTts = settings::setTtsEnabled,
                    onTtsAuto = settings::setTtsAuto,
                    onShowPron = settings::setShowPronunciation,
                )
            }

            if (state.weakest.isNotEmpty()) {
                item {
                    Text(
                        text = "Points faibles",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                    )
                }
                items(state.weakest, key = { it.conceptId }) { row ->
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(row.fr, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = row.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Remettre la progression à zéro")
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Tout remettre à zéro ?") },
            text = {
                Text(
                    "Toute votre progression en ${language.label} sera effacée : " +
                        "boîtes, historique et statistiques. Le contenu, lui, est conservé."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reset(language)
                    confirmReset = false
                }) { Text("Effacer") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Annuler") }
            },
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun DirectionProgress(
    title: String,
    started: Int,
    mastered: Int,
    total: Int,
    boxes: List<BoxCount>,
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else mastered.toFloat() / total },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$mastered maîtrisés · $started commencés · $total au total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (boxes.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text(
                    text = "Répartition par boîte",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                BoxChart(boxes)
            }
        }
    }
}

/** Petit histogramme des boites Leitner : du rouge (fragile) au vert (acquis). */
@Composable
private fun BoxChart(boxes: List<BoxCount>) {
    val byBox = (0..Leitner.MAX_BOX).map { box ->
        box to (boxes.firstOrNull { it.box == box }?.count ?: 0)
    }
    val max = byBox.maxOf { it.second }.coerceAtLeast(1)

    Row(
        Modifier
            .fillMaxWidth()
            .height(90.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        byBox.forEach { (box, count) ->
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Text(
                    text = if (count > 0) "$count" else "",
                    style = MaterialTheme.typography.labelSmall,
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height((count.toFloat() / max * 56).dp.coerceAtLeast(3.dp))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(boxColors.getOrElse(box) { boxColors.last() }),
                )
                Text(
                    text = "$box",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsCard(
    hasSpeech: Boolean,
    newPerDay: Int,
    sessionSize: Int,
    ttsEnabled: Boolean,
    ttsAuto: Boolean,
    showPron: Boolean,
    onNewPerDay: (Int) -> Unit,
    onSessionSize: (Int) -> Unit,
    onTts: (Boolean) -> Unit,
    onTtsAuto: (Boolean) -> Unit,
    onShowPron: (Boolean) -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Réglages", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            Text(
                text = "Nouveaux mots par jour : $newPerDay",
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = newPerDay.toFloat(),
                onValueChange = { onNewPerDay(it.toInt()) },
                valueRange = 0f..40f,
                steps = 39,
            )
            Text(
                text = "Un plafond bas évite l'accumulation de révisions : c'est " +
                    "la clé d'un système de répétition espacée qui tient dans la durée.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(14.dp))
            Text(
                text = "Cartes par session : $sessionSize",
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = sessionSize.toFloat(),
                onValueChange = { onSessionSize(it.toInt()) },
                valueRange = 5f..60f,
                steps = 54,
            )

            Spacer(Modifier.height(8.dp))
            HorizontalDivider()

            if (hasSpeech) {
                SettingSwitch("Synthèse vocale", ttsEnabled, onTts)
                SettingSwitch("Lire automatiquement la réponse", ttsAuto, onTtsAuto)
            } else {
                Text(
                    text = "Android ne propose pas de voix pour cette langue : "
                        + "la synthèse vocale est indisponible.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
            SettingSwitch("Afficher la prononciation", showPron, onShowPron)
        }
    }
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
