package npo.kib.odc_demo.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import npo.kib.odc_demo.core.design_system.ui.ThemePreviews
import npo.kib.odc_demo.core.design_system.ui.theme.ODCAppTheme

@Composable
fun HistoryBlock(onHistoryClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(0.dp).clip(RoundedCornerShape(10))) {
        Surface(
            shape = RoundedCornerShape(10),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(10)
                )
                .clickable { onHistoryClick() }) {
        }
        Text("Click to see transaction history", modifier = Modifier.align(Alignment.Center))
    }
}

@ThemePreviews
@Composable
private fun P() {
    ODCAppTheme {
        HistoryBlock(onHistoryClick = {}, modifier = Modifier)
    }
}