package com.skira.app.view.fragment

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.skira.app.components.ShimmerPlaceholder
import com.skira.app.viewmodel.HomeViewModel

@Composable
fun PlotDisplayFragment(viewModel: HomeViewModel) {
    AnimatedContent(targetState = viewModel.isLoadingPlot, modifier = Modifier.padding(top = 40.dp)) {
        if (it) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    ShimmerPlaceholder(
                        modifier = Modifier.aspectRatio(1F)
                            .weight(1F)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    ShimmerPlaceholder(
                        modifier = Modifier.aspectRatio(1F)
                            .weight(1F)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxSize()) {
                    ShimmerPlaceholder(
                        modifier = Modifier.weight(1F)
                            .aspectRatio(1F)
                            .fillMaxHeight()
                            .padding(vertical = 20.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    ShimmerPlaceholder(
                        modifier = Modifier.weight(1F)
                            .aspectRatio(1F)
                            .fillMaxHeight()
                            .padding(vertical = 20.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    ShimmerPlaceholder(
                        modifier = Modifier.weight(1F)
                            .fillMaxHeight()
                            .fillMaxWidth()
                    )
                }
            }
        } else {
            when {
                viewModel.loadError != null -> {
                    Spacer(
                        modifier = Modifier.fillMaxSize()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.error.copy(0.2F))
                    )
                }
                viewModel.plotBitmap != null && viewModel.dimPlotBitmap != null -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Image(
                                bitmap = viewModel.plotBitmap!!,
                                contentDescription = "Plot",
                                modifier = Modifier.border(
                                    width = 2.dp,
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.primary.copy(0.7F)
                                )
                                    .clip(MaterialTheme.shapes.medium)
                                    .aspectRatio(1F)
                                    .weight(1F)
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Column(modifier = Modifier.border(
                                width = 2.dp,
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.primary.copy(0.7F)
                            )
                                .clip(MaterialTheme.shapes.medium)
                                .aspectRatio(1F)
                                .weight(1F)
                            ) {
                                Image(
                                    bitmap = viewModel.dimPlotBitmap!!,
                                    contentDescription = "Clusters",
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.medium)
                                        .aspectRatio(1F)
                                        .weight(1F)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxSize()
                                .padding(top = 20.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Column(modifier = Modifier.fillMaxHeight()) {
                                VisualizationFragment(viewModel.selectedTimepoint)
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f, fill = true)
                                    .fillMaxHeight()
                                    .padding(start = 20.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(0.5F),
                                        shape = MaterialTheme.shapes.medium
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Cell cluster legend")
                            }
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Spacer(
                                modifier = Modifier.aspectRatio(1F)
                                    .weight(1F)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.primary.copy(0.5F))
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Spacer(
                                modifier = Modifier.aspectRatio(1F)
                                    .weight(1F)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.primary.copy(0.5F))
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxSize()) {
                            Spacer(
                                modifier = Modifier.weight(1F)
                                    .aspectRatio(1F)
                                    .fillMaxHeight()
                                    .padding(vertical = 20.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.primary.copy(0.5F))
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Spacer(
                                modifier = Modifier.weight(1F)
                                    .aspectRatio(1F)
                                    .fillMaxHeight()
                                    .padding(vertical = 20.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.primary.copy(0.5F))
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Spacer(
                                modifier = Modifier.weight(1F)
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.primary.copy(0.5F))
                            )
                        }
                    }
                }
            }
        }
    }
}