package com.skira.app.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skira.app.SKiRATheme
import com.skira.app.r.canInvoke
import com.skira.app.structures.DialogType
import com.skira.app.structures.PreferenceKey
import com.skira.app.utilities.PreferenceManager
import com.skira.app.view.dialog.*
import com.skira.app.view.fragment.PlotDisplayFragment
import com.skira.app.view.fragment.PlotOptionFragment
import com.skira.app.view.fragment.StatusBarFragment
import com.skira.app.view.fragment.TitleBarFragment
import com.skira.app.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import kotlin.collections.listOf

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WindowScope.HomeView(windowState: WindowState, exitApplication: () -> Unit) {
    val scope = rememberCoroutineScope()
    val viewModel: HomeViewModel = viewModel()

    /* Show the onboarding process if it is needed or not complete yet */
    LaunchedEffect(true) { viewModel.determineOnboardingStatus() }

    /* Initiate metadata loading only if prepared to do so */
    LaunchedEffect(viewModel.computeShouldLoadMeta()) { viewModel.warmupAndLoadMeta() }

    SKiRATheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        topBar = {
                            TitleBarFragment(
                                windowState = windowState,
                                onMinimize = {
                                    scope.launch {
                                        windowState.isMinimized = true
                                    }
                                },
                                exitApplication = exitApplication,
                                viewModel = viewModel
                            )
                        },
                        content = { paddingValues ->
                            Box(
                                modifier = Modifier.fillMaxSize()
                                    .padding(paddingValues)
                                    .padding(40.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.22F)) {
                                        PlotOptionFragment(viewModel)
                                    }
                                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.9F)) {
                                        Column(modifier = Modifier.zIndex(1F)) {
                                            StatusBarFragment(viewModel)
                                        }
                                        PlotDisplayFragment(viewModel)
                                    }
                                }
                            }
                        }
                    )
                    /* Dynamic dialog: all dialogs have the same container, just switching the content as navigated */
                    AnimatedVisibility(
                        visible = viewModel.currentDialogToShow != DialogType.NONE,
                        exit = fadeOut(animationSpec = tween(durationMillis = 200)),
                        enter = fadeIn(animationSpec = tween(durationMillis = 100))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                AnimatedContent(
                                    targetState = viewModel.currentDialogToShow,
                                    modifier = Modifier.padding(30.dp)
                                ) { dialogContentType ->
                                    when (dialogContentType) {
                                        DialogType.SETTINGS -> {
                                            SettingsDialogContent(
                                                onDismissRequest = { viewModel.currentDialogToShow = DialogType.NONE }
                                            )
                                        }
                                        DialogType.WELCOME -> {
                                            WelcomeDialogContent(
                                                onNavigationRequest = { viewModel.currentDialogToShow = it }
                                            )
                                        }
                                        DialogType.DOWNLOAD_ONBOARD -> {
                                            DownloadOnboardDialogContent(
                                                onNavigationRequest = { viewModel.currentDialogToShow = it }
                                            )
                                        }
                                        DialogType.SELECT_EXISTING_OBJECT -> {
                                            DownloadSelectExistingDialogContent(
                                                onNavigationRequest = { viewModel.currentDialogToShow = it }
                                            )
                                        }
                                        DialogType.SELECT_DOWNLOAD_PATH -> {
                                            DownloadSetPathDialogContent(
                                                onNavigationRequest = { viewModel.currentDialogToShow = it }
                                            )
                                        }
                                        DialogType.DOWNLOAD_OBJECT -> {
                                            DownloadFetchDialogContent(
                                                onNavigationRequest = { viewModel.currentDialogToShow = it }
                                            )
                                        }
                                        DialogType.R_INSTALLATION -> {
                                            RInstallationDialogContent(
                                                onNavigationRequest = { viewModel.currentDialogToShow = it }
                                            )
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
}

