/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * KSwitch.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.kizzy.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun KSwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    enable: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(250),
        label = "trackColor"
    )
    
    val thumbColor by animateColorAsState(
        targetValue = if (checked) 
            MaterialTheme.colorScheme.onPrimary 
        else 
            MaterialTheme.colorScheme.outline,
        animationSpec = tween(250),
        label = "thumbColor"
    )
    
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 32.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "thumbOffset"
    )
    
    val thumbSize by animateDpAsState(
        targetValue = if (checked) 22.dp else 18.dp,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "thumbSize"
    )

    Surface(
        modifier = modifier
            .requiredSize(58.dp, 30.dp)
            .alpha(if (enable) 1f else 0.5f),
        shape = CircleShape,
        color = trackColor,
        shadowElevation = if (checked) 2.dp else 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (onClick != null) 
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onClick() } 
                    else 
                        Modifier
                )
        ) {
            Surface(
                modifier = Modifier
                    .size(thumbSize)
                    .align(Alignment.CenterStart)
                    .offset(x = thumbOffset)
                    .shadow(if (checked) 4.dp else 1.dp, CircleShape),
                shape = CircleShape,
                color = thumbColor
            ) {}
        }
    }
}

@Preview
@Composable
fun KSwitchPreview() {
    var switchState by remember { mutableStateOf(true) }
    KSwitch(checked = switchState, enable = switchState, onClick = {
        switchState = !switchState
    })
}