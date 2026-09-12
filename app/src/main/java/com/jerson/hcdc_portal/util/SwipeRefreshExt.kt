package com.jerson.hcdc_portal.util

import android.content.Context
import android.content.res.Configuration
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jerson.hcdc_portal.R

/**
 * Extension functions for standardizing SwipeRefreshLayout behavior and appearance
 * to match Material Design 3 and HCDC brand aesthetics.
 */
fun SwipeRefreshLayout.setupCustomTheme(context: Context) {
    // Set HCDC themed color scheme: Primary red, Secondary blue, Accent amber
    setColorSchemeColors(
        ContextCompat.getColor(context, R.color.md_theme_light_primary),
        ContextCompat.getColor(context, R.color.md_theme_light_secondary),
        ContextCompat.getColor(context, R.color.status_offline_text)
    )

    // Dark-mode aware progress background
    val isNightMode = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val bgColor = if (isNightMode) {
        ContextCompat.getColor(context, R.color.md_theme_dark_surfaceVariant)
    } else {
        ContextCompat.getColor(context, R.color.md_theme_light_surface)
    }
    setProgressBackgroundColorSchemeColor(bgColor)

    // Set comfortable trigger distance and offset for modern screens
    val density = context.resources.displayMetrics.density
    setDistanceToTriggerSync((100 * density).toInt())
}

/**
 * Safely stops refreshing animation with slight settle if needed
 */
fun SwipeRefreshLayout.stopRefreshing() {
    if (isRefreshing) {
        isRefreshing = false
    }
}
