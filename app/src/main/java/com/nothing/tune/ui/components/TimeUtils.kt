package com.nothing.tune.ui.components

fun formatDuration(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0L)
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}
