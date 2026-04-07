package com.skira.app.view.fragment

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.skira.app.assistant.AssistantUiState
import com.skira.app.assistant.MyGeneInfoData
import com.skira.app.components.HoverAware
import com.skira.app.components.ShimmerPlaceholder
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.assistant_title
import com.skira.app.composeapp.generated.resources.assistant_idle_message
import com.skira.app.composeapp.generated.resources.assistant_link_google
import com.skira.app.composeapp.generated.resources.assistant_link_ncbi
import com.skira.app.composeapp.generated.resources.assistant_link_zfin
import com.skira.app.composeapp.generated.resources.assistant_unavailable_message
import com.skira.app.composeapp.generated.resources.icon_earth
import com.skira.app.composeapp.generated.resources.icon_googlescholar
import com.skira.app.composeapp.generated.resources.icon_warning_hex
import com.skira.app.composeapp.generated.resources.logo_nih
import com.skira.app.composeapp.generated.resources.logo_zfin
import com.skira.app.viewmodel.AssistantViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import java.net.URLEncoder

/**
 * Container which updates each time a plot is created to show more info about the
 * current gene that is plotted and link to online resources
 */
@Composable
fun AssistantFragment(
    selectedGene: String,
    selectedTimepoint: String,
    viewModel: AssistantViewModel
) {
    // Trigger a fresh fetch every time the selected gene changes
    LaunchedEffect(selectedGene) {
        if (selectedGene != "Select" && selectedGene.isNotBlank()) {
            viewModel.fetchGeneInfo(selectedGene)
        } else {
            viewModel.reset()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = MaterialTheme.shapes.medium
            )
            .border(
                width = (1.5).dp,
                color = MaterialTheme.colorScheme.outline,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.icon_earth),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(0.6F)),
            )
            Text(
                text = stringResource(Res.string.assistant_title),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                modifier = Modifier.padding(start = 5.dp)
            )
        }

        AnimatedContent(
            targetState = viewModel.uiState,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(150))
            },
            label = "assistant-state"
        ) { state ->
            when (state) {
                is AssistantUiState.Idle -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.assistant_idle_message),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.4F),
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                is AssistantUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(22.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.7f).height(14.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(14.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(14.dp))
                        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp))
                    }
                }

                is AssistantUiState.Success -> {
                    GeneInfoContent(data = state.data, selectedTimepoint = selectedTimepoint)
                }

                is AssistantUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(Res.drawable.icon_warning_hex),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground.copy(0.6F))
                            )
                            Text(
                                text = stringResource(Res.string.assistant_unavailable_message, selectedGene),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                                modifier = Modifier.padding(start = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GeneInfoContent(data: MyGeneInfoData, selectedTimepoint: String) {
    val uriHandler = LocalUriHandler.current
    val scholarQuery = buildString {
        append(data.symbol)
        if (selectedTimepoint.isNotBlank() && selectedTimepoint != "Select") {
            append(" at ")
            append(selectedTimepoint)
        }
        append(" in nothobranchius furzeri")
    }
    val scholarUrl = "https://scholar.google.com/scholar?q=${URLEncoder.encode(scholarQuery, "UTF-8")}"

    Column(
        modifier = Modifier.padding(start = 15.dp, end = 15.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = data.symbol,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = data.fullName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.7F),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(top = 5.dp)
            )
            if (data.description.isNotBlank()) {
                Text(
                    text = data.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.6F),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AssistantLinkPill(
                label = AnnotatedString(stringResource(Res.string.assistant_link_zfin, data.symbol)),
                url = data.zfinUrl,
                containerColor = Color(0XFFC7D8D9),
                onClick = uriHandler::openUri,
                icon = {
                    Image(
                        painter = painterResource(Res.drawable.logo_zfin),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            AssistantLinkPill(
                label = AnnotatedString(stringResource(Res.string.assistant_link_ncbi, data.symbol)),
                url = data.killifishSearchUrl,
                containerColor = Color(0XFFB2BCC7),
                onClick = uriHandler::openUri,
                icon = {
                    Image(
                        painter = painterResource(Res.drawable.logo_nih),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
            AssistantLinkPill(
                label = buildAnnotatedString {
                    append(stringResource(Res.string.assistant_link_google, data.symbol))
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append("Nothobranchius furzeri")
                    }
                },
                url = scholarUrl,
                containerColor = Color(0XFFD3DCEF),
                onClick = uriHandler::openUri,
                icon = {
                    Image(
                        painter = painterResource(Res.drawable.icon_googlescholar),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun AssistantLinkPill(
    label: AnnotatedString,
    url: String,
    containerColor: Color,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    HoverAware { isHovered, interactionSource ->
        val hoverAlpha = animateFloatAsState(
            targetValue = if (isHovered) 0.92f else 1f,
            animationSpec = tween(durationMillis = 120),
            label = "assistant-link-pill-alpha"
        )

        Row(
            modifier = modifier
                .height(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(containerColor)
                .hoverable(interactionSource)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable { onClick(url) }
                .padding(horizontal = 16.dp)
                .alpha(hoverAlpha.value),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(0.72f),
                maxLines = 1
            )
        }
    }
}

