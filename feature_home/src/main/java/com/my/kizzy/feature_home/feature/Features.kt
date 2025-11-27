/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * Features.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.feature_home.feature

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowMainAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.SizeMode
import com.my.kizzy.resources.R
import com.my.kizzy.ui.components.KSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Features(
    homeItems: List<HomeFeature> = emptyList(), onValueUpdate: (Int) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val featureSize = (LocalConfiguration.current.screenWidthDp.dp / 2)
    
    FlowRow(
        mainAxisSize = SizeMode.Expand,
        mainAxisAlignment = FlowMainAxisAlignment.SpaceBetween
    ) {
        for (i in homeItems.indices) {
            val backgroundColor by animateColorAsState(
                targetValue = if (homeItems[i].isChecked) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(300),
                label = "featureBg$i"
            )
            
            val contentColor by animateColorAsState(
                targetValue = if (homeItems[i].isChecked) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(300),
                label = "featureContent$i"
            )
            
            val iconColor by animateColorAsState(
                targetValue = if (homeItems[i].isChecked) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.secondary,
                animationSpec = tween(300),
                label = "featureIcon$i"
            )

            if (homeItems[i].tooltipText.isNotBlank()) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    state = rememberTooltipState(),
                    tooltip = {
                        RichTooltip(
                            title = {
                                Text(
                                    homeItems[i].title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            text = {
                                Text(homeItems[i].tooltipText)
                            },
                            action = {
                                TextButton(
                                    onClick = {
                                        uriHandler.openUri(homeItems[i].featureDocsLink)
                                    },
                                ) {
                                    Text(
                                        text = stringResource(R.string.learn_more),
                                    )
                                }
                            },
                        )
                    },
                ) {
                    FeatureCard(
                        modifier = Modifier.size(featureSize),
                        item = homeItems[i],
                        backgroundColor = backgroundColor,
                        contentColor = contentColor,
                        iconColor = iconColor,
                        onValueUpdate = { onValueUpdate(i) }
                    )
                }
            } else {
                FeatureCard(
                    modifier = Modifier.size(featureSize),
                    item = homeItems[i],
                    backgroundColor = backgroundColor,
                    contentColor = contentColor,
                    iconColor = iconColor,
                    onValueUpdate = { onValueUpdate(i) }
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    modifier: Modifier = Modifier,
    item: HomeFeature,
    backgroundColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    iconColor: androidx.compose.ui.graphics.Color,
    onValueUpdate: () -> Unit
) {
    Surface(
        modifier = modifier
            .padding(8.dp)
            .aspectRatio(1f)
            .clip(item.shape)
            .clickable { item.route?.let { item.onClick(it) } },
        shape = item.shape,
        color = backgroundColor,
        shadowElevation = if (item.isChecked) 6.dp else 2.dp,
        tonalElevation = if (item.isChecked) 4.dp else 1.dp
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                tint = iconColor,
                painter = painterResource(id = item.icon),
                contentDescription = item.title,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = contentColor,
                modifier = Modifier.weight(1.5f)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (item.showSwitch) {
                    Text(
                        text = if (item.isChecked) 
                            stringResource(id = R.string.android_on)
                        else 
                            stringResource(id = R.string.android_off),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = contentColor.copy(alpha = 0.8f)
                    )
                    KSwitch(
                        checked = item.isChecked,
                        modifier = Modifier.rotate(-90f),
                        onClick = {
                            item.onCheckedChange(!item.isChecked)
                            onValueUpdate()
                        }
                    )
                }
            }
        }
    }
}