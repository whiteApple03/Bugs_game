package com.example.myapplication

import android.widget.TextView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat

@Composable
fun RulesTab() {
    val context = LocalContext.current
    val htmlRules = context.getString(R.string.bug_game_rules_html)
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        AndroidView(
            factory = { ctx ->
                TextView(ctx).apply {
                    setTextColor(textColor)
                }
            },
            update = { textView ->
                textView.text = HtmlCompat.fromHtml(htmlRules, HtmlCompat.FROM_HTML_MODE_LEGACY)
                textView.setTextColor(textColor)
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
