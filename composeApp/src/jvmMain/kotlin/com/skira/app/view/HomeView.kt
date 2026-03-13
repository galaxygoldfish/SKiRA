package com.skira.app.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.focusable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skira.app.SKiRATheme
import com.skira.app.structures.DialogType
import com.skira.app.structures.SidebarPage
import com.skira.app.view.dialog.*
import com.skira.app.view.fragment.PlotDisplayFragment
import com.skira.app.view.fragment.SidebarFragment
import com.skira.app.view.fragment.StatusBarFragment
import com.skira.app.view.fragment.TabSelectorFragment
import com.skira.app.view.fragment.TitleBarFragment
import com.skira.app.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WindowScope.HomeView(windowState: WindowState, exitApplication: () -> Unit) {
    val scope = rememberCoroutineScope()
    val viewModel: HomeViewModel = viewModel()

    /* Show the onboarding process if it is needed or not complete yet */
    LaunchedEffect(true) { viewModel.determineOnboardingStatus() }

    /* Initiate metadata loading only when prerequisites are actually met */
    val shouldLoadMeta = viewModel.computeShouldLoadMeta()
    LaunchedEffect(shouldLoadMeta) {
        if (shouldLoadMeta) {
            viewModel.warmupAndLoadMeta()
        }
    }

    SKiRATheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                val isDialogVisible = viewModel.currentDialogToShow != DialogType.NONE
                val dialogFocusRequester = remember { FocusRequester() }
                val nonDismissibleDialogs = setOf(
                    DialogType.WELCOME,
                    DialogType.DOWNLOAD_ONBOARD,
                    DialogType.SELECT_EXISTING_OBJECT,
                    DialogType.SELECT_DOWNLOAD_PATH,
                    DialogType.DOWNLOAD_OBJECT
                )

                LaunchedEffect(viewModel.currentDialogToShow) {
                    if (viewModel.currentDialogToShow != DialogType.NONE) {
                        dialogFocusRequester.requestFocus()
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isDialogVisible) Modifier.blur(7.dp) else Modifier)
                ) {
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
                            BoxWithConstraints(
                                modifier = Modifier.fillMaxSize()
                                    .padding(paddingValues)
                            ) {
                                val isSidebarMinimized = viewModel.sidebarMinimized && viewModel.currentSidebarPage == SidebarPage.DEFAULT
                                val sidebarWidth by animateDpAsState(
                                    targetValue = maxWidth * if (isSidebarMinimized) 0.055f else 0.23f,
                                    animationSpec = tween(
                                        durationMillis = 280,
                                        easing = FastOutSlowInEasing
                                    ),
                                    label = "sidebar-width"
                                )
                                Column {
                                    TabSelectorFragment(viewModel)
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.fillMaxHeight().width(sidebarWidth)) {
                                            SidebarFragment(viewModel)
                                        }
                                        Box(
                                            modifier = Modifier.weight(1f).fillMaxHeight()
                                                .padding(top = 10.dp, end = 10.dp, bottom = 10.dp)
                                        ) {
                                            AnimatedContent(
                                                targetState = viewModel.currentTabInView,
                                                transitionSpec = {
                                                    val movingForward = targetState > initialState
                                                    (fadeIn(
                                                            animationSpec = tween(
                                                                durationMillis = 210,
                                                                easing = FastOutSlowInEasing
                                                            )
                                                        ) + slideInHorizontally(
                                                            initialOffsetX = { fullWidth ->
                                                                val offset = (fullWidth * 0.08f).toInt()
                                                                if (movingForward) offset else -offset
                                                            },
                                                            animationSpec = tween(
                                                                durationMillis = 210,
                                                                easing = FastOutSlowInEasing
                                                            )
                                                        )
                                                    ).togetherWith(
                                                        fadeOut(
                                                            animationSpec = tween(durationMillis = 120)
                                                        ) + slideOutHorizontally(
                                                            targetOffsetX = { fullWidth ->
                                                                val offset = (fullWidth * 0.05f).toInt()
                                                                if (movingForward) -offset else offset
                                                            },
                                                            animationSpec = tween(durationMillis = 150)
                                                        )
                                                    ).using(SizeTransform(clip = false))
                                                },
                                                label = "tab-content"
                                            ) { _ ->
                                                Column(modifier = Modifier.fillMaxSize()) {
                                                    StatusBarFragment(viewModel)
                                                    PlotDisplayFragment(viewModel)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
                AnimatedVisibility(
                    visible = viewModel.currentDialogToShow != DialogType.NONE,
                    enter = fadeIn(
                        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing)
                    ),
                    exit = fadeOut(
                        animationSpec = tween(durationMillis = 140)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.2F))
                            .focusRequester(dialogFocusRequester)
                            .focusable()
                            .onPreviewKeyEvent { event ->
                                if (event.type == KeyEventType.KeyUp && event.key == Key.Escape) {
                                    val dialogType = viewModel.currentDialogToShow
                                    if (dialogType !in nonDismissibleDialogs && dialogType != DialogType.NONE) {
                                        viewModel.currentDialogToShow = DialogType.NONE
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                            .pointerInput(Unit) {
                                // Consume scrim taps so background content cannot be interacted with.
                                detectTapGestures(onTap = { })
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        /* Dynamic dialog: all dialogs have the same container, just switching the content as navigated */
                        AnimatedVisibility(
                            visible = viewModel.currentDialogToShow != DialogType.NONE,
                            enter = fadeIn(
                                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                            ) + scaleIn(
                                initialScale = 0.9f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ),
                            exit = fadeOut(
                                animationSpec = tween(durationMillis = 140)
                            ) + scaleOut(
                                targetScale = 0.95f,
                                animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing)
                            )
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

                                        DialogType.COLOR_CREATION -> {
                                            ColorCreationDialogContent(
                                                onClose = {
                                                    viewModel.currentDialogToShow = DialogType.NONE
                                                }
                                            )
                                        }

                                        DialogType.EXPORT_PLOT -> {
                                            ExportPlotDialogContent(
                                                onDismissRequest = {
                                                    viewModel.currentDialogToShow = DialogType.NONE
                                                }
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
