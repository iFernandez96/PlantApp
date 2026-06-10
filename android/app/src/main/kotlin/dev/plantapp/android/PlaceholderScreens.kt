package dev.plantapp.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TodayPlaceholderScreen(modifier: Modifier = Modifier) {
    PlaceholderContent(
        headline = "Today",
        body = "Your care list is coming soon. For now, open a plant to see what it needs.",
        tag = "today_placeholder",
        modifier = modifier,
    )
}

@Composable
fun SpacesPlaceholderScreen(modifier: Modifier = Modifier) {
    PlaceholderContent(
        headline = "Spaces",
        body = "Browsing by balcony, backyard and windowsill is coming soon.",
        tag = "spaces_placeholder",
        modifier = modifier,
    )
}

@Composable
private fun PlaceholderContent(
    headline: String,
    body: String,
    tag: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp).testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}
