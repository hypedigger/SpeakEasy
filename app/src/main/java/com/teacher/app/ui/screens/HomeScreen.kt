package com.teacher.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teacher.app.data.model.Direction
import com.teacher.app.data.model.ItemType
import com.teacher.app.data.model.Language

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    language: Language,
    onVocab: (Direction) -> Unit,
    onPhrases: (Direction) -> Unit,
    onVerbs: () -> Unit,
    onGrammar: () -> Unit,
    onProfile: () -> Unit,
    onChangeLanguage: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${language.flag}  ${language.label}") },
                actions = {
                    AssistChip(
                        onClick = onChangeLanguage,
                        label = { Text("Changer") },
                        modifier = Modifier.padding(end = 12.dp),
                    )
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
                SectionTitle("Vocabulaire")
            }
            item {
                MenuCard(
                    icon = Icons.Default.Translate,
                    title = "Mots · Français → ${language.label}",
                    subtitle = "Vous voyez le mot français, vous le retrouvez",
                    onClick = { onVocab(Direction.FR_TO_TARGET) },
                )
            }
            item {
                MenuCard(
                    icon = Icons.Default.Translate,
                    title = "Mots · ${language.label} → Français",
                    subtitle = "Progression suivie séparément de l'autre sens",
                    onClick = { onVocab(Direction.TARGET_TO_FR) },
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle("Phrases")
            }
            item {
                MenuCard(
                    icon = Icons.Default.ChatBubbleOutline,
                    title = "Phrases · Français → ${language.label}",
                    subtitle = "Les phrases se retiennent mieux que les mots isolés",
                    onClick = { onPhrases(Direction.FR_TO_TARGET) },
                )
            }
            item {
                MenuCard(
                    icon = Icons.Default.ChatBubbleOutline,
                    title = "Phrases · ${language.label} → Français",
                    subtitle = "Compréhension à l'écoute et à la lecture",
                    onClick = { onPhrases(Direction.TARGET_TO_FR) },
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle("Grammaire")
            }
            item {
                MenuCard(
                    icon = Icons.Default.Spellcheck,
                    title = "Conjugaison",
                    subtitle = "Choisissez un temps et révisez les verbes",
                    onClick = onVerbs,
                )
            }
            item {
                MenuCard(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "Règles de grammaire",
                    subtitle = "Cas, genres, pluriels, pronoms, adjectifs",
                    onClick = onGrammar,
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle("Suivi")
            }
            item {
                MenuCard(
                    icon = Icons.Default.Person,
                    title = "Mon profil",
                    subtitle = "Avancement, points faibles et réglages",
                    onClick = onProfile,
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun MenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Spacer(Modifier.fillMaxWidth(0.04f))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
