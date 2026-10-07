package com.teacher.app.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teacher.app.data.model.Direction
import com.teacher.app.data.model.ItemType
import com.teacher.app.data.model.Language
import com.teacher.app.data.model.Tense
import com.teacher.app.ui.appViewModel
import com.teacher.app.ui.screens.ConjugationDetailScreen
import com.teacher.app.ui.screens.GrammarDetailScreen
import com.teacher.app.ui.screens.GrammarListScreen
import com.teacher.app.ui.screens.HomeScreen
import com.teacher.app.ui.screens.LanguageScreen
import com.teacher.app.ui.screens.ProfileScreen
import com.teacher.app.ui.screens.ReviewScreen
import com.teacher.app.ui.screens.TensePickerScreen
import com.teacher.app.ui.screens.VerbReviewScreen

object Routes {
    const val LANGUAGE = "language"
    const val HOME = "home"
    const val REVIEW = "review/{type}/{direction}"
    const val TENSES = "tenses"
    const val VERB_REVIEW = "verbs/{tense}"
    const val CONJUGATION = "conjugation/{verbId}"
    const val GRAMMAR = "grammar"
    const val GRAMMAR_PAGE = "grammar/{pageId}"
    const val PROFILE = "profile"

    fun review(type: ItemType, direction: Direction) = "review/${type.code}/${direction.code}"
    fun verbReview(tense: Tense) = "verbs/${tense.code}"
    fun conjugation(verbId: Long) = "conjugation/$verbId"
    fun grammarPage(pageId: String) = "grammar/$pageId"
}

@Composable
fun TeacherNavHost(
    contentReady: Boolean,
    navController: NavHostController = rememberNavController(),
) {
    val settings = appViewModel()
    val language by settings.language.collectAsState()

    if (!contentReady) {
        Box(Modifier.fillMaxSize()) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        }
        return
    }

    val start = if (language == null) Routes.LANGUAGE else Routes.HOME

    NavHost(navController = navController, startDestination = start) {

        composable(Routes.LANGUAGE) {
            LanguageScreen(onSelect = { picked ->
                settings.selectLanguage(picked)
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LANGUAGE) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            HomeScreen(
                language = language ?: Language.RU,
                onVocab = { direction ->
                    navController.navigate(Routes.review(ItemType.WORD, direction))
                },
                onPhrases = { direction ->
                    navController.navigate(Routes.review(ItemType.PHRASE, direction))
                },
                onVerbs = { navController.navigate(Routes.TENSES) },
                onGrammar = { navController.navigate(Routes.GRAMMAR) },
                onProfile = { navController.navigate(Routes.PROFILE) },
                onChangeLanguage = { navController.navigate(Routes.LANGUAGE) },
            )
        }

        composable(
            route = Routes.REVIEW,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("direction") { type = NavType.StringType },
            ),
        ) { entry ->
            val type = ItemType.fromCode(entry.arguments?.getString("type") ?: "mot")
            val direction = Direction.fromCode(entry.arguments?.getString("direction") ?: "fr2t")
            ReviewScreen(
                language = language ?: Language.RU,
                direction = direction,
                type = type,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.TENSES) {
            TensePickerScreen(
                language = language ?: Language.RU,
                onPick = { tense -> navController.navigate(Routes.verbReview(tense)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.VERB_REVIEW,
            arguments = listOf(navArgument("tense") { type = NavType.StringType }),
        ) { entry ->
            val tense = Tense.fromCode(entry.arguments?.getString("tense").orEmpty())
                ?: Tense.PRESENT
            VerbReviewScreen(
                language = language ?: Language.RU,
                tense = tense,
                onBack = { navController.popBackStack() },
                onOpenVerb = { verbId -> navController.navigate(Routes.conjugation(verbId)) },
            )
        }

        composable(
            route = Routes.CONJUGATION,
            arguments = listOf(navArgument("verbId") { type = NavType.LongType }),
        ) { entry ->
            ConjugationDetailScreen(
                verbId = entry.arguments?.getLong("verbId") ?: 0L,
                language = language ?: Language.RU,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.GRAMMAR) {
            GrammarListScreen(
                language = language ?: Language.RU,
                onOpen = { pageId -> navController.navigate(Routes.grammarPage(pageId)) },
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.GRAMMAR_PAGE,
            arguments = listOf(navArgument("pageId") { type = NavType.StringType }),
        ) { entry ->
            GrammarDetailScreen(
                pageId = entry.arguments?.getString("pageId").orEmpty(),
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                language = language ?: Language.RU,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
