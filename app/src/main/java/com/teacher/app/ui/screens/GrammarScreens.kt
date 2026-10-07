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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.teacher.app.data.model.Language
import com.teacher.app.ui.GrammarViewModel
import com.teacher.app.ui.components.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarListScreen(
    language: Language,
    onOpen: (String) -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: GrammarViewModel = viewModel(factory = GrammarViewModel.Factory)
    val pages by viewModel.pages.collectAsState()

    LaunchedEffect(language) { viewModel.setLanguage(language) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grammaire") },
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            val grouped = pages.groupBy { it.category }
            grouped.forEach { (category, categoryPages) ->
                item(key = "cat-$category") {
                    Text(
                        text = categoryLabel(category),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp, start = 4.dp),
                    )
                }
                items(categoryPages, key = { it.id }) { page ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(page.id) },
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
                            Text(
                                text = page.title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarDetailScreen(pageId: String, onBack: () -> Unit) {
    val viewModel: GrammarViewModel = viewModel(factory = GrammarViewModel.Factory)
    val page by viewModel.page.collectAsState()

    LaunchedEffect(pageId) { viewModel.loadPage(pageId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(page?.title ?: "Grammaire") },
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
        ) {
            item {
                page?.let { content ->
                    MarkdownText(content.contentMd, Modifier.fillMaxWidth())
                    if (content.links.isNotBlank()) {
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "Pour aller plus loin",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        content.links.lines().filter { it.isNotBlank() }.forEach { link ->
                            Text(
                                text = link,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

private fun categoryLabel(category: String): String = when (category) {
    "alphabet" -> "Alphabet et prononciation"
    "noms" -> "Les noms"
    "cas" -> "Les cas"
    "pronoms" -> "Les pronoms"
    "adjectifs" -> "Les adjectifs"
    "verbes" -> "Les verbes"
    "chiffres" -> "Les chiffres"
    "syntaxe" -> "La phrase"
    "prononciation" -> "Prononciation"
    "articles" -> "Les articles"
    "comparatifs" -> "Comparatifs et superlatifs"
    "prepositions" -> "Les prépositions"
    "lexique" -> "Du français à l'italien"
    "presentation" -> "La langue"
    "expressions" -> "Expressions du quotidien"
    "pieges" -> "Faux amis et pièges"
    else -> category.replaceFirstChar { it.uppercase() }
}
